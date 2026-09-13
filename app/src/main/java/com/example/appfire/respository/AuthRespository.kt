package com.example.appfire.respository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRespository {
    private val auth=  FirebaseAuth.getInstance()
    suspend fun login(email: String, password: String):Result<String>{
        return try{
            val result= auth.signInWithEmailAndPassword(email,password).await()
            Result.success(result.user?.uid?:
            throw Exception("User not found"))
        }
       catch (e: Exception){
           Result.failure(e)
       }
    }
    fun logout()=auth.signOut()
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    suspend fun register(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: throw Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}