package com.capstone.ggud

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("APP_INIT", "MyApplication.onCreate called ✅")

        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
        KakaoMapSdk.init(this, getString(R.string.kakao_native_app_key))
        Log.d("APP_INIT", "KakaoMapSdk.init called ✅")
    }
}
