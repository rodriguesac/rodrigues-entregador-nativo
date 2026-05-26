package com.rodriguesacai.entregador.ui.design

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object EntregadorColors {
    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFF7FAFC)
    val Border = Color(0xFFE8EEF3)
    val Text = Color(0xFF101216)
    val Muted = Color(0xFF677381)
    val MutedLight = Color(0xFF9AA6B2)
    val Green = Color(0xFF0FAE4B)
    val GreenDark = Color(0xFF07883E)
    val Orange = Color(0xFFFF7A00)
    val Red = Color(0xFFEF233C)
    val Blue = Color(0xFF2563EB)
}

object EntregadorShape {
    val Card = RoundedCornerShape(30.dp)
    val Button = RoundedCornerShape(22.dp)
    val Chip = RoundedCornerShape(999.dp)
}

@Composable
fun EntregadorCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = EntregadorShape.Card,
        colors = CardDefaults.cardColors(containerColor = EntregadorColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth().border(1.dp, EntregadorColors.Border, EntregadorShape.Card)
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun EntregadorPrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit, content: @Composable RowScope.() -> Unit = { Text(text, fontWeight = FontWeight.Black, fontSize = 15.sp) }) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = EntregadorShape.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = EntregadorColors.Green,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFE6EBEF),
            disabledContentColor = EntregadorColors.MutedLight
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        content = content
    )
}

@Composable
fun EntregadorTitle(title: String, subtitle: String = "") {
    Text(title, color = EntregadorColors.Text, fontSize = 26.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black)
    if (subtitle.isNotBlank()) Text(subtitle, color = EntregadorColors.Muted, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)
}
