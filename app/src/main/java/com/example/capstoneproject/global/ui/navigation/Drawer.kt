package com.example.capstoneproject.global.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ImageNotAvailable
import com.example.capstoneproject.global.ui.misc.NavigationItemColors
import com.example.capstoneproject.login.data.login.SignInResult
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import com.example.capstoneproject.user_management.ui.users.UserViewModel

@Composable
fun Drawer(
    user: SignInResult,
    currentItem: Int,
    userViewModel: UserViewModel,
    onClick: (Int) -> Unit
) {
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()

    val navigationList = remember {
        mutableStateListOf(
            NavigationItems.Dashboard,
            NavigationItems.Inventory,
            NavigationItems.Supplier,
            NavigationItems.Users,
            NavigationItems.ActivityLogs,
            NavigationItems.Report,
            NavigationItems.POS
        )
    }

    val subNavigationList = remember {
        mutableStateListOf(
            NavigationItems.Inventory.Product,
            NavigationItems.Inventory.Branch,
            NavigationItems.Inventory.Category,
            NavigationItems.Supplier.Contact,
            NavigationItems.Supplier.PurchaseOrder,
            NavigationItems.Supplier.ReturnOrder,
            NavigationItems.Supplier.TransferOrder
        )
    }

    var showSubItemForInventory by remember { mutableStateOf(false) }
    var showSubItemForSupplier by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = userAccountDetails.value) {
        when (userAccountDetails.value.userLevel) {
            UserLevel.Employee -> {
                navigationList.removeAll { true }
                navigationList.addAll(listOf(NavigationItems.Dashboard, NavigationItems.Inventory, NavigationItems.POS))
                subNavigationList.removeAll { true }
                subNavigationList.addAll(listOf(NavigationItems.Inventory.Product))
            }

            UserLevel.Owner -> {
                navigationList.removeAll { true }
                subNavigationList.removeAll { true }
                navigationList.addAll(listOf(NavigationItems.Dashboard, NavigationItems.Inventory, NavigationItems.Supplier, NavigationItems.Users, NavigationItems.Report, NavigationItems.POS))
                subNavigationList.addAll(listOf(NavigationItems.Inventory.Product, NavigationItems.Inventory.Branch, NavigationItems.Inventory.Category, NavigationItems.Supplier.Contact, NavigationItems.Supplier.PurchaseOrder, NavigationItems.Supplier.ReturnOrder, NavigationItems.Supplier.TransferOrder))
            }

            UserLevel.Admin -> {
                navigationList.removeAll { true }
                subNavigationList.removeAll { true }
                navigationList.addAll(listOf(NavigationItems.Users, NavigationItems.ActivityLogs))
            }
        }
    }

    Box(modifier = Modifier
        .background(color = MaterialTheme.colors.secondary)
        .fillMaxWidth()) {
        Spacer(modifier = Modifier.size(30.dp))
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SubcomposeAsyncImage(error = { ImageNotAvailable(modifier = Modifier.background(Color.LightGray)) },  model = user.data?.profilePicture, contentScale = ContentScale.Crop, modifier = Modifier
                .size(75.dp)
                .clip(CircleShape), loading = { CircularProgressIndicator() }, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = user.data?.username.toString(), color = Color.Black, fontSize = 24.sp)
            Text(text = userAccountDetails.value.userLevel.name, color = Color.Black, fontSize = 14.sp)
        }
    }
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        navigationList.forEach {
            item {
                NavigationDrawerItem(colors = NavigationItemColors(), icon = { Icon(imageVector = it.icon, contentDescription = null) }, label = { Row { Text(text = stringResource(id = it.title), modifier = Modifier.weight(1f)); if (it.isParent) Icon(imageVector = when (it.title) { R.string.inventory -> if (showSubItemForInventory) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown; R.string.supplier -> if (showSubItemForSupplier) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown; else -> null } as ImageVector, contentDescription = null) } }, selected = it.title == currentItem, onClick = {
                    if (it.isParent && (it.title == R.string.inventory)) {
                        showSubItemForInventory = !showSubItemForInventory
                        showSubItemForSupplier = false
                    } else if (it.isParent && (it.title == R.string.supplier)) {
                        showSubItemForSupplier = !showSubItemForSupplier
                        showSubItemForInventory = false
                    } else {
                        showSubItemForInventory = false
                        showSubItemForSupplier = false
                        onClick.invoke(it.title)
                    }
                })
            }

            if (it.isParent) {
                subNavigationList.forEach {
                        subItem ->
                    item {
                        if (it.title == subItem.parentItem && subItem.parentItem == R.string.inventory) {
                            AnimatedVisibility(visible = showSubItemForInventory, enter = expandVertically(expandFrom = Alignment.CenterVertically), exit = shrinkVertically()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                NavigationDrawerItem(colors = NavigationItemColors(), icon = { Icon(imageVector = subItem.icon, contentDescription = null) }, label = { Text(text = stringResource(id = subItem.title)) }, onClick = { onClick.invoke(subItem.title) }, selected = subItem.title == currentItem)
                            }
                        } else if (it.title == subItem.parentItem && subItem.parentItem == R.string.supplier) {
                            AnimatedVisibility(visible = showSubItemForSupplier, enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                NavigationDrawerItem(colors = NavigationItemColors(), icon = { Icon(imageVector = subItem.icon, contentDescription = null) }, label = { Text(text = stringResource(id = subItem.title)) }, onClick = { onClick.invoke(subItem.title) }, selected = subItem.title == currentItem)
                            }
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(colors = NavigationItemColors(), icon = { Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = null) }, label = { Text(text = stringResource(id = R.string.logout)) }, onClick = { onClick.invoke(R.string.logout) }, selected = false)
    }
}