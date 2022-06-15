package com.giosis.util.qdrive.singapore

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.text.TextUtils
import device.common.DecodeResult
import device.common.DecodeStateCallback
import device.common.ScanConst
import device.sdk.ScanManager

class Pm85ScanManager {

    var scanManager: ScanManager? = null
    var decodeResult: DecodeResult = DecodeResult()
    var scanResultReceiver: ScanResultReceiver = ScanResultReceiver()
    var handler = Handler()
    var isConnected = false

    var arrScanListener = ArrayList<ScanResultListener>()

    var stateCallback = object : DecodeStateCallback(handler) {
        override fun onChangedState(state: Int) {
            when (state) {
                ScanConst.STATE_ON, ScanConst.STATE_TURNING_ON -> {
                    isConnected = true
                }
                ScanConst.STATE_OFF, ScanConst.STATE_TURNING_OFF -> {
                    isConnected = false
                }
            }
        }
    }

    fun registScan(context: Context) {
        val filter = IntentFilter()
        filter.addAction(ScanConst.INTENT_USERMSG)
        context.registerReceiver(scanResultReceiver, filter)
    }

    fun unregistScan(context: Context) {
        context.unregisterReceiver(scanResultReceiver)
    }

    fun startScan() {

        scanManager = ScanManager()
        scanManager!!.aRegisterDecodeStateCallback(stateCallback)
        scanManager!!.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG)
    }

    fun stopScan() {
        if (isConnected) {
            scanManager!!.aUnregisterDecodeStateCallback(stateCallback)
            isConnected = false
            scanManager = null
        }
    }

    class ScanResultReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (TextUtils.equals(
                        ScanConst.INTENT_USERMSG,
                        intent?.action
                    ) && getInstance().scanManager != null
                ) {
                    getInstance().scanManager!!.aDecodeGetResult(getInstance().decodeResult.recycle())
                    if (getInstance().getLastScanListener() != null) {
                        getInstance().getLastScanListener()!!
                            .scanReceive(getInstance().decodeResult.toString())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    interface ScanResultListener {
        fun scanReceive(result: String)
    }

    fun addScanListener(scanListener: ScanResultListener) {
        arrScanListener.add(scanListener)
    }

    fun removeScanListener(scanListener: ScanResultListener) {
        arrScanListener.remove(scanListener)
    }

    fun getLastScanListener(): ScanResultListener? {
        return if (arrScanListener.size > 0) {
            arrScanListener[arrScanListener.size - 1]
        } else {
            null
        }
    }

    companion object {
        @Volatile
        private var instance: Pm85ScanManager? = null
        val PM85 = "Pm85"

        @JvmStatic
        fun getInstance(): Pm85ScanManager = instance ?: synchronized(this) {
            instance ?: Pm85ScanManager().also { instance = it }
        }
    }
}