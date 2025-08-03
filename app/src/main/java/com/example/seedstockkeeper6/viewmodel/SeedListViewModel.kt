package com.example.seedstockkeeper6.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SeedListViewModel : ViewModel() {
    init {
        Log.d("BootTrace", "SeedListViewModel init")
    }

    fun deleteSeedPacketWithImages(documentId: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = deleteSeedPacketWithImagesInternal(documentId)
            onComplete(result)
        }
    }

    // ★ public に変更
    suspend fun deleteSeedPacketWithImagesInternal(documentId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val db = Firebase.firestore
            val storage = Firebase.storage

            try {
                val docRef = db.collection("seeds").document(documentId)
                val documentSnapshot = docRef.get().await()

                if (!documentSnapshot.exists()) {
                    Log.w("SeedListVM", "Document $documentId does not exist.")
                    return@withContext Result.failure(NoSuchElementException("Document $documentId not found"))
                }

                val imageUrls = documentSnapshot.get("imageUrls") as? List<String> ?: emptyList()

                imageUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        try {
                            val path = Uri.decode(url).substringAfter("/o/").substringBefore("?")
                            if (path.isNotEmpty()) {
                                storage.reference.child(path).delete().await()
                                Log.d("SeedListVM", "Deleted image: $path")
                            }
                        } catch (e: Exception) {
                            Log.e("SeedListVM", "Failed to delete image: $url", e)
                        }
                    }
                }

                docRef.delete().await()
                Log.d("SeedListVM", "Deleted document: $documentId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("SeedListVM", "Error deleting packet $documentId", e)
                Result.failure(e)
            }
        }
}
