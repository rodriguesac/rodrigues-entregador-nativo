package com.rodriguesacai.entregador.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.rodriguesacai.entregador.data.DriverRepository
import com.rodriguesacai.entregador.data.DriverRide
import kotlin.math.max

class OnlineDriverService : Service() {
    private var pendingRideListener: ListenerRegistration? = null
    private var activeRideListener: ListenerRegistration? = null
    private var configListener: ListenerRegistration? = null

    private var lastNotifiedRideId: String? = null
    private var activeRide: DriverRide? = null
    private var activeRideKey: String? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var trackingIntervalMs: Long = 30_000L
    private var lastLocation: Location? = null
    private var trackedDistanceKm: Double = 0.0
    private var trackingStartedAtMillis: Long = 0L

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        DriverRepository.setOnline(this, true)
        startForeground(10, NotificationHelper.onlineNotification(this))
        startConfigListener()
        startRideListener()
        startActiveRideListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DriverRepository.setOnline(this, true)
        if (pendingRideListener == null) startRideListener()
        if (activeRideListener == null) startActiveRideListener()
        if (configListener == null) startConfigListener()
        return START_STICKY
    }

    private fun startRideListener() {
        pendingRideListener?.remove()
        pendingRideListener = DriverRepository.listenPendingRide(
            context = this,
            onRide = { ride ->
                if (ride != null && ride.id != lastNotifiedRideId) {
                    lastNotifiedRideId = ride.id
                    NotificationHelper.urgentRideNotification(
                        context = this,
                        rideId = ride.id,
                        value = ride.value,
                        distance = ride.distance,
                        duration = ride.duration,
                        pickup = ride.pickup,
                        dropoff = ride.dropoff
                    )
                }
            },
            onError = { }
        )
    }

    private fun startActiveRideListener() {
        activeRideListener?.remove()
        activeRideListener = DriverRepository.listenMyActiveRide(
            context = this,
            onRide = { ride -> handleActiveRideChanged(ride) },
            onError = { }
        )
    }

    private fun startConfigListener() {
        configListener?.remove()
        configListener = FirebaseFirestore.getInstance()
            .collection("configuracoes_loja")
            .document("logistica_ultra_v4")
            .addSnapshotListener { snap, _ ->
                val next = readTrackingIntervalMs(snap)
                if (next != trackingIntervalMs) {
                    trackingIntervalMs = next
                    if (activeRide != null) restartLocationUpdates()
                }
            }
    }

    private fun handleActiveRideChanged(ride: DriverRide?) {
        if (ride == null) {
            activeRide = null
            activeRideKey = null
            stopLocationUpdates()
            DriverRepository.clearDriverTracking(this)
            return
        }

        val key = "${ride.collectionName}:${ride.id}"
        activeRide = ride

        if (key != activeRideKey) {
            activeRideKey = key
            lastLocation = null
            trackedDistanceKm = 0.0
            trackingStartedAtMillis = System.currentTimeMillis()
        }

        startLocationUpdates()
    }

    private fun restartLocationUpdates() {
        stopLocationUpdates(clearFirebase = false)
        if (activeRide != null) startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val ride = activeRide ?: return
        if (!hasLocationPermission()) return
        if (locationCallback != null) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, trackingIntervalMs)
            .setMinUpdateIntervalMillis(max(5_000L, trackingIntervalMs / 2))
            .setMaxUpdateDelayMillis(trackingIntervalMs)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val currentRide = activeRide ?: return

                val previous = lastLocation
                if (previous != null) {
                    val meters = previous.distanceTo(location)
                    if (meters.isFinite() && meters in 2f..2500f) {
                        trackedDistanceKm += meters / 1000.0
                    }
                }
                lastLocation = location

                val tempoMovimentoMin = if (trackingStartedAtMillis > 0L) {
                    max(0L, (System.currentTimeMillis() - trackingStartedAtMillis) / 60_000L)
                } else {
                    0L
                }

                DriverRepository.updateDriverLocationForTracking(
                    context = this@OnlineDriverService,
                    ride = currentRide,
                    lat = location.latitude,
                    lng = location.longitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    bearing = location.bearing,
                    distanciaPercorridaKm = trackedDistanceKm,
                    tempoMovimentoMin = tempoMovimentoMin,
                    intervaloSeg = trackingIntervalMs / 1000L
                )
            }
        }

        fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                DriverRepository.updateDriverLocationForTracking(
                    context = this,
                    ride = ride,
                    lat = location.latitude,
                    lng = location.longitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    bearing = location.bearing,
                    distanciaPercorridaKm = trackedDistanceKm,
                    tempoMovimentoMin = 0L,
                    intervaloSeg = trackingIntervalMs / 1000L
                )
            }
        }
    }

    private fun stopLocationUpdates(clearFirebase: Boolean = true) {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
        }
        locationCallback = null
        lastLocation = null
        trackedDistanceKm = 0.0
        trackingStartedAtMillis = 0L
        if (clearFirebase) DriverRepository.clearDriverTracking(this)
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun readTrackingIntervalMs(snap: DocumentSnapshot?): Long {
        val raw =
            snap?.getDoubleCompat("rastreamentoIntervaloSeg")
                ?: snap?.getDoubleCompat("intervaloRastreamentoSeg")
                ?: snap?.getDoubleCompat("trackingIntervalSec")
                ?: snap?.getDoubleCompat("trackingIntervalSeconds")
                ?: 30.0

        val seconds = raw.toLong().coerceIn(10L, 120L)
        return seconds * 1000L
    }

    override fun onDestroy() {
        pendingRideListener?.remove()
        activeRideListener?.remove()
        configListener?.remove()
        pendingRideListener = null
        activeRideListener = null
        configListener = null
        stopLocationUpdates(clearFirebase = false)
        // Não marca offline aqui: se o Android matar/recriar o serviço, o entregador continua disponível.
        // O offline real é gravado quando o motoboy desliga o status no app.
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

private fun DocumentSnapshot.getDoubleCompat(key: String): Double? {
    val value = get(key) ?: return null
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.replace(",", ".").toDoubleOrNull()
        else -> null
    }
}

private fun Float.isFinite(): Boolean = !isNaN() && !isInfinite()
