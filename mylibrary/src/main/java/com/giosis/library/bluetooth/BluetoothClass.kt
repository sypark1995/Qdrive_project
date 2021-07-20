package com.giosis.library.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.giosis.library.R
import com.giosis.library.list.CnRPickupInfoGetHelper
import com.giosis.library.list.PrintDataResult
import com.giosis.library.setting.bluetooth.BluetoothDeviceData
import com.giosis.library.setting.bluetooth.PrinterSettingActivity
import com.giosis.library.util.Preferences
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.datamatrix.encoder.SymbolShapeHint
import com.gprinter.command.EscCommand
import com.gprinter.command.LabelCommand
import java.util.*
import kotlin.collections.ArrayList

// 엑티비티 종료시 clearBluetoothAdapter() 해줘야함 .

class BluetoothClass(val mActivity: Activity) : BluetoothListener {
    val TAG = "BluetoothClass"

    init {

    }

    // BluetoothAdapter
    var mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // 연결된 Print List
    var printerConnManagerList = ArrayList<PrinterConnManager>()


    override fun isConnectPortablePrint(trackingNo: String) {
        // 연결된 print 없으면..
        if (BluetoothDeviceData.connectedPrinterAddress == null) {
            Toast.makeText(mActivity, mActivity.resources.getString(R.string.msg_first_connect_printer), Toast.LENGTH_SHORT).show()
            clearBluetoothAdapter()
            val intent = Intent(mActivity, PrinterSettingActivity::class.java)
            mActivity.startActivity(intent)
        }

        val deviceAddress: String = getBluetoothPrinterAddress()

        if (deviceAddress != "") {

            // 프린터 연결됨   출력시작
            Toast.makeText(mActivity, mActivity.resources.getString(R.string.msg_wait_while_print_job), Toast.LENGTH_SHORT).show()
            printLabel("", trackingNo, "isConnectPortablePrint")
        } else {
            checkBluetoothState(trackingNo)
        }
    }

    override fun clearBluetoothAdapter() {

        try {
            GPrinterData.TRACKING_NO = ""
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter!!.cancelDiscovery()
                mBluetoothAdapter = null
            }

            for (printerCon in printerConnManagerList) {
                printerCon.closePort()
            }

            printerConnManagerList.clear()

            mActivity.unregisterReceiver(printerBroadcastReceiver)

            printerHandler.removeCallbacksAndMessages(null)

        } catch (e: Exception) {

        }

    }

    private fun getBluetoothPrinterAddress(): String {
        var address = ""
        val printerConnManager: PrinterConnManager

        if (0 < printerConnManagerList.size) {
            printerConnManager = printerConnManagerList[0]
            address = printerConnManager.macAddress
            //    Log.e("print", "getBluetoothPrinterAddress  address : " + address);
        }

        return address
    }


    private fun checkBluetoothState(trackingNo: String) {
        Log.e("print", "$TAG  checkBluetoothState")

        // Bluetooth 지원 여부 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Bluetooth 지원하지 않음
        if (mBluetoothAdapter == null) {
            Toast.makeText(mActivity, mActivity.resources.getString(R.string.msg_bluetooth_not_supported), Toast.LENGTH_SHORT).show()

        } else {

            // Bluetooth 지원 && 비활성화 상태
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                mActivity.startActivityForResult(enableIntent, GPrinterData.REQUEST_ENABLE_BT)
            } else {
                // Bluetooth 지원 && 활성화 상태
                registerPrintReceiver(trackingNo)
            }
        }
    }


    private fun registerPrintReceiver(tracking_no: String) {
        printerConnManagerList.clear()

        // 전역 변수로 프린터 세팅 해 놓고 커넥션 되면 바로 프린터 되도록 하기
        GPrinterData.isGPrint = true
        GPrinterData.TRACKING_NO = tracking_no
        try {
            registerBluetoothReceiver()
            discoveryDevice()
        } catch (e: java.lang.Exception) {
            Log.e("Exception", "$TAG  registerPrintReceiver Exception : $e")
        }
    }


    private fun registerBluetoothReceiver() {
        // 인텐트 동록
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND) // 기기 검색됨
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) // 기기 검색 종료
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) // 연결 끊김 확인
        filter.addAction(GPrinterData.ACTION_CONN_STATE) // action_connect_state
        mActivity.registerReceiver(printerBroadcastReceiver, filter)
    }


    private fun discoveryDevice() {
        // 만약 이미 검색중이라면 cancelDiscovery()를 호출하여 검색을 멈춘 후 다시 검색해야 합니다.
        if (mBluetoothAdapter != null) {

            if (mBluetoothAdapter!!.isDiscovering) {
                mBluetoothAdapter!!.cancelDiscovery()
            }
            mBluetoothAdapter!!.startDiscovery()

        }
    }


    private val printerBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null) {

                val action = intent.action
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (action != null && action != BluetoothDevice.ACTION_FOUND) {
                    Log.e("print", "$TAG  onReceive action : $action")
                }

                when (action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        if (device != null && device.bondState == BluetoothDevice.BOND_BONDED) {
                            if (BluetoothDeviceData.connectedPrinterAddress == device.address) {
                                Log.e("print", TAG + "  FOUND Device name " + device.name + " / " + device.address)
                                // 프린트 버튼을 눌렀을 때, 디바이스를 찾아서 커넥션이 이루어진다음에  printerConnManagerList 에 소켓 섹션을 담아서 저장한 다음에 SDK 포트로  열린다면 그 커넥션을 저장해 놓고
                                // 포트가 열리지 않는다면 커넥션 제거
                                // 포트가 열린다면 디바이스 맥어드레스 저장해 놓기 -> 블루투스 세팅 화면에서 커넥션 열어 한 소스처럼 관리
                                printerConnManagerList.add(PrinterConnManager(mActivity, PrinterConnManager.CONN_METHOD.BLUETOOTH, device.address))
                                val size = printerConnManagerList.size
                                if (0 < size) {
                                    printerConnManagerList[size - 1].openPort()

                                    if (!printerConnManagerList[size - 1].connState) {  // 포트가  열리지 않았다면
                                        Log.e("print", "$TAG  connState  $size")
                                        printerConnManagerList.removeAt(size - 1)
                                    }
                                }
                            }
                        }
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.e("print", TAG + "  ACTION_DISCOVERY_FINISHED  " + printerConnManagerList.size)
                        if (0 < printerConnManagerList.size) {
                            val connManager = printerConnManagerList[0]
                            val printAddr = connManager.macAddress

                            //여기서 만약 첫번째 버튼을 누른 상태로 프린터가 되야 한다면    // ??
                            if (GPrinterData.isGPrint && GPrinterData.TRACKING_NO != "") {
                                val message: Message = printerHandler.obtainMessage(GPrinterData.DOUBLE_PRINTER)
                                val bundle = Bundle()
                                bundle.putString("tracking_no", GPrinterData.TRACKING_NO)
                                bundle.putString("address", printAddr)
                                message.data = bundle

                                //먼저 초기화
                                GPrinterData.TRACKING_NO = ""
                                GPrinterData.isGPrint = false
                                printerHandler.sendMessage(message)
                            }
                        } else {
                            printerHandler.obtainMessage(GPrinterData.NONE_PRINTER).sendToTarget()
                        }
                    }

                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        if (device != null) {
                            Log.e("print", TAG + "  ACTION_ACL_DISCONNECTED  " + device.name + " / " + printerConnManagerList.size)

                            if (0 < printerConnManagerList.size) {
                                val size: Int = printerConnManagerList.size
                                if (device.address == printerConnManagerList[size - 1].macAddress) {
                                    printerConnManagerList.removeAt(size - 1)
                                }
                                Toast.makeText(context, "device disconnected", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    GPrinterData.ACTION_CONN_STATE -> {
                        val state = intent.getIntExtra("state", -1)
                        Log.e("print", "$TAG  action_connect_state  state : $state")

                        when (state) {
                            GPrinterData.CONN_STATE_DISCONNECT ->
                                Toast.makeText(context, "CONN_STATE_DISCONNECT", Toast.LENGTH_SHORT).show()

                            GPrinterData.CONN_PRINT_DONE ->
                                Toast.makeText(context, "Print Done!", Toast.LENGTH_SHORT).show()

                            else -> {
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }


    private val printerHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            // 제일처음은 983 / 그 다음부터는 333 (currentPrinterCommand 셋팅될때까지...)
            Log.e("print", TAG + "  handleMessage : " + msg.what)
            when (msg.what) {
                GPrinterData.PRINTER_COMMAND_ERROR -> {
                    if (GPrinterData.TEMP_TRACKING_NO != "") {
                        onStartGprinter(GPrinterData.TEMP_TRACKING_NO, "")
                    } else {
                        Toast.makeText(mActivity, "Please select the correct printer instructions", Toast.LENGTH_SHORT).show()
                    }
                }

                GPrinterData.DOUBLE_PRINTER -> {
                    val bundle = msg.data
                    val trackingNo = bundle.getString("tracking_no")
                    val macAddr = bundle.getString("address")

                    // 어떻게 customExpandable 버튼을 누른 것처럼 할 수 있을까?
                    if (trackingNo != null && macAddr != null) {
                        if (0 < printerConnManagerList.size) {
                            onStartGprinter(trackingNo, macAddr)
                        }
                    }
                }

                GPrinterData.NONE_PRINTER -> {
                    Toast.makeText(mActivity, "Please print it again.", Toast.LENGTH_SHORT).show()
                    clearBluetoothAdapter()
                }

                else -> {
                }
            }
        }
    }


    fun onStartGprinter(trackingNo: String, mac_addr: String) {
        //   Log.e("print", TAG + "  onStartGprinter > " + mac_addr);

        GPrinterData.TEMP_TRACKING_NO = trackingNo

        if (mac_addr == "") {
            isConnectPortablePrint(trackingNo)
        } else {
            printLabel(mac_addr, trackingNo, "onStartGprinter")
        }
    }


    private fun printLabel(address: String, tracking_no: String, where: String) {
        if (printerConnManagerList.size == 0 || !printerConnManagerList[0].connState) {
            return
        }

        // 위에 if 문은 아마 그냥 통과 될 것 왜냐면 커넥션을 자동으로 하고 바로 프린터 버튼 누른 것처럼 trigger 보완 소스 넣고 있음
        //  handler 에서 메시지 받으면 다시 버튼 클릭을 interface 함수로 getTodayPickupDone호출 하고 있음 - onStartGprinter
        if (printerConnManagerList[0].currentPrinterCommand == PrinterConnManager.PrinterCommand.TSC) {

            Log.e("print", TAG + "  printLabel Command : " + printerConnManagerList[0].currentPrinterCommand + " / " + address + " / " + tracking_no)

            CnRPickupInfoGetHelper.Builder(mActivity, Preferences.userId, tracking_no)
                    .setOnCnRPrintDataEventListener { stdResult: PrintDataResult? ->
                        try {
                            if (stdResult != null) {
                                if (stdResult.resultCode == 0) {

                                    // NOTIFICATION.  Send Label DATA
                                    Log.e("print", "$TAG  sendLabel")
                                    sendLabel(stdResult)
                                } else {
                                    Toast.makeText(mActivity, stdResult.resultMsg, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(mActivity, "GetCnRPrintData Error..", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: java.lang.Exception) {
                            Toast.makeText(mActivity, "GetCnRPrintData Exception : $e", Toast.LENGTH_SHORT).show()
                            Log.e("Exception", "$TAG  GetCnRPrintData Exception : $e")
                        }
                    }.build().execute()

            // TODO_ 프린트로 테스트 해보기
//            RetrofitClient.instanceDynamic().requestGetCnRPrintData(tracking_no)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({
//
//                        if (it.resultCode == 0) {
//
//                            val resultObj = Gson().fromJson(it.resultObject, ResultObject::class.java)
//                            Log.e(TAG, "result " + resultObj.invoiceNo + " / " + resultObj.deliveryCouse)
//                            // TEST_ 확인 후 sendLabel 호출하기 !!
//                        } else {
//                            Toast.makeText(mActivity, it.resultMsg, Toast.LENGTH_SHORT).show()
//                        }
//                    }, {
//                        Toast.makeText(mActivity, "Exception : " + it.message, Toast.LENGTH_SHORT).show()
//                    })
        } else {

            //   Log.e("print", TAG + "  printLabel : " + where + " / " + tracking_no + "  PRINTER_COMMAND_ERROR");
            printerHandler.obtainMessage(GPrinterData.PRINTER_COMMAND_ERROR).sendToTarget()
        }
    }


    private fun sendLabel(stdResult: PrintDataResult) {
        // tsc.addUserCommand("BARCODE 10, 0, \"39\", 80, 0, 0, 2, 5, \"C828996SGSG\"");  - Custom
        val result = stdResult.resultObject
        val tsc = LabelCommand()
        tsc.addSize(80, 52) // label 크기 설정 -- mm
        tsc.addGap(0)
        // 인쇄방향 설정
//        tsc.addDirection(LabelCommand.DIRECTION.BACKWARD, LabelCommand.MIRROR.MIRROR);
//        tsc.addDirection(LabelCommand.DIRECTION.BACKWARD, LabelCommand.MIRROR.NORMAL);  마지막 버전
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        //연속인쇄용?
        tsc.addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
        //원점좌표설정(인쇄방향하고 같이)
        tsc.addReference(0, 0)
        //        tsc.addTear(EscCommand.ENABLE.ON);
        tsc.addTear(EscCommand.ENABLE.OFF)
        //        tsc.addPeel(EscCommand.ENABLE.ON); //방법 설명 : 프린터 스트립 모드 설정
        // 인쇄 버퍼 데이터 지우기
        tsc.addCls()

        //첫번째 row
        if ("SG" == Preferences.userNation) {
            tsc.add1DBarcode(20, 0, LabelCommand.BARCODETYPE.CODE128, 80, LabelCommand.READABEL.EANBEL,
                    LabelCommand.ROTATION.ROTATION_0, result.invoiceNo)
        } else {
            tsc.add1DBarcode(20, 0, LabelCommand.BARCODETYPE.CODE39S, 80, LabelCommand.READABEL.EANBEL,
                    LabelCommand.ROTATION.ROTATION_0, 2, 5, result.invoiceNo)
        }

        //   tsc.addQRCode(450, 0, LabelCommand.EEC.LEVEL_L, 5, LabelCommand.ROTATION.ROTATION_0, result.getInvoiceNo());
        val bitmap: Bitmap? = stringToDataMatrix(result.invoiceNo)
        tsc.addBitmap(450, 0, 100, bitmap)

        // 두번째 row
        var list = cutString(result.custName, 1)
        val consignee = list[0]
        tsc.addText(15, 130, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "To(consignee)")
        tsc.addReverse(10, 115, 186, 50)
        tsc.addBox(195, 115, 565, 165, 1)
        tsc.addText(215, 130, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, consignee)

        // 세번째 row
        tsc.addBox(10, 165, 195, 265, 1)
        tsc.addText(35, 170, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "Postal code")
        tsc.addText(55, 200, LabelCommand.FONTTYPE.FONT_3, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, result.zipCode)
        tsc.addText(25, 235, LabelCommand.FONTTYPE.FONT_3, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, result.deliveryCouse)
        tsc.addErase(194, 165, 1, 100)
        val address = result.backaddress + " " + result.frontAddress
        //address = "#06-189SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5SUNSHINE GARDENS 489A CHOA CHU KANG AVENUE 5";
        list = cutString(address, 3)
        tsc.addBox(195, 165, 565, 265, 1)
        for (i in list.indices) {
            val positionY = 175 + 30 * i
            tsc.addText(215, positionY, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                    LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, list[i])
        }

        // 네번째 row
        tsc.addBox(10, 265, 565, 305, 1)
        tsc.addText(35, 275, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "(" + result.hpNo + "/" + result.telNo + ")")

        // 마지막 row
        list = cutString(result.sellerShop, 1)
        val seller_shop_nm = list[0]
        tsc.addText(15, 320, LabelCommand.FONTTYPE.FONT_2, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "From(shipper)")
        tsc.addReverse(10, 305, 186, 50)
        tsc.addBox(195, 305, 565, 355, 1)
        tsc.addText(215, 320, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0,
                LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, seller_shop_nm)

        // 라벨인쇄
        tsc.addPrint(1, 1)
        tsc.addSound(1, 100)
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255)
        val datas = tsc.command

        // 여기서 Gprinter 함수 콜
        printerConnManagerList[0].sendDataImmediately(datas)
    }


    private fun cutString(originStr: String?, lineNum: Int): ArrayList<String> {
        var originStr = originStr
        val arrayList = ArrayList<String>()
        if (originStr == null) {
            originStr = ""
        }

        val oriStr = originStr.trim { it <= ' ' }

        var str1 = ""
        var str2 = ""
        var str3 = ""
        var temp = ""

        if (lineNum == 1) {
            if (oriStr.length > 27) {
                str1 = oriStr.substring(0, 26)
                str1 += "..."
            } else {
                str1 = oriStr
            }

        } else if (lineNum == 3) {
            if (oriStr.length > 27) {
                str1 = oriStr.substring(0, 26)
                str2 = oriStr.substring(26)
                if (str2.length > 27) {
                    temp = oriStr
                    str2 = temp.substring(0, 26)
                    str3 = temp.substring(26)
                    if (str3.length > 27) {
                        str3 = str3.substring(0, 26)
                        str3 += "..."
                    }
                }
            } else {
                str1 = oriStr
            }
        }

        arrayList.add(str1)
        if (str2 != "") {
            arrayList.add(str2)
        }

        if (str3 != "") {
            arrayList.add(str3)
        }

        Log.e(TAG, " cutString $arrayList")
        return arrayList
    }

    private fun stringToDataMatrix(scan_no: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val gen = MultiFormatWriter()
        try {
            val WIDTH = 200
            val HEIGHT = 200
            val hints = Hashtable<EncodeHintType, Any?>(1)
            hints[EncodeHintType.DATA_MATRIX_SHAPE] = SymbolShapeHint.FORCE_SQUARE
            val bytemap = gen.encode(scan_no, BarcodeFormat.DATA_MATRIX, WIDTH, HEIGHT, hints)
            bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
            for (i in 0 until WIDTH) {
                for (j in 0 until HEIGHT) {
                    bitmap.setPixel(i, j, if (bytemap[i, j]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e("print", "stringToDataMatrix  MultiFormatWriter Exception  : $e")
        }
        return bitmap
    }
}