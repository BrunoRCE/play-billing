package com.github.brunorce.playbilling.model

data class BillingPurchase(
    val orderId: String?,
    val products: List<String>,
    val purchaseToken: String,
    val purchaseState: Int,
    val isAcknowledged: Boolean,
    val quantity: Int,
    val purchaseTime: Long
) {
    companion object {
        const val STATE_UNSPECIFIED = 0
        const val STATE_PURCHASED = 1
        const val STATE_PENDING = 2
    }
}
