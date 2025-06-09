package com.newstudio.ujkhataapp.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.newstudio.ujkhataapp.data.model.Customer
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import com.newstudio.ujkhataapp.ui.component.AddCustomerDialog
import com.newstudio.ujkhataapp.ui.component.CustomerCard
import com.newstudio.ujkhataapp.viewModel.KhataViewModel

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerScreen(
    viewModel: KhataViewModel,
    navController: NavController,
) {
    val customers by viewModel.customers.collectAsState()
    val entriesMap by viewModel.entriesMap.collectAsState()
    val entryInEntryMap by viewModel.entryInEntryMap.collectAsState()

    val dialogState = remember { mutableStateOf(false) }
    val editingCustomer = remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var principal by remember { mutableDoubleStateOf(0.0) }
    var interest by remember { mutableDoubleStateOf(0.0) }
    var paidPrincipal by remember { mutableDoubleStateOf(0.0) }
    var paidInterest by remember { mutableDoubleStateOf(0.0) }
    var paid by remember { mutableDoubleStateOf(0.0) }

    val customerToDelete = remember { mutableStateOf<Customer?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    fun calculateEntrySumsFromSubEntries(
        nestedEntries: List<EntryInEntry>,
        currentTimeMillis: Long
    ): Quadruple<Double, Double, Double, Double> {
        var totalPrincipal = 0.0
        var totalInterest = 0.0
        var paidPrincipal = 0.0
        var paidInterest = 0.0

        nestedEntries.forEach { subEntry ->
            val interestForEntry = calculateInterestFromEntryInEntry(
                subEntry,
                subEntry.interestRate,
                currentTimeMillis
            )
            if (!subEntry.cross) {
                totalPrincipal += subEntry.amount
                totalInterest += interestForEntry
            } else {
                paidPrincipal += subEntry.amount
                paidInterest += interestForEntry
            }
        }

        return Quadruple(totalPrincipal, totalInterest, paidPrincipal, paidInterest)
    }

    LaunchedEffect(customers, entriesMap, entryInEntryMap) {
        var totalPrincipal = 0.0
        var totalInterest = 0.0
        var totalPaidPrincipal = 0.0
        var totalPaidInterest = 0.0

        val currentTime = System.currentTimeMillis()

        customers.forEach { customer ->
            val allEntries = entriesMap[customer.id].orEmpty()
            allEntries.forEach { entry ->
                val nestedEntries = entryInEntryMap[customer.id]?.get(entry.id).orEmpty()
                val (p, i, paidP, paidI) = calculateEntrySumsFromSubEntries(nestedEntries, currentTime)
                totalPrincipal += p
                totalInterest += i
                totalPaidPrincipal += paidP
                totalPaidInterest += paidI
            }
        }

        principal = totalPrincipal
        interest = totalInterest
        paidPrincipal = totalPaidPrincipal
        paidInterest = totalPaidInterest
        paid = totalPaidPrincipal + totalPaidInterest
    }

    val filteredCustomers = customers
        .filter {
            it.customerName.contains(searchQuery, ignoreCase = true) ||
                    it.customerTown.contains(searchQuery, ignoreCase = true)
        }
        .sortedByDescending { it.time }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCustomer.value = null
                    dialogState.value = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        if (showDeleteDialog.value && customerToDelete.value != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog.value = false
                    customerToDelete.value = null
                },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete ${customerToDelete.value?.customerName}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteCustomer(customerToDelete.value!!.id)
                        showDeleteDialog.value = false
                        customerToDelete.value = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog.value = false
                        customerToDelete.value = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (dialogState.value) {
            AddCustomerDialog(
                onDismissRequest = {
                    dialogState.value = false
                    editingCustomer.value = null
                },
                onConfirmation = { name, town, number ->
                    val existing = editingCustomer.value
                    if (existing != null) {
                        viewModel.addOrUpdateCustomer(existing.id, name, town, number)
                    } else {
                        viewModel.addOrUpdateCustomer(name, town, number)
                    }
                    dialogState.value = false
                    editingCustomer.value = null
                },
                dialogTitle = if (editingCustomer.value == null) "Add Customer" else "Edit Customer",
                icon = Icons.Default.Info,
                initialName = editingCustomer.value?.customerName ?: "",
                initialTown = editingCustomer.value?.customerTown ?: ""
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(bottom = 30.dp)
            ) {
                stickyHeader {
                    Surface(
                        shadowElevation = 4.dp,
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by name or town") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search Icon")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Summary",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Unpaid Principal (शेष मूल्य): ₹%.2f".format(principal))
                            Text("Unpaid Interest (शेष ब्याज): ₹%.2f".format(interest))
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Text("Unpaid Amount (शेष कुल): ₹%.2f".format(principal + interest))
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Text("Paid Principal (वापस मूल्य): ₹%.2f".format(paidPrincipal))
                            Text("Paid Interest (वापस ब्याज): ₹%.2f".format(paidInterest))
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Text("Total Paid (वापस कुल): ₹%.2f".format(paid))
                        }
                    }
                }

                items(filteredCustomers) { customer ->
                    CustomerCard(
                        navController = navController,
                        customer = customer,
                        onEditClick = {
                            editingCustomer.value = customer
                            dialogState.value = true
                        },
                        onDeleteClick = {
                            customerToDelete.value = customer
                            showDeleteDialog.value = true
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Made by Raj Bagri",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Contact No: 9302168251",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}
