/* global cordova, module */
"use strict";

var stringToArrayBuffer = function(str) {
    var ret = new UInt8Array(str.length);
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
        var successWrapper = function(peripheral) {
            convertToNativeJS(peripheral);
            success(peripheral);
        };
        cordova.exec(successWrapper, failure, 'EkoDuoPlugin', 'startDeviceDiscovery', []);
    },

    startDeviceDiscoveryForDuration: function (seconds, success, failure) {
        var successWrapper = function(peripheral) {
            convertToNativeJS(peripheral);
            success(peripheral);
        };
        cordova.exec(successWrapper, failure, 'EkoDuoPlugin', 'startDeviceDiscoveryForDuration', [seconds]);
    },

    connect: function (arg0, success, failure) {
        cordova.exec(success, failure, 'EkoDuoPlugin', 'connect', [arg0]);
    },

    isConnected: function (arg0, success, failure) {
        cordova.exec(success, failure, 'EkoDuoPlugin', 'isConnected', [arg0]);
    },

    startStreaming: function(success, failure) {
        cordova.exec(success, failure, 'EkoDuoPlugin', 'startStreaming', []);
    },

     startRecording: function (seconds, success, failure) {
            cordova.exec(success, failure, 'EkoDuoPlugin', 'startRecording', [seconds]);
     },


};

module.exports.withPromises = {
    startDeviceDiscovery: module.exports.startDeviceDiscovery,
    startDeviceDiscoveryForDuration: module.exports.startDeviceDiscoveryForDuration,
    connect: module.exports.connect,
    isConnected: module.exports.isConnected,
    startStreaming: module.exports.startStreaming,
    startRecording: module.exports.startRecording,
};

