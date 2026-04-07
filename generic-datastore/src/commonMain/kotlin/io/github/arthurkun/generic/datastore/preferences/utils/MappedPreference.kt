@file:Suppress("unused")

package io.github.arthurkun.generic.datastore.preferences.utils

import io.github.arthurkun.generic.datastore.preferences.MappedPreferenceImpl
import io.github.arthurkun.generic.datastore.preferences.Preference

/**
 * Maps a [io.github.arthurkun.generic.datastore.core.DelegatedPreference] of type [T] to a [io.github.arthurkun.generic.datastore.core.DelegatedPreference] of type [R], inferring the default value by converting
 * the default value of the original preference.
 *
 * This is a convenience function for cases where the conversion of the default value is guaranteed
 * to be safe.
 *
 * **Warning:** This function will throw an exception during initialization if the `convert`
 * function fails on the original preference's default value. For a safer version where you can
 * provide an explicit default value of type [R], see [map].
 *
 * @param T The original type of the preference value in storage.
 * @param R The target type for the preference value in the application.
 * @param convert A lambda function `(T) -> R` for converting the stored value from type [T] to [R].
 * @param reverse A lambda function `(R) -> T` for converting an application value from type [R]
 *   back to type [T] for storage.
 * @return A new [io.github.arthurkun.generic.datastore.core.DelegatedPreference] instance of type [R].
 * @throws Exception if `convert(this.defaultValue)` fails.
 */
public fun <T, R> Preference<T>.mapIO(
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> =
    MappedPreferenceImpl(
        prefs = this,
        defaultValue = convert(this.defaultValue),
        convert = convert,
        reverse = reverse,
    )

/**
 * Maps a [io.github.arthurkun.generic.datastore.core.DelegatedPreference] of type [T] to a [io.github.arthurkun.generic.datastore.core.DelegatedPreference] of type [R] using provided converter functions.
 *
 * This function allows transforming a stored preference value to a different type for
 * application use, while the underlying storage remains in its original type [T].
 * It incorporates error handling for the conversion processes:
 * - When converting from [T] to [R] (e.g., on reads): if [convert] fails, [defaultValue] (of type [R]) is used.
 * - When converting from [R] to [T] (e.g., on writes): if [reverse] fails, the `defaultValue`
 *   of the original [io.github.arthurkun.generic.datastore.core.DelegatedPreference] instance (of type [T]) is used.
 *
 * @param T The original type of the preference value in storage.
 * @param R The target type for the preference value in the application.
 * @param defaultValue The default value of type [R] for the mapped preference. This is also
 *   used as a fallback if the [convert] (T -> R) operation fails.
 * @param convert A lambda function `(T) -> R` for converting the stored value from type [T] to [R].
 *   If this function throws an exception, [defaultValue] (type [R]) is returned.
 * @param reverse A lambda function `(R) -> T` for converting an application value from type [R]
 *   back to type [T] for storage. If this function throws an exception, the `defaultValue`
 *   of the original `Prefs<T>` instance is stored.
 * @return A new [io.github.arthurkun.generic.datastore.core.DelegatedPreference] instance of type [R] that applies the specified conversions
 *   and error handling logic.
 */
public fun <T, R> Preference<T>.map(
    defaultValue: R,
    convert: (T) -> R,
    reverse: (R) -> T,
): Preference<R> =
    MappedPreferenceImpl(
        this,
        defaultValue,
        convert,
        reverse,
    )
