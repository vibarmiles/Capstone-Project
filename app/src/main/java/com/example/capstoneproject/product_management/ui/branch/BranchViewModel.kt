package com.example.capstoneproject.product_management.ui.branch

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.product_management.data.firebase.branch.Branch
import com.example.capstoneproject.product_management.data.firebase.branch.BranchRepository
import com.example.capstoneproject.product_management.data.firebase.branch.IBranchRepository
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

class BranchViewModel : ViewModel() {
    private lateinit var branches: MutableLiveData<List<Branch>>
    private val branchRepository: IBranchRepository = BranchRepository()
    var isLoading: MutableState<Boolean> = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()

    fun getAll(): MutableLiveData<List<Branch>> {
        if (!this::branches.isInitialized) {
            branches = branchRepository.getAll(callback = { updateLoadingState() }) { result ->
                resultState.update { result }
            }

        }

        return branches
    }

    fun insert(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            branchRepository.insert(branch = branch) {
                    result ->
                resultState.update { result }
            }
        }
    }

    fun delete(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            branchRepository.delete(branch = branch) {
                    result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun getBranch(id: String?): Branch? {
        return branches.value?.firstOrNull { branch -> branch.id == id }
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }

    fun archiveItem(branch: Branch, remove: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),  "/${branch.name}_(${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}).json")
            val gson = Gson()
            val json = gson.toJson(branch)

            try {
                file.writeText(json)
            } catch (e: IOException) {
                resultState.update { FirebaseResult(errorMessage = e.message) }
            }

            if (remove) {
                branchRepository.archiveItem(branch = branch) {
                    resultState.update { it }
                }
            }
        }
    }

    fun readFromJson(file: InputStream): Branch {
        val gson = Gson()
        val json: String
        return try {
            json = file.bufferedReader().use { it.readText() }
            gson.fromJson(json, Branch::class.java)
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
            Branch()
        }
    }
}