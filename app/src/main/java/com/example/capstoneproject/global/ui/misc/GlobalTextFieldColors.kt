package com.example.capstoneproject.global.ui.misc

import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable

@Composable
fun GlobalTextFieldColors(): TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
    focusedLabelColor = MaterialTheme.colors.onSurface
)