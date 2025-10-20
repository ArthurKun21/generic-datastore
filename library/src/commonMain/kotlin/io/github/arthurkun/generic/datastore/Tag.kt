package io.github.arthurkun.generic.datastore

internal const val TAG = "GenericDataStore"

/**
 * Logger interface for handling log messages.
 * This abstraction allows for different logging implementations
 * and prevents sensitive data exposure in production builds.
 */
interface Logger {
    fun log(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * Default logger implementation that prints to console.
 * In production, this should be replaced with a proper logging framework.
 */
object ConsoleLogger : Logger {
    override fun log(message: String) {
        println("$TAG: $message")
    }

    override fun error(message: String, throwable: Throwable?) {
        println("$TAG ERROR: $message ${throwable?.message ?: ""}")
    }
}
