package io.github.arthurkun.generic.datastore.proto.app.ui.proto3

/**
 * UI state for the Proto3 (AppConfig) screen.
 *
 * Mirrors the flat + nested structure of AppConfig, including the
 * deeply nested NetworkConfig.RetryPolicy fields.
 */
data class Proto3ScreenState(
    val appName: String = "",
    val maxRetries: Int = 0,
    val debugMode: Boolean = false,
    val refreshInterval: Double = 0.0,
    val baseUrl: String = "",
    val timeoutMs: Int = 0,
    val maxAttempts: Int = 0,
    val backoffMs: Long = 0L,
    val exponential: Boolean = false,
)
