package com.giosis.library.main.route

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.giosis.library.R
import com.giosis.library.databinding.ActivityMyRouteBinding
import kotlinx.android.synthetic.main.activity_my_route.*
import kotlinx.android.synthetic.main.top_title.*

class TodayMyRouteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyRouteBinding
    val viewModel: TodayMyRouteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_route)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        text_top_title.text = resources.getString(R.string.text_today_my_route)

        layout_top_back.setOnClickListener {

            finish()
        }


        //
        val adapter = RouteAdapter(viewModel)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setHasFixedSize(true)


        // spinner
        val routeTypes = resources.getStringArray(R.array.route_type)

        layout_my_route_type_text.setOnClickListener {

            spinner_my_route_type.performClick()
        }


        viewModel.routeType.observe(this, Observer {

            spinner_my_route_type.setSelection(it)
            text_my_route_type.text = routeTypes[it]

            if (it == 0) {

                layout_my_route_pickup.visibility = View.VISIBLE
                layout_my_route_delivery.visibility = View.GONE
            } else {

                layout_my_route_pickup.visibility = View.GONE
                layout_my_route_delivery.visibility = View.VISIBLE
            }

            viewModel.getCount()
        })


        //   setDataObserve()
    }
}