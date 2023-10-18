package com.example.capstoneproject.supplier_management.data.firebase.contact

import androidx.lifecycle.MutableLiveData

interface IContactRepository {
    fun getAll(callback: () -> Unit): MutableLiveData<List<Contact>>
    fun insert(contact: Contact)
    fun delete(contact: Contact)
}