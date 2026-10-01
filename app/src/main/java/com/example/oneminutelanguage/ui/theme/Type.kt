package com.example.oneminutelanguage.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun style(weight: FontWeight, size: TextUnit, lineHeight: TextUnit, letterSpacing: TextUnit = 0.sp) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing
)

// Material 3 scale; displaySmall (big numbers, lesson prompt), headlineSmall (screen titles),
// titleMedium (card titles) and labelLarge (buttons) are the ones the app tunes.
val Typography = Typography(
    displayLarge = style(FontWeight.Normal, 57.sp, 64.sp, (-0.25).sp),
    displayMedium = style(FontWeight.Normal, 45.sp, 52.sp),
    displaySmall = style(FontWeight.SemiBold, 36.sp, 44.sp),
    headlineLarge = style(FontWeight.SemiBold, 32.sp, 40.sp),
    headlineMedium = style(FontWeight.SemiBold, 28.sp, 36.sp),
    headlineSmall = style(FontWeight.SemiBold, 24.sp, 32.sp),
    titleLarge = style(FontWeight.SemiBold, 22.sp, 28.sp),
    titleMedium = style(FontWeight.SemiBold, 16.sp, 24.sp, 0.15.sp),
    titleSmall = style(FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
    bodyLarge = style(FontWeight.Normal, 16.sp, 24.sp, 0.5.sp),
    bodyMedium = style(FontWeight.Normal, 14.sp, 20.sp, 0.25.sp),
    bodySmall = style(FontWeight.Normal, 12.sp, 16.sp, 0.4.sp),
    labelLarge = style(FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
    labelMedium = style(FontWeight.Medium, 12.sp, 16.sp, 0.5.sp),
    labelSmall = style(FontWeight.Medium, 11.sp, 16.sp, 0.5.sp)
)
