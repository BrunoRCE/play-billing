package com.github.brunorce.playbilling.internal

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class BillingConnectionManager(
    private val billingClientProvider: () -> BillingClient,
    private val scope: CoroutineScope,
    private val logger: BillingLogger,
    private val onConnected: () -> Unit
) : BillingClientStateListener {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    @Volatile
    private var isConnecting = false
    private var connectionRetries = 0

    companion object {
        private const val MAX_CONNECTION_RETRIES = 5
        private const val RETRY_DELAY_MS = 2000L
    }

    fun connect() {
        val client = billingClientProvider()
        if (client.isReady || isConnecting) return

        logger.i("Connecting to BillingClient...")
        isConnecting = true
        client.startConnection(this)
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        isConnecting = false
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            logger.i("BillingClient connected successfully")
            connectionRetries = 0
            _isReady.value = true
            onConnected()
        } else {
            logger.e("BillingClient connection failed: ${result.debugMessage} (code: ${result.responseCode})")
            _isReady.value = false
        }
    }

    override fun onBillingServiceDisconnected() {
        isConnecting = false
        _isReady.value = false
        logger.w("BillingClient disconnected. Retry attempt: $connectionRetries")

        if (connectionRetries < MAX_CONNECTION_RETRIES) {
            connectionRetries++
            scope.launch {
                delay(RETRY_DELAY_MS.milliseconds)
                connect()
            }
        }
    }
}
