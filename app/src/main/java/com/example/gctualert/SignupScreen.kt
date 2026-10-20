package com.example.gctualert

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var indexNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Dropdown expansion state
    var expanded by remember { mutableStateOf(false) }
    val courses = listOf("Computer Science", "Information Technology", " Computer  Engineering", "Business" )

    val scope = rememberCoroutineScope()

    fun registerUser() {
        errorMessage = null
        successMessage = null

        // 1. Check for empty fields
        if (name.isEmpty() || email.isEmpty() || course.isEmpty() || phoneNumber.isEmpty() || indexNumber.isEmpty() || password.isEmpty()) {
            errorMessage = "All fields are required. Please complete the form."
            return
        }
        // 2. Validate Email Format
        if (!email.contains("@") || !email.contains(".")) {
            errorMessage = "Please enter a valid email address."
            return
        }
        // 3. Password Length Check
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters long."
            return
        }
        // 4. Password Match Check
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match. Please verify."
            return
        }

        isLoading = true

        val newStudent = StudentRecord(
            name = name,
            email = email,
            course = course,
            department = course,
            indexNumber = indexNumber.toLongOrNull() ?: 0L,
            password = password
        )

        scope.launch {
            try {
                SupabaseManager.client.from("STUDENT").insert(newStudent)
                isLoading = false
                successMessage = "Account registered successfully!"

                // Clear input fields on success
                name = ""
                email = ""
                course = ""
                phoneNumber = ""
                indexNumber = ""
                password = ""
                confirmPassword = ""
            } catch (e: Exception) {
                isLoading = false
                val errStr = e.localizedMessage ?: ""
                errorMessage = if (errStr.contains("duplicate key") || errStr.contains("unique constraint")) {
                    "A student with this Email or Index Number already exists."
                } else {
                    "Failed to connect: $errStr"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF007AFF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.gctu_badge),
            contentDescription = null,
            modifier = Modifier.height(100.dp)
        )

        Text(
            text = "Register to gain access to the app",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        GCTUTextField(value = name, onValueChange = { name = it }, placeholder = "Full Name")
        GCTUTextField(value = email, onValueChange = { email = it }, placeholder = "Email Address", keyboardType = KeyboardType.Email)

        // MARK: - COURSE DROP DOWN MENU
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = course,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select Course / Program", color = Color.Gray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                courses.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            course = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        GCTUTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, placeholder = "Phone Number", keyboardType = KeyboardType.Phone)
        GCTUTextField(value = indexNumber, onValueChange = { indexNumber = it }, placeholder = "Index Number", keyboardType = KeyboardType.Number)
        GCTUSecureField(value = password, onValueChange = { password = it }, placeholder = "Password")
        GCTUSecureField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = "Confirm Password")

        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(error, color = Color.Red, fontSize = 12.sp)
            }
        }

        successMessage?.let { success ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Green.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Green.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(success, color = Color.Green, fontSize = 12.sp)
            }
        }

        Button(
            onClick = { registerUser() },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Blue, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Back to Login", color = Color.White)
        }
    }
}
