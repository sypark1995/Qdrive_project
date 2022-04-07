package com.giosis.library.main

import android.content.Intent
import android.graphics.drawable.Drawable
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


class NavListViewAdapter2() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "NavListViewAdapter2"
    private val item = ArrayList<NavListItem>()
    private var expandedPos = -1
    private var typeHeader = 0
    private var typeItem = 1
    private var beforeTitle = ""
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
        private val driverOffice: TextView = view.findViewById(R.id.text_nav_header_driver_office)
        private val driverName: TextView = view.findViewById(R.id.text_nav_header_driver_name)
        private val btnMessage: ImageView = view.findViewById(R.id.btn_message)
        fun bind() {
            driverOffice.text = Preferences.officeName
            driverName.text = Preferences.userName
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
        private val childRecyclerView: RecyclerView = view.findViewById(R.id.childRecyclerView)


        fun bind(item: NavListItem) {
            textNavListTitle.text = item.title
            imgNavListIcon.background = item.icon
            imgNavListArrowImg.setBackgroundResource(R.drawable.qdrive_side_arrow)

            if (item.childArrayList == null) {
                imgNavListArrowImg.visibility = View.GONE
            } else {
                imgNavListArrowImg.visibility = View.VISIBLE
                childRecyclerView.adapter = NavListViewAdapter3(item.childArrayList!!)
            }
            beforeTitle = textNavListTitle.text.toString()
            groupItemLayout.setOnClickListener {
                expandedPos = position
                item.isClicked = !item.isClicked

                when (item.title) {
                    view.resources.getString(R.string.navi_home) -> {
                        (view.context as AppBaseActivity).leftMenuGone()
//                        val intent = Intent(view.context, MainActivity::class.java)
//                        (view.context as AppBaseActivity).startActivity(intent)
                    }
                    view.resources.getString(R.string.navi_scan) -> {

//                        val intent = Intent(view.context, ScanActivity::class.java)
//                        (view.context as AppBaseActivity).startActivity(intent)
                    }
                    view.resources.getString(R.string.navi_list) -> {
//                        val intent = Intent(view.context, ListActivity::class.java)
//                        (view.context as AppBaseActivity).startActivity(intent)
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
            if (expandedPos == position) {
                if (item.isClicked) {
                    childRecyclerView.visibility = View.VISIBLE
                    imgNavListArrowImg.setBackgroundResource(R.drawable.qdrive_side_arrow_up)
                    if (item.title != view.resources.getString(R.string.navi_home)) {
                        textNavListTitle.setTextColor(
                            ContextCompat.getColor(
                                view.context,
                                R.color.color_4fb648
                            )
                        )
                    }

                    when (item.title) {
                        view.resources.getString(R.string.navi_scan) -> {
                            imgNavListIcon.setBackgroundResource(R.drawable.qdrive_side_scan_h)
                        }
                        view.resources.getString(R.string.navi_list) -> {
                            imgNavListIcon.setBackgroundResource(R.drawable.qdrive_side_list_h)
                        }
                    }
                } else {
                    childRecyclerView.visibility = View.GONE
                    imgNavListArrowImg.setBackgroundResource(R.drawable.qdrive_side_arrow)
                    textNavListTitle.setTextColor(
                        ContextCompat.getColor(
                            view.context,
                            R.color.color_303030
                        )
                    )
                }
            } else {
                childRecyclerView.visibility = View.GONE
            }
        }

    }


    override fun getItemCount(): Int {
        return if (item.size > 0) {
            item.size
        } else 0
    }

    fun addItem(
        icon: Drawable?,
        title: String?,
        list: ArrayList<String>?,
        position: Int
    ) {
        val data = NavListItem()
        data.icon = icon
        data.title = title
        data.childArrayList = list
        if (position != -1) {
            item.add(position, data)
        } else {
            item.add(data)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (position) {
            0 -> typeHeader
            else -> typeItem
        }
}