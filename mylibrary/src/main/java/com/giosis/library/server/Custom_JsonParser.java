package com.giosis.library.server;

import android.util.Log;

import com.giosis.library.message.MessageListResult;
import com.giosis.library.util.DataUtil;
import com.giosis.library.util.Preferences;

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
        String apiURL = Preferences.INSTANCE.getServerURL() + DataUtil.API_ADDRESS;
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
            conn.setRequestProperty("User-Agent", Preferences.INSTANCE.getUserAgent());

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


    // Main - Navigation - Message
    public static MessageListResult getAdminMessageList(String jsonString) {

        MessageListResult result = new MessageListResult();

        try {

            JSONObject jsonObject = new JSONObject(jsonString);
            result.setResultCode(jsonObject.getInt("ResultCode"));
            result.setResultMsg(jsonObject.getString("ResultMsg"));


            JSONArray jsonArray = jsonObject.getJSONArray("ResultObject");
            List<MessageListResult.MessageList> messageLists = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject resultObject = jsonArray.getJSONObject(i);

                MessageListResult.MessageList messageListItem = new MessageListResult.MessageList();
                messageListItem.setSender_id(resultObject.getString("sender_id"));
                messageListItem.setMessage(resultObject.getString("contents"));
                messageListItem.setTime(resultObject.getString("send_dt"));
                messageListItem.setRead_yn(resultObject.getString("read_yn"));
                messageListItem.setQuestion_seq_no(resultObject.getInt("question_seq_no"));
                messageLists.add(messageListItem);
            }

            result.setResultObject(messageLists);
        } catch (Exception e) {

            Log.e("Exception", TAG + "  getAdminMessageList Exception : " + e.toString());

            result.setResultCode(-15);
            result.setResultMsg(e.toString());
        }

        return result;
    }
}
