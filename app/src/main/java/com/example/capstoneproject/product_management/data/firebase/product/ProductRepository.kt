package com.example.capstoneproject.product_management.data.firebase.product

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.capstoneproject.global.data.firebase.FirebaseResult
import com.example.capstoneproject.point_of_sales.data.firebase.Invoice
import com.example.capstoneproject.point_of_sales.data.firebase.InvoiceType
import com.example.capstoneproject.supplier_management.data.firebase.purchase_order.PurchaseOrder
import com.example.capstoneproject.supplier_management.data.firebase.return_order.ReturnOrder
import com.example.capstoneproject.supplier_management.data.firebase.transfer_order.TransferOrder
import com.google.firebase.database.*
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ProductRepository : IProductRepository {
    private val firebase = Firebase.database.reference
    private val productCollectionReference = firebase.child("products")
    private val firestorage = Firebase.storage.reference
    private val productImageReference = firestorage.child("images")

    override fun getAll(callback: () -> Unit, update: () -> Unit, result: (FirebaseResult) -> Unit): SnapshotStateMap<String, Product> {
        val products = mutableStateMapOf<String, Product>()

        productCollectionReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
                Log.d("Added", snapshot.value.toString())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
                update.invoke()
                Log.d("Updated", snapshot.value.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                products.remove(snapshot.key)
                update.invoke()
                Log.d("Removed", snapshot.value.toString())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                products[snapshot.key!!] = snapshot.getValue<Product>()!!
            }

            override fun onCancelled(error: DatabaseError) {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
            }

        })

        productCollectionReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.invoke()
            }

            override fun onCancelled(error: DatabaseError) {
                result.invoke(FirebaseResult(result = false, errorMessage = error.message))
            }
        })

        return products
    }

    override fun setQuantityForBranch(key: String, value: Map<String, Int>, result: (FirebaseResult) -> Unit) {
        productCollectionReference.child(key).child("stock").setValue(value).addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }

    override fun setMonthlySales(
        key: String,
        value: Map<String, Int>,
        year: Int,
        result: (FirebaseResult) -> Unit
    ) {
        productCollectionReference.child(key).child("transaction").child("monthlySales").child(year.toString()).setValue(value).addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }

    override fun insert(key: String?, product: Product, result: (FirebaseResult) -> Unit) {
        val uri: Uri? = if (product.image != null) Uri.parse(product.image) else null

        if (key != null) {
            Log.d("ID is not blank", key)
            if (uri != null) {
                Log.d("Old Image", uri.lastPathSegment.toString())
                firestorage.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                    product.image = it.toString()
                    Log.d("Image", product.image.toString())
                    productCollectionReference.child(key).setValue(product).addOnSuccessListener {
                        result.invoke(FirebaseResult(result = true))
                    }.addOnFailureListener {
                            e ->
                        result.invoke(FirebaseResult(result = false, errorMessage = e.message))
                    }
                }.addOnFailureListener {
                    Log.d("Image", "File does not exist")
                    productImageReference.child(uri.lastPathSegment!!).putFile(uri).addOnSuccessListener {
                        productImageReference.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                            product.image = it.toString()
                            Log.d("Image", product.image.toString())
                            productCollectionReference.child(key).setValue(product).addOnSuccessListener {
                                result.invoke(FirebaseResult(result = true))
                            }.addOnFailureListener {
                                    e ->
                                result.invoke(FirebaseResult(result = false, errorMessage = e.message))
                            }
                        }
                    }
                }
            } else {
                productCollectionReference.child(key).setValue(product).addOnSuccessListener {
                    result.invoke(FirebaseResult(result = true))
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            }
        } else {
            Log.d("ID is blank", product.toString())
            if (uri != null) {
                productImageReference.child(uri.lastPathSegment!!).putFile(uri).addOnSuccessListener {
                    productImageReference.child(uri.lastPathSegment!!).downloadUrl.addOnSuccessListener {
                        product.image = it.toString()
                        Log.d("Image", product.image.toString())
                        productCollectionReference.push().setValue(product).addOnSuccessListener {
                            result.invoke(FirebaseResult(result = true))
                        }.addOnFailureListener { exception ->
                            result.invoke(FirebaseResult(result = false, errorMessage = exception.message))
                        }
                    }
                }
            } else {
                productCollectionReference.push().setValue(product).addOnSuccessListener {
                    result.invoke(FirebaseResult(result = true))
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            }
        }
    }

    override fun delete(key: String, product: Product, result: (FirebaseResult) -> Unit) {
        productCollectionReference.child(key).setValue(product.copy(active = false)).addOnSuccessListener {
            result.invoke(FirebaseResult(result = true))
        }.addOnFailureListener {
            result.invoke(FirebaseResult(result = false, errorMessage = it.message))
        }
    }

    override fun removeCategory(categoryId: String, result: (FirebaseResult) -> Unit) = productCollectionReference.orderByChild("category").equalTo(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (child in snapshot.children) {
                child.ref.child("category").removeValue().addOnSuccessListener {
                    result.invoke(FirebaseResult(result = true))
                }.addOnFailureListener {
                    result.invoke(FirebaseResult(result = false, errorMessage = it.message))
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            result.invoke(FirebaseResult(result = false, errorMessage = error.message))
        }

    })

    override fun removeBranchStock(branchId: String, result: (FirebaseResult) -> Unit) = productCollectionReference.orderByChild("stock/$branchId").startAt(0.0).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.d("Calling", "Remove Branch Stock")
            for (child in snapshot.children) {
                child.ref.child("stock/$branchId").removeValue()
            }

            result.invoke(FirebaseResult(result = true))
        }

        override fun onCancelled(error: DatabaseError) {
            result.invoke(FirebaseResult(result = false, errorMessage = error.message))
        }

    })

    override fun transact(document: Any, result: (FirebaseResult) -> Unit) {
        productCollectionReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentProducts = currentData.getValue<Map<String, Product>>() ?: mapOf()

                when (document) {
                    is PurchaseOrder -> {
                        Log.e("Document", "Purchase Order")

                        if (!document.products.values.all { it.id in currentProducts.keys }) {
                            Log.e("MISSING", "KEY")
                            return Transaction.abort()
                        }

                        Log.e("KEY", "ALL IN")

                        for (product in document.products.values) {
                            currentProducts[product.id]!!.let { current ->
                                currentData.child(product.id).value = current.copy(stock = current.stock.toMutableMap().let { stock ->
                                    stock[document.branchId] = stock.getOrDefault(document.branchId, 0) + product.quantity
                                    stock
                                }, transaction = current.transaction.let { it.copy(purchased = it.purchased + product.quantity) })
                            }
                        }

                        return Transaction.success(currentData)
                    }

                    is ReturnOrder -> {
                        Log.e("Document", "Return Order")

                        if (!document.products.values.all { it.id in currentProducts.keys }) {
                            return Transaction.abort()
                        }

                        for (product in document.products.values) {
                            if (currentProducts[product.id]!!.stock.getOrDefault(key = document.branchId, defaultValue = 0) - product.quantity < 0) {
                                return Transaction.abort()
                            }
                        }


                        for (product in document.products.values) {
                            currentProducts[product.id]!!.let { current ->
                                currentData.child(product.id).value = current.copy(stock = current.stock.toMutableMap().let { stock ->
                                    stock[document.branchId] = stock.getOrDefault(document.branchId, 0) - product.quantity
                                    stock
                                }, transaction = current.transaction.let { transaction ->
                                    transaction.copy(purchased = (transaction.purchased - product.quantity).let { if (it > 0) it else 0 })
                                })
                            }
                        }

                        return Transaction.success(currentData)
                    }

                    is TransferOrder -> {
                        Log.e("Document", "Transfer Order")

                        if (!document.products.values.all { it.id in currentProducts.keys }) {
                            return Transaction.abort()
                        }

                        for (product in document.products.values) {
                            if (currentProducts[product.id]!!.stock.getOrDefault(key = document.oldBranchId, defaultValue = 0) - product.quantity < 0) {
                                return Transaction.abort()
                            }
                        }

                        for (product in document.products.values) {
                            currentProducts[product.id]!!.let { current ->
                                currentData.child(product.id).value = current.copy(stock = current.stock.toMutableMap().let { stock ->
                                    stock[document.oldBranchId] = stock.getOrDefault(document.destinationBranchId, 0) - product.quantity
                                    stock[document.destinationBranchId] = stock.getOrDefault(document.destinationBranchId, 0) + product.quantity
                                    stock
                                })
                            }
                        }

                        return Transaction.success(currentData)
                    }

                    is Invoice -> {
                        Log.e("Document", "Sales Invoice")

                        if (!document.products.values.all { it.id in currentProducts.keys }) {
                            return Transaction.abort()
                        }

                        if (document.invoiceType != InvoiceType.REFUND) {
                            for (product in document.products.values) {
                                if (currentProducts[product.id]!!.stock.getOrDefault(key = document.branchId, defaultValue = 0) - product.quantity < 0) {
                                    return Transaction.abort()
                                }
                            }
                        }

                        when (document.invoiceType) {
                            InvoiceType.SALE -> {
                                for (product in document.products.values) {
                                    currentProducts[product.id]!!.let { current ->
                                        currentData.child(product.id).value = current.copy(stock = current.stock.toMutableMap().let { stock ->
                                            stock[document.branchId] = stock.getOrDefault(document.branchId, 0) - product.quantity
                                            stock
                                        }, transaction = current.transaction.let { transaction ->
                                            transaction.copy(
                                                soldThisYear = transaction.soldThisYear + product.quantity,
                                                soldThisMonth = transaction.soldThisMonth + product.quantity
                                            )
                                        })
                                    }
                                }
                            }

                            InvoiceType.REFUND -> {
                                for (product in document.products.values) {
                                    currentProducts[product.id]!!.let { current ->
                                        currentData.child(product.id).value = current.copy(transaction = current.transaction.let { it.copy(soldThisMonth = (it.soldThisMonth - product.quantity), soldThisYear = (it.soldThisYear - product.quantity)) })
                                    }
                                }
                            }

                            InvoiceType.EXCHANGE -> {
                                for (product in document.products.values) {
                                    currentProducts[product.id]!!.let { current -> currentData.child(product.id).value = current.copy(stock = current.stock.toMutableMap().let { stock ->
                                        stock[document.branchId] = stock.getOrDefault(document.branchId, 0) - product.quantity
                                        stock
                                    }) }
                                }
                            }
                        }

                        return Transaction.success(currentData)
                    }
                }

                Log.e("LAST ERROR", "LAST ERROR")
                return Transaction.abort()
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (!committed) {
                    result.invoke(FirebaseResult(errorMessage = "Error has occurred. Please check current document and/or inventory then try again later!"))
                } else {
                    result.invoke(FirebaseResult(result = true))
                }
            }
        })
    }

    override fun checkDate(date: LocalDate, callback: (Float) -> Unit) {
        productCollectionReference.get().addOnSuccessListener {
            val products = it.getValue<Map<String, Product>>()
            val numberOfProducts = products?.size ?: 0
            products?.toList()?.forEachIndexed { index, product ->
                val lastEditDate = Instant.ofEpochMilli(product.second.lastEdit as Long).atZone(ZoneId.systemDefault()).toLocalDate()
                if (lastEditDate.month != date.month) {
                    product.second.let { current ->
                        productCollectionReference.child(product.first).setValue(current.copy(lastEdit = ServerValue.TIMESTAMP, transaction = current.transaction.let { transaction ->
                            val didYearChange = lastEditDate.year != date.year
                            val yearSale = transaction.monthlySales.toMutableMap().let { monthlySale ->
                                monthlySale[date.year.toString()].let { _ ->
                                    if (!monthlySale.containsKey(date.year.toString())) {
                                        monthlySale.putIfAbsent(date.year.toString(), mapOf(Pair(date.minusMonths(1).month.name, transaction.soldThisMonth)))
                                    } else {
                                        val map = monthlySale[date.year.toString()]!!.toMutableMap()
                                        map[date.minusMonths(1).month.name] = transaction.soldThisMonth
                                        monthlySale[date.year.toString()] = map
                                    }

                                    monthlySale
                                }
                                monthlySale.remove(date.minusYears(5).year.toString())
                                monthlySale
                            }
                            transaction.copy(
                                openingStock = current.stock.values.sum(),
                                closingStock = transaction.openingStock,
                                soldThisMonth = 0,
                                purchased = if (didYearChange) 0 else transaction.purchased,
                                soldThisYear = yearSale[date.year.toString()]?.values?.sum() ?: 0,
                                monthlySales = yearSale
                            )
                        })).addOnCompleteListener {
                            callback.invoke((index + 1).toFloat() / numberOfProducts)
                        }
                    }
                } else {
                    callback.invoke((index + 1).toFloat() / numberOfProducts)
                }
            }
        }
    }
}