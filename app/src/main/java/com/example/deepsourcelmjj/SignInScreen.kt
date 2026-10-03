package com.example.deepsourcelmjj

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onLoginSuccess: (AppUser) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09121D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ingresa con tu cuenta corporativa",
                color = Color(0xFF8E9BAE),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    loginError = null
                },
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF8E9BAE)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                isError = loginError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1DB98A),
                    unfocusedBorderColor = Color(0xFF1E2D3E),
                    focusedContainerColor = Color(0xFF131E2C),
                    unfocusedContainerColor = Color(0xFF131E2C),
                    focusedLabelColor = Color(0xFF1DB98A),
                    unfocusedLabelColor = Color(0xFF8E9BAE),
                    errorBorderColor = Color(0xFFFF5252),
                    errorLabelColor = Color(0xFFFF5252)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    loginError = null
                },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF8E9BAE)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                isError = loginError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1DB98A),
                    unfocusedBorderColor = Color(0xFF1E2D3E),
                    focusedContainerColor = Color(0xFF131E2C),
                    unfocusedContainerColor = Color(0xFF131E2C),
                    focusedLabelColor = Color(0xFF1DB98A),
                    unfocusedLabelColor = Color(0xFF8E9BAE),
                    errorBorderColor = Color(0xFFFF5252),
                    errorLabelColor = Color(0xFFFF5252)
                )
            )

            if (loginError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = loginError ?: "",
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        loginError = "Ingresa tu correo y contraseña"
                    } else {
                        val authenticatedUser = UserRepository.authenticate(email, password)

                        if (authenticatedUser != null) {
                            loginError = null
                            onLoginSuccess(authenticatedUser)
                        } else {
                            loginError = "Correo o contraseña incorrectos"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB98A))
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
