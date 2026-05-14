package com.capstone.ggud.data

import android.util.Log
import com.capstone.ggud.network.dto.LocationSendRequest
import com.capstone.ggud.network.dto.LocationSocketResponse
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader

class PromiseLocationSocket(
    private val token: String,
    private val promiseId: Long,
    private val onLocationReceived: (LocationSocketResponse) -> Unit
) {
    private val gson = Gson()
    private val disposables = CompositeDisposable()

    private var isConnected = false

    private val stompClient: StompClient = Stomp.over(
        Stomp.ConnectionProvider.OKHTTP,
        "ws://3.37.196.242/ws/websocket"
    )

    private fun subscribeLocation() {
        disposables.add(
            stompClient.topic("/topic/promises/$promiseId/locations")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ message ->
                    Log.d("PromiseSocket", "received location: ${message.payload}")

                    val location = gson.fromJson(
                        message.payload,
                        LocationSocketResponse::class.java
                    )

                    onLocationReceived(location)
                }, { error ->
                    Log.e("PromiseSocket", "subscribe error", error)
                })
        )
    }

    fun connect() {
        val headers = listOf(
            StompHeader("Authorization", "Bearer $token")
        )

        disposables.add(
            stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    Log.d("PromiseSocket", "socket event: ${event.type}")

                    if (event.type == ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED) {
                        isConnected = true
                        subscribeLocation()
                    }

                    if (event.type == ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED) {
                        isConnected = false
                    }

                    if (event.type == ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR) {
                        isConnected = false
                        Log.e("PromiseSocket", "socket error message: ${event.message}")
                        Log.e("PromiseSocket", "socket error exception", event.exception)
                    }
                }
        )

        stompClient.connect(headers)
    }

    fun sendLocation(
        latitude: Double,
        longitude: Double
    ) {
        if (!isConnected) {
            Log.d("PromiseSocket", "skip send location: socket not connected")
            return
        }

        val body = gson.toJson(
            LocationSendRequest(
                latitude = latitude,
                longitude = longitude
            )
        )

        disposables.add(
            stompClient.send(
                "/app/promises/$promiseId/location",
                body
            )
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Log.d("PromiseSocket", "location sent: $body")
                }, { error ->
                    Log.e("PromiseSocket", "send error", error)
                })
        )
    }

    fun disconnect() {
        disposables.clear()
        stompClient.disconnect()
    }
}