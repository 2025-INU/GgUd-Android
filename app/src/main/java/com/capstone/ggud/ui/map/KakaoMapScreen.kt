package com.capstone.ggud.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelStyle
import com.capstone.ggud.R
import com.kakao.vectormap.label.LabelOptions

data class KakaoMapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double
)

@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier,
    mapKey: String = "default",
    markerLatitude: Double? = null,
    markerLongitude: Double? = null,
    markers: List<KakaoMapMarker> = emptyList(),
    focusedMarkerId: String? = null,
    markerResId: Int = R.drawable.ic_map_marker,
    moveToCurrentLocation: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val fusedLocationClient = remember { //구글 위치 서비스: 현재 위치 가져오기용
        LocationServices.getFusedLocationProviderClient(context)
    }

    val defaultLocation = LatLng.from(37.375, 126.632)

    var currentLocation by remember { //현재 위치상태
        mutableStateOf<LatLng?>(null)
    }

    var kakaoMap by remember(mapKey) { //카카오맵 객체
        mutableStateOf<KakaoMap?>(null)
    }

    var startRequested by remember(mapKey) { //map start() 중복 방지
        mutableStateOf(false)
    }

    //현재 위치 가져오는 함수
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

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) return@addOnSuccessListener

                val latLng = LatLng.from(
                    location.latitude,
                    location.longitude
                )

                currentLocation = latLng

                if (moveToCurrentLocation) {
                    kakaoMap?.moveCamera(
                        CameraUpdateFactory.newCenterPosition(latLng)
                    )
                }
            }
    }

    //위치 권한 요청
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

    //최초 실행 시 권한 있으면 위치 가져오기 없으면 권한 요청
    LaunchedEffect(moveToCurrentLocation) {
        if (!moveToCurrentLocation) return@LaunchedEffect

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

    LaunchedEffect(kakaoMap, markerLatitude, markerLongitude, markers, focusedMarkerId, markerResId) {
        val map = kakaoMap ?: return@LaunchedEffect

        val targetPosition = when {
            markers.isNotEmpty() -> {
                val targetMarker = markers.firstOrNull { it.id == focusedMarkerId }
                    ?: markers.first()

                LatLng.from(
                    targetMarker.latitude,
                    targetMarker.longitude
                )
            }

            markerLatitude != null && markerLongitude != null -> {
                LatLng.from(markerLatitude, markerLongitude)
            }

            else -> return@LaunchedEffect
        }

        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(targetPosition)
        )

        val labelManager = map.labelManager ?: return@LaunchedEffect
        val layer = labelManager.layer ?: return@LaunchedEffect
        val style = LabelStyle.from(markerResId)

        layer.removeAll()

        if (markers.isNotEmpty()) {
            markers.forEach { marker ->
                val position = LatLng.from(marker.latitude, marker.longitude)

                val options = LabelOptions.from(position)
                    .setStyles(style)

                layer.addLabel(options)
            }
        } else {
            val options = LabelOptions.from(targetPosition)
                .setStyles(style)

            layer.addLabel(options)
        }
    }

    //카카오 맵뷰 생성
    val mapView = remember(mapKey) {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    //화면에 지도 붙이는 부분
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = {
            //SurfaceView 크기 깨지는 버그 보정
            mapView.post { ensureSurfaceMatchParent(mapView) }
        }
    )

    //MapView가 화면에 그려진 이후 start() 호출
    DisposableEffect(mapView) {
        val listener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->

            if (!startRequested && v.width > 0 && v.height > 0) {
                startRequested = true

                ensureSurfaceMatchParent(mapView)

                startKakaoMap(
                    mapView = mapView,
                    initialPosition = currentLocation ?: defaultLocation,

                    //지도 준비 완료 시
                    onReady = { map ->
                        kakaoMap = map

                        if (moveToCurrentLocation) {
                            val position = currentLocation ?: defaultLocation

                            map.moveCamera(
                                CameraUpdateFactory.newCenterPosition(position)
                            )

                            loadCurrentLocation()
                        }
                    }
                )
            }
        }

        mapView.addOnLayoutChangeListener(listener)

        onDispose {
            mapView.removeOnLayoutChangeListener(listener)
        }
    }

    //앱 생명주기 지도 맞춰주기
    DisposableEffect(lifecycleOwner, startRequested) {
        val observer = LifecycleEventObserver { _, event ->

            if (!startRequested) return@LifecycleEventObserver

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.resume()

                    if (moveToCurrentLocation) {
                        loadCurrentLocation()
                    }
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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

//지도 시작
private fun startKakaoMap(
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
            override fun onMapReady(map: KakaoMap) { //지도 준비 완료
                onReady(map)
            }

            override fun getPosition(): LatLng { //최초 지도 중심 위치
                return initialPosition
            }

            override fun getZoomLevel(): Int = 15
        }
    )
}

//맵뷰 내부 버그 대응
private fun ensureSurfaceMatchParent(root: View, tryCount: Int = 0) {
    val surfaceViews = mutableListOf<SurfaceView>()

    fun dfs(v: View) {
        if (v is SurfaceView) surfaceViews.add(v)
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) dfs(v.getChildAt(i))
        }
    }
    dfs(root)

    if (surfaceViews.isEmpty()) {
        if (tryCount < 20) {
            root.postDelayed({ ensureSurfaceMatchParent(root, tryCount + 1) }, 50L)
        }
        return
    }

    surfaceViews.forEach { sv ->
        val lp = sv.layoutParams
        val needFix = lp == null ||
                lp.width != ViewGroup.LayoutParams.MATCH_PARENT ||
                lp.height != ViewGroup.LayoutParams.MATCH_PARENT

        if (needFix) {
            sv.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        //0x0으로 찍히는 상황이면 강제 레이아웃 트리거
        if (sv.width == 0 || sv.height == 0) {
            sv.requestLayout()
            (sv.parent as? View)?.requestLayout()
        }

        sv.invalidate()
    }
}