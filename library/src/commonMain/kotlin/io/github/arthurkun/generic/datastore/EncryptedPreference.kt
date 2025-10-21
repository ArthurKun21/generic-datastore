package io.github.arthurkun.generic.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Encrypted preference wrapper that encrypts values before storing and decrypts when retrieving.
 *
 * This provides transparent encryption for sensitive data stored in preferences.
 *
 * Example usage:
 * ```kotlin
 * // Generate and store encryption key securely (only once)
 * val encryption = createPlatformEncryption("")
 * val key = encryption.generateKey()
 * // Store key securely (e.g., Android Keystore, macOS Keychain)
 *
 * // Create encrypted preference
 * val encryptedPref = datastore.encrypted(
 *     key = "api_token",
 *     defaultValue = "",
 *     encryptionKey = key
 * )
 *
 * // Use like normal preference - encryption is transparent
 * encryptedPref.set("sensitive-token-value")
 * val token = encryptedPref.get() // Automatically decrypted
 * ```
 *
 * @param T The type of the preference value (must be String for encryption)
 * @property basePreference The underlying unencrypted preference
 * @property encryption The encryption implementation to use
 */
class EncryptedPreference(
    private val basePreference: Prefs<String>,
    private val encryption: DataEncryption,
) : Prefs<String> {

    override suspend fun get(): String {
        return try {
            val encryptedValue = basePreference.get()
            if (encryptedValue.isEmpty()) {
                encryptedValue
            } else {
                encryption.decrypt(encryptedValue)
            }
        } catch (e: EncryptionException) {
            ConsoleLogger.error("Failed to decrypt preference", e)
            basePreference.defaultValue
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to get encrypted preference", e)
            basePreference.defaultValue
        }
    }

    override suspend fun set(value: String) {
        try {
            val encryptedValue = if (value.isEmpty()) {
                value
            } else {
                encryption.encrypt(value)
            }
            basePreference.set(encryptedValue)
        } catch (e: EncryptionException) {
            ConsoleLogger.error("Failed to encrypt preference", e)
            throw e
        } catch (e: Exception) {
            ConsoleLogger.error("Failed to set encrypted preference", e)
            throw e
        }
    }

    override suspend fun delete() {
        basePreference.delete()
    }

    override fun asFlow(): Flow<String> {
        return basePreference.asFlow().map { encryptedValue ->
            try {
                if (encryptedValue.isEmpty()) {
                    encryptedValue
                } else {
                    encryption.decrypt(encryptedValue)
                }
            } catch (e: Exception) {
                ConsoleLogger.error("Failed to decrypt flow value", e)
                basePreference.defaultValue
            }
        }
    }

    override fun key(): String = basePreference.key()

    override val defaultValue: String
        get() = basePreference.defaultValue

    override fun getValue(): String = basePreference.getValue()

    override fun setValue(value: String) = basePreference.setValue(value)

    override fun stateIn(scope: kotlinx.coroutines.CoroutineScope): kotlinx.coroutines.flow.StateFlow<String> =
        basePreference.stateIn(scope)

    override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String = getValue()

    override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: String) = setValue(value)

    override fun resetToDefault() = basePreference.resetToDefault()

    override fun invalidateCache() = basePreference.invalidateCache()
}

/**
 * Extension function to create an encrypted String preference.
 *
 * @param key The preference key
 * @param defaultValue The default unencrypted value
 * @param encryptionKey The encryption key (Base64 encoded). Should be stored securely.
 * @return An encrypted preference that transparently encrypts/decrypts values
 */
fun PreferenceDatastore.encrypted(
    key: String,
    defaultValue: String = "",
    encryptionKey: String,
): Prefs<String> {
    require(key.isNotBlank()) { "Preference key must not be blank" }
    require(encryptionKey.isNotBlank()) { "Encryption key must not be blank" }

    val basePreference = this.string(key, defaultValue)
    val encryption = createPlatformEncryption(encryptionKey)
    return EncryptedPreference(basePreference, encryption)
}

/**
 * Extension function to create an encrypted serialized preference.
 *
 * This encrypts the serialized string representation of custom objects.
 *
 * @param T The type of the custom object
 * @param key The preference key
 * @param defaultValue The default unencrypted value
 * @param encryptionKey The encryption key (Base64 encoded)
 * @param serializer Function to serialize the object to a String
 * @param deserializer Function to deserialize the String back to the object
 * @return An encrypted preference for custom objects
 */
fun <T> PreferenceDatastore.encryptedSerialized(
    key: String,
    defaultValue: T,
    encryptionKey: String,
    serializer: (T) -> String,
    deserializer: (String) -> T,
): Prefs<T> {
    require(key.isNotBlank()) { "Preference key must not be blank" }
    require(encryptionKey.isNotBlank()) { "Encryption key must not be blank" }

    // Create encrypted string preference
    val basePreference = this.string(key, "")
    val encryption = createPlatformEncryption(encryptionKey)
    val encryptedStringPref = EncryptedPreference(basePreference, encryption)

    // Wrap with serialization
    return object : Prefs<T> {
        override suspend fun get(): T {
            val serialized = encryptedStringPref.get()
            return if (serialized.isEmpty()) {
                defaultValue
            } else {
                try {
                    deserializer(serialized)
                } catch (e: Exception) {
                    ConsoleLogger.error("Failed to deserialize encrypted value", e)
                    defaultValue
                }
            }
        }

        override suspend fun set(value: T) {
            try {
                val serialized = serializer(value)
                encryptedStringPref.set(serialized)
            } catch (e: Exception) {
                ConsoleLogger.error("Failed to serialize value for encryption", e)
                throw e
            }
        }

        override suspend fun delete() {
            encryptedStringPref.delete()
        }

        override fun asFlow(): Flow<T> {
            return encryptedStringPref.asFlow().map { serialized ->
                if (serialized.isEmpty()) {
                    defaultValue
                } else {
                    try {
                        deserializer(serialized)
                    } catch (e: Exception) {
                        ConsoleLogger.error("Failed to deserialize encrypted flow value", e)
                        defaultValue
                    }
                }
            }
        }

        override fun key(): String = encryptedStringPref.key()
        override val defaultValue: T = defaultValue

        override fun getValue(): T = encryptedStringPref.getValue().let {
            if (it.isEmpty()) defaultValue else deserializer(it)
        }

        override fun setValue(value: T) {
            encryptedStringPref.setValue(serializer(value))
        }

        override fun stateIn(scope: kotlinx.coroutines.CoroutineScope): kotlinx.coroutines.flow.StateFlow<T> {
            return encryptedStringPref.asFlow().map { serialized ->
                if (serialized.isEmpty()) defaultValue else deserializer(serialized)
            }.stateIn(
                scope,
                SharingStarted.Eagerly,
                defaultValue,
            )
        }

        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T = getValue()
        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) = setValue(value)
        override fun resetToDefault() = encryptedStringPref.resetToDefault()
        override fun invalidateCache() = encryptedStringPref.invalidateCache()
    }
}
