package com.example.appfire.gui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appfire.CartActivity
import com.example.appfire.ListItemsActivity
import com.example.appfire.R
import com.example.appfire.model.Category
import com.example.appfire.model.Items
import com.example.appfire.ui.theme.CamDao
import com.example.appfire.ui.theme.CreamBeige
import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.AuthViewModel
import com.example.appfire.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onCartClick: () -> Unit,
    navController: NavController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    val categories = remember { mutableStateListOf<Category>() }
    val recomended = remember { mutableStateListOf<Items>() }
    var showCategoryLoading by remember { mutableStateOf(true) }
    var showRecomendedLoading by remember { mutableStateOf(true) }


    // State tìm kiếm
    var searchText by rememberSaveable { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Items>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeViewModel.loadCategory()
        homeViewModel.loadRecomended()
    }

    LaunchedEffect(searchQuery, recomended) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            val keyword = searchQuery.trim().lowercase()
            searchResults = recomended.filter { item ->
                item.title.lowercase().contains(keyword) ||
                        item.description.lowercase().contains(keyword)
            }
        } else {
            isSearching = false
            searchResults = emptyList()
        }
    }

    val categoryObserver = remember {
        Observer<List<Category>> { categoryList ->
            categories.clear()
            categories.addAll(categoryList ?: emptyList())
            showCategoryLoading = false
        }
    }

    val recomendedObserver = remember {
        Observer<List<Items>> { itemList ->
            recomended.clear()
            recomended.addAll(itemList ?: emptyList())
            showRecomendedLoading = false
        }
    }

    DisposableEffect(Unit) {
        homeViewModel.categories.observeForever(categoryObserver)
        homeViewModel.recomended.observeForever(recomendedObserver)
        onDispose {
            homeViewModel.categories.removeObserver(categoryObserver)
            homeViewModel.recomended.removeObserver(recomendedObserver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = KemSua),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .background(CamDao)
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            val (search, cart) = createRefs()

            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Tìm kiếm...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.clickable {
                            searchQuery = searchText  // Nhấn icon để tìm
                        }
                    )
                },
                modifier = Modifier
                    .height(52.dp)
                    .constrainAs(search) {
                        start.linkTo(parent.start)
                        end.linkTo(cart.start, margin = 8.dp)
                        width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                        centerVerticallyTo(parent)
                    },
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchQuery = searchText  // Nhấn Enter để tìm
                    }
                )
            )

            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { context.startActivity(Intent(context, CartActivity::class.java)) }
                    .constrainAs(cart) {
                        end.linkTo(parent.end)
                        centerVerticallyTo(parent)
                    }
            )
        }

        // Nội dung chính
        if (isSearching) {
            // Kết quả tìm kiếm
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${searchResults.size} kết quả tìm kiếm",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Xóa",
                            color = CamDao,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                searchText = ""
                                searchQuery = ""
                            }
                        )
                    }
                }

                if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Không tìm thấy sản phẩm", color = Color.Gray)
                                Text("Thử tìm kiếm với từ khóa khác", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    item {
                        ListItems(searchResults)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } else {
            // Nội dung home bình thường
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                item {
                    Image(
                        painter = painterResource(id = R.drawable.bannering),
                        contentDescription = "Banner",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }

                item {
                    SectionTitle("Categories", "")
                }

                item {
                    if (showCategoryLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        CategoryList(categories)
                    }
                }
                item {
                    if (authViewModel.getCurrentUser()?.email == "lutungquy@gmail.com") {
                        // Trong HomeScreen
                        Button(
                            onClick = { navController.navigate("upload") },
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CamDao)
                        ) {
                            Text("QUẢN LÝ UPLOAD", color = Color.White)
                        }
                    }
                }
                item {
                    SectionTitle("Recommendation", "")
                }

                item {
                    if (showRecomendedLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        if (recomended.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No recommended items found")
                            }
                        } else {
                            ListItems(recomended)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        BottomMenu(
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars),
            onHomeClick = {
                searchText = ""
                searchQuery = ""
            },
            onAccountClick = {
                navController.navigate("profile")
            }
        )
    }
}

@Composable
fun CategoryList(categories: SnapshotStateList<Category>) {
    var selectedIndex by remember { mutableStateOf(-1) }
    val context = LocalContext.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        items(categories.size) { index ->
            CategoryItem(
                item = categories[index],
                isSelected = selectedIndex == index,
                onItemClick = {
                    selectedIndex = index
                    try {
                        val intent = Intent(context, ListItemsActivity::class.java).apply {
                            putExtra("id", categories[index].id)
                            putExtra("title", categories[index].title)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("CategoryList", "Error: ${e.message}")
                    }
                }
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, actionText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = actionText,
            color = colorResource(R.color.teal_200)
        )
    }
}

@Composable
fun CategoryItem(item: Category, isSelected: Boolean, onItemClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = item.picUrl,
            contentDescription = item.title,
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = if (isSelected) CamDao else CreamBeige,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Inside,
        )

        Text(
            text = item.title,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun BottomMenu(
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
        BottomMenuItem(
            icon = Icons.Default.Home,
            text = "Trang chủ",
            isSelected = true
        ) {
            onHomeClick()
        }

        BottomMenuItem(
            icon = Icons.Default.AccountCircle,
            text = "Tài khoản",
            isSelected = false
        ) {
            onAccountClick()
        }
    }
}

@Composable
fun BottomMenuItem(
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