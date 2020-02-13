package gmkt.inc.android.common;

import android.util.Log;

import java.util.HashMap;

import gmkt.inc.android.common.network.GMKT_MobileAPIService;
import gmkt.inc.android.common.network.GMKT_MobileAPIServiceRequestData;
import gmkt.inc.android.common.network.http.GMKT_HTTPResponseMessage;


public class GMKT_SyncHttpTask {

    private static int XML_PARSER_TYPE_RAW_STRING = 7;

    public GMKT_SyncHttpTask(String userAgent) {

        GMKT_MobileAPIService.sUserAgentForAPI = userAgent;
    }

    /**
     * AsyncTask 실행 - String XML
     *
     * @param strUrlAddress MobileAPIService 요청을 위한 Target URL 주소
     * @param strMethodName MobileAPIService 요청을 위한 Method 명
     * @param hmActionParam MobileAPIService 요청을 위한 Params 객체
     */
    public GMKT_HTTPResponseMessage requestServerDataReturnString(String strUrlAddress, String strMethodName, HashMap<String, String> hmActionParam) {

        // Async Task에 전달할 Request Data 객체 생성
        GMKT_MobileAPIServiceRequestData requestData = new GMKT_MobileAPIServiceRequestData();
        requestData.setTargetURL(strUrlAddress);
        requestData.setMethodName(strMethodName);
        requestData.setParams(hmActionParam);
        requestData.setXmlParserType(XML_PARSER_TYPE_RAW_STRING);

        return doSyncRequest(requestData);
    }

    private GMKT_HTTPResponseMessage doSyncRequest(GMKT_MobileAPIServiceRequestData params) {

        // GMKT MobileAPIService에 XML Data 요청
        GMKT_MobileAPIServiceRequestData requestData = params;
        String strURL = requestData.getTargetURL();
        String strMethodName = requestData.getMethodName();
        HashMap<String, String> hmActionParam = requestData.getParams();

        GMKT_HTTPResponseMessage responseMessage = null;

        try {

            if (requestData.getXmlParserType() == XML_PARSER_TYPE_RAW_STRING) {
                responseMessage = requestWithStringXML(strURL, strMethodName, hmActionParam);
            }
        } catch (Exception e) {

            Log.e("Exception", "Exception : " + e.toString());
        }

        return responseMessage;
    }


    /**
     * Sever Request With GMKT_HTTPResponseMessage
     *
     * @param strURL
     * @param strMethodName
     * @param hmActionParam
     * @return GMKT_HTTPResponseMessage
     */
    private GMKT_HTTPResponseMessage requestWithStringXML(String strURL, String strMethodName, HashMap<String, String> hmActionParam) throws Exception {

        GMKT_HTTPResponseMessage responseMessage = GMKT_MobileAPIService.requestReturnStringXML(strURL, strMethodName, hmActionParam);

        return responseMessage;
    }
}