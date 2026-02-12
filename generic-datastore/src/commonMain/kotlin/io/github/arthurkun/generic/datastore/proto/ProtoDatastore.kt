package io.github.arthurkun.generic.datastore.proto

import io.github.arthurkun.generic.datastore.core.DelegatedPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Defines the contract for a Proto DataStore.
 *
 * This interface provides methods to access the proto message as a preference-like wrapper.
 *
 * @param T The proto message type.
 */
public interface ProtoDatastore<T> {
    /**
     * Returns the proto message wrapped as a [DelegatedPreference] instance.
     *
     * @return A [DelegatedPreference] instance for the proto message.
     */
    public fun data(): ProtoPreference<T>

    /**
     * Creates a [ProtoPreference] for an individual field of the proto message.
     *
     * The [getter] extracts the field value from a proto snapshot, and [updater] returns
     * a new proto with the field updated. This works for any nesting depth — for nested
     * fields, use `copy()` chains in [updater].
     *
     * ```kotlin
     * val namePref = protoDatastore.field(
     *     key = "name",
     *     defaultValue = "",
     *     getter = { it.name },
     *     updater = { proto, value -> proto.copy(name = value) },
     * )
     * ```
     *
     * @param F The field type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default value for the field.
     * @param getter A function that extracts the field from the proto snapshot.
     * @param updater A function that returns a new proto with the field updated.
     * @return A [ProtoPreference] for the individual field.
     */
    public fun <F> field(
        key: String,
        defaultValue: F,
        getter: (T) -> F,
        updater: (T, F) -> T,
    ): ProtoPreference<F>

    // --- Enum fields ---

    /**
     * Creates a [ProtoPreference] for an enum field stored as a [String] in the proto message.
     *
     * @param F The enum type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default enum value.
     * @param enumValues All possible enum values (used for deserialization).
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the enum field.
     */
    public fun <F : Enum<F>> enumField(
        key: String,
        defaultValue: F,
        enumValues: Array<F>,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F>

    /**
     * Creates a [ProtoPreference] for a nullable enum field stored as a [String?] in the proto.
     *
     * @param F The enum type.
     * @param key A unique key identifying this field preference.
     * @param enumValues All possible enum values (used for deserialization).
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable enum field.
     */
    public fun <F : Enum<F>> nullableEnumField(
        key: String,
        enumValues: Array<F>,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?>

    /**
     * Creates a [ProtoPreference] for a [Set] of enum values stored as [Set]<[String]> in the proto.
     *
     * @param F The enum type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default set value.
     * @param enumValues All possible enum values (used for deserialization).
     * @param getter Extracts the raw [Set]<[String]> field from the proto snapshot.
     * @param updater Returns a new proto with the raw [Set]<[String]> field updated.
     * @return A [ProtoPreference] for the enum set field.
     */
    public fun <F : Enum<F>> enumSetField(
        key: String,
        defaultValue: Set<F> = emptySet(),
        enumValues: Array<F>,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>>

    // --- KSerialized fields ---

    /**
     * Creates a [ProtoPreference] for a field serialized as JSON using [KSerializer].
     * The proto field type is [String].
     *
     * @param F The field type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default value for the field.
     * @param serializer The [KSerializer] for the field type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized field.
     */
    public fun <F> kserializedField(
        key: String,
        defaultValue: F,
        serializer: KSerializer<F>,
        json: Json? = null,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F>

    /**
     * Creates a [ProtoPreference] for a nullable field serialized as JSON using [KSerializer].
     * The proto field type is [String?].
     *
     * @param F The field type (non-nullable).
     * @param key A unique key identifying this field preference.
     * @param serializer The [KSerializer] for the field type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized field.
     */
    public fun <F : Any> nullableKserializedField(
        key: String,
        serializer: KSerializer<F>,
        json: Json? = null,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?>

    /**
     * Creates a [ProtoPreference] for a [List] field serialized as a JSON array using [KSerializer].
     * The proto field type is [String].
     *
     * @param F The element type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default list value.
     * @param serializer The [KSerializer] for the element type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized list field.
     */
    public fun <F> kserializedListField(
        key: String,
        defaultValue: List<F> = emptyList(),
        serializer: KSerializer<F>,
        json: Json? = null,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>>

    /**
     * Creates a [ProtoPreference] for a nullable [List] field serialized as JSON using [KSerializer].
     * The proto field type is [String?].
     *
     * @param F The element type.
     * @param key A unique key identifying this field preference.
     * @param serializer The [KSerializer] for the element type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized list field.
     */
    public fun <F> nullableKserializedListField(
        key: String,
        serializer: KSerializer<F>,
        json: Json? = null,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?>

    /**
     * Creates a [ProtoPreference] for a [Set] field where each element is individually
     * JSON-serialized using [KSerializer]. The proto field type is [Set]<[String]>.
     *
     * @param F The element type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default set value.
     * @param serializer The [KSerializer] for the element type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [Set]<[String]> field from the proto snapshot.
     * @param updater Returns a new proto with the raw [Set]<[String]> field updated.
     * @return A [ProtoPreference] for the serialized set field.
     */
    public fun <F> kserializedSetField(
        key: String,
        defaultValue: Set<F> = emptySet(),
        serializer: KSerializer<F>,
        json: Json? = null,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>>

    // --- Serialized fields (caller-provided functions) ---

    /**
     * Creates a [ProtoPreference] for a field using caller-provided serialization functions.
     * The proto field type is [String].
     *
     * @param F The field type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default value for the field.
     * @param serializer Function to serialize the value to [String].
     * @param deserializer Function to deserialize a [String] to the value.
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized field.
     */
    public fun <F> serializedField(
        key: String,
        defaultValue: F,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F>

    /**
     * Creates a [ProtoPreference] for a nullable field using caller-provided serialization functions.
     * The proto field type is [String?].
     *
     * @param F The field type (non-nullable).
     * @param key A unique key identifying this field preference.
     * @param serializer Function to serialize the value to [String].
     * @param deserializer Function to deserialize a [String] to the value.
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized field.
     */
    public fun <F : Any> nullableSerializedField(
        key: String,
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?>

    /**
     * Creates a [ProtoPreference] for a [List] field using caller-provided per-element serialization.
     * The proto field type is [String] (JSON array of individually serialized elements).
     *
     * @param F The element type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default list value.
     * @param elementSerializer Function to serialize each element to [String].
     * @param elementDeserializer Function to deserialize each [String] element.
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized list field.
     */
    public fun <F> serializedListField(
        key: String,
        defaultValue: List<F> = emptyList(),
        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<List<F>>

    /**
     * Creates a [ProtoPreference] for a nullable [List] field using caller-provided per-element serialization.
     * The proto field type is [String?].
     *
     * @param F The element type.
     * @param key A unique key identifying this field preference.
     * @param elementSerializer Function to serialize each element to [String].
     * @param elementDeserializer Function to deserialize each [String] element.
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized list field.
     */
    public fun <F> nullableSerializedListField(
        key: String,
        elementSerializer: (F) -> String,
        elementDeserializer: (String) -> F,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<List<F>?>

    /**
     * Creates a [ProtoPreference] for a [Set] field using caller-provided per-element serialization.
     * The proto field type is [Set]<[String]>.
     *
     * @param F The element type.
     * @param key A unique key identifying this field preference.
     * @param defaultValue The default set value.
     * @param serializer Function to serialize each element to [String].
     * @param deserializer Function to deserialize each [String] element.
     * @param getter Extracts the raw [Set]<[String]> field from the proto snapshot.
     * @param updater Returns a new proto with the raw [Set]<[String]> field updated.
     * @return A [ProtoPreference] for the serialized set field.
     */
    public fun <F> serializedSetField(
        key: String,
        defaultValue: Set<F> = emptySet(),
        serializer: (F) -> String,
        deserializer: (String) -> F,
        getter: (T) -> Set<String>,
        updater: (T, Set<String>) -> T,
    ): ProtoPreference<Set<F>>
}

// --- Reified extension functions ---

/**
 * Creates a [ProtoPreference] for an enum field, inferring [enumValues] from the reified type.
 */
public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.enumField(
    key: String,
    defaultValue: F,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<F> = enumField(
    key = key,
    defaultValue = defaultValue,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a nullable enum field, inferring [enumValues] from the reified type.
 */
public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.nullableEnumField(
    key: String,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<F?> = nullableEnumField(
    key = key,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a [Set] of enum values, inferring [enumValues] from the reified type.
 */
public inline fun <T, reified F : Enum<F>> ProtoDatastore<T>.enumSetField(
    key: String,
    defaultValue: Set<F> = emptySet(),
    noinline getter: (T) -> Set<String>,
    noinline updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = enumSetField(
    key = key,
    defaultValue = defaultValue,
    enumValues = enumValues<F>(),
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a JSON-serialized field, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F> ProtoDatastore<T>.kserializedField(
    key: String,
    defaultValue: F,
    json: Json? = null,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<F> = kserializedField(
    key = key,
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
    key: String,
    json: Json? = null,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<F?> = nullableKserializedField(
    key = key,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a JSON-serialized [List] field, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F> ProtoDatastore<T>.kserializedListField(
    key: String,
    defaultValue: List<F> = emptyList(),
    json: Json? = null,
    noinline getter: (T) -> String,
    noinline updater: (T, String) -> T,
): ProtoPreference<List<F>> = kserializedListField(
    key = key,
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
    key: String,
    json: Json? = null,
    noinline getter: (T) -> String?,
    noinline updater: (T, String?) -> T,
): ProtoPreference<List<F>?> = nullableKserializedListField(
    key = key,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)

/**
 * Creates a [ProtoPreference] for a [Set] field with per-element JSON serialization, inferring [KSerializer] from the reified type.
 */
public inline fun <T, reified F> ProtoDatastore<T>.kserializedSetField(
    key: String,
    defaultValue: Set<F> = emptySet(),
    json: Json? = null,
    noinline getter: (T) -> Set<String>,
    noinline updater: (T, Set<String>) -> T,
): ProtoPreference<Set<F>> = kserializedSetField(
    key = key,
    defaultValue = defaultValue,
    serializer = serializer<F>(),
    json = json,
    getter = getter,
    updater = updater,
)
