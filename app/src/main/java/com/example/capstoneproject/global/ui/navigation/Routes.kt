package com.example.capstoneproject.global.ui.navigation

import com.example.capstoneproject.R

sealed class Routes(val route: String) {
    object SplashScreen : Routes("Splash")
    object Dashboard : Routes((R.string.dashboard).toString())
    object Product : Routes((R.string.product).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{productId}/{name}/{image}/{price}/{categoryId}/{quantity}") {
            fun createRoute(productId: String, name: String, image: String, price: Double, categoryId: String, quantity: Int) = Routes.Product.route + "/Edit/$productId/$name/$image/$price/$categoryId/$quantity"
        }
    }
    object Branch : Routes((R.string.branch).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{branchId}/{branchName}/{branchAddress}") {
            fun createRoute(branchId: String, branchName: String, branchAddress: String) = Routes.Branch.route + "/Edit/$branchId/$branchName/$branchAddress"
        }
    }
    object Category : Routes((R.string.category).toString())
    object Contact : Routes((R.string.contact).toString())
    object PurchaseOrder : Routes((R.string.purchase_order).toString())
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
