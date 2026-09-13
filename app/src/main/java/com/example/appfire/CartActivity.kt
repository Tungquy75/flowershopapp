package com.example.appfire

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.appfire.Helper.ChangeNumberItemsListener
import com.example.appfire.Helper.ManagmentCart
import com.example.appfire.model.Items
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.KemSua
import com.google.gson.Gson
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartScreen(
                managmentCart = ManagmentCart(this),
                onBackClick = { finish() }
            )
        }
    }
}

fun formatVND(price: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "${formatter.format(price)} VNĐ"
}

@Composable
fun CartScreen(
    managmentCart: ManagmentCart = ManagmentCart(LocalContext.current),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var cartItems by remember { mutableStateOf(managmentCart.getListCart()) }
    val tax = remember { mutableStateOf(0.0) }

    calculatorCart(managmentCart, tax)

    val listener = object : ChangeNumberItemsListener {
        override fun onChanged() {
            cartItems = managmentCart.getListCart()
            calculatorCart(managmentCart, tax)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KemSua)
            .padding(16.dp)
    ) {
        // Header
        ConstraintLayout(modifier = Modifier.systemBarsPadding()) {
            val (backBtn, cartTxt) = createRefs()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt) { centerTo(parent) },
                text = "Giỏ hàng của bạn",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )
        }

        // Nội dung giỏ hàng
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Giỏ hàng trống", fontSize = 18.sp, color = Color.Gray)
                    Text("Hãy thêm sản phẩm vào giỏ hàng", fontSize = 14.sp, color = Color.Gray)
                }
            }
        } else {
            // Danh sách sản phẩm
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    CartItem(
                        cartItems = cartItems,
                        item = item,
                        managmentCart = managmentCart,
                        onItemChange = listener
                    )
                }
            }

            // Tổng kết và nút thanh toán
            CartSummary(
                itemTotal = managmentCart.getTotalFee(),
                tax = tax.value,
                delivery = 15000.0,
                onCheckout = {
                    val intent = Intent(context, CheckoutActivity::class.java)
                    intent.putExtra("totalAmount", managmentCart.getTotalFee())
                    intent.putExtra("tax", tax.value)
                    intent.putExtra("delivery", 15000.0)
                    intent.putExtra("cartItemsJson", Gson().toJson(cartItems))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun CartSummary(
    itemTotal: Double,
    tax: Double,
    delivery: Double,
    onCheckout: () -> Unit
) {
    val total = itemTotal + tax + delivery
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Tổng đơn hàng",
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
            Text(formatVND(itemTotal), fontSize = 14.sp, color = Color.Black)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Thuế (2%):", fontSize = 14.sp, color = Color.Gray)
            Text(formatVND(tax), fontSize = 14.sp, color = Color.Black)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Phí vận chuyển:", fontSize = 14.sp, color = Color.Gray)
            Text(formatVND(delivery), fontSize = 14.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Divider()

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
                text = formatVND(total),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CamDao)
        ) {
            Text(
                text = "Tiến hành thanh toán",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

fun calculatorCart(
    managmentCart: ManagmentCart,
    tax: MutableState<Double>
) {
    val percentTax = 0.02
    tax.value = ((managmentCart.getTotalFee() * percentTax) * 100).roundToInt() / 100.0
}

@Composable
fun CartItem(
    cartItems: ArrayList<Items>,
    item: Items,
    managmentCart: ManagmentCart,
    onItemChange: ChangeNumberItemsListener
) {
    //cái gap
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh sản phẩm
            Image(
                painter = rememberAsyncImagePainter(item.picUrl.firstOrNull()),
                contentDescription = item.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CamDao)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin sản phẩm
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2
                )

                // Hiển thị model nếu có
                if (item.model.isNotEmpty() && item.model.firstOrNull()?.isNotBlank() == true) {
                    Text(
                        text = "Phiên bản: ${item.model.firstOrNull()}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatVND(item.price),
                    fontSize = 12.sp,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatVND(item.numberInCart * item.price),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            // Nút tăng/giảm số lượng
            Row(
                modifier = Modifier
                    .width(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CamDao),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            managmentCart.minusItem(cartItems, cartItems.indexOf(item), onItemChange)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = item.numberInCart.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            managmentCart.plusItem(cartItems, cartItems.indexOf(item), onItemChange)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}