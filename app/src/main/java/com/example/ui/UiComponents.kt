package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    val shimmerColors = listOf(
        SlateCard,
        SlateCard.copy(alpha = 1.3f).mix(IndigoAccent, 0.12f),
        SlateCard
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
    )
}

// Extension to mix colors
fun Color.mix(other: Color, fraction: Float): Color {
    val r = this.red + (other.red - this.red) * fraction
    val g = this.green + (other.green - this.green) * fraction
    val b = this.blue + (other.blue - this.blue) * fraction
    val a = this.alpha + (other.alpha - this.alpha) * fraction
    return Color(r, g, b, a)
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color = IndigoAccent,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCard)
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title.uppercase(),
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
        Column {
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subtitle,
                    color = if (color == AccentGreen || color == AccentYellow || color == AccentRed) color else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

// Beautiful customized Line Chart mapping forecasting accuracy weekly
@Composable
fun InteractiveLineChart(
    weeklyData: List<Float>, // Percentage coordinates
    weeks: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 50f
        val paddingRight = 30f
        val paddingTop = 30f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // 1. Draw Grid lines and text markers (50%, 75%, 100%)
        val gridLines = listOf(0.5f, 0.75f, 1.0f)
        gridLines.forEach { percentage ->
            val y = paddingTop + chartHeight * (1f - percentage)
            drawLine(
                color = SlateBorder.copy(alpha = 0.5f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f
            )
        }

        // 2. Plot Points and Draw Path
        val steps = weeklyData.size - 1
        val path = Path()
        val points = mutableListOf<Offset>()

        for (i in weeklyData.indices) {
            val x = paddingLeft + (chartWidth / steps) * i
            val y = paddingTop + chartHeight * (1f - weeklyData[i])
            points.add(Offset(x, y))
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw shadow under line
        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, paddingTop + chartHeight)
            lineTo(points.first().x, paddingTop + chartHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(IndigoAccent.copy(alpha = 0.25f), Color.Transparent),
                startY = points.map { it.y }.minOrNull() ?: 0f,
                endY = paddingTop + chartHeight
            )
        )

        drawPath(
            path = path,
            color = IndigoAccent,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw data circles and labels
        points.forEachIndexed { index, offset ->
            drawCircle(
                color = SlateBg,
                radius = 10f,
                center = offset
            )
            drawCircle(
                color = IndigoAccent,
                radius = 6f,
                center = offset
            )
        }
    }
}

// Gorgeous vertical bar chart mapping waste reduction percentages
@Composable
fun InteractiveBarChart(
    monthlyData: List<Float>,
    months: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 50f
        val paddingRight = 30f
        val paddingTop = 20f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw horizontal midlines
        val midLines = listOf(0.2f, 0.4f, 0.6f, 0.8f)
        midLines.forEach { m ->
            val y = paddingTop + chartHeight * (1f - m)
            drawLine(
                color = SlateBorder.copy(alpha = 0.4f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f
            )
        }

        val bars = monthlyData.size
        val barTotalWidth = chartWidth / bars
        val barSpacing = barTotalWidth * 0.25f
        val activeBarWidth = barTotalWidth - barSpacing

        for (i in monthlyData.indices) {
            val barHeightVal = chartHeight * monthlyData[i]
            val x = paddingLeft + (barTotalWidth * i) + (barSpacing / 2)
            val y = paddingTop + chartHeight - barHeightVal

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(AccentGreen, AccentGreen.copy(alpha = 0.3f))
                ),
                topLeft = Offset(x, y),
                size = Size(activeBarWidth, barHeightVal),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }
    }
}
