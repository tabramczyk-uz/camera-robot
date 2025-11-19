package com.example.camerarobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.camerarobot.ui.theme.CameraRobotTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ESP32ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraRobotTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Main(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Main(viewModel: ESP32ViewModel, modifier: Modifier = Modifier) {
    val esp32State by viewModel.esp32State.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { viewModel.getStatus() }
        ) {
            Text("Get status")
        }
        Text("Status: ${esp32State.status}")

        if (esp32State.status == ESP32Status.CONNECTED) {
            Button(
                onClick = { viewModel.toggleLed() }
            ) {
                Text("Toggle LED")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    CameraRobotTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Main(viewModel = ESP32ViewModel(), modifier = Modifier.padding(innerPadding))
        }
    }
}