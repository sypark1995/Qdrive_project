package com.giosis.util.qdrive.barcodescanner;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;
import com.giosis.util.qdrive.international.UploadData;
import com.giosis.util.qdrive.util.DatabaseHelper;
import com.giosis.util.qdrive.util.PermissionActivity;
import com.giosis.util.qdrive.util.PermissionChecker;

import java.util.ArrayList;


public class PodListActivity extends ListActivity {
    String TAG = "PodListActivity";

    FrameLayout layout_top_back;
    TextView text_top_title;

    ViewHolder holder;
    String opID = "";


    private PermissionChecker checker;
    boolean isPermissionTrue = false;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = new String[]{PermissionChecker.READ_EXTERNAL_STORAGE, PermissionChecker.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pod_list);


        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        final CheckBox check_pod_list_all = findViewById(R.id.check_pod_list_all);

        Button btn_pod_list_delete = findViewById(R.id.btn_pod_list_delete);
        Button btn_pod_list_upload = findViewById(R.id.btn_pod_list_upload);


        layout_top_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        text_top_title.setText(R.string.text_title_pod_list);


        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        opID = MyApplication.preferences.getUserId();

        String selectQuery = "SELECT " +
                " invoice_no" +
                ", stat " +
                ", punchOut_stat " +
                " from " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY +
                " where reg_id= '" + opID + "'" +
                " and punchOut_stat <> 'S' " +
                " order by punchOut_stat, invoice_no";
        Cursor cs = dbHelper.get(selectQuery);
        ArrayList<PodData> PodDataList = new ArrayList<>();

        if (cs.moveToFirst()) {
            do {
                PodData data = new PodData(cs.getString(cs.getColumnIndex("invoice_no")), getPodStat(cs.getString(cs.getColumnIndex("punchOut_stat"))));
                PodDataList.add(data);
            } while (cs.moveToNext());
        }


        final PodAdapter adapter = new PodAdapter(this, R.layout.pod_list_row, PodDataList);

        check_pod_list_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setAllChecked(check_pod_list_all.isChecked());
                adapter.notifyDataSetChanged();
            }
        });

        btn_pod_list_upload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String shippingStrings = adapter.getCheckedShippingString();

                if (shippingStrings.length() > 0) {
                    ArrayList<UploadData> songjanglist = new ArrayList<>();
                    // 업로드 대상건 로컬 DB 조회
                    DatabaseHelper dbHelper = DatabaseHelper.getInstance();

                    String selectQuery = "select invoice_no" +
                            " , stat " +
                            " from " + DatabaseHelper.DB_TABLE_SCAN_DELIVERY +
                            " where reg_id= '" + opID + "'" +
                            " and punchOut_stat <> 'S' " +
                            " and invoice_no in (" + shippingStrings + ")";
                    Cursor cs = dbHelper.get(selectQuery);

                    if (cs.moveToFirst()) {
                        do {
                            UploadData data = new UploadData();
                            data.setNoSongjang(cs.getString(cs.getColumnIndex("invoice_no")));
                            data.setStat(cs.getString(cs.getColumnIndex("stat")));
                            data.setType("D");
                            songjanglist.add(data);
                        } while (cs.moveToNext());
                    }

                    if (songjanglist.size() > 0) {
                        new ManualPodUploadHelper.Builder(PodListActivity.this, opID, MyApplication.preferences.getOfficeCode(),
                                MyApplication.preferences.getDeviceUUID(), songjanglist).
                                setOnPodUploadEventListener(new ManualPodUploadHelper.OnPodUploadEventListener() {

                                    @Override
                                    public void onPostResult() {
                                        finish();
                                    }

                                    @Override
                                    public void onPostFailList(ArrayList<String> resultList) {
                                        finish();
                                    }
                                }).build().execute();
                    }
                }
            }
        });

        //Delete 버튼 클릭
        btn_pod_list_delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PodListActivity.this);
                builder.setTitle("Confirm")
                        .setMessage("Would you like to delete?")        // 메세지 설정
                        .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            // 확인 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String shippingStrings = adapter.getCheckedShippingString();
                                if (shippingStrings.length() > 0) {

                                    // 업로드 대상건 로컬 DB 조회
                                    DatabaseHelper dbHelper = DatabaseHelper.getInstance();
                                    dbHelper.delete(DatabaseHelper.DB_TABLE_SCAN_DELIVERY, " reg_id= '" + opID + "' and invoice_no in (" + shippingStrings + ") COLLATE NOCASE");

                                    String[] shippingList = shippingStrings.split(", ");
                                    for (int i = 0; i < shippingList.length; i++) {
                                        adapter.deleteRow(shippingList[i].replace("'", ""));
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            // 취소 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        })
                        .create().show();
            }
        });

        setListAdapter(adapter);


        checker = new PermissionChecker(this);

        // 권한 여부 체크 (없으면 true, 있으면 false)
        if (checker.lacksPermissions(PERMISSIONS)) {

            isPermissionTrue = false;
            PermissionActivity.startActivityForResult(this, PERMISSION_REQUEST_CODE, PERMISSIONS);
            overridePendingTransition(0, 0);
        } else {

            isPermissionTrue = true;
        }
    }


    private String getPodStat(String stat) {
        String rtn = "";
        if (stat.equals("N")) {
            rtn = "Ready";
        } else if (stat.equals("F10") || stat.equals("F11")) {
            rtn = "Please contact the helpdesk";
        } // DB 저장 실패
        else if (stat.equals("F12")) {
            rtn = " Invalid shipping No";
        } else if (stat.equals("F13")) {
            rtn = " Already delivered ";
        } else if (stat.equals("F14")) {
            rtn = " Sinature is not exists";
        } else if (stat.equals("F15")) {
            rtn = " Request Timeout";
        } else if (stat.equals("F16")) {
            rtn = " Network disconnected";
        } else if (stat.equals("F99")) {
            rtn = " Network exception";
        } else {
            rtn = "Etc Fail";
        }
        return rtn;
    }

    private class PodAdapter extends ArrayAdapter<PodData> {

        private ArrayList<PodData> items;

        public PodAdapter(Context context, int textViewResourceId, ArrayList<PodData> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        // CheckBox를 모두 선택 / 해제
        public void setAllChecked(boolean isChecked) {
            for (PodData data : items) {
                data.setSelected(isChecked);
            }
        }

        public String getCheckedShippingString() {
            String shippingNo = "";
            for (PodData data : items) {
                if (data.isSelected()) {
                    shippingNo += shippingNo.length() > 0 ? ", '" + data.getShippingNo() + "'"
                            : "'" + data.getShippingNo() + "'";
                }
            }

            return shippingNo;
        }

        public void deleteRow(String shipping) {
            for (int i = 0; i < items.size(); i++) {
                PodData data = items.get(i);
                if (data.getShippingNo().equals(shipping.trim())) {
                    items.remove(i);
                }
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            holder = new ViewHolder();

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.pod_list_row, null);
            }

            TextView text_pod_list_item_tracking_no = v.findViewById(R.id.text_pod_list_item_tracking_no);
            TextView text_pod_list_item_reason = v.findViewById(R.id.text_pod_list_item_reason);
            CheckBox check_pod_list_item = v.findViewById(R.id.check_pod_list_item);

            PodData p = items.get(position);
            if (p != null) {

                text_pod_list_item_tracking_no.setText(p.getShippingNo());
                text_pod_list_item_reason.setText(p.getReason());

                if (!text_pod_list_item_reason.getText().equals("Ready")) {
                    text_pod_list_item_reason.setTextColor(Color.RED);
                } else {
                    text_pod_list_item_reason.setTextColor(Color.BLACK);
                }

                holder.txt_shipping = text_pod_list_item_tracking_no;
                holder.cb_checkbox = check_pod_list_item;
                holder.cb_checkbox.setChecked(p.isSelected());
                holder.cb_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(
                            CompoundButton buttonView, boolean isChecked) {

                        items.get(position).setSelected(isChecked);
                    }

                });
            }
            return v;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public PodData getItem(int position) {
            return super.getItem(position);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (resultCode == PermissionActivity.PERMISSIONS_GRANTED) {
                Log.e("eylee", "PodListActivity   onActivityResult  PERMISSIONS_GRANTED");

                isPermissionTrue = true;
            }
        }
    }

    class PodData {

        private String ShippingNo;
        private String Reason;
        boolean selected = true;

        public PodData(String _ShippingNo, String _Reason) {
            this.ShippingNo = _ShippingNo;
            this.Reason = _Reason;
            this.selected = true;
        }

        public String getShippingNo() {
            return ShippingNo;
        }

        public String getReason() {
            return Reason;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    class ViewHolder {
        TextView txt_shipping;
        CheckBox cb_checkbox;
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(MyApplication.localeManager.setLocale(base));
    }
}
