package com.example.capstoneproject.supplier_management.ui.contact

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.supplier_management.data.firebase.contact.Contact
import com.example.capstoneproject.supplier_management.data.firebase.contact.ContactRepository
import com.example.capstoneproject.supplier_management.data.firebase.contact.IContactRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    fun archiveItem(contact: Contact, remove: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  "/${contact.name}_(${
                    LocalDate.now().format(
                        DateTimeFormatter.ISO_LOCAL_DATE)}).json")
            val gson = Gson()
            val json = gson.toJson(contact)

            try {
                file.writeText(json)
            } catch (e: IOException) {
                resultState.update { FirebaseResult(errorMessage = e.message) }
            }

            if (remove) {
                contactRepository.archiveItem(contact = contact) {
                    resultState.update { it }
                }
            }
        }
    }

    fun readFromJson(file: InputStream): Contact {
        val gson = Gson()
        val json: String
        return try {
            json = file.bufferedReader().use { it.readText() }
            gson.fromJson(json, Contact::class.java)
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
            Contact()
        }
    }
}