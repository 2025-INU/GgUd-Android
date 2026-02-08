package com.capstone.ggud.ui.map

import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // start() 호출 여부 (resume/pause 가드에 사용)
    var startRequested by remember { mutableStateOf(false) }
    // onMapReady 도착 여부(원하시면 UI 분기 등에 사용)
    var ready by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = {
            // 혹시라도 compose update 타이밍에 내부 SurfaceView가 늦게 붙는 경우 대비
            mapView.post { ensureSurfaceMatchParent(mapView) }
        }
    )

    /**
     * ✅ MapView가 레이아웃된 뒤에 start()를 1번만 호출
     * + start 직후 내부 SurfaceView가 0x0이면 MATCH_PARENT로 강제
     */
    DisposableEffect(mapView) {
        val listener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (!startRequested && v.width > 0 && v.height > 0) {
                startRequested = true
                Log.d("KakaoMapFix", "onLayout: size=${v.width}x${v.height} -> start()")

                // start 직후 SurfaceView가 0x0으로 들어오는 케이스가 있어 강제 리사이징 반복
                ensureSurfaceMatchParent(mapView)

                startKakaoMap(
                    mapView = mapView,
                    onReady = {
                        ready = true
                    }
                )

                // start() 이후에도 SurfaceView가 늦게 붙는 경우가 많아서 몇 번 더 보정
                mapView.postDelayed({ ensureSurfaceMatchParent(mapView) }, 50)
                mapView.postDelayed({ ensureSurfaceMatchParent(mapView) }, 150)
                mapView.postDelayed({ ensureSurfaceMatchParent(mapView) }, 300)
                mapView.postDelayed({ ensureSurfaceMatchParent(mapView) }, 600)
            }
        }

        mapView.addOnLayoutChangeListener(listener)

        onDispose {
            mapView.removeOnLayoutChangeListener(listener)
        }
    }

    /**
     * ✅ Lifecycle: start()가 호출된 이후에만 resume/pause/finish
     * (ready가 아니라 startRequested로 가드하는 게 안전합니다)
     */
    DisposableEffect(lifecycleOwner, startRequested) {
        val observer = LifecycleEventObserver { _, event ->
            if (!startRequested) return@LifecycleEventObserver

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("KakaoMapFix", "ON_RESUME -> mapView.resume()")
                    runCatching { mapView.resume() }
                        .onFailure { Log.e("KakaoMapFix", "resume() failed: ${it.message}", it) }

                    // resume 때도 SurfaceView 보정
                    mapView.post { ensureSurfaceMatchParent(mapView) }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("KakaoMapFix", "ON_PAUSE -> mapView.pause()")
                    runCatching { mapView.pause() }
                        .onFailure { Log.e("KakaoMapFix", "pause() failed: ${it.message}", it) }
                }

                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("KakaoMapFix", "ON_DESTROY -> mapView.finish()")
                    runCatching { mapView.finish() }
                        .onFailure { Log.e("KakaoMapFix", "finish() failed: ${it.message}", it) }
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

private fun startKakaoMap(
    mapView: MapView,
    onReady: () -> Unit
) {
    runCatching {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    Log.d("KakaoMapFix", "onMapDestroy")
                }

                override fun onMapError(error: Exception) {
                    Log.e("KakaoMapFix", "onMapError: ${error.message}", error)
                }
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(map: KakaoMap) {
                    Log.d("KakaoMapFix", "✅ onMapReady: map loaded")
                    onReady()

                    // ready 이후 한 번 더 보정
                    mapView.post { ensureSurfaceMatchParent(mapView) }
                }

                override fun getPosition(): LatLng = LatLng.from(37.375, 126.632)
                override fun getZoomLevel(): Int = 15
            }
        )
    }.onFailure {
        Log.e("KakaoMapFix", "start() failed: ${it.message}", it)
    }
}

/**
 * ✅ MapView 내부에 붙는 KGLSurfaceView(SurfaceView)가 0x0으로 들어오는 경우가 있어
 * 발견 즉시 MATCH_PARENT로 강제하고 requestLayout/invalidate를 걸어줍니다.
 */
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
