This app example uses manual dependency injection in order to lessen the dependency needed to run the app.

You can refer to the creation of the GenericPreferenceDataStore here in [AppContainer](app/src/main/java/io/github/arthurkun/generic/datastore/app/AppContainer.kt) and was injected from the [MainApplication](app/src/main/java/io/github/arthurkun/generic/datastore/app/MainApplication.kt) class.

It is finally retrieved in the [MainActivity](app/src/main/java/io/github/arthurkun/generic/datastore/app/MainActivity.kt) class, where it is used to create the [MainViewModel](app/src/main/java/io/github/arthurkun/generic/datastore/app/ui/MainViewModel.kt) class.

You can then initialized the preferences in datastore in this way

[PreferenceStore](app/src/main/java/io/github/arthurkun/generic/datastore/app/domain/PreferenceStore.kt)

```kotlin
class PreferenceStore(
    datastore: GenericPreferenceDatastore,
) {

    val theme = datastore.enum(
        "theme",
        defaultValue = Theme.SYSTEM,
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
}
```