package io.github.arthurkun.generic.datastore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a single preference item.
 *
 * This interface defines the core functionalities for a preference,
 * including getting and setting its value, deleting it, accessing its default value,
 * and observing its changes as a Flow or StateFlow.
 *
 * @param T The type of the preference value.
 */
interface Preference<T> {

    /**
     * Returns the key of the preference.
     */
    fun key(): String

    /**
     * Gets the current value of the preference.
     *
     * @return The current preference value.
     */
    suspend fun get(): T

    /**
     * Sets the value of the preference.
     *
     * @param value The new value for the preference.
     */
    suspend fun set(value: T)

    /**
     * Atomically gets the current value of the preference and sets it to a new value
     * computed by the provided [transform] function.
     *
     * @param transform A function that takes the current value and returns the new value.
     */
    suspend fun getAndSet(transform: (T) -> T)

    /**
     * Deletes the preference from the underlying storage.
     */
    suspend fun delete()

    /**
     * Returns the default value of the preference.
     */
    val defaultValue: T

    /**
     * Returns a [Flow] that emits the preference value whenever it changes.
     *
     * @return A [Flow] of the preference value.
     */
    fun asFlow(): Flow<T>

    /**
     * Converts the preference [Flow] into a [StateFlow].
     *
     * @param scope The [CoroutineScope] to use for the [StateFlow].
     * @return A [StateFlow] of the preference value.
     */
    fun stateIn(scope: CoroutineScope): StateFlow<T>

    /**
     * Gets the current value of the preference.
     * This is a synchronous alternative to [get] for use cases where suspension is not possible
     * or desired, such as property delegation.
     * Note: This might block the calling thread if the underlying DataStore operation is slow.
     *
     * @return The current preference value.
     */
    fun getValue(): T

    /**
     * Sets the value of the preference.
     * This is a synchronous alternative to [set] for use cases where suspension is not possible
     * or desired, such as property delegation.
     * Note: This might block the calling thread if the underlying DataStore operation is slow.
     *
     * @param value The new value for the preference.
     */
    fun setValue(value: T)

    /**
     * Companion object for [Preference] related utility functions.
     */
    companion object {
        /**
         * Checks if a preference key is marked as private.
         * Private preferences should not be exposed in backups without user consent.
         *
         * @param key The preference key to check.
         * @return `true` if the key starts with the private prefix, `false` otherwise.
         */
        @Suppress("unused")
        fun isPrivate(key: String): Boolean {
            return key.startsWith(PRIVATE_PREFIX)
        }

        /**
         * Prepends the private prefix to a key.
         *
         * @param key The original key.
         * @return The key with the private prefix.
         */
        @Suppress("unused")
        fun privateKey(key: String): String {
            return "$PRIVATE_PREFIX$key"
        }

        /**
         * Checks if a preference key is marked as app state.
         * App state preferences are used for internal app state and should not be in backups.
         *
         * @param key The preference key to check.
         * @return `true` if the key starts with the app state prefix, `false` otherwise.
         */
        @Suppress("unused")
        fun isAppState(key: String): Boolean {
            return key.startsWith(APP_STATE_PREFIX)
        }

        /**
         * Prepends the app state prefix to a key.
         *
         * @param key The original key.
         * @return The key with the app state prefix.
         */
        @Suppress("unused")
        fun appStateKey(key: String): String = "$APP_STATE_PREFIX$key"

        private const val APP_STATE_PREFIX = "__APP_STATE_"
        private const val PRIVATE_PREFIX = "__PRIVATE_"
    }
}
