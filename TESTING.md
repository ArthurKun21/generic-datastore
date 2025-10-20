# Testing Guide

This project includes comprehensive test coverage across multiple test types:

## Test Structure

### Common Tests (`src/commonTest`)
Platform-agnostic tests that run on all platforms:
- **ValidationTest**: Input validation and error handling
- **ExportImportTest**: Data export/import functionality
- **MigrationTest**: JSON serialization/deserialization

### Desktop Tests (`src/desktopTest`)
JVM-specific tests that run on desktop platforms:
- **DesktopDatastoreInstrumentedTest**: All preference types
- **DesktopDatastoreBlockingTest**: Blocking operations

### Android Instrumented Tests (`src/androidInstrumentedTest`)
Android-specific tests that run on device/emulator:
- **AndroidDatastoreInstrumentedTest**: All preference types
- **AndroidValidationInstrumentedTest**: Validation on Android
- **AndroidExportImportInstrumentedTest**: Export/import on Android
- **AndroidPerformanceTest**: Performance benchmarks

## Running Tests

### Run all tests
```bash
./gradlew :library:test
```

### Run desktop tests only
```bash
./gradlew :library:desktopTest
```

### Run Android instrumented tests
```bash
./gradlew :library:connectedAndroidTest
```

### Run specific test class
```bash
./gradlew :library:desktopTest --tests "ValidationTest"
```

## Test Coverage

### Input Validation
- ✅ Blank key rejection for all preference types
- ✅ Whitespace key rejection
- ✅ Valid key acceptance

### Error Handling
- ✅ Serialization errors handled gracefully
- ✅ Deserialization errors return default values
- ✅ Conversion errors in mapped preferences
- ✅ Invalid enum values handled
- ✅ DataStore operation errors

### Export/Import
- ✅ All data types exported correctly
- ✅ Privacy controls (private/app state preferences)
- ✅ Round-trip import/export
- ✅ Invalid data handling
- ✅ String set support

### JSON Migration
- ✅ All primitive types to JSON
- ✅ Nested structures
- ✅ Array and object conversion
- ✅ Null value handling
- ✅ Invalid JSON error handling

### Performance Benchmarks
All tests verify operations complete within reasonable time limits:
- ✅ 100 writes < 5 seconds
- ✅ 100 reads < 2 seconds
- ✅ 300 mixed operations < 5 seconds
- ✅ Export operations
- ✅ Import operations
- ✅ Mapped preference operations
- ✅ Serialized preference operations
- ✅ Concurrent reads

## Adding New Tests

### For Common Tests
Create test in `src/commonTest/kotlin/.../`:
```kotlin
class MyNewTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferenceDatastore: GenericPreferenceDatastore
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setup() {
        testScope = CoroutineScope(Job() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
        ) {
            createTempFile("test", ".preferences_pb").also { it.deleteOnExit() }
        }
        preferenceDatastore = GenericPreferenceDatastore(dataStore)
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun myTest() = runTest(testDispatcher) {
        // Your test code
    }
}
```

### For Android Tests
Create test in `src/androidInstrumentedTest/kotlin/.../`:
```kotlin
@RunWith(AndroidJUnit4::class)
class MyAndroidTest {
    private lateinit var testContext: Context

    @Before
    fun setup() {
        testContext = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun myTest() = runTest {
        // Your test code
    }
}
```

## Continuous Integration

Tests are designed to run in CI environments:
- Fast execution (most tests complete in milliseconds)
- No external dependencies
- Deterministic results
- Proper cleanup

## Best Practices

1. **Always clean up**: Use `@AfterTest` to cancel scopes and delete files
2. **Use test dispatcher**: For deterministic testing
3. **Test edge cases**: Invalid input, errors, null values
4. **Verify behavior**: Don't just test happy path
5. **Performance limits**: Set reasonable time limits for operations
