package com.example.capstoneproject.login.ui.login

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
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
    loadLogin: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            if (!loadLogin) {
                Button(onClick = { onClick.invoke() }) {
                    Text(text = stringResource(R.string.login))
                }
            }
        }
    }
}