package com.capstone.ggud.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
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

@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier
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

    var kakaoMap by remember { //카카오맵 객체
        mutableStateOf<KakaoMap?>(null)
    }

    var startRequested by remember { //map start() 중복 방지
        mutableStateOf(false)
    }

    var ready by remember {
        mutableStateOf(false)
    }

    //현재 위치 가져오는 함수
    fun loadCurrentLocation() {
        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.d("KakaoMapLocation", "위치 권한 없음")
            return
        }

        //마지막 위치 가져오기
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng.from(
                        location.latitude,
                        location.longitude
                    )

                    //상태 업데이트
                    currentLocation = latLng

                    //지도 중심 이동
                    kakaoMap?.moveCamera(
                        CameraUpdateFactory.newCenterPosition(latLng)
                    )

                    kakaoMap?.let { map ->
                        addMyLocationMarker(
                            map = map,
                            position = latLng
                        )
                    }

                    Log.d(
                        "KakaoMapLocation",
                        "현재 위치: ${location.latitude}, ${location.longitude}"
                    )
                } else {
                    Log.d("KakaoMapLocation", "위치 null")
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
    LaunchedEffect(Unit) {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (
            finePermission == PackageManager.PERMISSION_GRANTED ||
            coarsePermission == PackageManager.PERMISSION_GRANTED
        ) {
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

    //카카오 맵뷰 생성
    val mapView = remember {
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
                        ready = true
                        kakaoMap = map

                        val position = currentLocation ?: defaultLocation

                        map.moveCamera( //초기 위치로
                            CameraUpdateFactory.newCenterPosition(position)
                        )

                        loadCurrentLocation() //이후 진짜 위치 다시
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
                    loadCurrentLocation() //다시 들어오면 위치 갱신
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

        // 0x0으로 찍히는 상황이면 강제 레이아웃 트리거
        if (sv.width == 0 || sv.height == 0) {
            sv.requestLayout()
            (sv.parent as? View)?.requestLayout()
        }

        sv.invalidate()

        Log.d(
            "KakaoMapFix",
            "SurfaceView found: size=${sv.width}x${sv.height}, lp=${sv.layoutParams?.width}x${sv.layoutParams?.height}"
        )
    }
}

//내 위치에 마커 찍기
private fun addMyLocationMarker(
    map: KakaoMap,
    position: LatLng
) {
    val labelManager = map.labelManager ?: return
    val layer = labelManager.layer ?: return

    layer.removeAll()

    val style = LabelStyle.from(R.drawable.ic_map_marker)

    val options = LabelOptions.from(position)
        .setStyles(style)

    layer.addLabel(options)
}
