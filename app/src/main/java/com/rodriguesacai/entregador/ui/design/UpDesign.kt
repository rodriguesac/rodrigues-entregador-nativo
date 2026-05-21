package com.rodriguesacai.entregador.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
object UpColors {
    val Screen = Color(0xFFF7F8FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFF9FAFB)
    val Ink = Color(0xFF111827)
    val Text = Color(0xFF1F2937)
    val Muted = Color(0xFF6B7280)
    val Subtle = Color(0xFF9CA3AF)
    val Line = Color(0xFFE5E7EB)
    val Green = Color(0xFF168A2F)
    val GreenDark = Color(0xFF047021)
    val GreenSoft = Color(0xFFEAF7EE)
    val Red = Color(0xFFE11D2E)
    val RedSoft = Color(0xFFFFE9EA)
    val Orange = Color(0xFFFF8A00)
    val OrangeSoft = Color(0xFFFFF4E5)
    val Blue = Color(0xFF2563EB)
    val BlueSoft = Color(0xFFEFF6FF)
    val Shadow = Color(0x1F101828)
    val MapGreen = Color(0xFF159947)
    val MapOrange = Color(0xFFFF7A00)
    val MapLine = Color(0xFFCBD5E1)
    val SuccessGradient = Brush.verticalGradient(listOf(Color(0xFF008F25), Color(0xFF006D1F)))
    val DarkGradient = Brush.verticalGradient(listOf(Color(0xFF032A10), Color(0xFF00892D)))
    val RedGradient = Brush.verticalGradient(listOf(Color(0xFFE11D2E), Color(0xFFC11121)))
}

@Immutable
object UpDimens {
    val screenPadding: Dp = 18.dp
    val cardRadius: Dp = 22.dp
    val buttonRadius: Dp = 16.dp
    val fieldRadius: Dp = 12.dp
    val cardSpace: Dp = 14.dp
    val bottomHeight: Dp = 72.dp
    val actionHeight: Dp = 54.dp
}

val UpShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val UpAppFont = FontFamily.SansSerif
private val AppFont = UpAppFont
private fun style(size: Int, weight: FontWeight, line: Int = (size * 1.25f).toInt()) = TextStyle(
    fontFamily = AppFont,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    color = UpColors.Ink,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

val UpTypography = Typography(
    displayLarge = style(32, FontWeight.Black, 38),
    displayMedium = style(28, FontWeight.Black, 34),
    displaySmall = style(24, FontWeight.Black, 30),
    headlineLarge = style(22, FontWeight.Black, 28),
    headlineMedium = style(20, FontWeight.ExtraBold, 26),
    headlineSmall = style(18, FontWeight.ExtraBold, 24),
    titleLarge = style(17, FontWeight.ExtraBold, 22),
    titleMedium = style(15, FontWeight.Bold, 20),
    titleSmall = style(14, FontWeight.Bold, 18),
    bodyLarge = style(15, FontWeight.Normal, 22),
    bodyMedium = style(13, FontWeight.Normal, 18),
    bodySmall = style(12, FontWeight.Normal, 17),
    labelLarge = style(14, FontWeight.Bold, 18),
    labelMedium = style(13, FontWeight.Bold, 17),
    labelSmall = style(11, FontWeight.SemiBold, 14)
)

val UpScheme = lightColorScheme(
    primary = UpColors.Green,
    onPrimary = Color.White,
    background = UpColors.Screen,
    onBackground = UpColors.Ink,
    surface = UpColors.Surface,
    onSurface = UpColors.Ink,
    outline = UpColors.Line,
    error = UpColors.Red,
    onError = Color.White
)

@Composable
fun UpTheme(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale = 1f)
    ) {
        MaterialTheme(
            colorScheme = UpScheme,
            typography = UpTypography,
            shapes = UpShapes
        ) {
            ProvideTextStyle(value = UpTypography.bodyMedium) {
                content()
            }
        }
    }
}

object UpElevations {
    val card = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
    val floating = CardDefaults.cardElevation(defaultElevation = 8.dp)
}

object UpBorders {
    val normal = BorderStroke(1.dp, UpColors.Line)
    val green = BorderStroke(1.dp, UpColors.Green.copy(alpha = .45f))
    val red = BorderStroke(1.dp, UpColors.Red.copy(alpha = .45f))
    val orange = BorderStroke(1.dp, UpColors.Orange.copy(alpha = .45f))
}
