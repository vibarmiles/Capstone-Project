package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.product_management.data.Room.branch.Branch

@Composable
fun FormButtons(cancel: () -> Unit, submit: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = cancel, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, Color.Black), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) {
            Text(text = stringResource(id = R.string.cancel_button))
        }

        Button(onClick = submit, modifier = Modifier.weight(1f)) {
            Text(text = stringResource(id = R.string.submit_button))
        }
    }
}