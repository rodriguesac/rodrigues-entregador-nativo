package com.rodriguesacai.entregador

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.rodriguesacai.entregador.ui.RodriguesEntregadorApp

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withLockedFontScale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resources.configuration.fontScale = 1f
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = 1f
                )
            ) {
                RodriguesEntregadorApp()
            }
        }
    }
}

fun Context.withLockedFontScale(): Context {
    val configuration = Configuration(resources.configuration)
    configuration.fontScale = 1f
    return createConfigurationContext(configuration)
}
