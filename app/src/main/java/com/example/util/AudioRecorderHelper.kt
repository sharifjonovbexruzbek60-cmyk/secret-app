package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorderHelper(private val context: Context) {
    private val TAG = "AudioRecorderHelper"
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMs: Long = 0L

    var isRecording: Boolean = false
        private set

    /**
     * Prepares and starts low-bitrate audio recording (AMR-NB, ~12.2 kbps, 8kHz mono).
     * This provides voice clarity while keeping files ultra-small (<15KB for 10s).
     */
    fun startRecording(): File? {
        val audioDir = File(context.filesDir, "chat_audio").apply { mkdirs() }
        val outputFile = File(audioDir, "voice_${System.currentTimeMillis()}.3gp")
        currentOutputFile = outputFile

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioChannels(1) // Mono for speech
                setAudioSamplingRate(8000) // 8 kHz standard telephone speech
                setAudioEncodingBitRate(12200) // 12.2 kbps low bitrate
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            startTimeMs = System.currentTimeMillis()
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recording", e)
            cleanup()
            null
        }
    }

    fun getElapsedTimeMs(): Long {
        return if (isRecording) System.currentTimeMillis() - startTimeMs else 0L
    }

    fun getMaxAmplitude(): Int {
        return try {
            if (isRecording) mediaRecorder?.maxAmplitude ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    fun stopRecording(): Pair<File?, Long> {
        val duration = getElapsedTimeMs()
        val file = currentOutputFile
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        } finally {
            cleanup()
        }
        return Pair(file, duration)
    }

    fun cancelRecording() {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
            currentOutputFile?.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling recording", e)
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        mediaRecorder = null
        isRecording = false
        startTimeMs = 0L
    }
}
