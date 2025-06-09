package com.newstudio.ujkhataapp.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import com.newstudio.ujkhataapp.ui.component.AddEntryInEntryDialog
import com.newstudio.ujkhataapp.ui.component.EntryInEntryCard
import com.newstudio.ujkhataapp.viewModel.KhataViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntryInEntryScreen(
    customerId: String,
    entryId: String,
    viewModel: KhataViewModel,
) {
    LaunchedEffect(customerId, entryId) {
        viewModel.getEntryInEntries(customerId, entryId)
    }

    val entryInEntryMap by viewModel.entryInEntryMap.collectAsState()
    val entries = entryInEntryMap[customerId]?.get(entryId) ?: emptyList()

    var searchQuery by remember { mutableStateOf("") }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Dialog state: null = no dialog for edit; and boolean flag for add dialog
    var dialogEntry by remember { mutableStateOf<EntryInEntry?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    var entryBeingDeleted by remember { mutableStateOf<EntryInEntry?>(null) }
    var entryPendingCrossToggle by remember { mutableStateOf<EntryInEntry?>(null) }
    var pendingCrossValue by remember { mutableStateOf(false) }

    val filtered = entries.filter {
        val formattedDate = dateFormatter.format(it.time)
        val query = searchQuery.trim()
        query.isEmpty() || formattedDate.contains(query, ignoreCase = true)
    }.sortedByDescending { it.time }

    val active = filtered.filterNot { it.cross }
    val crossed = filtered.filter { it.cross }

    val totalActive = active.sumOf { it.amount }
    val totalCrossed = active.sumOf { calculateInterestFromEntryInEntry(it, it.interestRate) }

    // Calculate paidPrincipal and paidInterest sums from crossed entries
    val paidPrincipal = crossed.sumOf { it.amount }  // Replace with actual field if different
    val paidInterest = crossed.sumOf { calculateInterestFromEntryInEntry(it, it.interestRate) }    // Replace with actual field if different
    val totalPaid = paidPrincipal + paidInterest

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                isAddDialogOpen = true
                dialogEntry = null
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add EntryInEntry")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by date") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Unpaid Principal(शेष मूल्य): ₹%.2f".format(totalActive))
                    Text("Unpaid Interest(शेष ब्याज): ₹%.2f".format(totalCrossed))

                    Spacer(Modifier.height(8.dp))

                    Divider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Text("Unpaid Amount(शेष कुल): ₹%.2f".format(totalActive + totalCrossed))
                    Divider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "Paid Principal (वापस मूल्य): ₹%.2f".format(paidPrincipal),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Paid Interest (वापस ब्याज): ₹%.2f".format(paidInterest),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "Total Paid (वापस कुल): ₹%.2f".format(totalPaid),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(filtered) { entry ->
                    EntryInEntryCard(
                        entry = entry,
                        onEdit = {
                            dialogEntry = entry
                            isAddDialogOpen = true
                        },
                        onDelete = {
                            entryBeingDeleted = entry
                        },
                        onCrossToggle = { currentCross ->
                            entryPendingCrossToggle = entry
                            pendingCrossValue = !currentCross
                        }
                    )
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }

        if (isAddDialogOpen) {
            AddEntryInEntryDialog(
                existingEntry = dialogEntry,
                onDismiss = {
                    dialogEntry = null
                    isAddDialogOpen = false
                },
                onConfirm = { entry, interestRate ->
                    val entryToSave = if (entry.id.isEmpty()) {
                        entry.copy(id = UUID.randomUUID().toString())
                    } else {
                        entry
                    }
                    viewModel.addOrUpdateEntryInEntry(customerId, entryId, entryToSave, interestRate)

                    dialogEntry = null
                    isAddDialogOpen = false
                }

            )
        }

        // Confirm Delete Dialog
        entryBeingDeleted?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryBeingDeleted = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this sub-entry?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteEntryInEntry(customerId, entryId, entry.id)
                            entryBeingDeleted = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryBeingDeleted = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Confirm Cross Toggle Dialog
        entryPendingCrossToggle?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryPendingCrossToggle = null },
                title = {
                    Text(text = if (pendingCrossValue) "Cross Sub-Entry?" else "Uncross Sub-Entry?")
                },
                text = {
                    Text(
                        text = if (pendingCrossValue)
                            "Are you sure you want to mark this sub-entry as crossed?"
                        else
                            "Are you sure you want to unmark this sub-entry?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val updatedEntry = entry.copy(cross = pendingCrossValue)
                            viewModel.addOrUpdateEntryInEntry(
                                customerId = customerId,
                                parentEntryId = entryId,
                                entryInEntry = updatedEntry,
                                interestRate = updatedEntry.interestRate
                            )
                            entryPendingCrossToggle = null
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryPendingCrossToggle = null }) {
                        Text("No")
                    }
                }
            )
        }

    }
}
