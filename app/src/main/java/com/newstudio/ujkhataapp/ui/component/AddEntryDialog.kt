package com.newstudio.ujkhataapp.ui.component

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEntryDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (
        amount: Double,
        jewellery: String,
        interestRate: Double,
        isCross: Boolean,
        selectedTime: Long
    ) -> Unit,
    dialogTitle: String,
    icon: ImageVector,
    initialAmount: Double? = null,
    initialJewellery: String? = null,
    initialInterestRate: Double? = null,
    initialIsCross: Boolean? = null,
    initialTime: Long? = null
) {
    var amountText by remember { mutableStateOf(initialAmount?.toString() ?: "") }
    var jewelleryText by remember { mutableStateOf(initialJewellery ?: "") }
    var interestRateText by remember { mutableStateOf(initialInterestRate?.toString() ?: "") }

    // Handle selected date
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    // Set initial date if provided
    initialTime?.let { calendar.timeInMillis = it }
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }

    val dateText = remember(selectedDate) {
        dateFormatter.format(Date(selectedDate))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(dialogTitle)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = jewelleryText,
                    onValueChange = { jewelleryText = it },
                    label = { Text("Jewellery") },
                    singleLine = true
                )


            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val interestRate = interestRateText.toDoubleOrNull() ?: 0.0
                    onConfirmation(amount, jewelleryText, interestRate, false, selectedDate)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
