package com.example.oneminutelanguage.ui.theme

import androidx.compose.ui.graphics.Color

// v2.0 palette, One UI style: blue-violet accent, lavender neutrals, a soft pastel page gradient
// (see AppBackground) with frosted cards on top. Opaque surfaceContainer* roles are for dialogs, fields and sheets.

val PrimaryLight = Color(0xFF3E5BF0)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDEE2FF)
val OnPrimaryContainerLight = Color(0xFF0B1A66)

val SecondaryLight = Color(0xFF5C5E78)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE4E2F7)
val OnSecondaryContainerLight = Color(0xFF191A2F)

val TertiaryLight = Color(0xFFD9415E)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFD9DF)
val OnTertiaryContainerLight = Color(0xFF400012)

val BackgroundLight = Color(0xFFEEEDFB)
val OnBackgroundLight = Color(0xFF1B1B24)

val SurfaceVariantLight = Color(0xFFE3E1F2)
val OnSurfaceVariantLight = Color(0xFF55566B)
val OutlineLight = Color(0xFF7D7E93)
val OutlineVariantLight = Color(0xFFCFCEE0)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF8F7FF)
val SurfaceContainerLight = Color(0xFFF1F0FB)
val SurfaceContainerHighLight = Color(0xFFEBEAF6)
val SurfaceContainerHighestLight = Color(0xFFE5E4F1)

val PrimaryDark = Color(0xFFA9B6FF)
val OnPrimaryDark = Color(0xFF0F1E78)
val PrimaryContainerDark = Color(0xFF2C3DA8)
val OnPrimaryContainerDark = Color(0xFFDEE2FF)

val SecondaryDark = Color(0xFFC5C5DD)
val OnSecondaryDark = Color(0xFF2E2F42)
val SecondaryContainerDark = Color(0xFF3A3B52)
val OnSecondaryContainerDark = Color(0xFFE2E1F9)

val TertiaryDark = Color(0xFFFFB1C1)
val OnTertiaryDark = Color(0xFF5E1129)
val TertiaryContainerDark = Color(0xFF5A2335)
val OnTertiaryContainerDark = Color(0xFFFFD9DF)

val BackgroundDark = Color(0xFF0F0F1A)
val OnBackgroundDark = Color(0xFFE5E4F2)

val SurfaceVariantDark = Color(0xFF2C2C3D)
val OnSurfaceVariantDark = Color(0xFFC6C5DA)
val OutlineDark = Color(0xFF8F8FA6)
val OutlineVariantDark = Color(0xFF3A3A4D)
val SurfaceContainerLowestDark = Color(0xFF0A0A14)
val SurfaceContainerLowDark = Color(0xFF17172A)
val SurfaceContainerDark = Color(0xFF1C1C30)
val SurfaceContainerHighDark = Color(0xFF252538)
val SurfaceContainerHighestDark = Color(0xFF2F2F44)

// Page gradient (top, middle, bottom) and the frosted card fill.
val PageLight = listOf(Color(0xFFE6E0FB), Color(0xFFF6E4F3), Color(0xFFDFE6FB))
val PageDark = listOf(Color(0xFF1A1636), Color(0xFF1E1430), Color(0xFF0F152E))
val GlassLight = Color.White.copy(alpha = 0.8f)
val GlassDark = Color.White.copy(alpha = 0.08f)

// Semantic colours (see AppColors in Theme.kt). Containers are light/dark enough for onSurface text.
val SuccessLight = Color(0xFF2E7D32)
val SuccessContainerLight = Color(0xFFDDF0DE)
val WarningLight = Color(0xFF9A6700)
val WarningContainerLight = Color(0xFFFFF0C2)
val ArticleDeLight = Color(0xFF00897B)
val ArticleHetLight = Color(0xFFE65100)

val SuccessDark = Color(0xFF7FD085)
val SuccessContainerDark = Color(0xFF1C3A22)
val WarningDark = Color(0xFFE8C468)
val WarningContainerDark = Color(0xFF3F3414)
val ArticleDeDark = Color(0xFF80CBC4)
val ArticleHetDark = Color(0xFFFFB878)

// Hero card: blue to violet (like the One UI 9 mark), always with white text.
val HeroStartLight = Color(0xFF4C8DFF)
val HeroEndLight = Color(0xFF7B5CFA)
val HeroStartDark = Color(0xFF3557D6)
val HeroEndDark = Color(0xFF5A3FC4)

// Accents. Light / dark: (text and ring colour, pastel pill). Tile: the glossy icon gradient, same in both themes.
val AccentTealLight = Color(0xFF00796B) to Color(0xFFD3F0EB)
val AccentCoralLight = Color(0xFFD9541E) to Color(0xFFFFE6DB)
val AccentAmberLight = Color(0xFFA86400) to Color(0xFFFFEFC7)
val AccentVioletLight = Color(0xFF6750A4) to Color(0xFFECE4FF)
val AccentBlueLight = Color(0xFF3E5BF0) to Color(0xFFDEE2FF)
val AccentRoseLight = Color(0xFFC2185B) to Color(0xFFFFDCE8)

val AccentTealDark = Color(0xFF7FE0CF) to Color(0xFF15403A)
val AccentCoralDark = Color(0xFFFFB59A) to Color(0xFF4A2A1E)
val AccentAmberDark = Color(0xFFFFD27A) to Color(0xFF3F3414)
val AccentVioletDark = Color(0xFFCDBDFF) to Color(0xFF342A55)
val AccentBlueDark = Color(0xFFA9B6FF) to Color(0xFF26306B)
val AccentRoseDark = Color(0xFFFFB0CB) to Color(0xFF4F1F33)

val TileTeal = Color(0xFF2BD4C0) to Color(0xFF0A9C8E)
val TileCoral = Color(0xFFFF9A6B) to Color(0xFFF0533A)
val TileAmber = Color(0xFFFFCF5A) to Color(0xFFF59E0B)
val TileViolet = Color(0xFFB39BFF) to Color(0xFF6D4AFF)
val TileBlue = Color(0xFF5FB2FF) to Color(0xFF3E5BF0)
val TileRose = Color(0xFFFF86B8) to Color(0xFFE83E8C)
