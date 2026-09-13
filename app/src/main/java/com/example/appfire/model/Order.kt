package com.example.appfire.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "pending",
    val orderDate: Long = System.currentTimeMillis(),
    val shippingAddress: String = "",
    val phoneNumber: String = ""
) : Parcelable

@Parcelize
data class OrderItem(
    val productId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val picUrl: String = "",
    val model: String = ""
) : Parcelable