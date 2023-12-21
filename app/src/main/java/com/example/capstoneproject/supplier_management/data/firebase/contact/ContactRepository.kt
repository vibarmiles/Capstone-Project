package com.example.capstoneproject.supplier_management.data.firebase.contact

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class ContactRepository : IContactRepository {
    private val firestore = Firebase.firestore
    private val contactCollectionReference = firestore.collection("contacts")

    override fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Contact>> {
        var contacts = MutableLiveData<List<Contact>>()
        contactCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
                return@addSnapshotListener
            }
            value?.let {
                contacts.value = value.toObjects()
                callback.invoke()
            }
        }
        return contacts
    }

    override fun insert(contact: Contact, result: (FirebaseResult) -> Unit) {
        if (contact.id.isNotBlank()) {
            contactCollectionReference.document(contact.id).set(contact, SetOptions.merge()).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        } else {
            contactCollectionReference.add(contact).addOnSuccessListener {
                result.invoke(FirebaseResult(result = true))
            }.addOnFailureListener {
                result.invoke(FirebaseResult(result = false, errorMessage = it.message))
            }
        }
    }

    override fun delete(contact: Contact, result: (FirebaseResult) -> Unit) {
        contactCollectionReference.document(contact.id).set(contact.copy(active = contact.active.not())).addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }

    override fun archiveItem(contact: Contact, result: (FirebaseResult) -> Unit) {
        contactCollectionReference.document(contact.id).delete().addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }
}