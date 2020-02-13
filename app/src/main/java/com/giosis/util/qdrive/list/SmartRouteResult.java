package com.giosis.util.qdrive.list;

import java.util.ArrayList;

/**
 * @author krm0219
 */
public class SmartRouteResult {

    private String resultCode = "-1";
    private String resultMsg = "";
    private ArrayList<RouteMaster> routeMasterList;


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

    public ArrayList<RouteMaster> getRouteMasterList() {
        return routeMasterList;
    }

    public void setRouteMasterList(ArrayList<RouteMaster> routeMasterList) {
        this.routeMasterList = routeMasterList;
    }

    public static class RouteMaster {

        private String resultCode;
        private String resultMsg;

        private String routeID;
        private String routeName;
        private String routeRegDate;
        private String routeNo;         // Route Detail 조회시 넘기는 값.
        private String GoogleURL;


        private ArrayList<RouteMaster.RouteDetail> routeDetailArrayList = null;
        ArrayList<RowItem> routeDetailList = null;

        public RouteMaster() {
        }

        public RouteMaster(String routeName, String routeNo, String GoogleURL) {
            this.routeName = routeName;
            this.routeNo = routeNo;
            this.GoogleURL = GoogleURL;
        }

        public static class RouteDetail {

            private String routeNo;
            private String trackingNo;
            private String contrNo;
            private int sortNo;             // 정렬 값.


            public String getRouteNo() {
                return routeNo;
            }

            public void setRouteNo(String routeNo) {
                this.routeNo = routeNo;
            }

            public String getTrackingNo() {
                return trackingNo;
            }

            public void setTrackingNo(String trackingNo) {
                this.trackingNo = trackingNo;
            }

            public String getContrNo() {
                return contrNo;
            }

            public void setContrNo(String contrNo) {
                this.contrNo = contrNo;
            }

            public int getSortNo() {
                return sortNo;
            }

            public void setSortNo(int sortNo) {
                this.sortNo = sortNo;
            }
        }

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

        public String getRouteID() {
            return routeID;
        }

        public void setRouteID(String routeID) {
            this.routeID = routeID;
        }

        public String getRouteName() {
            return routeName;
        }

        public void setRouteName(String routeName) {
            this.routeName = routeName;
        }

        public String getRouteRegDate() {
            return routeRegDate;
        }

        public void setRouteRegDate(String routeRegDate) {
            this.routeRegDate = routeRegDate;
        }

        public String getRouteNo() {
            return routeNo;
        }

        public void setRouteNo(String routeNo) {
            this.routeNo = routeNo;
        }

        public String getGoogleURL() {
            return GoogleURL;
        }

        public void setGoogleURL(String googleURL) {
            GoogleURL = googleURL;
        }

        public ArrayList<RouteDetail> getRouteDetailArrayList() {
            return routeDetailArrayList;
        }

        public void setRouteDetailArrayList(ArrayList<RouteDetail> routeDetailArrayList) {
            this.routeDetailArrayList = routeDetailArrayList;
        }

        public ArrayList<RowItem> getRouteDetailList() {
            return routeDetailList;
        }

        public void setRouteDetailList(ArrayList<RowItem> routeDetailList) {
            this.routeDetailList = routeDetailList;
        }
    }
}
