package com.newstudio.ujkhataapp.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.newstudio.ujkhataapp.Screen
import com.newstudio.ujkhataapp.data.model.Entry
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import com.newstudio.ujkhataapp.ui.component.AddEntryDialog
import com.newstudio.ujkhataapp.ui.component.EntryCard
import com.newstudio.ujkhataapp.viewModel.KhataViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntriesScreen(
    customerId: String,
    viewModel: KhataViewModel,
    navController: NavController
) {
    val entriesMap by viewModel.entriesMap.collectAsState()
    val entries = entriesMap[customerId].orEmpty()
    val customer by viewModel.customerName.collectAsState()
    val entryInEntriesMap by viewModel.entryInEntryMap.collectAsState()

    val listState = rememberLazyListState()

    var openAddEntryDialog by remember { mutableStateOf(false) }
    var entryBeingEdited by remember { mutableStateOf<Entry?>(null) }
    var entryBeingDeleted by remember { mutableStateOf<Entry?>(null) }
    var entryPendingCrossToggle by remember { mutableStateOf<Entry?>(null) }
    var pendingCrossValue by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }

    LaunchedEffect(customerId) {
        viewModel.getEntries(customerId)
        viewModel.getCustomersById(customerId)
    }

    val filteredEntries = entries
        .filter { entry ->
            val formattedDate = dateFormatter.format(entry.time)
            val query = searchQuery.trim()
            query.isEmpty() || (
                    formattedDate.contains(query, ignoreCase = true) ||
                            entry.jewellery.contains(query, ignoreCase = true)
                    )
        }
        .sortedByDescending { it.time }

    val currentTime = System.currentTimeMillis()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    entryBeingEdited = null
                    openAddEntryDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        entryBeingDeleted?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryBeingDeleted = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this entry?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteEntry(customerId, entry.id)
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

        if (openAddEntryDialog) {
            AddEntryDialog(
                onDismissRequest = {
                    openAddEntryDialog = false
                    entryBeingEdited = null
                },
                onConfirmation = { amount, jewellery, interestRate, cross, currTime ->
                    openAddEntryDialog = false
                    viewModel.addOrUpdateEntry(
                        customerId = customerId,
                        amount = amount,
                        entryId = entryBeingEdited?.id,
                        jewellery = jewellery,
                        cross = cross,
                        originalTime = System.currentTimeMillis()
                    )
                    entryBeingEdited = null
                },
                dialogTitle = if (entryBeingEdited == null) "Add Entry" else "Edit Entry",
                icon = Icons.Default.Info,
                initialAmount = entryBeingEdited?.amount,
                initialJewellery = entryBeingEdited?.jewellery,
                initialIsCross = entryBeingEdited?.cross
            )
        }

        entryPendingCrossToggle?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryPendingCrossToggle = null },
                title = {
                    Text(text = if (pendingCrossValue) "Cross Entry?" else "Uncross Entry?")
                },
                text = {
                    Text(
                        text = if (pendingCrossValue)
                            "Are you sure you want to mark this entry as crossed?"
                        else
                            "Are you sure you want to unmark this entry?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.addOrUpdateEntry(
                                customerId = customerId,
                                entryId = entry.id,
                                amount = entry.amount,
                                jewellery = entry.jewellery,
                                cross = pendingCrossValue,
                                originalTime = entry.time
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

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            stickyHeader {
                Surface(
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by date or jewellery") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search Icon")
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            item {
                val allEntryInEntriesForCustomer = entryInEntriesMap[customerId]?.values?.flatten().orEmpty()

                val activeEntryInEntries = allEntryInEntriesForCustomer.filterNot { it.cross }
                val crossedEntryInEntries = allEntryInEntriesForCustomer.filter { it.cross }

                val totalPrincipal = activeEntryInEntries.sumOf { it.amount }
                val totalInterest = activeEntryInEntries.sumOf { calculateInterestFromEntryInEntry(it, it.interestRate, currentTime) }
                val totalAmount = totalPrincipal + totalInterest

                val paidPrincipal = crossedEntryInEntries.sumOf { it.amount }
                val paidInterest = crossedEntryInEntries.sumOf { calculateInterestFromEntryInEntry(it, it.interestRate, currentTime) }
                val paid = paidPrincipal + paidInterest

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = customer.customerName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        if (customer.customerTown.isNotBlank()) {
                            Text(
                                text = customer.customerTown,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        if (customer.number.isNotBlank()) {
                            Text(
                                text = customer.number,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unpaid Principal(शेष मूल्य): ₹${"%.2f".format(totalPrincipal)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Unpaid Interest(शेष ब्याज): ₹${"%.2f".format(totalInterest)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Unpaid Amount(शेष कुल): ₹${"%.2f".format(totalAmount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                            text = "Total Paid (वापस कुल): ₹%.2f".format(paid),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            itemsIndexed(filteredEntries) { index, entry ->
                EntryCard(
                    entry = entry,
                    entryInEntries = entryInEntriesMap[customerId]?.get(entry.id) ?: emptyList(),
                    onClick = {
                        navController.navigate(Screen.EntryInEntryScreen.createRoute(customerId = customerId, entryId = entry.id))
                    },
                    onEditClick = {
                        entryBeingEdited = entry
                        openAddEntryDialog = true
                    },
                    onDeleteClick = {
                        entryBeingDeleted = entry
                    },
                    onCrossToggle = { currentIsCross ->
                        entryPendingCrossToggle = entry
                        pendingCrossValue = !currentIsCross
                    }
                )

                if (index < filteredEntries.lastIndex) {
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateInterest(entry: Entry, interestRate : Double, currentTime: Long = System.currentTimeMillis()): Double {
    val zone = ZoneId.systemDefault()
    val startDate = Instant.ofEpochMilli(entry.time).atZone(zone).toLocalDate()
    val endDate = Instant.ofEpochMilli(currentTime).atZone(zone).toLocalDate()

    val fullMonthsPassed = ChronoUnit.MONTHS.between(startDate, endDate).toInt()
    val dateAfterFullMonths = startDate.plusMonths(fullMonthsPassed.toLong())
    val extraDays = ChronoUnit.DAYS.between(dateAfterFullMonths, endDate).toInt()

    val monthlyRate = interestRate / 100.0
    val dailyRate = monthlyRate / 30.0

    return if (fullMonthsPassed < 1) {
        entry.amount * monthlyRate
    } else {
        var principal = entry.amount
        var monthsRemaining = fullMonthsPassed

        while (monthsRemaining > 0) {
            val monthsToApply = minOf(monthsRemaining, 12)
            val interest = principal * monthlyRate * monthsToApply
            principal += interest
            monthsRemaining -= monthsToApply
        }

        val dailyInterest = principal * dailyRate * extraDays
        (principal + dailyInterest) - entry.amount
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateInterestFromEntryInEntry(entryInEntry: EntryInEntry, interestRate: Double, currentTime: Long = System.currentTimeMillis()): Double {
    val dummyEntry = Entry(
        id = "",
        amount = entryInEntry.amount,
        time = entryInEntry.time,
        jewellery = "",
        cross = entryInEntry.cross
    )
    return calculateInterest(dummyEntry, interestRate, currentTime)
}
