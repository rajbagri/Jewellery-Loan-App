package com.newstudio.ujkhataapp.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AddCustomerDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String, String) -> Unit, // name, town, phone
    dialogTitle: String,
    icon: ImageVector,
    initialName: String = "",
    initialTown: String = "",
    initialPhone: String = ""
) {
    val nameState = remember { mutableStateOf(initialName) }
    val townState = remember { mutableStateOf(initialTown) }
    val phoneState = remember { mutableStateOf(initialPhone) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirmation(
                    nameState.value.trim(),
                    townState.value.trim(),
                    phoneState.value.trim()
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        icon = { Icon(imageVector = icon, contentDescription = null) },
        title = { Text(dialogTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text("Customer Name") }
                )
                OutlinedTextField(
                    value = townState.value,
                    onValueChange = { townState.value = it },
                    label = { Text("Town") }
                )
                OutlinedTextField(
                    value = phoneState.value,
                    onValueChange = { phoneState.value = it },
                    label = { Text("Phone Number") },
                    singleLine = true
                )
            }
        }
    )
}
