package com.rodriguesacai.entregador

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun RodriguesNativeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
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

    val dark = darkColorScheme(
        primary = Color(0xFF82C91E),
        onPrimary = Color(0xFF10200A),
        secondary = Color(0xFF9B6DFF),
        background = Color(0xFF050507),
        surface = Color(0xFF15151C),
        onSurface = Color.White,
        onBackground = Color.White,
        outline = Color(0xFF3B3644)
    )

    val light = lightColorScheme(
        primary = Color(0xFF2E7D00),
        onPrimary = Color.White,
        secondary = Color(0xFF008A2E),
        background = Color.White,
        surface = Color.White,
        onSurface = Color(0xFF111318),
        onBackground = Color(0xFF111318),
        outline = Color(0xFFE6EAF0)
    )

    MaterialTheme(colorScheme = light, typography = typography, content = content)
}
