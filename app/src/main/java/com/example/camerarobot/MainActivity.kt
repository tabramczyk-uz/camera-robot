package com.example.camerarobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var motorASpeed by remember { mutableIntStateOf(0) }
    var motorAForward by remember { mutableStateOf(true) }
    var motorBSpeed by remember { mutableIntStateOf(0) }
    var motorBForward by remember { mutableStateOf(true) }

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
                onClick = { viewModel.toggleLed() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Toggle LED")
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                Text("Motor A", modifier = Modifier.padding(bottom = 8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Checkbox(
                        checked = motorAForward,
                        onCheckedChange = {
                            motorAForward = it
                            viewModel.setMotorA(motorAForward, motorASpeed)
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Forward")
                }

                Text("Speed: $motorASpeed", modifier = Modifier.padding(top = 8.dp))
                Slider(
                    value = motorASpeed.toFloat(),
                    onValueChange = {
                        motorASpeed = it.toInt()
                        viewModel.setMotorA(motorAForward, motorASpeed)
                    },
                    valueRange = 0f..127f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(modifier = Modifier.padding(top = 24.dp)) {
                Text("Motor B", modifier = Modifier.padding(bottom = 8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Checkbox(
                        checked = motorBForward,
                        onCheckedChange = {
                            motorBForward = it
                            viewModel.setMotorB(motorBForward, motorBSpeed)
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Forward")
                }

                Text("Speed: $motorBSpeed", modifier = Modifier.padding(top = 8.dp))
                Slider(
                    value = motorBSpeed.toFloat(),
                    onValueChange = {
                        motorBSpeed = it.toInt()
                        viewModel.setMotorB(motorBForward, motorBSpeed)
                    },
                    valueRange = 0f..127f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    CameraRobotTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Main(
                viewModel = ESP32ViewModel(),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}