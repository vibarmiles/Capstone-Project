package com.example.capstoneproject.global.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.capstoneproject.R

class NavigationItem(
    icon: ImageVector,
    title: Int,
    parentItem: Int? = null,
    isParent: Boolean = false
) {
    var getIcon: ImageVector = icon
    var getTitle: Int = title
    var getParent: Boolean = isParent
    var getParentItem: Int? = parentItem
}

fun getDrawerItems(): List<NavigationItem> {
    return listOf(
        NavigationItem(Icons.Filled.Dashboard, R.string.dashboard),
        NavigationItem(Icons.Filled.Inventory, R.string.inventory, isParent = true),
        NavigationItem(Icons.Filled.Phone, R.string.supplier, isParent = true),
        NavigationItem(Icons.Filled.People, R.string.user),
        NavigationItem(Icons.Filled.Report, R.string.report),
        NavigationItem(Icons.Filled.PointOfSale, R.string.pos)
    )
}

fun getDrawerSubItems(): List<NavigationItem> {
    return listOf(
        NavigationItem(Icons.Filled.LocalOffer, R.string.product, R.string.inventory),
        NavigationItem(Icons.Filled.Store, R.string.branch, R.string.inventory),
        NavigationItem(Icons.Filled.Bookmarks, R.string.category, R.string.inventory),
        NavigationItem(Icons.Filled.ContactPhone, R.string.contact, R.string.supplier),
        NavigationItem(Icons.Filled.Reorder, R.string.purchase_order, R.string.supplier),
        NavigationItem(Icons.Filled.KeyboardReturn, R.string.return_order, R.string.supplier),
    )
}