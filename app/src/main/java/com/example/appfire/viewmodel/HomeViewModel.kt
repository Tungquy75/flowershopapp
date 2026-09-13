package com.example.appfire.viewmodel


import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appfire.model.Category
import com.example.appfire.model.Items
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class HomeViewModel : ViewModel() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _category = MutableLiveData<List<Category>>()
    private val _recomended = MutableLiveData<List<Items>>()
    private val _filteredItems = MutableLiveData<List<Items>>()

    val categories: LiveData<List<Category>> = _category
    val recomended: LiveData<List<Items>> = _recomended
    val filteredItems: LiveData<List<Items>> = _filteredItems

    fun loadFiltered(id: String) {
        val ref = firebaseDatabase.getReference("Items")
        val query: Query = ref.orderByChild("categoryId").equalTo(id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<Items>()
                for (childSnapshot in snapshot.children) {
                    if (!childSnapshot.exists()) continue

                    try {
                        val value = childSnapshot.getValue() as? Map<*, *>
                        if (value != null) {
                            val picUrlList = when (val picUrlData = value["picUrl"]) {
                                is List<*> -> picUrlData.mapNotNull { it as? String }.toCollection(ArrayList())
                                is String -> arrayListOf(picUrlData)
                                else -> ArrayList()
                            }

                            val item = Items(
                                title = value["title"] as? String ?: "",
                                description = value["description"] as? String ?: "",
                                picUrl = picUrlList,
                                model = (value["model"] as? List<*>)?.mapNotNull { it as? String }
                                    ?.toCollection(ArrayList()) ?: ArrayList(),
                                price = (value["price"] as? Number)?.toDouble() ?: 0.0,
                                rating = (value["rating"] as? Number)?.toDouble() ?: 0.0,
                                numberInCart = (value["numberInCart"] as? Number)?.toInt() ?: 0,
                                showRecommended = value["showRecommended"] as? Boolean ?: false,
                                categoryId = value["categoryId"] as? String ?: ""
                            )
                            lists.add(item)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeViewModel", "Error parsing item: ${e.message}")
                    }
                }
                _filteredItems.postValue(lists)  // Dùng postValue
                android.util.Log.d("HomeViewModel", "Filtered items count: ${lists.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("HomeViewModel", "loadFiltered error: ${error.message}")
                _filteredItems.postValue(emptyList())  // Trả về emptyList thay vì mutableListOf
            }
        })
    }

    fun loadRecomended() {
        val ref = firebaseDatabase.getReference("Items")
        val query: Query = ref.orderByChild("showRecommended").equalTo(true)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<Items>()
                for (childSnapshot in snapshot.children) {
                    if (!childSnapshot.exists()) continue

                    try {
                        val value = childSnapshot.getValue() as? Map<*, *>
                        if (value != null) {
                            val picUrlList = when (val picUrlData = value["picUrl"]) {
                                is List<*> -> picUrlData.mapNotNull { it as? String }.toCollection(ArrayList())
                                is String -> arrayListOf(picUrlData)
                                else -> ArrayList()
                            }

                            val item = Items(
                                title = value["title"] as? String ?: "",
                                description = value["description"] as? String ?: "",
                                picUrl = picUrlList,
                                model = (value["model"] as? List<*>)?.mapNotNull { it as? String }
                                    ?.toCollection(ArrayList()) ?: ArrayList(),
                                price = (value["price"] as? Number)?.toDouble() ?: 0.0,
                                rating = (value["rating"] as? Number)?.toDouble() ?: 0.0,
                                numberInCart = (value["numberInCart"] as? Number)?.toInt() ?: 0,
                                showRecommended = value["showRecommended"] as? Boolean ?: false,
                                categoryId = value["categoryId"] as? String ?: ""
                            )
                            lists.add(item)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeViewModel", "Error parsing recommended item: ${e.message}")
                    }
                }
                _recomended.postValue(lists)
                android.util.Log.d("HomeViewModel", "Recommended items count: ${lists.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("HomeViewModel", "loadRecomended error: ${error.message}")
                _recomended.postValue(emptyList())
            }
        })
    }

    fun loadCategory() {
        val ref = firebaseDatabase.getReference("Category")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<Category>()
                for (childSnapshot in snapshot.children) {
                    if (!childSnapshot.exists()) continue

                    try {
                        val value = childSnapshot.getValue() as? Map<*, *>
                        if (value != null) {
                            val idValue = value["id"]
                            val idString = when (idValue) {
                                is Number -> idValue.toString()
                                is String -> idValue
                                else -> ""
                            }

                            val picUrlValue = value["picUrl"] as? String ?: ""

                            val category = Category(
                                title = value["title"] as? String ?: "",
                                id = idString,
                                picUrl = picUrlValue
                            )
                            lists.add(category)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeViewModel", "Error parsing category: ${e.message}")
                    }
                }
                _category.postValue(lists)
                android.util.Log.d("HomeViewModel", "Categories count: ${lists.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("HomeViewModel", "loadCategory error: ${error.message}")
                _category.postValue(emptyList())
            }
        })
    }

    fun uploadImage(uri: Uri, callback: (String?) -> Unit) {
        val fileName = "ProductImages/${UUID.randomUUID()}.jpg"
        val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { url ->
                    Log.d("CHECK_STEP", "UP ẢNH XONG! Link đây: $url")
                    callback(url.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("CHECK_STEP", "UP ẢNH THẤT BẠI: ${e.message}")
                callback(null)
            }
    }
    fun uploadItem(item: Items, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Items")

        database.child(item.numberInCart.toString()).setValue(item)
            .addOnSuccessListener {
                Log.d("FIREBASE_OK", "Lưu Database thành công!")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_ERR", "Lỗi Database (Check Rules Database!): ${e.message}")
                callback(false)
            }
    }

    fun deleteItem(itemTitle: String, onComplete: (Boolean) -> Unit) {
        val ref = firebaseDatabase.getReference("Items")
        ref.orderByChild("title").equalTo(itemTitle).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    child.ref.removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
                }
            }
            override fun onCancelled(error: DatabaseError) { onComplete(false) }
        })
    }
}