package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore

/**
 * [CompositionLocal] used to provide a [PreferencesDatastore] to composables
 * in the composition tree.
 *
 * Use [ProvidePreferencesDatastore] to supply a value, then call the standalone
 * [rememberPreferences] overloads without an explicit datastore receiver.
 *
 * Accessing this local without a provider throws an [IllegalStateException].
 */
public val LocalPreferencesDatastore: ProvidableCompositionLocal<PreferencesDatastore> =
    compositionLocalOf {
        error(
            "No PreferencesDatastore provided. " +
                "Wrap your composable tree with ProvidePreferencesDatastore { ... }.",
        )
    }

/**
 * Provides a [PreferencesDatastore] to the composition tree via [LocalPreferencesDatastore].
 *
 * Usage:
 * ```
 * ProvidePreferencesDatastore(myDatastore) {
 *     val (name, age) = rememberPreferences(namePref, agePref)
 * }
 * ```
 *
 * @param datastore The [PreferencesDatastore] to provide.
 * @param content The composable content that can access the datastore via [LocalPreferencesDatastore].
 */
@Composable
public fun ProvidePreferencesDatastore(
    datastore: PreferencesDatastore,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalPreferencesDatastore provides datastore, content = content)
}
