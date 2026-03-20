package com.capstone.ggud.network.kakao

import android.content.Context
import com.capstone.ggud.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object KakaoApiClient {

    private const val BASE_URL = "https://dapi.kakao.com/"

    @Volatile
    private var api: KakaoLocalApi? = null

    fun getApi(context: Context): KakaoLocalApi {
        return api ?: synchronized(this) {
            api ?: buildRetrofit(context).create(KakaoLocalApi::class.java).also {
                api = it
            }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val kakaoKey = context.getString(R.string.kakao_rest_api_key)

        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "KakaoAK $kakaoKey")
                .build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}