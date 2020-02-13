/*
var LoadingDialog = function() {};

LoadingDialog.prototype.show = function() {
	cordova.exec(null, null, 'LoadingDialog', 'show', []);
};

LoadingDialog.prototype.hide = function() {
	cordova.exec(null, null, 'LoadingDialog', 'hide', []);
};

cordova.addConstructor(function () {
	cordova.addPlugin('loadingDialog', new LoadingDialog());
});
*/
cordova.define("com.giosis.util.qdrive.util.LoadingDialog", function (require, exports, module) {	
    var exec = require("cordova/exec");
    var LoadingDialog = function () {};
	
    LoadingDialog.prototype.show = function() {
        exec(null, null, 'LoadingDialog', 'show', []);
    }
	LoadingDialog.prototype.hide = function() {
		exec(null, null, 'LoadingDialog', 'hide', []);
	}
    var plugin = new LoadingDialog();
    module.exports = plugin;
});