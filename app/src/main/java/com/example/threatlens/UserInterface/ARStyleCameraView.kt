package com.example.threatlens.UserInterface

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

class ArStyleCameraActivity : ComponentActivity() {

    private val CAMERA_REQUEST = 9010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasCameraPermission()) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
            return
        }

        setContent {
            ArStyleCameraScreen()
        }
    }

    private fun hasCameraPermission(): Boolean =
        checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    @Deprecated("Deprecated in Android 13+")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate()
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

/**
 * AR-Style Camera Compose screen.
 *
 * - Camera preview (CameraX PreviewView)
 * - Analyzer that finds clusters of bright pixels (lens-like reflections)
 * - AR overlay: pulsing red circle at detected lens (normalized coords)
 */
@Composable
fun ArStyleCameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // UI state (observed by Compose)
    var detected by remember { mutableStateOf(false) }
    var normX by remember { mutableStateOf(0.5f) }   // normalized [0..1]
    var normY by remember { mutableStateOf(0.5f) }   // normalized [0..1]
    var statusText by remember { mutableStateOf("Scanning for hidden lenses...") }

    // camera provider future (remember so it's not recreated)
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Camera Preview (PreviewView inside AndroidView)
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    // Configure preview
                    val preview = Preview.Builder()
                        .setTargetResolution(Size(1280, 720)) // higher res gives better localization
                        .build()
                        .also { p -> p.setSurfaceProvider(previewView.surfaceProvider) }

                    // ImageAnalysis for detection
                    val analysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(640, 480)) // work resolution for analysis
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val exec = Executors.newSingleThreadExecutor()

                    // Analyzer lambda: calls onDetection with normalized coordinates
                    analysis.setAnalyzer(exec, LensClusterAnalyzer { nx, ny, isDetected ->
                        // Post to Compose state safely
                        detected = isDetected
                        if (isDetected) {
                            normX = nx.coerceIn(0f, 1f)
                            normY = ny.coerceIn(0f, 1f)
                            statusText = "⚠ Hidden camera detected"
                        } else {
                            statusText = "Scanning for hidden lenses..."
                        }
                    })

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            analysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top status banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 12.dp, end = 12.dp)
                .background(Color(0x66000000))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = statusText, color = if (detected) Color.Red else Color.White, fontSize = 16.sp)
        }

        // AR-style pulsing red circle overlay at normalized coords
        if (detected) {
            val transition = rememberInfiniteTransition()
            val pulse by transition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = normX * size.width
                val cy = normY * size.height
                val baseRadius = min(size.width, size.height) * 0.06f
                drawCircle(
                    color = Color(0xAAFF0000),
                    radius = baseRadius * pulse,
                    center = Offset(cx, cy),
                    style = Stroke(width = 6f)
                )
                // small solid center dot
                drawCircle(
                    color = Color.Red,
                    radius = baseRadius * 0.35f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

/**
 * Analyzer that groups bright pixels into clusters and computes a centroid.
 *
 * It analyzes the Y (luma) plane of YUV_420_888 imageProxy.
 * Returns normalized coordinates (0..1) via callback and whether detection occurred.
 */
class LensClusterAnalyzer(
    private val onDetection: (normX: Float, normY: Float, detected: Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    // Tunable parameters
    private val brightnessThreshold = 170         // pixel brightness to count as bright
    private val minClusterCount = 70              // minimum number of bright pixels to consider a cluster
    private val skip = 3                          // skip pixels for speed (3 -> sample every 3rd pixel)

    override fun analyze(image: ImageProxy) {
        try {
            val plane = image.planes.firstOrNull() ?: run {
                onDetection(0.5f, 0.5f, false)
                image.close()
                return
            }
            val buffer: ByteBuffer = plane.buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val width = image.width.takeIf { it > 0 } ?: 640
            val height = image.height.takeIf { it > 0 } ?: 480
            val stride = width

            // cluster accumulation
            var sumX = 0f
            var sumY = 0f
            var count = 0

            // scan with step to keep CPU load low
            var y = 0
            while (y < height) {
                var x = 0
                while (x < width) {
                    val idx = y * stride + x
                    if (idx < bytes.size) {
                        val value = bytes[idx].toInt() and 0xFF
                        if (value >= brightnessThreshold) {
                            sumX += x
                            sumY += y
                            count++
                        }
                    }
                    x += skip
                }
                y += skip
            }

            if (count >= minClusterCount) {
                val cx = sumX / count
                val cy = sumY / count
                val normX = cx / width.toFloat()
                val normY = cy / height.toFloat()
                onDetection(normX, normY, true)
            } else {
                onDetection(0.5f, 0.5f, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onDetection(0.5f, 0.5f, false)
        } finally {
            image.close()
        }
    }
}
