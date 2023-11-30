package com.example.capstoneproject.login.ui.login

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.capstoneproject.R
import com.example.capstoneproject.user_management.data.firebase.UserLevel

@Composable
fun LoginScreen(
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(true) }
    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_WIFI_STATE)
    val permissionState = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(), onResult = {
            it -> it.forEach {
        if (it.value) {
            hasPermission = false
        }
    } })

    SideEffect {
        permissionState.launch(permissions)
        if (
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permissions[1]) &&
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permissions[2]) &&
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permissions[3])
        ) {
            Log.d("Checking", "True")
        } else {
            Log.d("Permission", "Denied")
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { onClick.invoke() }) {
                Text(text = stringResource(R.string.login))
            }
        }
    }
}