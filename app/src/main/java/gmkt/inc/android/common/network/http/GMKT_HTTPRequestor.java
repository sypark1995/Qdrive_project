package gmkt.inc.android.common.network.http;

import android.util.Log;

import com.giosis.util.qdrive.singapore.MyApplication;
import com.giosis.util.qdrive.util.QDataUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @author wontae
 * @version 1.0.0
 * @breif HTTP/HTTPS 요청 Class
 * @date 2011. 07. 27
 */

public class GMKT_HTTPRequestor {

    public static final String CRLF = "\r\n";

    public String mUserAgentForOpenAPI = "";

    private HashMap<String, String> addHttpHeaderCustomFieldMap;
    private GMKT_HTTPResponseMessage httpResponseMessage;

    /**
     * 연결할 URL
     */
    private URL targetURL;

    /**
     * 파라미터 목록을 저장하고 있다.
     * 파라미터 이름과 값이 차례대로 저장된다.
     */
    private ArrayList<Object> list;

    public GMKT_HTTPRequestor(URL target, String userAgent, HashMap<String, String> addHttpHeaderCustomFieldMap) {
        this(target, 20, userAgent, addHttpHeaderCustomFieldMap);
    }

    /**
     * HttpRequest를 생성한다.
     *
     * @param target HTTP 메시지를 전송할 대상 URL
     */
    public GMKT_HTTPRequestor(URL target, int initialCapicity, String userAgent, HashMap<String, String> addHttpHeaderCustomFieldMap) {
        this.targetURL = target;
        this.list = new ArrayList<Object>(initialCapicity);
        System.setProperty("http.keepAlive", "false");

        mUserAgentForOpenAPI = userAgent;
        this.addHttpHeaderCustomFieldMap = addHttpHeaderCustomFieldMap;

        setHttpResponseMessage(new GMKT_HTTPResponseMessage());
    }


    /**
     * 파라미터를 추가한다.
     *
     * @param parameterName  파라미터 이름
     * @param parameterValue 파라미터 값
     * @throws IllegalArgumentException parameterValue가 null일 경우
     */
    public void addParameter(String parameterName, String parameterValue) {
        if (parameterValue == null)
            throw new IllegalArgumentException("parameterValue can't be null!");

        list.add(parameterName);
        list.add(parameterValue);
    }

    /**
     * 파일 파라미터를 추가한다.
     * 만약 parameterValue가 null이면(즉, 전송할 파일을 지정하지 않는다면
     * 서버에 전송되는 filename 은 "" 이 된다.
     *
     * @param parameterName  파라미터 이름
     * @param parameterValue 전송할 파일
     * @throws IllegalArgumentException parameterValue가 null일 경우
     */
    public void addFile(String parameterName, File parameterValue) {
        // paramterValue가 null일 경우 NullFile을 삽입한다.
        if (parameterValue == null) {
            list.add(parameterName);
            list.add(new NullFile());
        } else {
            list.add(parameterName);
            list.add(parameterValue);
        }
    }


    private class NullFile {
        NullFile() {
        }

        public String toString() {
            return "";
        }
    }


    /**
     * String을 URL Encoding 처리
     *
     * @param parameters URL Encoding 처리할 Parameter들
     * @return
     */
    private static String encodeString(ArrayList<Object> parameters) {
        StringBuffer sb = new StringBuffer(256);

        Object[] obj = new Object[parameters.size()];
        parameters.toArray(obj);

        for (int i = 0; i < obj.length; i += 2) {
            if (obj[i + 1] instanceof File || obj[i + 1] instanceof NullFile) continue;
            sb.append(URLEncoder.encode((String) obj[i]));
            sb.append('=');
            sb.append(URLEncoder.encode((String) obj[i + 1]));
            if (i + 2 < obj.length) sb.append('&');
        }

        return sb.toString();
    }

    private InputStream getResponseInputStream(URLConnection conn) throws IOException {
        String contentEncodingType = conn.getHeaderField("Content-Encoding");

        setResponseMessage(conn);

        InputStream returnInputStream = conn.getInputStream();

        if (contentEncodingType != null && contentEncodingType.equalsIgnoreCase("gzip")) {
            returnInputStream = new GZIPInputStream(returnInputStream);
        }

        return returnInputStream;
    }

    private void setResponseMessage(URLConnection conn) throws IOException {
        int responseCode = -999;
        String responseMessage = "";
        String httpMethodType = "";
        String requestUrl = "";
        String protocol = conn.getURL().getProtocol().toLowerCase();

        String paramString = "";
        if (list.size() > 0)
            paramString = "?" + encodeString(list);
        else
            paramString = "";

        if (protocol.equals("https")) {
            HttpsURLConnection https = (HttpsURLConnection) conn;
            responseCode = https.getResponseCode();
            responseMessage = https.getResponseMessage();
            requestUrl = https.getURL().toString();
            httpMethodType = https.getRequestMethod();
        } else if (protocol.equals("http")) {
            HttpURLConnection http = (HttpURLConnection) conn;
            responseCode = http.getResponseCode();
            responseMessage = http.getResponseMessage();
            requestUrl = http.getURL().toString();
            httpMethodType = http.getRequestMethod();
        }

        httpResponseMessage.setRequestUrl(requestUrl + paramString);
        httpResponseMessage.setHttpMethodType(httpMethodType);
        httpResponseMessage.setResponseCode(responseCode);
        httpResponseMessage.setResponseDesc(responseMessage);
    }

    /**
     * POST 방식으로 대상 URL에 파라미터를 전송한 후
     * 응답을 InputStream으로 리턴한다.
     *
     * @return InputStream
     */
    public InputStream sendPost() throws IOException {
        System.setProperty("http.keepAlive", "false");

        String paramString = null;
        if (list.size() > 0)
            paramString = encodeString(list);
        else
            paramString = "";

        if (targetURL.getProtocol().toLowerCase().equals("https")) {
            trustAllHosts();
            HttpsURLConnection https = (HttpsURLConnection) targetURL.openConnection();
            https.setHostnameVerifier(DO_NOT_VERIFY);

            https.setRequestMethod("POST");
            https.setConnectTimeout(480000);    // 4분
            https.setReadTimeout(480000);
            https.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            https.addRequestProperty("Accept-Encoding", "gzip");
            https.addRequestProperty("User-Agent", QDataUtil.Companion.getCustomUserAgent(MyApplication.getContext()));


            addHttpHeaderFields(https, addHttpHeaderCustomFieldMap);
            https.setDoInput(true);
            https.setDoOutput(true);
            https.setUseCaches(false);
            https.setDefaultUseCaches(false);

            DataOutputStream out = null;
            try {
                out = new DataOutputStream(https.getOutputStream());
                out.writeBytes(paramString);
                out.flush();
            } finally {
                if (out != null) out.close();
            }

            int iResponse = https.getResponseCode();

            Log.i("GMKT", "HTTP CODE : " + iResponse + " Message : " + https.getResponseMessage());

            return getResponseInputStream(https);
        } else {
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) targetURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(480000);
            conn.setReadTimeout(480000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.addRequestProperty("Accept-Encoding", "gzip");
            conn.addRequestProperty("User-Agent", QDataUtil.Companion.getCustomUserAgent(MyApplication.getContext()));
//            if (mUserAgentForOpenAPI != null && mUserAgentForOpenAPI.length() > 0) {
//                conn.addRequestProperty("User-Agent", mUserAgentForOpenAPI);
//            }
            addHttpHeaderFields(conn, addHttpHeaderCustomFieldMap);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            DataOutputStream out = null;
            try {
                out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes(paramString);
                out.flush();
            } finally {
                if (out != null) out.close();
            }

            int iResponse = conn.getResponseCode();

            Log.i("GMKT", "HTTP CODE : " + iResponse + " Message : " + conn.getResponseMessage());

            return getResponseInputStream(conn);
        }

        // TODO : HTTP Response Code에 따라 Excption처리
    }

    /**
     * 커스텀 헤더 필드에 값을 추가 한다.
     *
     * @param conn
     * @param addFieldMap
     */
    private void addHttpHeaderFields(URLConnection conn, HashMap<String, String> addFieldMap) {
        if (conn != null && addFieldMap != null) {
            Iterator<String> iter = addFieldMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = addFieldMap.get(key);

                conn.addRequestProperty(key, value);
            }
        }
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }
    };

    public GMKT_HTTPResponseMessage getHttpResponseMessage() {
        return httpResponseMessage;
    }

    public void setHttpResponseMessage(GMKT_HTTPResponseMessage httpResponseMessage) {
        this.httpResponseMessage = httpResponseMessage;
    }


}
