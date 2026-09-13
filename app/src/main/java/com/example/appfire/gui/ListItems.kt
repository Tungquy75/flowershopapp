package com.example.appfire.gui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appfire.DetailActivity
import com.example.appfire.R
import com.example.appfire.model.Items
import com.example.appfire.ui.theme.CreamBeige

@Composable
fun ListItemsFullSize(items: List<Items>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items.size) { index ->
            RecommendedItem(items, index)
        }
    }
}

@Composable
fun ListItems(items: List<Items>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .height(500.dp)
            .padding(start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items.size) { index ->
            RecommendedItem(items, index)
        }
    }
}

@Composable
fun RecommendedItem(items: List<Items>, pos: Int) {
    val context = LocalContext.current
    val item = items[pos]
    val imageUrl = item.picUrl.firstOrNull()

    Column(
        modifier = Modifier
            .padding(8.dp)
            .height(225.dp)
    ) {
        if (imageUrl.isNullOrEmpty()) {
            // khi không có ảnh hiển thị ảnh bouquet
            Image(
                painter = painterResource(R.drawable.bouquet),
                contentDescription = "Product Image",
                modifier = Modifier
                    .width(175.dp)
                    .height(175.dp)
                    .background(CreamBeige, shape = RoundedCornerShape(10.dp))
                    .clickable {
                        val intent = Intent(context, DetailActivity::class.java).apply {
                            putExtra("object", items[pos])
                        }
                        context.startActivity(intent)
                    }
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            // CÓ ẢNH
            AsyncImage(
                model = imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .width(175.dp)
                    .height(175.dp)
                    .background(CreamBeige, shape = RoundedCornerShape(10.dp))
                    .padding(8.dp)
                    .clickable {
                        val intent = Intent(context, DetailActivity::class.java).apply {
                            putExtra("object", items[pos])
                        }
                        context.startActivity(intent)
                    },
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.bouquet)
            )
        }

        Text(
            text = item.title,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Yellow

                    )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.rating.toString(),
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "${String.format("%,.0f", item.price)} VNĐ",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}