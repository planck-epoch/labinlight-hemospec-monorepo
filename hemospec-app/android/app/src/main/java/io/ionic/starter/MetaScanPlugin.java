package io.ionic.starter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ISCMetaScanSDK.ISCMetaScanSDK;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

@CapacitorPlugin(
    name = "MetaScan",
    permissions = {
        @Permission(
            alias = "bluetooth",
            strings = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                // These are ignored on older Android versions by the OS, but needed for compilation if targetSDK >= 31
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT"
            }
        )
    }
)
public class MetaScanPlugin extends Plugin {

    private ISCMetaScanSDK mNanoBLEService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    private boolean isScanning = false;
    private static final long SCAN_PERIOD = 10000;
    private static final String TAG = "MetaScanPlugin";

    private final BroadcastReceiver ScanDataReadyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             JSObject ret = new JSObject();
             ret.put("event", "SCAN_DATA_READY");

             // Attempt to marshall data immediately if available
             try {
                 if (ISCMetaScanSDK.Interpret_length > 0) {
                     JSArray wavelengths = new JSArray();
                     if (ISCMetaScanSDK.Interpret_wavelength != null) {
                         for (double d : ISCMetaScanSDK.Interpret_wavelength) {
                             wavelengths.put(d);
                         }
                     }

                     JSArray intensities = new JSArray();
                     if (ISCMetaScanSDK.Interpret_intensity != null) {
                         for (int i : ISCMetaScanSDK.Interpret_intensity) {
                             intensities.put(i);
                         }
                     }

                     JSObject data = new JSObject();
                     data.put("length", ISCMetaScanSDK.Interpret_length);
                     data.put("wavelengths", wavelengths);
                     data.put("intensities", intensities);
                     ret.put("data", data);
                 }
             } catch (Exception e) {
                 Log.e(TAG, "Error marshalling scan data", e);
             }

             notifyListeners("metaScanEvent", ret);
        }
    };

    private final BroadcastReceiver ScanStartedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            JSObject ret = new JSObject();
            ret.put("event", "SCAN_STARTED");
            notifyListeners("metaScanEvent", ret);
        }
    };

    private final BroadcastReceiver NotifyCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             JSObject ret = new JSObject();
             ret.put("event", "NOTIFY_COMPLETE");
             notifyListeners("metaScanEvent", ret);
        }
    };

    private final BroadcastReceiver DisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             JSObject ret = new JSObject();
             ret.put("event", "DISCONNECTED");
             notifyListeners("metaScanEvent", ret);
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mNanoBLEService = ((ISCMetaScanSDK.LocalBinder) service).getService();
            if (!mNanoBLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mNanoBLEService = null;
        }
    };

    @Override
    public void load() {
        super.load();
        Context context = getContext();
        Intent gattServiceIntent = new Intent(context, ISCMetaScanSDK.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mHandler = new Handler(Looper.getMainLooper());

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(ScanDataReadyReceiver, new IntentFilter(ISCMetaScanSDK.SCAN_DATA));
        LocalBroadcastManager.getInstance(context).registerReceiver(ScanStartedReceiver, new IntentFilter(ISCMetaScanSDK.ACTION_SCAN_STARTED));
        LocalBroadcastManager.getInstance(context).registerReceiver(NotifyCompleteReceiver, new IntentFilter(ISCMetaScanSDK.ACTION_NOTIFY_DONE));
        LocalBroadcastManager.getInstance(context).registerReceiver(DisconnectReceiver, new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED));
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
        Context context = getContext();
        context.unbindService(mServiceConnection);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(ScanDataReadyReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(ScanStartedReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(NotifyCompleteReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(DisconnectReceiver);
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        if (getPermissionState("bluetooth") != com.getcapacitor.PermissionState.GRANTED) {
            requestPermissionForAlias("bluetooth", call, "scanPermsCallback");
        } else {
            scan(call);
        }
    }

    @PermissionCallback
    private void scanPermsCallback(PluginCall call) {
        if (getPermissionState("bluetooth") == com.getcapacitor.PermissionState.GRANTED) {
            scan(call);
        } else {
            call.reject("Permission is required to scan for devices");
        }
    }

    private void scan(PluginCall call) {
        if (mBluetoothLeScanner == null) {
            call.reject("Bluetooth not initialized");
            return;
        }

        if (isScanning) {
            call.resolve();
            return;
        }

        isScanning = true;
        mHandler.postDelayed(() -> {
            isScanning = false;
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }, SCAN_PERIOD);

        mBluetoothLeScanner.startScan(mLeScanCallback);
        call.resolve();
    }

    @PluginMethod
    public void stopScan(PluginCall call) {
        if (mBluetoothLeScanner != null && isScanning) {
            isScanning = false;
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
        call.resolve();
    }

    @PluginMethod
    public void connect(PluginCall call) {
        if (getPermissionState("bluetooth") != com.getcapacitor.PermissionState.GRANTED) {
            requestPermissionForAlias("bluetooth", call, "connectPermsCallback");
        } else {
            connectToDevice(call);
        }
    }

    @PermissionCallback
    private void connectPermsCallback(PluginCall call) {
        if (getPermissionState("bluetooth") == com.getcapacitor.PermissionState.GRANTED) {
            connectToDevice(call);
        } else {
            call.reject("Permission is required to connect");
        }
    }

    private void connectToDevice(PluginCall call) {
        String address = call.getString("address");
        if (address == null) {
            call.reject("Address required");
            return;
        }
        if (mNanoBLEService != null) {
             mNanoBLEService.connect(address);
             call.resolve();
        } else {
            call.reject("Service not bound");
        }
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        String address = call.getString("address");
        if (mNanoBLEService != null) {
             mNanoBLEService.disconnect(address);
             call.resolve();
        } else {
             call.reject("Service not bound");
        }
    }

    @PluginMethod
    public void performScan(PluginCall call) {
        if (mNanoBLEService != null) {
             ISCMetaScanSDK.StartScan();
             call.resolve();
        } else {
             call.reject("Service not bound");
        }
    }

    @PluginMethod
    public void getScanData(PluginCall call) {
        try {
            if (ISCMetaScanSDK.Interpret_length > 0) {
                 JSArray wavelengths = new JSArray();
                 if (ISCMetaScanSDK.Interpret_wavelength != null) {
                     for (double d : ISCMetaScanSDK.Interpret_wavelength) {
                         wavelengths.put(d);
                     }
                 }

                 JSArray intensities = new JSArray();
                 if (ISCMetaScanSDK.Interpret_intensity != null) {
                     for (int i : ISCMetaScanSDK.Interpret_intensity) {
                         intensities.put(i);
                     }
                 }

                 JSObject ret = new JSObject();
                 ret.put("length", ISCMetaScanSDK.Interpret_length);
                 ret.put("wavelengths", wavelengths);
                 ret.put("intensities", intensities);

                 call.resolve(ret);
            } else {
                 call.reject("No data");
            }
        } catch (Exception e) {
            call.reject("Error retrieving data: " + e.getMessage());
        }
    }

    private final ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            if (name == null && result.getScanRecord() != null) {
                name = result.getScanRecord().getDeviceName();
            }

            if (name != null && name.contains("NIR")) {
                 JSObject deviceObj = new JSObject();
                 deviceObj.put("name", name);
                 deviceObj.put("address", device.getAddress());
                 deviceObj.put("rssi", result.getRssi());
                 notifyListeners("deviceFound", deviceObj);
            }
        }
    };
}
