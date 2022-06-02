//
//  EkoDuoPlugin.swift
//  
//
//  Created by mohit on 25/05/22.
//

import Foundation
import UIKit

@objc(EkoDuoPlugin) class EkoDuoPlugin : CDVPlugin{
    // MARK: Properties
    var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
    
    @objc(nativeToast:) func nativeToast(_ command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
        let msg = command.arguments[0] as? String ?? ""
        
        if msg.characters.count > 0 {
            let alert = UIAlertController(title: "My Title", message: "This is my message.", preferredStyle: UIAlertController.Style.alert)
              self.viewController?.present(alert, animated: true, completion: nil)
              DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                  alert.dismiss(animated: true, completion: nil)
              }
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK,messageAs: msg)
        }
//        let param1 = (command.arguments[0] as? NSObject)?.value(forKey: "param1") as? String
//        if param1 = "nativeToast"  {
//            nativeToast()
//            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: param1)
//        }
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
    
//    func nativeToast(){
//        let alert = UIAlertController(title: "My Title", message: "This is my message.", preferredStyle: UIAlertController.Style.alert)
//        alert.addAction(UIAlertAction(title: "OK", style: UIAlertAction.Style.default, handler: nil))
//        self.present(alert, animated: true, completion: nil)
//    }
}

