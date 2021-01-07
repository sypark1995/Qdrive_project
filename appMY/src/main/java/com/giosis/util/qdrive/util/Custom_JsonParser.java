package com.giosis.util.qdrive.util;

import android.util.Log;

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.main.ListNotInHousedResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Custom_JsonParser {
    static String TAG = "Custom_JsonParser";

    public static String requestServerDataReturnJSON(String method, JSONObject object) {

        URL url;
        HttpURLConnection conn;
        OutputStream os;
        InputStream is;
        ByteArrayOutputStream baos;

        String response = "";
        String apiURL = MyApplication.preferences.getServerURL() + DataUtil.API_ADDRESS;
        Log.e("krm0219", "JsonParser URL " + apiURL);


        try {

            url = new URL(apiURL + "/" + method);

            // URL 연결
            conn = (HttpURLConnection) url.openConnection();

            // Time Out 시간 (서버 접속 시 연결 시간)
            conn.setConnectTimeout(20000);

            // Time Out 시간 (Read시 연결 시간)
            conn.setReadTimeout(20000);

            // 서버 Response Data 형식 요청
            // conn.setRequestProperty("Accept", "application/xml");    // xml 형식
            // conn.setRequestProperty("Accept", "application/json");   // json 형식
            conn.setRequestProperty("Accept", "*/*");

            // User Agent
            conn.setRequestProperty("User-Agent", QDataUtil.Companion.getCustomUserAgent(MyApplication.getContext()));

            // 서버 Request Data 형식 (JSON 형식으로 서버에 전달)
            conn.setRequestProperty("Content-Type", "application/json");

            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            conn.setDoOutput(true);

            // InputStream으로 서버로 부터 응답을 받겠다는 옵션
            conn.setDoInput(true);

            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);

            // Request Data를 담기 위해 객체 생성
            os = conn.getOutputStream();
            // Request Data 셋팅
            os.write(object.toString().getBytes());
            // Request Data 입력
            os.flush();
            // 실제 서버로 Request 요청 (응답 코드 받음. 200 성공 / 나머지 에러)
            int responseCode = conn.getResponseCode();


            if (responseCode == HttpURLConnection.HTTP_OK) {

                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData;
                int nLength;
                while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();

                response = new String(byteData);

                Log.e("Server", method + "  Result : " + response);
            }
        } catch (Exception e) {

            response = e.toString();
            Log.e("Server", method + "   JsonParser Exception " + e.toString());
        }

        return response;
    }


    /**
     * 데이터 가공
     */
    // Main - Navigation - Not In Parcels
    public static ListNotInHousedResult getNotInHousedList(String jsonString) {

        ListNotInHousedResult result = new ListNotInHousedResult();

        try {

            JSONObject jsonObject = new JSONObject(jsonString);
            result.setResultCode(jsonObject.getInt("ResultCode"));
            result.setResultMsg(jsonObject.getString("ResultMsg"));


            JSONArray jsonArray = jsonObject.getJSONArray("ResultObject");
            List<ListNotInHousedResult.NotInhousedList> notInhousedLists = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject resultObject = jsonArray.getJSONObject(i);

                ListNotInHousedResult.NotInhousedList notInhousedListItem = new ListNotInHousedResult.NotInhousedList();
                notInhousedListItem.setInvoiceNo(resultObject.getString("invoice_no"));
                notInhousedListItem.setReqName(resultObject.getString("req_nm"));
                notInhousedListItem.setPartner_id(resultObject.getString("partner_id"));
                notInhousedListItem.setZipCode(resultObject.getString("zip_code"));
                notInhousedListItem.setAddress(resultObject.getString("address"));
                notInhousedListItem.setPickup_date(resultObject.getString("pickup_cmpl_dt"));
                notInhousedListItem.setReal_qty(resultObject.getString("real_qty"));
                notInhousedListItem.setNot_processed_qty(resultObject.getString("not_processed_qty"));


                JSONArray resultArray = resultObject.getJSONArray("qdriveOutstandingInhousedPickupLists");
                List<ListNotInHousedResult.NotInhousedList.NotInhousedSubList> notInhousedSubLists = new ArrayList<>();

                for (int j = 0; j < resultArray.length(); j++) {

                    JSONObject resultObject1 = resultArray.getJSONObject(j);

                    ListNotInHousedResult.NotInhousedList.NotInhousedSubList notInhousedSubListItem = new ListNotInHousedResult.NotInhousedList.NotInhousedSubList();
                    notInhousedSubListItem.setPackingNo(resultObject1.getString("packing_no"));
                    notInhousedSubListItem.setPurchasedAmount(resultObject1.getString("purchased_amt"));
                    notInhousedSubListItem.setPurchaseCurrency(resultObject1.getString("purchased_currency"));
                    notInhousedSubLists.add(notInhousedSubListItem);
                }

                notInhousedListItem.setSubLists(notInhousedSubLists);
                notInhousedLists.add(notInhousedListItem);
            }

            result.setResultObject(notInhousedLists);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getNotInHousedList Exception : " + e.toString());

            result.setResultCode(-15);
            result.setResultMsg(e.toString());
        }

        return result;
    }
}
