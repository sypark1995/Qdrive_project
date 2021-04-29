package com.giosis.library.gps

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.giosis.library.R
import com.giosis.library.server.RetrofitClient
import com.giosis.library.util.OnServerEventListener
import com.giosis.library.util.Preferences
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_gps_update.*


class GpsUpdateDialog(context: Context, private val model: LocationModel, val listener: OnServerEventListener) : Dialog(context) {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_gps_update)

        val parcelLat = String.format("%.6f", model.parcelLat)
        val parcelLng = String.format("%.6f", model.parcelLng)
        val myLat = String.format("%.6f", model.driverLat)
        val myLng = String.format("%.6f", model.driverLng)

        text_gps_qlps.text = "$parcelLat, $parcelLng"
        text_gps_your.text = "$myLat, $myLng"

        var updateCount = 0

        btn_gps_update_ok.setOnClickListener {

            // 실패 시 1회 재시도 하고, 그래도 실패하면 끝
            if (updateCount == 1) {

                dismiss()
                listener.onPostResult()
            } else {

                Log.e("GPSUpdate", "${Preferences.userNation} / ${model.zipCode} / ${model.state} / ${model.city} / ${model.street} / ${model.driverLat}, ${model.driverLng}")
                RetrofitClient.instanceDynamic().requestSetAddressUsingDriver(model.zipCode, model.state, model.city, model.street, model.driverLat, model.driverLng)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({

                            Log.e("GPSUpdate", "SetAddressUsingDriver Result ${it.resultCode}")
                            if (it.resultCode == 0) {

                                Toast.makeText(context, context.getString(R.string.msg_gps_update_success), Toast.LENGTH_SHORT).show()
                                dismiss()
                                listener.onPostResult()
                            } else {

                                updateCount++
                                Toast.makeText(context, context.resources.getString(R.string.msg_please_try_again), Toast.LENGTH_SHORT).show()
                            }
                        }, {

                            dismiss()
                            listener.onPostResult()
                        })
            }
        }

        btn_gps_update_cancel.setOnClickListener {

            dismiss()
            listener.onPostResult()
        }
    }

    override fun onBackPressed() {
    }
}