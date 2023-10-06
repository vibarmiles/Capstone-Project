package com.example.capstoneproject.global.ui.navigation

import com.example.capstoneproject.R

sealed class Routes(val route: String) {
    object SplashScreen : Routes("Splash")
    object Dashboard : Routes((R.string.dashboard).toString())
    object Product : Routes((R.string.product).toString()) {
        object View : Routes(this.route + "/View/{productId}/{name}/{image}/{purchasePrice}/{sellingPrice}/{supplier}/{categoryId}/{criticalLevel}/{stock}") {
            fun createRoute(productId: String, name: String, image: String, purchasePrice: Double, sellingPrice: Double, supplier: String, categoryId: String, criticalLevel: Int, stock: String) = Routes.Product.route + "/View/$productId/$name/$image/$purchasePrice/$sellingPrice/$supplier/$categoryId/$criticalLevel/$stock"
        }
        object Add : Routes(this.route + "/Add")
        object Set : Routes(this.route + "/Set/{productId}/{stock}") {
            fun createRoute(productId: String, stock: String) = Routes.Product.route + "/Set/$productId/$stock"
        }
        object Edit : Routes(this.route + "/Edit/{productId}/{name}/{image}/{purchasePrice}/{sellingPrice}/{supplier}/{categoryId}/{criticalLevel}/{stock}") {
            fun createRoute(productId: String, name: String, image: String, purchasePrice: Double, sellingPrice: Double, supplier: String, categoryId: String, criticalLevel: Int, stock: String) = Routes.Product.route + "/Edit/$productId/$name/$image/$purchasePrice/$sellingPrice/$supplier/$categoryId/$criticalLevel/$stock"
        }
    }
    object Branch : Routes((R.string.branch).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{branchId}/{branchName}/{branchAddress}") {
            fun createRoute(branchId: String, branchName: String, branchAddress: String) = Routes.Branch.route + "/Edit/$branchId/$branchName/$branchAddress"
        }
    }
    object Category : Routes((R.string.category).toString())
    object Contact : Routes((R.string.contact).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{contactId}/{contactName}/{contactNumber}") {
            fun createRoute(contactId: String, contactName: String, contactNumber: String) = Routes.Contact.route + "/Edit/$contactId/$contactName/$contactNumber"
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
