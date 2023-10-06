package com.example.capstoneproject.supplier_management.ui.contact

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.data.firebase.contact.ContactRepository
import com.example.capstoneproject.supplier_management.data.firebase.contact.IContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    val contacts: MutableLiveData<List<Contact>>
    private val contactRepository: IContactRepository = ContactRepository()

    init {
        contacts = contactRepository.getAll()
    }

    fun insert(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.insert(contact = contact)
        }
    }

    fun delete(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.delete(contact = contact)
        }
    }
}