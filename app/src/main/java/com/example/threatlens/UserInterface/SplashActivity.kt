// SplashActivity.kt
package com.example.threatlens.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.threatlens.R
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThreatLensSplash {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun ThreatLensSplash(onFinish: () -> Unit) {
    // Navigate after delay
    LaunchedEffect(Unit) {
        delay(2300)
        onFinish()
    }

    // Animation for logo scale and glow
    val transition = rememberInfiniteTransition()
    val logoScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1600, easing = LinearEasing),
            RepeatMode.Reverse
        )
    )

    // Theme colors
    val darkBlue = Color(0xFF0A0F24)
    val deepNavy = Color(0xFF121B3D)
    val primaryBlue = Color(0xFF3D5AFE)
    val neonCyan = Color(0xFF00E5FF)
    val neonGreen = Color(0xFF76FF03)

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(darkBlue, deepNavy, primaryBlue)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Logo with glow — adjusted size
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp) // reduced from 220
            ) {
                Box(
                    modifier = Modifier
                        .size(155.dp) // reduced from 190
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    neonCyan.copy(alpha = glowAlpha * 0.6f),
                                    neonGreen.copy(alpha = glowAlpha * 0.25f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.threatlens_logo),
                    contentDescription = "ThreatLens Logo",
                    modifier = Modifier
                        .size(115.dp) // reduced from 130
                        .scale(logoScale)
                        .shadow(8.dp, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // reduced from 18.dp

            // App name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Threat",
                    color = Color.White,
                    fontSize = 34.sp,  // slightly reduced
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Lens",
                    color = neonCyan,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "An AR Based Threat Detection App",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(24.dp)) // reduced from 30

            CircularProgressIndicator(
                color = neonCyan,
                strokeWidth = 3.5.dp,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}
