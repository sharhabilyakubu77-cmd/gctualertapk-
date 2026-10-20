package com.example.gctualert

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun updatePassword() {
        errorMessage = null
        successMessage = null

        if (newPassword != confirmPassword) {
            errorMessage = "Passwords do not match."
            return
        }
        if (newPassword.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }

        isLoading = true
        val trimmedEmail = email.trim().lowercase()

        scope.launch {
            try {
                SupabaseManager.client.from("STUDENT").update(
                    mapOf("Password" to newPassword)
                ) {
                    filter {
                        or {
                            eq("Email", trimmedEmail)
                            val numericIndex = trimmedEmail.toLongOrNull()
                            if (numericIndex != null) {
                                eq("Index Number", numericIndex)
                            }
                        }
                    }
                }
                isLoading = false
                successMessage = "Password successfully reset!"
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Failed to update password: ${e.localizedMessage}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF007AFF))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.gctu_badge),
            contentDescription = null,
            modifier = Modifier.height(90.dp).padding(top = 20.dp)
        )

        Text("Password Recovery", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Text(
            "Enter your student email address and new password below to update your account.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        GCTUTextField(value = email, onValueChange = { email = it }, placeholder = "School Email Address", keyboardType = KeyboardType.Email)
        GCTUSecureField(value = newPassword, onValueChange = { newPassword = it }, placeholder = "New Password")
        GCTUSecureField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = "Confirm New Password")

        Button(
            onClick = { updatePassword() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("Reset Password", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        errorMessage?.let { Text(it, color = Color.Yellow, fontSize = 12.sp) }
        successMessage?.let { Text(it, color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Login", color = Color.White)
        }
    }
}


