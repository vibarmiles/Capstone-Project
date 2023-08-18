package com.example.capstoneproject.global.ui.navigation

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.capstoneproject.AppSplashScreen
import com.example.capstoneproject.R
import com.example.capstoneproject.product_management.ui.category.CategoryScreen
import com.example.capstoneproject.product_management.ui.product.composable.ProductScreen
import com.example.capstoneproject.user_management.ui.add_users.composable.AddEditUserScreen
import com.example.capstoneproject.user_management.ui.users.composable.UserScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavigationHost(navController: NavHostController, scope: CoroutineScope, scaffoldState: ScaffoldState) {
    NavHost(navController = navController, startDestination = Routes.SplashScreen.route) {
        composable(Routes.SplashScreen.route) {
            AppSplashScreen {
                navController.navigate(Routes.Category.route) {
                    popUpTo(0)
                }
            }
        }
        composable(Routes.Dashboard.route) {

        }

        composable(Routes.Product.route) {
            ProductScreen(scope = scope, scaffoldState = scaffoldState)
        }

        composable(Routes.Branch.route) {

        }

        composable(Routes.Category.route) {
            CategoryScreen(scope = scope, scaffoldState = scaffoldState)
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
                backStackEntry ->
            AddEditUserScreen(decision = stringResource(id = R.string.edit), back = { navController.popBackStack() }, userId = backStackEntry.arguments?.getString("userId") ?: "")
        }
    }
}