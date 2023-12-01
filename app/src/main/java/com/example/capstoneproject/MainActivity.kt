package com.example.capstoneproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.capstoneproject.global.ui.navigation.Drawer
import com.example.capstoneproject.global.ui.navigation.NavigationHost
import com.example.capstoneproject.global.ui.viewmodel.AppViewModel
import com.example.capstoneproject.ui.theme.CapstoneProjectTheme
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            Firebase.database.setPersistenceEnabled(true)
        }
        super.onCreate(savedInstanceState)
        setContent {
            CapstoneProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.onSurface
                ) {
                    GlobalContent()
                }
            }
        }
    }
}

@Composable
fun GlobalContent(
    appViewModel: AppViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(R.string.dashboard) }
    var canExit by remember { mutableStateOf(false) }
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()
    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        drawerScrimColor = Color.Black.copy(0.7f),
        drawerGesturesEnabled = !appViewModel.isLoading.value,
        drawerContent = { Drawer(user = appViewModel.user.value, currentItem = selectedItem, userViewModel = userViewModel) {
            selectedItem = it
            navController.navigate(selectedItem.toString()) {
                popUpTo(0)
            }
            scope.launch {
                delay(500)
                scaffoldState.drawerState.close()
            }
        }}
    ) { paddingValues -> paddingValues
        NavigationHost(navController = navController, scope = scope, scaffoldState = scaffoldState, viewModel = appViewModel, userViewModel = userViewModel) {
            selectedItem = it
            navController.navigate(selectedItem.toString()) {
                popUpTo(0)
            }
        }

        LaunchedEffect(key1 = appViewModel.connection.value) {
            if (!appViewModel.connection.value) {
                scaffoldState.snackbarHostState.showSnackbar(message = "Lost Connection!", duration = SnackbarDuration.Short)
            }
        }

        BackHandler(!canExit) {
            if (userAccountDetails.value.userLevel != UserLevel.Admin) {
                if (selectedItem == R.string.dashboard || selectedItem == R.string.login) {
                    canExit = true
                } else {
                    if (navController.backQueue.size > 2) {
                        navController.popBackStack()
                    } else {
                        selectedItem = R.string.dashboard
                        navController.navigate(selectedItem.toString()) {
                            popUpTo(0)
                        }
                    }
                }
            } else {
                if (selectedItem == R.string.activity_logs || selectedItem == R.string.login) {
                    canExit = true
                } else {
                    if (navController.backQueue.size > 2) {
                        navController.popBackStack()
                    } else {
                        selectedItem = R.string.activity_logs
                        navController.navigate(selectedItem.toString()) {
                            popUpTo(0)
                        }
                    }
                }
            }
        }

        LaunchedEffect(key1 = canExit) {
            if (canExit) {
                Toast.makeText(context, "Press again to exit!", Toast.LENGTH_SHORT).show()
                delay(2000)
                canExit = false
            }
        }

        LaunchedEffect(key1 = userAccountDetails.value) {
            userAccountDetails.value.let {
                if (it.id.isNotBlank() && it.isActive && it.errorMessage == null) {
                    scope.launch {
                        userViewModel.log("user_logged_in")
                        scaffoldState.snackbarHostState.showSnackbar("Logged In Successfully!", duration = SnackbarDuration.Short)
                    }

                    selectedItem = if (userAccountDetails.value.userLevel == UserLevel.Admin) R.string.user else R.string.dashboard

                    navController.navigate(selectedItem.toString()) {
                        popUpTo(0)
                    }

                    appViewModel.isLoading.value = false
                } else if (it.id.isNotBlank() && !it.isActive && it.errorMessage == null) {
                    scaffoldState.snackbarHostState.showSnackbar("Account Inactive!", duration = SnackbarDuration.Short)
                } else if (it.errorMessage != null) {
                    scaffoldState.snackbarHostState.showSnackbar(it.errorMessage, duration = SnackbarDuration.Short)
                }
            }
        }

        LaunchedEffect(key1 = appViewModel.user.value) {
            if (appViewModel.user.value.data != null) {
                userViewModel.getUser(appViewModel.user.value.data!!.email)
            }
        }
    }
}