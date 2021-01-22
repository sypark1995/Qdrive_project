package com.giosis.library.list;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false, name = "StdCustomResultOfListOfScanPackingList")
public class PickupPackingListResult {

	@Element(name = "ResultCode", required = false)
	private int ResultCode = -1;

	@Element(name = "ResultMsg", required = false)
	private String ResultMsg = "";

	public int getResultCode() {
		return ResultCode;
	}

	public void setResultCode(int resultCode) {
		this.ResultCode = resultCode;
	}

	public String getResultMsg() {
		return ResultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.ResultMsg = resultMsg;
	}

	@ElementList(required = false, name = "ResultObject")
	private List<ScanPackingList> ResultObject;

	public List<ScanPackingList> getResultObject() {
		return ResultObject;
	}

	public void setResultObject(List<ScanPackingList> resultObj) {
		this.ResultObject = resultObj;
	}

	@Root(strict = false, name = "ScanPackingList")
	public static class ScanPackingList {

		@Element(name = "packing_no", required = false)
		private String packing_no = "";

		@Element(name = "pickup_no", required = false)
		private String pickup_no = "";

		@Element(name = "reg_dt", required = false)
		private String reg_dt = "";


		@Element(name = "op_id", required = false)
		private String op_id = "";


		public String getPackingNo() {
			return packing_no;
		}


		public String getPickupNo() {
			return pickup_no;
		}


		public String getRegDt() {
			return reg_dt;
		}


		public String getOpID() {
			return op_id;
		}
	}
}
