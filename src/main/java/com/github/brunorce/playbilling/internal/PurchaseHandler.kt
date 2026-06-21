package com.github.brunorce.playbilling.internal

import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PurchaseHandler(
    private val billingClientProvider: () -> BillingClient,
    private val scope: CoroutineScope,
    private val logger: BillingLogger,
    private val onPurchasesChanged: () -> Unit
) : PurchasesUpdatedListener {

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases = _purchases.asStateFlow()

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                var needsRefresh = false
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            if (acknowledgePurchase(purchase)) needsRefresh = true
                        } else {
                            needsRefresh = true
                        }
                    }
                }
                if (needsRefresh) refreshPurchases()
            }
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            logger.e("onPurchasesUpdated error: ${result.debugMessage}")
        }
    }

    suspend fun refreshPurchases() {
        val subs = scope.async { getPurchasesList(BillingClient.ProductType.SUBS) }
        val inApp = scope.async { getPurchasesList(BillingClient.ProductType.INAPP) }
        val all = subs.await() + inApp.await()
        _purchases.update { all }
        onPurchasesChanged()
    }

    private suspend fun getPurchasesList(type: String): List<Purchase> {
        val client = billingClientProvider()
        if (!client.isReady) return emptyList()
        val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
        val result = client.queryPurchasesAsync(params)
        return if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.purchasesList
        } else {
            emptyList()
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase): Boolean {
        val client = billingClientProvider()
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val result = client.acknowledgePurchase(params)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        offerToken: String?
    ) {
        val client = billingClientProvider()
        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val token = offerToken ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (token != null) params.setOfferToken(token)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(params.build()))
            .build()

        client.launchBillingFlow(activity, flowParams)
    }
}
