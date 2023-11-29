package com.example.capstoneproject.activity_logs.data.firebase

import androidx.lifecycle.MutableLiveData
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.global.data.firebase.log.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ActivityLogsRepository : IActivityLogsRepository {
    private val firestore = Firebase.firestore
    private val logsCollectionReference = firestore.collection("activity_logs")
    private val logs = MutableLiveData<List<Log>>()
    private var query = logsCollectionReference.orderBy("date", Query.Direction.DESCENDING)
    private var currentSize = 10
    private lateinit var document: DocumentSnapshot

    override fun getAll(callback: (Int) -> Unit, result: (FirebaseResult) -> Unit): MutableLiveData<List<Log>> {
        if (this::document.isInitialized) {
            query = query.startAfter(document)
        }

        query.limit(10).addSnapshotListener { value, error ->
            error?.let {
                result.invoke(FirebaseResult(errorMessage = it.message))
                return@addSnapshotListener
            }
            value?.let {
                val current = logs.value?.toMutableList() ?: mutableListOf()

                if (it.size() > 0) {
                    document = it.documents[it.size() - 1]
                    currentSize = if (currentSize == 10) it.size() else 0
                }

                for (queryDocumentSnapshot in it) {
                    val new = queryDocumentSnapshot.toObject<Log>()
                    current.firstOrNull { log -> log.id == new.id }.let { found ->
                        if (found != null) {
                            current[current.indexOf(found)] = new
                        } else {
                            current.add(new)
                        }
                    }
                }

                logs.value = current

                if (currentSize > 0) {
                    callback.invoke(it.size())
                } else {
                    callback.invoke(0)
                }
            }
        }

        return logs
    }
}