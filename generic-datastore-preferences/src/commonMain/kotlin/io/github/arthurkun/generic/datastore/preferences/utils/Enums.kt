package io.github.arthurkun.generic.datastore.preferences.utils

/**
 * Decodes a stored [Enum.name] back into a constant of [T].
 *
 * [enumValues] is the full set of constants for the enum type. Kotlin's `enumValueOf` needs a
 * reified type parameter, which neither the datastore interface members nor the batch
 * declarations have, so callers pass the constants explicitly and this helper performs the lookup.
 *
 * Throws [IllegalArgumentException] when no constant matches [name], mirroring `enumValueOf`.
 * Preference items catch the throw and apply their own fallback: the default value for non-nullable
 * preferences, `null` for nullable ones, and element skipping for sets.
 *
 * @param T The enum type.
 * @param enumValues All constants of the enum type.
 * @param name The stored [Enum.name].
 * @return The matching constant.
 * @throws IllegalArgumentException If no constant of [T] is named [name].
 */
internal fun <T : Enum<T>> decodeEnum(
    enumValues: Array<T>,
    name: String,
): T = enumValues.firstOrNull { it.name == name }
    ?: throw IllegalArgumentException("No enum constant named $name")
