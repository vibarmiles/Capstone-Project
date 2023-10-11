package com.example.capstoneproject.product_management.ui.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StackedBarChart
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmDeletion
import com.example.capstoneproject.global.ui.misc.ImageNotAvailable
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.ui.theme.primaryColor


@Composable
fun ViewProduct(dismissRequest: () -> Unit, productViewModel: ProductViewModel, productId: String, product: Product, edit: () -> Unit, set: () -> Unit, delete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product.productName) },
                navigationIcon = {
                    IconButton(onClick = dismissRequest) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    var expanded: Boolean by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = !expanded }, content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) }, text = { Text(text = "Edit Product") }, onClick = { expanded = false; edit.invoke() })
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.StackedBarChart, contentDescription = null) }, text = { Text(text = "Adjust Quantity") }, onClick = { expanded = false; set.invoke() })
                        DropdownMenuItem(leadingIcon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) }, text = { Text(text = "Delete Product") }, onClick = { expanded = false; showDeleteDialog = true })
                    }
                }
            )
        }
    ) {
            paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray)) {
                if (product.image == null) {
                    ImageNotAvailable(modifier = Modifier.size(200.dp))
                } else {
                    SubcomposeAsyncImage(error = { ImageNotAvailable() },  model = product.image ?: "", contentScale = ContentScale.Crop, modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .size(200.dp), loading = { CircularProgressIndicator() }, contentDescription = null)
                }

                Text(text = "NO IMAGE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                    Text(text = "Product Details")
                }

                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                    Text(text = "Stocks")
                }
            }

            if (showDeleteDialog) {
                ConfirmDeletion(item = product.productName, onCancel = { showDeleteDialog = false }) {
                    productViewModel.delete(key = productId)
                    showDeleteDialog = false
                    delete.invoke()
                }
            }
        }
    }
}