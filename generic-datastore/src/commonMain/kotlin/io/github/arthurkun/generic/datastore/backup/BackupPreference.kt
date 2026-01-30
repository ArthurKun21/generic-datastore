package io.github.arthurkun.generic.datastore.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class BackupPreference(
    @SerialName("key")
    val key: String,
    @SerialName("value")
    val value: PreferenceValue,
)

@Serializable
sealed class PreferenceValue {

    abstract fun getValue(): Any

    companion object {
        fun fromAny(value: Any?): PreferenceValue? {
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
data class IntPreferenceValue(
    @SerialName("value")
    val value: Int,
) : PreferenceValue() {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("long")
data class LongPreferenceValue(
    @SerialName("value")
    val value: Long,
) : PreferenceValue() {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("float")
data class FloatPreferenceValue(
    @SerialName("value")
    val value: Float,
) : PreferenceValue() {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("double")
data class DoublePreferenceValue(
    @SerialName("value")
    val value: Double,
) : PreferenceValue() {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("string")
data class StringPreferenceValue(
    @SerialName("value")
    val value: String,
) : PreferenceValue() {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("boolean")
data class BooleanPreferenceValue(
    @SerialName("value")
    val value: Boolean,
) : PreferenceValue() {
    override fun getValue(): Any = value
}

@Serializable
@SerialName("stringSet")
data class StringSetPreferenceValue(
    @SerialName("value")
    val value: Set<String>,
) : PreferenceValue() {
    override fun getValue(): Any = value
}

@Serializable
data class PreferencesBackup(
    @SerialName("version")
    val version: Int = BACKUP_VERSION,
    @SerialName("preferences")
    val preferences: List<BackupPreference>,
) {
    companion object {
        const val BACKUP_VERSION = 1

        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun fromJson(jsonString: String): PreferencesBackup {
            return json.decodeFromString<PreferencesBackup>(jsonString)
        }

        fun fromJsonElement(element: JsonElement): PreferencesBackup {
            return json.decodeFromJsonElement<PreferencesBackup>(element)
        }
    }

    fun toJson(): String {
        return Companion.json.encodeToString(this)
    }

    fun toJsonElement(): JsonElement {
        return Companion.json.encodeToJsonElement(this)
    }
}
