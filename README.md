# Generic Datastore Library

A Kotlin Multiplatform library that provides a thin, convenient wrapper around AndroidX DataStore Preferences and Proto DataStore.

Inspired by [flow-preferences](https://github.com/tfcporciuncula/flow-preferences) for SharedPreferences.

## Modules

| Module | Description |
|---|---|
| `generic-datastore` | Core library with Preferences DataStore and Proto DataStore wrappers |
| `generic-datastore-compose` | Jetpack Compose extensions (`Prefs<T>.remember()`) |

### KMP Targets

Both modules target **Android** and **Desktop (JVM)**.

## Installation

Add the JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

Add the dependencies:

```kotlin
dependencies {
    implementation("com.github.arthurkun:generic-datastore:<version>")

    // Optional: Compose extensions
    implementation("com.github.arthurkun:generic-datastore-compose:<version>")
}
```

## Preferences DataStore

### Setup

```kotlin
// Standard DataStore setup
val Context.myDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Create the GenericPreferencesDatastore
val datastore = GenericPreferencesDatastore(context.myDataStore)
```

### Defining Preferences

The `GenericPreferencesDatastore` provides factory methods for all supported types:

```kotlin
val userName: Prefs<String>      = datastore.string("user_name", "Guest")
val userScore: Prefs<Int>        = datastore.int("user_score", 0)
val highScore: Prefs<Long>       = datastore.long("high_score", 0L)
val volume: Prefs<Float>         = datastore.float("volume", 1.0f)
val darkMode: Prefs<Boolean>     = datastore.bool("dark_mode", false)
val tags: Prefs<Set<String>>     = datastore.stringSet("tags")
```

### Enum Preferences

Store enum values directly using the `enum()` extension:

```kotlin
enum class Theme { LIGHT, DARK, SYSTEM }

val themePref: Prefs<Theme> = datastore.enum("theme", Theme.SYSTEM)
```

### Custom Serialized Objects

Store any object by providing serializer/deserializer functions:

```kotlin
@Serializable
data class UserProfile(val id: Int, val email: String)

val userProfilePref: Prefs<UserProfile> = datastore.serialized(
    key = "user_profile",
    defaultValue = UserProfile(0, ""),
    serializer = { Json.encodeToString(UserProfile.serializer(), it) },
    deserializer = { Json.decodeFromString(UserProfile.serializer(), it) },
)
```

Or with a sealed class:

```kotlin
sealed class Animal(val name: String) {
    data object Dog : Animal("Dog")
    data object Cat : Animal("Cat")

    companion object {
        fun from(value: String): Animal = when (value) {
            "Dog" -> Dog
            "Cat" -> Cat
            else -> throw Exception("Unknown animal type: $value")
        }
        fun to(animal: Animal): String = animal.name
    }
}

val animalPref = datastore.serialized(
    key = "animal",
    defaultValue = Animal.Dog,
    serializer = { Animal.to(it) },
    deserializer = { Animal.from(it) },
)
```

### Reading & Writing Values

Each `Prefs<T>` provides multiple access patterns:

#### Suspend Functions

```kotlin
CoroutineScope(Dispatchers.IO).launch {
    val name = userName.get()
    userName.set("John Doe")
    userName.delete()
}
```

#### Flow-based Observation

```kotlin
userName.asFlow().collect { value -> /* react to changes */ }

val nameState: StateFlow<String> = userName.stateIn(viewModelScope)
```

#### Blocking Access

```kotlin
// Blocks the calling thread — avoid on the main/UI thread
val name = userName.getBlocking()
userName.setBlocking("John Doe")
```

#### Property Delegation

`Prefs<T>` implements `ReadWriteProperty`, so you can use it as a delegated property:

```kotlin
var currentUserName: String by userName

// Read (blocking)
println(currentUserName)

// Write (blocking)
currentUserName = "Jane Doe"
```

#### Reset to Default

```kotlin
// Suspend
userName.resetToDefault()

// Blocking — avoid on the main/UI thread
userName.resetToDefaultBlocking()
```

### Mapped Preferences

Transform a `Prefs<T>` into a `Prefs<R>` with converter functions:

```kotlin
val scoreAsString: Prefs<String> = userScore.map(
    defaultValue = "0",
    convert = { it.toString() },
    reverse = { it.toIntOrNull() ?: 0 },
)

// Or infer the default value from the original preference's default:
val scoreAsString2: Prefs<String> = userScore.mapIO(
    convert = { it.toString() },
    reverse = { it.toInt() },
)
```

`map` catches exceptions in conversions and falls back to defaults. `mapIO` throws if conversion of the default value fails.

### Export & Import

`GenericPreferencesDatastore` supports exporting and importing all preferences as JSON:

```kotlin
// Export (returns Map<String, JsonElement>)
val exported = datastore.export(
    exportPrivate = false,
    exportAppState = false,
)

// Import (accepts Map<String, Any>)
datastore.import(data)
```

### Private & App-State Key Prefixes

Use `Preference.privateKey(key)` or `Preference.appStateKey(key)` to prefix keys so they can be filtered during export:

```kotlin
val token = datastore.string(Preference.privateKey("auth_token"), "")
val onboarded = datastore.bool(Preference.appStateKey("onboarding_done"), false)
```

## Proto DataStore

Wrap a typed `DataStore<T>` for proto messages:

```kotlin
val protoDatastore = GenericProtoDatastore(
    datastore = myProtoDataStore,
    defaultValue = MyProtoMessage.getDefaultInstance(),
)

val dataPref: Prefs<MyProtoMessage> = protoDatastore.data()

// Then use get(), set(), asFlow(), etc. just like Preferences DataStore
```

## Compose Extensions (`generic-datastore-compose`)

The `remember()` extension turns any `Prefs<T>` into a lifecycle-aware `MutableState<T>`:

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun SettingsScreen(datastore: GenericPreferencesDatastore) {
    var userName by datastore.string("user_name", "Guest").remember()

    Column {
        Text("Current User: $userName")
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Enter username") },
        )
    }
}
```

Under the hood, `remember()` uses `collectAsStateWithLifecycle` for lifecycle-safe collection and launches a coroutine for writes.

## License

[Apache License 2.0](LICENSE.md)
