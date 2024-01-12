package com.example.capstoneproject.login.ui.login

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    scope: CoroutineScope,
    login: (String, String) -> Unit,
    onClick: () -> Unit
) {
    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_WIFI_STATE)
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(true) }
    val localFocusManager = LocalFocusManager.current

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
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .aspectRatio(ratio = 1f)
                    .background(color = MaterialTheme.colors.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(id = R.mipmap.app_icon_foreground), contentDescription = null, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(25.dp))

            OutlinedTextField(
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                colors = GlobalTextFieldColors(),
                modifier = Modifier.fillMaxWidth(0.8f),
                value = username,
                onValueChange = { username = it },
                placeholder = { Text(text = "Enter Email/Phone Number", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                label = {
                    Text(text = buildAnnotatedString {
                        append("Email/Phone Number")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") }
                    }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                isError = !isValid,
                trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )

            OutlinedTextField(
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                colors = GlobalTextFieldColors(),
                modifier = Modifier.fillMaxWidth(0.8f),
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text(text = "Enter Password") },
                label = {
                    Text(text = buildAnnotatedString {
                        append("Password")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") }
                    })
                },
                isError = !isValid,
                trailingIcon = { if (!isValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    localFocusManager.clearFocus()
                })
            )

            Button(
                onClick = {
                    isValid = username.isNotBlank() && password.isNotEmpty()

                    if (isValid) {
                        login.invoke(username, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(IntrinsicSize.Min),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(ColorUtils.blendARGB(Color.LightGray.toArgb(), Color.White.toArgb(), 0.5f)))
            ) {
                Text(text = "Login")
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 4.dp)
            ) {
                Divider()
                Text(text = "or", modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(horizontal = 4.dp))
            }

            Button(
                onClick = {
                    if (permissions.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }) {
                        onClick.invoke()
                    } else {
                        scope.launch {
                            Toast.makeText(context, "Permissions Required", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
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

            TextButton(
                onClick = { context.startActivity(Intent(Settings.ACTION_ADD_ACCOUNT)) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Unspecified,
                    contentColor = Color.Blue
                )
            ) {
                Text(text = "Manage Accounts")
            }
        }
    }
}