package com.giosis.library.server;

import android.util.Log;

import com.giosis.library.util.DataUtil;
import com.giosis.library.util.Preferences;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
        Log.e("krm0219", "JsonParser URL " + apiURL + method);


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
}