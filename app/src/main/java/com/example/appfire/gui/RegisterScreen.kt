package com.example.appfire.gui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.navigation.NavController
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.CreamBeige
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }  // mặc định false (ẩn mật khẩu)
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Hàm kiểm tra định dạng email
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Email
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = KemSua,
                unfocusedContainerColor = KemSua,
                focusedIndicatorColor = CamDao,
                unfocusedIndicatorColor = CreamBeige
            ),
            isError = email.isNotEmpty() && !isValidEmail(email)
        )

        if (email.isNotEmpty() && !isValidEmail(email)) {
            Text(
                text = "Email không đúng định dạng",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mật khẩu
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

        // Xác nhận mật khẩu
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = KemSua,
                unfocusedContainerColor = KemSua,
                focusedIndicatorColor = CamDao,
                unfocusedIndicatorColor = CreamBeige
            ),

            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Checkbox hiển thị mật khẩu
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isVisible,
                onCheckedChange = { isVisible = it },
                enabled = password.isNotEmpty(),  //chỉ hoạt động khi có mật khẩu
                colors = CheckboxDefaults.colors(
                    checkedColor = CamDao,
                    uncheckedColor = Color.Gray
                )
            )
            Text(
                text = "Hiển thị mật khẩu",
                modifier = Modifier.clickable {
                    if (password.isNotEmpty()) isVisible = !isVisible
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            Button(
                onClick = {
                    when {
                        email.isBlank() -> {
                            Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                        }
                        !isValidEmail(email) -> {
                            Toast.makeText(context, "Email không đúng định dạng", Toast.LENGTH_SHORT).show()
                        }
                        password.isBlank() -> {
                            Toast.makeText(context, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                        confirmPassword.isBlank() -> {
                            Toast.makeText(context, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                        password != confirmPassword -> {
                            Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                        }
                        password.length < 6 -> {
                            Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            isLoading = true
                            viewModel.register(email, password) { success, errorMessage ->
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, errorMessage ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamDao,
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Register")
                }
            }

            Button(
                onClick = {
                    navController.navigate("login")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CamDao,
                    contentColor = Color.White
                )
            ) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = message, color = Color.Red)
    }
}