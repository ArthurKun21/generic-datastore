# ProtoApp

A sample desktop (JVM) app demonstrating **Proto DataStore** integration using
[Square Wire](https://github.com/square/wire) for protocol buffer code generation and
`ProtoDatastore` from the `:generic-datastore-proto` module.

## Running

```bash
./gradlew :protoApp:run
```

## Features demonstrated

- Proto2 and Proto3 message definitions with nested messages
- `ProtoDatastore.data()` for whole-object proto access
- `ProtoDatastore.field()` for individual field access (including nested fields)
- Proto enum, set, Kotlin-serialized, and caller-serialized field APIs
- All `BasePreference` operations: `get`, `set`, `update`, `delete`, `asFlow`, `stateIn`,
  `resetToDefault`, `getBlocking`, `setBlocking`
- `DelegatedPreference` property delegation and `resetToDefaultBlocking`
- Compose `remember()` integration with proto preferences
