package com.example.oneminutelanguage.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * One UI-style page: a soft vertical pastel gradient with two large, faint swooshes (circles bigger than the screen).
 * Hues stay in one family (lavender, blush, periwinkle) so it never turns muddy, in light or dark.
 */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val page = MaterialTheme.appColors.page
    val dark = page.first().luminance() < 0.5f
    val swoosh = Color.White.copy(alpha = if (dark) 0.035f else 0.35f)
    val swoosh2 = (if (dark) Color(0xFF6B4FD8) else Color(0xFFC9B8F5)).copy(alpha = if (dark) 0.10f else 0.25f)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(page))
            .drawBehind {
                drawCircle(swoosh, radius = size.width * 0.95f, center = Offset(size.width * 1.05f, size.height * 0.08f))
                drawCircle(swoosh2, radius = size.width * 0.85f, center = Offset(-size.width * 0.25f, size.height * 0.78f))
            },
        content = content
    )
}
