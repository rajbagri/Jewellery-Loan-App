package com.newstudio.ujkhataapp.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.newstudio.ujkhataapp.data.model.Customer
import com.newstudio.ujkhataapp.data.model.Entry
import com.newstudio.ujkhataapp.data.model.EntryInEntry
import kotlinx.coroutines.tasks.await

class KhataRepository {
    private val db = Firebase.firestore
    private val TAG = "KhataRepository"

    fun getCustomers(): CollectionReference {
        Log.d(TAG, "Fetching customer collection")
        return db.collection("Khata")
    }

    fun addOrUpdateCustomer(customerId: String, name: String, town: String, number: String) {
        val customer = Customer(customerId, name, town, number)
        db.collection("Khata").document(customerId).set(customer)
    }

    fun addOrUpdateCustomer(name: String, town: String, number: String) {
        val collection = db.collection("Khata")

        collection
            .whereEqualTo("name", name)
            .whereEqualTo("town", town)
            .whereEqualTo("number", number)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val updatedCustomer = Customer(doc.id, name, town, number)
                    collection.document(doc.id).set(updatedCustomer)
                } else {
                    val id = collection.document().id
                    val newCustomer = Customer(id, name, town, number)
                    collection.document(id).set(newCustomer)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking existing customer", e)
            }
    }

    fun addOrUpdateEntry(
        customerId: String,
        entryId: String? = null,
        amount: Double,
        jewellery: String,
        cross: Boolean,
        originalTime: Long
    ) {
        val entryDocRef = if (entryId != null) {
            db.collection("Khata").document(customerId)
                .collection("entries").document(entryId)
        } else {
            db.collection("Khata").document(customerId)
                .collection("entries").document() // auto-generate ID
        }

        val id = entryDocRef.id
        val entry = Entry(
            id = id,
            amount = amount,
            jewellery = jewellery,
            cross = cross,
            time = originalTime
        )

        entryDocRef.set(entry)
    }

    suspend fun getEntries(customerId: String, useCache: Boolean = true): List<Entry> {
        return try {
            val source = if (useCache) Source.CACHE else Source.SERVER
            val result = db.collection("Khata").document(customerId)
                .collection("entries")
                .get(source)
                .await()
            result.toObjects(Entry::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching entries for customerId: $customerId", e)
            emptyList()
        }
    }

    suspend fun getCustomerById(customerId: String, useCache: Boolean = true): Customer? {
        return try {
            val source = if (useCache) Source.CACHE else Source.SERVER
            val document = db.collection("Khata").document(customerId)
                .get(source)
                .await()
            document.toObject(Customer::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching customer by ID: $customerId", e)
            null
        }
    }

    fun deleteCustomers(customerId: String) {
        db.collection("Khata").document(customerId).delete()
    }

    fun deleteEntry(customerId: String, entryId: String, onSuccess: () -> Unit) {
        db.collection("Khata").document(customerId)
            .collection("entries").document(entryId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Entry $entryId deleted successfully for customer $customerId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting entry $entryId for customer $customerId", e)
            }
    }

    fun addOrUpdateEntryInEntry(
        customerId: String,
        parentEntryId: String,
        entryInEntry: EntryInEntry,
        interestRate: Double
    ) {
        val docRef = db.collection("Khata").document(customerId)
            .collection("entries").document(parentEntryId)
            .collection("subEntries").document(entryInEntry.id.ifBlank { db.collection("unused").document().id })

        val entryWithId = entryInEntry.copy(id = docRef.id, interestRate = interestRate)

        docRef.set(entryWithId)
            .addOnSuccessListener {
                Log.d(TAG, "EntryInEntry ${entryWithId.id} added/updated for entry $parentEntryId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding/updating EntryInEntry", e)
            }
    }

    suspend fun getEntryInEntries(
        customerId: String,
        parentEntryId: String,
        useCache: Boolean = true
    ): List<EntryInEntry> {
        return try {
            val source = if (useCache) Source.CACHE else Source.SERVER
            val snapshot = db.collection("Khata").document(customerId)
                .collection("entries").document(parentEntryId)
                .collection("subEntries")
                .get(source)
                .await()
            snapshot.toObjects(EntryInEntry::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching EntryInEntries for entry $parentEntryId", e)
            emptyList()
        }
    }

    fun deleteEntryInEntry(
        customerId: String,
        parentEntryId: String,
        entryInEntryId: String,
        onSuccess: () -> Unit = {}
    ) {
        db.collection("Khata").document(customerId)
            .collection("entries").document(parentEntryId)
            .collection("subEntries").document(entryInEntryId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "EntryInEntry $entryInEntryId deleted successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting EntryInEntry $entryInEntryId", e)
            }
    }
}
