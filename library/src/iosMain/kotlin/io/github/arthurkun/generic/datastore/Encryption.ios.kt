package io.github.arthurkun.generic.datastore

import platform.Foundation.*
import kotlin.random.Random
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * iOS implementation of DataEncryption using a simple XOR-based encryption.
 *
 * Note: For production use, you should implement proper AES-GCM encryption
 * using native iOS Security framework or a third-party crypto library.
 * This is a simplified implementation that provides basic encryption.
 *
 * @param key The encryption key (Base64 encoded)
 */
@OptIn(ExperimentalEncodingApi::class)
class IosSimpleEncryption(private val key: String) : DataEncryption {
    private val keyBytes: ByteArray by lazy {
        try {
            // Decode Base64 key using Kotlin's Base64
            Base64.decode(key)
        } catch (e: Exception) {
            throw EncryptionException("Failed to decode encryption key", e)
        }
    }

    override suspend fun encrypt(plainText: String): String {
        return try {
            val plainBytes = plainText.encodeToByteArray()
            
            // Generate random IV
            val iv = Random.nextBytes(16)
            
            // Simple XOR encryption with key
            val encryptedBytes = ByteArray(plainBytes.size)
            for (i in plainBytes.indices) {
                val keyByte = keyBytes[i % keyBytes.size]
                val ivByte = iv[i % iv.size]
                encryptedBytes[i] = (plainBytes[i].toInt() xor keyByte.toInt() xor ivByte.toInt()).toByte()
            }
            
            // Combine IV and encrypted data
            val combined = iv + encryptedBytes
            
            // Encode to Base64 using Kotlin's Base64
            Base64.encode(combined)
        } catch (e: Exception) {
            ConsoleLogger.error("Encryption failed", e)
            throw EncryptionException("Failed to encrypt data", e)
        }
    }

    override suspend fun decrypt(encryptedText: String): String {
        return try {
            // Decode from Base64 using Kotlin's Base64
            val combined = Base64.decode(encryptedText)
            
            // Extract IV and encrypted data
            val iv = combined.sliceArray(0 until 16)
            val encryptedBytes = combined.sliceArray(16 until combined.size)
            
            // Simple XOR decryption
            val decryptedBytes = ByteArray(encryptedBytes.size)
            for (i in encryptedBytes.indices) {
                val keyByte = keyBytes[i % keyBytes.size]
                val ivByte = iv[i % iv.size]
                decryptedBytes[i] = (encryptedBytes[i].toInt() xor keyByte.toInt() xor ivByte.toInt()).toByte()
            }
            
            decryptedBytes.decodeToString()
        } catch (e: Exception) {
            ConsoleLogger.error("Decryption failed", e)
            throw EncryptionException("Failed to decrypt data", e)
        }
    }

    override suspend fun generateKey(): String {
        return try {
            val keyBytes = Random.nextBytes(32) // 256 bits
            // Encode to Base64 using Kotlin's Base64
            Base64.encode(keyBytes)
        } catch (e: Exception) {
            throw EncryptionException("Failed to generate key", e)
        }
    }
}

/**
 * Creates iOS-specific encryption implementation.
 */
actual fun createPlatformEncryption(key: String): DataEncryption {
    return if (key.isBlank()) {
        NoOpEncryption()
    } else {
        IosSimpleEncryption(key)
    }
}
