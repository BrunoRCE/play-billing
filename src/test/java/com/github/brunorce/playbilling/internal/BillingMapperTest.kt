package com.github.brunorce.playbilling.internal

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class BillingMapperTest {

    @Test
    fun `mapPurchase maps all fields correctly`() {
        val purchase = mockk<Purchase> {
            every { orderId } returns "order_123"
            every { products } returns listOf("p1", "p2")
            every { purchaseToken } returns "token_abc"
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns true
            every { quantity } returns 1
            every { purchaseTime } returns 1000L
        }

        val result = BillingMapper.mapPurchase(purchase)

        assertEquals("order_123", result.orderId)
        assertEquals(listOf("p1", "p2"), result.products)
        assertEquals("token_abc", result.purchaseToken)
        assertEquals(1, result.purchaseState) // PURCHASED = 1
        assertEquals(true, result.isAcknowledged)
        assertEquals(1, result.quantity)
        assertEquals(1000L, result.purchaseTime)
    }

    @Test
    fun `mapProduct maps subscription correctly with offers`() {
        val pricingPhase = mockk<ProductDetails.PricingPhase> {
            every { formattedPrice } returns "$9.99"
            every { priceAmountMicros } returns 9990000L
            every { priceCurrencyCode } returns "USD"
        }
        
        val pricingPhases = mockk<ProductDetails.PricingPhases> {
            every { pricingPhaseList } returns listOf(pricingPhase)
        }

        val offerDetails = mockk<ProductDetails.SubscriptionOfferDetails> {
            every { offerToken } returns "offer_token"
            every { basePlanId } returns "base_plan"
            every { offerId } returns "offer_id"
            every { this@mockk.pricingPhases } returns pricingPhases
        }

        val productDetails = mockk<ProductDetails> {
            every { productId } returns "sub_1"
            every { title } returns "Premium Subscription"
            every { name } returns "Premium"
            every { description } returns "Get all features"
            every { productType } returns "subs"
            every { subscriptionOfferDetails } returns listOf(offerDetails)
        }

        val result = BillingMapper.mapProduct(productDetails)

        assertEquals("sub_1", result.productId)
        assertEquals("Premium Subscription", result.title)
        assertEquals("subs", result.type)
        assertEquals(1, result.offers.size)
        
        val offer = result.offers.first()
        assertEquals("offer_token", offer.offerToken)
        assertEquals("$9.99", offer.formattedPrice)
        assertEquals("USD", offer.currencyCode)
    }
}
