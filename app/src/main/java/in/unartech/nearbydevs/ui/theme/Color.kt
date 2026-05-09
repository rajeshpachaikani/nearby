package `in`.unartech.nearbydevs.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Material 3 Expressive — NearByDevices palette.
// sRGB approximations of design tokens specified as oklch().

val LightPrimary = Color(0xFF6E4DDC)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE7E0FF)
val LightOnPrimaryContainer = Color(0xFF311B92)

val LightSecondary = Color(0xFF1FA8C2)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFD7F2F8)
val LightOnSecondaryContainer = Color(0xFF003640)

val LightTertiary = Color(0xFFE5A471)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFCE5CF)
val LightOnTertiaryContainer = Color(0xFF5B3105)

val LightError = Color(0xFFD63A36)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF7DAD7)

val LightBackground = Color(0xFFEDEAF3)
val LightSurface = Color(0xFFFCFAFF)
val LightSurfaceDim = Color(0xFFE7E2EF)
val LightSurfaceBright = Color(0xFFFFFEFF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF7F4FB)
val LightSurfaceContainer = Color(0xFFEFEAF6)
val LightSurfaceContainerHigh = Color(0xFFE8E3F1)
val LightSurfaceContainerHighest = Color(0xFFE1DCEB)

val LightOnSurface = Color(0xFF1B1726)
val LightOnSurfaceVariant = Color(0xFF565167)
val LightOutline = Color(0xFF8B86A1)
val LightOutlineVariant = Color(0xFFCAC4DA)

val DarkPrimary = Color(0xFFBAA8F2)
val DarkOnPrimary = Color(0xFF22125D)
val DarkPrimaryContainer = Color(0xFF4D38A4)
val DarkOnPrimaryContainer = Color(0xFFE7E0FF)

val DarkSecondary = Color(0xFF7BD1E0)
val DarkOnSecondary = Color(0xFF003640)
val DarkSecondaryContainer = Color(0xFF1F5560)
val DarkOnSecondaryContainer = Color(0xFFD7F2F8)

val DarkTertiary = Color(0xFFEBC09A)
val DarkOnTertiary = Color(0xFF402208)
val DarkTertiaryContainer = Color(0xFF6E4318)
val DarkOnTertiaryContainer = Color(0xFFFCE5CF)

val DarkError = Color(0xFFE99490)
val DarkOnError = Color(0xFF591716)
val DarkErrorContainer = Color(0xFF6F2622)

val DarkBackground = Color(0xFF131019)
val DarkSurface = Color(0xFF1A1622)
val DarkSurfaceDim = Color(0xFF15121C)
val DarkSurfaceBright = Color(0xFF2B2738)
val DarkSurfaceContainerLowest = Color(0xFF0F0C16)
val DarkSurfaceContainerLow = Color(0xFF1D1925)
val DarkSurfaceContainer = Color(0xFF231F2D)
val DarkSurfaceContainerHigh = Color(0xFF2B2738)
val DarkSurfaceContainerHighest = Color(0xFF332E40)

val DarkOnSurface = Color(0xFFE5E0F0)
val DarkOnSurfaceVariant = Color(0xFFB6B0C7)
val DarkOutline = Color(0xFF7C7691)
val DarkOutlineVariant = Color(0xFF3F3A4D)

val Success = Color(0xFF38B26B)
val SuccessDark = Color(0xFF6CD79B)

val NbdLightColorScheme: ColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurfaceBright,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

val NbdDarkColorScheme: ColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)
