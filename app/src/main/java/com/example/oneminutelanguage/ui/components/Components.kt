package com.example.oneminutelanguage.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.oneminutelanguage.course.ItemResult
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import com.example.oneminutelanguage.ui.theme.Accent
import com.example.oneminutelanguage.ui.theme.appColors

/** Frosted cards (One UI): milky white on the pastel page, a faint white veil in dark mode. */
@Composable
fun appCardColors() = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.glass)

/** Small grey heading above a group of cards. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(top = 12.dp, start = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** One UI app-icon look: white glyph on a glossy squircle with the accent's gradient and a soft top highlight. */
@Composable
fun IconTile(icon: ImageVector, accent: Accent, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val shape = RoundedCornerShape(size * 0.34f)
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(accent.tile), shape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
fun StatTile(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Accent = MaterialTheme.appColors.blue,
    onClick: (() -> Unit)? = null
) {
    val content: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IconTile(icon, accent, size = 36.dp, modifier = Modifier.padding(bottom = 8.dp))
            Text(text = value, style = MaterialTheme.typography.displaySmall)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (onClick == null) Card(modifier = modifier, shape = MaterialTheme.shapes.medium, colors = appCardColors(), content = content)
    else Card(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.medium, colors = appCardColors(), content = content)
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    strokeWidth: Dp = 10.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "ring")
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize, style = stroke)
            if (animated > 0f) {
                drawArc(color = color, startAngle = -90f, sweepAngle = 360f * animated, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize, style = stroke)
            }
        }
        content()
    }
}

@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    accent: Accent = MaterialTheme.appColors.blue
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = appCardColors()
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 72.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(icon, accent, size = 48.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (badge != null) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/** Small pill: `de` or `het`. Anything else renders nothing. */
@Composable
fun ArticleTag(article: String, modifier: Modifier = Modifier) {
    val color = when (article.lowercase()) {
        "de" -> MaterialTheme.appColors.articleDe
        "het" -> MaterialTheme.appColors.articleHet
        else -> return
    }
    Surface(modifier = modifier, shape = CircleShape, color = color.copy(alpha = 0.16f)) {
        Text(
            text = article.lowercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

enum class AnswerState { IDLE, SELECTED, CORRECT, WRONG, DIMMED }

/** One answer button for quiz and lessons. Correct / wrong also show an icon, never colour alone. */
@Composable
fun AnswerOption(
    text: String,
    state: AnswerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = state == AnswerState.IDLE || state == AnswerState.SELECTED
) {
    val scheme = MaterialTheme.colorScheme
    val container = when (state) {
        AnswerState.IDLE, AnswerState.DIMMED -> MaterialTheme.appColors.glass
        AnswerState.SELECTED -> scheme.primaryContainer
        AnswerState.CORRECT -> MaterialTheme.appColors.successContainer
        AnswerState.WRONG -> scheme.errorContainer
    }
    val border = when (state) {
        AnswerState.SELECTED -> scheme.primary
        AnswerState.CORRECT -> MaterialTheme.appColors.success
        AnswerState.WRONG -> scheme.error
        else -> scheme.outlineVariant
    }
    val content = when (state) {
        AnswerState.SELECTED -> scheme.onPrimaryContainer
        AnswerState.WRONG -> scheme.onErrorContainer
        AnswerState.DIMMED -> scheme.onSurfaceVariant
        else -> scheme.onSurface
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp),
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
        border = BorderStroke(if (state == AnswerState.IDLE || state == AnswerState.DIMMED) 1.dp else 2.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            when (state) {
                AnswerState.CORRECT -> Icon(
                    Icons.Default.Check, contentDescription = "Correct", tint = MaterialTheme.appColors.success
                )
                AnswerState.WRONG -> Icon(
                    Icons.Default.Close, contentDescription = "Wrong", tint = scheme.error
                )
                else -> Unit
            }
        }
    }
}

/** "Continue learning": deep teal gradient with two soft circles, a play button and an optional progress bar. */
@Composable
fun HeroCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    val colors = MaterialTheme.appColors
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.heroEnd, contentColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(colors.heroStart, colors.heroEnd)))
                .drawBehind {
                    drawCircle(Color.White.copy(alpha = 0.08f), radius = size.height * 0.9f, center = Offset(size.width, 0f))
                    drawCircle(Color.White.copy(alpha = 0.06f), radius = size.height * 0.45f, center = Offset(size.width * 0.72f, size.height))
                }
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                        Text(subtitle, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier.size(48.dp).background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = colors.heroEnd)
                    }
                }
                if (progress != null) {
                    val p = progress.coerceIn(0f, 1f)
                    Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { p },
                            modifier = Modifier.weight(1f).height(6.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {}
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("${(p * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/** Shown under an exercise after "Check". Correct / wrong is spelled out and has an icon, never colour alone. */
@Composable
fun FeedbackPanel(result: ItemResult, explain: String?, onContinue: () -> Unit, modifier: Modifier = Modifier, onReport: (() -> Unit)? = null) {
    val scheme = MaterialTheme.colorScheme
    val good = result.correct
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
        color = if (good) MaterialTheme.appColors.successContainer else scheme.errorContainer,
        contentColor = if (good) scheme.onSurface else scheme.onErrorContainer
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = if (good) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (good) MaterialTheme.appColors.success else scheme.error
                )
                Text(if (good) "Correct!" else "Not quite", style = MaterialTheme.typography.titleMedium)
            }
            result.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            if (!good) {
                Text("Correct answer", style = MaterialTheme.typography.labelMedium)
                Text(result.expected, style = MaterialTheme.typography.titleMedium)
            }
            result.diff?.let { diff ->
                Text(
                    text = buildAnnotatedString {
                        diff.forEachIndexed { i, (word, matched) ->
                            if (i > 0) append(' ')
                            if (matched) append(word) else withStyle(
                                SpanStyle(color = scheme.error, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                            ) { append(word) }
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            explain?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) { Text("Continue") }
            onReport?.let { TextButton(onClick = it, modifier = Modifier.fillMaxWidth()) { Text("Report a problem with this item") } }
        }
    }
}
