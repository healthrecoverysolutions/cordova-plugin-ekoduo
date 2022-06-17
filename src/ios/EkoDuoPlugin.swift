//
//  EkoDuoPlugin.swift
//
//
//

import Foundation
import UIKit
import EkoConnectSDK

@objc(EkoDuoPlugin) class EkoDuoPlugin : CDVPlugin{
    
    // MARK: Properties
    
    var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
    var baseURL: String?
    var apiKey: String?
    var secretKey: String?
    var sdk: EkoConnect?
    
    @objc(nativeToast:) func nativeToast(_ command: CDVInvokedUrlCommand) {
        
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
        let msg = command.arguments[0] as? String ?? ""
        
        baseURL = "https://dashboard.ekodevices.com"
        apiKey = "af951a54474122251b23ff27db4d71e80e5092b7a0e854cd085bb1b2273c617f"
        secretKey = "01d6508f6abe9991a2c77f68ff87868659ec4e06a4c16f7c81c1459f298f3367"
        sdk = EkoConnect.sharedInstance()
        
        // Just created for developer testing for plugin connection. Can be removed later.
        
//        if msg.count > 0 {
//            let alert = UIAlertController(title: "My Title", message: "This is my message.", preferredStyle: UIAlertController.Style.alert)
//            self.viewController?.present(alert, animated: true, completion: nil)
//            DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
//                alert.dismiss(animated: true, completion: nil)
//            }
//        }
        
        self.initializeSdk()
        
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK,messageAs: msg)
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        
    }
    
    func initializeSdk(){
        
        self.sdk!.disconnectIfNeeded()
        ECLogger.debug("Initialiser for EKO SDK called")
        
        self.sdk!.initEkoConnect(with: URL(string: self.baseURL!)!, APIid: self.apiKey!, andSecret: self.secretKey!, installationId: "pcm_id", onCompletion: {[weak self] results in
            switch results {
            case .noError:
                print("Eko authentication successful")
            case .authError:
                print("Eko SDK initialisation Authorization Error")
            case .hostUnreachable:
                print("Cannot Reach Eko Connect Services. Check your Network Connection")
            default:
                break
            }
        })
    }
}





