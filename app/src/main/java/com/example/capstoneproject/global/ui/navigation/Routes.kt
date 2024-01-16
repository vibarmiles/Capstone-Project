package com.example.capstoneproject.global.ui.navigation

import com.example.capstoneproject.R
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType

sealed class Routes(val route: String) {
    object LoginScreen : Routes((R.string.login).toString())
    object Dashboard : Routes((R.string.dashboard).toString())
    object Product : Routes((R.string.product).toString()) {
        object View : Routes("$route/View/{productId}") {
            fun createRoute(productId: String) = Product.route + "/View/$productId"
        }
        object Add : Routes("$route/Add")
        object SetBranchQuantity : Routes("$route/SetBranchQuantity/{productId}") {
            fun createRoute(productId: String) = Product.route + "/SetBranchQuantity/$productId"
        }
        object SetMonthlySales : Routes("$route/SetMonthlySales/{productId}") {
            fun createRoute(productId: String) = Product.route + "/SetMonthlySales/$productId"
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
        object Edit : Routes(this.route + "/Edit/{POID}") {
            fun createRoute(POID: String) = PurchaseOrder.route + "/Edit/$POID"
        }
        object View : Routes(this.route + "/View/{POID}") {
            fun createRoute(POID: String) = PurchaseOrder.route + "/View/$POID"
        }
        object CreateFromInventory : Routes(this.route + "/Create/{productId}") {
            fun createRoute(productId: String) = PurchaseOrder.route + "/Create/$productId"
        }
    }
    object ReturnOrder : Routes((R.string.return_order).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{ROID}") {
            fun createRoute(ROID: String) = ReturnOrder.route + "/Edit/$ROID"
        }
        object View : Routes(this.route + "/View/{ROID}") {
            fun createRoute(ROID: String) = ReturnOrder.route + "/View/$ROID"
        }
    }
    object TransferOrder : Routes((R.string.transfer_order).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{TOID}") {
            fun createRoute(TOID: String) = TransferOrder.route + "/Edit/$TOID"
        }
        object View : Routes(this.route + "/View/{TOID}") {
            fun createRoute(TOID: String) = TransferOrder.route + "/View/$TOID"
        }
    }
    object User : Routes((R.string.user).toString()) {
        object Add : Routes( this.route + "/Add")
        object Edit : Routes( this.route + "/Edit/{userId}") {
            fun createRoute(userId: String) = User.route + "/Edit/$userId"
        }
    }
    object ActivityLogs : Routes((R.string.activity_logs).toString())
    object Report : Routes((R.string.report).toString()) {
        object View : Routes(this.route + "/View/{year}/{month}") {
            fun createRoute(month: String, year: Int) = Report.route + "/View/$year/$month"
        }
    }
    object POS : Routes((R.string.pos).toString()) {
        object Add : Routes(this.route + "/Add")
        object Edit : Routes(this.route + "/Edit/{SIID}/{type}") {
            fun createRoute(SIID: String, type: InvoiceType) = POS.route + "/Edit/$SIID/$type"
        }
        object View : Routes(this.route + "/View/{SIID}") {
            fun createRoute(SIID: String) = POS.route + "/View/$SIID"
        }
        object RnE : Routes(this.route + "/RnE/{SIID}") {
            fun createRoute(SIID: String) = POS.route + "/RnE/$SIID"
        }
    }
    object Logout : Routes((R.string.logout).toString())
}
