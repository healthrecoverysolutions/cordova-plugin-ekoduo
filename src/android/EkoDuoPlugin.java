package cordova.plugin.ekoduo;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ekodevices.ekoconnect.EkoConnect;
import com.ekodevices.ekoconnect.audio.RecordingManager;
import com.ekodevices.ekoconnect.data.entities.device.ECDevice;
import com.ekodevices.ekoconnect.data.entities.device.ECScanResult;
import com.ekodevices.ekoconnect.data.entities.recording.ECRecordingAggregate;
import com.ekodevices.ekoconnect.eventlisteners.external.ECDeviceConnectListener;
import com.ekodevices.ekoconnect.eventlisteners.external.ECDeviceScanListener;
import com.ekodevices.ekoconnect.eventlisteners.external.ECRecordingListener;
import com.ekodevices.ekoconnect.eventlisteners.external.ECStreamListener;
import com.ekodevices.ekoconnect.network.response.ECRecording;
import com.ekodevices.ekoconnect.network.response.ECRecordingPosition;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kotlin.Unit;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;



/**
 * This class echoes a string called from JavaScript.
 */
public class EkoDuoPlugin extends CordovaPlugin implements  /*ECDeviceScanListener,*/ ECStreamListener {

    public static EkoConnect instance;
    private CallbackContext deviceDiscoveredCallback;
    private CallbackContext connectCallback;
    private CallbackContext streamingCallback;
    private CallbackContext recordingCallback;
    private static String ekoBaseUrl = "https://dashboard.ekodevices.com/";
    private static String apiKey = "af951a54474122251b23ff27db4d71e80e5092b7a0e854cd085bb1b2273c617f";
    private static String secretKey = "01d6508f6abe9991a2c77f68ff87868659ec4e06a4c16f7c81c1459f298f3367";
    boolean isDeviceConnected = false;
    // key is the MAC Address
    private Map<String, ECScanResult> scannedPeripherals = new LinkedHashMap<String, ECScanResult>();
    private Map<String, ECDevice> connectedPeripherals = new LinkedHashMap<String, ECDevice>();
    private cordova.plugin.ekoduo.DataCoordinator dataCoordinator = new cordova.plugin.ekoduo.DataCoordinator();
    public static RecordingManager recordingManager;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d("AB", "Execute with action " + action);

        if(action.equals("nativeToast")){
            Log.d("AB", " Native Call to show TOAST -- ");
            nativeToast();
            return true;
        }
        else if(action.equals("initializeSdk")) {
            Log.d("AB", "Initialize SDK -- ");

            cordova.getActivity().runOnUiThread(
                new Runnable() {
                    public void run() {
                        initializeSdk();
                    }
                });

            return true;

        } else if(action.equals("startDeviceDiscovery")) {
            if (instance != null) { // only if instance has been initialized
                Log.d("AB", "Start device discovery " + callbackContext);
                deviceDiscoveredCallback = callbackContext;
                startDeviceDiscovery(callbackContext);

            }
            return true;
        } else if(action.equals("startDeviceDiscoveryForDuration")) {
            if (instance != null) { // only if instance has been initialized
                Log.d("AB", "Start device discovery " + callbackContext);
                deviceDiscoveredCallback = callbackContext;
                int scanForSeconds = args.optInt(0);
                Log.d("AB", "scan for seconds ? " + scanForSeconds);
                startDeviceDiscoveryForDuration(callbackContext, scanForSeconds);

            }
            return true;
        } else if (action.equals("connect")) {
            if(instance!=null) {
                // instance.stopDeviceDiscovery(); // decide whether this should be exposed to ionic and done from there
                connectCallback = callbackContext;
                Log.d("AB", "Call to connect with callback context " + callbackContext);
                JSONObject selectedDevice = args.getJSONObject(0);
                Log.d("AB", "Selected device object " + selectedDevice);
                ECScanResult deviceToConnect = jsonToEkoDevice(selectedDevice);
                Log.d("AB", "Device to connect -- passed from native " + deviceToConnect.getAddress() + " " + deviceToConnect.getName()
                    + " " + deviceToConnect.getRssi());
                ECScanResult originalDeviceObject = scannedPeripherals.get(deviceToConnect.getAddress());
                Log.d("AB", "Original object to connect ? " + originalDeviceObject);
                if (originalDeviceObject!=null) {
                    instance.connect(originalDeviceObject);

                    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                    result.setKeepCallback(true);
                    Log.d("AB", "Callback context to send the plugin result for CONNECT " + callbackContext);
                    callbackContext.sendPluginResult(result);
                } else {
                    Log.d("AB", "Tried to connect to a null instance of eko ");
                }

            }
            return true;

        } else if (action.equals("isConnected")) {
            if (instance!=null) {
                Log.d("AB", "Is connected call for device ");
                String macAddress = args.getString(0);

                if (connectedPeripherals.containsKey(macAddress) && connectedPeripherals.get(macAddress).getPaired()) {
                    Log.d("AB", "DEVICE  " + macAddress + " is connected TRUE ");
                    callbackContext.success();
                } else {
                    Log.d("AB", "DEVICE  " + macAddress + " is connected FALSE ");
                    callbackContext.error("Not connected.");
                }

            }
            return true;
        } else if (action.equals("startStreaming")) {
            if(instance!=null) {
                Log.d("AB", "Eko Plugin: Start Streaming");
                streamingCallback = callbackContext;
                instance.startStreaming(this);

                // can stop device discovery now
                instance.stopDeviceDiscovery();

                // by this we will get Display Audio Data: Data suitable for visualization.
                // Filtered ECG Data: Data suitable for visualization. (Exclusively on Eko devices supporting ECG data)

                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);

            }
            return true;
        } else if (action.equals("startRecording")) {
            if(instance!=null) {
                Log.d("AB", "Eko Plugin: Start Recording");
                recordingCallback = callbackContext;

                // by this we will get Display Audio Data: Data suitable for visualization.
                // Filtered ECG Data: Data suitable for visualization. (Exclusively on Eko devices supporting ECG data)
                int recordingDuration = args.getInt(0);
                recordingManager.startRecording(recordingDuration, new ECRecordingPosition());

                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);

            }
            return true;
        }
        return false;
    }

    public void nativeToast(){
        Toast.makeText(webView.getContext(), "Hello World Cordova Plugin", Toast.LENGTH_LONG).show();
    }



    public void initializeSdk() {
        Log.d("AB", "Initializing the EKo SDK ###");
        instance = EkoConnect.Companion.getInstance(webView.getContext());
        Log.d("AB", "Initialized the instace -- " + instance);
        instance.initEkoConnect(ekoBaseUrl, apiKey, secretKey, "pcm_id", authorizationStatus -> Unit.INSTANCE);
        Log.d("AB", "setting the scan listener ");
        // instance.setDeviceScanListener(this::foundDevice);
        instance.setDeviceScanListener(ekoScanListener);
        // instance.getScannedDevicesLiveData().observeForever(this::foundDevice);
        // instance.getConnectedDeviceLiveData().observeForever(this::connectedToDevice);
        instance.setConnectListener(/*ecDeviceConnectListener*/ /*this*/ ekoDeviceConnectListener);
        Log.d("AB", "Eko SDK Initialized");
        recordingManager = instance.getRecordingManager();
        recordingManager.addRecordingListener(ekoRecordingListener);
    }

    public void startDeviceDiscovery(CallbackContext callbackContext){
       instance.startDeviceDiscovery();

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        Log.d("AB", "Callback context to send the plugin result for scan " + callbackContext);
        callbackContext.sendPluginResult(result);
        // }
    }

    public void startDeviceDiscoveryForDuration(CallbackContext callbackContext, int scanSeconds){
        instance.startDeviceDiscovery();

        if (scanSeconds > 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("AB", "Stopping Scan");
                    instance.stopDeviceDiscovery();
                }
            }, scanSeconds * 1000);
        }

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        Log.d("AB", "Callback context to send the plugin result for scan " + callbackContext);
        callbackContext.sendPluginResult(result);
        // }
    }

    private JSONObject ekoDeviceToJSON(ECScanResult device)  {
        Log.d("AB", "ekoDeviceToJSON " + device);
        JSONObject json = new JSONObject();
        try {
            json.put("name", device.getName());
            json.put("id", device.getAddress());
            json.put("rssi", device.getRssi());
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }
        // json.put("type", device.getType());
        return json;
    }

    private JSONObject convertECDeviceToJson(ECDevice device) {
        Log.d("AB", "convertECDeviceToJson " + device);
        JSONObject json = new JSONObject();
        try {
            json.put("name", device.getName());
            json.put("id", device.getAddress());
            json.put("rssi", device.getRssi());
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }
        //   json.put("type", device.getType());
        return json;
    }

    private ECScanResult jsonToEkoDevice(JSONObject device)  {
        Log.d("AB", "jsonToEkoDevice " + device);
        ECScanResult selectedDevice = new ECScanResult();
        try {
            selectedDevice.setName(device.getString("name"));
            selectedDevice.setAddress(device.getString("id"));
            selectedDevice.setRssi(device.getInt("rssi"));
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }

        return selectedDevice;
    }

//    @Override
//    public void foundDevice(@NonNull ECScanResult ecScanResult) {
//
//        //   Log.d("AB", "Found Device" + ecScanResult + " address " + ecScanResult.getAddress());
//        if (instance != null && ecScanResult != null && deviceDiscoveredCallback!=null /*&& !isDeviceConnected */) {
//            // Log.d("AB", "Eko Device Found " + ecScanResult);
//            //            Log.d("AB", "Eko Device Found name " + ecScanResult.getName());
//            //            Log.d("AB", "Eko Device Found address " + ecScanResult.getAddress());
//            //            Log.d("AB", "Eko Device Found tyoe " + ecScanResult.getType().name());
//
//            if (!scannedPeripherals.containsKey(ecScanResult.getAddress())) {
//                scannedPeripherals.put(ecScanResult.getAddress(), ecScanResult);
//                Log.d("AB", " IF  -------- Scanned Peripherals do not contain this mac address thus adding it " + scannedPeripherals.size() + "callback " + deviceDiscoveredCallback);
//                JSONObject scannedPeripheral = ekoDeviceToJSON(ecScanResult);
//                // Log.d("AB", "Scanned peripheral  == " + scannedPeripheral.toString());
//                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, scannedPeripheral);
//                pluginResult.setKeepCallback(true);
//                deviceDiscoveredCallback.sendPluginResult(pluginResult);
//
//            } else {
//                // Log.d("AB", " Else ----------Scanned Peripherals contain this mac address thus NOT -- adding it ");
//            }
//        }
//    }

    private ECDeviceScanListener ekoScanListener = new ECDeviceScanListener() {
        @Override
        public void foundDevice(@NonNull ECScanResult ecScanResult) {
            //   Log.d("AB", "Found Device" + ecScanResult + " address " + ecScanResult.getAddress());
            if (instance != null && ecScanResult != null && deviceDiscoveredCallback!=null /*&& !isDeviceConnected */) {
                // Log.d("AB", "Eko Device Found " + ecScanResult);
                //            Log.d("AB", "Eko Device Found name " + ecScanResult.getName());
                //            Log.d("AB", "Eko Device Found address " + ecScanResult.getAddress());
                //            Log.d("AB", "Eko Device Found tyoe " + ecScanResult.getType().name());

                if (!scannedPeripherals.containsKey(ecScanResult.getAddress())) {
                    scannedPeripherals.put(ecScanResult.getAddress(), ecScanResult);
                    Log.d("AB", " IF  -------- Scanned Peripherals do not contain this mac address thus adding it " + scannedPeripherals.size() + "callback " + deviceDiscoveredCallback);
                    JSONObject scannedPeripheral = ekoDeviceToJSON(ecScanResult);
                    // Log.d("AB", "Scanned peripheral  == " + scannedPeripheral.toString());
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, scannedPeripheral);
                    pluginResult.setKeepCallback(true);
                    deviceDiscoveredCallback.sendPluginResult(pluginResult);

                } else {
                    // Log.d("AB", " Else ----------Scanned Peripherals contain this mac address thus NOT -- adding it ");
                }
            }
        }
    };

    private ECDeviceConnectListener ekoDeviceConnectListener = new ECDeviceConnectListener() {
        @Override
        public void connectedToDevice(@Nullable ECDevice ecDevice) {
            Log.d("AB", "Connected to eko device " +ecDevice);

            if (ecDevice != null && !connectedPeripherals.containsKey(ecDevice.getAddress())) {
                Log.d("AB", "Added peripheral to connected peripherals");
                ecDevice.setPaired(true);
                connectedPeripherals.put(ecDevice.getAddress(), ecDevice);
                isDeviceConnected = true;
                boolean startStreaming = false;
                Log.d("AB", "Device is connected and ready to transmit " + ecDevice.getAddress());
                Log.d("AB", "As device is successfully conected will start fetching stream data now ");

                PluginResult result = new PluginResult(PluginResult.Status.OK, convertECDeviceToJson(ecDevice));

                result.setKeepCallback(true);
                connectCallback.sendPluginResult(result);

            }
        }

        @Override
        public void failedToConnectToDevice(@Nullable String s, @Nullable ECDevice ecDevice, @Nullable String s1) {
            Log.d("AB", "Did device eko disconnect ? " + ecDevice +  " string " + s);
        }

        @Override
        public void deviceDidDisconnect(@Nullable ECDevice ecDevice, @Nullable String s) {
            Log.d("AB", "deviceDidDisconnect ? " + ecDevice +  " string " + s);
            if(ecDevice!=null && connectedPeripherals.containsKey(ecDevice.getAddress())) {
                connectedPeripherals.remove(ecDevice);
            }
        }
    };

    private ECRecordingListener ekoRecordingListener = new ECRecordingListener() {
        @Override
        public void recordedAudioCurrentlyStreamingToDevice() {

        }

        @Override
        public void onRecordingUploaded(@NonNull ECRecording ecRecording) {

        }

        @Override
        public void onRecordingStarted() {
            // send notification that recording has been started
            Log.d("AB", "------------------ ON RECORDING STARTED --------------");
//            PluginResult result = new PluginResult(PluginResult.Status.OK, "RecordingStarted");
//
//            // result.setKeepCallback(true);
//            recordingCallback.sendPluginResult(result);
        }

        @Override
        public void onRecordingInfoRetrieved(@Nullable ECRecording ecRecording) {

        }

        @Override
        public void onRecordingFailed(@Nullable Exception e) {

        }

        @Override
        public void onRecordingCreatedLocally(@Nullable File file, @Nullable File file1, @Nullable File file2, @Nullable String s) {
            Log.d("AB", "On recording created lcallaly :: online file name " + s);

            // audio file

            Log.d("AB", "Got recordded audio file name " + file.getName());
            Log.d("AB", "Got recordded audio file path " + file.getPath());
            Log.d("AB", "Got recordded audio file absoulute path " + file.getAbsolutePath());
            Log.d("AB", "Got recordded audio file length " + file.length());
            // ECG file
            Log.d("AB", "Got recordded ECG file name " + file.getName());
            Log.d("AB", "Got recordded ECG file path " + file.getPath());
            Log.d("AB", "Got recordded ECG file absoulute path " + file.getAbsolutePath());
            Log.d("AB", "Got recordded ECG file length " + file.length());

            // can disconnect now as recording has been done.
            instance.disconnectFromDevice();

            PluginResult result = new PluginResult(PluginResult.Status.OK, sendRecordedFile( file, file1));
            result.setKeepCallback(true);
            recordingCallback.sendPluginResult(result);


//
//            Log.d("AB", "recordingManager.getAnalysisPdfLiveData() " + recordingManager.getAnalysisPdfLiveData());
//                recordingManager.getAnalysisPdfLiveData().observe(cordova.getActivity(), data -> {
//                    // update UI
////                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, sendPDFFile(data));
////                    pluginResult.setKeepCallback(true);
//                      Log.d("AB", "Sending PDF LIVE DATA ----- " + data);
////                    recordingCallback.sendPluginResult(pluginResult);
//                });

        }

        @Override
        public void onRecordingAggregateRetrieved(@NonNull ECRecordingAggregate ecRecordingAggregate) {

        }

        @Override
        public void onInvalidInputTime() {

        }

        @Override
        public void onDataLoss(@Nullable File file, @Nullable File file1, @Nullable File file2, @Nullable File file3, @Nullable File file4) {

        }

        @Override
        public void noStreamFromDevice() {

        }
    };

    private JSONObject sendRecordedFile(File audioFile, File ecgFile) {
        Log.d("AB", "Send recorded audio file wrapped in json == ");
        JSONObject json = new JSONObject();
        try {
            json.put("audioFile", audioFile);
            json.put("ecgFile", ecgFile);
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }
        return json;
    }

    private JSONObject convertEkoAudioDataToJSON(short[] shorts) {
        Log.d("AB", "Convert eko audio to == " + shorts);
        JSONObject json = new JSONObject();
        try {
            json.put("DisplayableAudio", shorts);
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }
        return json;
    }

    private JSONObject convertEkoECGDataToJSON(short[] shorts) {
        Log.d("AB", "Convert EC scanned device to json == " + shorts);
        JSONObject json = new JSONObject();
        try {
            json.put("ECGData", shorts);
        } catch (JSONException e) { // this shouldn't happen
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void onDisplayableAudioDataPoints(@Nullable short[] output) {
        Log.d("AB", "######### AUDIO DATA POINT " + output.length);
        dataCoordinator.addAudioPoints(output);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, convertEkoAudioDataToJSON(output));
        pluginResult.setKeepCallback(true);
        streamingCallback.sendPluginResult(pluginResult);
        //localAudioStreamingManager.feedAudio(shorts);

    }

    @Override
    public void onFilteredECGDataPoints(@Nullable short[] shorts) {
        Log.d("AB", " ************** ECG DATA POINT " + shorts.length);
        dataCoordinator.addECGPoints(shorts);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, convertEkoECGDataToJSON(shorts));
        pluginResult.setKeepCallback(true);
        streamingCallback.sendPluginResult(pluginResult);
    }

    @Override
    public void onRecordingCommandReceived() {
        Log.d("AB", "On Recording command recieved ");
    }


    @Override
    public void onPacketData(@Nullable short[] shorts, @Nullable short[] shorts1) {

    }

}

