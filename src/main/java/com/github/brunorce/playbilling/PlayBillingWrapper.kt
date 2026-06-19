package com.github.brunorce.playbilling

import android.app.Activity
import android.content.Context
import com.github.brunorce.playbilling.utils.BillingLogger
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PlayBillingWrapper(
    context: Context,
    debugEnabled: Boolean = false
) : PurchasesUpdatedListener {

    private val applicationContext = context.applicationContext
    private val logger = BillingLogger(debugEnabled)

    private val wrapperJob = SupervisorJob()
    private val scope = CoroutineScope(wrapperJob + Dispatchers.Main.immediate)

    companion object {
        private const val MAX_CONNECTION_RETRIES = 5
        private const val RETRY_DELAY_MS = 2000L
    }

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: Flow<List<Purchase>> = _purchases.asStateFlow()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: Flow<Boolean> = _isBillingReady.asStateFlow()

    @Volatile
    private var isConnecting = false
    private var connectionRetries = 0

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(applicationContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
    }

    init {
        connect()
    }

    fun connect() {
        if (billingClient.isReady || isConnecting) return

        logger.i("Connecting to BillingClient...")
        isConnecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                isConnecting = false
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    logger.i("BillingClient connected successfully")
                    connectionRetries = 0
                    _isBillingReady.value = true
                    refreshPurchases()
                } else {
                    logger.e("BillingClient connection failed: ${result.debugMessage} (code: ${result.responseCode})")
                    _isBillingReady.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
                _isBillingReady.value = false
                logger.w("BillingClient disconnected. Retry attempt: $connectionRetries")

                if (connectionRetries < MAX_CONNECTION_RETRIES) {
                    connectionRetries++
                    scope.launch {
                        delay(RETRY_DELAY_MS.milliseconds)
                        connect()
                    }
                }
            }
        })
    }

    /**
     * Sincroniza todas las compras (suscripciones e in-app) en paralelo
     */
    fun refreshPurchases() {
        scope.launch {
            logger.i("Refreshing purchases...")
            val subsJob = async { getPurchasesList(BillingClient.ProductType.SUBS) }
            val inAppJob = async { getPurchasesList(BillingClient.ProductType.INAPP) }

            val allPurchases = subsJob.await() + inAppJob.await()
            logger.i("Purchases refreshed: ${allPurchases.size} items found")
            _purchases.update { allPurchases }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        logger.i("onPurchasesUpdated: ${billingResult.responseCode}, purchases size: ${purchases?.size ?: 0}")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                var needsRefresh = false
                purchases.forEach { purchase ->
                    logger.i("Purchase found: ${purchase.products} - State: ${purchase.purchaseState}")
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            val success = acknowledgePurchase(purchase)
                            if (success) needsRefresh = true
                        } else {
                            needsRefresh = true
                        }
                    }
                }
                if (needsRefresh) refreshPurchases()
            }
        } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            logger.e("onPurchasesUpdated error: ${billingResult.debugMessage} (code: ${billingResult.responseCode})")
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase): Boolean {
        if (!billingClient.isReady) {
            logger.e("acknowledgePurchase failed: BillingClient not ready")
            connect()
            return false
        }

        logger.i("Acknowledging purchase: ${purchase.products}")
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val billingResult = billingClient.acknowledgePurchase(params)
        val success = billingResult.responseCode == BillingClient.BillingResponseCode.OK
        if (success) {
            logger.i("Acknowledge success for ${purchase.products}")
        } else {
            logger.e("Acknowledge failed: ${billingResult.debugMessage} (code: ${billingResult.responseCode})")
        }
        return success
    }

    private suspend fun getPurchasesList(productType: String): List<Purchase> {
        if (!billingClient.isReady) {
            logger.w("getPurchases failed: BillingClient not ready")
            connect()
            return emptyList()
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        return if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            logger.d("getPurchases success for $productType: ${result.purchasesList.size} items")
            result.purchasesList
        } else {
            logger.e("getPurchases error for $productType: ${result.billingResult.debugMessage}")
            emptyList()
        }
    }

    suspend fun getProducts(
        productType: String,
        productIds: List<String>
    ): List<ProductDetails> {
        if (!billingClient.isReady) {
            logger.w("getProducts failed: BillingClient not ready")
            connect()
            return emptyList()
        }

        logger.i("Querying products: $productIds ($productType)")
        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)
        return if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            logger.i("getProducts success: ${result.productDetailsList?.size ?: 0} products found")
            result.productDetailsList ?: emptyList()
        } else {
            logger.e("getProducts error: ${result.billingResult.debugMessage}")
            emptyList()
        }
    }

    /**
     * Lanza el flujo de compra. Se añade [selectedOfferToken] opcional para soportar
     * múltiples ofertas/precios de una misma suscripción.
     */
    fun launchPurchase(
        productDetails: ProductDetails,
        activity: Activity,
        selectedOfferToken: String? = null
    ) {
        if (!billingClient.isReady) {
            logger.e("launchPurchase failed: BillingClient not ready")
            connect()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            logger.e("launchPurchase failed: Invalid Activity state")
            return
        }

        logger.i("Launching purchase flow for: ${productDetails.productId}")
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerToken = selectedOfferToken
                ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

            if (offerToken != null) {
                productDetailsParams.setOfferToken(offerToken)
            } else {
                logger.e("launchPurchase failed: No offer token found for subscription")
                return
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams.build()))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    /**
     * Cancela las operaciones en curso y cierra la conexión de forma segura.
     */
    fun destroy() {
        logger.i("Destroying PlayBillingWrapper y liberando recursos")
        wrapperJob.cancelChildren()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
        _isBillingReady.value = false
    }
}