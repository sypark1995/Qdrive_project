package com.giosis.library.main.leftMenu


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giosis.library.R
import com.giosis.library.main.AppBaseActivity
import com.giosis.library.message.MessageListActivity
import com.giosis.library.util.Preferences


class LeftViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "LeftViewAdapter"

    var item = ArrayList<NavListItem>()

    private var expandedPos = -1
    private var typeHeader = 0
    private var typeItem = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            typeHeader -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_nav_list_header, parent, false)
                HeaderViewHolder(view)
            }
            typeItem -> {
                val view = LayoutInflater.from(parent.context)
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
            textNavListTitle.text = view.resources.getString(item.titleResource)
            imgNavListIcon.setImageResource(item.iconId)

            if (item.subList == null) {
                imgNavListArrowImg.visibility = View.GONE
            } else {
                imgNavListArrowImg.visibility = View.VISIBLE
                leftChildRecyclerView.adapter = LeftChildViewAdapter(item.subList!!)
            }

            if (expandedPos == layoutPosition) {
                leftChildRecyclerView.visibility = View.VISIBLE

                imgNavListIcon.isSelected = true
                textNavListTitle.isSelected = true
                imgNavListArrowImg.isSelected = true

            } else {
                leftChildRecyclerView.visibility = View.GONE

                imgNavListIcon.isSelected = false
                textNavListTitle.isSelected = false
                imgNavListArrowImg.isSelected = false
            }

            groupItemLayout.setOnClickListener {

                when (item) {
                    LeftMenu.HOME_MENU -> {
                        (view.context as AppBaseActivity).leftMenuGone()
                    }
                    LeftMenu.STATI_MENU,
                    LeftMenu.CREATE_PICKUP_MENU,
                    LeftMenu.SETTING_MENU -> {
                        (view.context as AppBaseActivity).leftMenuGone()
                        val intent = Intent(view.context, item.className)
                        (view.context as AppBaseActivity).startActivity(intent)
                    }
                    else -> {
                        expandedPos = if (expandedPos == layoutPosition) {
                            -1
                        } else {
                            layoutPosition
                        }
                    }
                }

                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun getItemViewType(position: Int): Int =
        when (position) {
            0 -> typeHeader
            else -> typeItem
        }
}