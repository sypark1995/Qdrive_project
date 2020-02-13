package gmkt.inc.android.common.network.http;

public class GMKT_HTTPResponseMessage {
	private String requestUrl;
	private String httpMethodType;
	
	private int responseCode;
	private String responseDesc;
	
	private String resultString;
	
	public int getResponseCode() {
		return responseCode;
	}
	
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	
	public String getResponseDesc() {
		return responseDesc;
	}
	
	public void setResponseDesc(String responseDesc) {
		if (responseDesc == null) responseDesc = "";
		this.responseDesc = responseDesc;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		if (requestUrl == null) requestUrl = "";
		this.requestUrl = requestUrl;
	}

	public String getHttpMethodType() {
		return httpMethodType;
	}

	public void setHttpMethodType(String httpMethodType) {
		if (httpMethodType == null) httpMethodType = "";
		this.httpMethodType = httpMethodType;
	}
	
	public String getResultString() {
		return resultString;
	}

	public void setResultString(String resultString) {
		if (resultString == null) resultString = "";
		this.resultString = resultString;
	}
	
	@Override
	public String toString() {
		return String.format("** Http Request URL : %s, MethodType : %s \n** Http Response Code : %d, Message : %s \n Result String : %s \n", 
				requestUrl, httpMethodType, responseCode, responseDesc, resultString);
	}
}
