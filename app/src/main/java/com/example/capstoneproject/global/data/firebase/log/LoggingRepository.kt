package com.example.capstoneproject.global.data.firebase.log

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import java.util.*

class LoggingRepository : ILoggingRepository {
    private val firestore = Firebase.firestore
    private val logsCollectionReference = firestore.collection("activity_logs")

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