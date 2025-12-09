package com.example.threatlens.UserInterface

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.threatlens.ui.LoginActivity

class ProfileActivity : FragmentActivity() {   // 🔥 Changed here
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoEditableProfile(
                onLogoutClick = {
                    performLogout()
                }
            )
        }
    }

    private fun performLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun DemoEditableProfile(onLogoutClick: () -> Unit) {

    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("johndoe@gmail.com") }
    var phone by remember { mutableStateOf("+91 9876543210") }

    var savedMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F24))
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Edit Profile",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 40.dp)
            )

            ProfileInputField("Full Name", name) { name = it }
            Spacer(modifier = Modifier.height(15.dp))

            ProfileInputField("Email Address", email) { email = it }
            Spacer(modifier = Modifier.height(15.dp))

            ProfileInputField("Phone Number", phone) { phone = it }
            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = { savedMessage = "Changes Saved Successfully!" },
                colors = ButtonDefaults.buttonColors(Color(0xFF3D5AFE)),
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (savedMessage.isNotEmpty()) {
                Text(
                    text = savedMessage,
                    color = Color(0xFF00E676),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onLogoutClick() },
                colors = ButtonDefaults.buttonColors(Color(0xFFFF1744)),
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Color.LightGray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF111A2F),
                unfocusedContainerColor = Color(0xFF111A2F),
                focusedIndicatorColor = Color(0xFF3D5AFE),
                unfocusedIndicatorColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}
