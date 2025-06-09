package com.newstudio.ujkhataapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newstudio.ujkhataapp.data.model.Customer
import com.newstudio.ujkhataapp.data.model.Entry
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import com.newstudio.ujkhataapp.data.repository.KhataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KhataViewModel : ViewModel() {
    private val khataRepo = KhataRepository()

    val customers = MutableStateFlow<List<Customer>>(emptyList())
    val entriesMap = MutableStateFlow<Map<String, List<Entry>>>(emptyMap())
    val customerName = MutableStateFlow(Customer())

    // Nested Map: customerId -> (entryId -> List<EntryInEntry>)
    val entryInEntryMap = MutableStateFlow<Map<String, Map<String, List<EntryInEntry>>>>(emptyMap())

    init {
        getCustomers()
    }

    fun addOrUpdateCustomer(name: String, town: String, number: String) {
        khataRepo.addOrUpdateCustomer(name, town, number)
    }

    fun addOrUpdateCustomer(id: String, name: String, town: String, number: String) {
        khataRepo.addOrUpdateCustomer(id, name, town, number)
    }

    fun addOrUpdateEntry(
        customerId: String,
        amount: Double,
        entryId: String? = null,
        jewellery: String,
        cross: Boolean,
        originalTime: Long
    ) {
        viewModelScope.launch {
            khataRepo.addOrUpdateEntry(
                customerId = customerId,
                entryId = entryId,
                amount = amount,
                jewellery = jewellery,
                cross = cross,
                originalTime = originalTime
            )
            getEntries(customerId, useCache = false)
        }
    }

    fun getCustomers() {
        khataRepo.getCustomers().addSnapshotListener { snapshot, _ ->
            val list = snapshot?.toObjects(Customer::class.java) ?: emptyList()
            customers.value = list
            list.forEach { customer ->
                getEntries(customer.id, useCache = true)
            }
        }
    }

    fun getCustomersById(customerId: String, useCache: Boolean = true) {
        viewModelScope.launch {
            customerName.value = khataRepo.getCustomerById(customerId, useCache) ?: Customer()
        }
    }

    fun getEntries(customerId: String, useCache: Boolean = true) {
        viewModelScope.launch {
            val list = khataRepo.getEntries(customerId, useCache)
            entriesMap.value = entriesMap.value.toMutableMap().apply {
                put(customerId, list)
            }
            // Also fetch entry-in-entries for each entry
            list.forEach { entry ->
                getEntryInEntries(customerId, entry.id, useCache)
            }
        }
    }

    fun deleteCustomer(customerId: String) {
        khataRepo.deleteCustomers(customerId)
        entriesMap.value = entriesMap.value.toMutableMap().apply {
            remove(customerId)
        }
        entryInEntryMap.value = entryInEntryMap.value.toMutableMap().apply {
            remove(customerId)
        }
    }

    fun deleteEntry(customerId: String, entryId: String) {
        khataRepo.deleteEntry(customerId, entryId) {
            getEntries(customerId, useCache = false)
        }
    }

    // ---------------- EntryInEntry Functions ----------------

    fun addOrUpdateEntryInEntry(
        customerId: String,
        parentEntryId: String,
        entryInEntry: EntryInEntry,
        interestRate: Double
    ) {
        viewModelScope.launch {
            khataRepo.addOrUpdateEntryInEntry(customerId, parentEntryId, entryInEntry, interestRate)
            getEntryInEntries(customerId, parentEntryId, useCache = false)
        }
    }

    fun getEntryInEntries(customerId: String, parentEntryId: String, useCache: Boolean = true) {
        viewModelScope.launch {
            val subEntries = khataRepo.getEntryInEntries(customerId, parentEntryId, useCache)
            val customerSubMap = entryInEntryMap.value[customerId]?.toMutableMap() ?: mutableMapOf()
            customerSubMap[parentEntryId] = subEntries
            entryInEntryMap.value = entryInEntryMap.value.toMutableMap().apply {
                put(customerId, customerSubMap)
            }
        }
    }

    fun deleteEntryInEntry(
        customerId: String,
        parentEntryId: String,
        entryInEntryId: String
    ) {
        khataRepo.deleteEntryInEntry(customerId, parentEntryId, entryInEntryId) {
            getEntryInEntries(customerId, parentEntryId, useCache = false)
        }
    }
}
