package com.rodriguesacai.entregador

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun RodriguesNativeTheme(content: @Composable () -> Unit) {
    val montserratLike = RodriguesFonts.Montserrat
    val base = Typography()
    val typography = base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = montserratLike),
        displayMedium = base.displayMedium.copy(fontFamily = montserratLike),
        displaySmall = base.displaySmall.copy(fontFamily = montserratLike),
        headlineLarge = base.headlineLarge.copy(fontFamily = montserratLike),
        headlineMedium = base.headlineMedium.copy(fontFamily = montserratLike),
        headlineSmall = base.headlineSmall.copy(fontFamily = montserratLike),
        titleLarge = base.titleLarge.copy(fontFamily = montserratLike),
        titleMedium = base.titleMedium.copy(fontFamily = montserratLike),
        titleSmall = base.titleSmall.copy(fontFamily = montserratLike),
        bodyLarge = base.bodyLarge.copy(fontFamily = montserratLike),
        bodyMedium = base.bodyMedium.copy(fontFamily = montserratLike),
        bodySmall = base.bodySmall.copy(fontFamily = montserratLike),
        labelLarge = base.labelLarge.copy(fontFamily = montserratLike),
        labelMedium = base.labelMedium.copy(fontFamily = montserratLike),
        labelSmall = base.labelSmall.copy(fontFamily = montserratLike)
    )
    val colors = darkColorScheme(
        primary = Color(0xFF82C91E),
        onPrimary = Color(0xFF10200A),
        secondary = Color(0xFF9B6DFF),
        background = Color(0xFF050507),
        surface = Color(0xFF15151C),
        onSurface = Color.White,
        onBackground = Color.White,
        outline = Color(0xFF3B3644)
    )
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
