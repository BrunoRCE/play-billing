package com.github.brunorce.playbilling.internal

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.github.brunorce.playbilling.PlayBilling
import com.github.brunorce.playbilling.model.BillingProduct
import com.github.brunorce.playbilling.model.BillingPurchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class BillingRepositoryImpl(
    context: Context,
    debugEnabled: Boolean = false
) : PlayBilling {

    private val logger = BillingLogger(debugEnabled)
    private val repositoryJob = SupervisorJob()
    private val scope = CoroutineScope(repositoryJob + Dispatchers.Main.immediate)

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context.applicationContext)
            .setListener { result, purchases ->
                purchaseHandler.onPurchasesUpdated(result, purchases)
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
    }

    private val billingClientProvider: () -> BillingClient = { billingClient }

    private val purchaseHandler by lazy {
        PurchaseHandler(
            billingClientProvider = billingClientProvider,
            scope = scope,
            logger = logger,
            onPurchasesChanged = { }
        )
    }

    private val connectionManager by lazy {
        BillingConnectionManager(
            billingClientProvider = billingClientProvider,
            scope = scope,
            logger = logger,
            onConnected = { refreshPurchases() }
        )
    }

    private val productDataSource by lazy {
        ProductDataSource(
            billingClientProvider = billingClientProvider,
            logger = logger
        )
    }

    override val purchases: Flow<List<BillingPurchase>> = purchaseHandler.purchases
        .map { list -> list.map { BillingMapper.mapPurchase(it) } }

    override val isReady: Flow<Boolean> = connectionManager.isReady

    override fun connect() {
        connectionManager.connect()
    }

    override fun refreshPurchases() {
        scope.launch { purchaseHandler.refreshPurchases() }
    }

    override suspend fun getProducts(
        productIds: List<String>,
        type: String
    ): List<BillingProduct> {
        return productDataSource.fetchProducts(productIds, type)
            .map { BillingMapper.mapProduct(it) }
    }

    override fun launchPurchase(
        activity: Activity,
        productId: String,
        type: String,
        offerToken: String?
    ) {
        val cached = productDataSource.getCachedProduct(productId)
        if (cached != null) {
            purchaseHandler.launchBillingFlow(activity, cached, offerToken)
        } else {
            scope.launch {
                productDataSource.fetchProducts(listOf(productId), type)
                productDataSource.getCachedProduct(productId)?.let {
                    purchaseHandler.launchBillingFlow(activity, it, offerToken)
                }
            }
        }
    }

    override fun destroy() {
        repositoryJob.cancelChildren()
        productDataSource.clearCache()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
