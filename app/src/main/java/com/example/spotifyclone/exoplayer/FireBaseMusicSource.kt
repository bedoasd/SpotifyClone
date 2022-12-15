package com.example.spotifyclone.exoplayer

import android.media.MediaDescription
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.spotifyclone.entities.Songs
import com.example.spotifyclone.exoplayer.State.*
import com.example.spotifyclone.remote.MusicDatabase
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FireBaseMusicSource @Inject constructor(
    private val  musicDatabase :MusicDatabase
) {

    //fetching our songs from fireStore and preparing them in a format that we want have in the our service

    var songs = emptyList<MediaMetadataCompat>() //caontains media meta data combat objects ..meta information about specific song

    //fetching our songs from fireStroe and preparing them in a format that we want have in the our service
    //IO for network operations or database operation
    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state=STATE_INITIALIZING
        val allSongs=musicDatabase.getAllSongs()
        //parsing from Songs object to MediaMetaDataCompact object using MAP
        songs=allSongs.map { songs ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST ,songs.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, songs.mediaId)
                .putString(METADATA_KEY_TITLE, songs.title)
                .putString(METADATA_KEY_DISPLAY_TITLE,songs.title)//title that display in notification
                .putString(METADATA_KEY_DISPLAY_ICON_URI,songs.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI,songs.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, songs.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE,songs.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION,songs.subtitle)
                .build()
        }

        state=STATE_INITIALIZED

    }

    //pass media to exoplayer and made EXO play song after song
    fun asMediaSource(datasourceFactory:DefaultDataSourceFactory) : ConcatenatingMediaSource{
        val concatenatingMediaSource=ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(datasourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    //media browser to open list of items

    fun asMediaItems()=songs.map { song ->
        val desc=MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)
    }


    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    //start to0 initialize our source
    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    //this block will only be accessed from the same thread
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }

        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false

        } else {
            action(state == STATE_INITIALIZED)
            return false
        }
    }


}

enum class State {
    STATE_CREATED,      /* INITIALIZE*/
    STATE_INITIALIZING,  /*BEFORE INITIALIZING*/
    STATE_INITIALIZED,   /*AFTER INITIALIZING*/
    STATE_ERROR
}