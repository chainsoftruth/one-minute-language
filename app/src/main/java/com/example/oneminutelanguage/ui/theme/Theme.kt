package com.example.oneminutelanguage.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** A feature colour: [color] for text and rings, [container] for pills, [tile] for the glossy icon gradient. */
data class Accent(val color: Color, val container: Color, val tile: List<Color>)

private fun accent(pair: Pair<Color, Color>, tile: Pair<Color, Color>) = Accent(pair.first, pair.second, tile.toList())

/** Colours the Material roles don't cover: answers, de / het tags, page and glass, the hero card and feature accents. */
data class AppColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val articleDe: Color,
    val articleHet: Color,
    val page: List<Color>,
    val glass: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val teal: Accent,
    val coral: Accent,
    val amber: Accent,
    val violet: Accent,
    val blue: Accent,
    val rose: Accent
)

private val LightAppColors = AppColors(
    success = SuccessLight,
    successContainer = SuccessContainerLight,
    warning = WarningLight,
    warningContainer = WarningContainerLight,
    articleDe = ArticleDeLight,
    articleHet = ArticleHetLight,
    page = PageLight,
    glass = GlassLight,
    heroStart = HeroStartLight,
    heroEnd = HeroEndLight,
    teal = accent(AccentTealLight, TileTeal),
    coral = accent(AccentCoralLight, TileCoral),
    amber = accent(AccentAmberLight, TileAmber),
    violet = accent(AccentVioletLight, TileViolet),
    blue = accent(AccentBlueLight, TileBlue),
    rose = accent(AccentRoseLight, TileRose)
)

private val DarkAppColors = AppColors(
    success = SuccessDark,
    successContainer = SuccessContainerDark,
    warning = WarningDark,
    warningContainer = WarningContainerDark,
    articleDe = ArticleDeDark,
    articleHet = ArticleHetDark,
    page = PageDark,
    glass = GlassDark,
    heroStart = HeroStartDark,
    heroEnd = HeroEndDark,
    teal = accent(AccentTealDark, TileTeal),
    coral = accent(AccentCoralDark, TileCoral),
    amber = accent(AccentAmberDark, TileAmber),
    violet = accent(AccentVioletDark, TileViolet),
    blue = accent(AccentBlueDark, TileBlue),
    rose = accent(AccentRoseDark, TileRose)
)

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

// One UI corners: soft and large.
private val AppShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(26.dp),
    large = RoundedCornerShape(32.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = BackgroundDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = BackgroundLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight
)

@Composable
fun OneMinuteLanguageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
