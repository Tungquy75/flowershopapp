package com.example.appfire

import com.example.appfire.model.Items
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appfire.gui.ListItemsFullSize

import com.example.appfire.ui.theme.KemSua
import com.example.appfire.viewmodel.HomeViewModel

class ListItemsActivity : ComponentActivity() {
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        id = intent.getStringExtra("id") ?: ""
        title = intent.getStringExtra("title") ?: ""
        setContent {
            ListItemScreen(
                title = title,
                onBackClick = { finish() },
                id = id
            )
        }
    }
}

@Composable
fun ListItemScreen(
    title: String,
    onBackClick: () -> Unit,
    id: String
) {
    val viewModel: HomeViewModel = viewModel()
    val items by viewModel.filteredItems.observeAsState(emptyList())
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(id) {
        isLoading = true
        viewModel.loadFiltered(id)
    }

    LaunchedEffect(items) {
        if (items.isNotEmpty()) {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(KemSua)) {
        ConstraintLayout(modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp)) {
            val (backBtn, cartTxt) = createRefs()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt) { centerTo(parent) },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                text = title
            )
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ListItemsFullSize(items)
        }
    }
}