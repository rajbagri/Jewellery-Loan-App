package com.newstudio.ujkhataapp.ui.component

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEntryInEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (EntryInEntry, Double) -> Unit,
    existingEntry: EntryInEntry? = null,
    existingInterestRate: Double? = null
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf(existingEntry?.amount?.toString() ?: "") }
    var interestRate by remember { mutableStateOf(existingInterestRate?.toString() ?: "3") }

    var selectedDateMillis by remember { mutableStateOf(existingEntry?.time ?: System.currentTimeMillis()) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val selectedDateString = dateFormatter.format(Date(selectedDateMillis))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (existingEntry == null) "Add Sub Entry" else "Edit Sub Entry")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest Rate (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = selectedDateString,
                    onValueChange = {},
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = selectedDateMillis
                            }
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                    val newCalendar = Calendar.getInstance()
                                    newCalendar.set(year, month, dayOfMonth)
                                    selectedDateMillis = newCalendar.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                val parsedInterestRate = interestRate.toDoubleOrNull() ?: 3.0

                val entry = EntryInEntry(
                    id = existingEntry?.id ?: "",
                    amount = parsedAmount,
                    cross = false,
                    time = selectedDateMillis
                )
                onConfirm(entry, parsedInterestRate)
            }) {
                Text(text = if (existingEntry == null) "Add" else "Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = Modifier.padding(16.dp)
    )
}
