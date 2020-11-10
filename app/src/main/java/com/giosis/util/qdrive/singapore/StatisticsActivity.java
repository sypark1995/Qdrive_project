package com.giosis.util.qdrive.singapore;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author krm0219
 * Statistics
 */
public class StatisticsActivity extends AppCompatActivity {
    String TAG = "StatisticsActivity";


    FrameLayout layout_top_back;
    TextView text_top_title;

    RelativeLayout layout_statistics_type;
    TextView text_statistics_type;
    Spinner spinner_statistics_type;
    RelativeLayout layout_statistics_result_type;
    TextView text_statistics_result_type;
    Spinner spinner_statistics_result_type;

    RelativeLayout layout_statistics_delivery_type;
    RelativeLayout layout_statistics_delivery_type1;
    TextView text_statistics_delivery_type;
    Spinner spinner_statistics_delivery_type;
    TextView text_statistics_start_date;
    TextView text_statistics_end_date;
    Button btn_statistics_search;

    LinearLayout layout_statistics_delivery_result_count;
    LinearLayout layout_statistics_d_total;
    TextView text_statistics_d_total;
    LinearLayout layout_statistics_d_delivered;
    TextView text_statistics_d_delivered;
    LinearLayout layout_statistics_d_not_delivered;
    TextView text_statistics_d_not_delivered;
    LinearLayout layout_d_s_item;
    TextView text_d_s_dpc3_out_date;
    LinearLayout layout_d_d_item;

    LinearLayout layout_statistics_pickup_result_count;
    LinearLayout layout_statistics_p_total;
    TextView text_statistics_p_total;
    LinearLayout layout_statistics_p_done;
    TextView text_statistics_p_done;
    LinearLayout layout_statistics_p_failed;
    TextView text_statistics_p_failed;
    LinearLayout layout_statistics_p_confirmed;
    TextView text_statistics_p_confirmed;
    LinearLayout layout_p_s_item;
    LinearLayout layout_p_d_item;

    ListView list_statistics_result;
    TextView text_statistics_orders_not;


    //
    Context context;
    String opID;

    String selectedType = "Delivery";
    ArrayAdapter typeArrayAdapter;
    String selectedResultType = "Summary";
    ArrayAdapter resultTypeArrayAdapter;
    String selectedDeliveryType = "DPC3-Out";
    ArrayAdapter deliveryTypeArrayAdapter;

    Calendar startCalendar;
    Calendar endCalendar;
    Calendar maxDate;

    String dateFormat = "yyyy-MM-dd";
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);

    String searchType = "";
    int dTotalCount = 0;
    int deliveredCount = 0;
    int pTotalCount = 0;
    int doneCount = 0;
    int failedCount = 0;
    int confirmedCount = 0;

    StatisticsResultListAdapter listAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        layout_top_back = findViewById(R.id.layout_top_back);
        text_top_title = findViewById(R.id.text_top_title);

        initView();

        //
        context = getApplicationContext();
//        opID = SharedPreferencesHelper.getSigninOpID(context);
        opID = MyApplication.preferences.getUserId();

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        maxDate = Calendar.getInstance();


        //
        text_top_title.setText(context.getResources().getString(R.string.navi_statistics));

        spinner_statistics_type.setPrompt(getResources().getString(R.string.text_delivery));
        typeArrayAdapter = ArrayAdapter.createFromResource(this, R.array.statistics_type, android.R.layout.simple_spinner_item);
        typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_statistics_type.setAdapter(typeArrayAdapter);
        spinner_statistics_type.setSelection(0);

        spinner_statistics_type.post(new Runnable() {
            @Override
            public void run() {

                spinner_statistics_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        text_statistics_orders_not.setVisibility(View.GONE);
                        if (listAdapter != null)
                            listAdapter.clearList();


                        selectedType = parent.getItemAtPosition(position).toString();
                        text_statistics_type.setText(selectedType);

                        if (selectedType.equals("Delivery")) {

                            layout_statistics_delivery_type.setVisibility(View.VISIBLE);
                            layout_statistics_delivery_result_count.setVisibility(View.VISIBLE);
                            layout_statistics_pickup_result_count.setVisibility(View.GONE);

                            setDeliveryResult(null);
                        } else {

                            layout_statistics_delivery_type.setVisibility(View.GONE);
                            layout_statistics_delivery_result_count.setVisibility(View.GONE);
                            layout_statistics_pickup_result_count.setVisibility(View.VISIBLE);

                            setPickupResult(null);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });

        spinner_statistics_result_type.setPrompt(getResources().getString(R.string.text_summary));
        resultTypeArrayAdapter = ArrayAdapter.createFromResource(this, R.array.statistics_result_type, android.R.layout.simple_spinner_item);
        resultTypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_statistics_result_type.setAdapter(resultTypeArrayAdapter);

        spinner_statistics_result_type.post(new Runnable() {
            @Override
            public void run() {

                spinner_statistics_result_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        text_statistics_orders_not.setVisibility(View.GONE);
                        if (listAdapter != null)
                            listAdapter.clearList();


                        selectedResultType = parent.getItemAtPosition(position).toString();
                        text_statistics_result_type.setText(selectedResultType);

                        if (selectedType.equals("Delivery")) {

                            setDeliveryResult(null);
                        } else {

                            setPickupResult(null);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });

        spinner_statistics_delivery_type.setPrompt(getResources().getString(R.string.text_dpc3_out_date));
        deliveryTypeArrayAdapter = ArrayAdapter.createFromResource(this, R.array.statistics_delivery_type, android.R.layout.simple_spinner_item);
        deliveryTypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_statistics_delivery_type.setAdapter(deliveryTypeArrayAdapter);

        spinner_statistics_delivery_type.post(new Runnable() {
            @Override
            public void run() {

                spinner_statistics_delivery_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        text_statistics_orders_not.setVisibility(View.GONE);
                        if (listAdapter != null)
                            listAdapter.clearList();
                        setDeliveryResult(null);


                        selectedDeliveryType = parent.getItemAtPosition(position).toString();
                        text_statistics_delivery_type.setText(selectedDeliveryType);

                        if (selectedDeliveryType.contains("Delivered")) {

                            text_d_s_dpc3_out_date.setText(context.getResources().getString(R.string.text_delivered_date));
                        } else {

                            text_d_s_dpc3_out_date.setText(context.getResources().getString(R.string.text_dpc3_out_date));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });

        // Date Picker
        startCalendar.set(Calendar.YEAR, maxDate.get(Calendar.YEAR));
        startCalendar.set(Calendar.MONTH, maxDate.get(Calendar.MONTH));
        startCalendar.set(Calendar.DAY_OF_MONTH, maxDate.get(Calendar.DAY_OF_MONTH));

        endCalendar.set(Calendar.YEAR, maxDate.get(Calendar.YEAR));
        endCalendar.set(Calendar.MONTH, maxDate.get(Calendar.MONTH));
        endCalendar.set(Calendar.DAY_OF_MONTH, maxDate.get(Calendar.DAY_OF_MONTH));

        text_statistics_start_date.setText(sdf.format(startCalendar.getTime()));
        text_statistics_end_date.setText(sdf.format(endCalendar.getTime()));
    }

    void initView() {

        layout_statistics_type = findViewById(R.id.layout_statistics_type);
        text_statistics_type = findViewById(R.id.text_statistics_type);
        spinner_statistics_type = findViewById(R.id.spinner_statistics_type);
        layout_statistics_result_type = findViewById(R.id.layout_statistics_result_type);
        text_statistics_result_type = findViewById(R.id.text_statistics_result_type);
        spinner_statistics_result_type = findViewById(R.id.spinner_statistics_result_type);

        layout_statistics_delivery_type = findViewById(R.id.layout_statistics_delivery_type);
        layout_statistics_delivery_type1 = findViewById(R.id.layout_statistics_delivery_type1);
        text_statistics_delivery_type = findViewById(R.id.text_statistics_delivery_type);
        spinner_statistics_delivery_type = findViewById(R.id.spinner_statistics_delivery_type);
        text_statistics_start_date = findViewById(R.id.text_statistics_start_date);
        text_statistics_end_date = findViewById(R.id.text_statistics_end_date);
        btn_statistics_search = findViewById(R.id.btn_statistics_search);

        layout_statistics_delivery_result_count = findViewById(R.id.layout_statistics_delivery_result_count);
        layout_statistics_d_total = findViewById(R.id.layout_statistics_d_total);
        text_statistics_d_total = findViewById(R.id.text_statistics_d_total);
        layout_statistics_d_delivered = findViewById(R.id.layout_statistics_d_delivered);
        text_statistics_d_delivered = findViewById(R.id.text_statistics_d_delivered);
        layout_statistics_d_not_delivered = findViewById(R.id.layout_statistics_d_not_delivered);
        text_statistics_d_not_delivered = findViewById(R.id.text_statistics_d_not_delivered);
        layout_d_s_item = findViewById(R.id.layout_d_s_item);
        text_d_s_dpc3_out_date = findViewById(R.id.text_d_s_dpc3_out_date);
        layout_d_d_item = findViewById(R.id.layout_d_d_item);

        layout_statistics_pickup_result_count = findViewById(R.id.layout_statistics_pickup_result_count);
        layout_statistics_p_total = findViewById(R.id.layout_statistics_p_total);
        text_statistics_p_total = findViewById(R.id.text_statistics_p_total);
        layout_statistics_p_done = findViewById(R.id.layout_statistics_p_done);
        text_statistics_p_done = findViewById(R.id.text_statistics_p_done);
        layout_statistics_p_failed = findViewById(R.id.layout_statistics_p_failed);
        text_statistics_p_failed = findViewById(R.id.text_statistics_p_failed);
        layout_statistics_p_confirmed = findViewById(R.id.layout_statistics_p_confirmed);
        text_statistics_p_confirmed = findViewById(R.id.text_statistics_p_confirmed);
        layout_p_s_item = findViewById(R.id.layout_p_s_item);
        layout_p_d_item = findViewById(R.id.layout_p_d_item);

        list_statistics_result = findViewById(R.id.list_statistics_result);
        text_statistics_orders_not = findViewById(R.id.text_statistics_orders_not);


        layout_top_back.setOnClickListener(clickListener);

        layout_statistics_type.setOnClickListener(clickListener);
        layout_statistics_result_type.setOnClickListener(clickListener);
        layout_statistics_delivery_type1.setOnClickListener(clickListener);
        text_statistics_start_date.setOnClickListener(clickListener);
        text_statistics_end_date.setOnClickListener(clickListener);
        btn_statistics_search.setOnClickListener(clickListener);

        layout_statistics_d_total.setOnClickListener(clickListener);
        layout_statistics_d_delivered.setOnClickListener(clickListener);
        layout_statistics_d_not_delivered.setOnClickListener(clickListener);
        layout_statistics_p_total.setOnClickListener(clickListener);
        layout_statistics_p_done.setOnClickListener(clickListener);
        layout_statistics_p_failed.setOnClickListener(clickListener);
        layout_statistics_p_confirmed.setOnClickListener(clickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, TAG + "  onResume");

        setDeliveryResult(null);
        setPickupResult(null);
    }

    // NOTIFICATION.  Delivery
    @SuppressLint("SetTextI18n")
    void setDeliveryResult(StatisticsResult result) {

        Log.e(TAG, TAG + "  setDeliveryResult  " + selectedResultType + " / " + selectedDeliveryType);

        if (selectedResultType.equals("Summary")) {

            layout_d_s_item.setVisibility(View.VISIBLE);
            layout_d_d_item.setVisibility(View.GONE);
        } else {

            layout_d_s_item.setVisibility(View.GONE);
            layout_d_d_item.setVisibility(View.VISIBLE);
        }


        if (result != null) {

            text_statistics_d_total.setText(Integer.toString(dTotalCount));
            text_statistics_d_delivered.setText(Integer.toString(deliveredCount));
            text_statistics_d_not_delivered.setText(Integer.toString(dTotalCount - deliveredCount));

            listAdapter = new StatisticsResultListAdapter(context, result, searchType);
            list_statistics_result.setAdapter(listAdapter);
        } else {

            text_statistics_d_total.setText(Integer.toString(0));
            text_statistics_d_delivered.setText(Integer.toString(0));
            text_statistics_d_not_delivered.setText(Integer.toString(0));
        }
    }


    // NOTIFICATION.  Pickup
    @SuppressLint("SetTextI18n")
    void setPickupResult(StatisticsResult result) {

        if (selectedResultType.equals("Summary")) {

            layout_p_s_item.setVisibility(View.VISIBLE);
            layout_p_d_item.setVisibility(View.GONE);
        } else {

            layout_p_s_item.setVisibility(View.GONE);
            layout_p_d_item.setVisibility(View.VISIBLE);
        }

        if (result != null) {

            text_statistics_p_total.setText(Integer.toString(pTotalCount));
            text_statistics_p_done.setText(Integer.toString(doneCount));
            text_statistics_p_failed.setText(Integer.toString(failedCount));
            text_statistics_p_confirmed.setText(Integer.toString(confirmedCount));

            listAdapter = new StatisticsResultListAdapter(context, result, searchType);
            list_statistics_result.setAdapter(listAdapter);
        } else {

            text_statistics_p_total.setText(Integer.toString(0));
            text_statistics_p_done.setText(Integer.toString(0));
            text_statistics_p_failed.setText(Integer.toString(0));
            text_statistics_p_confirmed.setText(Integer.toString(0));
        }
    }


    void downloadStatistics(String status) {

        String type = selectedType + selectedResultType;    // DeliverySummary, DeliveryDetail, PickupSummary, PickupDetail

        if (type.equalsIgnoreCase("DeliverySummary")) {

            if (selectedDeliveryType.contains("Delivered")) {
                searchType = "D_Summary_DLV";
            } else {
                searchType = "D_Summary";
            }
        } else if (type.equalsIgnoreCase("DeliveryDetail")) {

            if (selectedDeliveryType.contains("Delivered")) {
                searchType = "D_Detail_DLV";
            } else {
                searchType = "D_Detail";
            }
            status = "total";
        } else if (type.equalsIgnoreCase("PickupSummary")) {

            searchType = "P_Summary";
        } else if (type.equalsIgnoreCase("PickupDetail")) {

            searchType = "P_Detail";
            status = "total";
        }

        Log.e(TAG, TAG + " : " + searchType + " / " + status);

        new StatisticsDownloadHelper.Builder(StatisticsActivity.this, opID, searchType,
                sdf.format(startCalendar.getTime()), sdf.format(endCalendar.getTime()), status).setOnStatisticsDownloadListener(
                new StatisticsDownloadHelper.OnStatisticsDownloadListener() {

                    @Override
                    public void onDownloadResult(String searchType, StatisticsResult result) {

                        if (result != null) {

                            if (searchType.contains("D_Summary")) {

                                dTotalCount = 0;
                                deliveredCount = 0;

                                if (result.getSummaryDataArrayList().size() == 0) {

                                    list_statistics_result.setVisibility(View.GONE);
                                    text_statistics_orders_not.setVisibility(View.VISIBLE);
                                } else {

                                    list_statistics_result.setVisibility(View.VISIBLE);
                                    text_statistics_orders_not.setVisibility(View.GONE);

                                    for (int i = 0; i < result.getSummaryDataArrayList().size(); i++) {

                                        dTotalCount += result.getSummaryDataArrayList().get(i).getTotalCount();
                                        deliveredCount += result.getSummaryDataArrayList().get(i).getDeliveredCount();
                                    }

                                    setDeliveryResult(result);
                                }
                            } else if (searchType.contains("D_Detail")) {
                                if (result.getDetailDataArrayList().size() == 0) {

                                    list_statistics_result.setVisibility(View.GONE);
                                    text_statistics_orders_not.setVisibility(View.VISIBLE);
                                } else {

                                    list_statistics_result.setVisibility(View.VISIBLE);
                                    text_statistics_orders_not.setVisibility(View.GONE);

                                    setDeliveryResult(result);
                                }
                            } else if (searchType.contains("P_Summary")) {

                                pTotalCount = 0;
                                doneCount = 0;
                                failedCount = 0;
                                confirmedCount = 0;

                                if (result.getSummaryDataArrayList().size() == 0) {

                                    list_statistics_result.setVisibility(View.GONE);
                                    text_statistics_orders_not.setVisibility(View.VISIBLE);
                                } else {

                                    list_statistics_result.setVisibility(View.VISIBLE);
                                    text_statistics_orders_not.setVisibility(View.GONE);

                                    for (int i = 0; i < result.getSummaryDataArrayList().size(); i++) {

                                        pTotalCount += result.getSummaryDataArrayList().get(i).getTotalCount();
                                        doneCount += result.getSummaryDataArrayList().get(i).getDoneCount();
                                        failedCount += result.getSummaryDataArrayList().get(i).getFailedCount();
                                        confirmedCount += result.getSummaryDataArrayList().get(i).getConfirmedCount();
                                    }

                                    setPickupResult(result);
                                }
                            } else if (searchType.contains("P_Detail")) {
                                if (result.getDetailDataArrayList().size() == 0) {

                                    list_statistics_result.setVisibility(View.GONE);
                                    text_statistics_orders_not.setVisibility(View.VISIBLE);
                                } else {

                                    list_statistics_result.setVisibility(View.VISIBLE);
                                    text_statistics_orders_not.setVisibility(View.GONE);

                                    setPickupResult(result);
                                }
                            }
                        }
                    }
                }).build().execute();
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.layout_top_back: {

                    finish();
                }
                break;

                case R.id.layout_statistics_type: {

                    spinner_statistics_type.performClick();
                }
                break;

                case R.id.layout_statistics_result_type: {

                    spinner_statistics_result_type.performClick();
                }
                break;

                case R.id.layout_statistics_delivery_type1: {

                    spinner_statistics_delivery_type.performClick();
                }
                break;

                case R.id.text_statistics_start_date: {

                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            StatisticsActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                                    startCalendar.set(Calendar.YEAR, year);
                                    startCalendar.set(Calendar.MONTH, month);
                                    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    text_statistics_start_date.setText(sdf.format(startCalendar.getTime()));
                                }
                            },
                            startCalendar.get(Calendar.YEAR),
                            startCalendar.get(Calendar.MONTH),
                            startCalendar.get(Calendar.DAY_OF_MONTH));

                    datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
                    datePickerDialog.show();
                }
                break;

                case R.id.text_statistics_end_date: {

                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            StatisticsActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                                    endCalendar.set(Calendar.YEAR, year);
                                    endCalendar.set(Calendar.MONTH, month);
                                    endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    text_statistics_end_date.setText(sdf.format(endCalendar.getTime()));
                                }
                            },
                            endCalendar.get(Calendar.YEAR),
                            endCalendar.get(Calendar.MONTH),
                            endCalendar.get(Calendar.DAY_OF_MONTH));

                    datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
                    datePickerDialog.show();
                }
                break;

                case R.id.btn_statistics_search: {

                    // 날짜 비교
                    // 0 이면 같음. 1이면 >, -1이면 <
                    int compareDate = startCalendar.compareTo(endCalendar);

                    if (0 < compareDate) { // start date 가 더 크다  : Error!

                        Toast.makeText(StatisticsActivity.this, context.getResources().getString(R.string.msg_check_date_range), Toast.LENGTH_SHORT).show();
                    } else {

                        long dateDiff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
                        int diff = (int) (dateDiff / (24 * 60 * 60 * 1000));            // 30일 이상
                        Log.e(TAG, TAG + "  compareDate : " + compareDate + " / diff Date : " + diff);

                        if (30 < diff) {

                            Toast.makeText(StatisticsActivity.this, context.getResources().getString(R.string.msg_check_date_range), Toast.LENGTH_SHORT).show();
                        } else {

                            downloadStatistics("");
                        }
                    }
                }
                break;

                case R.id.layout_statistics_d_total: {

                    downloadStatistics("total");
                }
                break;

                case R.id.layout_statistics_d_delivered: {

                    downloadStatistics("delivered");
                }
                break;

                case R.id.layout_statistics_d_not_delivered: {

                    downloadStatistics("not_delivered");
                }
                break;

                case R.id.layout_statistics_p_total: {

                    downloadStatistics("total");
                }
                break;

                case R.id.layout_statistics_p_done: {

                    downloadStatistics("done");
                }
                break;
                case R.id.layout_statistics_p_failed: {

                    downloadStatistics("failed");
                }
                break;
                case R.id.layout_statistics_p_confirmed: {

                    downloadStatistics("confirmed");
                }
                break;
            }
        }
    };
}