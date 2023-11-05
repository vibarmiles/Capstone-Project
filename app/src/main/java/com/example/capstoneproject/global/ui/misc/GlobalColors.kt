package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

@Composable
fun NavigationItemColors(): NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
    selectedContainerColor = /*if (isSystemInDarkTheme()) Color.DarkGray else*/ Color.LightGray,
    unselectedContainerColor = Color(ColorUtils.blendARGB(MaterialTheme.colors.surface.toArgb(), Color.White.toArgb(), 0.2f)),
    unselectedIconColor = MaterialTheme.colors.onSurface,
    unselectedTextColor = MaterialTheme.colors.onSurface
)