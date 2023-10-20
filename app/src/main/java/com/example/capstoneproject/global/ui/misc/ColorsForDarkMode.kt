package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.capstoneproject.ui.theme.darkText1
import com.example.capstoneproject.ui.theme.darkbg
import com.example.capstoneproject.ui.theme.darkprimary1

@Composable
fun ProjectListItemColors(): ListItemColors = if (isSystemInDarkTheme()) ListItemDefaults.colors(
    containerColor = darkbg,
    headlineColor = darkText1,
    leadingIconColor = darkprimary1,
    overlineColor = darkText1,
    supportingColor = darkText1,
    trailingIconColor = darkprimary1,
    disabledHeadlineColor = Color.Transparent,
    disabledLeadingIconColor = Color.Transparent,
    disabledTrailingIconColor = Color.Transparent
) else ListItemDefaults.colors()