package com.example.capstoneproject.global.data.firebase

import androidx.lifecycle.MutableLiveData

interface ILoggingRepository {
    fun getAll(callback: () -> Unit): MutableLiveData<List<Log>>
    fun log(log: Log)
}