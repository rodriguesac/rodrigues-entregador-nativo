package com.rodriguesacai.entregador

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

object RodriguesFonts {
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
    private val montserrat = GoogleFont("Montserrat")

    val Montserrat: FontFamily = FontFamily(
        Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.SemiBold),
        Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.Bold),
        Font(googleFont = montserrat, fontProvider = provider, weight = FontWeight.Black)
    )
}
