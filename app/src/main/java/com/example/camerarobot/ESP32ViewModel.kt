package com.example.camerarobot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camerarobot.Global.ESP32_BASE_URL
import com.example.camerarobot.Global.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ESP32ViewModel : ViewModel() {
    private val _esp32State = MutableStateFlow(ESP32State())
    val esp32State: StateFlow<ESP32State> = _esp32State.asStateFlow()

    fun getStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _esp32State.value = _esp32State.value.copy(status = ESP32Status.CONNECTING)

            val result = try {
                val url = URL("$ESP32_BASE_URL/status")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val newResponse = inputStream.bufferedReader().use { it.readText() }
                    Log.v(TAG, "Response: $newResponse")

                    ESP32Status.CONNECTED
                } else {
                    Log.e(TAG, "Error: $responseCode")
                    ESP32Status.ERROR
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error: ${e.message}")
                ESP32Status.ERROR
            }

            _esp32State.value = _esp32State.value.copy(status = result)
        }
    }

    fun toggleLed() {
        viewModelScope.launch(Dispatchers.IO) {
            _esp32State.value = _esp32State.value.copy(isLedEnabled = !_esp32State.value.isLedEnabled)
            val brightness = if (_esp32State.value.isLedEnabled) 1 else 0

            try {
                val url = URL("$ESP32_BASE_URL/control?var=led_intensity&val=$brightness")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "LED toggled successfully")
                } else {
                    Log.e(TAG, "Error: $responseCode")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }
}