package com.meetingdoctors.cordova;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.meetingdoctors.chat.MeetingDoctorsClient;
import com.meetingdoctors.chat.data.webservices.CustomerSdkBuildMode;
import com.meetingdoctors.chat.data.Repository;
import com.meetingdoctors.mdsecure.sharedpref.OnResetDataListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVMeetingDoctors extends CordovaPlugin {
    public static final String ENV_DEV = "DEVELOPMENT";

    public static final String STYLE_PRIMARY_COLOR = "primaryColor";
    public static final String STYLE_SECONDARY_COLOR = "secondaryColor";
    public static final String STYLE_NAVIGATION_COLOR = "navigationColor";
    public static final String STYLE_SPECIALITY_COLOR = "specialityColor";

    public static final String METHOD_INIT = "initialize";
    public static final String METHOD_AUTHENTICATE = "authenticate";
    public static final String METHOD_LOGOUT = "logout";
    public static final String METHOD_SET_FCM_TOKEN = "setFcmToken";
    public static final String METHOD_FCM_MESSAGE = "onFcmMessage";
    public static final String METHOD_FCM_BACKGROUND_MESSAGE = "onFcmBackgroundMessage";
    public static final String METHOD_OPEN_ACTIVITY = "openList";
    public static final String METHOD_SET_STYLE = "setStyle";
    public static final String METHOD_SET_NAVIGATION = "setNavigationImage";
    private String navigationImageName;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case METHOD_INIT:
                String apiKey = args.getString(0);
                String env = args.getString(1);
                String encryptionPassword = args.getString(2);
                this.initialize(apiKey, env, encryptionPassword,callbackContext);
                return true;
            case METHOD_AUTHENTICATE:
                String userid = args.getString(0);
                this.authenticate(userid, callbackContext);
                return true;
            case METHOD_LOGOUT:
                this.logout(callbackContext);
                return true;
            case METHOD_SET_FCM_TOKEN:
                String token = args.getString(0);
                this.setFCMToken(token, callbackContext);
                return true;
            case METHOD_FCM_MESSAGE:
                String dataJson = args.getString(0);
                this.fcmMessage(dataJson, callbackContext);
                return true;
            case METHOD_FCM_BACKGROUND_MESSAGE:
                String dataBackJson = args.getString(0);
                this.fcmBackgroundMessage(dataBackJson, callbackContext);
                return true;
            case METHOD_OPEN_ACTIVITY:
                this.openMainActivity(callbackContext);
                return true;
            case METHOD_SET_STYLE:
                JSONObject style = args.getJSONObject(0);
                this.setStyle(style, callbackContext);
                return true;
            case METHOD_SET_NAVIGATION:
                String imageName = args.getString(0);
                this.setNavigationImage(imageName, callbackContext);
                return true;
        }

        return false;
    }

    private void initialize(String apikey, String env, String encryptionPassword, CallbackContext callbackContext) {
        if (apikey != null && apikey.length() > 0) {
            this.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    CustomerSdkBuildMode buildMode = CustomerSdkBuildMode.PROD;
                    if (env != null && env.equals(ENV_DEV)) {
                        buildMode = CustomerSdkBuildMode.DEV;
                    }
                    boolean isSharedPreferencesEncrypted = false;
        
		    if (isSharedPreferencesEncrypted !=null && !isSharedPreferencesEncrypted.isEmpty() ) {
		        isSharedPreferencesEncrypted = true;
		    } 
                    
                    MeetingDoctorsClient.newInstance(((Application) cordova.getContext().getApplicationContext()),
                            apikey,
                            buildMode,
                            isSharedPreferencesEncrypted,
                            encryptionPassword,
                            cordova.getContext().getResources().getConfiguration().locale);

                    callbackContext.success();
                }
            });
        } else {
            callbackContext.error("Expected one non-empty string apiKey on first argument.");
        }
    }

    private void authenticate(String userid, CallbackContext callbackContext) {
        if (userid != null && userid.length() > 0) {
            this.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
                    if (instance != null) {
                        instance.authenticate(
                                userid,
                                new MeetingDoctorsClient.AuthenticationListener() {
                                    @Override
                                    public void onAuthenticated() {
                                        callbackContext.success();
                                    }
                                    @Override
                                    public void onAuthenticationError(@NotNull Throwable throwable) {
                                        callbackContext.error("MeetingLawyers not initialized, call initialize first");
                                    }
                                });
                    } else {
                        callbackContext.error("MeetingDoctorsClient not initialized, call initialize first");
                    }
                }
            });
        } else {
            callbackContext.error("Expected one non-empty string userid on first argument.");
        }
    }

    private void logout(CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
                if (instance != null) {
                    instance.deauthenticate(new OnResetDataListener() {
                        @Override
                        public void dataResetSuccessFul() {
                            callbackContext.success();
                        }

                        @Override
                        public void dataResetError(Exception e) {
                            callbackContext.error("MeetingDoctors deauthenticate fails");
                        }
                    });
                } else {
                    callbackContext.error("MeetingDoctors not initialized, call initialize first");
                }
            }
        });
    }

    private void setFCMToken(String token, CallbackContext callbackContext) {
        if (token != null && token.length() > 0) {
            this.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
                    if (instance != null) {
                        instance.onNewTokenReceived(token);
                        callbackContext.success();
                    } else {
                        callbackContext.error("MeetingDoctors not initialized, call initialize first");
                    }
                }
            });
        } else {
            callbackContext.error("Expected one non-empty string token on first argument.");
        }
    }

    private void fcmMessage(String dataJson, CallbackContext callbackContext) {
        if (dataJson != null && dataJson.length() > 0) {
            this.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
                    if (instance != null) {
                        Boolean handled = instance.onFirebaseMessageReceived(dataJson, null);
                        callbackContext.success(handled.toString());
                    } else {
                        callbackContext.error("MeetingDoctors not initialized, call initialize first");
                    }
                }
            });
        } else {
            callbackContext.error("Expected one non-empty string token on first argument.");
        }
    }

    private void fcmBackgroundMessage(String dataJson, CallbackContext callbackContext) {
        if (dataJson != null && dataJson.length() > 0) {
            this.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
                    if (instance != null) {
                        instance.onNotificationDataReceived(dataJson);
                        callbackContext.success(Boolean.toString(true));
                    } else {
                        callbackContext.error("MeetingDoctors not initialized, call initialize first");
                    }
                }
            });
        } else {
            callbackContext.error("Expected one non-empty string token on first argument.");
        }
    }

    private void openMainActivity(CallbackContext callbackContext) {
        MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
        if (instance != null) {
            int navigationResourceId = this.cordova.getActivity().getApplication().getResources().getIdentifier(navigationImageName, "drawable", this.cordova.getActivity().getApplication().getPackageName());
            instance.launchProfessionalList(this.cordova.getContext(), navigationResourceId);
            callbackContext.success();
        } else {
            callbackContext.error("MeetingDoctors not initialized, call initialize first");
        }
    }

    private void setStyle(JSONObject style, CallbackContext callbackContext) throws JSONException {
        MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
        if (instance != null) {
            if (style.has(STYLE_PRIMARY_COLOR)) {
                String primaryColor = style.getString(STYLE_PRIMARY_COLOR);
                instance.setPrimaryColor(Color.parseColor(primaryColor));
                // Secondary color
                if (style.has(STYLE_SECONDARY_COLOR)) {
                    String secondaryColor = style.getString(STYLE_SECONDARY_COLOR);
                    instance.setSecondaryColor(Color.parseColor(secondaryColor));
                } else {
                    instance.setSecondaryColor(Color.parseColor(primaryColor));
                }
            }
            callbackContext.success();
        } else {
            callbackContext.error("MeetingDoctors not initialized, call initialize first");
        }
    }

    private void setNavigationImage(String imageName, CallbackContext callbackContext) {
        MeetingDoctorsClient instance = MeetingDoctorsClient.Companion.getInstance();
        if (instance != null) {
            if (imageName != null && imageName.length() > 0) {
                this.navigationImageName = imageName;
                callbackContext.success();
            } else {
                callbackContext.error("Expected one non-empty string token on first argument.");
            }
        } else {
            callbackContext.error("MeetingDoctors not initialized, call initialize first");
        }
    }
}
