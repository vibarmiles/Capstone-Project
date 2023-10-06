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
import com.example.capstoneproject.product_management.ui.product.*
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.ui.contact.ContactFormScreen
import com.example.capstoneproject.supplier_management.ui.contact.ContactScreen
import com.example.capstoneproject.supplier_management.ui.contact.ContactViewModel
import com.example.capstoneproject.supplier_management.ui.purchase_order.PurchaseOrderForm
import com.example.capstoneproject.supplier_management.ui.purchase_order.PurchaseOrderScreen
import com.example.capstoneproject.supplier_management.ui.purchase_order.PurchaseOrderViewModel
import com.example.capstoneproject.user_management.ui.add_users.composable.AddEditUserScreen
import com.example.capstoneproject.user_management.ui.users.composable.UserScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavigationHost(navController: NavHostController, scope: CoroutineScope, scaffoldState: ScaffoldState, viewModel: AppViewModel) {
    val branchViewModel: BranchViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val purchaseOrderViewModel: PurchaseOrderViewModel = viewModel()
    val contactViewModel: ContactViewModel = viewModel()

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
            ProductScreen(scope = scope, scaffoldState = scaffoldState, branchViewModel = branchViewModel, productViewModel = productViewModel, categoryViewModel = categoryViewModel, set = { id, stock -> navController.navigate(Routes.Product.Set.createRoute(id, stock)) }, add = { navController.navigate(Routes.Product.Add.route) }, edit = {
                id, product -> navController.navigate(Routes.Product.Edit.createRoute(id, product.productName, product.image ?: "null", product.purchasePrice, product.sellingPrice, product.supplier, product.category ?: "null", product.criticalLevel, product.stock.toString()))
            }, view = {
                id, product -> navController.navigate(Routes.Product.View.createRoute(id, product.productName, product.image ?: "null", product.purchasePrice, product.sellingPrice, product.supplier, product.category ?: "null", product.criticalLevel, product.stock.toString()))
            })
        }

        composable(Routes.Product.View.route) {
            val productId: String = it.arguments?.getString("productId")!!
            val image: String? = if (it.arguments?.getString("image")!! == "null") null else it.arguments?.getString("image")!!
            val productName: String = it.arguments?.getString("name")!!
            val supplier: String = it.arguments?.getString("supplier")!!
            val sellingPrice: Double = it.arguments?.getString("sellingPrice")!!.toDouble()
            val purchasePrice: Double = it.arguments?.getString("purchasePrice")!!.toDouble()
            val category: String? = if (it.arguments?.getString("categoryId")!! == "null") null else it.arguments?.getString("categoryId")!!
            val criticalLevel: Int = it.arguments?.getString("criticalLevel")!!.toInt()
            val stock: String = it.arguments?.getString("stock")!!
            val input: String = stock.substring(1, stock.length - 1)
            var map: Map<String, Int>? = null
            if (input.isNotBlank()) {
                map = input.split(", ").associate { value -> val split = value.split("="); split[0] to split[1].toInt() }
            }
            val product = Product(image = image, productName = productName, category = category, criticalLevel = criticalLevel, purchasePrice = purchasePrice, sellingPrice = sellingPrice, supplier = supplier, stock = map ?: mapOf())

            ViewProduct(dismissRequest = { navController.popBackStack() }, product = product, edit = {
                navController.navigate(Routes.Product.Edit.createRoute(productId, product.productName, product.image ?: "null", product.purchasePrice, product.sellingPrice, product.supplier, product.category ?: "null", product.criticalLevel, product.stock.toString()))
            }, set = {
                navController.navigate(Routes.Product.Set.createRoute(productId, stock))
            }, delete = {
                navController.navigate(Routes.Product.Edit.createRoute(productId, product.productName, product.image ?: "null", product.purchasePrice, product.sellingPrice, product.supplier, product.category ?: "null", product.criticalLevel, product.stock.toString()))
            })
        }

        composable(Routes.Product.Add.route) {
            ProductForm(function = "Add", productViewModel = productViewModel, contactViewModel = contactViewModel, categoryViewModel = categoryViewModel, dismissRequest = {
                navController.popBackStack()
            })
        }

        composable(Routes.Product.Edit.route) {
            val productId: String = it.arguments?.getString("productId")!!
            val image: String? = if (it.arguments?.getString("image")!! == "null") null else it.arguments?.getString("image")!!
            val productName: String = it.arguments?.getString("name")!!
            val supplier: String = it.arguments?.getString("supplier")!!
            val sellingPrice: Double = it.arguments?.getString("sellingPrice")!!.toDouble()
            val purchasePrice: Double = it.arguments?.getString("purchasePrice")!!.toDouble()
            val category: String? = if (it.arguments?.getString("categoryId")!! == "null") null else it.arguments?.getString("categoryId")!!
            val criticalLevel: Int = it.arguments?.getString("criticalLevel")!!.toInt()
            val stock: String = it.arguments?.getString("stock")!!
            val input: String = stock.substring(1, stock.length - 1)
            var map: Map<String, Int>? = null
            if (input.isNotBlank()) {
                map = input.split(", ").associate { value -> val split = value.split("="); split[0] to split[1].toInt() }
            }
            ProductForm(function = "Edit", productViewModel = productViewModel, categoryViewModel = categoryViewModel, contactViewModel = contactViewModel, productId = productId, product = Product(image = image, productName = productName, category = category, criticalLevel = criticalLevel, purchasePrice = purchasePrice, sellingPrice = sellingPrice, supplier = supplier, stock = map ?: mapOf()), dismissRequest = {
                navController.popBackStack()
            })
        }

        composable(Routes.Product.Set.route) {
            val productId: String = it.arguments?.getString("productId")!!
            val stock: String = it.arguments?.getString("stock")!!
            val input: String = stock.substring(1, stock.length - 1)
            var map: Map<String, Int>? = null
            if (input.isNotBlank()) {
                map = input.split(", ").associate { value -> val split = value.split("="); split[0] to split[1].toInt() }
            }
            ProductQuantityFormScreen(productViewModel = productViewModel, branchViewModel = branchViewModel, productId = productId, map = map, dismissRequest = {
                navController.popBackStack()
            })
        }

        composable(Routes.Branch.route) {
            BranchScreen(scope = scope, scaffoldState = scaffoldState, viewModel = branchViewModel, productViewModel = productViewModel, add = { navController.navigate(Routes.Branch.Add.route) }) {
                branch -> navController.navigate(Routes.Branch.Edit.createRoute(branchId = branch.id, branchName = branch.name, branchAddress = branch.address))
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
            ContactScreen(scope = scope, scaffoldState = scaffoldState, contactViewModel = contactViewModel, edit = {
                contact -> navController.navigate(Routes.Contact.Edit.createRoute(contactId = contact.id, contactName = contact.name, contactNumber = contact.contact))
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
            val name: String = it.arguments?.getString("contactName")!!
            val contact: String = it.arguments?.getString("contactNumber")!!
            ContactFormScreen(function = "Edit", contactViewModel = contactViewModel, contact = Contact(id = id, name = name, contact = contact)) {
                navController.popBackStack()
            }
        }

        composable(Routes.PurchaseOrder.route) {
            PurchaseOrderScreen(scope = scope, scaffoldState = scaffoldState, contactViewModel = contactViewModel, purchaseOrderViewModel = purchaseOrderViewModel, add = { navController.navigate(Routes.PurchaseOrder.Add.route) })
        }

        composable(Routes.PurchaseOrder.Add.route) {
            PurchaseOrderForm(contactViewModel = contactViewModel, purchaseOrderViewModel = purchaseOrderViewModel, productViewModel = productViewModel) {
                navController.popBackStack()
            }
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