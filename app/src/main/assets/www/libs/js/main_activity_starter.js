
cordova.define("com.giosis.util.qdrive.util.MainActivityStarter", function (require, exports, module) {	
    var exec = require("cordova/exec");
    var MainActivityStarter = function () {};
    
    
    MainActivityStarter.prototype.start = function() {
		exec(null, null, 'MainActivityStarter', 'start', []);
	}
    MainActivityStarter.prototype.goMarket = function() {
		exec(null, null, 'MainActivityStarter', 'goMarket', []);
	}
    MainActivityStarter.prototype.LoginStart = function() {
		exec(null, null, 'MainActivityStarter', 'LoginStart', []);
	}
    MainActivityStarter.prototype.verify = function() {
		exec(null, null, 'MainActivityStarter', 'verify', []);
	}
    MainActivityStarter.prototype.goHome = function() {
		exec(null, null, 'MainActivityStarter', 'goHome', []);
	}
    var plugin = new MainActivityStarter();
    module.exports = plugin;
});