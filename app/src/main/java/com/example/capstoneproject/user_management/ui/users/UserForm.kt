package com.example.capstoneproject.user_management.ui.users

import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.capstoneproject.R
import com.example.capstoneproject.global.ui.misc.ConfirmationForAddingDialog
import com.example.capstoneproject.global.ui.misc.FormButtons
import com.example.capstoneproject.global.ui.misc.GlobalTextFieldColors
import com.example.capstoneproject.product_management.ui.branch.BranchViewModel
import com.example.capstoneproject.user_management.data.firebase.User
import com.example.capstoneproject.user_management.data.firebase.UserLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserForm(
    userViewModel: UserViewModel,
    branchViewModel: BranchViewModel,
    decision: String,
    userId: String? = null,
    back: () -> Unit
) {
    var id = userId
    val users = userViewModel.getAll()
    var user = userViewModel.getUserDetails(userId = userId) ?: User()
    var expandedUsers by remember { mutableStateOf(false) }
    var expandedBranches by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf(user.firstName) }
    var isFirstNameValid by remember { mutableStateOf(true) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var isLastNameValid by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf(user.email) }
    var isEmailValid by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber.let { if (it.length > 10) it.removeRange(0, 1) else "" }) }
    var isPhoneNumber by remember { mutableStateOf(true) }
    val branches = branchViewModel.getAll().observeAsState(listOf())
    var branchId by remember { mutableStateOf(branches.value.firstOrNull()?.id)}
    var branchName by remember { mutableStateOf(branches.value.firstOrNull()?.name ?: "No Branches")}
    val userAccountDetails = userViewModel.userAccountDetails.collectAsState()
    var userLevel by remember { mutableStateOf(if (userAccountDetails.value.userLevel == UserLevel.Admin && user.userLevel == UserLevel.Cashier) UserLevel.Owner else user.userLevel) }
    val userLevels = if (userAccountDetails.value.userLevel == UserLevel.Admin) enumValues<UserLevel>().filter { it != UserLevel.Cashier && it != UserLevel.Manager } else enumValues<UserLevel>().filter { it != UserLevel.Admin }
    val localFocusManager = LocalFocusManager.current
    val showConfirmationDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val jsonUriLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent(), onResult = {
        if (it != null) {
            val item = context.contentResolver.openInputStream(it)
            if (item != null) {
                user = userViewModel.readFromJson(item)
                id = user.id
                lastName = user.lastName
                firstName = user.firstName
                email = user.email
                branchId = user.branchId
                branchName = branches.value.firstOrNull()?.name ?: "No Branches"
                userLevel = user.userLevel

                runBlocking {
                    withContext(Dispatchers.IO) {
                        item.close()
                    }
                }
            }
        }
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = ("$decision User").uppercase())
                },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        jsonUriLauncher.launch("application/json")
                    }) {
                        Icon(imageVector = Icons.Filled.Upload, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(expanded = expandedUsers && userAccountDetails.value.id != userId, onExpandedChange = { expandedUsers = !expandedUsers }) {
                OutlinedTextField(enabled = userAccountDetails.value.id != userId, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUsers && userAccountDetails.value.id != userId) }, colors = GlobalTextFieldColors(), modifier = Modifier.fillMaxWidth(), value = userLevel.name, onValueChange = {  }, readOnly = true, label = { Text(text = buildAnnotatedString { append(text = stringResource(id = R.string.user_level)); withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") } }) })

                DropdownMenu(modifier = Modifier
                    .exposedDropdownSize()
                    .fillMaxWidth(), expanded = expandedUsers && userAccountDetails.value.id != userId, onDismissRequest = { expandedUsers = false }) {
                    userLevels.forEach {
                        androidx.compose.material3.DropdownMenuItem(text = { Text(text = it.name) }, onClick = {
                            userLevel = it
                            expandedUsers = false
                        })
                    }
                }
            }

            if (userLevel == UserLevel.Cashier || userLevel == UserLevel.Manager) {
                ExposedDropdownMenuBox(expanded = expandedBranches, onExpandedChange = { expandedBranches = !expandedBranches }) {
                    OutlinedTextField(trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBranches) }, colors = GlobalTextFieldColors(), modifier = Modifier.fillMaxWidth(), value = branchName, onValueChange = {  }, readOnly = true, label = { Text(text = buildAnnotatedString { append(text = stringResource(id = R.string.branch)); withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") } }) })

                    DropdownMenu(modifier = Modifier
                        .exposedDropdownSize()
                        .fillMaxWidth(), expanded = expandedBranches, onDismissRequest = { expandedBranches = false }) {
                        branches.value.forEach {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(text = it.name) },
                                onClick = {
                                    branchId = it.id
                                    branchName = it.name
                                    expandedBranches = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = firstName,
                colors = GlobalTextFieldColors(),
                onValueChange = {
                    firstName = it.filter { value ->
                        value.isLetter() || value.isWhitespace()
                    }
                },
                label = {
                    Text(text = buildAnnotatedString {
                        append(text = "First Name")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") }
                    })
                },
                placeholder = { Text(text = "Enter First Name") },
                isError = !isFirstNameValid,
                trailingIcon = { if (!isFirstNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )
            OutlinedTextField(
                value = lastName,
                colors = GlobalTextFieldColors(),
                onValueChange = {
                    lastName = it.filter { value ->
                        value.isLetter() || value.isWhitespace()
                    }
                },
                label = {
                    Text(text = buildAnnotatedString {
                        append(text = "Last Name")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") }
                    })
                },
                placeholder = { Text(text = "Enter Last Name") },
                isError = !isLastNameValid,
                trailingIcon = { if (!isLastNameValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                })
            )
            OutlinedTextField(
                value = email,
                colors = GlobalTextFieldColors(),
                onValueChange = { email = it },
                label = {
                    Text(text = buildAnnotatedString {
                        append(text = "Email")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") }
                    })
                },
                placeholder = { Text(text = "Enter Email") },
                isError = !isEmailValid, trailingIcon = { if (!isEmailValid) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    localFocusManager.moveFocus(FocusDirection.Down)
                }),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phoneNumber,
                colors = GlobalTextFieldColors(),
                onValueChange = { value -> if (value.length <= 10) phoneNumber = value.filter { it.isDigit() } },
                label = {
                    Text(text = buildAnnotatedString {
                        append(text = "Phone Number")
                        withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) { append(text = " *") }
                    })
                },
                placeholder = { Text(text = "Enter Phone Number") },
                isError = !isPhoneNumber, trailingIcon = { if (!isPhoneNumber) Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = Color.Red) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    localFocusManager.clearFocus()
                }),
                leadingIcon = { Text(text = "+63") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FormButtons(cancel = back) {
                    isFirstNameValid = firstName.isNotBlank()
                    isLastNameValid = lastName.isNotBlank()
                    isEmailValid = email.let { it.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(it).matches() && users.filterKeys { key -> id != key }.all { entry -> entry.value.email != it } }
                    isPhoneNumber = phoneNumber.let { it.isNotBlank() && Patterns.PHONE.matcher(it).matches() && it.length == 10 && users.filterKeys { key -> id != key }.all { entry -> entry.value.phoneNumber != "0$it" } }
                    val isBranchValid = if (userLevel == UserLevel.Cashier || userLevel == UserLevel.Manager) branchId != null else true

                    if (isFirstNameValid && isLastNameValid && isEmailValid && isBranchValid && isPhoneNumber) {
                        showConfirmationDialog.value = true
                    }
                }
            }

            if (showConfirmationDialog.value) {
                ConfirmationForAddingDialog(onCancel = { showConfirmationDialog.value = false }) {
                    userViewModel.insert(id = id, user = user.copy(id = null, lastName = lastName, firstName = firstName, phoneNumber = "0$phoneNumber", password = "0$phoneNumber", active = true, email = email, userLevel = userLevel, branchId = if (userLevel == UserLevel.Cashier || userLevel == UserLevel.Manager) branchId else null))
                    userViewModel.log("${decision}_user")
                    showConfirmationDialog.value = false
                    back.invoke()
                }
            }
        }
    }
}