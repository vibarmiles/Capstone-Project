package com.example.capstoneproject.global.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.capstoneproject.R

sealed class NavigationItems(
    val icon: ImageVector,
    val title: Int,
    val parentItem: Int? = null,
    val isParent: Boolean = false
) {
    object Dashboard : NavigationItems(icon = Icons.Filled.Dashboard, title = R.string.dashboard)
    object Inventory : NavigationItems(icon = Icons.Filled.Inventory, title = R.string.inventory, isParent = true) {
        object Product : NavigationItems(icon = Icons.Filled.LocalOffer, title = R.string.product, parentItem = title)
        object Branch : NavigationItems(icon = Icons.Filled.Store, title = R.string.branch, parentItem = title)
        object Category : NavigationItems(icon = Icons.Filled.Bookmarks, title = R.string.category, parentItem = title)
    }
    object Supplier : NavigationItems(icon = Icons.Filled.Phone, title = R.string.supplier, isParent = true) {
        object Contact : NavigationItems(icon = Icons.Filled.ContactPhone, title = R.string.contact, parentItem = title)
        object PurchaseOrder : NavigationItems(icon = Icons.Filled.Reorder, title = R.string.purchase_order, parentItem = title)
        object ReturnOrder : NavigationItems(icon = Icons.Filled.KeyboardReturn, title = R.string.return_order, parentItem = title)
        object TransferOrder : NavigationItems(icon = Icons.Filled.SyncAlt, title = R.string.transfer_order, parentItem = title)
    }
    object Users : NavigationItems(icon = Icons.Filled.People, title = R.string.user)
    object Report : NavigationItems(icon = Icons.Filled.Report, title = R.string.report)
    object POS : NavigationItems(icon = Icons.Filled.PointOfSale, title = R.string.pos)
}
