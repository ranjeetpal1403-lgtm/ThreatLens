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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.concurrent.Executors

class CameraLensTFActivity : ComponentActivity() {

    private lateinit var objectDetector: ObjectDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasCameraPermission()) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            return
        }

        initTensorFlowModel()
        setComposeUI()
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun initTensorFlowModel() {
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(3)
            .setScoreThreshold(0.5f)
            .build()

        objectDetector = ObjectDetector.createFromFileAndOptions(
            this,
            "camera_lens_detector.tflite",
            options
        )
    }

    private fun setComposeUI() {
        setContent {

            var resultText by remember { mutableStateOf("Scanning for hidden cameras...") }

            Box(modifier = Modifier.fillMaxSize()) {

                TFPreviewView { bitmap ->

                    val imageProcessor = ImageProcessor.Builder()
                        .add(ResizeOp(320, 320, ResizeOp.ResizeMethod.BILINEAR))
                        .build()

                    val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
                    val results = objectDetector.detect(tensorImage)

                    resultText = if (results.isNotEmpty()) {
                        "⚠ Hidden Camera Lens Detected!"
                    } else {
                        "Scanning..."
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp)
                        .background(Color(0x66000000), MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    Text(text = resultText, color = Color.White, fontSize = 20.sp)
                }
            }
        }
    }

    @Composable
    fun TFPreviewView(onFrame: (android.graphics.Bitmap) -> Unit) {
        val lifecycleOwner = this
        val executor = Executors.newSingleThreadExecutor()

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val previewView = PreviewView(ctx)
                val providerFuture = ProcessCameraProvider.getInstance(ctx)

                providerFuture.addListener({

                    val cameraProvider = providerFuture.get()

                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(executor) { imageProxy ->

                        val bitmap = previewView.bitmap
                        if (bitmap != null) {
                            onFrame(bitmap)
                        }

                        imageProxy.close()
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )
    }
}
