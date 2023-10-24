package com.example.capstoneproject.global.ui.navigation

import com.example.capstoneproject.R

sealed class Routes(val route: String) {
    object LoginScreen : Routes("Login")
    object Dashboard : Routes((R.string.dashboard).toString())
    object Product : Routes((R.string.product).toString()) {
        object View : Routes("$route/View/{productId}") {
            fun createRoute(productId: String) = Product.route + "/View/$productId"
        }
        object Add : Routes("$route/Add")
        object Set : Routes("$route/Set/{productId}") {
            fun createRoute(productId: String) = Product.route + "/Set/$productId"
        }
        object Edit : Routes("$route/Edit/{productId}") {
            fun createRoute(productId: String) = Product.route + "/Edit/$productId"
        }
    }
    object Branch : Routes((R.string.branch).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{branchId}") {
            fun createRoute(branchId: String) = Branch.route + "/Edit/$branchId"
        }
    }
    object Category : Routes((R.string.category).toString())
    object Contact : Routes((R.string.contact).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{contactId}") {
            fun createRoute(contactId: String) = Contact.route + "/Edit/$contactId"
        }
    }
    object PurchaseOrder : Routes((R.string.purchase_order).toString()) {
        object Add : Routes(this.route + "/Add")
        object View : Routes(this.route + "/View/{POID}") {
            fun createRoute(POID: String) = Routes.PurchaseOrder.route + "/View/$POID"
        }
    }
    object ReturnOrder : Routes((R.string.return_order).toString())
    object User : Routes((R.string.user).toString()) {
        object Add : Routes( this.route + "/Add")
        object Edit : Routes( this.route + "/Edit/{userId}") {
            fun createRoute(userId: Int) = Routes.User.route + "/Edit/$userId"
        }
    }
    object Report : Routes((R.string.report).toString())
    object POS : Routes((R.string.pos).toString())
}
