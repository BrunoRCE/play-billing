package com.github.brunorce.playbilling.internal

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.github.brunorce.playbilling.model.BillingOffer
import com.github.brunorce.playbilling.model.BillingProduct
import com.github.brunorce.playbilling.model.BillingPurchase

internal object BillingMapper {

    fun mapPurchase(purchase: Purchase): BillingPurchase = BillingPurchase(
        orderId = purchase.orderId,
        products = purchase.products,
        purchaseToken = purchase.purchaseToken,
        purchaseState = purchase.purchaseState,
        isAcknowledged = purchase.isAcknowledged,
        quantity = purchase.quantity,
        purchaseTime = purchase.purchaseTime
    )

    fun mapProduct(productDetails: ProductDetails): BillingProduct {
        val offers = productDetails.subscriptionOfferDetails?.map { offer ->
            val pricing = offer.pricingPhases.pricingPhaseList.first()
            BillingOffer(
                offerToken = offer.offerToken,
                basePlanId = offer.basePlanId,
                offerId = offer.offerId,
                formattedPrice = pricing.formattedPrice,
                priceAmountMicros = pricing.priceAmountMicros,
                currencyCode = pricing.priceCurrencyCode
            )
        } ?: emptyList()

        return BillingProduct(
            productId = productDetails.productId,
            title = productDetails.title,
            name = productDetails.name,
            description = productDetails.description,
            type = productDetails.productType,
            offers = offers
        )
    }
}
