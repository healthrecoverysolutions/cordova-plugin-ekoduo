'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var tslib = require('tslib');
var core$1 = require('@angular/core');
var core = require('@awesome-cordova-plugins/core');

var EkoSdkPlugin = /** @class */ (function (_super) {
    tslib.__extends(EkoSdkPlugin, _super);
    function EkoSdkPlugin() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    EkoSdkPlugin.prototype.nativeToast = function () { return core.cordova(this, "nativeToast", {}, arguments); };
    EkoSdkPlugin.prototype.initilizeSdk = function () { return core.cordova(this, "initilizeSdk", {}, arguments); };
    EkoSdkPlugin.pluginName = "EkoSdkPlugin";
    EkoSdkPlugin.plugin = "com.hrs.ekosdkplugin";
    EkoSdkPlugin.pluginRef = "ekosdkplugin";
    EkoSdkPlugin.repo = "";
    EkoSdkPlugin.install = "";
    EkoSdkPlugin.installVariables = [];
    EkoSdkPlugin.platforms = ["Android"];
    EkoSdkPlugin.decorators = [
        { type: core$1.Injectable }
    ];
    return EkoSdkPlugin;
}(core.AwesomeCordovaNativePlugin));

exports.EkoSdkPlugin = EkoSdkPlugin;
