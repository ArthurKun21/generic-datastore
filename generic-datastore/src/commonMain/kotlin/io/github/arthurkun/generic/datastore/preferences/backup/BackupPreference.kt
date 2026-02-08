package io.github.arthurkun.generic.datastore.preferences.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single preference entry in a backup, consisting of a key and its typed value.
 */
@Serializable
public data class BackupPreference(
    @SerialName("key")
    val key: String,
    @SerialName("value")
    val value: PreferenceValue,
)

/**
 * Sealed interface representing a typed preference value supported by DataStore Preferences.
 */
@Serializable
public sealed interface PreferenceValue {
    public fun getValue(): Any

    public companion object {
        /**
         * Converts an [Any] value to the corresponding [PreferenceValue] subtype,
         * or returns `null` if the value type is not supported.
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
 * Top-level backup container holding a list of [BackupPreference] entries.
 */
@Serializable
public data class PreferencesBackup(
    @SerialName("preferences")
    val preferences: List<BackupPreference>,
)
