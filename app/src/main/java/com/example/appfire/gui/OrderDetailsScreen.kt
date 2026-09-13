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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun OrderDetailScreen(
    order: Order,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val orderViewModel: OrderViewModel = viewModel()
    var isCancelling by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))

    // Màu sắc cơ bản
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KemSua)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
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
                text = "Chi tiết đơn hàng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.constrainAs(title) {
                    centerTo(parent)
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thông tin đơn hàng
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                            text = "Ngày đặt: ${dateFormat.format(Date(order.orderDate))}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Thông tin giao hàng
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Thông tin giao hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Địa chỉ: ${order.shippingAddress}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "Số điện thoại: ${order.phoneNumber}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Danh sách sản phẩm
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Sản phẩm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.picUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${formatVND(item.price)} x ${item.quantity}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = formatVND(item.price * item.quantity),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            // Tổng cộng
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                text = "Tạm tính:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = formatVND(order.totalAmount - 15000),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Phí vận chuyển:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = formatVND(15000.0),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tổng cộng:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = formatVND(order.totalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            // Nút hủy đơn (chỉ hiển thị khi đơn hàng đang chờ xác nhận)
            if (order.status == "pending") {
                item {
                    Button(
                        onClick = {
                            isCancelling = true
                            orderViewModel.cancelOrder(order.orderId) { success ->
                                isCancelling = false
                                if (success) {
                                    onBackClick()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        enabled = !isCancelling
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Hủy đơn hàng",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatVND(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "${formatter.format(price)} VNĐ"
}