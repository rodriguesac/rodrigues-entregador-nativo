package com.rodriguesacai.entregador.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.abs

private const val TOMTOM_API_KEY = "tmsKTjnNOPUHNDHOYh2m12VrmwejmK8t"
private const val ROUTE_REFRESH_MS = 30_000L

private val MapPanel = Color(0xFF0E1117)
private val MapGreen = Color(0xFF82C91E)
private val MapPurple = Color(0xFF7C4DFF)
private val MapBlue = Color(0xFF1684FF)

/**
 * Modo da rota exibida no mapa.
 * PICKUP_TO_DROPOFF: usado na oferta/preview geral.
 * DRIVER_TO_PICKUP: usado antes de retirar pedido, rota do motoboy até a loja.
 * DRIVER_TO_DROPOFF: usado depois de retirar pedido, rota do motoboy até o cliente.
 */
enum class DeliveryMapMode {
    PICKUP_TO_DROPOFF,
    DRIVER_TO_PICKUP,
    DRIVER_TO_DROPOFF
}

private data class RouteMapState(
    val driver: GeoPoint? = null,
    val pickup: GeoPoint? = null,
    val dropoff: GeoPoint? = null,
    val route: List<GeoPoint> = emptyList(),
    val loading: Boolean = true,
    val updatedAtMillis: Long = 0L
)

@Composable
fun RealDeliveryMap(
    title: String,
    subtitle: String,
    pickupAddress: String,
    dropoffAddress: String,
    pickupLat: Double? = null,
    pickupLng: Double? = null,
    dropoffLat: Double? = null,
    dropoffLng: Double? = null,
    mode: DeliveryMapMode = DeliveryMapMode.PICKUP_TO_DROPOFF,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var state by remember(pickupAddress, dropoffAddress, pickupLat, pickupLng, dropoffLat, dropoffLng, mode) {
        mutableStateOf(RouteMapState())
    }
    var fullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(pickupAddress, dropoffAddress, pickupLat, pickupLng, dropoffLat, dropoffLng, mode) {
        while (true) {
            state = state.copy(loading = true)
            state = withContext(Dispatchers.IO) {
                buildRouteMapState(
                    context = context,
                    pickupAddress = pickupAddress,
                    dropoffAddress = dropoffAddress,
                    pickupLat = pickupLat,
                    pickupLng = pickupLng,
                    dropoffLat = dropoffLat,
                    dropoffLng = dropoffLng,
                    mode = mode
                )
            }
            delay(ROUTE_REFRESH_MS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(245.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MapPanel)
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(26.dp))
    ) {
        CleanOsmMap(
            state = state,
            modifier = Modifier.fillMaxSize(),
            tapOpensFullscreen = true,
            onTapMap = { fullscreen = true }
        )

        FullscreenMapButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) { fullscreen = true }
    }

    if (fullscreen) {
        FullscreenRouteMap(
            title = title,
            subtitle = subtitle,
            state = state,
            onClose = { fullscreen = false }
        )
    }
}

@Composable
private fun FullscreenRouteMap(
    title: String,
    subtitle: String,
    state: RouteMapState,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CleanOsmMap(
                state = state,
                modifier = Modifier.fillMaxSize(),
                tapOpensFullscreen = false,
                onTapMap = {}
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC0A0C11))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            }

        }
    }
}

@Composable
private fun CleanOsmMap(
    state: RouteMapState,
    modifier: Modifier,
    tapOpensFullscreen: Boolean,
    onTapMap: () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            Configuration.getInstance().userAgentValue = ctx.packageName
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(!tapOpensFullscreen)
                minZoomLevel = 4.0
                maxZoomLevel = 20.0
                controller.setZoom(if (tapOpensFullscreen) 14.5 else 15.2)
                controller.setCenter(GeoPoint(-20.4697, -54.6201))
                if (tapOpensFullscreen) {
                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_UP) onTapMap()
                        true
                    }
                } else {
                    setOnTouchListener { view, event ->
                        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                        } else {
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        false
                    }
                }
            }
        },
        update = { map -> applyRouteToMap(map, state) }
    )
}

@Composable
private fun FullscreenMapButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xE60A0C11))
            .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val stroke = Stroke(width = 3f, cap = StrokeCap.Round)
            val c = Color.White
            val s = size.width
            val p = 1.5f
            val l = s * 0.34f

            drawLine(c, Offset(p, p + l), Offset(p, p), strokeWidth = stroke.width, cap = stroke.cap)
            drawLine(c, Offset(p, p), Offset(p + l, p), strokeWidth = stroke.width, cap = stroke.cap)

            drawLine(c, Offset(s - p - l, p), Offset(s - p, p), strokeWidth = stroke.width, cap = stroke.cap)
            drawLine(c, Offset(s - p, p), Offset(s - p, p + l), strokeWidth = stroke.width, cap = stroke.cap)

            drawLine(c, Offset(p, s - p - l), Offset(p, s - p), strokeWidth = stroke.width, cap = stroke.cap)
            drawLine(c, Offset(p, s - p), Offset(p + l, s - p), strokeWidth = stroke.width, cap = stroke.cap)

            drawLine(c, Offset(s - p - l, s - p), Offset(s - p, s - p), strokeWidth = stroke.width, cap = stroke.cap)
            drawLine(c, Offset(s - p, s - p - l), Offset(s - p, s - p), strokeWidth = stroke.width, cap = stroke.cap)
        }
    }
}

private fun applyRouteToMap(map: MapView, state: RouteMapState) {
    map.overlays.clear()

    val pointsForCamera = buildList {
        state.driver?.let { add(it) }
        state.pickup?.let { add(it) }
        state.dropoff?.let { add(it) }
        addAll(state.route)
    }.distinctBy { "${it.latitude},${it.longitude}" }

    if (pointsForCamera.isNotEmpty()) {
        val center = pointsForCamera.centerPoint()
        map.controller.setCenter(center)
        map.controller.setZoom(pointsForCamera.bestZoom())
    }

    if (state.route.size >= 2) {
        val line = Polyline().apply {
            setPoints(state.route)
            outlinePaint.color = android.graphics.Color.rgb(124, 77, 255)
            outlinePaint.strokeWidth = 10f
            outlinePaint.isAntiAlias = true
        }
        map.overlays.add(line)
    }

    state.pickup?.let {
        map.overlays.add(Marker(map).apply {
            position = it
            title = "Loja / coleta"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
    }

    state.dropoff?.let {
        map.overlays.add(Marker(map).apply {
            position = it
            title = "Cliente / entrega"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
    }

    state.driver?.let {
        map.overlays.add(Marker(map).apply {
            position = it
            title = "Sua localização"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        })
    }

    map.invalidate()
}

private suspend fun buildRouteMapState(
    context: Context,
    pickupAddress: String,
    dropoffAddress: String,
    pickupLat: Double?,
    pickupLng: Double?,
    dropoffLat: Double?,
    dropoffLng: Double?,
    mode: DeliveryMapMode
): RouteMapState {
    val pickup = coordinateOrNull(pickupLat, pickupLng) ?: geocodeTomTom(pickupAddress)
    val dropoff = coordinateOrNull(dropoffLat, dropoffLng) ?: geocodeTomTom(dropoffAddress)
    val driver = if (mode == DeliveryMapMode.PICKUP_TO_DROPOFF) null else context.lastKnownDriverPoint()

    val start = when (mode) {
        DeliveryMapMode.PICKUP_TO_DROPOFF -> pickup
        DeliveryMapMode.DRIVER_TO_PICKUP -> driver ?: pickup
        DeliveryMapMode.DRIVER_TO_DROPOFF -> driver ?: pickup
    }

    val end = when (mode) {
        DeliveryMapMode.PICKUP_TO_DROPOFF -> dropoff
        DeliveryMapMode.DRIVER_TO_PICKUP -> pickup
        DeliveryMapMode.DRIVER_TO_DROPOFF -> dropoff
    }

    val route = if (start != null && end != null) fetchTomTomRoute(start, end) else emptyList()
    val fallbackRoute = route.ifEmpty { listOfNotNull(start, end) }

    return RouteMapState(
        driver = driver,
        pickup = pickup,
        dropoff = dropoff,
        route = fallbackRoute,
        loading = false,
        updatedAtMillis = System.currentTimeMillis()
    )
}

private fun coordinateOrNull(lat: Double?, lng: Double?): GeoPoint? {
    val safeLat = lat ?: return null
    val safeLng = lng ?: return null
    if (safeLat == 0.0 || safeLng == 0.0) return null
    if (abs(safeLat) > 90 || abs(safeLng) > 180) return null
    return GeoPoint(safeLat, safeLng)
}

@SuppressLint("MissingPermission")
private fun Context.lastKnownDriverPoint(): GeoPoint? {
    val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return null

    val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)

    val best: Location = providers
        .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }
        ?: return null

    return coordinateOrNull(best.latitude, best.longitude)
}

private fun geocodeTomTom(address: String): GeoPoint? {
    val clean = address.trim()
    if (clean.length < 4) return null
    return runCatching {
        val query = URLEncoder.encode("$clean, Campo Grande, MS, Brasil", "UTF-8")
        val url = "https://api.tomtom.com/search/2/geocode/$query.json?key=$TOMTOM_API_KEY&countrySet=BR&limit=1"
        val json = httpGetJson(url)
        val position = JSONObject(json)
            .optJSONArray("results")
            ?.optJSONObject(0)
            ?.optJSONObject("position")
        val lat = position?.optDouble("lat") ?: return null
        val lon = position.optDouble("lon")
        coordinateOrNull(lat, lon)
    }.getOrNull()
}

private fun fetchTomTomRoute(pickup: GeoPoint, dropoff: GeoPoint): List<GeoPoint> {
    return runCatching {
        val url = "https://api.tomtom.com/routing/1/calculateRoute/${pickup.latitude},${pickup.longitude}:${dropoff.latitude},${dropoff.longitude}/json?key=$TOMTOM_API_KEY&traffic=true&routeType=fastest&travelMode=motorcycle"
        val json = httpGetJson(url)
        val points = JSONObject(json)
            .optJSONArray("routes")
            ?.optJSONObject(0)
            ?.optJSONArray("legs")
            ?.optJSONObject(0)
            ?.optJSONArray("points")
            ?: return emptyList()
        buildList {
            for (i in 0 until points.length()) {
                val point = points.optJSONObject(i) ?: continue
                val lat = point.optDouble("latitude")
                val lon = point.optDouble("longitude")
                coordinateOrNull(lat, lon)?.let { add(it) }
            }
        }
    }.getOrElse { emptyList() }
}

private fun httpGetJson(url: String): String {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 9000
        readTimeout = 12000
        useCaches = true
    }
    return try {
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        stream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun List<GeoPoint>.centerPoint(): GeoPoint {
    val lat = map { it.latitude }.average().takeIf { !it.isNaN() } ?: -20.4697
    val lon = map { it.longitude }.average().takeIf { !it.isNaN() } ?: -54.6201
    return GeoPoint(lat, lon)
}

private fun List<GeoPoint>.bestZoom(): Double {
    if (size < 2) return 15.2
    val latSpread = (maxOf { it.latitude } - minOf { it.latitude }).let { abs(it) }
    val lonSpread = (maxOf { it.longitude } - minOf { it.longitude }).let { abs(it) }
    val spread = maxOf(latSpread, lonSpread)
    return when {
        spread < 0.006 -> 16.2
        spread < 0.018 -> 15.0
        spread < 0.045 -> 13.8
        spread < 0.090 -> 12.8
        else -> 11.7
    }
}
