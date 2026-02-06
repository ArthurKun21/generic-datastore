package io.github.arthurkun.generic.datastore.compose.app.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun platformColorScheme(
    useDarkTheme: Boolean,
    useDynamicColor: Boolean,
): ColorScheme {
    // Desktop doesn't support dynamic colors
    return if (useDarkTheme) DarkColorScheme else LightColorScheme
}
