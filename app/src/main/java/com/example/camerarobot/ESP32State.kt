package com.example.camerarobot

data class ESP32State(
    val status: ESP32Status = ESP32Status.DISCONNECTED,
    val isLedEnabled: Boolean = false
)
