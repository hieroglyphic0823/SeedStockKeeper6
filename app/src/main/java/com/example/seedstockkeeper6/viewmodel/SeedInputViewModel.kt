package com.example.seedstockkeeper6.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.model.SeedPacket
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SeedInputViewModel : ViewModel() {
    var packet: SeedPacket = SeedPacket() // UIがバインドする用

    fun setSeed(seed: SeedPacket?) {
        packet = seed ?: SeedPacket()
    }

    fun saveSeed(onComplete: () -> Unit) {
        viewModelScope.launch {
            val db = Firebase.firestore
            val ref = db.collection("seeds")
            val id = packet.id ?: ref.document().id
            ref.document(id).set(packet.copy(id = id)).await()
            onComplete()
        }
    }
}
