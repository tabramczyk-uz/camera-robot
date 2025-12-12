package com.example.camerarobot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camerarobot.Global.ESP32_BASE_URL
import com.example.camerarobot.Global.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var motorAJob: Job? = null
    private var motorBJob: Job? = null

    private companion object {
        const val HTTP_TIMEOUT = 5000
        const val MOTOR_DEBOUNCE_DELAY = 100L // milliseconds
    }

    fun getStatus() {
        _esp32State.value = _esp32State.value.copy(status = ESP32Status.CONNECTING)

        sendRequest("/status") { response ->
            _esp32State.value = _esp32State.value.copy(status = ESP32Status.CONNECTED)
            Log.v(TAG, "Status response: $response")
        }
    }

    fun toggleLed() {
        val newState = !_esp32State.value.isLedEnabled
        _esp32State.value = _esp32State.value.copy(isLedEnabled = newState)
        val brightness = if (newState) 1 else 0

        sendRequest(
            url = "/control?var=led_intensity&val=$brightness",
            logMessage = "LED brightness set to: $brightness"
        )
    }

    fun setMotorA(forward: Boolean, speed: Int) {
        motorAJob?.cancel()
        motorAJob = viewModelScope.launch {
            delay(MOTOR_DEBOUNCE_DELAY)
            setMotor("motor_a", forward, speed)
        }
    }

    fun setMotorB(forward: Boolean, speed: Int) {
        motorBJob?.cancel()
        motorBJob = viewModelScope.launch {
            delay(MOTOR_DEBOUNCE_DELAY)
            setMotor("motor_b", forward, speed)
        }
    }

    private fun setMotor(motorVar: String, forward: Boolean, speed: Int) {
        val speedBits = speed.coerceIn(0, 0x7F) // bits 0-6 (0..127)
        val forwardBit = if (forward) 0 else (1 shl 7) // bit 7
        val packedValue = forwardBit or speedBits

        sendRequest(
            url = "/control?var=$motorVar&val=$packedValue",
            logMessage = "${motorVar.uppercase()} set to: forward=$forward speed=$speed packed=$packedValue"
        )
    }

    private fun sendRequest(
        url: String,
        logMessage: String = "",
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fullUrl = URL("$ESP32_BASE_URL$url")
                val connection = fullUrl.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = HTTP_TIMEOUT
                    readTimeout = HTTP_TIMEOUT
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    if (logMessage.isNotEmpty()) {
                        Log.i(TAG, logMessage)
                    }
                    onSuccess(response)
                } else {
                    Log.e(TAG, "HTTP Error: $responseCode for $url")
                    _esp32State.value = _esp32State.value.copy(status = ESP32Status.ERROR)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Request failed: ${e.message}")
                _esp32State.value = _esp32State.value.copy(status = ESP32Status.ERROR)
            }
        }
    }
}