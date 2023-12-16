package com.example.capstoneproject.product_management.ui.branch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.global.ui.misc.ConfirmationForAddingDialog
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.user_management.ui.users.UserViewModel

@Composable
fun BranchFormScreen(
    viewModel: BranchViewModel,
    function: String = "Add",
    id: String? = null,
    userViewModel: UserViewModel,
    back: () -> Unit
) {
    val branch = viewModel.getBranch(id) ?: Branch()
    val localFocusManager = LocalFocusManager.current
    val showConfirmationDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = ("$function Branch").uppercase())}, navigationIcon = {
                IconButton(onClick = back) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            })
        }
    ) { paddingValues ->
        var name by remember { mutableStateOf(branch.name) }
        var address by remember { mutableStateOf(branch.address) }
        var isNameValid by remember { mutableStateOf(true) }
        var isAddressValid by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                colors = GlobalTextFieldColors(),
                onValueChange = { name = it },
                label = {
                    Text(text = buildAnnotatedString { append("Branch Name")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
                            append(text = " *")
                        }
                    })
                },
                placeholder = { Text(text = "Enter Branch Name") },
                isError = !isNameValid,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { if (!isNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )
            OutlinedTextField(
                value = address,
                colors = GlobalTextFieldColors(),
                onValueChange = { address = it },
                label = {
                    Text(text = buildAnnotatedString {
                        append(text = "Branch Address")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
                            append(text = " *")
                        }
                    })
                },
                placeholder = { Text(text = "Enter Branch Address") },
                isError = !isAddressValid,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { if (!isAddressValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    localFocusManager.clearFocus()
                })
            )
            FormButtons(cancel = back) {
                isNameValid = name.isNotBlank()
                isAddressValid = address.isNotBlank()

                if (isNameValid && isAddressValid) {
                    showConfirmationDialog.value = true
                }
            }

            if (showConfirmationDialog.value) {
                ConfirmationForAddingDialog(onCancel = { showConfirmationDialog.value = false }) {
                    viewModel.insert(branch.copy(name = name, address = address))
                    userViewModel.log(event = "${function.lowercase()}_branch")
                    showConfirmationDialog.value = false
                    back.invoke()
                }
            }
        }
    }
}