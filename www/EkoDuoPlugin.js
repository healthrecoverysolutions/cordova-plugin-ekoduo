/* global cordova, module */
"use strict";

var stringToArrayBuffer = function(str) {
    var ret = new Uint8Array(str.length);
    for (var i = 0; i < str.length; i++) {
        ret[i] = str.charCodeAt(i);
    }
    return ret.buffer;
};

var base64ToArrayBuffer = function(b64) {
    return stringToArrayBuffer(atob(b64));
};

function massageMessageNativeToJs(message) {
    if (message.CDVType == 'ArrayBuffer') {
        message = base64ToArrayBuffer(message.data);
    }
    return message;
}

function convertToNativeJS(object) {
    Object.keys(object).forEach(function (key) {
        var value = object[key];
        object[key] = massageMessageNativeToJs(value);
        if (typeof(value) === 'object') {
            convertToNativeJS(value);
        }
    });
}

module.exports = {
    nativeToast: function (arg0, success, error) {
        cordova.exec(success, error, 'EkoDuoPlugin', 'nativeToast', [arg0]);
    },
    
    initializeSdk: function(arg0, success, error) {
          cordova.exec(success, error, 'EkoDuoPlugin', 'initializeSdk', [arg0]);
    },
    
    startDeviceDiscovery: function (success, failure) {
//        var successWrapper = function(peripheral) {
//                 convertToNativeJS(peripheral);
//                 success(peripheral);
//             };
        cordova.exec(/*successWrapper*/ success, failure, 'EkoDuoPlugin', 'startDeviceDiscovery', []);
    },

    connect: function (success, failure) {
        cordova.exec( success, failure, 'EkoDuoPlugin', 'connect', []);
    }
    
};

module.exports.withPromises = {
    startDeviceDiscovery: module.exports.startDeviceDiscovery
};

