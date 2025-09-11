package com.stream.domain.manager

import android.net.Uri
import androidx.media3.common.Player

interface PlayerManager {
    val player: Player?
    fun playVideo(uri: Uri, isLocal: Boolean = false)
    fun play()
    fun pause()
    fun seekTo(position: Long)
    fun setPlaybackSpeed(speed: Float)
    fun release()
}