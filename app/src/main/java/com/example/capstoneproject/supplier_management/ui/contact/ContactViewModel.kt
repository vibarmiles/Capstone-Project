package com.example.capstoneproject.supplier_management.ui.contact

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.data.firebase.contact.ContactRepository
import com.example.capstoneproject.supplier_management.data.firebase.contact.IContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    val contacts: SnapshotStateMap<String, Contact>
    private val contactRepository: IContactRepository = ContactRepository()

    init {
        contacts = contactRepository.getAll()
    }

    fun insert(key: String? = null, contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.insert(key = key, contact = contact)
        }
    }

    fun addProductForSupplier(key: String, product: Map<String, Double>) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.addProductsForSupplier(key = key, product = product)
        }
    }

    fun removeProductForSupplier(contactId: String, productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.removeProductForSupplier(contactId = contactId, productId = productId)
        }
    }

    fun delete(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.delete(key = key)
        }
    }
}