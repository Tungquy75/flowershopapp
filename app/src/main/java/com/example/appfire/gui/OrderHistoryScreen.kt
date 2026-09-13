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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appfire.model.Order
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatVN(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "${formatter.format(price)} VNĐ"
}

@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit,
    onOrderClick: (Order) -> Unit
) {
    val orderViewModel: OrderViewModel = viewModel()
    val orders by orderViewModel.orders.observeAsState(initial = emptyList())
    val isLoading by orderViewModel.isLoading.observeAsState(initial = false)

    LaunchedEffect(Unit) {
        orderViewModel.loadOrderHistory()
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
                text = "Lịch sử đơn hàng",
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
            orders.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chưa có đơn hàng nào", fontSize = 16.sp, color = Color.Gray)
                        Text("Hãy mua sắm ngay!", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        OrderItemCard(order = order, onOrderClick = { onOrderClick(order) })
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: Order, onOrderClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
    val statusColor = when (order.status) {
        "pending" -> Color.Yellow
        "confirmed" -> Color.Blue
        "shipping" -> Color.Magenta
        "delivered" -> Color.Green
        "cancelled" -> Color.Red
        else -> Color.Gray
    }
    val statusText = when (order.status) {
        "pending" -> "Chờ xác nhận"
        "confirmed" -> "Đã xác nhận"
        "shipping" -> "Đang giao hàng"
        "delivered" -> "Đã giao hàng"
        "cancelled" -> "Đã hủy"
        else -> order.status
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOrderClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mã đơn: ${order.orderId}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black)
                Box(modifier = Modifier
                    .background(statusColor,
                        shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(statusText, fontSize = 12.sp,
                        color = if (order.status == "pending") Color.Black else Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ngày đặt: ${dateFormat.format(Date(order.orderDate))}",
                fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            order.items.take(2).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = item.picUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            maxLines = 1
                        )
                        // Hiển thị model nếu có
                        if (item.model.isNotEmpty()) {
                            Text(
                                text = "Model: ${item.model}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Text(
                        text = "x${item.quantity}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            if (order.items.size > 2) Text("... và ${order.items.size - 2} sản phẩm khác",
                fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Địa chỉ: ${order.shippingAddress.take(20)}...",
                        fontSize = 12.sp, color = Color.Gray)
                    Text("SDT: ${order.phoneNumber}", fontSize = 12.sp, color = Color.Gray)
                }
                Text(formatVN(order.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            }
        }
    }
}