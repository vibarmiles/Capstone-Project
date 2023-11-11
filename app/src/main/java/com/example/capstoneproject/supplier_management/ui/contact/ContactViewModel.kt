package com.example.capstoneproject.supplier_management.ui.contact

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.data.firebase.contact.ContactRepository
import com.example.capstoneproject.supplier_management.data.firebase.contact.IContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    private lateinit var contacts: MutableLiveData<List<Contact>>
    private val contactRepository: IContactRepository = ContactRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<Contact>> {
        if (!this::contacts.isInitialized) {
            contacts = contactRepository.getAll(callback = { updateLoadingState() }) {
                    result ->
                resultState.update { result }
            }
        }

        return contacts
    }

    fun insert(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.insert(contact = contact) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.delete(contact = contact) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getContact(id: String?): Contact? {
        return contacts.value?.firstOrNull { contact -> contact.id == id }
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }
}