package com.example.oneminutelanguage.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val amberGlow = if (isDark) GlowAmberDark else GlowAmberLight
    val primaryGlow = MaterialTheme.colorScheme.primary
    val tertiaryGlow = MaterialTheme.colorScheme.tertiary
    val baseAlpha = if (isDark) 0.35f else 0.5f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawWithCache {
                val w = size.width
                val h = size.height

                fun glow(color: Color, center: Offset, radius: Float) = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = baseAlpha),
                        color.copy(alpha = baseAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                )

                val glow1 = glow(primaryGlow, Offset(w * 0.12f, h * 0.05f), w * 0.85f)
                val glow2 = glow(tertiaryGlow, Offset(w * 1.0f, h * 0.15f), w * 0.9f)
                val glow3 = glow(amberGlow, Offset(w * 0.15f, h * 1.0f), w * 0.9f)

                onDrawBehind {
                    drawRect(brush = glow1)
                    drawRect(brush = glow2)
                    drawRect(brush = glow3)
                }
            }
    ) {
        content()
    }
}
