package com.example.threatlens.UserInterface

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import java.util.concurrent.Executors

class CameraViewActivity : ComponentActivity() {

    private val CAMERA_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permission
        if (!hasCameraPermission()) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
            return
        }

        setComposeUI()
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun setComposeUI() {

        setContent {

            var detectionText by remember { mutableStateOf("Scanning...") }
            var isDetected by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxSize()) {

                androidCameraPreview(
                    onBrightnessCheck = { brightness ->
                        if (brightness > 180) {
                            detectionText = "⚠ Hidden Camera Reflection Detected!"
                            isDetected = true
                        } else {
                            detectionText = "Scanning..."
                            isDetected = false
                        }
                    }
                )

                // Overlay Text
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 50.dp)
                        .background(Color(0x88000000), MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    Text(
                        text = detectionText,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    @Composable
    fun androidCameraPreview(onBrightnessCheck: (Double) -> Unit) {

        val context = this
        val lifecycleOwner = this

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val previewView = PreviewView(ctx)
                val executor = Executors.newSingleThreadExecutor()

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .apply { setSurfaceProvider(previewView.surfaceProvider) }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(executor) { image ->

                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)

                        val avgBrightness = bytes.map { it.toInt() and 0xFF }.average()
                        onBrightnessCheck(avgBrightness)

                        image.close()
                    }

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        analysis
                    )

                }, ContextCompat.getMainExecutor(context))

                previewView
            }
        )
    }
}
