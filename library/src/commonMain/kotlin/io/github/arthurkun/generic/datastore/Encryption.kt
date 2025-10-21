package io.github.arthurkun.generic.datastore

/**
 * Interface for encrypting and decrypting data.
 *
 * Implementations should provide platform-specific encryption mechanisms
 * that are secure and appropriate for the target platform.
 */
interface DataEncryption {
    /**
     * Encrypts the given plain text.
     *
     * @param plainText The text to encrypt
     * @return The encrypted text (Base64 encoded)
     * @throws EncryptionException if encryption fails
     */
    suspend fun encrypt(plainText: String): String

    /**
     * Decrypts the given encrypted text.
     *
     * @param encryptedText The text to decrypt (Base64 encoded)
     * @return The decrypted plain text
     * @throws EncryptionException if decryption fails
     */
    suspend fun decrypt(encryptedText: String): String

    /**
     * Generates a new encryption key.
     * This should be called once and the key stored securely.
     *
     * @return The generated key (Base64 encoded)
     */
    suspend fun generateKey(): String
}

/**
 * Exception thrown when encryption or decryption fails.
 */
class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * No-op implementation that doesn't encrypt/decrypt.
 * Used as default when encryption is disabled.
 */
class NoOpEncryption : DataEncryption {
    override suspend fun encrypt(plainText: String): String = plainText
    override suspend fun decrypt(encryptedText: String): String = encryptedText
    override suspend fun generateKey(): String = ""
}

/**
 * Expect function to get platform-specific encryption implementation.
 * Each platform (Android, Desktop, iOS) provides its own secure implementation.
 */
expect fun createPlatformEncryption(key: String): DataEncryption
