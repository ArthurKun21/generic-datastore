# GenericDataStore Desktop Sample

This is a desktop sample application demonstrating the usage of the GenericDataStore library with Compose Desktop.

## Features

- String preferences
- Integer preferences with increment/decrement
- Boolean preferences with toggle
- Enum preferences (Theme selection)
- Custom serialized objects (Animal)
- Duration/Timestamp handling
- Export/Import preferences to JSON files
- Real-time updates using Compose state

## Running the Application

### From Command Line

```bash
./gradlew :desktop-app:run
```

### Building Distributable

```bash
# Create distributable package for your platform
./gradlew :desktop-app:packageDistributionForCurrentOS

# Or create for all platforms
./gradlew :desktop-app:packageDmg        # macOS
./gradlew :desktop-app:packageMsi        # Windows
./gradlew :desktop-app:packageDeb        # Linux
```

The distributable will be created in `desktop-app/build/compose/binaries/main/`.

## Project Structure

```
desktop-app/
├── src/
│   └── main/
│       └── kotlin/
│           └── io/github/arthurkun/generic/datastore/desktop/
│               ├── Main.kt                  # Application entry point
│               ├── ui/
│               │   ├── MainScreen.kt        # Main UI composables
│               │   └── MainViewModel.kt     # ViewModel
│               └── domain/
│                   ├── PreferenceStore.kt   # Preference definitions
│                   ├── Animal.kt            # Custom serialized model
│                   └── Theme.kt             # Theme enum
└── build.gradle.kts
```

## Key Differences from Android

1. **DataStore Creation**: Uses JVM file system instead of Android Context
2. **File Pickers**: Uses Swing/AWT file choosers instead of ActivityResultContracts
3. **Theme**: Uses MaterialTheme instead of AppCompat
4. **No Android-specific APIs**: Pure Kotlin Multiplatform code

## Dependencies

- `generic-datastore`: Core library
- `generic-datastore-compose`: Compose extensions (remember() function)
- `compose.desktop`: Compose Desktop runtime
- `datastore-preferences`: AndroidX DataStore for JVM
- `kotlinx-serialization-json`: JSON serialization

## Notes

- Preferences are stored in the user's home directory: `~/.genericdatastore/preferences.pb`
- Export/import uses standard file dialogs
- All preference changes are immediately reflected in the UI
