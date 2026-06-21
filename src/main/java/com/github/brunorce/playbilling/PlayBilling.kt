package com.github.brunorce.playbilling

import android.app.Activity
import android.content.Context
import com.github.brunorce.playbilling.internal.BillingRepositoryImpl
import com.github.brunorce.playbilling.model.BillingProduct
import com.github.brunorce.playbilling.model.BillingPurchase
import kotlinx.coroutines.flow.Flow

interface PlayBilling {
    val purchases: Flow<List<BillingPurchase>>
    val isReady: Flow<Boolean>

    fun connect()
    fun refreshPurchases()
    suspend fun getProducts(
        productIds: List<String>,
        type: String
    ): List<BillingProduct>

    fun launchPurchase(
        activity: Activity,
        productId: String,
        type: String,
        offerToken: String? = null
    )

    fun destroy()

    companion object {
        fun create(context: Context, debugEnabled: Boolean = false): PlayBilling {
            return BillingRepositoryImpl(context, debugEnabled)
        }
    }
}
