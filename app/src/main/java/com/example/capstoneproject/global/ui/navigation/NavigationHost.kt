package com.example.capstoneproject.global.ui.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.viewmodel.AppViewModel
import com.example.capstoneproject.login.data.login.GoogleLoginRepository
import com.example.capstoneproject.product_management.ui.branch.BranchFormScreen
import com.example.capstoneproject.product_management.ui.branch.BranchScreen
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.product_management.ui.category.CategoryScreen
import com.example.capstoneproject.product_management.ui.category.CategoryViewModel
import com.example.capstoneproject.product_management.ui.product.*
import com.example.capstoneproject.supplier_management.ui.contact.ContactFormScreen
import com.example.capstoneproject.supplier_management.ui.contact.ContactScreen
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.supplier_management.ui.purchase_order.PurchaseOrderForm
import com.example.capstoneproject.supplier_management.ui.purchase_order.PurchaseOrderScreen
import com.example.capstoneproject.supplier_management.ui.purchase_order.PurchaseOrderViewModel
import com.example.capstoneproject.login.ui.login.LoginScreen
import com.example.capstoneproject.supplier_management.ui.purchase_order.ViewPurchaseOrder
import com.example.capstoneproject.user_management.ui.users.UserForm
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import com.example.capstoneproject.user_management.ui.users.UserScreen
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationHost(navController: NavHostController, scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: AppViewModel) {
    val branchViewModel: BranchViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val purchaseOrderViewModel: PurchaseOrderViewModel = viewModel()
    val contactViewModel: ContactViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current

    val googleAuthUiClient by lazy {
        GoogleLoginRepository(context = context, oneTapClient = Identity.getSignInClient(context))
    }

    NavHost(navController = navController, startDestination = Routes.LoginScreen.route) {
        composable(Routes.LoginScreen.route) {
            val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult(), onResult = { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    scope.launch {
                        val signInResult = googleAuthUiClient.getSignInResult(
                            intent = result.data ?: return@launch
                        )

                        viewModel.user.value = signInResult
                    }
                }
            })

            LoginScreen(signedIn = viewModel.user.value.data != null, signIn = {
                if (viewModel.user.value.data != null) {
                    userViewModel.getUser(viewModel.user.value.data!!.email) {
                        if (it) {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("Logged In Successfully! $it", duration = SnackbarDuration.Short)
                            }

                            navController.navigate(Routes.Dashboard.route) {
                                popUpTo(0)
                            }

                            viewModel.isLoading.value = false
                        }
                    }
                }
            }) {
                navController.navigate(Routes.Dashboard.route) {
                    popUpTo(0)
                }
                viewModel.isLoading.value = false
                /*scope.launch {
                    val signIn = googleAuthUiClient.signIn()
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            signIn ?: return@launch
                        ).build()
                    )
                }*/
            }
        }
        composable(Routes.Dashboard.route) {
            Column {
                Text(text = viewModel.user.value.data?.profilePicture.toString())
                Text(text = viewModel.user.value.data?.username.toString())
                Text(text = viewModel.connection.value.toString())
            }
        }

        composable(Routes.Product.route) {
            ProductScreen(scope = scope, scaffoldState = scaffoldState, branchViewModel = branchViewModel, productViewModel = productViewModel, categoryViewModel = categoryViewModel, set = {
                id -> navController.navigate(Routes.Product.Set.createRoute(id))
            }, add = { navController.navigate(Routes.Product.Add.route) }, edit = {
                id -> navController.navigate(Routes.Product.Edit.createRoute(id))
            }, view = {
                id -> navController.navigate(Routes.Product.View.createRoute(id))
            })
        }

        composable(Routes.Product.View.route) {
            val productId: String = it.arguments?.getString("productId")!!

            ViewProduct(dismissRequest = { navController.popBackStack() }, productViewModel = productViewModel, contactViewModel = contactViewModel, categoryViewModel = categoryViewModel, branchViewModel = branchViewModel, productId = productId, edit = {
                navController.navigate(Routes.Product.Edit.createRoute(productId))
            }, set = {
                navController.navigate(Routes.Product.Set.createRoute(productId))
            }, delete = {
                navController.popBackStack()
            })
        }

        composable(Routes.Product.Add.route) {
            ProductForm(function = "Add", productViewModel = productViewModel, contactViewModel = contactViewModel, categoryViewModel = categoryViewModel, dismissRequest = {
                navController.popBackStack()
            })
        }

        composable(Routes.Product.Edit.route) {
            val productId: String = it.arguments?.getString("productId")!!

            ProductForm(function = "Edit", productViewModel = productViewModel, categoryViewModel = categoryViewModel, contactViewModel = contactViewModel, productId = productId, dismissRequest = {
                navController.popBackStack()
            })
        }

        composable(Routes.Product.Set.route) {
            val productId: String = it.arguments?.getString("productId")!!

            ProductQuantityFormScreen(productViewModel = productViewModel, branchViewModel = branchViewModel, productId = productId, dismissRequest = {
                navController.popBackStack()
            })
        }

        composable(Routes.Branch.route) {
            BranchScreen(scope = scope, scaffoldState = scaffoldState, viewModel = branchViewModel, productViewModel = productViewModel, add = { navController.navigate(Routes.Branch.Add.route) }) {
                branch -> navController.navigate(Routes.Branch.Edit.createRoute(branchId = branch.id))
            }
        }

        composable(Routes.Branch.Add.route) {
            BranchFormScreen(viewModel = branchViewModel) {
                navController.popBackStack()
            }
        }

        composable((Routes.Branch.Edit.route)) {
            val id: String = it.arguments?.getString("branchId")!!

            BranchFormScreen(viewModel = branchViewModel, function = "Edit", id = id) {
                navController.popBackStack()
            }
        }

        composable(Routes.Category.route) {
            CategoryScreen(scope = scope, scaffoldState = scaffoldState, viewModel = categoryViewModel, productViewModel = productViewModel)
        }

        composable(Routes.Contact.route) {
            ContactScreen(scope = scope, scaffoldState = scaffoldState, contactViewModel = contactViewModel, edit = {
                contact -> navController.navigate(Routes.Contact.Edit.createRoute(contactId = contact.id))
            }) {
                navController.navigate(Routes.Contact.Add.route)
            }
        }

        composable(Routes.Contact.Add.route) {
            ContactFormScreen(function = "Add", contactViewModel = contactViewModel) {
                navController.popBackStack()
            }
        }

        composable(Routes.Contact.Edit.route) {
            val id: String = it.arguments?.getString("contactId")!!

            ContactFormScreen(function = "Edit", contactViewModel = contactViewModel, id = id) {
                navController.popBackStack()
            }
        }

        composable(Routes.PurchaseOrder.route) {
            PurchaseOrderScreen(scope = scope, scaffoldState = scaffoldState, purchaseOrderViewModel = purchaseOrderViewModel, add = { navController.navigate(Routes.PurchaseOrder.Add.route) }, view = { navController.navigate(Routes.PurchaseOrder.View.createRoute(it)) })
        }

        composable(Routes.PurchaseOrder.Add.route) {
            PurchaseOrderForm(contactViewModel = contactViewModel, purchaseOrderViewModel = purchaseOrderViewModel, productViewModel = productViewModel) {
                navController.popBackStack()
            }
        }

        composable(Routes.PurchaseOrder.View.route) {
            val id = it.arguments?.getString("POID")!!

            ViewPurchaseOrder(purchaseOrderId = id, purchaseOrderViewModel = purchaseOrderViewModel) {
                navController.popBackStack()
            }
        }

        composable(Routes.ReturnOrder.route) {

        }

        composable(Routes.User.route) {
            UserScreen(scope = scope, scaffoldState = scaffoldState, userViewModel = userViewModel, add = { navController.navigate(Routes.User.Add.route) }) {
                navController.navigate(it)
            }
        }

        composable(Routes.Report.route) {

        }

        composable(Routes.POS.route) {

        }

        composable(Routes.User.Add.route) {
            UserForm(userViewModel = userViewModel, decision = stringResource(id = R.string.add), back = { navController.popBackStack() })
        }

        composable(Routes.User.Edit.route) {
            UserForm(userViewModel = userViewModel, decision = stringResource(id = R.string.edit), back = { navController.popBackStack() }, userId = it.arguments?.getString("userId")!!)
        }
    }
}