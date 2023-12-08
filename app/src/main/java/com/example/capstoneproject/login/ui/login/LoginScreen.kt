package com.example.capstoneproject.login.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.example.capstoneproject.R

@Composable
fun LoginScreen(
    loadLogin: Boolean,
    onClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = ("Inventory Management System").uppercase(),
                textAlign = TextAlign.Center
            )
        }
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (!loadLogin) {
            Button(
                onClick = { onClick.invoke() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(IntrinsicSize.Min),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(ColorUtils.blendARGB(Color.LightGray.toArgb(), Color.White.toArgb(), 0.5f)))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        modifier = Modifier.height(30.dp),
                        painter = painterResource(R.mipmap.google_icon_foreground),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Login with Google",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}