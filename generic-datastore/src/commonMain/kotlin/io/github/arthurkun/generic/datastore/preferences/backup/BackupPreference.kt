package io.github.arthurkun.generic.datastore.preferences.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class BackupPreference(
    @SerialName("key")
    val key: String,
    @SerialName("value")
    val value: PreferenceValue,
)

@Serializable
public sealed interface PreferenceValue {
    public fun getValue(): Any

    public companion object {
        public fun fromAny(value: Any?): PreferenceValue? {
            return when (value) {
                is Int -> IntPreferenceValue(value)

                is Long -> LongPreferenceValue(value)

                is Float -> FloatPreferenceValue(value)

                is Double -> DoublePreferenceValue(value)

                is String -> StringPreferenceValue(value)

                is Boolean -> BooleanPreferenceValue(value)

                is Set<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    (value as? Set<String>)?.let { StringSetPreferenceValue(it) }
                }

                else -> null
            }
        }
    }
}

@Serializable
@SerialName("int")
public data class IntPreferenceValue(
    @SerialName("value")
    val value: Int,
) : PreferenceValue {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("long")
public data class LongPreferenceValue(
    @SerialName("value")
    val value: Long,
) : PreferenceValue {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("float")
public data class FloatPreferenceValue(
    @SerialName("value")
    val value: Float,
) : PreferenceValue {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("double")
public data class DoublePreferenceValue(
    @SerialName("value")
    val value: Double,
) : PreferenceValue {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("string")
public data class StringPreferenceValue(
    @SerialName("value")
    val value: String,
) : PreferenceValue {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("boolean")
public data class BooleanPreferenceValue(
    @SerialName("value")
    val value: Boolean,
) : PreferenceValue {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("stringSet")
public data class StringSetPreferenceValue(
    @SerialName("value")
    val value: Set<String>,
) : PreferenceValue {
    override fun getValue(): Any = value
}

@Serializable
public data class PreferencesBackup(
    @SerialName("preferences")
    val preferences: List<BackupPreference>,
)
