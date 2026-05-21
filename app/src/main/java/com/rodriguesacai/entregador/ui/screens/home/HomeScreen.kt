package com.rodriguesacai.entregador.ui.screens.home

import androidx.compose.runtime.Composable
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.DriverUiState
import com.rodriguesacai.entregador.ui.components.V17HomeScaffold
import com.rodriguesacai.entregador.ui.navigation.AppRoute

@Composable
fun HomeScreen(
    state: DriverUiState,
    onOnline: (Boolean) -> Unit,
    onPermissions: () -> Unit,
    onUrgent: (Ride) -> Unit,
    onRide: (Ride) -> Unit,
    onNav: (AppRoute) -> Unit,
    onLogout: () -> Unit,
    onToggleValues: (Boolean) -> Unit
) {
    V17HomeScaffold(
        state = state,
        onOnline = onOnline,
        onUrgent = onUrgent,
        onRide = onRide,
        onNav = onNav,
        onToggleValues = onToggleValues
    )
}
