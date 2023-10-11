package com.example.capstoneproject

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
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
    val connection = appViewModel.connection

    Scaffold(
        scaffoldState = scaffoldState,
        drawerScrimColor = Color.Black.copy(0.7f),
        drawerGesturesEnabled = !appViewModel.isLoading.value,
        drawerContent = { Drawer {
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
        paddingValues ->
        NavigationHost(navController = navController, scope = scope, scaffoldState = scaffoldState, viewModel = appViewModel)
        if (!connection.value) {
            Snackbar(modifier = Modifier
                .padding(paddingValues)
                .padding(8.dp)) {
                Text(text = "No Internet Connection")
            }
        }
    }
}

@Composable
fun LoginScreen(onLoad: (Boolean) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(true) }
    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_WIFI_STATE)
    val permissionState = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = {
        it -> it.forEach {
            if (it.value) {
                hasPermission = false
            }
        }
    })

    LaunchedEffect(key1 = hasPermission, block = {
        permissionState.launch(permissions)
        if (
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permissions[1]) &&
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permissions[2]) &&
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permissions[3])
        ) {
            Log.d("Checking", "True")
            onLoad.invoke(true)
        } else {
            Log.d("Permission", "Denied")
        }
    })

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {

        }) {
            Text(text = stringResource(R.string.login))
        }
    }
}