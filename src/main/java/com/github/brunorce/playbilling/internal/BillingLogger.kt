package com.github.brunorce.playbilling.internal

import android.util.Log

internal class BillingLogger(private val enabled: Boolean) {
    private val tag = "PlayBillingWrapper"

    fun d(message: String) {
        if (enabled) Log.d(tag, message)
    }

    fun i(message: String) {
        if (enabled) Log.i(tag, message)
    }

    fun w(message: String) {
        if (enabled) Log.w(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (enabled) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
}
