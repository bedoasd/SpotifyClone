package com.example.spotifyclone.exoplayer
import com.example.spotifyclone.exoplayer.State.*

class FireBaseMusicSource {

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit> ()

    //start to0 initialize our source
    private var state:State = STATE_CREATED
    set(value){
        if (value == STATE_INITIALIZED || value ==STATE_ERROR ){
            synchronized(onReadyListeners){
                //this block will only be accessed from the same thread
                field=value
                onReadyListeners.forEach{listener->
                    listener(state == STATE_INITIALIZED)
                }
            }
        }else{
            field=value
        }

    }

    fun whenReady(action:(Boolean) ->Unit ):Boolean{
        if(state==STATE_CREATED||state==STATE_INITIALIZING){
            onReadyListeners +=action
            return false

        }  else{
            action( state ==STATE_INITIALIZED)
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