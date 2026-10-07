This Compose Multiplatform app example uses manual dependency injection to keep dependencies minimal. It targets both Android and Desktop (JVM).

Shared code lives in `commonMain`. The [AppContainer](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.kt) is declared as an `expect class` with platform-specific `actual` implementations for [Android](./src/androidMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.android.kt) and [Desktop](./src/jvmMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/AppContainer.desktop.kt).

The Desktop entry point is [Main.kt](./src/jvmMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/Main.kt), which creates an `AppContainer`, observes the theme preference using the `remember()` Compose extension, and passes the [PreferenceStore](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/PreferenceStore.kt) to the shared [MainScreen](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/ui/MainScreen.kt).

On Desktop, the DataStore file is stored under `~/.generic-datastore-sample/`.

You can initialize preferences in the datastore like this:

[PreferenceStore](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/PreferenceStore.kt)

```kotlin
class PreferenceStore(
    private val datastore: PreferencesDatastore,
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

    // Custom values whose serialized form is a primitive are stored under int/long/float/double
    // keys instead of a string entry.
    val fontScale = datastore.serializedAsInt(
        key = "font_scale",
        defaultValue = FontScale(percent = 100),
        serializer = { it.percent },
        deserializer = { FontScale(it) },
    )

    val sessionTimeout = datastore.serializedAsLong(
        key = "session_timeout",
        defaultValue = SessionTimeout(millis = 30_000L),
        serializer = { it.millis },
        deserializer = { SessionTimeout(it) },
    )

    val volume = datastore.serializedAsFloat(
        key = "volume",
        defaultValue = Volume(level = 0.5f),
        serializer = { it.level },
        deserializer = { Volume(it) },
    )

    val latitude = datastore.serializedAsDouble(
        key = "latitude",
        defaultValue = Latitude(degrees = 0.0),
        serializer = { it.degrees },
        deserializer = { Latitude(it) },
    )

    val nullableSessionTimeout = datastore.nullableSerializedAsLong(
        key = "nullable_session_timeout",
        serializer = { it.millis },
        deserializer = { SessionTimeout(it) },
    )

    suspend fun exportPreferences() = datastore.exportAsString()

    suspend fun importPreferences(backupString: String) =
        datastore.importDataAsString(backupString)
}
```

### Numeric-backed custom values

The [`Serialized as Primitives`](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/ui/MainScreen.kt)
section demonstrates `serializedAsInt`, `serializedAsLong`, `serializedAsFloat`,
`serializedAsDouble`, and `nullableSerializedAsLong`. The default values are declared in
[PreferenceStore](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/PreferenceStore.kt)
and the small value types they wrap live in
[NumericSettings.kt](./src/commonMain/kotlin/io/github/arthurkun/generic/datastore/compose/app/domain/NumericSettings.kt).

Unlike `serialized` (which stores a string entry), these variants store the serialized form under
`intPreferencesKey`, `longPreferencesKey`, `floatPreferencesKey`, or `doublePreferencesKey`. Missing
keys and decode failures fall back to the default value, and the nullable variant returns `null`
when unset (writing `null` removes the key). Because DataStore matches keys by name, avoid reusing
one key across different storage kinds.
