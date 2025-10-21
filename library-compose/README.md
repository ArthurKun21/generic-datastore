# Generic DataStore Compose Extensions

This module provides Jetpack Compose extensions for the Generic DataStore library.

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    // Core library (required)
    implementation("com.github.ArthurKun21:generic-datastore:1.0.0")
    
    // Compose extensions (optional - only if you need Compose integration)
    implementation("com.github.ArthurKun21:generic-datastore-compose:1.0.0")
}
```

### JitPack

Add JitPack repository to your build file:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

## Features

### 🚀 High-Performance Compose State Integration

The `remember()` extension function allows you to observe and update preferences directly in your Composable functions with **optimized performance for multiple preferences**:

```kotlin
@Composable
fun SettingsScreen(datastore: PreferenceDatastore) {
    // Optimized: All remembers share resources efficiently
    var theme by datastore.theme.remember()
    var language by datastore.language.remember()
    var notifications by datastore.notifications.remember()
    
    // Use values in your UI - no performance penalty!
    Switch(
        checked = theme == "dark",
        onCheckedChange = { isDark ->
            theme = if (isDark) "dark" else "light"  // Coalesced updates
        }
    )
}
```

### Performance Optimizations

✨ **Optimized for multiple `remember()` calls:**
- **Write coalescing** - Rapid consecutive updates are batched to reduce I/O
- **Resource sharing** - Minimal memory footprint even with many preferences
- **Automatic cleanup** - Resources freed when composable leaves composition
- **Flow caching** - Flow collections reused across recompositions

### API

#### `Prefs<T>.remember()`

Remembers the value of a preference and returns a `MutableState<T>` that can be used to observe and update the preference value.

**Performance:**
- **Single preference**: Zero overhead, direct state management
- **Multiple preferences**: Optimized resource sharing and batched updates
- **Rapid updates**: Write coalescing prevents excessive I/O operations

**Parameters:**
- `context: CoroutineContext` - Optional coroutine context for the flow collection (default: `EmptyCoroutineContext`)

**Returns:**
- `MutableState<T>` - A mutable state representing the preference value

**Example - Single Preference:**

```kotlin
@Composable
fun UserProfileScreen(datastore: PreferenceDatastore) {
    var username by datastore.string("username", "").remember()
    
    TextField(
        value = username,
        onValueChange = { newValue ->
            username = newValue  // Automatically updates the preference
        },
        label = { Text("Username") }
    )
}
```

**Example - Multiple Preferences (Optimized):**

```kotlin
@Composable
fun SettingsForm(datastore: PreferenceDatastore) {
    // All remembers are optimized to share resources
    var theme by datastore.string("theme", "light").remember()
    var language by datastore.string("language", "en").remember()
    var fontSize by datastore.int("font_size", 14).remember()
    var notifications by datastore.bool("notifications", true).remember()
    var autoSync by datastore.bool("auto_sync", false).remember()
    
    // Use in UI without performance concerns
    Column {
        ThemeSelector(theme) { theme = it }
        LanguageSelector(language) { language = it }
        FontSizeSlider(fontSize) { fontSize = it }
        NotificationToggle(notifications) { notifications = it }
        AutoSyncToggle(autoSync) { autoSync = it }
    }
}
```

#### `Prefs<T>.observeAsState()`

Observes a preference as a read-only `State<T>` without write capabilities. More efficient than `remember()` when you only need to read values.

**Parameters:**
- `context: CoroutineContext` - Optional coroutine context (default: `EmptyCoroutineContext`)

**Returns:**
- `State<T>` - A read-only state representing the preference value

**Example:**

```kotlin
@Composable
fun DisplayTheme(datastore: PreferenceDatastore) {
    val theme by datastore.string("theme", "light").observeAsState()
    
    // Read-only access, optimized for observation
    Text("Current theme: $theme")
}
```

#### `rememberPreferences()`

Collects multiple preferences as a Map for batch read operations. Useful when you need to observe several preferences together.

**Parameters:**
- `preferences: vararg Pair<String, Prefs<*>>` - Pairs of keys to Prefs instances
- `context: CoroutineContext` - Optional coroutine context (default: `EmptyCoroutineContext`)

**Returns:**
- `State<Map<String, Any?>>` - State containing a Map of keys to current values

**Example:**

```kotlin
@Composable
fun SettingsScreen(datastore: PreferenceDatastore) {
    val settings by rememberPreferences(
        "theme" to datastore.string("theme", "light"),
        "language" to datastore.string("language", "en"),
        "notifications" to datastore.bool("notifications", true)
    )
    
    val theme = settings["theme"] as String
    val language = settings["language"] as String
    val notifications = settings["notifications"] as Boolean
    
    // Use settings...
}
```

### Best Practices

1. **Use `remember()` for read-write access:**
   ```kotlin
   var theme by datastore.theme.remember()
   ```

2. **Use `observeAsState()` for read-only access:**
   ```kotlin
   val theme by datastore.theme.observeAsState()
   ```

3. **Use `rememberPreferences()` for batch observations:**
   ```kotlin
   val prefs by rememberPreferences(
       "key1" to pref1,
       "key2" to pref2
   )
   ```

4. **Multiple remembers are optimized - don't worry about performance!**

## Why a Separate Module?

The Compose extensions are provided in a separate module to:

1. **Reduce library size** - Users who don't need Compose integration don't have to include Compose dependencies
2. **Modular architecture** - Keep concerns separated and dependencies minimal
3. **Flexibility** - Use only what you need

## Requirements

- Kotlin 2.2.20 or higher
- Jetpack Compose
- The core `generic-datastore` library

## License

```
Copyright 2024 Arthur

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
