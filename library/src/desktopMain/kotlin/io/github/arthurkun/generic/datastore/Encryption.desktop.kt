package io.github.arthurkun.generic.datastore

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop/JVM implementation of DataEncryption using AES-GCM.
 *
 * AES-GCM provides authenticated encryption which ensures both
 * confidentiality and integrity of the data.
 *
 * @param key The encryption key (Base64 encoded)
 */
class DesktopAESEncryption(private val key: String) : DataEncryption {
    private val transformation = "AES/GCM/NoPadding"
    private val keyAlgorithm = "AES"
    private val gcmTagLength = 128
    private val ivLength = 12 // 96 bits recommended for GCM

    private val secretKey: SecretKey by lazy {
        try {
            val decodedKey = Base64.getDecoder().decode(key)
            SecretKeySpec(decodedKey, keyAlgorithm)
        } catch (e: Exception) {
            throw EncryptionException("Failed to decode encryption key", e)
        }
    }

    override suspend fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Combine IV and encrypted data
            val combined = iv + encryptedBytes
            Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            ConsoleLogger.error("Encryption failed", e)
            throw EncryptionException("Failed to encrypt data", e)
        }
    }

    override suspend fun decrypt(encryptedText: String): String {
        return try {
            val combined = Base64.getDecoder().decode(encryptedText)

            // Extract IV and encrypted data
            val iv = combined.sliceArray(0 until ivLength)
            val encryptedBytes = combined.sliceArray(ivLength until combined.size)

            val cipher = Cipher.getInstance(transformation)
            val gcmParameterSpec = GCMParameterSpec(gcmTagLength, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            ConsoleLogger.error("Decryption failed", e)
            throw EncryptionException("Failed to decrypt data", e)
        }
    }

    override suspend fun generateKey(): String {
        return try {
            val keyGenerator = KeyGenerator.getInstance(keyAlgorithm)
            keyGenerator.init(256) // AES-256
            val key = keyGenerator.generateKey()
            Base64.getEncoder().encodeToString(key.encoded)
        } catch (e: Exception) {
            throw EncryptionException("Failed to generate key", e)
        }
    }
}

/**
 * Creates Desktop-specific encryption implementation.
 */
actual fun createPlatformEncryption(key: String): DataEncryption {
    return if (key.isBlank()) {
        NoOpEncryption()
    } else {
        DesktopAESEncryption(key)
    }
}
