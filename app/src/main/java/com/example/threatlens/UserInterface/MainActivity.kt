package com.example.threatlens.UserInterface

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.threatlens.R
import com.example.threatlens.ui.LoginActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F24))
    ) {

        when (selectedTab) {
            0 -> DashboardScreen()
            2 -> ThreatLogsScreen()
        }

        // bottom nav untouched
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it }
            )
        }
    }
}

// ===============================================================
// DASHBOARD SCREEN (ONLY HEADER FIXED)
// ===============================================================
@Composable
fun DashboardScreen() {

    val context = LocalContext.current

    val gradient = Brush.verticalGradient(
        listOf(
            Color(0xFF3D5AFE),
            Color(0xFF536DFE),
            Color(0xFF00E5FF)
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // ------------------------------------------------------
        // CLEAN + COMPACT HEADER WITH LOGO
        // ------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)    // reduced from 260dp
                .background(gradient)
                .padding(16.dp)
        ) {

            // 3-dot menu
            var showMenu by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = Color(0xFF111A2F)
                ) {

                    DropdownMenuItem(
                        text = { Text("Profile", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        },
                        onClick = {
                            showMenu = false
                            context.startActivity(Intent(context, ProfileActivity::class.java))
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Logout", color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                        },
                        onClick = {
                            showMenu = false
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Center content (logo + title + subtitle)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.threatlens_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(82.dp)
                        .shadow(6.dp, CircleShape)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ThreatLens",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "An AR-Based Threat Detection App",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------------------------------------------
        // FEATURE CARDS
        // ------------------------------------------------------
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            NeonCard(
                title = "WiFi Scanner",
                subtitle = "Detect suspicious WiFi networks",
                icon = Icons.Default.Wifi,
                glowColor = Color(0xFF00E5FF),
                onClick = {}
            )

            NeonCard(
                title = "Bluetooth Scanner",
                subtitle = "Scan unknown Bluetooth devices",
                icon = Icons.Default.Bluetooth,
                glowColor = Color(0xFF76FF03),
                onClick = {}
            )

            NeonCard(
                title = "Magnetometer Scan",
                subtitle = "Detect magnetic fields of devices",
                icon = Icons.Default.Explore,
                glowColor = Color(0xFFFF4081),
                onClick = {}
            )
        }
    }
}

// The rest of your code remains EXACTLY same below ===========================

@Composable
fun ThreatLogsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F24)),
        contentAlignment = Alignment.Center
    ) {
        Text("Threat Logs will appear here.", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun NeonCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    glowColor: Color,
    onClick: () -> Unit
) {

    val glow by rememberInfiniteTransition().animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(Color(0xFF111A2F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(glowColor.copy(alpha = 0.2f), CircleShape)
                    .padding(16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = glowColor.copy(alpha = glow),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 14.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabChange: (Int) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        NavigationBar(
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
        ) {

            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, "Home") },
                selected = selectedTab == 0,
                onClick = { onTabChange(0) }
            )

            Spacer(modifier = Modifier.weight(1f))

            NavigationBarItem(
                icon = { Icon(Icons.Default.ListAlt, "Logs") },
                selected = selectedTab == 2,
                onClick = { onTabChange(2) }
            )
        }

        // Your original scanner button (untouched)
        Box(
            modifier = Modifier
                .offset(y = (-28).dp)
                .size(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFF00E5FF),
                            Color(0x8000E5FF),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .clickable {
                    context.startActivity(Intent(context, CameraViewActivity::class.java))
                },
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color(0xFF00E5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Scanner",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
