package com.example.capstoneproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = darkprimary1,
    secondary = secondaryColor,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = darkText1,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.Red,
)

private val LightColorPalette = lightColors(
    primary = primaryColor,
    primaryVariant = Purple700,
    secondary = secondaryColor,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color.Red,
    onError = Color.Red
)

@Composable
fun CapstoneProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    if (darkTheme) {
        systemUiController.setSystemBarsColor(color = Color(ColorUtils.blendARGB(darkprimary1.toArgb(), Color.Black.toArgb(), 0.2f)))
    } else {
        systemUiController.setSystemBarsColor(color = Color(ColorUtils.blendARGB(primaryColor.toArgb(), Color.Black.toArgb(), 0.2f)))
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}