package com.example.capstoneproject.global.ui.Misc

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
import com.example.capstoneproject.global.ui.navigation.getDrawerItems
import com.example.capstoneproject.global.ui.navigation.getDrawerSubItems

@Composable
fun Drawer(onClick: (Int) -> Unit) {
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
            for (it in getDrawerItems()) {
                item {
                    NavigationItem(icon = it.getIcon, isExpandable = it.getParent, expand = when (it.getTitle) { R.string.inventory -> showSubItemForInventory; R.string.supplier -> showSubItemForSupplier; R.string.supplier -> showSubItemForSupplier; else -> false}, title = it.getTitle) {
                        if (it.getParent && (it.getTitle == R.string.inventory)) {
                            showSubItemForInventory = !showSubItemForInventory
                            showSubItemForSupplier = false
                        } else if (it.getParent && (it.getTitle == R.string.supplier)) {
                            showSubItemForSupplier = !showSubItemForSupplier
                            showSubItemForInventory = false
                        } else {
                            showSubItemForInventory = false
                            showSubItemForSupplier = false
                            onClick.invoke(it.getTitle)
                        }}
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                if (it.getParent) {
                    for (subItem in getDrawerSubItems()) {
                        if (it.getTitle == subItem.getParentItem && subItem.getParentItem == R.string.inventory) {
                            item {
                                AnimatedVisibility(visible = showSubItemForInventory, enter = expandVertically(expandFrom = Alignment.CenterVertically), exit = shrinkVertically()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NavigationItem(icon = subItem.getIcon, title = subItem.getTitle) {
                                        onClick.invoke(subItem.getTitle)
                                    }
                                }
                            }
                        } else if (it.getTitle == subItem.getParentItem && subItem.getParentItem == R.string.supplier) {
                            item {
                                AnimatedVisibility(visible = showSubItemForSupplier, enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NavigationItem(icon = subItem.getIcon, title = subItem.getTitle) {
                                        onClick.invoke(subItem.getTitle)
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