package com.example.capstoneproject.product_management.ui.product

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.product_management.data.Room.product.Product
import com.example.capstoneproject.product_management.ui.product.viewModel.ProductViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductFormSreen(function: String, viewModel: ProductViewModel, back: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "$function Product") }, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) {
        it -> it
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val category = viewModel.categories.collectAsState(initial = listOf())
            var name by remember { mutableStateOf("") }
            var isNameValid by remember { mutableStateOf(true) }
            var price by remember { mutableStateOf("") }
            var isPriceValid by remember { mutableStateOf(true) }
            var expanded by remember { mutableStateOf(false) }
            var categoryId: Int by remember { mutableStateOf(0) }
            var selectedCategory by remember { mutableStateOf("None") }
            var quantity by remember { mutableStateOf("") }
            var isQuantityValid by remember { mutableStateOf(true) }
            var imageUri by remember { mutableStateOf<Uri?>(null) }
            val imageUriLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument(), onResult = { imageUri = it })

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), contentAlignment = Alignment.Center) {
                AsyncImage(error = rememberVectorPainter(image = Icons.Filled.BrokenImage), model = imageUri, contentDescription = null, fallback = rememberVectorPainter(Icons.Filled.Image), modifier = Modifier
                    .fillMaxHeight()
                    .width(200.dp), contentScale = ContentScale.Crop)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .border(
                    border = BorderStroke(width = 1.dp, color = Color.Gray),
                    shape = RoundedCornerShape(5.dp)
                )
                .height(intrinsicSize = IntrinsicSize.Min)) {
                Text(text = imageUri?.path ?: "No Image Selected", modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = OutlinedTextFieldDefaults
                            .contentPadding()
                            .calculateLeftPadding(layoutDirection = LayoutDirection.Ltr)
                    ))
                Button(modifier = Modifier.fillMaxHeight(), onClick = { imageUriLauncher.launch(arrayOf("image/*")) }) {
                    Text(text = "Upload Image")
                }
            }

            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = name, onValueChange = { name = it }, placeholder = { Text(text = "Enter Product Name") }, label = { Text(text = "Product Name") }, isError = !isNameValid, trailingIcon = { if (!isNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) })
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = price, onValueChange = { price = it }, placeholder = { Text(text = "Enter Selling Price") }, label = { Text(text = "Selling Price") }, isError = !isPriceValid, trailingIcon = { if (!isPriceValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth(), value = selectedCategory, onValueChange = {  }, readOnly = true, label = { Text(text = stringResource(id = R.string.category)) })

                DropdownMenu(modifier = Modifier
                    .exposedDropdownSize()
                    .fillMaxWidth(), expanded = expanded, onDismissRequest = { expanded = false }) {
                    androidx.compose.material3.DropdownMenuItem(text = { Text(text = "None") }, onClick = { categoryId = 0; selectedCategory = "None" })

                    category.value.forEach {
                        androidx.compose.material3.DropdownMenuItem(text = { Text(text = it.categoryName) }, onClick = { categoryId = it.id; selectedCategory = it.categoryName })
                    }
                }
            }

            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = quantity, onValueChange = { quantity = it }, placeholder = { Text(text = "Enter Current Quantity") }, label = { Text(text = "Quantity") }, isError = !isQuantityValid, trailingIcon = { if (!isQuantityValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            val context = LocalContext.current

            FormButtons(cancel = back) {
                isNameValid = name.isNotBlank()
                isPriceValid = if (price.isNotBlank()) price.toDouble() > 0 else false
                isQuantityValid = if (quantity.isNotBlank()) quantity.toInt() >= 0 else false

                context.contentResolver.takePersistableUriPermission(imageUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (isNameValid && isPriceValid && isQuantityValid) {
                    viewModel.insert(product = Product(image = imageUri.toString(), productName = name, price = price.toDouble(), category = categoryId, quantity = quantity.toInt()))
                    back.invoke()
                }
            }
        }
    }
}

