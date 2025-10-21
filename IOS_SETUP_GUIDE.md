# iOS Setup Guide for Generic Datastore

This guide will help you integrate the Generic Datastore library into your iOS project using Kotlin Multiplatform.

## Overview

The Generic Datastore library now supports iOS with full feature parity including:
- ✅ All preference types (String, Int, Long, Float, Boolean, Set, Enum, Serialized, KSerializer)
- ✅ AES-256-GCM encryption using CommonCrypto
- ✅ Thread-safe operations
- ✅ In-memory caching with LRU eviction
- ✅ Batch operations
- ✅ Version migrations
- ✅ DataStore Preferences Core

## Installation

### 1. Add to your `build.gradle.kts`

```kotlin
kotlin {
    // iOS targets
    ios()
    iosSimulatorArm64()
    
    sourceSets {
        val iosMain by creating {
            dependencies {
                implementation("com.github.ArthurKun21:generic-datastore:1.0.0")
            }
        }
    }
}
```

### 2. Framework Configuration

The library is exported as a static framework named `GenericDatastore`:

```kotlin
// This is already configured in the library
binaries.framework {
    baseName = "GenericDatastore"
    isStatic = true
}
```

## Usage Examples

### Basic Preferences

```kotlin
import io.github.arthurkun.generic.datastore.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

// Create DataStore instance
val dataStore: DataStore<Preferences> = createDataStore(
    path = { "app.preferences_pb" }
)

// Create preference store
val preferenceStore = GenericPreferenceDatastore(dataStore)

// String preference
val usernamePref = preferenceStore.string("username", "")
launch {
    usernamePref.set("JohnDoe")
    val username = usernamePref.get()
}

// Int preference
val agePref = preferenceStore.int("age", 0)
launch {
    agePref.set(25)
    val age = agePref.get()
}

// Boolean preference
val isDarkMode = preferenceStore.bool("dark_mode", false)
launch {
    isDarkMode.set(true)
    val isEnabled = isDarkMode.get()
}
```

### Encrypted Preferences

```kotlin
// Generate encryption key (do this once and store securely in Keychain)
val encryption = createPlatformEncryption("")
val encryptionKey = encryption.generateKey()

// Store key securely in iOS Keychain
// ... (use iOS Keychain services)

// Create encrypted preference
val authTokenPref = preferenceStore.encrypted(
    key = "auth_token",
    defaultValue = "",
    encryptionKey = encryptionKey
)

launch {
    authTokenPref.set("sensitive-api-token-12345")
    val token = authTokenPref.get() // Automatically decrypted
}
```

### KSerializer Preferences

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val theme: String = "light",
    val language: String = "en",
    val notifications: Boolean = true
)

val settingsPref = preferenceStore.kserializer(
    key = "user_settings",
    defaultValue = UserSettings(),
    serializer = UserSettings.serializer()
)

launch {
    val settings = settingsPref.get()
    settingsPref.set(settings.copy(theme = "dark"))
}
```

### Batch Operations

```kotlin
// Batch get - single DataStore read
val prefs = listOf(usernamePref, agePref, isDarkMode)
val values = preferenceStore.batchGet(prefs)

// Batch set - single atomic write
preferenceStore.batchSet(mapOf(
    usernamePref to "NewUser",
    agePref to 30,
    isDarkMode to true
))

// Batch delete
preferenceStore.batchDelete(listOf(tempPref1, tempPref2))
```

### Using with SwiftUI

You can expose the Kotlin code to Swift and use it in SwiftUI:

```swift
import GenericDatastore
import SwiftUI

class PreferencesViewModel: ObservableObject {
    private let preferenceStore: GenericPreferenceDatastore
    
    @Published var username: String = ""
    @Published var isDarkMode: Bool = false
    
    init() {
        // Initialize DataStore
        let dataStore = DataStoreUtilsKt.createDataStore { "app.preferences_pb" }
        self.preferenceStore = GenericPreferenceDatastore(datastore: dataStore)
        
        // Load initial values
        loadPreferences()
    }
    
    func loadPreferences() {
        let usernamePref = preferenceStore.string(key: "username", defaultValue: "")
        let darkModePref = preferenceStore.bool(key: "dark_mode", defaultValue: false)
        
        // Load asynchronously
        Task {
            self.username = try await usernamePref.get()
            self.isDarkMode = try await darkModePref.get()
        }
    }
    
    func saveUsername(_ newUsername: String) {
        let usernamePref = preferenceStore.string(key: "username", defaultValue: "")
        Task {
            try await usernamePref.set(value: newUsername)
            self.username = newUsername
        }
    }
}

struct ContentView: View {
    @StateObject private var viewModel = PreferencesViewModel()
    
    var body: some View {
        VStack {
            TextField("Username", text: $viewModel.username)
                .onSubmit {
                    viewModel.saveUsername(viewModel.username)
                }
            
            Toggle("Dark Mode", isOn: $viewModel.isDarkMode)
        }
    }
}
```

## DataStore File Location

On iOS, DataStore files are stored in the app's Documents directory by default:

```kotlin
// Customize the path
val dataStore = createDataStore(
    path = { 
        val documentsDirectory = NSFileManager.defaultManager
            .URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)
        "${documentsDirectory?.path}/preferences.pb"
    }
)
```

## Encryption on iOS

The iOS implementation uses Apple's CommonCrypto framework with AES-256-GCM:

- **Algorithm**: AES-256-GCM (Galois/Counter Mode)
- **Key Size**: 256 bits
- **IV Size**: 96 bits (12 bytes)
- **Tag Size**: 128 bits (16 bytes)
- **Random Number Generator**: `SecRandomCopyBytes` (cryptographically secure)

### Secure Key Storage

**⚠️ Important**: Always store encryption keys securely in the iOS Keychain, never in UserDefaults or DataStore:

```swift
import Security

func saveKeyToKeychain(key: String, identifier: String) {
    let keyData = key.data(using: .utf8)!
    
    let query: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: identifier,
        kSecValueData as String: keyData,
        kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
    ]
    
    SecItemDelete(query as CFDictionary) // Delete any existing item
    SecItemAdd(query as CFDictionary, nil)
}

func loadKeyFromKeychain(identifier: String) -> String? {
    let query: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: identifier,
        kSecReturnData as String: true,
        kSecMatchLimit as String: kSecMatchLimitOne
    ]
    
    var result: AnyObject?
    let status = SecItemCopyMatching(query as CFDictionary, &result)
    
    guard status == errSecSuccess,
          let data = result as? Data,
          let key = String(data: data, encoding: .utf8) else {
        return nil
    }
    
    return key
}
```

## Caching on iOS

The library includes a high-performance Caffeine-inspired cache that works seamlessly on iOS:

```kotlin
// Configure cache globally
CaffeineCache.configure(
    maxSize = 10_000,              // LRU eviction
    ttl = 5.minutes,               // Write-time expiration
    idleTimeout = 10.minutes       // Access-time expiration
)

// Enable caching
GenericPreference.cacheEnabled = true

// Cache statistics
val stats = CaffeineCache.getStats()
print("Hit rate: ${stats.hitRate}%")
```

## Thread Safety

All operations are thread-safe and can be called from any thread:

```swift
// Safe to call from main thread or background threads
Task {
    let value = try await preference.get()
}

DispatchQueue.global().async {
    Task {
        try await preference.set(value: "newValue")
    }
}
```

## Migration Support

Version migrations work identically on iOS:

```kotlin
val migrationManager = PreferenceMigrationManager(dataStore)

migrationManager.addMigration(
    PreferenceMigration(
        fromVersion = 0,
        toVersion = 1,
        migrate = { prefs ->
            // Rename preference
            val oldValue = prefs.string("old_key", "").get()
            prefs.string("new_key", "").set(oldValue)
            prefs.string("old_key", "").delete()
        }
    )
)

migrationManager.migrateIfNeeded(targetVersion = 1)
```

## Performance Tips

1. **Use Caching**: Enable caching for read-heavy preferences
2. **Batch Operations**: Use `batchGet()` and `batchSet()` for multiple preferences
3. **Lazy Loading**: Load preferences on demand rather than all at once
4. **Flow Collections**: Use Flow for reactive updates instead of polling

```kotlin
// Good: Use Flow for reactive updates
launch {
    preference.flow.collect { value ->
        // UI updates automatically
    }
}

// Good: Batch load at startup
val allPrefs = batchGet(listOf(pref1, pref2, pref3))

// Avoid: Polling for changes
while (true) {
    val value = preference.get()
    delay(1000)
}
```

## Troubleshooting

### Framework Not Found

If you get "Framework not found GenericDatastore":

1. Ensure the framework is built: `./gradlew :library:linkDebugFrameworkIosSimulatorArm64`
2. Add the framework to your Xcode project's "Frameworks, Libraries, and Embedded Content"
3. Set "Embed" to "Do Not Embed" (it's a static framework)

### Encryption Errors

If you encounter encryption errors:

1. Verify the encryption key is valid Base64
2. Ensure you're using the same key for encryption and decryption
3. Check that the key is properly stored in Keychain

### DataStore File Permissions

If DataStore fails to write:

1. Verify the app has write permissions to the Documents directory
2. Check the path returned by `NSFileManager`
3. Ensure the directory exists before creating DataStore

## Platform Differences

| Feature | Android | Desktop | iOS |
|---------|---------|---------|-----|
| AES-256-GCM Encryption | ✅ javax.crypto | ✅ javax.crypto | ✅ CommonCrypto |
| Key Storage | Keystore | File System | Keychain |
| DataStore Location | Internal Storage | ~/.local/share | Documents |
| Thread Safety | ✅ | ✅ | ✅ |
| Caching | ✅ | ✅ | ✅ |
| Batch Operations | ✅ | ✅ | ✅ |

## Example Project

Check out the complete iOS sample app in the repository:

```
ios-app/
├── iosApp.swift
├── ContentView.swift
├── PreferencesViewModel.swift
└── Models/
    └── UserSettings.swift
```

## Additional Resources

- [Main README](README.md)
- [Cache Guide](CACHE_GUIDE.md)
- [Encryption Guide](ENCRYPTION_GUIDE.md)
- [Batch Operations Guide](BATCH_OPERATIONS_GUIDE.md)
- [Version Migration Guide](VERSION_MIGRATION_GUIDE.md)

## Support

For issues specific to iOS:
1. Check that you're using the latest version of Xcode
2. Verify Kotlin Multiplatform plugin is up to date
3. Review the [GitHub Issues](https://github.com/ArthurKun21/generic-datastore/issues)

## License

Apache License 2.0 - Same as the main library
