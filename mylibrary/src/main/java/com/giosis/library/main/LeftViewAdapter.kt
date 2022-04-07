package com.giosis.library.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import com.giosis.library.message.MessageListActivity
import com.giosis.library.pickup.CreatePickupOrderActivity
import com.giosis.library.setting.SettingActivity
import com.giosis.library.util.Preferences


class LeftViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "LeftViewAdapter"

    private val item = ArrayList<NavListItem>()

    private var expandedPos = -1
    private var typeHeader = 0
    private var typeItem = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            typeHeader -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_nav_list_header, parent, false)
                HeaderViewHolder(view)
            }
            typeItem -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_nav_list, parent, false)
                ViewHolder(view)
            }
            else -> throw Exception("Unknown viewType $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            (holder as HeaderViewHolder).bind()
        } else {
            (holder as ViewHolder).bind(item[position])
        }
        holder.setIsRecyclable(false)
    }

    inner class HeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val layoutMessage: RelativeLayout = view.findViewById(R.id.layout_message)
        private val driverOffice: TextView = view.findViewById(R.id.text_nav_header_driver_office)
        private val driverName: TextView = view.findViewById(R.id.text_nav_header_driver_name)
        private val btnMessage: ImageView = view.findViewById(R.id.btn_message)

        fun bind() {
            driverOffice.text = Preferences.officeName
            driverName.text = Preferences.userName

            if (Preferences.userNation == "SG") {
                layoutMessage.visibility = View.VISIBLE
            } else {
                layoutMessage.visibility = View.GONE
            }

            btnMessage.setOnClickListener {
                val intent = Intent(it.context, MessageListActivity::class.java)
                //todo_sypark data
//                intent.putExtra("customer_count", customerMessageCount)
//                intent.putExtra("admin_count", adminMessageCount)
                (it.context as AppBaseActivity).startActivity(intent)
            }
        }
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val groupItemLayout: RelativeLayout = view.findViewById(R.id.group_item_layout)
        private val imgNavListIcon: ImageView = view.findViewById(R.id.img_nav_list_icon)
        private val textNavListTitle: TextView = view.findViewById(R.id.text_nav_list_title)
        private val imgNavListArrowImg: ImageView = view.findViewById(R.id.img_nav_list_arrow_img)
        private val leftChildRecyclerView: RecyclerView =
            view.findViewById(R.id.left_child_recyclerView)

        fun bind(item: NavListItem) {
            textNavListTitle.text = item.title
            imgNavListIcon.setImageResource(item.id)
            imgNavListArrowImg.setBackgroundResource(R.drawable.qdrive_side_arrow)

            if (item.childArrayList == null) {
                imgNavListArrowImg.visibility = View.GONE
            } else {
                imgNavListArrowImg.visibility = View.VISIBLE
                leftChildRecyclerView.adapter = LeftChildViewAdapter(item.childArrayList!!)
            }

            groupItemLayout.setOnClickListener {

                when (item.title) {
                    view.resources.getString(R.string.navi_home) -> {
                        (view.context as AppBaseActivity).leftMenuGone()
                    }
                    view.resources.getString(R.string.navi_scan),
                    view.resources.getString(R.string.navi_list) -> {
                        expandedPos = if (expandedPos == layoutPosition) {
                            -1
                        } else {
                            layoutPosition
                        }
                    }
                    view.resources.getString(R.string.navi_statistics) -> {
                        (view.context as AppBaseActivity).leftMenuGone()
                        val intent = Intent(view.context, ScanActivity::class.java)
                        (view.context as AppBaseActivity).startActivity(intent)
                    }
                    view.resources.getString(R.string.text_create_pickup_order) -> {
                        (view.context as AppBaseActivity).leftMenuGone()
                        val intent = Intent(view.context, CreatePickupOrderActivity::class.java)
                        (view.context as AppBaseActivity).startActivity(intent)
                    }
                    view.resources.getString(R.string.navi_setting) -> {
                        (view.context as AppBaseActivity).leftMenuGone()
                        val intent = Intent(view.context, SettingActivity::class.java)
                        (view.context as AppBaseActivity).startActivity(intent)
                    }
                }
                notifyDataSetChanged()
            }

            imgNavListIcon.isSelected = false

            if (expandedPos == layoutPosition) {
                imgNavListIcon.isSelected = true
                leftChildRecyclerView.visibility = View.VISIBLE
                imgNavListArrowImg.setBackgroundResource(R.drawable.qdrive_side_arrow_up)

                when (item.title) {
//                    view.resources.getString(R.string.navi_scan) -> {
//                        imgNavListIcon.setImageResource(R.drawable.qdrive_side_scan_h)
//                        textNavListTitle.setTextColor(
//                            ContextCompat.getColor(
//                                view.context,
//                                R.color.color_4fb648
//                            )
//                        )
//                    }
                    view.resources.getString(R.string.navi_list) -> {
                        imgNavListIcon.setImageResource(R.drawable.qdrive_side_list_h)

                        textNavListTitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.color_4fb648
                            )
                        )
                    }
                    else -> {
                        //
                    }
                }

            } else {
                leftChildRecyclerView.visibility = View.GONE
            }
        }

    }

    override fun getItemCount(): Int {
        return item.size
    }

    fun addItem(
        id: Int,
        title: String,
        list: ArrayList<String>?
    ) {
        val data = NavListItem()
        data.id = id
        data.title = title
        data.childArrayList = list

        item.add(data)
    }

    override fun getItemViewType(position: Int): Int =
        when (position) {
            0 -> typeHeader
            else -> typeItem
        }
}