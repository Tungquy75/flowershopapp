package com.example.appfire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appfire.model.Order
import com.google.firebase.database.*

class AdminOrderViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("Orders")

    private val _allOrders = MutableLiveData<List<OrderWithUserInfo>>()
    val allOrders: LiveData<List<OrderWithUserInfo>> = _allOrders

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var ordersListener: ValueEventListener? = null

    fun loadAllOrders() {
        _isLoading.value = true

        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ordersList = mutableListOf<OrderWithUserInfo>()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue

                    for (orderSnapshot in userSnapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            ordersList.add(
                                OrderWithUserInfo(
                                    order = order,
                                    userId = userId,
                                    orderId = orderSnapshot.key ?: ""
                                )
                            )
                        }
                    }
                }

                _allOrders.value = ordersList
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        }

        ordersRef.addValueEventListener(ordersListener!!)
    }

    fun updateOrderStatus(orderId: String, userId: String, newStatus: String, onResult: (Boolean, String) -> Unit) {
        ordersRef.child(userId).child(orderId).child("status").setValue(newStatus)
            .addOnSuccessListener {
                onResult(true, "Cập nhật trạng thái thành công!")
            }
            .addOnFailureListener { e ->
                onResult(false, "Lỗi: ${e.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.let {
            ordersRef.removeEventListener(it)
        }
    }
}

data class OrderWithUserInfo(
    val order: Order,
    val userId: String,
    val orderId: String
)