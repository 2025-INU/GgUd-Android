package com.capstone.ggud.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
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
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.SurfaceView
import androidx.compose.runtime.mutableStateMapOf
import android.graphics.Canvas
import android.graphics.Shader
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.capstone.ggud.R
import com.capstone.ggud.network.dto.RouteOption
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapGravity
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.PolylineOptions
import com.kakao.vectormap.shape.PolylineStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

data class OngoingParticipantLocation(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
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

    var locationCallback by remember {
        mutableStateOf<LocationCallback?>(null)
    }

    var kakaoMap by remember(mapKey) {
        mutableStateOf<KakaoMap?>(null)
    }

    var startRequested by remember(mapKey) {
        mutableStateOf(false)
    }

    var followCurrentLocation by remember {
        mutableStateOf(true)
    }

    val profileMarkerBitmaps = remember {
        mutableStateMapOf<Long, Bitmap>()
    }

    val cachedProfileUrls = remember {
        mutableStateMapOf<Long, String?>()
    }

    var profileMarkerVersion by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(participantLocations) {
        participantLocations.forEach { participant ->
            val cacheKey = participant.profileImageUrl ?: "fallback"
            val cachedKey = cachedProfileUrls[participant.userId]

            if (cachedKey != cacheKey) {
                val bitmap = loadProfileMarkerBitmap(
                    context = context,
                    imageUrl = participant.profileImageUrl
                )

                profileMarkerBitmaps[participant.userId] = bitmap
                cachedProfileUrls[participant.userId] = cacheKey
                profileMarkerVersion += 1

                Log.d(
                    "OngoingMap",
                    "bitmap cached: userId=${participant.userId}, cacheKey=$cacheKey, version=$profileMarkerVersion, size=${bitmap.width}x${bitmap.height}"
                )
            }
        }
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

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermission()) return
        if (locationCallback != null) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        )
            .setMinUpdateIntervalMillis(3000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

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

        locationCallback = callback

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            context.mainLooper
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            loadCurrentLocation()
            startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            loadCurrentLocation()
            startLocationUpdates()
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
        profileMarkerVersion
    ) {
        val map = kakaoMap ?: return@LaunchedEffect

        drawSelectedRoute(
            map = map,
            selectedRouteOption = selectedRouteOption,
            currentLocation = currentLocation,
            destinationLat = destinationLat,
            destinationLon = destinationLon,
            participantLocations = participantLocations,
            profileMarkerBitmaps = profileMarkerBitmaps
        )
    }

    LaunchedEffect(kakaoMap, currentLocation, followCurrentLocation) {
        val map = kakaoMap ?: return@LaunchedEffect
        val location = currentLocation ?: return@LaunchedEffect

        if (followCurrentLocation) {
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(location)
            )
        }
    }

    val mapView = remember(mapKey) {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = {
                mapView.post {
                    ensureOngoingSurfaceMatchParent(mapView)
                }
            }
        )

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(size = 52.dp),
            containerColor = androidx.compose.ui.graphics.Color.White,
            contentColor = androidx.compose.ui.graphics.Color(0xFF2563EB),
            onClick = {
                val location = currentLocation ?: return@FloatingActionButton

                followCurrentLocation = true

                kakaoMap?.moveCamera(
                    CameraUpdateFactory.newCenterPosition(location)
                )
            }
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "현재 위치"
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let { callback ->
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }

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

                        map.setOnCameraMoveEndListener { _, _, gestureType ->
                            if (gestureType.name != "Unknown") {
                                followCurrentLocation = false
                            }
                        }

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

private suspend fun loadProfileMarkerBitmap(
    context: Context,
    imageUrl: String?
): Bitmap = withContext(Dispatchers.IO) {
    val validImageUrl = imageUrl?.takeIf { it.isNotBlank() }

    var usedFallback = true

    val sourceBitmap = if (validImageUrl == null) {
        Log.d("OngoingMap", "profile image skipped: url is null")
        null
    } else {
        runCatching {
            Log.d("OngoingMap", "profile image request start: $validImageUrl")

            val connection = URL(validImageUrl).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val responseCode = connection.responseCode
            val contentType = connection.contentType

            Log.d(
                "OngoingMap",
                "profile image response: code=$responseCode, contentType=$contentType"
            )

            if (responseCode !in 200..299) {
                connection.disconnect()
                null
            } else {
                val decodedBitmap = connection.inputStream.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }

                connection.disconnect()

                Log.d(
                    "OngoingMap",
                    "profile image decoded: success=${decodedBitmap != null}, size=${decodedBitmap?.width}x${decodedBitmap?.height}"
                )

                decodedBitmap
            }
        }.onFailure { throwable ->
            Log.e(
                "OngoingMap",
                "profile image load failed: $validImageUrl, error=${throwable.javaClass.simpleName}, message=${throwable.message}",
                throwable
            )
        }.getOrNull()
    }

    val markerSource = if (sourceBitmap != null) {
        usedFallback = false
        sourceBitmap
    } else {
        drawableToBitmap(context, R.drawable.ic_promise_profile)
    }

    val markerBitmap = createCircleProfileMarker(markerSource)

    Log.d(
        "OngoingMap",
        "profile marker created: usedFallback=$usedFallback, size=${markerBitmap.width}x${markerBitmap.height}"
    )

    markerBitmap
}

private fun drawableToBitmap(
    context: Context,
    drawableResId: Int
): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableResId)
        ?: return Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)

    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

private fun createCircleProfileMarker(
    source: Bitmap
): Bitmap {
    val size = 96
    val borderWidth = 6f

    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val scaled = Bitmap.createScaledBitmap(source, size, size, true)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = BitmapShader(
            scaled,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
    }

    val center = size / 2f

    canvas.drawCircle(
        center,
        center,
        center,
        borderPaint
    )

    canvas.drawCircle(
        center,
        center,
        center - borderWidth,
        imagePaint
    )

    return output
}

private fun drawSelectedRoute(
    map: KakaoMap,
    selectedRouteOption: RouteOption?,
    currentLocation: LatLng?,
    destinationLat: Double?,
    destinationLon: Double?,
    participantLocations: List<OngoingParticipantLocation>,
    profileMarkerBitmaps: Map<Long, Bitmap>
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
            Color.parseColor("#2563EB")
        )

        val polylineOptions = PolylineOptions.from(
            MapPoints.fromLatLng(routePoints),
            polylineStyle
        )

        shapeLayer?.addPolyline(polylineOptions)
    } else {
        currentLocation?.let { position ->
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(position)
            )
        }
    }

    val destinationMarkerStyle = LabelStyle.from(R.drawable.ic_map_marker) //도착지

    if (destinationLat != null && destinationLon != null) {
        val destination = LatLng.from(destinationLat, destinationLon)

        labelLayer?.addLabel(
            LabelOptions.from(destination)
                .setStyles(destinationMarkerStyle)
        )
    }

    participantLocations.forEach { participant ->
        val markerBitmap = profileMarkerBitmaps[participant.userId]
            ?: return@forEach

        val participantPosition = LatLng.from(
            participant.latitude,
            participant.longitude
        )

        val participantMarkerStyle = LabelStyle.from(markerBitmap)
            .setTextStyles(22, Color.parseColor("#111827"), 8, Color.WHITE)
            .setTextGravity(MapGravity.BOTTOM)
            .setAnchorPoint(0.5f, 1.0f)

        labelLayer?.addLabel(
            LabelOptions.from(participantPosition)
                .setStyles(participantMarkerStyle)
                .setTexts(
                    LabelTextBuilder().setTexts(participant.nickname)
                )
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