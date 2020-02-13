package com.giosis.util.qdrive.qdelivery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

/**
 * @author krm0219
 **/
public class SearchAddressListAdapter extends BaseAdapter {

    private Context context;


    private SearchAddressResult searchAddressResult;
    static int selectedItem = -1;

    public SearchAddressListAdapter(Context mContext, SearchAddressResult searchAddressResult) {

        this.context = mContext;
        this.searchAddressResult = searchAddressResult;
    }


    @Override
    public int getCount() {

        if (searchAddressResult.getAdddressArrayList() != null && searchAddressResult.getAdddressArrayList().size() > 0) {
            return searchAddressResult.getAdddressArrayList().size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return searchAddressResult.getAdddressArrayList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_search_address, null);
        } else {

            view = convertView;
        }

        final FrameLayout layout_search_address_item = view.findViewById(R.id.layout_search_address_item);
        TextView text_search_address_item = view.findViewById(R.id.text_search_address_item);


        if (selectedItem != -1 && position == selectedItem) {

            layout_search_address_item.setBackgroundColor(context.getResources().getColor(R.color.color_4fb648));
        } else {

            layout_search_address_item.setBackgroundColor(context.getResources().getColor(R.color.white));
        }


        text_search_address_item.setText(searchAddressResult.getAdddressArrayList().get(position));
        return view;
    }

    public static void setSelectedItem(int position) {
        selectedItem = position;
    }
}
