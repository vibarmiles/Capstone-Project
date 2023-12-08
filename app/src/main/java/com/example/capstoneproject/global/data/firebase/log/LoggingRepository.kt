package com.example.capstoneproject.global.data.firebase.log

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class LoggingRepository : ILoggingRepository {
    private val firestore = Firebase.firestore
    private val logsCollectionReference = firestore.collection("activity_logs")

    override fun log(log: Log) {
        logsCollectionReference.count().get(AggregateSource.SERVER).addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.count + 1 > 200) {
                    logsCollectionReference.orderBy("date").limit((it.result.count + 1) - 200).get().addOnSuccessListener { snapshot ->
                        firestore.runBatch { batch ->
                            for (document in snapshot.toObjects<Log>()) {
                                batch.delete(logsCollectionReference.document(document.id))
                            }
                        }
                    }
                }
            }
        }
        logsCollectionReference.add(log)
    }
}