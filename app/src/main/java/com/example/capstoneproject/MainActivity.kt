package com.example.capstoneproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.capstoneproject.global.ui.AppViewModel
import com.example.capstoneproject.global.ui.navigation.Drawer
import com.example.capstoneproject.global.ui.navigation.NavigationHost
import com.example.capstoneproject.global.ui.navigation.Routes
import com.example.capstoneproject.ui.theme.CapstoneProjectTheme
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import com.example.capstoneproject.user_management.ui.users.UserViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

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
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_WIFI_STATE)
                    val permissionState = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = {  })

                    LaunchedEffect(key1 = Unit) {
                        permissionState.launch(permissions)
                    }

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
    val appUi = appViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        drawerScrimColor = Color.Black.copy(0.7f),
        drawerGesturesEnabled = !appUi.value.lockMenu,
        drawerContent = { Drawer(user = appUi.value.user, currentItem = selectedItem, userViewModel = userViewModel) {
            selectedItem = it
            navController.navigate(selectedItem.toString()) {
                popUpTo(0)
            }
            scope.launch {
                delay(500)
                scaffoldState.drawerState.close()
            }
        }}
    ) { paddingValues ->
        LaunchedEffect(key1 = appUi.value.user) {
            if (appUi.value.user.data != null) {
                userViewModel.getUser(appUi.value.user.data!!.email)
            }
        }

        if (appUi.value.loadLogin) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (appUi.value.showProgress) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.7f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Updating Records...", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                CircularProgressIndicator(color = Color.White, progress = appUi.value.loadingProgress)
            }
        }

        NavigationHost(
            navController = navController,
            scope = scope,
            scaffoldState = scaffoldState,
            viewModel = appViewModel,
            userViewModel = userViewModel
        ) {
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
            Log.e("LOGIN", userAccountDetails.value.toString())
            userAccountDetails.value.let {
                if (it.id.isNotBlank() && it.isActive && it.errorMessage == null) {
                    val previous = Instant.ofEpochMilli(userAccountDetails.value.previousLoginDate).atZone(ZoneId.systemDefault()).toLocalDate()
                    val current = Instant.ofEpochMilli(userAccountDetails.value.loginDate).atZone(ZoneId.systemDefault()).toLocalDate()

                    scope.launch {
                        userViewModel.log("user_logged_in")
                        scaffoldState.snackbarHostState.showSnackbar("Logged In Successfully!", duration = SnackbarDuration.Short)
                    }

                    selectedItem = if (userAccountDetails.value.userLevel == UserLevel.Admin) R.string.user else R.string.dashboard

                    navController.navigate(selectedItem.toString()) {
                        popUpTo(0)
                    }

                    delay(500)
                    appViewModel.loadLogin(false)
                    appViewModel.updateMonthlyCounters(previous = previous, current = current)
                } else if (it.id.isNotBlank() && !it.isActive && it.errorMessage == null) {
                    appViewModel.loadLogin(false)
                    navController.navigate(Routes.Logout.route)
                    scaffoldState.snackbarHostState.showSnackbar("Account Inactive!", duration = SnackbarDuration.Short)
                } else if (it.errorMessage != null) {
                    appViewModel.loadLogin(false)
                    navController.navigate(Routes.Logout.route)
                    scaffoldState.snackbarHostState.showSnackbar(it.errorMessage, duration = SnackbarDuration.Short)
                }
            }
        }
    }
}