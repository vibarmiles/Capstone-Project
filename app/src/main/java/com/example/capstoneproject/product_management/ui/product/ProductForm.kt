package com.example.capstoneproject.product_management.ui.product

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.SubcomposeAsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmationForAddingDialog
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.global.ui.misc.ImageNotAvailable
import com.example.capstoneproject.product_management.data.firebase.category.Category
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.category.CategoryDialog
import com.example.capstoneproject.product_management.ui.category.CategoryViewModel
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.user_management.ui.users.UserViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductForm(
    scaffoldState: ScaffoldState,
    dismissRequest: () -> Unit,
    function: String,
    productId: String? = null,
    productViewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel,
    contactViewModel: ContactViewModel,
    userViewModel: UserViewModel
) {
    var product = productViewModel.getProduct(productId) ?: Product()
    val category = categoryViewModel.getAll().observeAsState(listOf())
    val supplier = contactViewModel.getAll().observeAsState(listOf())
    var name by remember { mutableStateOf(product.productName) }
    var isNameValid by remember { mutableStateOf(true) }
    var purchasePrice by remember { mutableStateOf(if (product.purchasePrice > 0) String.format("%.99f", product.purchasePrice).trimEnd('0').trimEnd('.') else "") }
    var sellingPrice by remember { mutableStateOf(if (product.sellingPrice > 0) String.format("%.99f", product.sellingPrice).trimEnd('0').trimEnd('.') else "") }
    var isPurchasePriceValid by remember { mutableStateOf(true) }
    var isSellingPriceValid by remember { mutableStateOf(true) }
    var expandedContacts by remember { mutableStateOf(false) }
    var expandedCategories by remember { mutableStateOf(false) }
    var contactId: String? by remember { mutableStateOf(product.supplier.let { it.ifBlank { supplier.value.firstOrNull()?.id } } ) }
    var selectedContact by remember { mutableStateOf(supplier.value.firstOrNull { contact -> contact.id == contactId }?.name ?: supplier.value.firstOrNull()?.name ?: "No Suppliers Entered") }
    var categoryId: String? by remember { mutableStateOf(product.category) }
    var selectedCategory by remember { mutableStateOf(if (categoryId == null) "None" else category.value.firstOrNull { category -> categoryId == category.id }?.categoryName ?: "None") }
    var imageUri by remember { mutableStateOf(if (product.image == null) null else Uri.parse(product.image)) }
    var leadTime by remember { mutableStateOf(product.leadTime) }
    var leadTimeText by remember { mutableStateOf(if (leadTime != 0) leadTime.toString() else "") }
    var isLeadTimeValid by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val imageUriLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument(), onResult = { imageUri = it })
    val jsonUriLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent(), onResult = {
        if (it != null) {
            val item = context.contentResolver.openInputStream(it)
            if (item != null) {
                product = productViewModel.readFromJson(item)
                name = product.productName
                purchasePrice = product.purchasePrice.toString()
                sellingPrice = product.sellingPrice.toString()
                contactId = product.supplier
                selectedContact = supplier.value.firstOrNull { contact -> contact.id == contactId }?.name ?: supplier.value.firstOrNull()?.name ?: "No Suppliers Entered"
                categoryId = product.category
                selectedCategory = category.value.firstOrNull { category -> categoryId == category.id }?.categoryName ?: "None"
                imageUri = if (product.image == null) null else Uri.parse(product.image)
                leadTimeText = product.leadTime.toString()
                item.close()
            }
        }
    })
    var showDialog by remember { mutableStateOf(false) }
    var newCategory = Category()
    val state by categoryViewModel.result.collectAsState()
    val localFocusManager = LocalFocusManager.current
    val showConfirmationDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = ("$function Product").uppercase()) },
                navigationIcon = {
                    IconButton(onClick = dismissRequest) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        jsonUriLauncher.launch("application/json")
                    }) {
                        Icon(imageVector = Icons.Filled.Upload, contentDescription = null)
                    }
                }
            )
        },
        scaffoldState = scaffoldState
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    error = { ImageNotAvailable(modifier = Modifier.background(Color.LightGray)) },
                    loading = { CircularProgressIndicator() },
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(200.dp), contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .height(intrinsicSize = IntrinsicSize.Min)
            ) {
                Text(maxLines = 1, overflow = TextOverflow.Ellipsis, text = imageUri?.path ?: "No Image Selected", modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = OutlinedTextFieldDefaults
                            .contentPadding()
                            .calculateLeftPadding(layoutDirection = LayoutDirection.Ltr)
                    )
                )
                Button(modifier = Modifier.fillMaxHeight(), onClick = { imageUriLauncher.launch(arrayOf("image/*")) }) {
                    Text(text = "Upload Image")
                }
            }

            ExposedDropdownMenuBox(expanded = expandedContacts, onExpandedChange = { expandedContacts = !expandedContacts }) {
                OutlinedTextField(isError = contactId == null, colors = GlobalTextFieldColors(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedContacts) }, modifier = Modifier.fillMaxWidth(), value = selectedContact, onValueChange = {  }, readOnly = true, label = { Text(text = buildAnnotatedString { append(text = stringResource(id = R.string.supplier)); withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") } }) })

                DropdownMenu(
                    modifier = Modifier
                        .exposedDropdownSize()
                        .fillMaxWidth(),
                    expanded = expandedContacts,
                    onDismissRequest = { expandedContacts = false }
                ) {
                    supplier.value.forEach {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = it.name) },
                            onClick = {
                                contactId = it.id
                                selectedContact = it.name
                                expandedContacts = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                colors = GlobalTextFieldColors(),
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(text = "Enter Product's Name") },
                label = {
                    Text(text = buildAnnotatedString {
                        append(text = "Product Name")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
                            append(text = " *")
                        }
                    })
                },
                isError = !isNameValid,
                trailingIcon = { if (!isNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                colors = GlobalTextFieldColors(),
                value = purchasePrice,
                onValueChange = { value ->
                    if (value.substringAfter('.').length <= 2 || !value.contains('.')) {
                        if (value.length < 10) value.toDoubleOrNull()?.let { num ->
                            if (num >= 0) purchasePrice = value
                        } ?: run { if (value.isBlank()) purchasePrice = "" }
                    }
                },
                placeholder = { Text(text = "Enter Purchase Price") },
                label = { Text(text =buildAnnotatedString {
                    append(text = "Purchase Price")
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
                        append(text = " *")
                    }
                }) },
                isError = !isPurchasePriceValid,
                trailingIcon = { if (!isPurchasePriceValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                colors = GlobalTextFieldColors(),
                value = sellingPrice,
                onValueChange = { value ->
                    if (value.substringAfter('.').length <= 2 || !value.contains('.')) {
                        if (value.length < 10) value.toDoubleOrNull()?.let { num ->
                            if (num >= 0) sellingPrice = value
                        } ?: run { if (value.isBlank()) sellingPrice = "" }
                    }
                },
                placeholder = { Text(text = "Enter Selling Price") },
                label = { Text(text = buildAnnotatedString {
                    append(text = "Selling Price")
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
                        append(text = " *")
                    }
                }) },
                isError = !isSellingPriceValid,
                trailingIcon = { if (!isSellingPriceValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                colors = GlobalTextFieldColors(),
                value = leadTimeText,
                onValueChange = { value ->
                    value.toIntOrNull()?.let { num ->
                        if (num >= 0) leadTimeText = value
                    } ?: run { if (value.isBlank()) leadTimeText = "" }
                },
                placeholder = { Text(text = "Enter Product's Lead Time (Default value is 3 days)") },
                label = { Text(text = buildAnnotatedString {
                    append(text = "Lead Time")
                    withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
                        append(text = " *")
                    }
                }) },
                isError = !isLeadTimeValid,
                trailingIcon = { if (!isLeadTimeValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    localFocusManager.clearFocus()
                })
            )

            ExposedDropdownMenuBox(expanded = expandedCategories, onExpandedChange = { expandedCategories = !expandedCategories }) {
                OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategories) }, colors = GlobalTextFieldColors(), modifier = Modifier.fillMaxWidth(), value = selectedCategory, onValueChange = {  }, readOnly = true, label = { Text(text = stringResource(id = R.string.category)) })

                DropdownMenu(
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier
                        .exposedDropdownSize()
                        .requiredHeightIn(max = 300.dp)
                        .fillMaxWidth(),
                    expanded = expandedCategories,
                    onDismissRequest = { expandedCategories = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = "None") },
                        onClick = {
                            categoryId = null
                            selectedCategory = "None"
                            expandedCategories = false
                        }
                    )

                    category.value.forEach {
                        androidx.compose.material3.DropdownMenuItem(text = {
                            Text(text = it.categoryName) },
                            onClick = {
                                categoryId = it.id
                                selectedCategory = it.categoryName
                                expandedCategories = false
                                localFocusManager.clearFocus()
                            }
                        )
                    }

                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(text = "Add New Category...") },
                        onClick = {
                            showDialog = true
                        }
                    )
                }
            }

            FormButtons(cancel = dismissRequest) {
                isNameValid = name.isNotBlank()
                purchasePrice.toDoubleOrNull()?.let { isPurchasePriceValid = it > 0 } ?: run { isPurchasePriceValid = false }
                sellingPrice.toDoubleOrNull()?.let { isSellingPriceValid = it > 0 } ?: run { isSellingPriceValid = false }
                if (isPurchasePriceValid && isSellingPriceValid) {
                    if (sellingPrice.toDouble() <= purchasePrice.toDouble()) {
                        isSellingPriceValid = false
                    }
                }
                leadTimeText.toIntOrNull()?.let { isLeadTimeValid = it >= 0; leadTime = it } ?: run {
                    if (leadTimeText.isBlank()) {
                        leadTime = 0
                    } else {
                        isLeadTimeValid = false
                    }
                }

                Log.d("PATH",imageUri.toString())
                if (isNameValid && isPurchasePriceValid && contactId != null && isSellingPriceValid && isLeadTimeValid) {
                    showConfirmationDialog.value = true
                }
            }

            if (showConfirmationDialog.value) {
                ConfirmationForAddingDialog(onCancel = { showConfirmationDialog.value = false }) {
                    productViewModel.insert(id = productId, product = product.copy(
                        image = if (imageUri != null) imageUri.toString() else null,
                        productName = name,
                        purchasePrice = purchasePrice.toDouble(),
                        sellingPrice = sellingPrice.toDouble(),
                        category = categoryId,
                        supplier = contactId!!,
                        leadTime = leadTime
                    ))
                    showConfirmationDialog.value = false
                    userViewModel.log(event = "${function.lowercase()}_product")
                    dismissRequest.invoke()
                }
            }

            if (showDialog) {
                CategoryDialog(category = newCategory, onConfirm = {
                    newCategory = it
                    categoryViewModel.insert(category = newCategory)
                    showDialog = false
                }) {
                    showDialog = false
                    selectedCategory = "None"
                    categoryId = null
                }
            }
        }

        LaunchedEffect(key1 = state.result, state.errorMessage) {
            if (!state.result && state.errorMessage != null) {
                categoryId = null
                selectedCategory = "None"
                scaffoldState.snackbarHostState.showSnackbar(message = state.errorMessage!!, duration = SnackbarDuration.Short)
                categoryViewModel.resetMessage()
            } else if (state.result) {
                categoryId = newCategory.id
                selectedCategory = newCategory.categoryName
                localFocusManager.clearFocus()
                userViewModel.log(event = "add_category")
                scaffoldState.snackbarHostState.showSnackbar(message = "Successfully Done!", duration = SnackbarDuration.Short)
                categoryViewModel.resetMessage()
            }
        }
    }
}