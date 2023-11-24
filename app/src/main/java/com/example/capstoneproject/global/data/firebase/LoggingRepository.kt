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

                for (snapshot in value.toObjects<Log>().sortedBy { LocalDateTime.parse(it.date) }.toMutableList()) {
                    if (LocalDateTime.now().minusWeeks(1).isAfter(LocalDateTime.parse(snapshot.date))) {
                        android.util.Log.e("LOGGING", "DELETE")
                        logsCollectionReference.document(snapshot.id).delete()
                    } else {
                        if (logs.value!!.size > 100) {
                            android.util.Log.e("LOGGING", "DELETE")
                            logsCollectionReference.document(logs.value!!.sortedBy { LocalDateTime.parse(it.date) }.first().id).delete()
                        } else {
                            android.util.Log.e("LOGGING", "READ")
                        }
                    }
                }

                callback.invoke()
            }
        }

        return logs
    }

    override fun log(log: Log) {
        logsCollectionReference.count().get(AggregateSource.SERVER).addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.count >= 100) {
                    firestore.runTransaction { transaction ->
                        logsCollectionReference.orderBy("name").limit(1).get().addOnSuccessListener { snapshot ->
                            transaction.delete(logsCollectionReference.document(snapshot.first().toObject<Log>().id))
                        }
                    }
                }
            }

            logsCollectionReference.add(log)
        }
    }
}