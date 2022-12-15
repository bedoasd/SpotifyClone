package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.graphics.ColorSpace.Connector
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.exoplayer.callback.MusicPlayerNotificationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

private const val SERVICE_TAG ="MusicService"

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {



    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: ExoPlayer
     var isForgroundService =false

    private lateinit var musicNotificationmanager:MusicNotoificatioManager

    private val serviceJob = Job()
    private val serviceScope= CoroutineScope(Dispatchers.Main+serviceJob) // on cancellation don't make memory leak

    private lateinit var mediaSession :MediaSessionCompat  //hold media data
    private lateinit var mediaSessionConnector: MediaSessionConnector


    override fun onCreate() {
        super.onCreate()
        //on click on service Notification navigate me to Activity
        val activityIntent=packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)
        }

        mediaSession=MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive=true

        }

        sessionToken=mediaSession.sessionToken

        musicNotificationmanager= MusicNotoificatioManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {

        }

        mediaSessionConnector=MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()  // to make sure that all coroutines are cancelled when service dies
    }





    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {


    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {


    }
}