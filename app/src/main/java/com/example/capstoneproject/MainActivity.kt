package com.example.capstoneproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.capstoneproject.global.ui.navigation.Drawer
import com.example.capstoneproject.global.ui.navigation.NavigationHost
import com.example.capstoneproject.global.ui.viewmodel.AppViewModel
import com.example.capstoneproject.ui.theme.CapstoneProjectTheme
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
fun GlobalContent(appViewModel: AppViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(R.string.dashboard) }

    Scaffold(
        scaffoldState = scaffoldState,
        drawerScrimColor = Color.Black.copy(0.7f),
        drawerGesturesEnabled = !appViewModel.isLoading.value,
        drawerContent = { Drawer(user = appViewModel.user.value) {
            selectedItem = it
            navController.navigate(selectedItem.toString()) {
                popUpTo(0)
            }
            scope.launch {
                delay(500)
                scaffoldState.drawerState.close()
            }
        }}
    ) {
        paddingValues -> paddingValues

        NavigationHost(navController = navController, scope = scope, scaffoldState = scaffoldState, viewModel = appViewModel)

        LaunchedEffect(key1 = appViewModel.connection.value) {
            if (!appViewModel.connection.value) {
                scaffoldState.snackbarHostState.showSnackbar(message = "Lost Connection!", duration = SnackbarDuration.Indefinite)
            }
        }
    }
}