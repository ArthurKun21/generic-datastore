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
     *     defaultValue = "",
     *     getter = { it.name },
     *     updater = { proto, value -> proto.copy(name = value) },
     * )
     * ```
     *
     * @param F The field type.
     * @param defaultValue The default value for the field.
     * @param getter A function that extracts the field from the proto snapshot.
     * @param updater A function that returns a new proto with the field updated.
     * @return A [ProtoPreference] for the individual field.
     */
    public fun <F> field(
        defaultValue: F,
        getter: (T) -> F,
        updater: (T, F) -> T,
    ): ProtoPreference<F>

    // --- Enum fields ---

    /**
     * Creates a [ProtoPreference] for an enum field stored as a [String] in the proto message.
     *
     * @param F The enum type.
     * @param defaultValue The default enum value.
     * @param enumValues All possible enum values (used for deserialization).
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the enum field.
     */
    public fun <F : Enum<F>> enumField(
        defaultValue: F,
        enumValues: Array<F>,
        getter: (T) -> String,
        updater: (T, String) -> T,
    ): ProtoPreference<F>

    /**
     * Creates a [ProtoPreference] for a nullable enum field stored as a [String?] in the proto.
     *
     * @param F The enum type.
     * @param enumValues All possible enum values (used for deserialization).
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable enum field.
     */
    public fun <F : Enum<F>> nullableEnumField(
        enumValues: Array<F>,
        getter: (T) -> String?,
        updater: (T, String?) -> T,
    ): ProtoPreference<F?>

    /**
     * Creates a [ProtoPreference] for a [Set] of enum values stored as [Set]<[String]> in the proto.
     *
     * @param F The enum type.
     * @param defaultValue The default set value.
     * @param enumValues All possible enum values (used for deserialization).
     * @param getter Extracts the raw [Set]<[String]> field from the proto snapshot.
     * @param updater Returns a new proto with the raw [Set]<[String]> field updated.
     * @return A [ProtoPreference] for the enum set field.
     */
    public fun <F : Enum<F>> enumSetField(
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
     * @param defaultValue The default value for the field.
     * @param serializer The [KSerializer] for the field type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized field.
     */
    public fun <F> kserializedField(
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
     * @param serializer The [KSerializer] for the field type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized field.
     */
    public fun <F : Any> nullableKserializedField(
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
     * @param defaultValue The default list value.
     * @param serializer The [KSerializer] for the element type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized list field.
     */
    public fun <F> kserializedListField(
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
     * @param serializer The [KSerializer] for the element type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized list field.
     */
    public fun <F> nullableKserializedListField(
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
     * @param defaultValue The default set value.
     * @param serializer The [KSerializer] for the element type.
     * @param json Optional [Json] instance; defaults to [io.github.arthurkun.generic.datastore.core.PreferenceDefaults.defaultJson].
     * @param getter Extracts the raw [Set]<[String]> field from the proto snapshot.
     * @param updater Returns a new proto with the raw [Set]<[String]> field updated.
     * @return A [ProtoPreference] for the serialized set field.
     */
    public fun <F> kserializedSetField(
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
     * @param defaultValue The default value for the field.
     * @param serializer Function to serialize the value to [String].
     * @param deserializer Function to deserialize a [String] to the value.
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized field.
     */
    public fun <F> serializedField(
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
     * @param serializer Function to serialize the value to [String].
     * @param deserializer Function to deserialize a [String] to the value.
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized field.
     */
    public fun <F : Any> nullableSerializedField(
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
     * @param defaultValue The default list value.
     * @param elementSerializer Function to serialize each element to [String].
     * @param elementDeserializer Function to deserialize each [String] element.
     * @param getter Extracts the raw [String] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String] field updated.
     * @return A [ProtoPreference] for the serialized list field.
     */
    public fun <F> serializedListField(
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
     * @param elementSerializer Function to serialize each element to [String].
     * @param elementDeserializer Function to deserialize each [String] element.
     * @param getter Extracts the raw [String?] field from the proto snapshot.
     * @param updater Returns a new proto with the raw [String?] field updated.
     * @return A [ProtoPreference] for the nullable serialized list field.
     */
    public fun <F> nullableSerializedListField(
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
     * @param defaultValue The default set value.
     * @param serializer Function to serialize each element to [String].
     * @param deserializer Function to deserialize each [String] element.
     * @param getter Extracts the raw [Set]<[String]> field from the proto snapshot.
     * @param updater Returns a new proto with the raw [Set]<[String]> field updated.
     * @return A [ProtoPreference] for the serialized set field.
     */
    public fun <F> serializedSetField(
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
