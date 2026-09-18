package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

class AudioPlayerHelper(private val context: Context) {
    private val TAG = "AudioPlayerHelper"
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    var currentPlayingId: Long? = null
        private set
    var isPlaying: Boolean = false
        private set

    fun playAudio(
        messageId: Long,
        filePath: String,
        onProgress: (progress: Float, currentMs: Long) -> Unit,
        onComplete: () -> Unit
    ) {
        if (currentPlayingId == messageId && isPlaying) {
            pause()
            return
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                val file = File(filePath)
                if (file.exists()) {
                    setDataSource(file.absolutePath)
                } else {
                    setDataSource(context, Uri.parse(filePath))
                }
                prepare()
                start()
            }

            currentPlayingId = messageId
            isPlaying = true
            mediaPlayer = player

            player.setOnCompletionListener {
                stop()
                onComplete()
            }

            progressRunnable = object : Runnable {
                override fun run() {
                    mediaPlayer?.let { p ->
                        if (p.isPlaying) {
                            val cur = p.currentPosition
                            val dur = p.duration.coerceAtLeast(1)
                            onProgress(cur.toFloat() / dur.toFloat(), cur.toLong())
                            handler.postDelayed(this, 100)
                        }
                    }
                }
            }
            handler.post(progressRunnable!!)

        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio file $filePath", e)
            stop()
            onComplete()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            isPlaying = false
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio", e)
        }
        stopProgressUpdates()
    }

    fun stop() {
        stopProgressUpdates()
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio", e)
        } finally {
            mediaPlayer = null
            currentPlayingId = null
            isPlaying = false
        }
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        progressRunnable = null
    }

    fun release() {
        stop()
    }
}
