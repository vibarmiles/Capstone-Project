package com.example.capstoneproject.activity_logs.data.firebase

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.global.data.firebase.log.Log

interface IActivityLogsRepository {
    fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Log>>
}