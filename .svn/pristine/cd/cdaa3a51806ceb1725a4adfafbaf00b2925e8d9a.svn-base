package com.giosis.util.qdrive.barcodescanner;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.giosis.util.qdrive.singapore.R;

import java.util.ArrayList;


public class FailListActivity extends AppCompatActivity {
    String TAG = "FailListActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    ListView list_fail_data;

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail_list);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        list_fail_data = findViewById(R.id.list_fail_data);

        //
        layout_top_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        text_top_title.setText(R.string.text_title_fail_list);


        context = getApplicationContext();


        Intent intent = getIntent();
        String failString = intent.getStringExtra("failList");

        // TODO TEST
        //  failString = "1:test1, 2:test2, 3:test3, 4:test4, 5:test5";

        ArrayList<FailData> faildDataList = new ArrayList<>();

        if (failString.length() > 0) {
            String[] failList = failString.split(",");
            for (int i = 0; i < failList.length; i++) {
                String[] faildata = failList[i].split(":");
                if (faildata.length > 1) {
                    FailData data = new FailData(faildata[0], faildata[1]);
                    faildDataList.add(data);
                }
            }
        }

        FailedAdapter adapter = new FailedAdapter(this, R.layout.item_fail_list, faildDataList);
        list_fail_data.setAdapter(adapter);
    }


    private class FailedAdapter extends ArrayAdapter<FailData> {

        private ArrayList<FailData> items;

        public FailedAdapter(Context context, int textViewResourceId, ArrayList<FailData> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.item_fail_list, null);
            }

            TextView text_fail_list_item_tracking_no = v.findViewById(R.id.text_fail_list_item_tracking_no);
            TextView text_fail_list_item_fail_reason = v.findViewById(R.id.text_fail_list_item_fail_reason);

            FailData p = items.get(position);

            if (p != null) {

                text_fail_list_item_tracking_no.setText(p.getShippingNo());
                text_fail_list_item_fail_reason.setText(p.getReason());
            }

            return v;
        }
    }


    class FailData {

        private String ShippingNo;
        private String Reason;

        public FailData(String _ShippingNo, String _Reason) {
            this.ShippingNo = _ShippingNo;
            this.Reason = _Reason;
        }

        public String getShippingNo() {
            return ShippingNo;
        }

        public String getReason() {
            return Reason;
        }
    }
}
