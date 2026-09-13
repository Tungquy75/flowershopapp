package com.example.appfire.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appfire.model.Order
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.AdminOrderViewModel
import com.example.appfire.viewmodel.OrderWithUserInfo
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminOrderManagementScreen(
    onBackClick: () -> Unit,
    onOrderClick: (Order, String) -> Unit
) {
    val adminViewModel: AdminOrderViewModel = viewModel()
    val allOrders by adminViewModel.allOrders.observeAsState(emptyList())
    val isLoading by adminViewModel.isLoading.observeAsState(false)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
    val context = LocalContext.current

    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedOrderId by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf("") }
    var selectedNewStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        adminViewModel.loadAllOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KemSua)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .background(CamDao)
                .padding(16.dp)
        ) {
            val (backBtn, title) = createRefs()

            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
                    .constrainAs(backBtn) {
                        start.linkTo(parent.start)
                        centerVerticallyTo(parent)
                    }
            )

            Text(
                text = "Quản lý đơn hàng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.constrainAs(title) {
                    centerTo(parent)
                }
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            allOrders.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Không có đơn hàng nào", fontSize = 16.sp, color = Color.Gray)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allOrders) { orderWithInfo ->
                        AdminOrderCard(
                            orderWithInfo = orderWithInfo,
                            dateFormat = dateFormat,
                            onConfirmClick = { orderId, userId, currentStatus ->
                                when (currentStatus) {
                                    "pending" -> {
                                        selectedOrderId = orderId
                                        selectedUserId = userId
                                        selectedNewStatus = "confirmed"
                                        showConfirmDialog = true
                                    }
                                    "confirmed" -> {
                                        selectedOrderId = orderId
                                        selectedUserId = userId
                                        selectedNewStatus = "delivered"
                                        showConfirmDialog = true
                                    }
                                    else -> {
                                        android.widget.Toast.makeText(context, "Không thể cập nhật đơn hàng này", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onCancelClick = { orderId, userId ->
                                selectedOrderId = orderId
                                selectedUserId = userId
                                selectedNewStatus = "cancelled"
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận") },
            text = {
                val actionText = when (selectedNewStatus) {
                    "confirmed" -> "xác nhận"
                    "delivered" -> "xác nhận đã giao"
                    "cancelled" -> "hủy"
                    else -> "cập nhật"
                }
                Text("Bạn có chắc chắn muốn $actionText đơn hàng này?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        adminViewModel.updateOrderStatus(
                            selectedOrderId,
                            selectedUserId,
                            selectedNewStatus
                        ) { success, message ->
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                            if (success) {
                                showConfirmDialog = false
                            }
                        }
                    }
                ) {
                    Text("Xác nhận", color = CamDao)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun AdminOrderCard(
    orderWithInfo: OrderWithUserInfo,
    dateFormat: SimpleDateFormat,
    onConfirmClick: (String, String, String) -> Unit,
    onCancelClick: (String, String) -> Unit
) {
    val order = orderWithInfo.order
    val orderId = orderWithInfo.orderId
    val userId = orderWithInfo.userId

    val statusColor = when (order.status) {
        "pending" -> CamDao
        "confirmed" -> Color(0xFF2196F3)
        "delivered" -> Color(0xFF4CAF50)
        "cancelled" -> Color.Red
        else -> Color.Gray
    }

    val statusText = when (order.status) {
        "pending" -> "Chờ xác nhận"
        "confirmed" -> "Đã xác nhận"
        "delivered" -> "Đã giao hàng"
        "cancelled" -> "Đã hủy"
        else -> order.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mã đơn: ${order.orderId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .background(statusColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = if (order.status == "pending") Color.Black else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ngày: ${dateFormat.format(Date(order.orderDate))}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Text(
                text = "SĐT: ${order.phoneNumber}",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Địa chỉ: ${order.shippingAddress.take(30)}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Text(
                text = "Tổng tiền: ${formatVNH(order.totalAmount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (order.status != "delivered" && order.status != "cancelled") {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onConfirmClick(orderId, userId, order.status) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CamDao)
                    ) {
                        Text(
                            text = when (order.status) {
                                "pending" -> "Xác nhận đơn"
                                "confirmed" -> "Xác nhận đã giao"
                                else -> "Cập nhật"
                            },
                            color = Color.White
                        )
                    }

                    if (order.status != "delivered") {
                        OutlinedButton(
                            onClick = { onCancelClick(orderId, userId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Hủy đơn", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

fun formatVNH(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "${formatter.format(amount)} VNĐ"
}