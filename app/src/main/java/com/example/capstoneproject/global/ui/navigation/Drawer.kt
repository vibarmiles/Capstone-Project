package com.example.capstoneproject.global.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstoneproject.R

@Composable
fun Drawer(onClick: (Int) -> Unit) {
    val navigationList: List<NavigationItems> = listOf(NavigationItems.Dashboard, NavigationItems.Inventory, NavigationItems.Supplier, NavigationItems.Users, NavigationItems.Report, NavigationItems.POS)
    val subNavigationList: List<NavigationItems> = listOf(NavigationItems.Inventory.Product, NavigationItems.Inventory.Branch, NavigationItems.Inventory.Category, NavigationItems.Supplier.Contact, NavigationItems.Supplier.PurchaseOrder, NavigationItems.Supplier.ReturnOrder)
    var showSubItemForInventory by remember { mutableStateOf(false) }
    var showSubItemForSupplier by remember { mutableStateOf(false) }
    Column {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(Color(135, 206, 235))) {
            Spacer(modifier = Modifier.size(30.dp))
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(imageVector = Icons.Filled.Person, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Gray))
                Text(text = "First Name M. Last", fontSize = 24.sp)
                Text(text = "ADMIN", fontSize = 14.sp)
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)) {
            for (it in navigationList) {
                item {
                    NavigationItem(icon = it.icon, isExpandable = it.isParent, expand = when (it.title) { R.string.inventory -> showSubItemForInventory; R.string.supplier -> showSubItemForSupplier; else -> false}, title = it.title) {
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
                        }}
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                if (it.isParent) {
                    for (subItem in subNavigationList) {
                        if (it.title == subItem.parentItem && subItem.parentItem == R.string.inventory) {
                            item {
                                AnimatedVisibility(visible = showSubItemForInventory, enter = expandVertically(expandFrom = Alignment.CenterVertically), exit = shrinkVertically()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NavigationItem(icon = subItem.icon, title = subItem.title) {
                                        onClick.invoke(subItem.title)
                                    }
                                }
                            }
                        } else if (it.title == subItem.parentItem && subItem.parentItem == R.string.supplier) {
                            item {
                                AnimatedVisibility(visible = showSubItemForSupplier, enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NavigationItem(icon = subItem.icon, title = subItem.title) {
                                        onClick.invoke(subItem.title)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    NavigationItem(icon = Icons.Filled.ExitToApp, title = R.string.logout) {

                    }
                }
            }
        }
    }
}

@Composable
fun NavigationItem(icon: ImageVector, title: Int, isExpandable: Boolean = false, expand: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier
        .clickable(
            onClick = onClick
        )
        .fillMaxWidth()
        .padding(8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = stringResource(title), fontWeight = FontWeight.Bold, modifier = Modifier
            .fillMaxWidth()
            .weight(1f))
        if (isExpandable) {
            Icon(imageVector = if (expand) { Icons.Filled.ArrowDropUp } else { Icons.Filled.ArrowDropDown }, contentDescription = null)
        }
    }
}