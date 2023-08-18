package com.example.capstoneproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import com.example.capstoneproject.global.ui.Misc.Drawer
import com.example.capstoneproject.global.ui.navigation.NavigationHost
import com.example.capstoneproject.ui.theme.CapstoneProjectTheme
import kotlinx.coroutines.delay
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
        drawerScrimColor = Color.Black.copy(0.7f),
        drawerContent = { Drawer {
            navController.navigate(it.toString())
            scope.launch {
                delay(500)
                scaffoldState.drawerState.close()
            }
        }}
    ) {
        it
        NavigationHost(navController = navController, scope = scope, scaffoldState = scaffoldState)
    }
}

@Composable
fun AppSplashScreen(onLoad: () -> Unit) {
    var fade: Boolean by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = true) {
        delay(3000)
        fade = false
        delay(500)
        onLoad()
    }
    AnimatedVisibility(visible = fade,exit = fadeOut()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = null)
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null)
        }
    }
}