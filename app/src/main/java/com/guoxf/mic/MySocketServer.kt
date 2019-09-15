package com.guoxf.mic

import android.util.Log
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class MySocketServer(private var mErrorListener: OnErrorListener) {

    companion object {
        private const val TAG = "MySocketServer"

        private const val PORT = 12321
    }

    private var mServerSocket: ServerSocket? = null

    private var mMyAudioRecorder: MyAudioRecorder? = null

    private var mClient: Socket? = null

    private var mFlag = false


    fun setup() {
        mFlag = true
        Thread(Runnable {
            try {
                mServerSocket = ServerSocket(PORT)
                while (mFlag) {
                    mFlag = false
                    try {
                        mClient = mServerSocket?.accept()
                    } catch (ex: Exception) {
                        Log.d(TAG, "", ex.fillInStackTrace())
//                        return@Runnable
                    }

                    work(mClient)
                }
            } catch (e: Exception) {
                Log.d(TAG, "", e.fillInStackTrace())
                return@Runnable
            }

        }).start()
    }

    fun setup(ip : String) {
        mClient = Socket()
        mClient?.connect(InetSocketAddress(ip, PORT), 3000)
        work(mClient)
    }

    private fun work(client : Socket?) {

        Log.d(TAG, "work $client")

        Thread({
            mMyAudioRecorder = MyAudioRecorder()
            mMyAudioRecorder?.init()

            //try {
                val result = mMyAudioRecorder?.startRecord(client?.getOutputStream())
                if (!result!!) {
                    mErrorListener.onError()
                    mMyAudioRecorder?.stopRecord()
                    mMyAudioRecorder?.release()
                }
            /*} catch (e1: Exception) {
                Log.d(TAG, "", e1.fillInStackTrace())
                return@Thread
            }*/

        }).start()
    }

    fun release() {
        mFlag = false
        mClient?.close()
        mServerSocket?.close()
    }

    interface OnErrorListener {
        fun onError()
    }

}