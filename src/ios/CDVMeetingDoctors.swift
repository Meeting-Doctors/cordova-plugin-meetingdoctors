// Copyright © 2023 MeetingDoctors S.L. All rights reserved.

import MeetingDoctorsSDK
import UIKit

@objc(CDVMeetingDoctors) class CDVMeetingDoctors : CDVPlugin {
    // Created as var and set in the initialize of the plugin because otherwise the constants do not work
    var ENV_DEV: String = ""
    var PRIMARY_COLOR: String = ""
    var SECONDARY_COLOR: String = ""
    var NAVIGATION_COLOR: String = ""
    var SPECIALITY_COLOR: String = ""
    
    
    override func pluginInitialize() {
        super.pluginInitialize()
        self.ENV_DEV = "DEVELOPMENT"
        self.PRIMARY_COLOR = "primaryColor"
        self.SECONDARY_COLOR = "secondaryColor"
        self.NAVIGATION_COLOR = "navigationColor"
        self.SPECIALITY_COLOR = "specialityColor"
    }
    
    @objc(initialize:)
    func initialize(_ command: CDVInvokedUrlCommand) {
        let apikey = command.arguments[0] as? String ?? ""
        let env = command.arguments[1] as? String ?? ""
        
        var environment: Environment = .production
        if env == self.ENV_DEV {
            environment = .development
        }
        
        if apikey.isEmpty {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error empty api_key"), callbackId: command.callbackId)
            return
        }
        
        self.commandDelegate.run {
            MeetingDoctors.configure(apiKey: apikey,
                                        environment: environment) { error in
                if let error = error {
                    self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription), callbackId: command.callbackId)
                    return
                }
                self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            }
        }
    }
    
    @objc(authenticate:)
    func authenticate(_ command: CDVInvokedUrlCommand) {
        let token = command.arguments[0] as? String ?? ""
        
        if userid.isEmpty {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error empty userid"), callbackId: command.callbackId)
            return
        }
        
        self.commandDelegate.run {
            MeetingDoctors.authenticate(token: token) { error in
                if let error = error {
                    self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription), callbackId: command.callbackId)
                    return
                }
                self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            }
        }
    }
    
    @objc(logout:)
    func logout(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run {
            MeetingDoctors.logout { error in
                if let error = error {
                    self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription), callbackId: command.callbackId)
                    return
                }
                self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            }
        }
    }
    
    // MARK: - FCM

    @objc(setFcmToken:)
    func setFcmToken(_ command: CDVInvokedUrlCommand) {
        let token = command.arguments[0] as? String ?? ""
        
        if token.isEmpty {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error empty token"), callbackId: command.callbackId)
            return
        }
        
        self.commandDelegate.run {
            MeetingDoctors.registerFirebaseForNotifications(token: token) { error in
                if let error = error {
                    self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription), callbackId: command.callbackId)
                    return
                }
                self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            }
        }
    }
    
    @objc(onFcmMessage:)
    func onFcmMessage(_ command: CDVInvokedUrlCommand) {
        let data = command.arguments[0] as? String ?? ""
        
        if data.isEmpty {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error empty data"), callbackId: command.callbackId)
            return
        }
        
        let notificationRequest = self.createNotificationRequest(data)

        let handled = MeetingDoctors.userNotificationCenter(willPresent: notificationRequest) { options in
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: true), callbackId: command.callbackId)
        }

        if !handled {
            // No error, push notification not from ML
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: false), callbackId: command.callbackId)
            return
        }
    }
    
    @objc(onFcmBackgroundMessage:)
    func onFcmBackgroundMessage(_ command: CDVInvokedUrlCommand) {
        let data = command.arguments[0] as? String ?? ""
        
        if data.isEmpty {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Error empty data"), callbackId: command.callbackId)
            return
        }
        
        let notificationRequest = self.createNotificationRequest(data)

        let handled = MeetingDoctors.userNotificationCenter(didReceive: notificationRequest) { error in
            if let error = error {
                self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription), callbackId: command.callbackId)
            } else {
                self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: true), callbackId: command.callbackId)
            }
        }

        if !handled {
            // No error, push notification not from ML
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: false), callbackId: command.callbackId)
            return
        }
    }
    
    private func createNotificationRequest(_ data: String) -> UNNotificationRequest {
        let content = UNMutableNotificationContent()
        content.userInfo = ["data" : data]
        
        return UNNotificationRequest(identifier: "com.meetingdoctors.cordova",
                                           content: content,
                                           trigger: nil)
    }
    
    
    // END
    
    @objc(openList:)
    func openList(_ command: CDVInvokedUrlCommand) {
        self.setBackButton()
        if let professionalList = MeetingDoctors.professionalListViewController() {
            professionalList.modalPresentationStyle = .fullScreen
            self.viewController?.present(
                professionalList,
                animated: true,
                completion: nil)
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
            return
        }
        
        self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR), callbackId: command.callbackId)
    }
    
    private func setBackButton() {
        if var style = MLMediQuo.style {
            if let image = UIImage(systemName: "chevron.backward") {
                style.rootLeftBarButtonItem = UIBarButtonItem(image: image, style: .plain, target: self, action: #selector(self.dismiss))
            } else {
                style.rootLeftBarButtonItem = UIBarButtonItem(barButtonSystemItem: .close, target: self, action: #selector(self.dismiss))
            }
            MLMediQuo.style = style
        }
    }
    
    @objc
    func dismiss() {
        self.viewController?.dismiss(animated: true)
    }
    
    // MARK: - STYLE
    
    @objc(setStyle:)
    func setStyle(_ command: CDVInvokedUrlCommand) {
        if let style = command.arguments[0] as? NSDictionary,
           let primaryColorString = style[self.PRIMARY_COLOR] as? String {
            
            let primaryColor = UIColor(hex: primaryColorString)
            MeetingDoctors.setStyle(primaryColor: primaryColor)
            
            // Override secondary color
            if let secondaryColorString = style[self.SECONDARY_COLOR] as? String {
                let secondaryColor = UIColor(hex: secondaryColorString)
                MeetingDoctors.setStyle(secondaryColor: secondaryColor)
            }
            // Override navigation color
            if let navigationColorString = style[self.NAVIGATION_COLOR] as? String {
                let navigationColor = UIColor(hex: navigationColorString)
                MeetingDoctors.setStyle(navigationColor: navigationColor)
            }
            // Override speciality color
            if let specialityColorString = style[self.SPECIALITY_COLOR] as? String {
                let specialityColor = UIColor(hex: specialityColorString)
                MeetingDoctors.setStyle(specialityColor: specialityColor)
            }
            
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
        } else {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR), callbackId: command.callbackId)
        }
    }

    @objc(setNavigationImage:)
    func setNavigationImage(_ command: CDVInvokedUrlCommand) {
        let imageName = command.arguments[0] as? String ?? ""
        if let image = UIImage(named: imageName),
            image.size.height != 0 {

            MeetingDoctors.setStyle(navigationImage: image)

            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_OK), callbackId: command.callbackId)
        } else {
            self.commandDelegate!.send(CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid image path: \(imageName)"), callbackId: command.callbackId)
        }
    }
    
    // END
}
