package com.github.brunorce.playbilling.internal

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BillingConnectionManagerTest {

    private lateinit var billingClient: BillingClient
    private lateinit var logger: BillingLogger
    private var onConnectedCalled = false
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var connectionManager: BillingConnectionManager

    @Before
    fun setup() {
        billingClient = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        onConnectedCalled = false
        connectionManager = BillingConnectionManager(
            { billingClient },
            testScope,
            logger,
            { onConnectedCalled = true }
        )
    }

    @Test
    fun `connect starts connection when client is not ready`() {
        every { billingClient.isReady } returns false
        
        connectionManager.connect()
        
        verify { billingClient.startConnection(connectionManager) }
    }

    @Test
    fun `onBillingSetupFinished OK updates isReady and calls onConnected`() {
        val result = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.OK
        }
        
        connectionManager.onBillingSetupFinished(result)
        
        assertTrue(connectionManager.isReady.value)
        assertTrue(onConnectedCalled)
    }

    @Test
    fun `onBillingSetupFinished error updates isReady to false`() {
        val result = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.ERROR
            every { debugMessage } returns "error"
        }
        
        connectionManager.onBillingSetupFinished(result)
        
        assertFalse(connectionManager.isReady.value)
        assertFalse(onConnectedCalled)
    }

    @Test
    fun `onBillingServiceDisconnected retries connection`() = runTest(testDispatcher) {
        every { billingClient.isReady } returns false
        
        connectionManager.onBillingServiceDisconnected()
        
        assertFalse(connectionManager.isReady.value)
        
        // Advance time by RETRY_DELAY_MS (2000L)
        advanceTimeBy(2001)
        
        verify(exactly = 1) { billingClient.startConnection(any()) }
    }
}
