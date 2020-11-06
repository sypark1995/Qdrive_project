package com.giosis.util.qdrive.barcodescanner;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false, name="StdCustomResultOfListOfQdrivePickupCNR")
public class PickupCNRResult {
	
	public static ResultObject ResultObject;
	
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
		
//	@ElementList(required=false, name="ResultObject")
//	private List<QdriveCNRList> resultObject;
//
//	public List<QdriveCNRList> getResultObject() {
//		return resultObject;
//	}
//	
//	public void setResultObject(List<QdriveCNRList> resultObj) {
//		this.resultObject = resultObj;
//	}
	
	@Element(required=false, name="ResultObject")
	private ResultObject resultObject;

	public ResultObject getResultObject() {
		return resultObject;
	}
	
	public void setResultObject(ResultObject resultObj) {
		this.resultObject = resultObj;
	}
//	@Root(strict=false, name="QdriveCNRList")
//	public static class QdriveCNRList {
	@Root(strict=false, name="ResultObject")
	public static class ResultObject {
		@Element(name="contr_no", required=false)
		private String ContrNo = "";
		
		@Element(name="partner_ref_no", required=false)
		private String partnerRefNo = "";
		
		@Element(name="invoice_no", required=false)
		private String invoiceNo = "";

		@Element(name="stat", required=false)
		private String stat = "";

		@Element(name="req_nm", required=false)
		private String reqName = "";
		
		@Element(name="req_dt", required=false)
		private String reqDate = "";
		
//		@Element(name="del_driver_id", required=false)
//		private String del_driver_id = "";
		
		
		@Element(name="tel_no", required=false)
		private String telNo = "";		
		
		
		@Element(name="hp_no", required=false)
		private String hpNo = "";

		@Element(name="zip_code", required=false)
		private String zipCode = "";

		@Element(name="address", required=false)
		private String address = "";
		
		@Element(name="pickup_hopeday", required=false)
		private String pickupHopeDay = "";		
		
		@Element(name="pickup_hopetime", required=false)
		private String pickupHopeTime= "";	
		

		@Element(name="sender_nm", required=false)
		private String senderName = "";
		
		
		@Element(name="del_memo", required=false)
		private String delMemo = "";

		@Element(name="driver_memo", required=false)
		private String driverMemo = "";
		
		@Element(name="fail_reason", required=false)
		private String failReason = "";
		
	
		
		@Element(name="qty", required=false)
		private String qty = "";		
		
		@Element(name="cust_nm", required=false)
		private String custName = "";
		
		@Element(name="partner_id", required=false)
		private String partnerID = "";
		

		@Element(name="dr_assign_requestor", required=false)
		private String drAssignRequestor = "";
		
		@Element(name="dr_assign_req_dt", required=false)
		private String drAssignReqDate = "";		
		
		@Element(name="dr_assign_stat", required=false)
		private String drAssignStat = "";
		
		@Element(name="dr_req_no", required=false)
		private String dr_req_no = "";	
		
		
		@Element(name="failed_count", required=false)
		private String failedCount = "";	
		

		@Element(name="route", required=false)
		private String route = "";		
		
		@Element(name="cust_no", required=false)
		private String cust_no = "";
		
		
		public String getCustNo() {
			return cust_no;
		}
		
		
		public String getDrReqNo() {
			return dr_req_no;
		}
		
		public String getContrNo() {
			return ContrNo;
		}

		public String getPartnerRefNo() {
			return partnerRefNo;
		}

		public String getInvoiceNo() {
			return invoiceNo;
		}

		public String getStat() {
			return stat;
		}

		public String getReqName() {
			return reqName;
		}

		public String getTelNo() {
			return telNo;
		}

		public String getHpNo() {
			return hpNo;
		}

		public String getZipCode() {
			return zipCode;
		}

		public String getAddress() {
			return address;
		}

		public String getDelMemo() {
			return delMemo;
		}

		public String getDriverMemo() {
			return driverMemo;
		}

		public String getFailReason() {
			return failReason;
		}

		public String getQty(){
			return	qty;
		}
		
		public String getRoute(){
			return	route;
		}
		
		public String senderName(){
			return	senderName;
		}
		
		public String getCustName(){
			return	custName;
		}
		
		public String getFailedCount(){
			return	failedCount;
		}
		
		public String getDrAssignRequestor(){
			return drAssignRequestor;
		}
		
		public String getDrAssignStat(){
			return drAssignStat;
		}
		public String getReqDate(){
			return	reqDate;
		}
		
		public String getDrAssignReqDate(){
			return	drAssignReqDate;
		}
		public String getPartnerID(){
			return	partnerID;
		}
		public String getPickupHopeDay(){
			return	pickupHopeDay;
		}
		public String getPickupHopeTime(){
			return	pickupHopeTime;
		}
		
		public void setPartnerRefNo(String partnerRefNo) {
			this.partnerRefNo = partnerRefNo;
		}
		
	}

}
