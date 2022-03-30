package cordova.plugin.ekoduo;

import android.util.Log;

import com.ekodevices.ekoconnect.viewmodel.BaseEkoConnectViewModel;
import com.ekodevices.ekoconnect.viewmodel.BaseEkoRecordingViewModel;

import java.util.LinkedList;

public class DataCoordinator {
    private static final String TAG = DataCoordinator.class.getSimpleName();
    public static final int AUDIO_SAMPLES_PER_POINT = 8; //
    // Note: if AUDIO_SAMPLES_PER_POINT is not 2, then
    // assignment of x coordinates for ECG data needs to be
    // changed.  ECG data point should align with 8th original
    // audio data point.

    private static final int AUDIO_SUBSAMPLE_INDEX = 0;  // 0 based index, should be 1 when samples per point is 2, 0 otherwise
    private LinkedList<Short> dynamicSourceAudio;    // data stream from device
    private LinkedList<Short> dynamicSourceECG;    // data stream from device
    private long lastFetchTime = -1;
    private long lastElapsedTime = -1;
    private static int AUDIO_SAMPLING_RATE = 4000;
    private static int ECG_SAMPLING_RATE = 500;

    public DataCoordinator(){
        dynamicSourceAudio = new LinkedList<Short>();
        dynamicSourceECG = new LinkedList<Short>();
    }

    public void addAudioPoints(short[] vals){
        for(short val : vals) {
            Log.d("AB", "Added Audio Point " + val);
            dynamicSourceAudio.add(val);
        }
    }

    public void addECGPoints(short[] vals){
        for(short val : vals) {
            Log.d("AB", "Added ECG Point " + val);
            dynamicSourceECG.add(val);
        }
    }

    public void updateTime(int newTime){ BaseEkoRecordingViewModel viewModel;
        newTime = newTime / (AUDIO_SAMPLING_RATE * 1000) * AUDIO_SAMPLING_RATE * 1000; // remove fractional portion of time
        lastElapsedTime = newTime - lastFetchTime;
        lastFetchTime = newTime;
    }

    public int getNumAudioSamplesToFetch(int currentTime) {
        if (lastFetchTime == -1) {
            return 0;
        } else {
            long elapsedTime = currentTime - lastFetchTime;
            int numAudioSamplesToFetch = (int) ( ((float) elapsedTime)  * (float) AUDIO_SAMPLING_RATE / 1000);
            if(numAudioSamplesToFetch > dynamicSourceAudio.size()){
                numAudioSamplesToFetch = dynamicSourceAudio.size();
            }
            //  Log.d(TAG, String.valueOf(numAudioSamplesToFetch) + " " + dynamicSourceAudio.size());

            return numAudioSamplesToFetch;
        }
    }

    public int getNumECGSamplesToFetch(int currentTime) {
        if (lastFetchTime == -1) {
            return 0;
        } else {
            long elapsedTime = currentTime - lastFetchTime;
            int numECGSamplesToFetch = (int) ( ((float) elapsedTime) * (float) ECG_SAMPLING_RATE / 1000);
            if(numECGSamplesToFetch > dynamicSourceECG.size()){
                numECGSamplesToFetch = dynamicSourceECG.size();
            }
            // Log.d(TAG, String.valueOf(numECGSamplesToFetch) + " " + dynamicSourceECG.size());

            return numECGSamplesToFetch;
        }
    }

//    public void fetchAudioPoints(DynamicWaveformView waveformDrawer, int numSamplesToFetch) {
//        int j = 0, totalPoints = 0;
//
//        for (int i = 0; i < numSamplesToFetch; i++) {
//            short val = dynamicSourceAudio.pollFirst();
//            if ( (j % AUDIO_SAMPLES_PER_POINT) == AUDIO_SUBSAMPLE_INDEX) {
//                waveformDrawer.forwardAudio(val);
//                totalPoints++;
//            }
//            j++;
//        }
//
//        waveformDrawer.drawEnd += totalPoints;
//        if (waveformDrawer.drawEnd >= waveformDrawer.getInnerDataListAudioSize()) {
//            waveformDrawer.drawEnd = waveformDrawer.getInnerDataListAudioSize() - 1;
//        }
//        waveformDrawer.drawStart = 0;
//    }


//    public void fetchECGPoints(DynamicWaveformView waveformDrawer, int numSamplesToFetch) {
//        for (int i = 0; i < numSamplesToFetch; i++) {
//            short val = dynamicSourceECG.pollFirst();
//            waveformDrawer.forwardECG(val); // TODO move buffer management into DataCoordinator (innerDataList, etc).
//        }
//
//        waveformDrawer.drawEndECG += numSamplesToFetch;
//        if (waveformDrawer.drawEndECG >= waveformDrawer.getInnerDataListECGSize()) {
//            waveformDrawer.drawEndECG = waveformDrawer.getInnerDataListECGSize() - 1;
//        }
//        waveformDrawer.drawStartECG = 0;
//    }

    public boolean audioSourceReady(int numAudioSamplesToAdd){

        boolean audioSourceReady = false;
        if (dynamicSourceAudio != null) {
            if (dynamicSourceAudio.size() >= numAudioSamplesToAdd) {
                audioSourceReady = true;
            }
        }
        if(!audioSourceReady){
            //  Log.d(TAG, "audioSource not ready " + String.valueOf(numAudioSamplesToAdd) + " " + dynamicSourceAudio.size());
        } else {
            //   Log.d(TAG, "audioSource ready " + String.valueOf(numAudioSamplesToAdd) + " " + dynamicSourceAudio.size());

        }

        return audioSourceReady;

    }

    public boolean ecgSourceReady(int numECGSamplesToAdd){

        boolean ecgSourceReady = false;
        if (dynamicSourceECG != null) {
            if (dynamicSourceECG.size() >= numECGSamplesToAdd) {
                ecgSourceReady = true;
            }
        }
        if(!ecgSourceReady){
            // Log.d(TAG, "ecgSource not ready " + String.valueOf(numECGSamplesToAdd) + " " + dynamicSourceECG.size());
        } else {
            // Log.d(TAG, "ecgSource ready " + String.valueOf(numECGSamplesToAdd) + " " + dynamicSourceECG.size());
        }
        return ecgSourceReady;

    }
}
