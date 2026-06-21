package com.github.brunorce.playbilling.model

data class BillingOffer(
    val offerToken: String,
    val basePlanId: String,
    val offerId: String?,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val currencyCode: String
)
