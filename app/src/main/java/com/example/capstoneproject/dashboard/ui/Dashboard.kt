package com.example.capstoneproject.dashboard.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.navigation.BaseTopAppBar
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.product_management.ui.product.getCriticalLevel
import com.example.capstoneproject.supplier_management.data.firebase.Status
import com.example.capstoneproject.supplier_management.ui.purchase_order.PurchaseOrderViewModel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun Dashboard(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    branchViewModel: BranchViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    goToBranches: () -> Unit,
    goToProducts: () -> Unit,
    goToPO: () -> Unit,
    goToPOS: () -> Unit
) {
    val branches = branchViewModel.getAll().observeAsState(listOf())
    val products = productViewModel.getAll()
    val purchaseOrders = purchaseOrderViewModel.getAll().observeAsState(listOf())

    Scaffold(
        topBar = {
            BaseTopAppBar(title = stringResource(id = R.string.dashboard), scope = scope, scaffoldState = scaffoldState)
        }
    ) {
            paddingValues ->
        if (branchViewModel.isLoading.value || productViewModel.isLoading.value || purchaseOrderViewModel.isLoading.value) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            val numberOfBranches = branches.value.size
            val productsUnderCriticalLevel by remember(productViewModel.update.value, branches) { mutableStateOf(branches.value.sumOf { branch -> products.values.count { product -> product.stock.getOrDefault(key = branch.id, defaultValue = 0) <= getCriticalLevel(product = product) } }) }
            val productsSold by remember(productViewModel.update.value) { mutableStateOf(products.values.sumOf { product -> product.transaction.soldThisYear }) }
            val productsPurchased by remember(productViewModel.update.value) { mutableStateOf(products.values.sumOf { product -> product.transaction.purchased }) }
            val stockInHand by remember(productViewModel.update.value, branches) { mutableStateOf(branches.value.sumOf { branch -> products.values.sumOf { product -> product.stock.getOrDefault(key = branch.id, defaultValue = 0) } }) }
            val stockToBeReceived by remember(purchaseOrders.value) { mutableStateOf(purchaseOrders.value.filter { purchaseOrder -> purchaseOrder.status == Status.WAITING }.sumOf { purchaseOrders -> purchaseOrders.products.values.sumOf { product -> product.quantity } }) }

            Column(modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Activity Summary", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(2.dp))
                DashboardItem(icon = DashboardItemIcon(image = Icons.Default.Store, color = Color.Red), data = numberOfBranches.toString(), supportingText = "Branches", onClick = goToBranches)
                DashboardItem(icon = DashboardItemIcon(image = Icons.Default.Reorder, color = Color.Blue), data = productsUnderCriticalLevel.toString(), supportingText = "Products Under Critical Level", onClick = goToProducts)
                DashboardItem(icon = DashboardItemIcon(image = Icons.Default.ShoppingCart, color = Color.Magenta), data = productsPurchased.toString(), supportingText = "Products Bought This Year", onClick = goToPO)
                DashboardItem(icon = DashboardItemIcon(image = Icons.Default.PointOfSale, color = Color.DarkGray), data = productsSold.toString(), supportingText = "Products Sold This Year", onClick = goToPOS)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Inventory Summary (in Units)", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardItem(data = stockInHand.toString(), textColor = Color(red = 0f, green = 0.8f, blue = 0f), supportingText = "In Hand", modifier = Modifier.weight(1f), onClick = goToProducts)
                    DashboardItem(data = stockToBeReceived.toString(), textColor = Color.Red, supportingText = "To Be Received", modifier = Modifier.weight(1f), onClick = goToPO)
                }
            }
        }
    }
}

data class DashboardItemIcon(
    val image: ImageVector,
    val color: Color
)

@Composable
fun DashboardItem(
    modifier: Modifier = Modifier,
    icon: DashboardItemIcon? = null,
    data: String,
    textColor: Color = MaterialTheme.colors.onBackground,
    supportingText: String,
    onClick: () -> Unit
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .clickable { onClick.invoke() }
        .border(color = Color.Black, shape = RoundedCornerShape(size = 10.dp), width = 1.dp)
        .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Box(modifier = Modifier
                .size(50.dp)
                .background(color = icon.color, shape = CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon.image, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = data, color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = supportingText, fontSize = 14.sp)
        }
    }
}