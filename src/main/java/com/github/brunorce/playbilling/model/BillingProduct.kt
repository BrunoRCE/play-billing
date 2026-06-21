package com.github.brunorce.playbilling.model

data class BillingProduct(
    val productId: String,
    val title: String,
    val name: String,
    val description: String,
    val type: String,
    val offers: List<BillingOffer> = emptyList()
)
