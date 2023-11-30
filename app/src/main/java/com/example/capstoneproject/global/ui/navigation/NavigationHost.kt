package com.example.capstoneproject.global.ui.navigation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.capstoneproject.R
import com.example.capstoneproject.activity_logs.ui.ActivityLogsScreen
import com.example.capstoneproject.activity_logs.ui.ActivityLogsViewModel
import com.example.capstoneproject.dashboard.ui.Dashboard
import com.example.capstoneproject.global.ui.viewmodel.AppViewModel
import com.example.capstoneproject.login.data.login.GoogleLoginRepository
import com.example.capstoneproject.login.data.login.SignInResult
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
import com.example.capstoneproject.point_of_sales.ui.*
import com.example.capstoneproject.supplier_management.ui.purchase_order.ViewPurchaseOrder
import com.example.capstoneproject.supplier_management.ui.return_order.ReturnOrderForm
import com.example.capstoneproject.supplier_management.ui.return_order.ReturnOrderScreen
import com.example.capstoneproject.supplier_management.ui.return_order.ReturnOrderViewModel
import com.example.capstoneproject.supplier_management.ui.return_order.ViewReturnOrder
import com.example.capstoneproject.supplier_management.ui.transfer_order.TransferOrderForm
import com.example.capstoneproject.supplier_management.ui.transfer_order.TransferOrderScreen
import com.example.capstoneproject.supplier_management.ui.transfer_order.TransferOrderViewModel
import com.example.capstoneproject.supplier_management.ui.transfer_order.ViewTransferOrder
import com.example.capstoneproject.user_management.ui.users.UserForm
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import com.example.capstoneproject.user_management.ui.users.UserScreen
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationHost(
    navController: NavHostController,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    viewModel: AppViewModel,
    userViewModel: UserViewModel,
    callback: (Int) -> Unit
) {
    val branchViewModel: BranchViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val purchaseOrderViewModel: PurchaseOrderViewModel = viewModel()
    val returnOrderViewModel: ReturnOrderViewModel = viewModel()
    val transferOrderViewModel: TransferOrderViewModel = viewModel()
    val contactViewModel: ContactViewModel = viewModel()
    val posViewModel: POSViewModel = viewModel()
    val activityLogsRepository: ActivityLogsViewModel = viewModel()
    val context = LocalContext.current
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()

    val googleAuthUiClient by lazy {
        GoogleLoginRepository(context = context, oneTapClient = Identity.getSignInClient(context))
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LoginScreen.route
    ) {
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

            LoginScreen {
                /*navController.navigate(Routes.Dashboard.route) {
                    popUpTo(0)
                }
                viewModel.isLoading.value = false*/
                scope.launch {
                    val signIn = googleAuthUiClient.signIn()
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            signIn ?: return@launch
                        ).build()
                    )
                }
            }
        }

        composable(Routes.Dashboard.route) {
            Dashboard(
                scope = scope,
                scaffoldState = scaffoldState,
                branchViewModel = branchViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel,
                purchaseOrderViewModel = purchaseOrderViewModel,
                goToBranches = { callback.invoke(R.string.branch) },
                goToProducts = { callback.invoke(R.string.product) },
                goToPO = { callback.invoke(R.string.purchase_order) },
                goToPOS = { callback.invoke(R.string.pos) }
            )
        }

        composable(Routes.Product.route) {
            ProductScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                branchViewModel = branchViewModel,
                contactViewModel = contactViewModel,
                productViewModel = productViewModel,
                categoryViewModel = categoryViewModel,
                userViewModel = userViewModel,
                add = { navController.navigate(Routes.Product.Add.route) },
                view = { id -> navController.navigate(Routes.Product.View.createRoute(id)) }
            )
        }

        composable(Routes.Product.View.route) {
            val productId: String = it.arguments?.getString("productId")!!

            ViewProduct(
                dismissRequest = { navController.popBackStack() },
                productViewModel = productViewModel,
                contactViewModel = contactViewModel,
                categoryViewModel = categoryViewModel,
                branchViewModel = branchViewModel,
                userLevel = userAccountDetails.value.userLevel,
                productId = productId,
                edit = { navController.navigate(Routes.Product.Edit.createRoute(productId)) },
                set = { navController.navigate(Routes.Product.Set.createRoute(productId)) },
                delete = { navController.popBackStack() }
            )
        }

        composable(Routes.Product.Add.route) {
            ProductForm(
                scaffoldState = scaffoldState,
                function = "Add",
                productViewModel = productViewModel,
                contactViewModel = contactViewModel,
                categoryViewModel = categoryViewModel,
                dismissRequest = { navController.popBackStack() },
                userViewModel = userViewModel
            )
        }

        composable(Routes.Product.Edit.route) {
            val productId: String = it.arguments?.getString("productId")!!

            ProductForm(
                scaffoldState = scaffoldState,
                function = "Edit",
                productViewModel = productViewModel,
                categoryViewModel = categoryViewModel,
                contactViewModel = contactViewModel,
                productId = productId,
                dismissRequest = { navController.popBackStack() },
                userViewModel = userViewModel
            )
        }

        composable(Routes.Product.Set.route) {
            val productId: String = it.arguments?.getString("productId")!!

            ProductQuantityFormScreen(
                productViewModel = productViewModel,
                branchViewModel = branchViewModel,
                productId = productId,
                dismissRequest = { navController.popBackStack() },
                userViewModel = userViewModel
            )
        }

        composable(Routes.Branch.route) {
            BranchScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                viewModel = branchViewModel,
                productViewModel = productViewModel,
                add = { navController.navigate(Routes.Branch.Add.route) }
            ) { branch ->
                navController.navigate(Routes.Branch.Edit.createRoute(branchId = branch.id))
            }
        }

        composable(Routes.Branch.Add.route) {
            BranchFormScreen(
                viewModel = branchViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable((Routes.Branch.Edit.route)) {
            val id: String = it.arguments?.getString("branchId")!!

            BranchFormScreen(
                viewModel = branchViewModel,
                function = "Edit",
                id = id,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.Category.route) {
            CategoryScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                viewModel = categoryViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel
            )
        }

        composable(Routes.Contact.route) {
            ContactScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                contactViewModel = contactViewModel,
                edit = { contact -> navController.navigate(Routes.Contact.Edit.createRoute(contactId = contact.id)) }
            ) {
                navController.navigate(Routes.Contact.Add.route)
            }
        }

        composable(Routes.Contact.Add.route) {
            ContactFormScreen(
                function = "Add",
                contactViewModel = contactViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.Contact.Edit.route) {
            val id: String = it.arguments?.getString("contactId")!!

            ContactFormScreen(
                function = "Edit",
                contactViewModel = contactViewModel,
                userViewModel = userViewModel,
                id = id
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.PurchaseOrder.route) {
            PurchaseOrderScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                purchaseOrderViewModel = purchaseOrderViewModel,
                add = { navController.navigate(Routes.PurchaseOrder.Add.route) },
                view = { navController.navigate(Routes.PurchaseOrder.View.createRoute(it)) }
            )
        }

        composable(Routes.PurchaseOrder.Add.route) {
            PurchaseOrderForm(
                contactViewModel = contactViewModel,
                purchaseOrderViewModel = purchaseOrderViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.PurchaseOrder.View.route) {
            val id = it.arguments?.getString("POID")!!

            ViewPurchaseOrder(
                purchaseOrderId = id,
                purchaseOrderViewModel = purchaseOrderViewModel,
                productViewModel = productViewModel,
                branchViewModel = branchViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.ReturnOrder.route) {
            ReturnOrderScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                returnOrderViewModel = returnOrderViewModel,
                add = { navController.navigate(Routes.ReturnOrder.Add.route) },
                view = { navController.navigate(Routes.ReturnOrder.View.createRoute(it)) }
            )
        }

        composable(Routes.ReturnOrder.Add.route) {
            ReturnOrderForm(
                contactViewModel = contactViewModel,
                returnOrderViewModel = returnOrderViewModel,
                branchViewModel = branchViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.ReturnOrder.View.route) {
            val id = it.arguments?.getString("ROID")!!

            ViewReturnOrder(
                returnOrderId = id,
                returnOrderViewModel = returnOrderViewModel,
                productViewModel = productViewModel,
                branchViewModel = branchViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.TransferOrder.route) {
            TransferOrderScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                transferOrderViewModel = transferOrderViewModel,
                branchViewModel = branchViewModel,
                add = { navController.navigate(Routes.TransferOrder.Add.route) },
                view = { navController.navigate(Routes.TransferOrder.View.createRoute(it)) }
            )
        }

        composable(Routes.TransferOrder.Add.route) {
            TransferOrderForm(
                contactViewModel = contactViewModel,
                transferOrderViewModel = transferOrderViewModel,
                branchViewModel = branchViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.TransferOrder.View.route) {
            val id = it.arguments?.getString("TOID")!!

            ViewTransferOrder(
                transferOrderId = id,
                transferOrderViewModel = transferOrderViewModel,
                productViewModel = productViewModel,
                branchViewModel = branchViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.User.route) {
            UserScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                userViewModel = userViewModel,
                add = { navController.navigate(Routes.User.Add.route) }
            ) {
                navController.navigate(Routes.User.Edit.createRoute(userId = it))
            }
        }

        composable(Routes.ActivityLogs.route) {
            ActivityLogsScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                activityLogsViewModel = activityLogsRepository,
                userViewModel = userViewModel
            )
        }

        composable(Routes.Report.route) {

        }

        composable(Routes.POS.route) {
            POSScreen(
                scope = scope,
                scaffoldState = scaffoldState,
                posViewModel = posViewModel,
                branchViewModel = branchViewModel,
                add = { navController.navigate(Routes.POS.Add.route) },
                view = { navController.navigate(Routes.POS.View.createRoute(it)) }
            )
        }

        composable(Routes.POS.Add.route) {
            POSForm(
                userId = userAccountDetails.value.id,
                posViewModel = posViewModel,
                contactViewModel = contactViewModel,
                branchViewModel = branchViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.POS.View.route) {
            val id = it.arguments?.getString("SIID")!!

            ViewInvoice(
                invoiceId = id,
                posViewModel = posViewModel,
                userViewModel = userViewModel,
                productViewModel = productViewModel,
                branchViewModel = branchViewModel,
                returnAndExchange = {
                    navController.navigate(Routes.POS.RnE.createRoute(id))
                }
            ) {
                navController.popBackStack()
            }
        }

        composable(Routes.POS.RnE.route) {
            val id = it.arguments?.getString("SIID")!!
            Log.e("SALES INVOICE ID", id)

            ReturnAndExchangeForm(
                userId = userAccountDetails.value.id,
                invoiceId = id,
                posViewModel = posViewModel,
                returnOrderViewModel = returnOrderViewModel,
                contactViewModel = contactViewModel,
                productViewModel = productViewModel,
                userViewModel = userViewModel
            ) {
                callback.invoke(R.string.pos)
            }
        }

        composable(Routes.User.Add.route) {
            UserForm(
                userViewModel = userViewModel,
                branchViewModel = branchViewModel,
                decision = stringResource(id = R.string.add),
                back = { navController.popBackStack() }
            )
        }

        composable(Routes.User.Edit.route) {
            UserForm(
                userViewModel = userViewModel,
                branchViewModel = branchViewModel,
                decision = stringResource(id = R.string.edit),
                back = { navController.popBackStack() },
                userId = it.arguments?.getString("userId")!!
            )
        }

        composable(Routes.Logout.route) {
            LaunchedEffect(key1 = Unit) {
                callback.invoke(R.string.login)
                viewModel.user.value = SignInResult(data = null, errorMessage = null)
                userViewModel.logout()
                viewModel.isLoading.value = true
                googleAuthUiClient.signOut()
                scaffoldState.snackbarHostState.showSnackbar(message = "Signed Out Successfully!", duration = SnackbarDuration.Short)
            }
        }
    }
}