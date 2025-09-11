package com.stream.domain.manager

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlayerManagerImpl @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val cache: SimpleCache
) : PlayerManager {

    private var exoPlayer: ExoPlayer? = null

    override val player: Player?
        get() = exoPlayer

    @OptIn(UnstableApi::class)
    override fun playVideo(uri: Uri, isLocal: Boolean) {
        println("DEBUG: PlayerManager.playVideo - URI: $uri, isLocal: $isLocal")
        println("DEBUG: URI scheme: ${uri.scheme}, path: ${uri.path}")
        release() // Release any existing player

        exoPlayer = ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()
            .apply {
                // Add listener to debug player state
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val stateName = when (playbackState) {
                            Player.STATE_IDLE -> "IDLE"
                            Player.STATE_BUFFERING -> "BUFFERING"
                            Player.STATE_READY -> "READY"
                            Player.STATE_ENDED -> "ENDED"
                            else -> "UNKNOWN"
                        }
                        println("DEBUG: Player state changed to: $stateName ($playbackState)")
                    }
                    
                    override fun onPlayerError(error: PlaybackException) {
                        println("DEBUG: Player error: ${error.message}")
                        println("DEBUG: Error code: ${error.errorCode}")
                    }
                    
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        println("DEBUG: Player isPlaying changed to: $isPlaying")
                    }
                })
                
                // Simplified media source setup - no caching for now
                val dataSourceFactory = DefaultDataSource.Factory(context)
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))

                println("DEBUG: Setting media source and preparing player")
                setMediaSource(mediaSource)
                prepare()
                
                // Auto-play the video
                play()
                println("DEBUG: Player prepared and started playing")
            }
    }

    override fun play() {
        println("DEBUG: PlayerManager.play() called")
        exoPlayer?.play()
        println("DEBUG: Player isPlaying: ${exoPlayer?.isPlaying}")
    }

    override fun pause() {
        println("DEBUG: PlayerManager.pause() called")
        exoPlayer?.pause()
    }

    override fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
    }

    override fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
