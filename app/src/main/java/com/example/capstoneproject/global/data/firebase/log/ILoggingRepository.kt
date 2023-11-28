package com.example.capstoneproject.global.data.firebase.log

import androidx.lifecycle.MutableLiveData
import java.util.Date

interface ILoggingRepository {
    fun getAll(callback: () -> Unit): MutableLiveData<List<Log>>
    fun log(log: Log)
}