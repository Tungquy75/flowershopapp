package com.example.appfire.gui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.CreamBeige
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.AuthViewModel



@Composable
fun LoginScreen(navController: NavController,
                viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }  // false = ẩn mật khẩu (mặc định)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(30.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = KemSua,
                unfocusedContainerColor = KemSua,
                focusedIndicatorColor = CamDao,
                unfocusedIndicatorColor = CreamBeige
            )
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nhập mật khẩu") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = KemSua,
                unfocusedContainerColor = KemSua,
                focusedIndicatorColor = CamDao,
                unfocusedIndicatorColor = CreamBeige
            ),
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isVisible,
                onCheckedChange = { isVisible = it },
                enabled = password.isNotEmpty(),
                colors = CheckboxDefaults.colors(
                    checkedColor = CamDao,
                    uncheckedColor = Color.Gray
                )
            )
            Text(text = "Hiển thị mật khẩu")
        }

        Row {
            Button(
                onClick = {
                    viewModel.login(email, password) { success ->
                        if (success) {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(navController.context, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamDao,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Login")
            }

            Button(
                onClick = {
                    navController.navigate("register")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamDao,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Register")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = message)
    }
}