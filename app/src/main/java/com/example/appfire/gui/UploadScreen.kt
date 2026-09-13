package com.example.appfire.gui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appfire.model.Items
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.ui.theme.Teal
import com.example.appfire.viewmodel.HomeViewModel

@Composable
fun UploadScreen(navController: NavController, homeViewModel: HomeViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var numberInCart by remember { mutableStateOf("") }
    var showRecommended by remember { mutableStateOf(false) }

    // State để hiển thị lỗi
    var titleError by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf("") }
    var numberInCartError by remember { mutableStateOf("") }
    var categoryIdError by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf("") }

    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris.clear()
        selectedImageUris.addAll(uris)
        if (uris.isNotEmpty()) imageError = ""
    }

    // Hàm kiểm tra dữ liệu
    fun validateFields(): Boolean {
        var isValid = true

        // Kiểm tra ID sản phẩm (numberInCart)
        if (numberInCart.isBlank()) {
            numberInCartError = "Vui lòng nhập ID sản phẩm"
            isValid = false
        } else if (numberInCart.toIntOrNull() == null) {
            numberInCartError = "ID sản phẩm phải là số"
            isValid = false
        } else {
            numberInCartError = ""
        }

        // Kiểm tra tên sản phẩm
        if (title.isBlank()) {
            titleError = "Vui lòng nhập tên sản phẩm"
            isValid = false
        } else if (title.length < 3) {
            titleError = "Tên sản phẩm phải có ít nhất 3 ký tự"
            isValid = false
        } else {
            titleError = ""
        }

        // Kiểm tra giá
        if (price.isBlank()) {
            priceError = "Vui lòng nhập giá sản phẩm"
            isValid = false
        } else {
            val priceValue = price.toDoubleOrNull()
            if (priceValue == null) {
                priceError = "Giá phải là số"
                isValid = false
            } else if (priceValue <= 0) {
                priceError = "Giá phải lớn hơn 0"
                isValid = false
            } else {
                priceError = ""
            }
        }

        // Kiểm tra danh mục
        if (categoryId.isBlank()) {
            categoryIdError = "Vui lòng nhập ID danh mục"
            isValid = false
        } else {
            categoryIdError = ""
        }

        // Kiểm tra ảnh
        if (selectedImageUris.isEmpty()) {
            imageError = "Vui lòng chọn ít nhất 1 ảnh"
            isValid = false
        } else {
            imageError = ""
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KemSua)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Thêm Sản Phẩm Mới", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // ID Sản phẩm
        OutlinedTextField(
            value = numberInCart,
            onValueChange = {
                numberInCart = it
                numberInCartError = ""
            },
            label = { Text("ID Sản phẩm") },
            modifier = Modifier.fillMaxWidth(),
            isError = numberInCartError.isNotEmpty(),
            supportingText = {
                if (numberInCartError.isNotEmpty()) {
                    Text(numberInCartError, color = Color.Red)
                }
            }
        )

        // Tên sản phẩm
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleError = ""
            },
            label = { Text("Tên sản phẩm") },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError.isNotEmpty(),
            supportingText = {
                if (titleError.isNotEmpty()) {
                    Text(titleError, color = Color.Red)
                }
            }
        )

        // Giá
        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
                priceError = ""
            },
            label = { Text("Giá (VNĐ)") },
            modifier = Modifier.fillMaxWidth(),
            isError = priceError.isNotEmpty(),
            supportingText = {
                if (priceError.isNotEmpty()) {
                    Text(priceError, color = Color.Red)
                }
            }
        )

        // Mô tả (không bắt buộc)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Mô tả (không bắt buộc)") },
            modifier = Modifier.fillMaxWidth()
        )

        // ID Danh mục
        OutlinedTextField(
            value = categoryId,
            onValueChange = {
                categoryId = it
                categoryIdError = ""
            },
            label = { Text("ID Danh mục") },
            modifier = Modifier.fillMaxWidth(),
            isError = categoryIdError.isNotEmpty(),
            supportingText = {
                if (categoryIdError.isNotEmpty()) {
                    Text(categoryIdError, color = Color.Red)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Phần ảnh
        Text("Ảnh đã chọn (${selectedImageUris.size})", fontWeight = FontWeight.Bold)

        if (imageError.isNotEmpty()) {
            Text(imageError, color = Color.Red, fontSize = 12.sp)
        }

        if (selectedImageUris.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedImageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Button(
            onClick = {
                launcher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CamDao)
        ) {
            Text(if (selectedImageUris.isEmpty()) "Chọn ảnh từ thiết bị" else "Đã chọn ${selectedImageUris.size} ảnh")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hiển thị ở mục Gợi ý?", fontSize = 16.sp)
            Switch(
                checked = showRecommended,
                onCheckedChange = { showRecommended = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CamDao,
                    checkedTrackColor = Color.White,
                    uncheckedThumbColor = CamDao,
                    uncheckedTrackColor = Color.LightGray,
                    checkedBorderColor = Teal,
                    uncheckedBorderColor = Color.LightGray
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút lưu với kiểm tra
        Button(
            onClick = {
                if (validateFields()) {
                    isUploading = true

                    val fakeDownloadUrls = arrayListOf(
                        "https://vcdn1-kinhdoanh.vnecdn.net/2023/01/05/hoa-hong-7261-1672911634.jpg"
                    )

                    val newItem = Items(
                        title = title,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        picUrl = fakeDownloadUrls,
                        categoryId = categoryId,
                        numberInCart = numberInCart.toIntOrNull() ?: 0,
                        showRecommended = showRecommended
                    )

                    homeViewModel.uploadItem(newItem) { success ->
                        isUploading = false
                        if (success) {
                            Toast.makeText(context, "Lưu thành công!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Lưu thất bại!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = CamDao)
        ) {
            Text(if (isUploading) "ĐANG XỬ LÝ..." else "LƯU THÔNG TIN")
        }
    }
}