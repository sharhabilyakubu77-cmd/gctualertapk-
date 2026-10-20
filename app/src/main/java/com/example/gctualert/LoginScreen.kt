package com.example.gctualert

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loginErrorMsg by remember { mutableStateOf<String?>(null) }
    var biometricErrorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isFormValid = identifier.trim().isNotEmpty() && password.trim().isNotEmpty()

    fun authenticateWithBiometrics() {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onNavigateToHome("")
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    biometricErrorMsg = "Biometric authentication failed: $errString"
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using Face or Fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun loginUser() {
        loginErrorMsg = null
        isLoading = true
        val trimmedInput = identifier.trim()

        scope.launch {
            try {
                val response = SupabaseManager.client.from("STUDENT").select {
                    filter {
                        or {
                            eq("Email", trimmedInput)
                            val numericIndex = trimmedInput.toLongOrNull()
                            if (numericIndex != null) {
                                eq("Index Number", numericIndex)
                            }
                        }
                        eq("Password", password)
                    }
                }.decodeList<StudentRecord>()

                isLoading = false
                if (response.isNotEmpty()) {
                    onNavigateToHome(response.first().indexNumber.toString())
                } else {
                    loginErrorMsg = "Invalid Email/Index Number or Password."
                }
            } catch (e: Exception) {
                isLoading = false
                loginErrorMsg = "Login error: ${e.localizedMessage}"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF007AFF))
            .padding(horizontal = 40.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.gctu_badge),
                contentDescription = "GCTU Badge",
                modifier = Modifier.size(300.dp, 100.dp)
            )

            Text(
                text = "Welcome to GCTU Alert System",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            GCTUTextField(
                value = identifier,
                onValueChange = { identifier = it },
                placeholder = "Enter your index_no or school email"
            )

            GCTUSecureField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter your password"
            )

            Text(
                text = "Forgot password? Click here",
                fontSize = 12.sp,
                color = Color.White,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onNavigateToForgotPassword() }
            )

            Button(
                onClick = { loginUser() },
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056B3)),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            loginErrorMsg?.let {
                Text(text = it, color = Color.Yellow, fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            TextButton(onClick = { authenticateWithBiometrics() }) {
                Text("Sign in with Biometrics", color = Color.White, fontWeight = FontWeight.Bold)
            }

            biometricErrorMsg?.let {
                Text(text = it, color = Color.Yellow, fontSize = 12.sp)
            }

            Text(
                text = "If you don't have an account sign up",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Button(
                onClick = onNavigateToSignup,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Signup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}


