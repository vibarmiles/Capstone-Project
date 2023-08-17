package com.example.capstoneproject.global.ui.navigation

import com.example.capstoneproject.R

class NavigationItem(
    icon: Int,
    title: Int,
    parentItem: Int? = null,
    isParent: Boolean = false
) {
    var getIcon: Int = icon
    var getTitle: Int = title
    var getParent: Boolean = isParent
    var getParentItem: Int? = parentItem
}

fun getDrawerItems(): List<NavigationItem> {
    return listOf(
        NavigationItem(R.drawable.dashboard, R.string.dashboard),
        NavigationItem(R.drawable.inventory, R.string.inventory, isParent = true),
        NavigationItem(R.drawable.supplier, R.string.supplier, isParent = true),
        NavigationItem(R.drawable.user, R.string.user),
        NavigationItem(R.drawable.report, R.string.report),
        NavigationItem(R.drawable.pos, R.string.pos)
    )
}

fun getDrawerSubItems(): List<NavigationItem> {
    return listOf(
        NavigationItem(R.drawable.product, R.string.product, R.string.inventory),
        NavigationItem(R.drawable.branch, R.string.branch, R.string.inventory),
        NavigationItem(R.drawable.category, R.string.category, R.string.inventory),
        NavigationItem(R.drawable.contact, R.string.contact, R.string.supplier),
        NavigationItem(R.drawable.purchase_order, R.string.purchase_order, R.string.supplier),
        NavigationItem(R.drawable.return_order, R.string.return_order, R.string.supplier),
    )
}