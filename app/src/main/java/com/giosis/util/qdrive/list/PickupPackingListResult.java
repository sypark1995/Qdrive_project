package com.giosis.util.qdrive.list;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict=false, name="StdCustomResultOfListOfScanPackingList")
public class PickupPackingListResult {
	
	@Element(name="ResultCode", required=false)
	private int resultCode = -1;
	
	@Element(name="ResultMsg", required=false)
	private String resultMsg = "";

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	
	public String getResultMsg() {
		return resultMsg;
	}
	
	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}
		
	@ElementList(required=false, name="ResultObject")
	private List<ScanPackingList> resultObject;

	public List<ScanPackingList> getResultObject() {
		return resultObject;
	}
	
	public void setResultObject(List<ScanPackingList> resultObj) {
		this.resultObject = resultObj;
	}
	
	@Root(strict=false, name="ScanPackingList")
	public static class ScanPackingList {
		
		@Element(name="packing_no", required=false)
		private String packingNo = "";
		
		@Element(name="pickup_no", required=false)
		private String pickupNo = "";
		
		@Element(name="reg_dt", required=false)
		private String regDt = "";

	
		@Element(name="op_id", required=false)
		private String opID = "";


		public String getPackingNo() {
			return packingNo;
		}


		public String getPickupNo() {
			return pickupNo;
		}


		public String getRegDt() {
			return regDt;
		}


		public String getOpID() {
			return opID;
		}		
		
		
		
		

	}

}
