package com.example.capstoneproject.activity_logs.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstoneproject.activity_logs.data.firebase.ActivityLogsRepository
import com.example.capstoneproject.activity_logs.data.firebase.IActivityLogsRepository
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.global.data.firebase.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActivityLogsViewModel : ViewModel() {
    private lateinit var logs: MutableLiveData<List<Log>>
    val activityLogsRepository: IActivityLogsRepository = ActivityLogsRepository()
    val isLoading = mutableStateOf(true)
    val update = mutableStateOf(true)
    private val resultState = MutableStateFlow(FirebaseResult())
    val result = resultState.asStateFlow()
    val returnSize = mutableStateOf(0)

    fun getLogs(): MutableLiveData<List<Log>> {
        if (!this::logs.isInitialized) {
            logs = activityLogsRepository.getAll(callback = { updateLoadingState(); returnSize.value = it }) { result ->
                resultState.update { result }
            }
        }

        return logs
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            logs = activityLogsRepository.getAll(callback = { returnSize.value = it }) { result ->
                resultState.update { result }
            }
        }
    }

    private fun updateLoadingState() {
        isLoading.value = false
    }

    fun resetMessage() {
        resultState.update { FirebaseResult() }
    }
}