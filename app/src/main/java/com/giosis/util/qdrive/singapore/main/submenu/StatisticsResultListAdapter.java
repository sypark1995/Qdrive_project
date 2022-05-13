package com.giosis.util.qdrive.singapore.main.submenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.singapore.data.StatisticsResult;

import java.util.ArrayList;


public class StatisticsResultListAdapter extends BaseAdapter {
    private String TAG = "StatisticsResultListAdapter";


    private Context context;
    private String searchType;

    private ArrayList<StatisticsResult.SummaryData> summaryDataArrayList;
    private ArrayList<StatisticsResult.DetailData> detailDataArrayList;

    StatisticsResultListAdapter(Context context, StatisticsResult statisticsResult, String searchType) {

        this.context = context;
        this.searchType = searchType;

        if (searchType.contains("Summary")) {

            summaryDataArrayList = statisticsResult.getSummaryDataArrayList();
        } else if (searchType.contains("Detail")) {

            detailDataArrayList = statisticsResult.getDetailDataArrayList();
        }
    }


    @Override
    public int getCount() {

        if (searchType.contains("Summary")) {

            if (summaryDataArrayList != null && summaryDataArrayList.size() > 0) {
                return summaryDataArrayList.size();
            }
        } else if (searchType.contains("Detail")) {

            if (detailDataArrayList != null && detailDataArrayList.size() > 0) {
                return detailDataArrayList.size();
            }
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {

        if (searchType.contains("Summary")) {

            return summaryDataArrayList.get(position);
        } else if (searchType.contains("Detail")) {

            return detailDataArrayList.get(position);
        }

        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    void clearList() {

        if (summaryDataArrayList != null)
            summaryDataArrayList.clear();

        if (detailDataArrayList != null)
            detailDataArrayList.clear();

        notifyDataSetChanged();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = null;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (searchType.contains("D_S")) {

                view = inflater.inflate(R.layout.item_statistics_delivery_summary, null);
            } else if (searchType.contains("P_S")) {

                view = inflater.inflate(R.layout.item_statistics_pickup_summary, null);
            } else if (searchType.contains("D_D")) {

                view = inflater.inflate(R.layout.item_statistics_delivery_detail, null);
            } else if (searchType.contains("P_D")) {

                view = inflater.inflate(R.layout.item_statistics_pickup_detail, null);
            }
        } else {

            view = convertView;
        }


        if (searchType.contains("D_S")) {

            TextView text_d_s_dpc3_out_date = view.findViewById(R.id.text_d_s_dpc3_out_date);
            TextView text_d_s_total_count = view.findViewById(R.id.text_d_s_total_count);
            TextView text_d_s_delivered_count = view.findViewById(R.id.text_d_s_delivered_count);
            TextView text_d_s_delivered_percent = view.findViewById(R.id.text_d_s_delivered_percent);
            TextView text_d_s_avg_date = view.findViewById(R.id.text_d_s_avg_date);

            StatisticsResult.SummaryData data = summaryDataArrayList.get(position);

            text_d_s_dpc3_out_date.setText(data.getDate());
            text_d_s_total_count.setText(Integer.toString(data.getTotalCount()));
            text_d_s_delivered_count.setText(Integer.toString(data.getDeliveredCount()));
            text_d_s_delivered_percent.setText(data.getPercent());
            text_d_s_avg_date.setText(data.getAvgDate());

        } else if (searchType.contains("P_S")) {

            TextView text_p_s_desired_date = view.findViewById(R.id.text_p_s_desired_date);
            TextView text_p_s_total_count = view.findViewById(R.id.text_p_s_total_count);
            TextView text_p_s_done_count = view.findViewById(R.id.text_p_s_done_count);
            TextView text_p_s_pickup_percent = view.findViewById(R.id.text_p_s_pickup_percent);
            TextView text_p_s_avg_date = view.findViewById(R.id.text_p_s_avg_date);

            StatisticsResult.SummaryData data = summaryDataArrayList.get(position);

            text_p_s_desired_date.setText(data.getDate());
            text_p_s_total_count.setText(Integer.toString(data.getTotalCount()));
            text_p_s_done_count.setText(Integer.toString(data.getDoneCount()));
            text_p_s_pickup_percent.setText(data.getPercent());
            text_p_s_avg_date.setText(data.getAvgDate());

        } else if (searchType.contains("D_D")) {

            TextView text_d_d_shipping_no = view.findViewById(R.id.text_d_d_shipping_no);
            TextView text_d_d_tracking_no = view.findViewById(R.id.text_d_d_tracking_no);
            TextView text_d_d_status = view.findViewById(R.id.text_d_d_status);
            TextView text_d_d_delivered_date = view.findViewById(R.id.text_d_d_delivered_date);

            StatisticsResult.DetailData data = detailDataArrayList.get(position);

            text_d_d_shipping_no.setText(data.getShippingNo());
            text_d_d_tracking_no.setText(data.getTrackingNo());
            text_d_d_status.setText(data.getStat());
            text_d_d_delivered_date.setText(data.getDate());

        } else if (searchType.contains("P_D")) {

            TextView text_p_d_pickup_no = view.findViewById(R.id.text_p_d_pickup_no);
            TextView text_p_d_status = view.findViewById(R.id.text_p_d_status);
            TextView text_p_d_qty = view.findViewById(R.id.text_p_d_qty);
            TextView text_p_d_desired_date = view.findViewById(R.id.text_p_d_desired_date);

            StatisticsResult.DetailData data = detailDataArrayList.get(position);

            text_p_d_pickup_no.setText(data.getPickupNo());
            text_p_d_status.setText(data.getStat());
            text_p_d_qty.setText(data.getPickupQty());
            text_p_d_desired_date.setText(data.getDate());
        }

        return view;
    }
}