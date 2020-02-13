package com.giosis.util.qdrive.singapore;

/**
 * @author krm0219
 */
public class LoginResult {

    private String resultCode = "-1";
    private String resultMsg = "";
    private LoginData resultObject;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public LoginData getResultObject() {
        return resultObject;
    }

    public void setResultObject(LoginData resultObject) {
        this.resultObject = resultObject;
    }

    public static class LoginData {

        String AuthNo;
        String DefaultYn;
        String DeviceYn;
        String EpEmail;
        String GroupNo;
        String locker_driver_status;
        String OfficeCode;
        String OfficeName;
        String OpId;
        String OpNm;
        String OpType;
        String PickupDriverYN;
        String shuttle_driver_yn;
        String SmsYn;
        String Version;

        String Password;


        public String getAuthNo() {
            return AuthNo;
        }

        public void setAuthNo(String authNo) {
            AuthNo = authNo;
        }

        public String getDefaultYn() {
            return DefaultYn;
        }

        public void setDefaultYn(String defaultYn) {
            DefaultYn = defaultYn;
        }

        public String getDeviceYn() {
            return DeviceYn;
        }

        public void setDeviceYn(String deviceYn) {
            DeviceYn = deviceYn;
        }

        public String getEpEmail() {
            return EpEmail;
        }

        public void setEpEmail(String epEmail) {
            EpEmail = epEmail;
        }

        public String getGroupNo() {
            return GroupNo;
        }

        public void setGroupNo(String groupNo) {
            GroupNo = groupNo;
        }

        public String getLocker_driver_status() {
            return locker_driver_status;
        }

        public void setLocker_driver_status(String locker_driver_status) {
            this.locker_driver_status = locker_driver_status;
        }

        public String getOfficeCode() {
            return OfficeCode;
        }

        public void setOfficeCode(String officeCode) {
            OfficeCode = officeCode;
        }

        public String getOfficeName() {
            return OfficeName;
        }

        public void setOfficeName(String officeName) {
            OfficeName = officeName;
        }

        public String getOpId() {
            return OpId;
        }

        public void setOpId(String opId) {
            OpId = opId;
        }

        public String getOpNm() {
            return OpNm;
        }

        public void setOpNm(String opNm) {
            OpNm = opNm;
        }

        public String getOpType() {
            return OpType;
        }

        public void setOpType(String opType) {
            OpType = opType;
        }

        public String getPickupDriverYN() {
            return PickupDriverYN;
        }

        public void setPickupDriverYN(String pickupDriverYN) {
            PickupDriverYN = pickupDriverYN;
        }

        public String getShuttle_driver_yn() {
            return shuttle_driver_yn;
        }

        public void setShuttle_driver_yn(String shuttle_driver_yn) {
            this.shuttle_driver_yn = shuttle_driver_yn;
        }

        public String getSmsYn() {
            return SmsYn;
        }

        public void setSmsYn(String smsYn) {
            SmsYn = smsYn;
        }

        public String getVersion() {
            return Version;
        }

        public void setVersion(String version) {
            Version = version;
        }

        public String getPassword() {
            return Password;
        }

        public void setPassword(String password) {
            Password = password;
        }
    }
}
