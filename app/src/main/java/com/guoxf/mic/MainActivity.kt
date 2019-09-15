package com.guoxf.mic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import java.net.InetAddress


class MainActivity : AppCompatActivity(), View.OnClickListener, MySocketServer.OnErrorListener {

    companion object {
        private const val TAG = "MainActivity"

        private const val REQUEST_CODE_PERMISSION = 0x01
    }

    private lateinit var mMySocketServer: MySocketServer

    private lateinit var mMicButton : ImageButton

    private lateinit var mIpAddressEt: EditText

    private var mStart = false

    private var mLastClickTime = -1L

    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        mHandler = Handler()
        setContentView(R.layout.activity_main)
        mMicButton = findViewById(R.id.mic_button)
        mIpAddressEt = findViewById(R.id.pc_ip_address_et)
        mMicButton.setBackgroundResource(R.drawable.ic_mic_off_black_24dp)
        mMicButton.setOnClickListener(this)
        mMicButton.isEnabled = false

        mMySocketServer = MySocketServer(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume start : $mStart")

        if (!checkAndRequestPermissions()) {
            return
        }

        mMicButton.isEnabled = true

        //window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        /*Handler().postDelayed({
            onBackPressed()
        }, 2 * 60 * 1000)*/

        if (mStart) {
            mMicButton.setBackgroundResource(R.drawable.ic_mic_black_24dp)
        } else {
            mMicButton.setBackgroundResource(R.drawable.ic_mic_off_black_24dp)
        }

        startService(Intent(this, NotificationService::class.java))


    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        //window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }



    private fun setWindowBrightness(brightness : Int) {
        val lp = window.attributes
        lp.screenBrightness = brightness / 255.0f
        window.attributes = lp
    }


    private fun micOn() {
        Log.d(TAG, "micOn")
        mHandler.removeCallbacksAndMessages(null)
        /*runOnUiThread {
            mHandler.removeCallbacksAndMessages(null)
            setWindowBrightness(5)
        }*/
//        val ip = mIpAddressEt.text.toString()
        val ip = "192.168.1.5"
        Thread {
            try {
                InetAddress.getByName(ip).isReachable(200)

            } catch (e : Exception) {
                Log.d(TAG, "", e.fillInStackTrace())
                return@Thread
            }

            mStart = true
            mMySocketServer.setup(ip)

        }.start()

    }

    private fun micOff() {
        Log.d(TAG, "micOff")
        mStart = false
        mMySocketServer.release()

        /*runOnUiThread {
            setWindowBrightness(-255)
        }*/

    }

    override fun onBackPressed() {
        super.onBackPressed()
        //moveTaskToBack(false)
    }

    override fun onError() {
        Log.d(TAG, "onError")
        runOnUiThread {
            mMicButton.setBackgroundResource(R.drawable.ic_mic_off_black_24dp)
        }
        micOff()
    }

    override fun onClick(v: View?) {

        mLastClickTime = if (mLastClickTime == -1L) {
            System.currentTimeMillis()
        } else {
            val now  = System.currentTimeMillis()
            if (now - mLastClickTime < 1000L) {
                return
            } else {
                now
            }
        }

        when (v?.id) {
            R.id.mic_button -> {
                if (mStart) {
                    mMicButton.setBackgroundResource(R.drawable.ic_mic_off_black_24dp)
                    micOff()
                } else {
                    mMicButton.setBackgroundResource(R.drawable.ic_mic_black_24dp)
                    micOn()
                }
            }
        }
    }

    private fun checkAndRequestPermissions() : Boolean {
        val check = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (PackageManager.PERMISSION_GRANTED == check) {
            return true
        }

        ActivityCompat.requestPermissions(this, Array(1,  {
            Manifest.permission.RECORD_AUDIO
        }), REQUEST_CODE_PERMISSION)

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted")
                } else {
                    Log.d(TAG, "permission denied")
                    finish()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
