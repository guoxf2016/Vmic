package com.guoxf.mic

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.OutputStream

class MyAudioRecorder {

    companion object {
        private const val TAG = "MyAudioRecorder"

        private const val mAudioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION

        private const val mSampleRate = 48000

        private const val mChannelConfig = AudioFormat.CHANNEL_IN_STEREO

        private const val mAudioFormat = AudioFormat.ENCODING_PCM_16BIT

        private val mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat)

    }

    private lateinit var mAudioRecord: AudioRecord


    fun init() {
        Log.d(TAG, "init")
    }

    fun startRecord(outputStream: OutputStream?) : Boolean {
        Log.d(TAG, "startRecord")

        if (mAudioRecord.state == AudioRecord.STATE_INITIALIZED) {
            mAudioRecord.startRecording()
        }

        Thread.currentThread().priority = Thread.MAX_PRIORITY
        /*while (true) {
            if (AudioRecord.RECORDSTATE_RECORDING == mAudioRecord.state) {
                Log.d(TAG, "state " + mAudioRecord.state)
                break
            }
        }*/

        val buffer = ByteArray(mBufferSize)

        try {
            while (true /*|| AudioRecord.RECORDSTATE_RECORDING == mAudioRecord.state*/) {
                val readSize = mAudioRecord.read(buffer, 0, mBufferSize)
//                Log.d(TAG, "readSize $readSize")
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                    outputStream?.write(buffer, 0, readSize)
                    outputStream?.flush()
                } else {
                    return false
                }
            }
        } catch (e : Exception) {
            return false
        }

//        return true
    }

    fun stopRecord() {
        Log.d(TAG, "stopRecord")

        if (mAudioRecord.state == AudioRecord.STATE_INITIALIZED) {
            mAudioRecord.stop()
        }
    }

    fun release() {
        Log.d(TAG, "release")

        mAudioRecord.release()
    }

    init {
        mAudioRecord = AudioRecord(mAudioSource, mSampleRate, mChannelConfig, mAudioFormat, mBufferSize)
    }

}