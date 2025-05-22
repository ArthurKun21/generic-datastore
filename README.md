# Generic Datastore Library

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

The library offers a `PreferenceDatastore` interface and its implementation `GenericPreferenceDatastore`. You initialize `GenericPreferenceDatastore` with your `DataStore<Preferences>` instance. Then, you can use its methods like `string()`, `long()`, `int()`, `float()`, `bool()`, `stringSet()`, and `serialized()` to create `Prefs<T>` objects. These `Prefs<T>` objects allow you to easily get (as a Flow) and set preference values.

## Example Usage (Conceptual)

```kotlin
// Initialize DataStore (this is standard DataStore setup)
val Context.myDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Initialize GenericPreferenceDatastore
val genericDatastore = GenericPreferenceDatastore(context.myDataStore)

// Define preferences
val userNamePref: Prefs<String> = genericDatastore.string("user_name", "Guest")
val userScorePref: Prefs<Int> = genericDatastore.int("user_score", 0)

// Example for a custom object
data class UserProfile(val id: Int, val email: String)
val userProfilePref: Prefs<UserProfile> = genericDatastore.serialized(
    key = "user_profile",
    defaultValue = UserProfile(0, ""),
    serializer = { profile -> gson.toJson(profile) }, // Replace with your serialization logic
    deserializer = { json -> gson.fromJson(json, UserProfile::class.java) } // Replace with your deserialization logic
)

// Using the preferences
CoroutineScope(Dispatchers.IO).launch {
    // Get a value
    val currentUserName = userNamePref.get().first()
    println("Current user: $currentUserName")

    // Set a value
    userNamePref.set("John Doe")

    // Get a custom object
    val profile = userProfilePref.get().first()
    println("User email: ${profile.email}")

    // Set a custom object
    userProfilePref.set(UserProfile(1, "john.doe@example.com"))
}
```

This library aims to reduce boilerplate code when working with Jetpack DataStore for common preference types and custom objects.
