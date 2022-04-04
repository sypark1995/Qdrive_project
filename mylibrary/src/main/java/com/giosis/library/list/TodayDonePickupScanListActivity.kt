package com.giosis.library.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.giosis.library.R
import com.giosis.library.barcodescanner.CaptureActivity1
import com.giosis.library.databinding.ActivityPickupScanListBinding
import com.giosis.library.server.RetrofitClient
import com.giosis.library.data.PickupPackingListResult
import com.giosis.library.util.BarcodeType
import com.giosis.library.util.CommonActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

class TodayDonePickupScanListActivity : CommonActivity() {
    var TAG = "TodayDonePickupScanListActivity"

    val binding by lazy {
        ActivityPickupScanListBinding.inflate(layoutInflater)
    }

    private var todayDonePickupScanListAdapter: TodayDonePickupScanListAdapter? = null
    private var buttonType: String = ""

    var pickupNo: String = ""
    var applicant: String = ""
    var scannedQty: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pickupNo = intent.getStringExtra("pickup_no").toString()
        applicant = intent.getStringExtra("applicant").toString()
        buttonType = intent.getStringExtra("button_type").toString()


        binding.layoutTopTitle.textTopTitle.text =
            resources.getString(R.string.text_today_done_scan_list)
        binding.textPickupNo.text = pickupNo

        if (buttonType == "Add Scan") {
            binding.btnAdd.setText(R.string.button_add_scan_list)
        } else if (buttonType == "Take Back") {
            binding.btnAdd.setText(R.string.button_take_back)
        }

        binding.layoutTopTitle.layoutTopBack.setOnClickListener {
            val intent = intent
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        binding.btnAdd.setOnClickListener {
            if (buttonType == "Add Scan") {
                clickedAddScan()
            } else if (buttonType == "Take Back") {
                clickedTakeBack()
            }
        }

        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.instanceDynamic().requestGetScanPackingList(pickupNo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.resultCode == 0) {

                    val list = Gson().fromJson<ArrayList<PickupPackingListResult>>(
                        it.resultObject,
                        object : TypeToken<ArrayList<PickupPackingListResult>>() {}.type
                    )
                    setScannedList(list)
                }

                binding.progressBar.visibility = View.GONE
            }, {

                binding.progressBar.visibility = View.GONE
            })
    }

    private fun setScannedList(result: ArrayList<PickupPackingListResult>) {

        scannedQty = result.size.toString()
        binding.textScannedQty.text = scannedQty

        todayDonePickupScanListAdapter =
            TodayDonePickupScanListAdapter(this@TodayDonePickupScanListActivity, result)
        binding.listScannedList.adapter = todayDonePickupScanListAdapter
    }

    private fun clickedAddScan() {

        val intent = Intent(this@TodayDonePickupScanListActivity, CaptureActivity1::class.java)
        intent.putExtra("title", "ADD Scan List")
        intent.putExtra("type", BarcodeType.PICKUP_ADD_SCAN)
        intent.putExtra("pickup_no", pickupNo)
        intent.putExtra("applicant", applicant)
        resultLauncher.launch(intent)
    }

    private fun clickedTakeBack() {

        val intent = Intent(this@TodayDonePickupScanListActivity, CaptureActivity1::class.java)
        intent.putExtra("title", "Take Back List")
        intent.putExtra("type", BarcodeType.PICKUP_TAKE_BACK)
        intent.putExtra("pickup_no", pickupNo)
        intent.putExtra("applicant", applicant)
        intent.putExtra("qty", scannedQty)
        resultLauncher.launch(intent)
    }

    private val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        setResult(RESULT_OK)
        finish()
    }
}