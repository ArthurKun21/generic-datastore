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

### Compose State Integration

The `remember()` extension function allows you to observe and update preferences directly in your Composable functions:

```kotlin
@Composable
fun SettingsScreen(datastore: PreferenceDatastore) {
    val themePref = datastore.string("theme", "light")
    val theme by themePref.remember()
    
    // Use theme value in your UI
    Switch(
        checked = theme == "dark",
        onCheckedChange = { isDark ->
            // Updates preference automatically
            theme = if (isDark) "dark" else "light"
        }
    )
}
```

### API

#### `Prefs<T>.remember()`

Remembers the value of a preference and returns a `MutableState<T>` that can be used to observe and update the preference value.

**Parameters:**
- `context: CoroutineContext` - Optional coroutine context for the flow collection (default: `EmptyCoroutineContext`)

**Returns:**
- `MutableState<T>` - A mutable state representing the preference value

**Example:**

```kotlin
@Composable
fun UserProfileScreen(datastore: PreferenceDatastore) {
    val usernamePref = datastore.string("username", "")
    val username by usernamePref.remember()
    
    TextField(
        value = username,
        onValueChange = { newValue ->
            username = newValue  // Automatically updates the preference
        },
        label = { Text("Username") }
    )
}
```

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
