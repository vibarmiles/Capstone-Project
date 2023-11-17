package com.example.capstoneproject.global.ui.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R

@Composable
fun FormButtons(
    cancelText: String = stringResource(id = R.string.cancel_button),
    submitText: String = stringResource(id = R.string.submit_button),
    cancel: () -> Unit,
    submit: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = cancel, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, MaterialTheme.colors.onPrimary), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onPrimary)) {
            Text(text = cancelText.uppercase())
        }

        Button(onClick = submit, modifier = Modifier.weight(1f)) {
            Text(text = submitText.uppercase())
        }
    }
}