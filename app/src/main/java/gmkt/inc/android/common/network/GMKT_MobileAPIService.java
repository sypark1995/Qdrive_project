
package gmkt.inc.android.common.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import gmkt.inc.android.common.network.http.GMKT_HTTPRequestor;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;

/**
 * @author wontae
 * @date 2011. 07. 27
 */
public class GMKT_MobileAPIService {

    private GMKT_HTTPRequestor mHttpRequestor = null;

    public static String sUserAgentForAPI = "";
    public static HashMap<String, String> sAddHttpHeaderCustomFieldMap = null;

    public GMKT_MobileAPIService(URL targetURL, String userAgent, HashMap<String, String> addHttpHeaderFieldMap) {
        mHttpRequestor = new GMKT_HTTPRequestor(targetURL, userAgent, addHttpHeaderFieldMap);
    }

    /**
     * MobileAPIService에 XML Data 요청
     *
     * @param strUrlAddress MobileAPIService Target  URL Address
     * @param strMethodName MobileAPIService Method Name
     * @param hmActionParam Parameters	[HashMap<String, String>]
     * @return XML String
     */
    public static GMKT_HTTPResponseMessage requestReturnStringXML(String strUrlAddress, String strMethodName, HashMap<String, String> hmActionParam) throws Exception {

        if (strUrlAddress.contains("GlobalMobileService.qapi")) {
            if (!hmActionParam.containsKey("returnType")) {
                hmActionParam.put("returnType", "xml");
            }
        }

        long start = System.currentTimeMillis();
        Log.i("GMKT", "API requestDocument Time Check: " + strUrlAddress + strMethodName + " start: " + start);

        URL targetURL = string2URL(strUrlAddress, strMethodName);

        GMKT_HTTPResponseMessage responseMessage = null;

        GMKT_MobileAPIService mobileAPIService = new GMKT_MobileAPIService(targetURL, sUserAgentForAPI, sAddHttpHeaderCustomFieldMap);
        // Response된 데이터를 XML String으로  변환
        try {
            mobileAPIService.setServerRequestParam(hmActionParam);
            InputStream inputStream = mobileAPIService.sendPost();

            String xmlString = getStringXML(inputStream);
            inputStream.close();

            responseMessage = mobileAPIService.getmHttpRequestor().getHttpResponseMessage();
            responseMessage.setResultString(xmlString);
        } catch (Exception e) {
            responseMessage = mobileAPIService.getmHttpRequestor().getHttpResponseMessage();
            responseMessage.setResultString("");
        }

        long end = System.currentTimeMillis();
        Log.i("GMKT", "API requestDocument Time Check: " + strUrlAddress + strMethodName + " end: " + end);
        Log.i("GMKT", "API requestDocument Time Check: " + strUrlAddress + strMethodName + " end-start: " + (end - start));

        return responseMessage;
    }

    private static String getStringXML(InputStream inputStream) {
        StringBuffer sb = new StringBuffer();
        byte[] buffer = new byte[4096];

        String xmlString = null;

        try {
            for (int n; (n = inputStream.read(buffer)) != -1; ) {
                sb.append(new String(buffer, 0, n));
            }
            xmlString = sb.toString();
        } catch (IOException e) {
            return xmlString;
        }

        return xmlString;
    }


    /**
     * String으로 받은 MobileAPIService Target  URL Address와 MobileAPIService Method Name를
     * URL로 변환
     *
     * @param strUrlAddress MobileAPIService Target  URL Address
     * @param strMethodName MobileAPIService Method Name
     * @return URL                Target URL
     */
    public static URL string2URL(String strUrlAddress, String strMethodName) throws Exception {

        if (!strMethodName.equals("")) {
            // 주소 마지막에 '/'없을 경우 추가
            if (!strUrlAddress.endsWith("/")) {
                strUrlAddress += "/";
            }
        }
        String strTargetURL = strUrlAddress + strMethodName;

        URL targetURL = new URL(strTargetURL);

        Log.i("GMKT", "targetURL : " + strTargetURL);

        return targetURL;
    }


    /**
     * MobileAPIService에 전달할 Parameter 형식으로 변환
     *
     * @param hmActionParam Parameters	[HashMap<String, String>]
     */
    private void setServerRequestParam(HashMap<String, String> hmActionParam) {
        StringBuffer sbfServerParams = new StringBuffer();
        String strParam = "";
        int iParamCnt = 0;

        Iterator<String> iter = hmActionParam.keySet().iterator();

        String strKey = "";
        String strValue = "";

        while (iter.hasNext()) {
            strKey = iter.next();
            strValue = hmActionParam.get(strKey);

            if (strKey.equals("")) continue;        // key가 없으면 패스

            if (iParamCnt == 0) {
                strParam = strKey + "=" + strValue;
            } else {
                strParam = "&" + strKey + "=" + strValue;
            }

            mHttpRequestor.addParameter(strKey, strValue);
            sbfServerParams.append(strParam);
            iParamCnt++;
        }

        Log.i("GMKT", "Request Param : " + sbfServerParams.toString());
    }


    /**
     * 설정된 HttpRequestor를 POST방식으로 전송 요청
     *
     * @return Return InputStream (보통 XML)
     */
    private InputStream sendPost() throws Exception {
        InputStream inputStream = mHttpRequestor.sendPost();

        return inputStream;
    }


    public GMKT_HTTPRequestor getmHttpRequestor() {
        return mHttpRequestor;
    }

    public void setmHttpRequestor(GMKT_HTTPRequestor mHttpRequestor) {
        this.mHttpRequestor = mHttpRequestor;
    }
}
