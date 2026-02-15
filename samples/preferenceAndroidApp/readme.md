This Android app example uses manual dependency injection to keep dependencies minimal.

The shared [AppContainer](../composeApp/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.kt) (with its [Android-specific implementation](../composeApp/src/androidMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.android.kt)) is instantiated in [MainApplication](./src/main/java/io/github/arthurkun/generic/datastore/app/MainApplication.kt) and retrieved in [MainActivity](./src/main/java/io/github/arthurkun/generic/datastore/app/MainActivity.kt), where it provides the [PreferenceStore](../composeApp/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/PreferenceStore.kt) to the shared [MainScreen](../composeApp/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/ui/MainScreen.kt).

The app also observes the theme preference and applies it via `AppCompatDelegate` using [setAppCompatDelegateThemeMode](./src/main/java/io/github/arthurkun/generic/datastore/app/domain/Theme.kt).

You can initialize preferences in the datastore like this:

[PreferenceStore](../composeApp/src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/PreferenceStore.kt)

```kotlin
class PreferenceStore(
    private val datastore: GenericPreferencesDatastore,
) {

    val theme = datastore.enum(
        "theme",
        Theme.SYSTEM,
    )

    val text = datastore.string(
        "text",
        defaultValue = "Hello World!",
    )

    val num = datastore.int(
        "num",
        defaultValue = 0,
    )

    val bool = datastore.bool(
        "bool",
        defaultValue = false,
    )

    val customObject = datastore.serialized(
        key = "animal",
        defaultValue = Animal.Dog,
        serializer = { Animal.to(it) },
        deserializer = { Animal.from(it) },
    )

    val duration = datastore.long(
        key = "duration",
        defaultValue = 0L,
    ).mapIO(
        convert = {
            Instant.fromEpochMilliseconds(it)
        },
        reverse = {
            it.toEpochMilliseconds()
        },
    )

    suspend fun exportPreferences() = datastore.export()

    suspend fun importPreferences(data: Map<String, Any>) = datastore.import(data)
}
```
