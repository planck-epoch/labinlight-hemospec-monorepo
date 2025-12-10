package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ISCMetaScanSDK.ISCMetaScanSDK;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ISCMetaScanSDK.ISCMetaScanSDK.GetScanConfiguration;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.getBooleanPref;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.getStringPref;
import static com.Innospectra.MetaScan.CommonAPI.ErrorFlash;
import static com.Innospectra.MetaScan.CommonAPI.ReadWarmUpFile;
import static com.Innospectra.MetaScan.CommonAPI.SearchDeviceWarmUpTime;
import static com.Innospectra.MetaScan.CommonAPI.ShouldWarmUp;
import static com.Innospectra.MetaScan.CommonAPI.filterDate;
import static com.Innospectra.MetaScan.CommonAPI.hexToBytes;
import static com.Innospectra.MetaScan.CommonStruct.PreferenceKey.WarmUpSetting;
import static com.Innospectra.MetaScan.CommonStruct.deviceInfo;
import static com.Innospectra.MetaScan.CommonStruct.listScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.configSelectType;
import static com.Innospectra.MetaScan.CommonStruct.CurrentScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.*;

public class MainSelectConfigViewActivity extends Activity {
    Context mContext;
    Button bt_selectdeviceconfig;
    Button bt_quicksetconfig;
    Button bt_quickstart;
    Button bt_warmup;
    RelativeLayout rl_devicesetting;
    TextView tv_physicalbutton;
    Switch switch_physicalbutton;
    TextView tv_activationkey_status;
    EditText et_activationkey;
    Button bt_setkey;
    Button led_connect;
    Button led_ble;
    Button led_scan;
    Button led_error;

    private AnimationDrawable ani_connecting;
    private ProgressBar calProgress;
    private TextView progressBarinsideText;
    private ProgressDialog barProgressDialog;
    private ISCMetaScanSDK mNanoBLEService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    //Check the device is connected or not
    private boolean connected;
    private Handler mHandler;
    private String preferredDevice;
    private AlertDialog alertDialog;
    private Menu mMenu;
    private int NumberOfConfig = 0;
    int receivedConfSize;
    Boolean ShouldDetectWarmUp = false;

    private final BroadcastReceiver NotifyCompleteReceiver = new NotifyCompleteReceiver();
    private final BroadcastReceiver DisconnectReceiver = new DisconnectReceiver();
    private final BroadcastReceiver RefDataReadyReceiver = new RefDataReadyReceiver();
    private final BroadcastReceiver RefCoefficientDataProgressReceiver = new RefCoefficientDataProgressReceiver();
    private final BroadcastReceiver SpectrumCalCoefficientsReadyReceiver = new SpectrumCalCoefficientsReadyReceiver();
    private final BroadcastReceiver CalMatrixDataProgressReceiver = new CalMatrixDataProgressReceiver();
    private final BroadcastReceiver DeviceInfoReceiver = new DeviceInfoReceiver();
    private final BroadcastReceiver GetHWVerReceiver = new GetHWVerReceiver();
    private final BroadcastReceiver GetUUIDReceiver = new GetUUIDReceiver();
    private final BroadcastReceiver ReturnMFGNumReceiver = new ReturnMFGNumReceiver();
    private final BroadcastReceiver ReturnReadActivateStatusReceiver = new ReturnReadActivateStatusReceiver();
    private final BroadcastReceiver ReturnActivateStatusReceiver = new ReturnActivateStatusReceiver();
    private final BroadcastReceiver ResetConfigCompleteReceiver = new ResetConfigCompleteReceiver();
    private final BroadcastReceiver ScanConfSizeReceiver = new ScanConfSizeReceiver();
    private final BroadcastReceiver GetDefaultScanConfReceiver = new GetDefaultScanConfReceiver();
    private final BroadcastReceiver SetDefaultConfigCompleteReceiver = new SetDefaultConfigCompleteReceiver();
    private final BroadcastReceiver ScanConfReceiver = new ScanConfReceiver();
    private final BroadcastReceiver GetDeviceStatusReceiver = new GetDeviceStatusReceiver();
    private final BroadcastReceiver ControlDeviceButtonReceiver = new ControlDeviceButtonReceiver();
    private final BroadcastReceiver SetTimeCompleteReceiver = new SetTimeCompleteReceiver();
    private final BroadcastReceiver BackgroundReciver = new BackGroundReciver();
    private final BroadcastReceiver HomeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();

    private final IntentFilter NotifyCompleteFilter = new IntentFilter(ISCMetaScanSDK.ACTION_NOTIFY_DONE);
    private final IntentFilter DisconnectFilter = new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED);
    private final IntentFilter refReadyFilter = new IntentFilter(ISCMetaScanSDK.REF_CONF_DATA);
    private final IntentFilter RequestCalCoefficientFilter = new IntentFilter(ISCMetaScanSDK.ACTION_REQ_CAL_COEFF);
    private final IntentFilter SpectrumCalCoefficientsReadyFilter = new IntentFilter(ISCMetaScanSDK.SPEC_CONF_DATA);
    private final IntentFilter requestCalMatrixFilter = new IntentFilter(ISCMetaScanSDK.ACTION_REQ_CAL_MATRIX);
    private final IntentFilter ReturnMFGNumFilter = new IntentFilter(ISCMetaScanSDK.ACTION_RETURN_MFGNUM);
    private final IntentFilter DeviceInfoFilter = new IntentFilter(ISCMetaScanSDK.ACTION_INFO);
    private final IntentFilter GetHWVerFilter = new IntentFilter(ISCMetaScanSDK.GET_HWVER);
    private final IntentFilter GetUUIDFilter = new IntentFilter(ISCMetaScanSDK.SEND_DEVICE_UUID);
    private final IntentFilter ReturnReadActivateStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_RETURN_READ_ACTIVATE_STATE);
    private final IntentFilter ReturnActivateStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_RETURN_ACTIVATE);
    private final IntentFilter ResetConfigCompleteFilter = new IntentFilter(ISCMetaScanSDK.RESET_SCANCONFIG_COMPLETE);
    private final IntentFilter GetNumberOfScanConfFilter = new IntentFilter(ISCMetaScanSDK.SCAN_CONF_SIZE);
    private final IntentFilter GetDefaultScanConfFilter = new IntentFilter(ISCMetaScanSDK.SEND_ACTIVE_CONF);
    private final IntentFilter SetDefaultConfigCompleteFilter = new IntentFilter(ISCMetaScanSDK.SET_ACTIVECONFIG_COMPLETE);
    private final IntentFilter ScanConfFilter = new IntentFilter(ISCMetaScanSDK.SCAN_CONF_DATA);
    private final IntentFilter GetDeviceStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_STATUS);
    private final IntentFilter ControlDeviceButtonFilter = new IntentFilter(ISCMetaScanSDK.ACTION_CONTROL_DEVICEBUTTON);
    private final IntentFilter SetTimeCompleteFilter = new IntentFilter(ISCMetaScanSDK.SET_TIME_COMPLETE);
    public static final String NOTIFY_BACKGROUND = "com.Innospectra.MetaScan.MainSelectConfig.NotifyBackground";
    private String  NOTIFY_ISEXTVER = "com.Innospectra.NanoScan.ISEXTVER";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.activity_selectconfig_mainpage);
        setActivityTouchDisable(true);
        mContext = this;
        connected = false;
        ShouldDetectWarmUp = true;
        NewReferenceIntensity.clear();
        NewRefConfigName = "";
        currentDeviceStatus = new CurrentDeviceStatus();
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        InitComponent();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(NotifyCompleteReceiver, NotifyCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(RefDataReadyReceiver, refReadyFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(RefCoefficientDataProgressReceiver, RequestCalCoefficientFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SpectrumCalCoefficientsReadyReceiver, SpectrumCalCoefficientsReadyFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(CalMatrixDataProgressReceiver, requestCalMatrixFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(DeviceInfoReceiver,DeviceInfoFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetHWVerReceiver,GetHWVerFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetUUIDReceiver,GetUUIDFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ReturnMFGNumReceiver, ReturnMFGNumFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ReturnReadActivateStatusReceiver, ReturnReadActivateStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ReturnActivateStatusReceiver, ReturnActivateStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SetTimeCompleteReceiver, SetTimeCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(BackgroundReciver, new IntentFilter(NOTIFY_BACKGROUND));
        registerReceiver(HomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), Context.RECEIVER_NOT_EXPORTED);

        //Bind to the service. This will start it, and call the start command function
        Intent gattServiceIntent = new Intent(this, ISCMetaScanSDK.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }
    @Override
    public void onResume() {
        super.onResume();
        if(AppStatus == APPStatus.Home)
            finish();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(DisconnectReceiver, DisconnectFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ResetConfigCompleteReceiver, ResetConfigCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ScanConfSizeReceiver, GetNumberOfScanConfFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ScanConfReceiver, ScanConfFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetDefaultScanConfReceiver, GetDefaultScanConfFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SetDefaultConfigCompleteReceiver, SetDefaultConfigCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetDeviceStatusReceiver,GetDeviceStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ControlDeviceButtonReceiver,ControlDeviceButtonFilter);
        if(connected)
        {
            if(currentDeviceStatus.ForceWarmUp)
            {
                if(ShouldWarmUp())
                {
                    bt_warmup.setTextColor(getResources().getColor(R.color.red));
                    bt_warmup.setBackground(getDrawable(R.drawable.button_style_not_warmup));
                }
                else
                {
                    bt_warmup.setTextColor(getResources().getColor(R.color.royalblue));
                    bt_warmup.setBackground(getDrawable(R.drawable.button_style));
                }
            }
            else
            {
                bt_warmup.setTextColor(getResources().getColor(R.color.black));
                bt_warmup.setBackground(getDrawable(R.drawable.button_style_diable_warmup));
            }
            setActivityTouchDisable(false);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(NotifyCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DisconnectReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(RefDataReadyReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(RefCoefficientDataProgressReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SpectrumCalCoefficientsReadyReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(CalMatrixDataProgressReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DeviceInfoReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetHWVerReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetUUIDReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnMFGNumReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnReadActivateStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnActivateStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ResetConfigCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanConfSizeReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanConfReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetDefaultScanConfReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetDefaultConfigCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetDeviceStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ControlDeviceButtonReceiver);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(NotifyCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DisconnectReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(RefDataReadyReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(RefCoefficientDataProgressReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SpectrumCalCoefficientsReadyReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(CalMatrixDataProgressReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DeviceInfoReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetUUIDReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnMFGNumReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnReadActivateStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnActivateStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ResetConfigCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanConfSizeReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanConfReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetDefaultScanConfReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetDefaultConfigCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetDeviceStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ControlDeviceButtonReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetTimeCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(BackgroundReciver);
        mNanoBLEService.close();
        mHandler.removeCallbacksAndMessages(null);
        unbindService(mServiceConnection);
        unregisterReceiver(HomeKeyEventReceiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_right_top_text, menu);
        mMenu = menu;
        MenuItem action_righttop_button = menu.findItem(R.id.action_righttop_button);
        SpannableString s = new SpannableString(getResources().getString(R.string.reset_config));
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        action_righttop_button.setTitle(s);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            this.finish();
        else if(id == R.id.action_righttop_button)
            confirmationDialog();
        return super.onOptionsItemSelected(item);
    }
    //region GUI
    public void InitComponent()
    {
        bt_selectdeviceconfig = (Button)findViewById(R.id.bt_selectdeviceconfig);
        bt_quicksetconfig = (Button)findViewById(R.id.bt_quicksetconfig);
        bt_quickstart = (Button)findViewById(R.id.bt_quickstart);
        bt_warmup = (Button)findViewById(R.id.bt_warmup);
        rl_devicesetting = (RelativeLayout) findViewById(R.id.rl_devicesetting);
        tv_physicalbutton = (TextView) findViewById(R.id.tv_physicalbutton);
        switch_physicalbutton = (Switch)findViewById(R.id.switch_physicalbutton);
        tv_activationkey_status = (TextView) findViewById(R.id.tv_activationkey_status);
        et_activationkey = (EditText) findViewById(R.id.et_activationkey);
        bt_setkey = (Button) findViewById(R.id.bt_setkey);
        led_connect = (Button)findViewById(R.id.led_connect);
        led_ble = (Button)findViewById(R.id.led_ble);
        led_scan = (Button)findViewById(R.id.led_scan);
        led_error = (Button)findViewById(R.id.led_error);
        calProgress = (ProgressBar)findViewById(R.id.calProgress);
        progressBarinsideText = (TextView)findViewById(R.id.progressBarinsideText);

        bt_selectdeviceconfig.setOnClickListener(Button_Listener);
        bt_quicksetconfig.setOnClickListener(Button_Listener);
        bt_quickstart.setOnClickListener(Button_Listener);
        bt_warmup.setOnClickListener(Button_Listener);
        led_connect.setOnClickListener(Button_Listener);
        led_ble.setOnClickListener(Button_Listener);
        led_scan.setOnClickListener(Button_Listener);
        led_error.setOnClickListener(Button_Listener);
        switch_physicalbutton.setOnCheckedChangeListener(Swich_Button_Listener);
        bt_setkey.setOnClickListener(Button_Listener);

        ani_connecting = (AnimationDrawable)getResources().getDrawable(R.drawable.led_flashing_connecting);
        led_connect.setCompoundDrawablesWithIntrinsicBounds( ani_connecting, null, null, null);
        ani_connecting.start();
    }
    private Button.OnClickListener Button_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId())
            {
                case R.id.bt_selectdeviceconfig:
                    setActivityTouchDisable(true);
                    configSelectType = CommonStruct.ConfigSelectType.SelectConfig;
                    intent = new Intent(mContext, SelectConfigViewActivity.class);
                    startActivity(intent);
                    break;
                case R.id.bt_quicksetconfig:
                    setActivityTouchDisable(true);
                    configSelectType = CommonStruct.ConfigSelectType.SetConfig;
                    intent = new Intent(mContext, QuicksetViewActivity.class);
                    startActivity(intent);
                    break;
                case R.id.bt_quickstart:
                    setActivityTouchDisable(true);
                    SetDefaultConfig();
                    configSelectType = CommonStruct.ConfigSelectType.QuickScan;
                    break;
                case R.id.bt_warmup:
                    setActivityTouchDisable(true);
                    intent = new Intent(mContext, WarmUpViewActivity.class);
                    startActivity(intent);
                    break;
                case R.id.bt_setkey:
                    Boolean checklength = checkActivationKeyLength();
                    if(!checklength)
                        Dialog_Pane("Error","Activation key length is not correct.");
                    else
                    {
                        String filterdata = filterDate(et_activationkey.getText().toString());
                        byte data[] = hexToBytes(filterdata);
                        ISCMetaScanSDK.SetLicenseKey(data);
                        progressBarinsideText.setText("Set Activation Key");
                        DisableGUI();
                    }
                    break;
                case R.id.led_connect:
                case R.id.led_ble:
                case R.id.led_scan:
                case R.id.led_error:
                    setActivityTouchDisable(true);
                    intent = new Intent(mContext, DeviceStatusViewActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
    ToggleButton.OnCheckedChangeListener Swich_Button_Listener = new ToggleButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            boolean IsLock = switch_physicalbutton.isChecked();
            SetDeviceButtonStatus(IsLock);
            if(IsLock)
                tv_physicalbutton.setText(R.string.lock_device_button);
            else
                tv_physicalbutton.setText(R.string.unlock_device_button);
        }
    };
    private void notConnectedDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.not_connected_title));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(mContext.getResources().getString(R.string.not_connected_message));

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                finish();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    //endregion
    //region Manage service lifecycle
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            //Get a reference to the service from the service connection
            mNanoBLEService = ((ISCMetaScanSDK.LocalBinder) service).getService();

            //initialize bluetooth, if BLE is not available, then finish
            if (!mNanoBLEService.initialize()) {
                finish();
            }
            //Start scanning for devices that match DEVICE_NAME
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if(mBluetoothLeScanner == null){
                finish();
                Toast.makeText(MainSelectConfigViewActivity.this, "Please ensure Bluetooth is enabled and try again", Toast.LENGTH_SHORT).show();
            }
            mHandler = new Handler();
            if (getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null) != null) {
                preferredDevice = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
                scanPreferredLeDevice(true);
            } else {
                scanLeDevice(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mNanoBLEService = null;
        }
    };

    private final ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String preferredNano = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if(name == null)
                name = result.getScanRecord().getDeviceName();
            if (device.getAddress().equals(preferredNano)) {
                mNanoBLEService.connect(device.getAddress());
                connected = true;
                scanLeDevice(false);
            }
        }
    };

    private final ScanCallback mPreferredLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String preferredNano = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
            if(name == null)
                name = result.getScanRecord().getDeviceName();
            if (device.getAddress().equals(preferredNano)) {
                if (device.getAddress().equals(preferredDevice)) {
                    mNanoBLEService.connect(device.getAddress());
                    connected = true;
                    scanPreferredLeDevice(false);
                }
            }
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mBluetoothLeScanner != null) {
                        mBluetoothLeScanner.stopScan(mLeScanCallback);
                        if (!connected) {
                            notConnectedDialog();
                        }
                    }
                }
            }, ISCMetaScanSDK.SCAN_PERIOD);
            if(mBluetoothLeScanner != null) {
                mBluetoothLeScanner.startScan(mLeScanCallback);
            }else{
                finish();
                Toast.makeText(MainSelectConfigViewActivity.this, "Please ensure Bluetooth is enabled and try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    private void scanPreferredLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mPreferredLeScanCallback);
                    if (!connected) {

                        scanLeDevice(true);
                    }
                }
            }, ISCMetaScanSDK.SCAN_PERIOD);
            if(mBluetoothLeScanner == null)
            {
                notConnectedDialog();
            }
            else
            {
                mBluetoothLeScanner.startScan(mPreferredLeScanCallback);
            }

        } else {
            mBluetoothLeScanner.stopScan(mPreferredLeScanCallback);
        }
    }
    public class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(mContext, R.string.nano_disconnected, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    //endregion
    //region After connect to the device
    public class NotifyCompleteReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            //Connected
            ani_connecting.stop();
            led_connect.setText("Connect");
            led_connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.led_g, 0, 0, 0);
            //led_flashing
            AnimationDrawable ani = (AnimationDrawable)getResources().getDrawable(R.drawable.led_flashing);
            led_ble.setCompoundDrawablesWithIntrinsicBounds( ani, null, null, null);
            ani.start();
            ISCMetaScanSDK.getHardwareRev();
        }
    }
    public class GetHWVerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            deviceInfo = new CommonStruct.DeviceInfo();
            deviceInfo.TIVAVer = intent.getStringExtra(ISCMetaScanSDK.EXTRA_TIVA_REV);
            String HWrev = intent.getStringExtra(ISCMetaScanSDK.EXTRA_HW_REV);
            deviceInfo.HWVer = HWrev;
            String split_hw[] = HWrev.split("\\.");
            deviceInfo.MainBoard = split_hw[0] + ",";
            deviceInfo.DetectBoard = split_hw[2] + ",";
            if(deviceInfo.TIVAVer.substring(0,1) .equals("3") && (deviceInfo.HWVer.substring(0,1).equals("E")|| deviceInfo.HWVer.substring(0,1).equals("O")))
                deviceInfo.deviceWavelengthType = DeviceWavelengthType.Extend;
            else if(deviceInfo.TIVAVer.substring(0,1) .equals("5"))
                deviceInfo.deviceWavelengthType = DeviceWavelengthType.Extend_Plus;
            else
                deviceInfo.deviceWavelengthType = DeviceWavelengthType.Standard;
            //Synchronize time
            ISCMetaScanSDK.SetCurrentTime();
        }
    }
    public class SetTimeCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ISCMetaScanSDK.requestRefCalCoefficients();
        }
    }
    /**
     * Custom receiver for receiving calibration coefficient data.(ISCMetaScanSDK.SetCurrentTime()must be called)
     */
    public class RefCoefficientDataProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getIntExtra(ISCMetaScanSDK.EXTRA_REF_CAL_COEFF_SIZE, 0);
            Boolean size = intent.getBooleanExtra(ISCMetaScanSDK.EXTRA_REF_CAL_COEFF_SIZE_PACKET, false);
            if (size) {
                calProgress.setVisibility(View.GONE);
                progressBarinsideText.setVisibility(View.GONE);
                barProgressDialog = new ProgressDialog(MainSelectConfigViewActivity.this);
                barProgressDialog.setTitle(getString(R.string.dl_ref_cal));
                barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                barProgressDialog.setProgress(0);
                barProgressDialog.setMax(intent.getIntExtra(ISCMetaScanSDK.EXTRA_REF_CAL_COEFF_SIZE, 0));
                barProgressDialog.setCancelable(false);
                barProgressDialog.show();
            } else {
                barProgressDialog.setProgress(barProgressDialog.getProgress() + intent.getIntExtra(ISCMetaScanSDK.EXTRA_REF_CAL_COEFF_SIZE, 0));
            }
        }
    }
    /**
     * Custom receiver for receiving calibration matrix data. When this receiver action complete, it
     * will request the active configuration so that it can be displayed in the listview(ISCMetaScanSDK.SetCurrentTime()must be called)
     */
    public class CalMatrixDataProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getIntExtra(ISCMetaScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0);
            Boolean size = intent.getBooleanExtra(ISCMetaScanSDK.EXTRA_REF_CAL_MATRIX_SIZE_PACKET, false);
            if (size) {
                barProgressDialog.dismiss();
                barProgressDialog = new ProgressDialog(MainSelectConfigViewActivity.this);
                barProgressDialog.setTitle(getString(R.string.dl_cal_matrix));
                barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                barProgressDialog.setProgress(0);
                barProgressDialog.setMax(intent.getIntExtra(ISCMetaScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0));
                barProgressDialog.setCancelable(false);
                barProgressDialog.show();
            } else {
                barProgressDialog.setProgress(barProgressDialog.getProgress() + intent.getIntExtra(ISCMetaScanSDK.EXTRA_REF_CAL_MATRIX_SIZE, 0));
            }
            if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                calProgress.setVisibility(View.VISIBLE);
                progressBarinsideText.setText("Get Device Info ...");
                progressBarinsideText.setVisibility(View.VISIBLE);
                //Get spectrum calibration coefficient
                ISCMetaScanSDK.GetSpectrumCoef();
            }
        }
    }
    /**
     * Complete to  download reference calibration  matrix
     */
    public class RefDataReadyReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            barProgressDialog.dismiss();
        }
    }
    public class SpectrumCalCoefficientsReadyReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            deviceInfo.SpectrumCalCoefficients = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_SPEC_COEF_DATA);
            ISCMetaScanSDK.GetNumberOfScanConfig();
        }
    }
    /**
     * Send broadcast  GET_SCAN_CONF will  through ScanConfSizeReceiver to get the number of scan config(ISCMetaScanSDK.GetScanConfig() should be claaed)
     */
    private class ScanConfSizeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NumberOfConfig = intent.getIntExtra(ISCMetaScanSDK.EXTRA_CONF_SIZE, 0);
            listScanConfig.ScanConfigName = new String[NumberOfConfig];
            listScanConfig.Configs.clear();
            ISCMetaScanSDK.requestStoredConfigurationList();
            if (NumberOfConfig > 0) {
                barProgressDialog = new ProgressDialog(MainSelectConfigViewActivity.this);
                barProgressDialog.setTitle(getString(R.string.reading_configurations));
                barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                barProgressDialog.setProgress(0);
                barProgressDialog.setMax(intent.getIntExtra(ISCMetaScanSDK.EXTRA_CONF_SIZE, 0));
                barProgressDialog.setCancelable(false);
                barProgressDialog.show();
                receivedConfSize = 0;
            }
        }
    }
    /**
     * Send broadcast  GET_SCAN_CONF will  through ScanConfSizeReceiver to get the scan config data(ISCMetaScanSDK.GetScanConfig() should be called)
     */
    private class ScanConfReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ISCMetaScanSDK.ScanConfiguration scanConf = GetScanConfiguration(intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_DATA));
            listScanConfig.ScanConfigName[receivedConfSize] = scanConf.getConfigName();
            receivedConfSize++;
            listScanConfig.Configs.add(scanConf);
            if (receivedConfSize == NumberOfConfig) {
                ISCMetaScanSDK.GetActiveConfig();
            } else {
                barProgressDialog.setProgress(receivedConfSize);
            }
        }
    }
    /**
     * Send broadcast  GET_ACTIVE_CONF will  through GetDefaultScanConfReceiver to get active config(ISCMetaScanSDK.GetDefaultConfig() should be called)
     */
    private class GetDefaultScanConfReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            listScanConfig.DefaultConfigIndex = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_ACTIVE_CONF)[0];
            for(int i=0;i<listScanConfig.Configs.size();i++)
            {
                //get the first one byte
                int ScanConfigIndextoByte = (byte)listScanConfig.Configs.get(i).getScanConfigIndex();
                if(listScanConfig.Configs.get(i).getScanConfigIndex() == listScanConfig.DefaultConfigIndex || ScanConfigIndextoByte == listScanConfig.DefaultConfigIndex)
                    listScanConfig.DefaultConfigName = listScanConfig.Configs.get(i).getConfigName();
            }
            barProgressDialog.dismiss();
            ISCMetaScanSDK.GetDeviceStatus();
        }
    }
    public class SetDefaultConfigCompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            intent = new Intent(mContext, ScanViewActivity.class);
            startActivity(intent);
        }
    }
    public class GetDeviceStatusReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            byte[] errbyte = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_ERR_BYTE);
            for(int i= 0;i<20;i++)
            {
                if(errbyte[i] !=0)
                {
                    AnimationDrawable ani_error = ErrorFlash(mContext);
                    led_error.setCompoundDrawablesWithIntrinsicBounds( ani_error, null, null, null);
                    ani_error.start();
                    break;
                }
            }
            ISCMetaScanSDK.GetDeviceInfo();
        }
    }
    public class DeviceInfoReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            deviceInfo.Manufacturer = "Inno-Spectra Corp.";
            deviceInfo.ModelName = intent.getStringExtra(ISCMetaScanSDK.EXTRA_MODEL_NUM);
            deviceInfo.SerialNum = intent.getStringExtra(ISCMetaScanSDK.EXTRA_SERIAL_NUM);
            device_supportfunction = ISCMetaScanSDK.GetSupportFunction();

            if((deviceInfo.deviceWavelengthType.compareTo(DeviceWavelengthType.Extend) == 0 || deviceInfo.deviceWavelengthType.compareTo(DeviceWavelengthType.Extend_Plus) == 0) && deviceInfo.SerialNum.length()>8)
                deviceInfo.SerialNum = deviceInfo.SerialNum.substring(0,8);
            else if(deviceInfo.deviceWavelengthType.compareTo(DeviceWavelengthType.Standard) == 0 && deviceInfo.SerialNum.length()>7)
                deviceInfo.SerialNum = deviceInfo.SerialNum.substring(0,7);
            if(deviceInfo.HWVer.substring(0,1).equals("N"))
                Dialog_Pane_Finish("Not support","Not to support the N version of the main board.\nWill go to the home page.");
            else
            {
                InitParameter();
                if(device_supportfunction.NotSupport)
                {
                    if(deviceInfo.deviceWavelengthType.compareTo(DeviceWavelengthType.Extend) == 0)
                        Dialog_Pane_Finish("Firmware Out of Date","You must update the firmware on your NIRScan Nano to make this App working correctly!\n" +
                                "FW required version at least V3.3.0.\nDetected version is V" + deviceInfo.TIVAVer +".");
                    else
                        Dialog_Pane_Finish("Firmware Out of Date","You must update the firmware on your NIRScan Nano to make this App working correctly!\n" +
                                "FW required version at least V2.4.4.\nDetected version is V" + deviceInfo.TIVAVer +".");
                }
                else
                    ISCMetaScanSDK.GetMFGNumber();
            }
        }
    }
    /**
     *Get MFG Num (ISCMetaScanSDK.GetMFGNumber() should be called)
     */
    public class ReturnMFGNumReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            byte MFG_NUM[] = intent.getByteArrayExtra(ISCMetaScanSDK.MFGNUM_DATA);
            try {
                deviceInfo.MFGNum  = new String(MFG_NUM, "ISO-8859-1");
                if (!deviceInfo.MFGNum.contains("70UB1") && !deviceInfo.MFGNum.contains("95UB1"))
                    deviceInfo.MFGNum = "";
                else if(deviceInfo.MFGNum.contains("95UB1"))
                    deviceInfo.MFGNum = deviceInfo.MFGNum.substring(0,deviceInfo.MFGNum.length()-2);
            }catch (Exception e)
            {
                deviceInfo.MFGNum = "";
            }
            //Get the uuid of the device
            ISCMetaScanSDK.GetUUID();
        }
    }
    public class GetUUIDReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            deviceInfo.UUID = "";
            byte buf[] = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_DEVICE_UUID);
            for(int i=0;i<buf.length;i++)
            {
                String Val = Integer.toHexString( 0xff & buf[i] );
                if(Val.length() == 1)
                    Val = "0" + Val;
                deviceInfo.UUID += Val;
                if(i!= buf.length-1)
                {
                    deviceInfo.UUID +=":";
                }
            }
            deviceInfo.UUID = deviceInfo.UUID.toUpperCase();
            currentDeviceStatus.UUID = deviceInfo.UUID;
            currentDeviceStatus.WarmUpTime = null;
            if(device_supportfunction.OldVersion)
            {
                if(deviceInfo.deviceWavelengthType.compareTo(DeviceWavelengthType.Extend) == 0)
                    Dialog_Pane_OldTIVA("Firmware Out of Date", "You must update the firmware on your NIRScan Nano to make this App working correctly!\n" +
                            "FW required version at least V3.3.0\nDetected version is V" + deviceInfo.TIVAVer + "\nDo you still want to continue?");
                else
                    Dialog_Pane_OldTIVA("Firmware Out of Date", "You must update the firmware on your NIRScan Nano to make this App working correctly!\n" +
                            "FW required version at least V2.4.4\nDetected version is V" + deviceInfo.TIVAVer + "\nDo you still want to continue?");
            }
            else
            {
                progressBarinsideText.setText("Get Activation Status ...");
                //Get the device is activate or not
                ISCMetaScanSDK.ReadActivateState();
            }
        }
    }
    /**
     * Get the activate state of the device(ISCMetaScanSDK.ReadActivateState() should be called)
     */
    public class ReturnReadActivateStatusReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            byte state[] = intent.getByteArrayExtra(ISCMetaScanSDK.RETURN_READ_ACTIVATE_STATE);
            if(state[0] != 1)
            {
                deviceInfo.IsActivated = false;
                FunctionLockGUI();
            }
            else
            {
                deviceInfo.IsActivated = true;
                FunctionOpenGUI();
            }
           SetDeviceButtonStatus(false);//Default unlock device button
        }
    }
    /**
     *  Get the activate state of the device(ISCMetaScanSDK.SetLicenseKey(data) should be called)
     */
    public class ReturnActivateStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            byte state[] = intent.getByteArrayExtra(ISCMetaScanSDK.RETURN_ACTIVATE_STATUS);
            if(state[0] != 1)
            {
                deviceInfo.IsActivated = false;
                FunctionLockGUI();
            }
            else
            {
                deviceInfo.IsActivated = true;
                FunctionOpenGUI();
            }
            SetDeviceButtonStatus(false);//Default unlock device button
        }
    }
    /**
     *Get MFG Num (ISCMetaScanSDK.GetMFGNumber() should be called)
     */
    public class ControlDeviceButtonReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if(ShouldDetectWarmUp)
            {
                ShouldDetectWarmUp = false;
                ReadWarmUpFile();
                currentDeviceStatus.ForceWarmUp = getBooleanPref(mContext, WarmUpSetting,true);
                SearchDeviceWarmUpTime();
                if(currentDeviceStatus.ForceWarmUp)
                {
                    if(ShouldWarmUp())
                    {
                        intent = new Intent(mContext, WarmUpViewActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        bt_warmup.setTextColor(getResources().getColor(R.color.royalblue));
                        bt_warmup.setBackground(getDrawable(R.drawable.button_style));
                    }
                }
                else
                {
                    bt_warmup.setTextColor(getResources().getColor(R.color.black));
                    bt_warmup.setBackground(getDrawable(R.drawable.button_style_diable_warmup));
                }
                bt_warmup.setVisibility(View.VISIBLE);
                EnablelGUI();
            }
        }
    }
    //endregion
    private void EnablelGUI()
    {
        calProgress.setVisibility(View.GONE);
        progressBarinsideText.setVisibility(View.GONE);
        setActivityTouchDisable(false);
        if(device_supportfunction.OldVersion)
            mMenu.findItem(R.id.action_righttop_button).setVisible(false);
        else
            rl_devicesetting.setVisibility(View.VISIBLE);//enable device setting
    }
    private void DisableGUI()
    {
        calProgress.setVisibility(View.VISIBLE);
        progressBarinsideText.setVisibility(View.VISIBLE);
        setActivityTouchDisable(true);
        rl_devicesetting.setVisibility(View.GONE);//enable device setting
    }
    private void FunctionLockGUI()
    {
        bt_selectdeviceconfig.setVisibility(View.GONE);
        bt_quicksetconfig.setVisibility(View.GONE);
        bt_quickstart.setEnabled(true);
        et_activationkey.setVisibility(View.VISIBLE);
        bt_setkey.setVisibility(View.VISIBLE);
        tv_activationkey_status.setText(R.string.not_activated);
    }
    private void FunctionOpenGUI()
    {
        bt_selectdeviceconfig.setVisibility(View.VISIBLE);
        bt_quicksetconfig.setVisibility(View.VISIBLE);
        bt_quickstart.setEnabled(true);
        et_activationkey.setVisibility(View.GONE);
        bt_setkey.setVisibility(View.GONE);
        tv_activationkey_status.setText(R.string.activated);
    }
    private void Dialog_Pane_Finish(String title,String content)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(content);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void Dialog_Pane(String title,String content)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(content);

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                FunctionLockGUI();
                if(ShouldDetectWarmUp)
                {
                    ShouldDetectWarmUp = false;
                    ReadWarmUpFile();
                    currentDeviceStatus.ForceWarmUp = getBooleanPref(mContext, WarmUpSetting,true);
                    SearchDeviceWarmUpTime();
                    if(currentDeviceStatus.ForceWarmUp)
                    {
                        if(ShouldWarmUp())
                        {
                            Intent intent = new Intent(mContext, WarmUpViewActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            bt_warmup.setTextColor(getResources().getColor(R.color.royalblue));
                            bt_warmup.setBackground(getDrawable(R.drawable.button_style));
                        }
                    }
                    else
                    {
                        bt_warmup.setTextColor(getResources().getColor(R.color.black));
                        bt_warmup.setBackground(getDrawable(R.drawable.button_style_diable_warmup));
                    }
                    bt_warmup.setVisibility(View.VISIBLE);
                    EnablelGUI();
                }
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void setActivityTouchDisable(boolean value) {
        if (value) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    //region API
    private Boolean checkActivationKeyLength()
    {
        String filterdata = filterDate(et_activationkey.getText().toString());
        if(filterdata.length()!=24)
            return false;
        return true;
    }
    /**
     *  Determine the wavelength range of the device and parameter initialization
     */
    private void InitParameter()
    {
        if(deviceInfo.deviceWavelengthType.compareTo(DeviceWavelengthType.Extend_Plus) == 0)
        {
            deviceInfo.minWavelength = 1600;
            deviceInfo.maxWavelength = 2400;
            deviceInfo.MINWAV = 1600;
            deviceInfo.MAXWAV = 2400;
        }
        else if(deviceInfo.deviceWavelengthType.compareTo(DeviceWavelengthType.Extend) == 0)
        {
            deviceInfo.minWavelength = 1350;
            deviceInfo.maxWavelength = 2150;
            deviceInfo.MINWAV = 1350;
            deviceInfo.MAXWAV = 2150;
        }
        else
        {
            deviceInfo.minWavelength = 900;
            deviceInfo.maxWavelength = 1700;
            deviceInfo.MINWAV = 900;
            deviceInfo.MAXWAV = 1700;
        }
    }
    private void SetDeviceButtonStatus(Boolean isLockButton)
    {
        //User open lock button on Configure page
        if(isLockButton)
            ISCMetaScanSDK.ControlPhysicalButton(ISCMetaScanSDK.PhysicalButton.Lock);
        else
            ISCMetaScanSDK.ControlPhysicalButton(ISCMetaScanSDK.PhysicalButton.Unlock);
    }
    private void Dialog_Pane_OldTIVA(String title,String content)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(content);

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                Dialog_Pane("Limited Functions","Running with older Tiva firmware\nis not recommended and functions\nwill be limited!");
            }
        });
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    //endregion
    //region Reset Config
    public class ResetConfigCompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
           finish();
        }
    }
    public void confirmationDialog() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.reset_config));
        alertDialogBuilder.setMessage("Reset device config will DELETE ALL CONFIGURATIONS on the device and RESTORE the default Configurations!");

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.reset_default), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setActivityTouchDisable(true);
                ISCMetaScanSDK.ResetConfig();
                alertDialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    //endregion
    //region Set Default Config
    private void SetDefaultConfig()
    {
        for(int i=0;i<listScanConfig.Configs.size();i++)
        {
            //get the first one byte
            int ScanConfigIndextoByte = (byte)listScanConfig.Configs.get(i).getScanConfigIndex();
            if(listScanConfig.Configs.get(i).getScanConfigIndex() == listScanConfig.DefaultConfigIndex || ScanConfigIndextoByte == listScanConfig.DefaultConfigIndex)
            {
                byte[] index = {0, 0};
                index[0] = (byte) listScanConfig.Configs.get(i).getScanConfigIndex();
                //the index over 256 should calculate index[1]
                index[1] = (byte) (listScanConfig.Configs.get(i).getScanConfigIndex()/256);
                ISCMetaScanSDK.SetDefaultConfig(index);
                CurrentScanConfig = listScanConfig.Configs.get(i);
                break;
            }
        }
    }
    //endregion
    private class  BackGroundReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }
    private class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {
        static final String SYSTEM_REASON = "reason";
        static final String SYSTEM_HOME_KEY = "homekey";
        static final String SYSTEM_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (reason != null) {
                    if (reason.equals(SYSTEM_HOME_KEY) || reason.equals(SYSTEM_RECENT_APPS)) {
                        finish();
                    }
                }
            }
        }
    }
}
