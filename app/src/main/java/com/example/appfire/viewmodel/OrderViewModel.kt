package com.example.appfire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfire.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _orderResult = MutableLiveData<Result<String>>()
    val orderResult: LiveData<Result<String>> = _orderResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private var _selectedOrder: Order? = null
    val selectedOrder: Order?
        get() = _selectedOrder

    fun setSelectedOrder(order: Order) {
        _selectedOrder = order
    }

    fun clearSelectedOrder() {
        _selectedOrder = null
    }

    private suspend fun generateOrderId(userId: String): String {
        val snapshot = database.child("Orders").child(userId).get().await()
        val orderCount = snapshot.childrenCount.toInt()
        val nextNumber = orderCount + 1
        return String.format("DH%03d", nextNumber)
    }

    fun placeOrder(order: Order, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: ""
                if (userId.isEmpty()) {
                    _orderResult.value = Result.failure(Exception("Vui lòng đăng nhập"))
                    _isLoading.value = false
                    return@launch
                }

                val orderId = generateOrderId(userId)
                val newOrder = order.copy(
                    orderId = orderId,
                    userId = userId,
                    orderDate = System.currentTimeMillis(),
                    status = "pending"
                )

                database.child("Orders").child(userId).child(orderId).setValue(newOrder).await()

                database.child("Cart").child(userId).removeValue().await()

                _orderResult.value = Result.success(orderId)
                loadOrderHistory()
                onSuccess()
            } catch (e: Exception) {
                _orderResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOrderHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: ""
                if (userId.isEmpty()) {
                    _orders.value = emptyList()
                    return@launch
                }

                val snapshot = database.child("Orders").child(userId).get().await()
                val ordersList = mutableListOf<Order>()
                for (child in snapshot.children) {
                    val order = child.getValue(Order::class.java)
                    order?.let { ordersList.add(it) }
                }
                _orders.value = ordersList.sortedByDescending { it.orderDate }
            } catch (e: Exception) {
                _orders.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelOrder(orderId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: ""
                if (userId.isEmpty()) {
                    onResult(false)
                    return@launch
                }
                database.child("Orders").child(userId).child(orderId).child("status").setValue("cancelled").await()
                loadOrderHistory()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}