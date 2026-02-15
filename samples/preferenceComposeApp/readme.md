This Compose Multiplatform app example uses manual dependency injection to keep dependencies minimal. It targets both Android and Desktop (JVM).

Shared code lives in `commonMain`. The [AppContainer](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.kt) is declared as an `expect class` with platform-specific `actual` implementations for [Android](./src/androidMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.android.kt) and [Desktop](./src/desktopMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.desktop.kt).

The Desktop entry point is [Main.kt](./src/desktopMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/Main.kt), which creates an `AppContainer`, observes the theme preference using the `remember()` Compose extension, and passes the [PreferenceStore](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/PreferenceStore.kt) to the shared [MainScreen](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/ui/MainScreen.kt).

On Desktop, the DataStore file is stored under `~/.generic-datastore-sample/`.

You can initialize preferences in the datastore like this:

[PreferenceStore](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/PreferenceStore.kt)

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
