document.write('<script type="text/javascript" src="../libs/js/enviroment.js"><\/script>');

var result_GetDeliveryList;
var result_GetPickupList;
var networkState;
var currentAction;
var results_PickupList;
var results_deliveryList;
var results_scan_deliveryList;
var scanType;
var scanTitle;
var scanList;
var arryList = '';
var targetCnt = 0;
var successCnt = 0;
var failCnt = 0;
var noImageCnt = 0;
var noImageInvoiceNo = '';
var failList = '';
var processType = '';
var notChangedList_D = '';
var notChangedList_P = '';
var reAssignList_D = '';
var partner_ref_no_fail_assign = '';
var reason_fail_assign = '';
var scan_sheet_upload_gubun = 'normal';
var pickup_binary_files;
var openApiKey = '';
var dr_req_list;

var cntTimer;
var serverError = null;
var apiStat = new Object(); // key/value - P123, PX Qoo10 상태연동 서비스 호출 시 필요


function setButton(elementId, eventObj, eventParams) {

	var elementObj_1 = $("#" + elementId);
	var elementObj_2 = $("#" + elementId + " span");
	var elementObj_3 = $("#" + elementId + " strong");
	var elementObj_4 = $("#" + elementId + " .r_arrow");

	elementObj_1.bind('click', function() {
		elementObj_1.addClass('active');
		elementObj_2.addClass('active');
		elementObj_3.addClass('active');
		elementObj_4.addClass('active');

		var timer = setInterval(function() {
			elementObj_1.removeClass('active');
			elementObj_2.removeClass('active');
			elementObj_3.removeClass('active');
			elementObj_4.removeClass('active');

			eventObj(eventParams);

			clearInterval(timer);
		}, 200);
	});
}


function exceptionClose() {
	cordova.require("com.giosis.util.qdrive.util.LoadingDialog").hide();
	$("#qlps_alert_processing").hide();
	$("#qlps_alert_complete").hide();

	$("a").removeClass("ui-disabled");
	$("div.btn_area").removeClass("ui-disabled");

}


function onBackKeyDown() {
	// Handle the back button
}