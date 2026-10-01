package com.example.oneminutelanguage.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Brush
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
import com.example.oneminutelanguage.ui.theme.appColors

/** Cards sit on the mesh background; a little transparency lets the glow show through. */
@Composable
fun appCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f)
)

@Composable
fun StatTile(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.medium, colors = appCardColors()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = value, style = MaterialTheme.typography.displaySmall)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
    badge: String? = null
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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
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
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        AnswerState.IDLE, AnswerState.DIMMED -> scheme.surfaceContainerLow
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

/** "Continue learning": teal-to-coral gradient card with an optional progress bar. */
@Composable
fun HeroCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    val scheme = MaterialTheme.colorScheme
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = scheme.onPrimary.copy(alpha = 0.85f))
            Text(subtitle, style = MaterialTheme.typography.titleLarge, color = scheme.onPrimary)
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = scheme.onPrimary,
                    trackColor = scheme.onPrimary.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/** Shown under an exercise after "Check". Correct / wrong is spelled out and has an icon, never colour alone. */
@Composable
fun FeedbackPanel(result: ItemResult, explain: String?, onContinue: () -> Unit, modifier: Modifier = Modifier) {
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
        }
    }
}
