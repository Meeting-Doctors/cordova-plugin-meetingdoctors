var exec = require("cordova/exec");

exports.initialize = function (apikey, env, success, error) {
  exec(success, error, "CDVMeetingDoctors", "initialize", [apikey, env]);
};

exports.authenticate = function (userid, success, error) {
  exec(success, error, "CDVMeetingDoctors", "authenticate", [userid]);
};

exports.logout = function (success, error) {
  exec(success, error, "CDVMeetingDoctors", "logout", []);
};

exports.setFcmToken = function (userid, success, error) {
  exec(success, error, "CDVMeetingDoctors", "setFcmToken", [userid]);
};

exports.onFcmMessage = function (data, success, error) {
  exec(success, error, "CDVMeetingDoctors", "onFcmMessage", [data]);
};

exports.onFcmBackgroundMessage = function (data, success, error) {
  exec(success, error, "CDVMeetingDoctors", "onFcmBackgroundMessage", [data]);
};

exports.openList = function (success, error) {
  exec(success, error, "CDVMeetingDoctors", "openList", []);
};

exports.setStyle = function(style) {
  exec(function() {}, function(error) {}, "CDVMeetingDoctors", "setStyle", [style]);
}

exports.setNavigationImage = function(imageName) {
  exec(function() {}, function(error) {}, "CDVMeetingDoctors", "setNavigationImage", [imageName]);
}

