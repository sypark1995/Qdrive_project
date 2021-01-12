package com.giosis.library.main.submenu

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.giosis.library.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author krm0219
 * NavigationBar > LIST > Not-IN Parcels
 */
class ListNotInHousedAdapter(var context: Context, var result: NotInHousedResult) : BaseExpandableListAdapter() {
    var TAG = "ListNotInHousedAdapter"

    override fun getGroup(i: Int): Any {

        return if (result.resultObject == null) {
            0
        } else result.resultObject!![i]
    }

    override fun getGroupCount(): Int {
        return result.resultObject!!.size
    }

    override fun getGroupId(i: Int): Long {
        return i.toLong()
    }

    override fun getGroupView(group_position: Int, isExpanded: Boolean, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_not_in_housed, null)
        } else {
            convertView
        }

        val layout_not_in_parcels_item_card_view = view.findViewById<LinearLayout>(R.id.layout_not_in_parcels_item_card_view)
        val text_not_in_parcels_item_pickup_no = view.findViewById<TextView>(R.id.text_not_in_parcels_item_pickup_no)
        val text_not_in_parcels_item_seller_name = view.findViewById<TextView>(R.id.text_not_in_parcels_item_seller_name)
        val img_not_in_parcels_item_up_icon = view.findViewById<ImageView>(R.id.img_not_in_parcels_item_up_icon)
        val text_not_in_parcels_item_address = view.findViewById<TextView>(R.id.text_not_in_parcels_item_address)
        val text_not_in_parcels_item_desired_date = view.findViewById<TextView>(R.id.text_not_in_parcels_item_desired_date)
        val text_not_in_parcels_item_qty = view.findViewById<TextView>(R.id.text_not_in_parcels_item_qty)
        val text_not_in_parcels_item_not_processed_qty = view.findViewById<TextView>(R.id.text_not_in_parcels_item_not_processed_qty)


        val item = result.resultObject!![group_position]

        if (isExpanded && result.resultObject!![group_position].subLists != null) {

            layout_not_in_parcels_item_card_view.setBackgroundResource(R.drawable.bg_top_round_10_ffffff)
            img_not_in_parcels_item_up_icon.visibility = View.VISIBLE
        } else {

            layout_not_in_parcels_item_card_view.setBackgroundResource(R.drawable.bg_round_10_ffffff_shadow)
            img_not_in_parcels_item_up_icon.visibility = View.GONE
        }

        text_not_in_parcels_item_pickup_no.text = item.invoiceNo
        text_not_in_parcels_item_seller_name.text = item.reqName
        text_not_in_parcels_item_address.text = "(${item.zipCode}) ${item.address}"
        text_not_in_parcels_item_qty.text = item.real_qty

        if (item.subLists!!.isEmpty()) {

            text_not_in_parcels_item_not_processed_qty.text = "0"
        } else {

            text_not_in_parcels_item_not_processed_qty.text = item.not_processed_qty
        }


        try {

            val date = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.ENGLISH).parse(item.pickup_date) //"Jul 24 2018  4:01PM"
            val pickupDate = SimpleDateFormat("yyyy-MM-dd").format(date)
            text_not_in_parcels_item_desired_date.text = pickupDate
        } catch (e: ParseException) {

            Log.e("krm0219", "$TAG Exception : $e")
            text_not_in_parcels_item_desired_date.text = context.resources.getString(R.string.text_error)
            e.printStackTrace()
        }


        return view
    }

    override fun getChildrenCount(i: Int): Int {
        return if (result.resultObject!![i].subLists == null) {
            0
        } else result.resultObject!![i].subLists!!.size
    }

    override fun getChild(i: Int, i1: Int): Any {
        return result.resultObject!![i].subLists!![i1]
    }

    override fun getChildId(i: Int, i1: Int): Long {
        return i1.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getChildView(group_position: Int, child_position: Int, b: Boolean, convertView: View?, viewGroup: ViewGroup): View {
        val view: View

        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_not_in_housed_child, null)
        } else {
            convertView
        }

        val layout_not_in_parcels_child_view = view.findViewById<LinearLayout>(R.id.layout_not_in_parcels_child_view)
        val text_not_in_parcels_child_scanned_no = view.findViewById<TextView>(R.id.text_not_in_parcels_child_scanned_no)
        val text_not_in_parcels_child_amount = view.findViewById<TextView>(R.id.text_not_in_parcels_child_amount)
        val text_not_in_parcels_child_currency = view.findViewById<TextView>(R.id.text_not_in_parcels_child_currency)

        val subitem = result.resultObject!![group_position].subLists!![child_position]
        val sub_size = result.resultObject!![group_position].subLists!!.size
        Log.e("krm0219", result.resultObject!![group_position].subLists!!.size.toString() + "  " + child_position)

        if (sub_size - 1 == child_position) {

            layout_not_in_parcels_child_view.setPadding(0, 0, 0, 40)
            layout_not_in_parcels_child_view.setBackgroundResource(R.drawable.bg_bottom_round_10_ffffff)
        } else {

            layout_not_in_parcels_child_view.setPadding(0, 0, 0, 0)
            layout_not_in_parcels_child_view.setBackgroundColor(context.resources.getColor(R.color.white))
        }

        text_not_in_parcels_child_scanned_no.text = subitem.packingNo
        text_not_in_parcels_child_amount.text = subitem.purchasedAmount
        text_not_in_parcels_child_currency.text = "(${subitem.purchaseCurrency})"
        return view
    }

    override fun isChildSelectable(i: Int, i1: Int): Boolean {
        return true
    }
}