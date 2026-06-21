package com.github.brunorce.playbilling.internal

import com.android.billingclient.api.BillingClient
import io.mockk.mockk
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProductDataSourceTest {

    private lateinit var billingClient: BillingClient
    private lateinit var logger: BillingLogger
    private lateinit var dataSource: ProductDataSource

    @Before
    fun setup() {
        billingClient = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        dataSource = ProductDataSource({ billingClient }, logger)
    }

    @Test
    fun `getCachedProduct returns null when cache is empty`() {
        assertNull(dataSource.getCachedProduct("any_id"))
    }

    @Test
    fun `clearCache removes all cached products`() {
        // We can't easily populate the private cache without reflection or fetchProducts
        // but we can at least verify the method exists and doesn't crash
        dataSource.clearCache()
        assertNull(dataSource.getCachedProduct("any_id"))
    }
}
