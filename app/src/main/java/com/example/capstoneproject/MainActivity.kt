package com.example.capstoneproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capstoneproject.global.ui.composable.Drawer
import com.example.capstoneproject.global.ui.list.Routes
import com.example.capstoneproject.product_management.ui.product.composable.ProductScreen
import com.example.capstoneproject.ui.theme.CapstoneProjectTheme
import com.example.capstoneproject.user_management.ui.add_users.composable.AddEditUserScreen
import com.example.capstoneproject.user_management.ui.users.composable.UserScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CapstoneProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    GlobalContent()
                }
            }
        }
    }
}

@Composable
fun GlobalContent() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    Scaffold(
        scaffoldState = scaffoldState,
        drawerScrimColor = Color.Black.copy(0.3f),
        drawerContent = { Drawer {
            navController.navigate(it.toString())
            scope.launch { scaffoldState.drawerState.close()
        }}},
        content = {
            it
            NavHost(navController = navController, startDestination = Routes.Dashboard.route) {
                composable(Routes.Dashboard.route) {  }
                composable(Routes.Product.route) { ProductScreen(scope = scope, scaffoldState = scaffoldState) }
                composable(Routes.Branch.route) {  }
                composable(Routes.Category.route) {  }
                composable(Routes.Contact.route) {  }
                composable(Routes.PurchaseOrder.route) {  }
                composable(Routes.ReturnOrder.route) {  }
                composable(Routes.User.route) { UserScreen(scope = scope, scaffoldState = scaffoldState, add = { navController.navigate(Routes.User.Add.route) }, edit = { navController.navigate(Routes.User.Edit.route) })}
                composable(Routes.Report.route) {  }
                composable(Routes.POS.route) {  }
                composable(Routes.User.Add.route) { AddEditUserScreen(decision = "Add", back = { back(navController) }) }
                composable(Routes.User.Edit.route) { AddEditUserScreen(decision = "Edit", back = { back(navController) }) }
            }
        }
    )
}

fun back(navController: NavController) {
    navController.popBackStack()
}