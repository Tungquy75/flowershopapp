package com.example.appfire

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.appfire.Helper.ManagmentCart
import com.example.appfire.Helper.TinyDB
import com.example.appfire.model.Order
import com.example.appfire.model.OrderItem
import com.example.appfire.model.Items
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.OrderViewModel
import com.google.gson.Gson
import java.text.NumberFormat
import java.util.Locale
import java.util.regex.Pattern

class CheckoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        val tax = intent.getDoubleExtra("tax", 0.0)
        val delivery = intent.getDoubleExtra("delivery", 15000.0)
        val cartItemsJson = intent.getStringExtra("cartItemsJson") ?: ""
        val isBuyNow = intent.getBooleanExtra("isBuyNow", false)
        val cartItems = Gson().fromJson(cartItemsJson, Array<Items>::class.java).toList()

        setContent {
            CheckoutScreen(
                totalAmount = totalAmount,
                tax = tax,
                delivery = delivery,
                cartItems = cartItems,
                isBuyNow = isBuyNow,
                onBackClick = { finish() }
            )
        }
    }
}

// Hàm kiểm tra số điện thoại Việt Nam
fun isValidVietnamesePhone(phone: String): Boolean {
    // Regex cho số điện thoại Việt Nam:
    // - Đầu số: 03, 05, 07, 08, 09 (2 số)
    // - Tổng độ dài: 10 số
    val phoneRegex = Pattern.compile("^(03|05|07|08|09|01[2|6|8|9])[0-9]{8}$")
    return phoneRegex.matcher(phone).matches()
}

@Composable
fun CheckoutScreen(
    totalAmount: Double,
    tax: Double,
    delivery: Double,
    cartItems: List<Items>,
    isBuyNow: Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val orderViewModel: OrderViewModel = viewModel()
    val managmentCart = remember { ManagmentCart(context) }

    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    // State hiển thị lỗi
    var addressError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }

    val total = totalAmount + tax + delivery

    // Hàm kiểm tra dữ liệu
    fun validateFields(): Boolean {
        var isValid = true

        // Kiểm tra địa chỉ
        if (address.isBlank()) {
            addressError = "Vui lòng nhập địa chỉ giao hàng"
            isValid = false
        } else if (address.length < 5) {
            addressError = "vui lòng nhập chi tiết hơn"
            isValid = false
        } else {
            addressError = ""
        }

        // Kiểm tra số điện thoại
        if (phone.isBlank()) {
            phoneError = "Vui lòng nhập số điện thoại"
            isValid = false
        } else if (!isValidVietnamesePhone(phone)) {
            phoneError = "Số điện thoại không hợp lệ (VD: 0912345678, 0987654321)"
            isValid = false
        } else {
            phoneError = ""
        }

        return isValid
    }

    // Xử lý khi đặt hàng thành công
    LaunchedEffect(Unit) {
        orderViewModel.orderResult.observeForever { result ->
            if (isProcessing) {
                result.onSuccess { orderId ->
                    Toast.makeText(context, "Đặt hàng thành công! Mã đơn: $orderId", Toast.LENGTH_LONG).show()

                    if (isBuyNow) {
                        val currentCart = managmentCart.getListCart()
                        val productTitle = cartItems.firstOrNull()?.title
                        if (productTitle != null) {
                            currentCart.removeAll { it.title == productTitle }
                            TinyDB(context).putListObject("CartList", currentCart)
                        }
                    } else {
                        TinyDB(context).putListObject("CartList", arrayListOf())
                    }

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }.onFailure { error ->
                    Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                isProcessing = false
            }
        }
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
                text = if (isBuyNow) "Mua ngay" else "Thanh toán",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.constrainAs(title) {
                    centerTo(parent)
                }
            )
        }

        // Nội dung
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Địa chỉ
                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                addressError = ""
                            },
                            label = { Text("Địa chỉ giao hàng *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            isError = addressError.isNotEmpty(),
                            supportingText = {
                                if (addressError.isNotEmpty()) {
                                    Text(addressError, color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Số điện thoại
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = ""
                            },
                            label = { Text("Số điện thoại *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            isError = phoneError.isNotEmpty(),
                            supportingText = {
                                if (phoneError.isNotEmpty()) {
                                    Text(phoneError, color = Color.Red, fontSize = 11.sp)
                                } else {
                                    Text("VD: 0912345678, 0987654321", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
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
                            text = "Đơn hàng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = item.picUrl.firstOrNull(),
                                        contentDescription = item.title,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.LightGray)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            maxLines = 1
                                        )
                                        item.model.firstOrNull()?.let { modelName ->
                                            Text(
                                                text = "Phiên bản: $modelName",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = "${formatVN(item.price)} x ${item.numberInCart}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Text(
                                    text = formatVN(item.price * item.numberInCart),
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
                        Text(
                            text = "Tổng cộng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tạm tính:", fontSize = 14.sp, color = Color.Gray)
                            Text(formatVN(totalAmount), fontSize = 14.sp, color = Color.Black)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Thuế (2%):", fontSize = 14.sp, color = Color.Gray)
                            Text(formatVN(tax), fontSize = 14.sp, color = Color.Black)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Phí vận chuyển:", fontSize = 14.sp, color = Color.Gray)
                            Text(formatVN(delivery), fontSize = 14.sp, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tổng thanh toán:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = formatVN(total),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

            // Nút xác nhận
            item {
                Button(
                    onClick = {
                        if (validateFields()) {
                            val orderItems = cartItems.map { item ->
                                OrderItem(
                                    productId = item.categoryId,
                                    title = item.title,
                                    price = item.price,
                                    quantity = item.numberInCart,
                                    picUrl = item.picUrl.firstOrNull() ?: "",
                                    model = item.model.firstOrNull() ?: ""
                                )
                            }
                            val order = Order(
                                items = orderItems,
                                totalAmount = total,
                                shippingAddress = address,
                                phoneNumber = phone
                            )
                            isProcessing = true
                            orderViewModel.placeOrder(order)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamDao),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Xác nhận thanh toán", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

fun formatVN(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "${formatter.format(price)} VNĐ"
}