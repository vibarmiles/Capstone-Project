package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.capstoneproject.R


@Composable
fun ImageNotAvailable(
    modifier: Modifier = Modifier
) {
    Image(painter = painterResource(R.mipmap.app_icon_foreground), contentDescription = null, modifier = modifier)
}