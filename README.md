# Generic Datastore Library

[![Release](https://jitpack.io/v/ArthurKun21/generic-datastore.svg)](https://jitpack.io/#ArthurKun21/generic-datastore)
[![GitHub Packages](https://img.shields.io/badge/GitHub%20Packages-published-brightgreen?logo=github)](https://github.com/ArthurKun21?tab=packages&repo_name=generic-datastore)
[![build](https://github.com/ArthurKun21/generic-datastore/actions/workflows/build.yml/badge.svg)](https://github.com/ArthurKun21/generic-datastore/actions/workflows/build.yml)
[![Tests](https://github.com/ArthurKun21/generic-datastore/actions/workflows/test.yml/badge.svg)](https://github.com/ArthurKun21/generic-datastore/actions/workflows/test.yml)

A Kotlin Multiplatform library that provides a thin, convenient wrapper around AndroidX DataStore
Preferences and Proto DataStore.

Inspired by [flow-preferences](https://github.com/tfcporciuncula/flow-preferences) for
SharedPreferences.

## Modules

| Module                      | Description                                                          |
|-----------------------------|----------------------------------------------------------------------|
| `generic-datastore`         | Core library with Preferences DataStore and Proto DataStore wrappers |
| `generic-datastore-compose` | Jetpack Compose extensions (`DelegatedPreference<T>.remember()`)     |

### KMP Targets

Both modules target **Android**, **Desktop (JVM)**, and **iOS** (`iosX64`, `iosArm64`,
`iosSimulatorArm64`).

## Installation

### JitPack

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

### GitHub Packages

Add the GitHub Packages repository to your `settings.gradle.kts`. Authentication requires a GitHub
personal access token (classic) with the `read:packages` scope:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/ArthurKun21/generic-datastore")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Set the credentials in your `~/.gradle/gradle.properties` (or use the environment variables
`GITHUB_ACTOR` and `GITHUB_TOKEN`):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Add the dependencies:

```kotlin
dependencies {
    implementation("com.github.ArthurKun21:generic-datastore:<version>")

    // Optional: Compose extensions
    implementation("com.github.ArthurKun21:generic-datastore-compose:<version>")
}
```

## Preferences DataStore

### Setup Preference DataStore

Use the `createPreferencesDatastore` factory function to create a `GenericPreferencesDatastore`:

```kotlin
val datastore = createPreferencesDatastore(
    producePath = { context.filesDir.resolve("settings.preferences_pb").absolutePath },
)
```

A `fileName` overload is available that appends the file name to a directory path:

```kotlin
val datastore = createPreferencesDatastore(
    fileName = "settings.preferences_pb",
    producePath = { context.filesDir.absolutePath },
)
```

Optional parameters allow customizing the corruption handler, migrations, coroutine scope, and the
default `Json` instance used for serialization-based preferences:

```kotlin
val datastore = createPreferencesDatastore(
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    migrations = listOf(myMigration),
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    defaultJson = Json { prettyPrint = true },
    producePath = { context.filesDir.resolve("settings.preferences_pb").absolutePath },
)
```

Overloads accepting `okio.Path` and `kotlinx.io.files.Path` are also available:

```kotlin
val datastore = createPreferencesDatastore(
    produceOkioPath = { "/data/settings.preferences_pb".toPath() },
)

val datastore = createPreferencesDatastore(
    produceKotlinxIoPath = { Path("/data/settings.preferences_pb") },
)
```

Alternatively, wrap an existing `DataStore<Preferences>` directly:

```kotlin
val datastore = GenericPreferencesDatastore(myExistingDataStore)
```

### Defining Preferences

The `GenericPreferencesDatastore` provides factory methods for all supported types:

```kotlin
val userName: Preference<String> = datastore.string("user_name", "Guest")
val userScore: Preference<Int> = datastore.int("user_score", 0)
val highScore: Preference<Long> = datastore.long("high_score", 0L)
val volume: Preference<Float> = datastore.float("volume", 1.0f)
val precision: Preference<Double> = datastore.double("precision", 0.0)
val darkMode: Preference<Boolean> = datastore.bool("dark_mode", false)
val tags: Preference<Set<String>> = datastore.stringSet("tags")
```

### Enum Preferences

Store enum values directly using the `enum()` extension:

```kotlin
enum class Theme { LIGHT, DARK, SYSTEM }

val themePref: Preference<Theme> = datastore.enum("theme", Theme.SYSTEM)
```

### Custom Serialized Objects

Store any object by providing serializer/deserializer functions:

```kotlin
@Serializable
data class UserProfile(val id: Int, val email: String)

val userProfilePref: Preference<UserProfile> = datastore.serialized(
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

### Kotlin Serialization (`kserialized`)

Store any `@Serializable` type directly without manual serializer/deserializer functions:

```kotlin
@Serializable
data class UserProfile(val name: String, val age: Int)

val userProfilePref: Preference<UserProfile> = datastore.kserialized(
    key = "user_profile",
    defaultValue = UserProfile(name = "John", age = 25),
)
```

A custom `Json` instance can be provided if needed:

```kotlin
val customJson = Json { prettyPrint = true }

val userProfilePref: Preference<UserProfile> = datastore.kserialized(
    key = "user_profile",
    defaultValue = UserProfile(name = "John", age = 25),
    json = customJson,
)
```

### Serialized Set

Store a `Set` of custom objects using per-element serialization with `serializedSet()`:

```kotlin
val animalSetPref: Preference<Set<Animal>> = datastore.serializedSet(
    key = "animal_set",
    defaultValue = emptySet(),
    serializer = { Animal.to(it) },
    deserializer = { Animal.from(it) },
)
```

Each element is individually serialized to a String and stored using a `stringSetPreferencesKey`.
Elements that fail deserialization are silently skipped.

### Kotlin Serialization Set (`kserializedSet`)

Store a `Set` of `@Serializable` objects without manual serializer/deserializer functions:

```kotlin
@Serializable
data class UserProfile(val name: String, val age: Int)

val profileSetPref: Preference<Set<UserProfile>> = datastore.kserializedSet(
    key = "profile_set",
    defaultValue = emptySet(),
)
```

Each element is individually serialized to JSON and stored using a `stringSetPreferencesKey`.
Elements that fail deserialization are silently skipped. A custom `Json` instance can be provided if
needed.

### Serialized List

Store a `List` of custom objects using per-element serialization with `serializedList()`:

```kotlin
val animalListPref: Preference<List<Animal>> = datastore.serializedList(
    key = "animal_list",
    defaultValue = emptyList(),
    serializer = { Animal.to(it) },
    deserializer = { Animal.from(it) },
)
```

Each element is individually serialized to a String and stored as a JSON array string using a
`stringPreferencesKey`. Elements that fail deserialization are silently skipped. Unlike sets, lists
preserve insertion order and allow duplicates.

### Kotlin Serialization List (`kserializedList`)

Store a `List` of `@Serializable` objects without manual serializer/deserializer functions:

```kotlin
@Serializable
data class UserProfile(val name: String, val age: Int)

val profileListPref: Preference<List<UserProfile>> = datastore.kserializedList(
    key = "profile_list",
    defaultValue = emptyList(),
)
```

The list is serialized to a JSON array string and stored using a `stringPreferencesKey`. If
deserialization fails (e.g., due to corrupted data), the default value is returned. A custom `Json`
instance can be provided if needed.

### Enum Set

Store a `Set` of enum values with the `enumSet()` extension:

```kotlin
val themeSetPref: Preference<Set<Theme>> = datastore.enumSet<Theme>(
    key = "theme_set",
    defaultValue = emptySet(),
)
```

Each enum value is stored by its `name`. Unknown enum values encountered during deserialization are
skipped.

### Nullable Preferences

Create preferences that return `null` when no value has been set, instead of a default value:

```kotlin
val nickname: Preference<String?> = datastore.nullableString("nickname")
val age: Preference<Int?> = datastore.nullableInt("age")
val timestamp: Preference<Long?> = datastore.nullableLong("timestamp")
val weight: Preference<Float?> = datastore.nullableFloat("weight")
val latitude: Preference<Double?> = datastore.nullableDouble("latitude")
val agreed: Preference<Boolean?> = datastore.nullableBool("agreed")
val labels: Preference<Set<String>?> = datastore.nullableStringSet("labels")
```

Setting a nullable preference to `null` removes the key from DataStore. `resetToDefault()` also
removes the key, since the default is `null`.

```kotlin
nickname.get()        // null (not set yet)
nickname.set("Alice") // stores "Alice"
nickname.get()        // "Alice"
nickname.set(null)    // removes the key
nickname.get()        // null
```

### Nullable Enum

Store an enum value that returns `null` when not set:

```kotlin
val themePref: Preference<Theme?> = datastore.nullableEnum<Theme>("theme")
```

### Nullable Custom Serialized Objects

Store a nullable custom-serialized object:

```kotlin
val animalPref: Preference<Animal?> = datastore.nullableSerialized(
    key = "animal",
    serializer = { Animal.to(it) },
    deserializer = { Animal.from(it) },
)
```

### Nullable Kotlin Serialization (`nullableKserialized`)

Store a nullable `@Serializable` type:

```kotlin
@Serializable
data class UserProfile(val name: String, val age: Int)

val userProfilePref: Preference<UserProfile?> =
    datastore.nullableKserialized<UserProfile>("user_profile")
```

### Nullable Serialized List (`nullableSerializedList`)

Store a nullable list of custom-serialized objects:

```kotlin
val animalListPref: Preference<List<Animal>?> = datastore.nullableSerializedList(
    key = "animal_list",
    serializer = { Animal.to(it) },
    deserializer = { Animal.from(it) },
)
```

### Nullable Kotlin Serialization List (`nullableKserializedList`)

Store a nullable list of `@Serializable` objects:

```kotlin
val profileListPref: Preference<List<UserProfile>?> =
    datastore.nullableKserializedList<UserProfile>(
        key = "profile_list",
    )
```

All nullable variants return `null` when the key is not set. Setting `null` removes the key. If
deserialization fails, `null` is returned.

### Reading & Writing Values

Each `Preference<T>` provides multiple access patterns:

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

`DelegatedPreference<T>` implements `ReadWriteProperty`, so you can use it as a delegated property:

```kotlin
var currentUserName: String by userName

// Read (blocking)
println(currentUserName)

// Write (blocking)
currentUserName = "Jane Doe"
```

#### Atomic Update

Atomically read-modify-write a preference value in a single DataStore transaction:

```kotlin
userScore.update { current -> current + 1 }
```

#### Toggle

Flip a `Boolean` preference:

```kotlin
darkMode.toggle()
```

Toggle an item in a `Set` preference (adds if absent, removes if present):

```kotlin
tags.toggle("kotlin")
```

#### Reset to Default

```kotlin
// Suspend
userName.resetToDefault()

// Blocking — avoid on the main/UI thread
userName.resetToDefaultBlocking()
```

#### Key Access

Retrieve the underlying DataStore key name:

```kotlin
val key: String = userName.key()
```

### Batch Operations (WIP documentation)

Batch operations let you read or write multiple preferences in a single DataStore transaction,
avoiding redundant I/O and ensuring atomicity.

#### Batch Read

Read multiple preferences from a single snapshot:

```kotlin
class SettingsViewModel(
    private val datastore: GenericPreferencesDatastore,
) : ViewModel() {

    private val userName = datastore.string("user_name", "Guest")
    private val darkMode = datastore.bool("dark_mode", false)
    private val volume = datastore.float("volume", 1.0f)

    fun loadSettings() {
        viewModelScope.launch {
            val (name, isDark, vol) = datastore.batchGet {
                Triple(this[userName], this[darkMode], this[volume])
            }
        }
    }
}
```

#### Batch Read Flow

Observe multiple preferences reactively from the same snapshot. The flow re-emits whenever any
preference in the datastore changes:

```kotlin
class SettingsViewModel(
    private val datastore: GenericPreferencesDatastore,
) : ViewModel() {

    private val userName = datastore.string("user_name", "Guest")
    private val darkMode = datastore.bool("dark_mode", false)

    val settingsFlow: Flow<Pair<String, Boolean>> = datastore.batchReadFlow().map { scope ->
        scope[userName] to scope[darkMode]
    }.distinctUntilChanged()
}
```

#### Batch Write

Write multiple preferences in a single atomic transaction:

```kotlin
class SettingsViewModel(
    private val datastore: GenericPreferencesDatastore,
) : ViewModel() {

    private val userName = datastore.string("user_name", "Guest")
    private val darkMode = datastore.bool("dark_mode", false)
    private val volume = datastore.float("volume", 1.0f)

    fun resetSettings() {
        viewModelScope.launch {
            datastore.batchWrite {
                this[userName] = "Guest"
                this[darkMode] = false
                this[volume] = 1.0f
            }
        }
    }
}
```

#### Batch Update

Atomically read current values and write new values in a single transaction, guaranteeing
consistency when new values depend on current ones:

```kotlin
class GameViewModel(
    private val datastore: GenericPreferencesDatastore,
) : ViewModel() {

    private val userScore = datastore.int("user_score", 0)
    private val highScore = datastore.long("high_score", 0L)

    fun submitScore(newScore: Int) {
        viewModelScope.launch {
            datastore.batchUpdate {
                this[userScore] = newScore
                val currentHigh = this[highScore]
                if (newScore > currentHigh) {
                    this[highScore] = newScore.toLong()
                }
            }
        }
    }
}
```

The `BatchUpdateScope` also provides `update`, `delete`, and `resetToDefault` helpers:

```kotlin
datastore.batchUpdate {
    update(userScore) { current -> current + 10 }
    delete(nickname)
    resetToDefault(volume)
}
```

#### Blocking Batch Operations

Blocking variants are available for non-coroutine contexts. Avoid calling these on the main/UI
thread:

```kotlin
val (name, isDark) = datastore.batchGetBlocking {
    this[userName] to this[darkMode]
}

datastore.batchWriteBlocking {
    this[userName] = "Guest"
    this[darkMode] = false
}

datastore.batchUpdateBlocking {
    update(userScore) { current -> current + 1 }
}
```

### Mapped Preferences

Transform a `Preference<T>` into a `Preference<R>` with converter functions:

```kotlin
val scoreAsString: Preference<String> = userScore.map(
    defaultValue = "0",
    convert = { it.toString() },
    reverse = { it.toIntOrNull() ?: 0 },
)

// Or infer the default value from the original preference's default:
val scoreAsString2: Preference<String> = userScore.mapIO(
    convert = { it.toString() },
    reverse = { it.toInt() },
)
```

`map` catches exceptions in conversions and falls back to defaults. `mapIO` throws if conversion of
the default value fails.

### Backup & Restore

`GenericPreferencesDatastore` supports exporting and importing all preferences using type-safe
backup models:

#### Export as structured data

```kotlin
val backup: PreferencesBackup = datastore.exportAsData(
    exportPrivate = false,
    exportAppState = false,
)
```

#### Export as JSON string

```kotlin
val jsonString: String = datastore.exportAsString(
    exportPrivate = false,
    exportAppState = false,
)
```

A custom `Json` instance can be provided if needed:

```kotlin
val jsonString: String = datastore.exportAsString(
    exportPrivate = false,
    exportAppState = false,
    json = customJson,
)
```

#### Import from structured data

```kotlin
datastore.importData(
    backup = backup,
    importPrivate = false,
    importAppState = false,
)
```

#### Import from JSON string

```kotlin
datastore.importDataAsString(
    backupString = jsonString,
    importPrivate = false,
    importAppState = false,
)
```

Import merges into existing preferences. Call `datastore.clearAll()` before import for full replace
semantics. A `BackupParsingException` is thrown if the JSON string is invalid or exceeds the 10 MB
size limit.

### Private & App-State Key Prefixes

Use `BasePreference.privateKey(key)` or `BasePreference.appStateKey(key)` to prefix keys so they can
be filtered during backup:

```kotlin
val token = datastore.string(BasePreference.privateKey("auth_token"), "")
val onboarded = datastore.bool(BasePreference.appStateKey("onboarding_done"), false)
```

## Proto DataStore

### Setup Proto DataStore

Use the `createProtoDatastore` factory function to create a `GenericProtoDatastore`:

```kotlin
val protoDatastore = createProtoDatastore(
    serializer = MyProtoSerializer,
    defaultValue = MyProtoMessage.getDefaultInstance(),
    producePath = { context.filesDir.resolve("my_proto.pb").absolutePath },
)
```

A `fileName` overload is available that appends the file name to a directory path:

```kotlin
val protoDatastore = createProtoDatastore(
    serializer = MyProtoSerializer,
    defaultValue = MyProtoMessage.getDefaultInstance(),
    fileName = "my_proto.pb",
    producePath = { context.filesDir.absolutePath },
)
```

Optional parameters allow customizing the key, corruption handler, migrations, and coroutine scope:

```kotlin
val protoDatastore = createProtoDatastore(
    serializer = MyProtoSerializer,
    defaultValue = MyProtoMessage.getDefaultInstance(),
    key = "my_proto",
    corruptionHandler = ReplaceFileCorruptionHandler { MyProtoMessage.getDefaultInstance() },
    migrations = listOf(myMigration),
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    producePath = { context.filesDir.resolve("my_proto.pb").absolutePath },
)
```

Overloads accepting `okio.Path` and `kotlinx.io.files.Path` are also available:

```kotlin
val protoDatastore = createProtoDatastore(
    serializer = MyProtoSerializer,
    defaultValue = MyProtoMessage.getDefaultInstance(),
    produceOkioPath = { "/data/my_proto.pb".toPath() },
)

val protoDatastore = createProtoDatastore(
    serializer = MyProtoSerializer,
    defaultValue = MyProtoMessage.getDefaultInstance(),
    produceKotlinxIoPath = { Path("/data/my_proto.pb") },
)
```

Alternatively, wrap an existing `DataStore<T>` directly:

```kotlin
val protoDatastore = GenericProtoDatastore(
    datastore = myExistingProtoDataStore,
    defaultValue = MyProtoMessage.getDefaultInstance(),
)
```

### Usage

```kotlin
val dataPref: ProtoPreference<MyProtoMessage> = protoDatastore.data()

// Then use get(), set(), asFlow(), etc. just like Preferences DataStore
```

## Compose Extensions (`generic-datastore-compose`)

The `remember()` extension turns any `DelegatedPreference<T>` into a lifecycle-aware
`MutableState<T>`:

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

Under the hood, `remember()` uses `collectAsStateWithLifecycle` for lifecycle-safe collection for
Android while it uses `collectAsState()` for Desktop/JVM. It launches a coroutine for writes.

## License

[Apache License 2.0](LICENSE.md)
