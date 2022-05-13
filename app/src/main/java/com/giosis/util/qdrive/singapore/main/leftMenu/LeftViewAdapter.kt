package com.giosis.util.qdrive.singapore.main.leftMenu


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.main.MainActivity


class LeftViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "LeftViewAdapter"

    var item = ArrayList<NavListItem>()

    private var expandedPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nav_list, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(item[position])
        holder.setIsRecyclable(false)
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
                        (view.context as MainActivity).leftMenuGone()
                    }
                    LeftMenu.STATI_MENU,
                    LeftMenu.CREATE_PICKUP_MENU,
                    LeftMenu.SETTING_MENU -> {
                        (view.context as MainActivity).leftMenuGone()
                        val intent = Intent(view.context, item.className)
                        (view.context as MainActivity).startActivity(intent)
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
}