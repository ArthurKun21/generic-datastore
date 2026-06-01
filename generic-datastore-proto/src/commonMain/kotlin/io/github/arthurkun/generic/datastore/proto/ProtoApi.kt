package io.github.arthurkun.generic.datastore.proto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Creates a [ProtoPreference] for an enum field, inferring [enumValues] from the reified type.
 */
public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.enumField(
    defaultValue: F,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<F> = enumField(
    defaultValue = defaultValue,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a nullable enum field, inferring [enumValues] from the reified type.
 */
public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.nullableEnumField(
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<F?> = nullableEnumField(
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a [Set] of enum values, inferring [enumValues] from the reified type.
 */
public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.enumSetField(
    defaultValue: Set<F> = emptySet(),
    noinline getter: (T) -> Set<String>,
    noinline updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = enumSetField(
    defaultValue = defaultValue,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a JSON-serialized field, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F> ProtoDatastore<T>.kserializedField(
    defaultValue: F,
    json: Json? = null,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<F> = kserializedField(
    defaultValue = defaultValue,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a nullable JSON-serialized field, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F : Any> ProtoDatastore<T>.nullableKserializedField(
    json: Json? = null,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<F?> = nullableKserializedField(
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a JSON-serialized [List] field, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F> ProtoDatastore<T>.kserializedListField(
    defaultValue: List<F> = emptyList(),
    json: Json? = null,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<List<F>> = kserializedListField(
    defaultValue = defaultValue,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a nullable JSON-serialized [List] field, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F> ProtoDatastore<T>.nullableKserializedListField(
    json: Json? = null,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<List<F>?> = nullableKserializedListField(
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a [Set] field with per-element JSON serialization, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F> ProtoDatastore<T>.kserializedSetField(
    defaultValue: Set<F> = emptySet(),
    json: Json? = null,
    noinline getter: (T) -> Set<String>,
    noinline updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = kserializedSetField(
    defaultValue = defaultValue,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)
