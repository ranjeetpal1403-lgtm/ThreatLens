package com.example.threatlens.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginThemeSplash(
                onFinish = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun LoginThemeSplash(onFinish: () -> Unit) {

    // Auto navigate after 2.2 sec
    LaunchedEffect(true) {
        delay(2200)
        onFinish()
    }

    // Glow animation for title
    val glowAnim = rememberInfiniteTransition().animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1500),
            RepeatMode.Reverse
        )
    )

    // SAME GRADIENT AS LOGIN PAGE
    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFF1A237E),
            Color(0xFF3949AB),
            Color(0xFF5C6BC0)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "ThreatLens",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = glowAnim.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "An AR Based Detection App",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(55.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }
    }
}
