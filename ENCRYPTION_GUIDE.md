# Encryption Support Guide

## Overview

The encryption system provides transparent encryption for sensitive data stored in preferences using AES-256-GCM encryption. The implementation is Kotlin Multiplatform compatible with platform-specific implementations for Android and Desktop/JVM.

## Key Features

- **AES-256-GCM Encryption**: Industry-standard authenticated encryption
- **Transparent Encryption**: Automatically encrypts on write, decrypts on read
- **Platform-Specific**: Secure implementations for each platform
- **Type-Safe**: Full Kotlin type safety with compile-time checks
- **Easy to Use**: Simple API that integrates seamlessly with existing preferences

## Platform Support

### Android
- Uses `javax.crypto` with AES/GCM/NoPadding
- Compatible with Android API 24+
- Secure random IV generation for each encryption

### Desktop/JVM
- Uses Java's `javax.crypto` with AES/GCM/NoPadding  
- Compatible with all JVM platforms
- Same security guarantees as Android

### iOS (Future)
- Will use CommonCrypto or CryptoKit
- Requires platform-specific implementation

## Quick Start

### 1. Generate Encryption Key

**Important**: Generate the key once and store it securely. Never hardcode it!

```kotlin
// Generate key (do this ONCE)
val encryption = createPlatformEncryption("")
val key = encryption.generateKey()

// Store key securely:
// - Android: Use EncryptedSharedPreferences or Android Keystore
// - Desktop: Use system keychain or secure storage
// - Never commit the key to version control!
```

### 2. Create Encrypted Preference

```kotlin
val apiTokenPref = datastore.encrypted(
    key = "api_token",
    defaultValue = "",
    encryptionKey = key  // The key you generated and stored securely
)

// Use like normal preference - encryption is automatic
apiTokenPref.set("sensitive-api-token-12345")
val token = apiTokenPref.get() // Automatically decrypted
```

### 3. Encrypted Custom Objects

```kotlin
data class Credentials(val username: String, val password: String)

val credsPref = datastore.encryptedSerialized(
    key = "credentials",
    defaultValue = Credentials("", ""),
    encryptionKey = key,
    serializer = { creds -> "${creds.username}|${creds.password}" },
    deserializer = { str ->
        val parts = str.split("|")
        Credentials(parts[0], parts[1])
    }
)

// Use like normal preference
val creds = Credentials("user", "pass123")
credsPref.set(creds)
val retrieved = credsPref.get() // Automatically decrypted and deserialized
```

## API Reference

### Encryption Interface

```kotlin
interface DataEncryption {
    suspend fun encrypt(plainText: String): String
    suspend fun decrypt(encryptedText: String): String
    suspend fun generateKey(): String
}
```

### Factory Functions

**Create platform encryption:**
```kotlin
fun createPlatformEncryption(key: String): DataEncryption
```

**Create encrypted String preference:**
```kotlin
fun PreferenceDatastore.encrypted(
    key: String,
    defaultValue: String = "",
    encryptionKey: String
): Prefs<String>
```

**Create encrypted serialized preference:**
```kotlin
fun <T> PreferenceDatastore.encryptedSerialized(
    key: String,
    defaultValue: T,
    encryptionKey: String,
    serializer: (T) -> String,
    deserializer: (String) -> T
): Prefs<T>
```

## Security Best Practices

### Key Management

1. **Generate Once**: Generate the encryption key only once per app installation
2. **Store Securely**:
   - **Android**: Use `EncryptedSharedPreferences` or Android Keystore System
   - **Desktop**: Use platform keychain (macOS Keychain, Windows Credential Manager, Linux Secret Service)
3. **Never Hardcode**: Don't embed keys in source code
4. **Don't Log**: Never log encryption keys or decrypted sensitive data
5. **Key Rotation**: Plan for key rotation strategy for long-term apps

### Android Key Storage Example

```kotlin
// Using EncryptedSharedPreferences (recommended for Android)
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureKeyStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getOrCreateEncryptionKey(): String {
        return encryptedPrefs.getString("encryption_key", null) ?: run {
            val encryption = createPlatformEncryption("")
            val key = runBlocking { encryption.generateKey() }
            encryptedPrefs.edit().putString("encryption_key", key).apply()
            key
        }
    }
}
```

### What to Encrypt

**Do encrypt:**
- API tokens and secrets
- User credentials (passwords, auth tokens)
- Personal identifying information (PII)
- Financial data
- Health information
- Private messages

**Don't need to encrypt:**
- Public data
- UI preferences (theme, language)
- Non-sensitive app state
- Cached public content

## Usage Patterns

### 1. User Credentials

```kotlin
class AuthRepository(datastore: PreferenceDatastore, encryptionKey: String) {
    private val accessTokenPref = datastore.encrypted("access_token", "", encryptionKey)
    private val refreshTokenPref = datastore.encrypted("refresh_token", "", encryptionKey)
    
    suspend fun saveTokens(access: String, refresh: String) {
        accessTokenPref.set(access)
        refreshTokenPref.set(refresh)
    }
    
    suspend fun getAccessToken(): String = accessTokenPref.get()
    suspend fun clearTokens() {
        accessTokenPref.delete()
        refreshTokenPref.delete()
    }
}
```

### 2. API Keys

```kotlin
class ApiKeyManager(datastore: PreferenceDatastore, encryptionKey: String) {
    private val apiKeyPref = datastore.encrypted("api_key", "", encryptionKey)
    
    suspend fun setApiKey(key: String) {
        apiKeyPref.set(key)
    }
    
    suspend fun getApiKey(): String? {
        val key = apiKeyPref.get()
        return key.ifEmpty { null }
    }
}
```

### 3. Encrypted User Profile

```kotlin
data class UserProfile(
    val email: String,
    val phoneNumber: String,
    val ssn: String  // Sensitive!
)

class ProfileRepository(datastore: PreferenceDatastore, encryptionKey: String) {
    private val profilePref = datastore.encryptedSerialized(
        key = "user_profile",
        defaultValue = UserProfile("", "", ""),
        encryptionKey = encryptionKey,
        serializer = { profile ->
            Json.encodeToString(profile)  // Using kotlinx.serialization
        },
        deserializer = { json ->
            Json.decodeFromString<UserProfile>(json)
        }
    )
    
    suspend fun saveProfile(profile: UserProfile) {
        profilePref.set(profile)
    }
    
    suspend fun getProfile(): UserProfile = profilePref.get()
}
```

### 4. Migration from Unencrypted

If you have existing unencrypted preferences, migrate them:

```kotlin
suspend fun migrateToEncrypted(
    datastore: GenericPreferenceDatastore,
    encryptionKey: String
) {
    // Read old unencrypted value
    val oldPref = datastore.string("api_token", "")
    val value = oldPref.get()
    
    if (value.isNotEmpty()) {
        // Write to encrypted preference
        val newPref = datastore.encrypted("api_token_encrypted", "", encryptionKey)
        newPref.set(value)
        
        // Delete old unencrypted value
        oldPref.delete()
    }
}
```

## Performance Considerations

### Encryption Overhead

- **Encryption time**: ~1-5ms per operation
- **Decryption time**: ~1-5ms per operation
- **Key generation**: ~10-50ms (done once)

### Caching

Encrypted preferences work with the in-memory cache:

```kotlin
// First read: decrypt from DataStore (~10-50ms)
val token1 = tokenPref.get()

// Second read: from cache (~0.01ms) - still encrypted in DataStore
val token2 = tokenPref.get()
```

The cache stores **decrypted** values in memory for performance.

### Batch Operations

Use batch operations for multiple encrypted preferences:

```kotlin
// Efficient: Single DataStore transaction
datastore.batchSet(mapOf(
    encryptedToken to "token123",
    encryptedSecret to "secret456"
))

// Less efficient: Multiple transactions
encryptedToken.set("token123")
encryptedSecret.set("secret456")
```

## Error Handling

### Decryption Failures

Decryption failures return the default value:

```kotlin
val pref = datastore.encrypted("key", "default", encryptionKey)

// If decryption fails (corrupted data, wrong key, etc.)
val value = pref.get()  // Returns "default"
```

### Invalid Keys

```kotlin
// Throws IllegalArgumentException
datastore.encrypted("key", "", "")  // Empty key
datastore.encrypted("", "", encryptionKey)  // Empty preference key
```

### Encryption Exceptions

```kotlin
try {
    pref.set("value")
} catch (e: EncryptionException) {
    // Handle encryption failure
    Log.e(TAG, "Encryption failed", e)
}
```

## Testing

### Unit Tests

For unit tests, use a test encryption key:

```kotlin
class MyRepositoryTest {
    private val testKey = "dGVzdC1lbmNyeXB0aW9uLWtleS0xMjM0NTY3ODkw"  // Base64 test key
    
    @Test
    fun testEncryptedPreference() = runTest {
        val pref = datastore.encrypted("test", "", testKey)
        pref.set("secret")
        assertEquals("secret", pref.get())
    }
}
```

### Integration Tests

Test with real platform encryption:

```kotlin
@Test
fun testRealEncryption() = runTest {
    val encryption = createPlatformEncryption("")
    val key = encryption.generateKey()
    
    val pref = datastore.encrypted("test", "", key)
    pref.set("secret")
    
    // Verify it's actually encrypted
    val rawPref = datastore.string("test", "")
    val rawValue = rawPref.get()
    assertNotEquals("secret", rawValue)  // Should be encrypted
    
    // But decrypts correctly
    assertEquals("secret", pref.get())
}
```

## Troubleshooting

### "Failed to decrypt" errors

- Check that you're using the same encryption key
- Verify the key hasn't been corrupted
- Ensure the stored value hasn't been manually modified

### Performance issues

- Enable caching: `GenericPreference.cacheEnabled = true`
- Use batch operations for multiple preferences
- Consider encrypting only truly sensitive data

### Key rotation

```kotlin
suspend fun rotateEncryptionKey(
    datastore: GenericPreferenceDatastore,
    oldKey: String,
    newKey: String
) {
    val oldPref = datastore.encrypted("data", "", oldKey)
    val value = oldPref.get()  // Decrypt with old key
    
    val newPref = datastore.encrypted("data", "", newKey)
    newPref.set(value)  // Encrypt with new key
}
```

## Security Considerations

1. **IV Randomness**: Each encryption uses a fresh random IV
2. **Authenticated Encryption**: GCM mode provides both confidentiality and integrity
3. **No IV Reuse**: Random IV prevents IV reuse attacks
4. **Key Size**: AES-256 provides strong security
5. **Memory Safety**: Keys and decrypted values are kept in memory - consider clearing when done

## Limitations

- **Android Only**: Currently only Android and Desktop/JVM supported
- **String Data**: Direct encryption only supports String preferences
- **Key Storage**: You must implement secure key storage yourself
- **No Key Derivation**: Keys must be full 256-bit keys, no password-based derivation included

## Future Enhancements

- iOS platform support
- Key derivation from passwords (PBKDF2/Argon2)
- Hardware-backed encryption (Android Keystore, Secure Enclave)
- Encrypted preference collections
- Automatic key rotation
- Cloud key backup/sync

## Summary

Encryption support provides:
- ✅ **Strong security** - AES-256-GCM encryption
- ✅ **Easy to use** - Transparent encryption/decryption
- ✅ **Platform-specific** - Optimized for each platform
- ✅ **Type-safe** - Full Kotlin type safety
- ✅ **Performance** - Works with caching for speed
- ✅ **Flexible** - Supports custom objects via serialization
- ✅ **Error-resilient** - Graceful handling of failures

Use encryption for sensitive data while keeping the same simple preference API you're familiar with.
