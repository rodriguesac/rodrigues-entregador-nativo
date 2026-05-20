package com.rodriguesacai.entregador.ui.components

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.rodriguesacai.entregador.data.Ride
import com.rodriguesacai.entregador.ui.deliveryAddressVisible
import com.rodriguesacai.entregador.ui.theme.AppColors
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun NativeMapPreview(ride: Ride?, modifier: Modifier = Modifier.fillMaxWidth().height(230.dp)) {
    if (ride == null) {
        MiniMapDrawing(modifier)
        return
    }
    val mapView = rememberMapViewWithLifecycle()
    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.getMapAsync { googleMap ->
                val loja = LatLng(ride.lojaLat, ride.lojaLng)
                val cliente = LatLng(ride.clienteLat, ride.clienteLng)
                val showDelivery = ride.deliveryAddressVisible()
                googleMap.clear()
                googleMap.uiSettings.isZoomControlsEnabled = false
                googleMap.uiSettings.isMapToolbarEnabled = false
                googleMap.addMarker(MarkerOptions().position(loja).title("Coleta"))
                if (showDelivery) {
                    googleMap.addMarker(MarkerOptions().position(cliente).title("Entrega"))
                    googleMap.addPolyline(
                        PolylineOptions()
                            .add(loja, cliente)
                            .width(7f)
                            .color(AppColors.Green.toArgb())
                    )
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(if (showDelivery) cliente else loja, 14f))
            }
        }
    )
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }
    return mapView
}
