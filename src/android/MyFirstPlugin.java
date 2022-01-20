package com.joangape.myfirstplugin;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ekodevices.ekoconnect.EkoConnect;
import com.ekodevices.ekoconnect.data.entities.device.ECDevice;
import com.ekodevices.ekoconnect.data.entities.device.ECScanResult;
import com.ekodevices.ekoconnect.eventlisteners.external.ECDeviceConnectListener;
import com.ekodevices.ekoconnect.eventlisteners.external.ECDeviceScanListener;
import com.ekodevices.ekoconnect.eventlisteners.external.ECStreamListener;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kotlin.Unit;


/**
 * This class echoes a string called from JavaScript.
 */
public class MyFirstPlugin extends CordovaPlugin implements  ECDeviceScanListener, ECDeviceConnectListener, ECStreamListener {

    public static EkoConnect instance;
    public static ECDeviceScanListener ecDeviceScanListener;
    public static ECDeviceConnectListener ecDeviceConnectListener;
    @Override
    public boolean execute(String action, JSONArray     args, CallbackContext callbackContext) throws JSONException {
        Log.d("AB", "Execute with action " + action);
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if(action.equals("nativeToast")){
            Log.d("AB", " Native Call to show TOAST -- ");
            nativeToast();
        } else if(action.equals("initializeSdk")) {
            Log.d("AB", "Initialize SDK -- ");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    initializeSdk();
                }
            });
           // initializeSdk();
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    public void nativeToast(){
        Toast.makeText(webView.getContext(), "Hello World Cordova Plugin", Toast.LENGTH_LONG).show();
    }

    private static String ekoBaseUrl = "https://dashboard.ekodevices.com/";
    private static String apiKey = "af951a54474122251b23ff27db4d71e80e5092b7a0e854cd085bb1b2273c617f";
    private static String secretKey = "01d6508f6abe9991a2c77f68ff87868659ec4e06a4c16f7c81c1459f298f3367";

    public void initializeSdk() {
        Log.d("AB", "Initializing the EKo SDK ###");
        instance = EkoConnect.Companion.getInstance(webView.getContext());
        Log.d("AB", "Initialized the instace -- " + instance);
        instance.initEkoConnect(ekoBaseUrl, apiKey, secretKey, "pcm_id", authorizationStatus -> Unit.INSTANCE);
        Log.d("AB", "setting the scan listener ");
        instance.setDeviceScanListener(this::foundDevice);
        instance.getScannedDevicesLiveData().observeForever(this::foundDevice);
        instance.getConnectedDeviceLiveData().observeForever(this::connectedToDevice);
        instance.setConnectListener(ecDeviceConnectListener);
        Log.d("AB", "Starting Device Discovery  for Eko device");
        instance.startDeviceDiscovery();
    }

    // @Override
    public void foundDevice(@NonNull ECScanResult ecScanResult) {
        Log.d("AB", "Device found called ");
        if (instance != null && ecScanResult != null && !isDeviceConnected ) {
            Log.d("AB", "Eko Device Found " + ecScanResult);
            Log.d("AB", "Eko Device Found name " + ecScanResult.getName());
            Log.d("AB", "Eko Device Found address " + ecScanResult.getAddress());
            Log.d("AB", "Eko Device Found tyoe " + ecScanResult.getType().name());

            instance.connect(ecScanResult);
        }
    }
    boolean isDeviceConnected = false;

    @Override
    public void connectedToDevice(@Nullable ECDevice ecDevice) {
        Log.d("AB", "Connected to eko device");
        isDeviceConnected = true;
        boolean startStreaming = false;
        if (ecDevice != null) {
            Log.d("AB", "Device is connected and ready to transmit " + ecDevice.getAddress());
            Log.d("AB", "As device is successfully conected will start fetching stream data now ");

            if(!startStreaming) {
                startStreaming = true;
                instance.startStreaming(this);
            }
        }
    }

    @Override
    public void deviceDidDisconnect(@Nullable ECDevice ecDevice, @Nullable String s) {
        Log.d("AB", "Did device eko disconnect ? ");
    }

    @Override
    public void failedToConnectToDevice(@Nullable String s, @Nullable ECDevice ecDevice, @Nullable String s1) {
        Log.d("AB", "Failed to connect  to eko device");
    }

    @Override
    public void onDisplayableAudioDataPoints(@Nullable short[] shorts) {
        Log.d("AB", "######### AUDIO DATA POINT " + shorts.length);
        //localAudioStreamingManager.feedAudio(shorts);

    }

    @Override
    public void onFilteredECGDataPoints(@Nullable short[] shorts) {
        Log.d("AB", " ************** ECG DATA POINT " + shorts.length);
    }

    @Override
    public void onRecordingCommandReceived() {
        Log.d("AB", "On Recording command recieved ");
    }


    @Override
    public void onPacketData(@Nullable short[] shorts, @Nullable short[] shorts1) {

    }


}
