package com.example.capstoneproject.global.ui.navigation

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
import com.example.capstoneproject.product_management.ui.product.ProductScreen
import com.example.capstoneproject.product_management.ui.product.ProductViewModel
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

    NavHost(navController = navController, startDestination = Routes.SplashScreen.route) {
        composable(Routes.SplashScreen.route) {
            AppSplashScreen {
                navController.navigate(Routes.Product.route) {
                    popUpTo(0)
                }
                viewModel.isLoading.value = false
            }
        }
        composable(Routes.Dashboard.route) {

        }

        composable(Routes.Product.route) {
            ProductScreen(scope = scope, scaffoldState = scaffoldState, branchViewModel = branchViewModel, productViewModel = productViewModel, edit = {
                id, productName, image, price, category -> navController.navigate(Routes.Product.Edit.createRoute(
                productId = id, name = productName, image = URLEncoder.encode(image, StandardCharsets.UTF_8.toString()), price = price, categoryId = category))
            }, add = { navController.navigate(Routes.Product.Add.route) })
        }

        composable(Routes.Product.Add.route) {
            ProductFormSreen(function = "Add", productViewModel = productViewModel, categoryViewModel = categoryViewModel) {
                navController.popBackStack()
            }
        }

        composable(Routes.Product.Edit.route) {
            val productId: String = it.arguments?.getString("productId")!!
            val image: String = it.arguments?.getString("image")!!
            val productName: String = it.arguments?.getString("name")!!
            val price: Double = it.arguments?.getString("price")!!.toDouble()
            val category: String = it.arguments?.getString("categoryId")!!
            ProductFormSreen(function = "Edit", productViewModel = productViewModel, categoryViewModel = categoryViewModel, product = Product(id = productId, image = image, productName = productName, price = price, category = category)) {
                navController.popBackStack()
            }
        }

        composable(Routes.Branch.route) {
            BranchScreen(scope = scope, scaffoldState = scaffoldState, branchViewModel, add = { navController.navigate(Routes.Branch.Add.route) }) {
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
            CategoryScreen(scope = scope, scaffoldState = scaffoldState, viewModel = categoryViewModel)
        }

        composable(Routes.Contact.route) {

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