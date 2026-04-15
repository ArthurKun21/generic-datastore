package io.github.arthurkun.generic.datastore.preferences.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One entry inside a [PreferencesBackup].
 *
 * The [key] stores the raw DataStore preference name and [value] keeps the typed payload needed
 * to restore it later.
 */
@Serializable
public data class BackupPreference(
    @SerialName("key")
    val key: String,
    @SerialName("value")
    val value: PreferenceValue,
)

/**
 * Typed backup representation for the preference value types supported by this library's backup
 * format.
 */
@Serializable
public sealed interface PreferenceValue {
    public fun getValue(): Any

    public companion object {
        /**
         * Converts a runtime value to the matching [PreferenceValue] subtype.
         *
         * Returns `null` when the value type is not part of the supported Preferences backup set.
         */
        public fun fromAny(value: Any?): PreferenceValue? {
            return when (value) {
                is Int -> IntPreferenceValue(value)

                is Long -> LongPreferenceValue(value)

                is Float -> FloatPreferenceValue(value)

                is Double -> DoublePreferenceValue(value)

                is String -> StringPreferenceValue(value)

                is Boolean -> BooleanPreferenceValue(value)

                is Set<*> -> {
                    if (value.all { it is String }) {
                        @Suppress("UNCHECKED_CAST")
                        StringSetPreferenceValue(value as Set<String>)
                    } else {
                        null
                    }
                }

                else -> null
            }
        }
    }
}

/**
 * A [PreferenceValue] holding an [Int].
 */
@Serializable
@SerialName("int")
public data class IntPreferenceValue(
    @SerialName("value")
    val value: Int,
) : PreferenceValue {
    override fun getValue(): Any = value
}

/**
 * A [PreferenceValue] holding a [Long].
 */
@Serializable
@SerialName("long")
public data class LongPreferenceValue(
    @SerialName("value")
    val value: Long,
) : PreferenceValue {
    override fun getValue(): Any = value
}

/**
 * A [PreferenceValue] holding a [Float].
 */
@Serializable
@SerialName("float")
public data class FloatPreferenceValue(
    @SerialName("value")
    val value: Float,
) : PreferenceValue {
    override fun getValue(): Any = value
}

/**
 * A [PreferenceValue] holding a [Double].
 */
@Serializable
@SerialName("double")
public data class DoublePreferenceValue(
    @SerialName("value")
    val value: Double,
) : PreferenceValue {
    override fun getValue(): Any = value
}

/**
 * A [PreferenceValue] holding a [String].
 */
@Serializable
@SerialName("string")
public data class StringPreferenceValue(
    @SerialName("value")
    val value: String,
) : PreferenceValue {
    override fun getValue(): Any = value
}

/**
 * A [PreferenceValue] holding a [Boolean].
 */
@Serializable
@SerialName("boolean")
public data class BooleanPreferenceValue(
    @SerialName("value")
    val value: Boolean,
) : PreferenceValue {
    override fun getValue(): Any = value
}

/**
 * A [PreferenceValue] holding a [Set] of [String]s.
 */
@Serializable
@SerialName("stringSet")
public data class StringSetPreferenceValue(
    @SerialName("value")
    val value: Set<String>,
) : PreferenceValue {
    override fun getValue(): Any = value
}

/**
 * Serializable snapshot used by `PreferencesDatastore.exportAsData` and restore APIs.
 */
@Serializable
public data class PreferencesBackup(
    @SerialName("preferences")
    val preferences: List<BackupPreference>,
)
