import { AwesomeCordovaNativePlugin } from '@awesome-cordova-plugins/core';
/**
 * @name Eko Sdk Plugin
 * @description
 * This plugin does something
 *
 * @usage
 * ```typescript
 * import { EkoSdkPlugin } from '@awesome-cordova-plugins/eko-sdk-plugin';
 *
 *
 * constructor(private ekoSdkPlugin: EkoSdkPlugin) { }
 *
 * ...
 *
 *
 * this.ekoSdkPlugin.functionName('Hello', 123)
 *   .then((res: any) => console.log(res))
 *   .catch((error: any) => console.error(error));
 *
 * ```
 */
export declare class EkoSdkPluginOriginal extends AwesomeCordovaNativePlugin {
    /**
     * This function does something
     * @param arg1 {string} Some param to configure something
     * @param arg2 {number} Another param to configure something
     * @return {Promise<any>} Returns a promise that resolves when something happens
     */
    nativeToast(): Promise<any>;
    initilizeSdk(): void;
}

export declare const EkoSdkPlugin: EkoSdkPluginOriginal;