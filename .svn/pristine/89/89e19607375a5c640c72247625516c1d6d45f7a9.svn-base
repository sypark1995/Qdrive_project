package com.giosis.util.qdrive.qdelivery

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import com.giosis.util.qdrive.singapore.R
import kotlinx.android.synthetic.main.dialog_search_address.*
import java.util.*


class AddressDialog(context: Context) : Dialog(context) {

    interface SearchListener {
        fun onSubmitClicked(zipCode: String, address: String)
    }


    lateinit var searchListener: SearchListener

    fun setDialogListener(searchListener: SearchListener) {
        this.searchListener = searchListener
    }


    var progressDialog: ProgressDialog? = null
    var zipCodeArrayList: ArrayList<String>? = null
    var addressArrayList: ArrayList<String>? = null
    var searchAddressListAdapter: SearchAddressListAdapter? = null

    var keyword: String? = null
    var selectedZipCode: String? = null
    var selectedFrontAddress: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_search_address)


        //
        edit_search_address_keyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                if (charSequence.isNotEmpty()) {
                    btn_search_address_delete.visibility = View.VISIBLE
                } else {
                    btn_search_address_delete.visibility = View.GONE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })


        list_search_address.onItemClickListener = OnItemClickListener { adapterView, view, position, id ->
            selectedZipCode = zipCodeArrayList?.get(position)
            selectedFrontAddress = addressArrayList?.get(position)
            SearchAddressListAdapter.setSelectedItem(position)
            searchAddressListAdapter?.notifyDataSetChanged()
        }






        searchListener.onSubmitClicked("123", "front address")



        btn_search_address_close.setOnClickListener {

            dismiss()
        }
    }
}
