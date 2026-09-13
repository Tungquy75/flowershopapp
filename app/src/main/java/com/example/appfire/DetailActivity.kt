package com.example.appfire

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appfire.Helper.ManagmentCart
import com.example.appfire.R
import com.example.appfire.model.Items
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.CreamBeige
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.AuthViewModel
import com.example.appfire.viewmodel.HomeViewModel
import com.google.gson.Gson

class DetailActivity : ComponentActivity() {
    private lateinit var item: Items
    private lateinit var managmentCart: ManagmentCart
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        item = intent.getParcelableExtra("object") ?: Items()
        managmentCart = ManagmentCart(this)
        homeViewModel = androidx.lifecycle.ViewModelProvider(this).get(HomeViewModel::class.java)

        setContent {
            val authViewModel: AuthViewModel = viewModel()

            DetailScreen(
                item = item,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                onBackClick = { finish() },
                onAddToCartClick = { selectedModel ->
                    val existingItem = managmentCart.getListCart().find {
                        it.title == item.title && (it.model.firstOrNull() ?: "") == selectedModel
                    }
                    if (existingItem != null) {
                        existingItem.numberInCart += 1
                        managmentCart.insertItem(existingItem)
                    } else {
                        val newItem = item.copy()
                        newItem.numberInCart = 1
                        if (selectedModel.isNotEmpty()) {
                            newItem.model = arrayListOf(selectedModel)
                        }
                        managmentCart.insertItem(newItem)
                        Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                    }
                },
                onBuyNowClick = { selectedModel ->
                    val buyNowItem = item.copy()
                    buyNowItem.numberInCart = 1
                    if (selectedModel.isNotEmpty()) {
                        buyNowItem.model = arrayListOf(selectedModel)
                    }
                    val itemsList = listOf(buyNowItem)
                    val itemsJson = Gson().toJson(itemsList)

                    val intent = Intent(this, CheckoutActivity::class.java)
                    intent.putExtra("totalAmount", buyNowItem.price)
                    intent.putExtra("tax", 0.0)
                    intent.putExtra("delivery", 15000.0)
                    intent.putExtra("cartItemsJson", itemsJson)
                    intent.putExtra("isBuyNow", true)
                    startActivity(intent)
                },
                onCartClick = {
                    startActivity(Intent(this, CartActivity::class.java))
                },
                onItemUpdated = { updatedItem ->
                    item = updatedItem
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    recreate()
                },
                onItemDeleted = { deletedItem ->
                    Toast.makeText(this, "Đã xóa '${deletedItem.title}' thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        }
    }
}

@Composable
fun DetailScreen(
    item: Items,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    onBackClick: () -> Unit,
    onAddToCartClick: (String) -> Unit,
    onBuyNowClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onItemUpdated: (Items) -> Unit,
    onItemDeleted: (Items) -> Unit
) {
    val context = LocalContext.current
    val defaultImageUrl = item.picUrl.firstOrNull() ?: ""
    var selectedImageUrl by remember { mutableStateOf(defaultImageUrl) }
    var selectedModelIndex by remember { mutableStateOf(0) }

    // State cho Dialog cập nhật
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateTitle by remember { mutableStateOf(item.title) }
    var updatePrice by remember { mutableStateOf(item.price.toString()) }
    var updateDescription by remember { mutableStateOf(item.description) }
    val updatePhotosUris = remember { mutableStateListOf<Uri>() }
    val currentPhotos = remember { mutableStateListOf<String>().apply { addAll(item.picUrl) } }
    var isUpdating by remember { mutableStateOf(false) }

    // State kiểm tra lỗi
    var updateTitleError by remember { mutableStateOf("") }
    var updatePriceError by remember { mutableStateOf("") }
    var updateImageError by remember { mutableStateOf("") }

    // State cho Dialog xóa
    var showDeleteDialog by remember { mutableStateOf(false) }

    val selectedModel = if (item.model.isNotEmpty()) item.model[selectedModelIndex] else ""
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Launcher chọn ảnh
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        updatePhotosUris.clear()
        updatePhotosUris.addAll(uris)
        if (uris.isNotEmpty()) updateImageError = ""
    }

    // Hàm kiểm tra dữ liệu cập nhật
    fun validateUpdateFields(): Boolean {
        var isValid = true

        // Kiểm tra tên
        if (updateTitle.isBlank()) {
            updateTitleError = "Vui lòng nhập tên sản phẩm"
            isValid = false
        } else if (updateTitle.length < 3) {
            updateTitleError = "Tên sản phẩm phải có ít nhất 3 ký tự"
            isValid = false
        } else {
            updateTitleError = ""
        }

        // Kiểm tra giá
        if (updatePrice.isBlank()) {
            updatePriceError = "Vui lòng nhập giá sản phẩm"
            isValid = false
        } else {
            val priceValue = updatePrice.toDoubleOrNull()
            if (priceValue == null) {
                updatePriceError = "Giá phải là số"
                isValid = false
            } else if (priceValue <= 0) {
                updatePriceError = "Giá phải lớn hơn 0"
                isValid = false
            } else {
                updatePriceError = ""
            }
        }

        // Kiểm tra ảnh (ít nhất 1 ảnh)
        if (currentPhotos.isEmpty() && updatePhotosUris.isEmpty()) {
            updateImageError = "Vui lòng có ít nhất 1 ảnh sản phẩm"
            isValid = false
        } else {
            updateImageError = ""
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KemSua)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarHeight + 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
            )

            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onCartClick() }
            )
        }

        // Nội dung cuộn
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Ảnh sản phẩm chính
            if (selectedImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = selectedImageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(CreamBeige, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(CreamBeige, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Red)
                }
            }

            // Danh sách ảnh thumbnail
            if (item.picUrl.isNotEmpty() && item.picUrl.size > 1) {
                LazyRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(item.picUrl) { imageUrl ->
                        ImageThumbnail(
                            imageUrl = imageUrl,
                            isSelected = selectedImageUrl == imageUrl,
                            onClick = { selectedImageUrl = imageUrl }
                        )
                    }
                }
            }

            // Tên và giá sản phẩm
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = item.title.ifEmpty { "No Title" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${String.format("%,.0f", item.price)} VNĐ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            // Rating
            RatingBar(rating = item.rating)

            // Model selector
            if (item.model.isNotEmpty()) {
                ModelSelector(
                    models = item.model,
                    selectedModelIndex = selectedModelIndex,
                    onModelSelected = { selectedModelIndex = it }
                )
            }

            // Mô tả sản phẩm
            Text(
                text = item.description.ifEmpty { "No description available" },
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Nút admin cập nhật xóa
            if (authViewModel.getCurrentUser()?.email == "lutungquy@gmail.com") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showUpdateDialog = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CẬP NHẬT", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("XÓA HOA", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .background(Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onBuyNowClick(selectedModel) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamDao),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(50.dp)
                ) {
                    Text(text = "Mua Ngay", fontSize = 18.sp, color = Color.White)
                }

                IconButton(
                    onClick = { onAddToCartClick(selectedModel) },
                    modifier = Modifier
                        .background(CamDao, shape = RoundedCornerShape(10.dp))
                        .size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Thêm vào giỏ",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Dialog cập nhật sản phẩm
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = CamDao,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            containerColor = KemSua,
            title = {
                Text(
                    "Cập nhật sản phẩm",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(475.dp) //độ dài box
                        .verticalScroll(rememberScrollState())
                ) {
                    // ID sản phẩm (chỉ đọc)
                    OutlinedTextField(
                        value = item.numberInCart.toString(),
                        onValueChange = {},
                        label = { Text("ID sản phẩm") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tên sản phẩm
                    OutlinedTextField(
                        value = updateTitle,
                        onValueChange = {
                            updateTitle = it
                            updateTitleError = ""
                        },
                        label = { Text("Tên sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = updateTitleError.isNotEmpty(),
                        supportingText = {
                            if (updateTitleError.isNotEmpty()) {
                                Text(updateTitleError, color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Giá
                    OutlinedTextField(
                        value = updatePrice,
                        onValueChange = {
                            updatePrice = it
                            updatePriceError = ""
                        },
                        label = { Text("Giá sản phẩm") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = updatePriceError.isNotEmpty(),
                        supportingText = {
                            if (updatePriceError.isNotEmpty()) {
                                Text(updatePriceError, color = Color.Red, fontSize = 12.sp)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mô tả
                    OutlinedTextField(
                        value = updateDescription,
                        onValueChange = { updateDescription = it },
                        label = { Text("Mô tả") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Khu vực ảnh
                    Text(
                        "Ảnh sản phẩm",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    if (updateImageError.isNotEmpty()) {
                        Text(updateImageError, color = Color.Red, fontSize = 12.sp)
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ảnh cũ
                        items(currentPhotos) { url ->
                            Box {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Button(
                                    onClick = {
                                        currentPhotos.remove(url)
                                        if (currentPhotos.isEmpty() && updatePhotosUris.isEmpty()) {
                                            updateImageError = "Vui lòng có ít nhất 1 ảnh sản phẩm"
                                        } else {
                                            updateImageError = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("x", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        // Ảnh mới
                        items(updatePhotosUris) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Button(
                                    onClick = {
                                        updatePhotosUris.remove(uri)
                                        if (currentPhotos.isEmpty() && updatePhotosUris.isEmpty()) {
                                            updateImageError = "Vui lòng có ít nhất 1 ảnh sản phẩm"
                                        } else {
                                            updateImageError = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("x", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        // Nút thêm ảnh
                        item {
                            Button(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier.size(80.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CamDao),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+", fontSize = 24.sp, color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (validateUpdateFields()) {
                            isUpdating = true
                            val uploadedUrls = ArrayList<String>(currentPhotos)

                            if (updatePhotosUris.isEmpty()) {
                                saveUpdate(
                                    item = item,
                                    title = updateTitle,
                                    price = updatePrice,
                                    description = updateDescription,
                                    urls = uploadedUrls,
                                    viewModel = homeViewModel,
                                    onSuccess = { updatedItem ->
                                        isUpdating = false
                                        showUpdateDialog = false
                                        onItemUpdated(updatedItem)
                                    },
                                    onError = {
                                        isUpdating = false
                                        Toast.makeText(context, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                var uploadCount = 0
                                updatePhotosUris.forEach { uri ->
                                    homeViewModel.uploadImage(uri) { url ->
                                        if (url != null) {
                                            uploadedUrls.add(url)
                                        }
                                        uploadCount++
                                        if (uploadCount == updatePhotosUris.size) {
                                            saveUpdate(
                                                item = item,
                                                title = updateTitle,
                                                price = updatePrice,
                                                description = updateDescription,
                                                urls = uploadedUrls,
                                                viewModel = homeViewModel,
                                                onSuccess = { updatedItem ->
                                                    isUpdating = false
                                                    showUpdateDialog = false
                                                    onItemUpdated(updatedItem)
                                                },
                                                onError = {
                                                    isUpdating = false
                                                    Toast.makeText(context, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isUpdating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isUpdating) "ĐANG XỬ LÝ..." else "LƯU")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUpdateDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text("HỦY")
                }
            }
        )
    }

    // Dialog xóa sản phẩm
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = CamDao,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            containerColor = KemSua,
            title = {
                Text(
                    "Xác nhận xóa",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    "Bạn có chắc chắn muốn xóa sản phẩm '${item.title}' này không?",
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        homeViewModel.deleteItem(item.title) { success ->
                            if (success) {
                                showDeleteDialog = false
                                onItemDeleted(item)
                            } else {
                                Toast.makeText(context, "Xóa thất bại!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("XÓA NGAY")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text("HỦY")
                }
            }
        )
    }
}

// Hàm hỗ trợ lưu cập nhật
private fun saveUpdate(
    item: Items,
    title: String,
    price: String,
    description: String,
    urls: ArrayList<String>,
    viewModel: HomeViewModel,
    onSuccess: (Items) -> Unit,
    onError: () -> Unit
) {
    val updatedItem = item.copy(
        title = title,
        price = price.toDoubleOrNull() ?: 0.0,
        description = description,
        picUrl = urls
    )
    viewModel.uploadItem(updatedItem) { success ->
        if (success) {
            onSuccess(updatedItem)
        } else {
            onError()
        }
    }
}

@Composable
fun RatingBar(rating: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(
            text = "Rating:",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (index < rating.toInt()) Color.Yellow else Color.Gray
            )
        }

        Text(
            text = " $rating",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun ModelSelector(
    models: List<String>,
    selectedModelIndex: Int,
    onModelSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Select Model:",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(models) { index, model ->
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .then(
                            if (index == selectedModelIndex) {
                                Modifier.border(
                                    2.dp,
                                    colorResource(R.color.teal_200),
                                    RoundedCornerShape(10.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            if (index == selectedModelIndex) CamDao else CreamBeige,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onModelSelected(index) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = model,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = if (index == selectedModelIndex) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ImageThumbnail(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) CamDao else CreamBeige

    Box(
        modifier = Modifier
            .size(70.dp)
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        colorResource(id = R.color.teal_200),
                        RoundedCornerShape(10.dp)
                    )
                } else {
                    Modifier
                }
            )
            .background(backgroundColor, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Thumbnail",
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )
    }
}