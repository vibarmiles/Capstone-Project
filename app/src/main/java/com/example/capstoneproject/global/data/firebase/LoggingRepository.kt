package com.example.capstoneproject.global.data.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

class LoggingRepository : ILoggingRepository {
    private val firestore = Firebase.firestore
    private val logsCollectionReference = firestore.collection("activity_logs")
    private val logs = MutableLiveData<List<Log>>()


    override fun getAll(callback: () -> Unit): MutableLiveData<List<Log>> {
        logsCollectionReference.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            value?.let {
                logs.value = value.toObjects()

                callback.invoke()
            }
        }

        return logs
    }

    override fun log(log: Log) {
        logsCollectionReference.count().get(AggregateSource.SERVER).addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.count + 1 > 100) {
                    logsCollectionReference.orderBy("date").limit(1).get().addOnSuccessListener { snapshot ->
                        firestore.runBatch { batch ->
                            batch.delete(logsCollectionReference.document(snapshot.first().toObject<Log>().id))
                        }
                    }
                }
            }

            logsCollectionReference.add(log)
        }
    }
}