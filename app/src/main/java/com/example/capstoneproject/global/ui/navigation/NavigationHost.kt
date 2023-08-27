package com.example.capstoneproject.global.ui.navigation

import android.app.Application
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.capstoneproject.AppSplashScreen
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.viewmodel.AppViewModel
import com.example.capstoneproject.product_management.data.Room.branch.Branch
import com.example.capstoneproject.product_management.data.Room.product.Product
import com.example.capstoneproject.product_management.ui.branch.BranchFormScreen
import com.example.capstoneproject.product_management.ui.branch.BranchScreen
import com.example.capstoneproject.product_management.ui.branch.viewmodel.BranchViewModel
import com.example.capstoneproject.product_management.ui.branch.viewmodel.BranchViewModelFactory
import com.example.capstoneproject.product_management.ui.category.CategoryScreen
import com.example.capstoneproject.product_management.ui.category.viewmodel.CategoryViewModel
import com.example.capstoneproject.product_management.ui.category.viewmodel.CategoryViewModelFactory
import com.example.capstoneproject.product_management.ui.product.ProductFormSreen
import com.example.capstoneproject.product_management.ui.product.ProductScreen
import com.example.capstoneproject.product_management.ui.product.viewModel.ProductViewModel
import com.example.capstoneproject.product_management.ui.product.viewModel.ProductViewModelFactory
import com.example.capstoneproject.user_management.ui.add_users.composable.AddEditUserScreen
import com.example.capstoneproject.user_management.ui.users.composable.UserScreen
import kotlinx.coroutines.CoroutineScope
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavigationHost(navController: NavHostController, scope: CoroutineScope, scaffoldState: ScaffoldState, appViewModel: AppViewModel) {
    val application: Application = LocalContext.current.applicationContext as Application
    var screenViewModel: AndroidViewModel

    NavHost(navController = navController, startDestination = Routes.SplashScreen.route) {
        composable(Routes.SplashScreen.route) {
            AppSplashScreen {
                navController.navigate(Routes.Product.route) {
                    popUpTo(0)
                }
                appViewModel.isLoading.value = false
            }
        }
        composable(Routes.Dashboard.route) {

        }

        composable(Routes.Product.route) {
            screenViewModel = viewModel(factory = ProductViewModelFactory(application))
            ProductScreen(scope = scope, scaffoldState = scaffoldState, viewModel = screenViewModel as ProductViewModel, edit = {
                id, productName, image, price, category, quantity -> navController.navigate(Routes.Product.Edit.createRoute(productId = id, name = productName, image = URLEncoder.encode(image, StandardCharsets.UTF_8.toString()), price = price, categoryId = category, quantity = quantity))
            }, add = { navController.navigate(Routes.Product.Add.route) })
        }

        composable(Routes.Product.Add.route) {
            screenViewModel = viewModel(factory = ProductViewModelFactory(application))
            ProductFormSreen(function = "Add", viewModel = screenViewModel as ProductViewModel) {
                navController.popBackStack()
            }
        }

        composable(Routes.Product.Edit.route) {
            screenViewModel = viewModel(factory = ProductViewModelFactory(application))
            val productId: Int = it.arguments?.getString("productId")!!.toInt()
            val image: String = it.arguments?.getString("image")!!
            val productName: String = it.arguments?.getString("name")!!
            val price: Double = it.arguments?.getString("price")!!.toDouble()
            val category: Int = it.arguments?.getString("categoryId")!!.toInt()
            val quantity: Int = it.arguments?.getString("quantity")!!.toInt()
            ProductFormSreen(function = "Edit", viewModel = screenViewModel as ProductViewModel, product = Product(id = productId, image = image, productName = productName, price = price, category = category, quantity = quantity)) {
                navController.popBackStack()
            }
        }

        composable(Routes.Branch.route) {
            screenViewModel = viewModel(factory = BranchViewModelFactory(application))
            BranchScreen(scope = scope, scaffoldState = scaffoldState, screenViewModel as BranchViewModel, add = { navController.navigate(Routes.Branch.Add.route) }) {
                id, name, address -> navController.navigate(Routes.Branch.Edit.createRoute(id, name, address))
            }
        }

        composable(Routes.Branch.Add.route) {
            screenViewModel = viewModel(factory = BranchViewModelFactory(application))
            BranchFormScreen(screenViewModel as BranchViewModel) {
                navController.popBackStack()
            }
        }

        composable((Routes.Branch.Edit.route)) {
            screenViewModel = viewModel(factory = BranchViewModelFactory(application))
            val id: Int = it.arguments?.getString("branchId")!!.toInt()
            val name: String = it.arguments?.getString("branchName")!!
            val address: String = it.arguments?.getString("branchAddress")!!
            BranchFormScreen(viewModel = screenViewModel as BranchViewModel, branch = Branch(id = id, branchName = name, address = address), function = "Edit") {
                navController.popBackStack()
            }
        }

        composable(Routes.Category.route) {
            screenViewModel = viewModel(factory = CategoryViewModelFactory(LocalContext.current.applicationContext as Application))
            CategoryScreen(scope = scope, scaffoldState = scaffoldState, viewModel = screenViewModel as CategoryViewModel)
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
                it ->
            AddEditUserScreen(decision = stringResource(id = R.string.edit), back = { navController.popBackStack() }, userId = it.arguments?.getString("userId") ?: "")
        }
    }
}