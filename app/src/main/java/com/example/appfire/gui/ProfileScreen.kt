package com.example.appfire.gui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appfire.R
import com.example.appfire.ui.theme.CamDao
import com.google.firebase.auth.FirebaseAuth

private val IconBg = Color(color = 0xfff0f1f3)
private val PrimaryText = Color(color = 0xff1b1c1e)
private val SecondaryText = Color(color = 0xff8a8e95)
private val MidSheet = Color(color = 0xfff3f4f6)

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit = {},
    onAdminOrderClick: () -> Unit = {}
) {
    val scroll = rememberScrollState()

    // Lấy thông tin user từ Firebase
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // State cho thông tin user
    var userEmail by remember { mutableStateOf("") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Cập nhật thông tin user
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            userEmail = user.email ?: ""
            userPhotoUrl = user.photoUrl?.toString()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MidSheet)
    ) {
        Image(
            painter = painterResource(R.drawable.arc_pic),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 400.dp)
            .clip(shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
            .background(Color.White)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 230.dp)
            .verticalScroll(state = scroll)
    ) {
        // Avatar
        Surface(
            shape = CircleShape,
            shadowElevation = 6.dp,
            color = Color.White,
            modifier = Modifier
                .size(size = 96.dp)
                .align(Alignment.CenterHorizontally)
        ) {

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    tint = Color.Gray)

        }

        Spacer(Modifier.height(height = 16.dp))

        // Email đang đăng nhập
        Text(
            text = userEmail.ifEmpty { "user@gmail.com" },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                fontSize = 18.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(height = 32.dp))

        // Menu items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            MenuItemRow(
                title = "Lịch sử đơn hàng",
                iconVector = Icons.Default.Check,
                onClick = {
                    navController.navigate("order_history")
                }
            )
            if (userEmail == "lutungquy@gmail.com") {
                MenuItemRow(
                    title = "Quản lý đơn hàng",
                    iconVector = Icons.Default.AccountCircle,  // Có thể đổi icon khác
                    onClick = onAdminOrderClick  // Dùng callback đã có
                )
            }


            MenuItemRow(
                title = "Đăng xuất",
                iconVector = Icons.Default.ExitToApp,
                onClick = onLogout
            )
        }

        // Bottom Menu
        ProfileBottomMenu(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
            onHomeClick = {
                navController.navigate("home") {
                    popUpTo("profile") { inclusive = true }
                }
            },
            onAccountClick = {
                // Đang ở profile
            }
        )
    }
}

@Composable
fun ProfileBottomMenu(
    modifier: Modifier,
    onHomeClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
            .fillMaxWidth()
            .background(
                CamDao,
                shape = RoundedCornerShape(10.dp)
            )
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ProfileBottomMenuItem(
            icon = Icons.Default.Home,
            text = "Trang chủ",
            isSelected = false
        ) {
            onHomeClick()
        }

        ProfileBottomMenuItem(
            icon = Icons.Default.AccountCircle,
            text = "Tài khoản",
            isSelected = true
        ) {
            onAccountClick()
        }
    }
}

@Composable
fun ProfileBottomMenuItem(
    icon: ImageVector,
    text: String,
    isSelected: Boolean = false,
    onItemClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .height(60.dp)
            .clickable { onItemClick?.invoke() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) Color.Black else Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        navController = androidx.navigation.compose.rememberNavController(),
        onLogout = {}
    )
}

@Composable
private fun MenuItemRow(
    title: String,
    iconVector: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 6.dp,
            color = IconBg,
            modifier = Modifier.size(size = 50.dp)
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(Modifier.width(width = 14.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            ),
            modifier = Modifier.weight(weight = 1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(24.dp)
        )
    }
}