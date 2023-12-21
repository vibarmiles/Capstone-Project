package com.example.capstoneproject.supplier_management.data.firebase.contact

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult

interface IContactRepository {
    fun getAll(callback: () -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Contact>>
    fun insert(contact: Contact, result: (FirebaseResult) -> Unit)
    fun delete(contact: Contact, result: (FirebaseResult) -> Unit)
    fun archiveItem(contact: Contact, result: (FirebaseResult) -> Unit)
}