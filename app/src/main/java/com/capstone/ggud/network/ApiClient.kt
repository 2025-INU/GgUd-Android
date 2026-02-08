package com.capstone.ggud.network

import android.content.Context
import com.capstone.ggud.data.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://3.37.196.242:80/"

    @Volatile
    private var retrofit: Retrofit? = null
    @Volatile
    private var authApi: AuthApi? = null

    @Volatile
    private var promiseApi: PromiseApi? = null

    fun getAuthApi(context: Context): AuthApi {
        return authApi ?: synchronized(this) {
            authApi ?: buildRetrofit(context).create(AuthApi::class.java).also {
                authApi = it
            }
        }
    }

    fun getPromiseApi(context: Context): PromiseApi {
        return promiseApi ?: synchronized(this) {
            promiseApi ?: buildRetrofit(context).create(PromiseApi::class.java).also {
                promiseApi = it
            }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: run {
                val tokenStore = TokenStore(context.applicationContext)

                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(AuthInterceptor(tokenStore))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build()

                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .also { retrofit = it }
            }
        }
    }

    fun clear() {
        authApi = null
        promiseApi = null
        retrofit = null
    }
}