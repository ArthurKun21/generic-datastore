# Generic Preference Datastore Library

Inspired by the [flow-preferences](https://github.com/tfcporciuncula/flow-preferences) library for shared-preferences. I wanted to create a preference datastore version of it.

This library provides a convenient way to work with AndroidX Jetpack DataStore by offering a `GenericPreferenceDatastore` class. This class simplifies the creation and management of various preference types.

## Features

The `GenericPreferenceDatastore` wraps a `DataStore<Preferences>` instance and provides methods to easily define and use preferences for:

*   **Primitive Types:**
    *   String
    *   Long
    *   Int
    *   Float
    *   Boolean
    *   Set<String>
*   **Custom Objects:**
    *   Supports storing any custom object by providing serializer and deserializer functions (e.g., using Gson or Kotlinx Serialization).

## How it Works

The library offers a `PreferenceDatastore` interface and its implementation `GenericPreferenceDatastore`. You initialize `GenericPreferenceDatastore` with your `DataStore<Preferences>` instance. Then, you can use its methods like `string()`, `long()`, `int()`, `float()`, `bool()`, `stringSet()`, and `serialized()` to create `Prefs<T>` objects. These `Prefs<T>` objects provide flexible ways to interact with your preferences:

*   **Asynchronous Operations:** Use the `get()` suspend function to retrieve values and `set(value: T)` suspend function to store values within coroutines.
*   **Flow-based Observation:** Use `asFlow()` to get a `Flow<T>` that emits updates whenever the preference changes, or `stateIn(scope)` to get a `StateFlow<T>`.
*   **Synchronous Access & Fire-and-Forget Update:**
    *   `getValue()`: Synchronously retrieves the current value from DataStore. It blocks the calling thread (using `runBlocking`) until the value is available. While useful for immediate access, it should be used cautiously, especially on UI threads, to prevent unresponsiveness.
    *   `setValue(value: T)`: Sets the preference value from a non-suspending context. This method launches a fire-and-forget coroutine internally to perform the update.

## Example Usage (Conceptual)

```kotlin
// Initialize DataStore (this is standard DataStore setup)
val Context.myDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Initialize GenericPreferenceDatastore
val genericDatastore = GenericPreferenceDatastore(context.myDataStore)

// Define preferences
val userNamePref: Prefs<String> = genericDatastore.string("user_name", "Guest")
val userScorePref: Prefs<Int> = genericDatastore.int("user_score", 0)
```

```kotlin
// Example for a custom object
@Serializable // Add Kotlinx Serialization annotation
data class UserProfile(val id: Int, val email: String)

val userProfilePref: Prefs<UserProfile> = genericDatastore.serialized(
    key = "user_profile",
    defaultValue = UserProfile(0, ""),
    serializer = { profile -> Json.encodeToString(UserProfile.serializer(), profile) }, 
    deserializer = { json -> Json.decodeFromString(UserProfile.serializer(), json) }
)

// Using the preferences
CoroutineScope(Dispatchers.IO).launch {
    // Get a value
    val currentUserName = userNamePref.get()
    println("Current user: $currentUserName")

    // Set a value
    userNamePref.set("John Doe")

    // Get a custom object
    val profile = userProfilePref.get()
    println("User email: ${profile.email}")

    // Set a custom object
    userProfilePref.set(UserProfile(1, "john.doe@example.com"))
}
```

Or you can check this example with sealed class

[Animal.kt](app/src/main/java/io/github/arthurkun/generic/datastore/app/domain/Animal.kt)

```kotlin
sealed class Animal(val name: String) {

    data object Dog : Animal("Dog")

    data object Cat : Animal("Cat")

    override fun toString(): String = name

    companion object {
        fun from(value: String): Animal {
            return when (value) {
                "Dog" -> Dog
                "Cat" -> Cat
                else -> throw Exception("Unknown animal type: $value")
            }
        }

        fun to(animal: Animal): String {
            return animal.name
        }

        val entries by lazy {
            listOf(
                Dog,
                Cat,
            )
        }
    }
}


val customObject = datastore.serialized(
    key = "animal",
    defaultValue = Animal.Dog,
    serializer = { Animal.to(it) },
    deserializer = { Animal.from(it) },
)
```

## Synchronous Access & Property Delegation
The library provides multiple ways to access and modify preference values synchronously or from non-suspending contexts.

### Delegated Properties
`Prefs<T>` implements the `ReadWriteProperty` interface, allowing you to use preferences as delegated properties. This is a concise way to work with preference values directly as if they were regular variables.

```kotlin
// Assuming userNamePref is a Prefs<String> (e.g., genericDatastore.string("user_name", "Guest"))
var currentUserName: String by userNamePref

// Read the value
println("Current user name: $currentUserName")

// Update the value
currentUserName = "Jane Doe" 
// This will internally call setValue and update the DataStore
```

### Direct Synchronous Access
You can also access values synchronously or set them from non-suspending contexts using `getValue()` and `setValue()`:

```kotlin
// Assuming userScorePref is a Prefs<Int>

// Synchronous read - BE CAUTIOUS
// This blocks the current thread until the value is retrieved.
// Avoid on the main thread to prevent UI unresponsiveness.
val currentScore = userScorePref.getValue()
println("Current score via getValue(): $currentScore")

// Fire-and-forget write from a non-suspending context
userScorePref.setValue(100)

// The value is updated in the DataStore.
// You can observe this change via its Flow or by calling get()/getValue() later.
// For example, in a coroutine:
// CoroutineScope(Dispatchers.Main).launch {
//     delay(100) // give a moment for the background write
//     println("Score after setValue: ${userScorePref.get()}")
// }
```

**Note:** When using `getValue()` for synchronous reads, be cautious. This method blocks the current
thread by calling the suspending `get()` function within `runBlocking`. This can lead to UI
unresponsiveness if the DataStore operation is slow, especially if called on the main thread. For
non-blocking alternatives, consider using `asFlow()`, `stateIn()`, or calling `get()` from within a
coroutine. Property delegation (`by userNamePref`) for reads also relies on this synchronous
`getValue()` mechanism. Writes via delegation or `setValue` are generally safe fire-and-forget
operations.

## Usage in Jetpack Compose

You can easily integrate these preferences into your Jetpack Compose UI using the `.remember()` extension function. This function observes the preference value and provides a `MutableState` that recomposes your UI when the preference changes.

Under the hood, `.remember()` utilizes `collectAsStateWithLifecycle` to ensure that the preference value is collected in a lifecycle-aware manner, preventing unnecessary work.

For the implementation you can take a look here in [Remember.kt](library/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/Remember.kt).

Here's an example of how you might use it in a Composable function:

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// In your Composable function
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    // Assuming preferenceStore is an instance of GenericPreferenceDatastore
    // and 'userNamePref' is a Prefs<String> defined as shown previously.
    var userName by viewModel.preferenceStore.userNamePref.remember()

    Column {
        Text("Current User: $userName")
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Enter username") }
        )
    }
}
```

This library aims to reduce boilerplate code when working with Jetpack DataStore for common preference types and custom objects.
