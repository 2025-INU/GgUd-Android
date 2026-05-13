package com.capstone.ggud.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.graphics.Color
import android.view.SurfaceView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.capstone.ggud.R
import com.capstone.ggud.network.dto.RouteOption
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.PolylineOptions
import com.kakao.vectormap.shape.PolylineStyle

data class OngoingParticipantLocation(
    val userId: Long,
    val nickname: String,
    val latitude: Double,
    val longitude: Double,
    val isArrived: Boolean
)

@Composable
fun OngoingMapScreen(
    modifier: Modifier = Modifier,
    mapKey: String = "ongoing_route_map",
    selectedRouteOption: RouteOption?,
    destinationLat: Double?,
    destinationLon: Double?,
    participantLocations: List<OngoingParticipantLocation> = emptyList(),
    onCurrentLocationLoaded: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val defaultLocation = LatLng.from(37.5665, 126.9780)

    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    var kakaoMap by remember(mapKey) {
        mutableStateOf<KakaoMap?>(null)
    }

    var startRequested by remember(mapKey) {
        mutableStateOf(false)
    }

    fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return finePermission == PackageManager.PERMISSION_GRANTED ||
                coarsePermission == PackageManager.PERMISSION_GRANTED
    }

    fun loadCurrentLocation() {
        if (!hasLocationPermission()) return

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
                if (location == null) return@addOnSuccessListener

                val latLng = LatLng.from(
                    location.latitude,
                    location.longitude
                )

                currentLocation = latLng

                onCurrentLocationLoaded(
                    location.latitude,
                    location.longitude
                )
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            loadCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            loadCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(
        kakaoMap,
        selectedRouteOption,
        destinationLat,
        destinationLon,
        participantLocations,
        currentLocation
    ) {
        val map = kakaoMap ?: return@LaunchedEffect

        drawSelectedRoute(
            map = map,
            selectedRouteOption = selectedRouteOption,
            currentLocation = currentLocation,
            destinationLat = destinationLat,
            destinationLon = destinationLon,
            participantLocations = participantLocations
        )
    }

    val mapView = remember(mapKey) {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = {
            mapView.post {
                ensureOngoingSurfaceMatchParent(mapView)
            }
        }
    )

    DisposableEffect(mapView) {
        val listener = View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->

            if (!startRequested && view.width > 0 && view.height > 0) {
                startRequested = true

                ensureOngoingSurfaceMatchParent(mapView)

                startOngoingKakaoMap(
                    mapView = mapView,
                    initialPosition = currentLocation ?: defaultLocation,
                    onReady = { map ->
                        kakaoMap = map
                        loadCurrentLocation()
                    }
                )
            }
        }

        mapView.addOnLayoutChangeListener(listener)

        onDispose {
            mapView.removeOnLayoutChangeListener(listener)
        }
    }

    DisposableEffect(lifecycleOwner, startRequested) {
        val observer = LifecycleEventObserver { _, event ->

            if (!startRequested) return@LifecycleEventObserver

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.resume()
                    loadCurrentLocation()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    mapView.pause()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    mapView.finish()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private fun drawSelectedRoute(
    map: KakaoMap,
    selectedRouteOption: RouteOption?,
    currentLocation: LatLng?,
    destinationLat: Double?,
    destinationLon: Double?,
    participantLocations: List<OngoingParticipantLocation>
) {
    val shapeLayer = map.shapeManager?.layer
    val labelLayer = map.labelManager?.layer

    shapeLayer?.removeAll()
    labelLayer?.removeAll()

    val routePoints = selectedRouteOption
        ?.routes
        ?.flatMap { routeStep ->
            parseLineStringToLatLng(routeStep.linestring)
        }
        ?.distinctBy { "${it.latitude},${it.longitude}" }
        .orEmpty()

    if (routePoints.size >= 2) {
        val polylineStyle = PolylineStyle.from(
            8f,
            Color.parseColor("#4F46E5")
        )

        val polylineOptions = PolylineOptions.from(
            MapPoints.fromLatLng(routePoints),
            polylineStyle
        )

        shapeLayer?.addPolyline(polylineOptions)

        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(routePoints.first())
        )
    } else {
        currentLocation?.let { position ->
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(position)
            )
        }
    }

    val myMarkerStyle = LabelStyle.from(R.drawable.ic_map_marker) //내위치
    val destinationMarkerStyle = LabelStyle.from(R.drawable.ic_map_marker) //도착지
    val participantMarkerStyle = LabelStyle.from(R.drawable.ic_map_marker) //참가자

    currentLocation?.let { origin ->
        labelLayer?.addLabel(
            LabelOptions.from(origin)
                .setStyles(myMarkerStyle)
        )
    }

    if (destinationLat != null && destinationLon != null) {
        val destination = LatLng.from(destinationLat, destinationLon)

        labelLayer?.addLabel(
            LabelOptions.from(destination)
                .setStyles(destinationMarkerStyle)
        )
    }

    participantLocations.forEach { participant ->
        val participantPosition = LatLng.from(
            participant.latitude,
            participant.longitude
        )

        labelLayer?.addLabel(
            LabelOptions.from(participantPosition)
                .setStyles(participantMarkerStyle)
        )
    }
}

private fun parseLineStringToLatLng(linestring: String?): List<LatLng> {
    if (linestring.isNullOrBlank()) return emptyList()

    return linestring
        .trim()
        .split(" ")
        .mapNotNull { point ->
            val parts = point.split(",")

            if (parts.size != 2) return@mapNotNull null

            val lon = parts[0].toDoubleOrNull()
            val lat = parts[1].toDoubleOrNull()

            if (lat != null && lon != null) {
                LatLng.from(lat, lon)
            } else {
                null
            }
        }
}

private fun startOngoingKakaoMap(
    mapView: MapView,
    initialPosition: LatLng,
    onReady: (KakaoMap) -> Unit
) {
    mapView.start(
        object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}

            override fun onMapError(error: Exception) {}
        },
        object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                onReady(map)
            }

            override fun getPosition(): LatLng {
                return initialPosition
            }

            override fun getZoomLevel(): Int = 15
        }
    )
}

private fun ensureOngoingSurfaceMatchParent(
    root: View,
    tryCount: Int = 0
) {
    val surfaceViews = mutableListOf<SurfaceView>()

    fun dfs(view: View) {
        if (view is SurfaceView) {
            surfaceViews.add(view)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                dfs(view.getChildAt(i))
            }
        }
    }

    dfs(root)

    if (surfaceViews.isEmpty()) {
        if (tryCount < 20) {
            root.postDelayed(
                {
                    ensureOngoingSurfaceMatchParent(
                        root = root,
                        tryCount = tryCount + 1
                    )
                },
                50L
            )
        }
        return
    }

    surfaceViews.forEach { surfaceView ->
        val layoutParams = surfaceView.layoutParams

        val needFix = layoutParams == null ||
                layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT ||
                layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT

        if (needFix) {
            surfaceView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        if (surfaceView.width == 0 || surfaceView.height == 0) {
            surfaceView.requestLayout()
            (surfaceView.parent as? View)?.requestLayout()
        }

        surfaceView.invalidate()
    }
}