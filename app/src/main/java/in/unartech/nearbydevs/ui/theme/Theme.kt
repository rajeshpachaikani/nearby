package `in`.unartech.nearbydevs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import `in`.unartech.nearbydevs.data.model.ThemeMode

val LocalProtocolColors = staticCompositionLocalOf { ProtocolColorsLight }

@Composable
fun NearbydevsTheme(
    mode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colorScheme = if (darkTheme) NbdDarkColorScheme else NbdLightColorScheme
    val protocolColors = if (darkTheme) ProtocolColorsDark else ProtocolColorsLight

    CompositionLocalProvider(LocalProtocolColors provides protocolColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NbdTypography,
            shapes = NbdShapes,
            content = content,
        )
    }
}
