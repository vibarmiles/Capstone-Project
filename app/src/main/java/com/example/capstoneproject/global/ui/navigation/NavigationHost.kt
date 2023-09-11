package com.example.capstoneproject.global.ui.navigation

import android.util.Log
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.capstoneproject.AppSplashScreen
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.viewmodel.AppViewModel
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchFormScreen
import com.example.capstoneproject.product_management.ui.branch.BranchScreen
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.category.CategoryScreen
import com.example.capstoneproject.product_management.ui.category.CategoryViewModel
import com.example.capstoneproject.product_management.ui.product.ProductFormSreen
import com.example.capstoneproject.product_management.ui.product.ProductQuantityFormScreen
import com.example.capstoneproject.product_management.ui.product.ProductScreen
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.ui.contact.ContactFormScreen
import com.example.capstoneproject.supplier_management.ui.contact.ContactScreen
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.supplier_management.ui.contact.OfferedProductScreen
import com.example.capstoneproject.user_management.ui.add_users.composable.AddEditUserScreen
import com.example.capstoneproject.user_management.ui.users.composable.UserScreen
import kotlinx.coroutines.CoroutineScope
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavigationHost(navController: NavHostController, scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: AppViewModel) {
    val branchViewModel: BranchViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    var contactViewModel: ContactViewModel? = null

    NavHost(navController = navController, startDestination = Routes.SplashScreen.route) {
        composable(Routes.SplashScreen.route) {
            AppSplashScreen {
                navController.navigate(Routes.Dashboard.route) {
                    popUpTo(0)
                }
                viewModel.isLoading.value = false
            }
        }
        composable(Routes.Dashboard.route) {

        }

        composable(Routes.Product.route) {
            ProductScreen(scope = scope, scaffoldState = scaffoldState, branchViewModel = branchViewModel, productViewModel = productViewModel, categoryViewModel = categoryViewModel, edit = {
                id, productName, image, price, category, stock -> navController.navigate(Routes.Product.Edit.createRoute(productId = id, name = productName, image = URLEncoder.encode(image ?: "null", StandardCharsets.UTF_8.toString()), price = price, categoryId = category ?: "null", stock = stock))
            }, set = { id, stock -> navController.navigate(Routes.Product.Set.createRoute(id, stock)) }, add = { navController.navigate(Routes.Product.Add.route) })
        }

        composable(Routes.Product.Add.route) {
            ProductFormSreen(function = "Add", productViewModel = productViewModel, categoryViewModel = categoryViewModel) {
                navController.popBackStack()
            }
        }

        composable(Routes.Product.Edit.route) {
            val productId: String = it.arguments?.getString("productId")!!
            val image: String? = if (it.arguments?.getString("image")!! == "null") null else it.arguments?.getString("image")!!
            val productName: String = it.arguments?.getString("name")!!
            val price: Double = it.arguments?.getString("price")!!.toDouble()
            val category: String? = if (it.arguments?.getString("categoryId")!! == "null") null else it.arguments?.getString("categoryId")!!
            val stock: String = it.arguments?.getString("stock")!!
            val input: String = stock.substring(1, stock.length - 1)
            var map: Map<String, Int>? = null
            if (input.isNotBlank()) {
                map = input.split(", ").associate { value -> val split = value.split("="); split[0] to split[1].toInt() }
            }
            ProductFormSreen(function = "Edit", productViewModel = productViewModel, categoryViewModel = categoryViewModel, productId = productId, product = Product(image = image, productName = productName, price = price, category = category), map = map) {
                navController.popBackStack()
            }
        }

        composable(Routes.Product.Set.route) {
            val productId: String = it.arguments?.getString("productId")!!
            val stock: String = it.arguments?.getString("stock")!!
            val input: String = stock.substring(1, stock.length - 1)
            var map: Map<String, Int>? = null
            if (input.isNotBlank()) {
                map = input.split(", ").associate { value -> val split = value.split("="); split[0] to split[1].toInt() }
            }
            ProductQuantityFormScreen(productViewModel = productViewModel, branchViewModel = branchViewModel, productId = productId, map = map) {
                navController.popBackStack()
            }
        }

        composable(Routes.Branch.route) {
            BranchScreen(scope = scope, scaffoldState = scaffoldState, viewModel = branchViewModel, productViewModel = productViewModel, add = { navController.navigate(Routes.Branch.Add.route) }) {
                id, name, address -> navController.navigate(Routes.Branch.Edit.createRoute(id, name, address))
            }
        }

        composable(Routes.Branch.Add.route) {
            BranchFormScreen(viewModel = branchViewModel) {
                navController.popBackStack()
            }
        }

        composable((Routes.Branch.Edit.route)) {
            val id: String = it.arguments?.getString("branchId")!!
            val name: String = it.arguments?.getString("branchName")!!
            val address: String = it.arguments?.getString("branchAddress")!!
            BranchFormScreen(viewModel = branchViewModel, branch = Branch(id = id, name = name, address = address), function = "Edit") {
                navController.popBackStack()
            }
        }

        composable(Routes.Category.route) {
            CategoryScreen(scope = scope, scaffoldState = scaffoldState, viewModel = categoryViewModel, productViewModel = productViewModel)
        }

        composable(Routes.Contact.route) {
            if (!viewModel.viewModelLoaded.value) {
                viewModel.viewModelLoaded.value = true
                contactViewModel = viewModel()
            }
            
            ContactScreen(scope = scope, scaffoldState = scaffoldState, contactViewModel = contactViewModel!!, edit = {
                id, name, contact, product -> navController.navigate(Routes.Contact.Edit.createRoute(contactId = id, contactName = name, contactNumber = contact, product = product))
            }, set = {
                id, name, product -> navController.navigate(Routes.Contact.Set.createRoute(contactId = id, contactName = name, product = product))
            }) {
                navController.navigate(Routes.Contact.Add.route)
            }
        }

        composable(Routes.Contact.Add.route) {
            ContactFormScreen(function = "Add", contactViewModel = contactViewModel!!) {
                navController.popBackStack()
            }
        }

        composable(Routes.Contact.Set.route) {
            val id: String = it.arguments?.getString("contactId")!!
            val name: String = it.arguments?.getString("contactName")!!
            val product: String = it.arguments?.getString("product")!!
            val input: String = product.substring(1, product.length - 1)
            var map: Map<String, Double>? = null
            if (input.isNotBlank()) {
                map = input.split(", ").associate { value -> val split = value.split("="); split[0] to split[1].toDouble() }
            }

            OfferedProductScreen(contactViewModel = contactViewModel!!, productViewModel = productViewModel, contactId = id, contactName = name, product = map ?: mapOf()) {
                navController.popBackStack()
            }
        }

        composable(Routes.Contact.Edit.route) {
            val id: String = it.arguments?.getString("contactId")!!
            val name: String = it.arguments?.getString("contactName")!!
            val contact: String = it.arguments?.getString("contactNumber")!!
            val product: String = it.arguments?.getString("product")!!
            val input: String = product.substring(1, product.length - 1)
            var map: Map<String, Double>? = null
            if (input.isNotBlank()) {
                map = input.split(", ").associate { value -> val split = value.split("="); split[0] to split[1].toDouble() }
            }
            ContactFormScreen(function = "Edit", contactViewModel = contactViewModel!!, contactId = id, contact = Contact(name = name, contact = contact), map = map) {
                navController.popBackStack()
            }
        }

        composable(Routes.PurchaseOrder.route) {

        }

        composable(Routes.ReturnOrder.route) {

        }

        composable(Routes.User.route) {
            UserScreen(scope = scope, scaffoldState = scaffoldState, add = { navController.navigate(Routes.User.Add.route) }) {
                navController.navigate(it)
            }
        }

        composable(Routes.Report.route) {

        }

        composable(Routes.POS.route) {

        }

        composable(Routes.User.Add.route) {
            AddEditUserScreen(decision = stringResource(id = R.string.add), back = { navController.popBackStack() })
        }

        composable(Routes.User.Edit.route) {
            AddEditUserScreen(decision = stringResource(id = R.string.edit), back = { navController.popBackStack() }, userId = it.arguments?.getString("userId") ?: "")
        }
    }
}