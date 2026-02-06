package io.github.arthurkun.generic.datastore.compose.app.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

/**
 * Platform-specific color scheme provider.
 * On Android, this may provide dynamic colors on Android 12+.
 * On Desktop, this falls back to static color schemes.
 *
 * @param useDarkTheme Whether to use dark theme. If null, follows system.
 * @param useDynamicColor Whether to use dynamic colors (Android 12+ only).
 */
@Composable
expect fun platformColorScheme(
    useDarkTheme: Boolean,
    useDynamicColor: Boolean,
): ColorScheme

@Composable
fun GenericDataStoreAppTheme(
    useDarkTheme: Boolean = false,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = platformColorScheme(
        useDarkTheme = useDarkTheme,
        useDynamicColor = useDynamicColor,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
