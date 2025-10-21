package io.github.arthurkun.generic.datastore

internal const val TAG = "GenericDataStore"

/**
 * Logger interface for handling log messages.
 *
 * This abstraction allows for different logging implementations
 * and prevents sensitive data exposure in production builds.
 *
 * To use a custom logger implementation:
 * ```kotlin
 * object CustomLogger : Logger {
 *     override fun log(message: String) {
 *         // Custom implementation, e.g., Timber
 *     }
 *     override fun error(message: String, throwable: Throwable?) {
 *         // Custom error logging
 *     }
 * }
 * ```
 *
 * @since 1.0.0
 */
interface Logger {
    /**
     * Logs an informational message.
     *
     * @param message The message to log
     */
    fun log(message: String)

    /**
     * Logs an error message with optional throwable.
     *
     * @param message The error message to log
     * @param throwable Optional throwable for stack trace
     */
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * Default logger implementation that prints to console.
 *
 * In production, this should be replaced with a proper logging framework
 * like Timber, Logcat, or a custom implementation that respects privacy.
 *
 * Example production usage:
 * ```kotlin
 * // In your Application class or initialization code
 * object ProductionLogger : Logger {
 *     override fun log(message: String) {
 *         if (BuildConfig.DEBUG) {
 *             Log.d(TAG, message)
 *         }
 *     }
 *     override fun error(message: String, throwable: Throwable?) {
 *         // Log to crash reporting service
 *         FirebaseCrashlytics.getInstance().log(message)
 *         throwable?.let { FirebaseCrashlytics.getInstance().recordException(it) }
 *     }
 * }
 * ```
 *
 * @since 1.0.0
 */
object ConsoleLogger : Logger {
    override fun log(message: String) {
        println("$TAG: $message")
    }

    override fun error(message: String, throwable: Throwable?) {
        println("$TAG ERROR: $message ${throwable?.message ?: ""}")
    }
}
