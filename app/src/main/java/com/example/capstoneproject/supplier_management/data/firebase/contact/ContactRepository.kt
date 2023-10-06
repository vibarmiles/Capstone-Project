package com.example.capstoneproject.supplier_management.data.firebase.contact

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class ContactRepository : IContactRepository {
    private val firestore = Firebase.firestore
    private val contactCollectionReference = firestore.collection("contacts")

    override fun getAll(): MutableLiveData<List<Contact>> {
        var contacts = MutableLiveData<List<Contact>>()
        contactCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            value?.let {
                contacts.value = value.toObjects()
            }
        }
        return contacts
    }

    override fun insert(contact: Contact) {
        if (contact.id.isNotBlank()) {
            contactCollectionReference.document(contact.id).set(contact, SetOptions.merge())
        } else {
            contactCollectionReference.add(contact)
        }
    }

    override fun delete(contact: Contact) {
        contactCollectionReference.document(contact.id).delete()
    }
}