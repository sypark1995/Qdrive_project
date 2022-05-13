package com.giosis.util.qdrive.singapore.main.route

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.databinding.ActivityMyRouteBinding
import com.giosis.util.qdrive.singapore.gps.GPSTrackerManager
import com.giosis.util.qdrive.singapore.util.DataUtil
import com.giosis.util.qdrive.singapore.util.PermissionActivity
import com.giosis.util.qdrive.singapore.util.PermissionChecker
import kotlinx.android.synthetic.main.activity_my_route.*
import kotlinx.android.synthetic.main.top_title.*

class TodayMyRouteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyRouteBinding
    val viewModel: TodayMyRouteViewModel by viewModels()

    // Permission
    var isPermissionTrue = false
    val PERMISSION_REQUEST_CODE = 1000
    val PERMISSIONS =
        arrayOf(PermissionChecker.ACCESS_FINE_LOCATION, PermissionChecker.ACCESS_COARSE_LOCATION)

    private val gpsTrackerManager: GPSTrackerManager? by lazy {
        GPSTrackerManager(this@TodayMyRouteActivity)
    }

    var gpsEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_route)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        text_top_title.text = resources.getString(R.string.text_today_my_route)

        layout_top_back.setOnClickListener {

            finish()
        }

        // View
        setRecyclerView()

        layout_my_route_type_text.setOnClickListener {

            spinner_my_route_type.performClick()
        }


        // Observe
        viewModel.spinnerPosition.observe(this, Observer {

            spinner_my_route_type.setSelection(it)

            val routeTypes = resources.getStringArray(R.array.route_type)
            text_my_route_type.text = routeTypes[it]

            if (it == 0) {

                layout_my_route_pickup.visibility = View.VISIBLE
                layout_my_route_delivery.visibility = View.GONE
            } else {

                layout_my_route_pickup.visibility = View.GONE
                layout_my_route_delivery.visibility = View.VISIBLE
            }

            viewModel.getCount(it)
        })

        viewModel.showToast.observe(this, Observer { event ->

            event.getContentIfNotHandled()?.let {

                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.permissionCheck.observe(this, Observer {

            if (it) {

                viewModel.getGpsManager()?.let { gpsManager ->
                    gpsEnable = gpsManager.enableGPSSetting()
                }

                if (gpsEnable && viewModel.getGpsManager() != null) {
                    viewModel.getGpsManager()!!.gpsTrackerStart()
                } else {
                    DataUtil.enableLocationSettings(this@TodayMyRouteActivity)
                }
            }
        })

        viewModel.googleMap.observe(this, Observer {

            try {

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    getString(R.string.msg_install_google_maps),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


        // permission
        val checker = PermissionChecker(this)

        if (checker.lacksPermissions(*PERMISSIONS)) {

            viewModel.setPermission(false)
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
            overridePendingTransition(0, 0)
        } else {

            viewModel.setPermission(true)
        }
    }


    private fun setRecyclerView() {

        val adapter = RouteAdapter(viewModel)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)

        viewModel.routeData.observe(this, Observer {

            binding.routeData = it
            adapter.setRouteList(it.routeList)
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == PermissionActivity.PERMISSIONS_GRANTED) {

            viewModel.setPermission(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DataUtil.stopGPSManager(gpsTrackerManager)
    }
}
