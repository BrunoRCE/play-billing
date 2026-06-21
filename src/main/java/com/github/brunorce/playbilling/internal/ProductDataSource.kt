package com.github.brunorce.playbilling.internal

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails

internal class ProductDataSource(
    private val billingClientProvider: () -> BillingClient,
    private val logger: BillingLogger
) {
    private val productDetailsCache = mutableMapOf<String, ProductDetails>()

    fun getCachedProduct(productId: String): ProductDetails? = productDetailsCache[productId]

    fun clearCache() {
        productDetailsCache.clear()
    }

    suspend fun fetchProducts(
        productIds: List<String>,
        productType: String
    ): List<ProductDetails> {
        val client = billingClientProvider()
        if (!client.isReady) {
            logger.e("fetchProducts failed: BillingClient not ready")
            return emptyList()
        }

        val productList = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = client.queryProductDetails(params)
        return if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val details = result.productDetailsList ?: emptyList()
            details.forEach { productDetailsCache[it.productId] = it }
            details
        } else {
            logger.e("fetchProducts error: ${result.billingResult.debugMessage}")
            emptyList()
        }
    }
}
