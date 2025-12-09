package com.example.threatlens.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class ArHiddenCameraActivity : ComponentActivity() {

    private val CAMERA_REQUEST = 2026

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check Permission
        if (!hasCameraPermission()) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
            return
        }

        setContent {
            ArHiddenCameraScreen()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    @Deprecated("Deprecated in Android 13+")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                recreate()
            } else {
                Toast.makeText(this, "Camera permission required.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

@Composable
fun ArHiddenCameraScreen() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var warningText by remember { mutableStateOf("Scanning for reflections…") }
    var detected by remember { mutableStateOf(false) }
    var dotX by remember { mutableStateOf(-1f) }
    var dotY by remember { mutableStateOf(-1f) }

    val cameraProviderFuture =
        remember { ProcessCameraProvider.getInstance(context) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---------------- CAMERA PREVIEW ----------------
        AndroidView(
            factory = { ctx ->

                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                cameraProviderFuture.addListener({

                    val provider = cameraProviderFuture.get()

                    // PREVIEW PIPELINE
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // ANALYSIS PIPELINE
                    val analysis = ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val executor = Executors.newSingleThreadExecutor()

                    analysis.setAnalyzer(executor) { image ->

                        try {
                            val plane = image.planes.firstOrNull() ?: return@setAnalyzer
                            val buffer = plane.buffer
                            buffer.rewind()

                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)

                            // Find the brightest pixel → possible lens reflection
                            var maxBrightness = 0
                            var maxIndex = 0

                            for (i in bytes.indices step 10) { // Faster
                                val value = bytes[i].toInt() and 0xFF
                                if (value > maxBrightness) {
                                    maxBrightness = value
                                    maxIndex = i
                                }
                            }

                            detected = maxBrightness > 200

                            if (detected) {
                                warningText = "⚠ Possible Hidden Camera Detected!"

                                // Convert index → screen coords
                                dotX = (maxIndex % 640).toFloat()
                                dotY = (maxIndex / 640).toFloat()

                            } else {
                                warningText = "Scanning…"
                                dotX = -1f
                                dotY = -1f
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            image.close()
                        }
                    }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // ---------------- WARNING TEXT ----------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .background(Color(0x66000000))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = warningText,
                color = if (detected) Color.Red else Color.White,
                fontSize = 20.sp
            )
        }

        // ---------------- RED HIGHLIGHT CIRCLE ----------------
        if (detected && dotX > 0 && dotY > 0) {
            Canvas(modifier = Modifier.fillMaxSize()) {

                // Convert 640x480 coordinates → screen space
                val sx = dotX * (size.width / 640f)
                val sy = dotY * (size.height / 480f)

                drawCircle(
                    color = Color.Red,
                    radius = 50f,
                    center = androidx.compose.ui.geometry.Offset(sx, sy),
                    style = Stroke(width = 6f)
                )
            }
        }
    }
}
