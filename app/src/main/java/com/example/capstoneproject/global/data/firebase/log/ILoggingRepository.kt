package com.example.capstoneproject.global.data.firebase.log

import androidx.lifecycle.MutableLiveData
import java.util.Date

interface ILoggingRepository {
    fun log(log: Log)
}