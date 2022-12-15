package com.example.spotifyclone.remote

import com.example.spotifyclone.entities.Songs
import com.example.spotifyclone.others.Constatns
import com.example.spotifyclone.others.Constatns.SONGS_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


//to get all song  as a list from firebase

class MusicDatabase {


    private val firestore=FirebaseFirestore.getInstance()
    private val songsCollection=firestore.collection(SONGS_COLLECTION)

    suspend fun getAllSongs():List<Songs>{
        return try {
            songsCollection.get().await().toObjects(Songs::class.java)
        }
        catch (e:Exception){
            emptyList()
        }
    }

}