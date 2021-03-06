package com.wopo.plugin;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import com.samsung.android.simplehealth.DataReporter;

import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;

public class SHealthConnector {

    private final int MENU_ITEM_PERMISSION_SETTING = 1;

    private HealthDataStore mStore;
    private HealthConnectionErrorResult mConnError;
    private Set<PermissionKey> mKeySet;
    private DataReporter mReporter;

    String APP_TAG = "CordovaSHealthPlugin";

    Activity activity;
    CallbackContext callbackContext;

    public SHealthConnector(Activity pActivity, CallbackContext pCallbackContext){
        this.activity = pActivity;
        this.callbackContext = pCallbackContext;

        mKeySet = new HashSet<PermissionKey>();
        mKeySet.add(new PermissionKey(HealthConstants.HeartRate.HEALTH_DATA_TYPE, PermissionType.READ));

        mKeySet.add(new PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ));
        mKeySet.add(new PermissionKey(HealthConstants.CaffeineIntake.HEALTH_DATA_TYPE, PermissionType.READ));
        mKeySet.add(new PermissionKey(HealthConstants.WaterIntake.HEALTH_DATA_TYPE, PermissionType.READ));
        mKeySet.add(new PermissionKey(HealthConstants.FoodIntake.HEALTH_DATA_TYPE, PermissionType.READ));

    }

    public void callHealthPermissionManager() {
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(mKeySet, activity).setResultListener(mPermissionListener);
        } catch (Exception e) {
            Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(APP_TAG, "Permission setting fails.");
        }
    }

    public void getData(long startTime, long endTime) {
        HealthDataService healthDataService = new HealthDataService();
        try {
            healthDataService.initialize(activity.getApplicationContext());
        } catch (Exception e) {
            Log.e(APP_TAG, "healthDataService.initialize - " + e.toString());
            e.printStackTrace();
        }

        // Create a HealthDataStore instance and set its listener
        mStore = new HealthDataStore(activity.getApplicationContext(), mConnectionListener);
        // Request the connection to the health data store
        mStore.connectService();
    }

    public void disconnectService() {
        mStore.disconnectService();
    }

    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {

        @Override
        public void onConnected() {
            Log.d(APP_TAG, "Health data service is connected.");
            HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
            mReporter = new DataReporter(mStore, activity, callbackContext);

            try {
                // Check whether the permissions that this application needs are acquired
                Map<PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);

                if (resultMap.containsValue(Boolean.FALSE)) {
                    Log.e(APP_TAG, "try pmsManager.requestPermissions");
                    // Request the permission for reading step counts if it is not acquired
                    pmsManager.requestPermissions(mKeySet, activity).setResultListener(mPermissionListener);
                } else {
                    // Get the current step count and display it
                    mReporter.start();
                }
            } catch (Exception e) {
                Log.e(APP_TAG, e.getClass().getName() + " - " + e.getMessage());
                Log.e(APP_TAG, "Permission setting fails.");
            }
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.d(APP_TAG, "Health data service is not available.");
            //showConnectionFailureDialog(error);
        }

        @Override
        public void onDisconnected() {
            Log.d(APP_TAG, "Health data service is disconnected.");
        }
    };

    private final HealthResultHolder.ResultListener<PermissionResult> mPermissionListener =
            new HealthResultHolder.ResultListener<PermissionResult>() {

                @Override
                public void onResult(PermissionResult result) {
                    Log.d(APP_TAG, "Permission callback is received.");
                    Map<PermissionKey, Boolean> resultMap = result.getResultMap();

                    if (resultMap.containsValue(Boolean.FALSE)) {
                        //drawStepCount("");
                        //showPermissionAlarmDialog();
                    } else {
                        // Get the current step count and display it
                        mReporter.start();
                    }
                }
            };


    @Override
    public String toString() {
        return "Test von SHealthConnector";
    }
}
