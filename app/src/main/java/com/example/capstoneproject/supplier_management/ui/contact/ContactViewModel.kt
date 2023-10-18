package com.example.capstoneproject.supplier_management.ui.contact

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.data.firebase.contact.ContactRepository
import com.example.capstoneproject.supplier_management.data.firebase.contact.IContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    private lateinit var contacts: MutableLiveData<List<Contact>>
    private val contactRepository: IContactRepository = ContactRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)

    fun getAll(): MutableLiveData<List<Contact>> {
        if (!this::contacts.isInitialized) {
            contacts = contactRepository.getAll { updateLoadingState() }
        }

        return contacts
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

    private fun updateLoadingState() {
        isLoading.value = isLoading.value.not()
    }
}