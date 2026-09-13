package com.example.appfire.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfire.respository.AuthRespository
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {
    fun getCurrentUser() = repo.getCurrentUser()
    private val repo= AuthRespository()
    fun login(email: String,password: String,onResult: (Boolean) -> Unit)
    {
        viewModelScope.launch {
            val result= repo.login(email,password)
            onResult(result.isSuccess)
        }
    }
    fun logout()=repo.logout()
    fun isLoggedIn()= repo.getCurrentUser()!=null
    fun register(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repo.register(email, password)
                if (result.isSuccess) {
                    // Đăng ký thành công, không tự động đăng nhập
                    onResult(true, null)
                } else {
                    onResult(false, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}