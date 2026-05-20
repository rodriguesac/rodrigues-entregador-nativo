package com.rodriguesacai.entregador

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rodriguesacai.entregador.ui.theme.RodriguesTheme

class UrgentOfferActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContent {
            RodriguesTheme {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.DeliveryDining, contentDescription = null)
                    Text(
                        text = "Nova corrida",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Abra o app para aceitar ou recusar.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                    )
                    Button(onClick = {
                        startActivity(Intent(this@UrgentOfferActivity, MainActivity::class.java))
                        finish()
                    }) {
                        Text("Abrir Rodrigues Entregador")
                    }
                }
            }
        }
    }
}
