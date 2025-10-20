# Version Migration Guide

## Overview

The version migration system provides a structured way to evolve your preference schema over time while maintaining data integrity and backward compatibility. As your app grows, you may need to rename preferences, transform values, or restructure data - migrations make this safe and automatic.

## Core Concepts

### Version
Each preference schema has a version number (starting at 0). When you change the schema, you increment the version and provide a migration.

### Migration
A migration describes how to transform data from one version to another. Migrations are executed sequentially and automatically.

### Migration Manager
`PreferenceMigrationManager` coordinates all migrations, tracking the current version and executing necessary migrations.

## Quick Start

### 1. Create Migration Manager

```kotlin
class AppPreferences(private val datastore: DataStore<Preferences>) {
    private val migrationManager = PreferenceMigrationManager(datastore)
    
    init {
        // Define migrations
        setupMigrations()
    }
    
    suspend fun initialize() {
        // Execute migrations to latest version
        migrationManager.migrateIfNeeded(CURRENT_VERSION)
    }
    
    companion object {
        private const val CURRENT_VERSION = 2
    }
}
```

### 2. Define Migrations

```kotlin
private fun setupMigrations() {
    migrationManager.addMigrations(
        // V0 -> V1: Rename preference
        PreferenceMigration(
            fromVersion = 0,
            toVersion = 1,
            migrate = { prefs ->
                val oldValue = prefs.string("user_name", "").get()
                prefs.string("profile_name", "").set(oldValue)
                prefs.string("user_name", "").delete()
            }
        ),
        // V1 -> V2: Transform value
        PreferenceMigration(
            fromVersion = 1,
            toVersion = 2,
            migrate = { prefs ->
                val theme = prefs.string("theme", "").get()
                val themeId = if (theme == "dark") 1 else 0
                prefs.int("theme_id", 0).set(themeId)
                prefs.string("theme", "").delete()
            }
        )
    )
}
```

### 3. Run Migrations

```kotlin
// In your Application class or app startup
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        lifecycleScope.launch {
            appPreferences.initialize()  // Migrations run automatically
        }
    }
}
```

## API Reference

### PreferenceMigration

```kotlin
data class PreferenceMigration(
    val fromVersion: Int,          // Version to migrate from
    val toVersion: Int,            // Version to migrate to
    val migrate: suspend (GenericPreferenceDatastore) -> Unit  // Migration logic
)
```

**Example:**
```kotlin
PreferenceMigration(
    fromVersion = 1,
    toVersion = 2,
    migrate = { prefs ->
        // Your migration logic here
    }
)
```

### PreferenceMigrationManager

#### Constructor
```kotlin
PreferenceMigrationManager(datastore: DataStore<Preferences>)
```

#### Methods

**Add Migration:**
```kotlin
fun addMigration(migration: PreferenceMigration): PreferenceMigrationManager
fun addMigrations(vararg migrations: PreferenceMigration): PreferenceMigrationManager
```

**Check Version:**
```kotlin
suspend fun getCurrentVersion(): Int
suspend fun needsMigration(targetVersion: Int): Boolean
```

**Execute Migrations:**
```kotlin
suspend fun migrate(targetVersion: Int): Boolean
suspend fun migrateIfNeeded(targetVersion: Int): Boolean
```

## Common Migration Patterns

### 1. Renaming a Preference

```kotlin
PreferenceMigration(
    fromVersion = 1,
    toVersion = 2,
    migrate = { prefs ->
        val oldValue = prefs.string("old_key", "").get()
        prefs.string("new_key", "").set(oldValue)
        prefs.string("old_key", "").delete()
    }
)

// Or use the helper:
PreferenceMigration(
    fromVersion = 1,
    toVersion = 2,
    migrate = MigrationHelpers.renameKey(
        oldKey = "old_key",
        newKey = "new_key",
        defaultValue = "",
        factory = { prefs, key, default -> prefs.string(key, default) }
    )
)
```

### 2. Transforming a Value

```kotlin
PreferenceMigration(
    fromVersion = 2,
    toVersion = 3,
    migrate = { prefs ->
        val celsius = prefs.int("temperature", 0).get()
        val fahrenheit = (celsius * 9 / 5) + 32
        prefs.int("temperature", 0).set(fahrenheit)
    }
)

// Or use the helper:
PreferenceMigration(
    fromVersion = 2,
    toVersion = 3,
    migrate = MigrationHelpers.transformValue(
        key = "temperature",
        defaultValue = 0,
        factory = { prefs, key, default -> prefs.int(key, default) },
        transform = { celsius -> (celsius * 9 / 5) + 32 }
    )
)
```

### 3. Removing Deprecated Preferences

```kotlin
PreferenceMigration(
    fromVersion = 3,
    toVersion = 4,
    migrate = { prefs ->
        prefs.batchDelete(listOf(
            prefs.string("deprecated_1", ""),
            prefs.string("deprecated_2", ""),
            prefs.int("deprecated_3", 0)
        ))
    }
)
```

### 4. Splitting a Preference

```kotlin
PreferenceMigration(
    fromVersion = 4,
    toVersion = 5,
    migrate = { prefs ->
        // Old: "John Doe" in full_name
        val fullName = prefs.string("full_name", "").get()
        val parts = fullName.split(" ", limit = 2)
        
        // New: first_name and last_name
        prefs.string("first_name", "").set(parts.getOrNull(0) ?: "")
        prefs.string("last_name", "").set(parts.getOrNull(1) ?: "")
        prefs.string("full_name", "").delete()
    }
)
```

### 5. Combining Preferences

```kotlin
PreferenceMigration(
    fromVersion = 5,
    toVersion = 6,
    migrate = { prefs ->
        val firstName = prefs.string("first_name", "").get()
        val lastName = prefs.string("last_name", "").get()
        
        // Combine into single preference
        val displayName = "$firstName $lastName".trim()
        prefs.string("display_name", "").set(displayName)
        
        // Clean up old preferences
        prefs.batchDelete(listOf(
            prefs.string("first_name", ""),
            prefs.string("last_name", "")
        ))
    }
)
```

### 6. Type Changes

```kotlin
PreferenceMigration(
    fromVersion = 6,
    toVersion = 7,
    migrate = { prefs ->
        // Convert string to int
        val ageStr = prefs.string("age", "").get()
        val age = ageStr.toIntOrNull() ?: 0
        prefs.int("age_int", 0).set(age)
        prefs.string("age", "").delete()
    }
)
```

### 7. Batch Operations for Performance

```kotlin
PreferenceMigration(
    fromVersion = 7,
    toVersion = 8,
    migrate = { prefs ->
        // Efficiently migrate many preferences at once
        val oldPrefs = (1..100).map { prefs.string("old_pref_$it", "") }
        val oldValues = prefs.batchGet(oldPrefs)
        
        val newPrefs = (1..100).map { prefs.string("new_pref_$it", "") }
        val updates: Map<Prefs<*>, Any?> = newPrefs.mapIndexed { index, pref ->
            pref to oldValues["old_pref_${index + 1}"]
        }.toMap()
        
        prefs.batchSet(updates)
        prefs.batchDelete(oldPrefs)
    }
)
```

## Best Practices

### 1. Always Increment Versions Sequentially

```kotlin
// Good: Sequential versions
PreferenceMigration(fromVersion = 0, toVersion = 1, ...)
PreferenceMigration(fromVersion = 1, toVersion = 2, ...)
PreferenceMigration(fromVersion = 2, toVersion = 3, ...)

// Bad: Gaps in versions
PreferenceMigration(fromVersion = 0, toVersion = 1, ...)
PreferenceMigration(fromVersion = 2, toVersion = 3, ...)  // Missing 1->2!
```

### 2. Never Skip Migrations

Even if you want to make multiple changes, create migrations for each step:

```kotlin
// Good: Each change is a separate migration
v1 -> v2: Rename user_name to profile_name
v2 -> v3: Add birth_year field
v3 -> v4: Remove deprecated fields

// Avoid: Jumping versions
v1 -> v4: Do everything at once
```

### 3. Test Migrations Thoroughly

```kotlin
@Test
fun testMigrationV1ToV2() = runTest {
    // Setup V1 data
    val prefs = GenericPreferenceDatastore(datastore)
    prefs.string("user_name", "").set("John")
    
    // Run migration
    migrationManager.addMigration(/* v1 -> v2 migration */)
    val success = migrationManager.migrate(2)
    
    // Verify V2 data
    assertTrue(success)
    assertEquals("John", prefs.string("profile_name", "").get())
    assertEquals("", prefs.string("user_name", "").get())
}
```

### 4. Handle Default Values Properly

```kotlin
PreferenceMigration(
    fromVersion = 8,
    toVersion = 9,
    migrate = { prefs ->
        val value = prefs.string("key", "").get()
        // Only migrate if value was actually set (not default)
        if (value.isNotEmpty()) {
            // Transform and save
        }
    }
)
```

### 5. Use Migration Helpers

```kotlin
// Instead of writing repetitive code:
PreferenceMigration(
    fromVersion = 9,
    toVersion = 10,
    migrate = { prefs ->
        val old1 = prefs.string("key1", "").get()
        prefs.string("new_key1", "").set(old1)
        prefs.string("key1", "").delete()
        
        val old2 = prefs.string("key2", "").get()
        prefs.string("new_key2", "").set(old2)
        prefs.string("key2", "").delete()
    }
)

// Use combineMigrations helper:
PreferenceMigration(
    fromVersion = 9,
    toVersion = 10,
    migrate = MigrationHelpers.combineMigrations(
        MigrationHelpers.renameKey("key1", "new_key1", "", 
            { p, k, d -> p.string(k, d) }),
        MigrationHelpers.renameKey("key2", "new_key2", "", 
            { p, k, d -> p.string(k, d) })
    )
)
```

### 6. Document Your Migrations

```kotlin
private fun setupMigrations() {
    migrationManager.addMigrations(
        // V0 -> V1 (2024-01-15): Changed user_name to profile_name
        // to match backend API field names
        PreferenceMigration(fromVersion = 0, toVersion = 1, ...),
        
        // V1 -> V2 (2024-02-20): Added theme_id as integer
        // to support new theme system
        PreferenceMigration(fromVersion = 1, toVersion = 2, ...),
    )
}
```

## Advanced Scenarios

### Conditional Migrations

```kotlin
PreferenceMigration(
    fromVersion = 10,
    toVersion = 11,
    migrate = { prefs ->
        val isPremium = prefs.bool("is_premium", false).get()
        
        if (isPremium) {
            // Premium users get extra features
            prefs.int("max_downloads", 0).set(100)
        } else {
            // Free users get default
            prefs.int("max_downloads", 0).set(10)
        }
    }
)
```

### Complex Data Transformations

```kotlin
PreferenceMigration(
    fromVersion = 11,
    toVersion = 12,
    migrate = { prefs ->
        // Migrate from CSV to JSON
        val csvData = prefs.string("favorites", "").get()
        val items = csvData.split(",").filter { it.isNotBlank() }
        
        // Convert to JSON array (in real app, use proper serialization)
        val jsonData = items.joinToString(separator = ",", 
            prefix = "[\"", postfix = "\"]") { it }
        
        prefs.string("favorites_json", "").set(jsonData)
        prefs.string("favorites", "").delete()
    }
)
```

### Migration with External Data

```kotlin
PreferenceMigration(
    fromVersion = 12,
    toVersion = 13,
    migrate = { prefs ->
        // Could fetch data from network, database, etc.
        val userId = prefs.string("user_id", "").get()
        
        // Set new fields based on external data
        prefs.string("user_region", "").set(determineRegion(userId))
    }
)
```

## Error Handling

Migrations include built-in error handling:

```kotlin
// If a migration fails, it stops and doesn't update the version
val success = migrationManager.migrate(5)

if (!success) {
    // Migration failed - handle gracefully
    // Data is still at the previous version
    Log.e(TAG, "Migration failed - app may use old schema")
}
```

## Testing Strategy

### Unit Tests for Individual Migrations

```kotlin
@Test
fun testEachMigrationIndividually() = runTest {
    // Test V0 -> V1
    setupV0Data()
    migrationManager.migrate(1)
    verifyV1Data()
    
    // Reset and test V1 -> V2
    resetDatastore()
    setupV1Data()
    migrationManager.migrate(2)
    verifyV2Data()
}
```

### Integration Tests for Full Migration Path

```kotlin
@Test
fun testFullMigrationPath() = runTest {
    // Start from V0
    setupV0Data()
    
    // Migrate all the way to current version
    val success = migrationManager.migrate(CURRENT_VERSION)
    
    assertTrue(success)
    verifyCurrentVersionData()
}
```

## Version Management Tips

1. **Bump version in a dedicated commit**: Makes it easy to track schema changes
2. **Keep old migrations**: Don't delete old migrations even if no users are on those versions
3. **Test with real user data**: Use anonymized production data for migration testing
4. **Monitor migration performance**: Log migration execution time in production
5. **Plan rollback strategy**: Have a way to handle migration failures gracefully

## Summary

Version migrations provide:
- ✅ **Safe schema evolution** - Structured way to change preferences
- ✅ **Automatic execution** - Runs transparently on app startup
- ✅ **Data integrity** - Atomic operations ensure consistency
- ✅ **Backward compatibility** - Old app versions don't break
- ✅ **Testing support** - Easy to test each migration
- ✅ **Performance** - Can use batch operations for efficiency
- ✅ **Error resilience** - Built-in error handling

Use migrations whenever you need to change your preference schema to ensure smooth upgrades for all users.
