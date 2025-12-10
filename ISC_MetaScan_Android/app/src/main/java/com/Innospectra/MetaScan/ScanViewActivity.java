package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ISCMetaScanSDK.ISCMetaScanSDK;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.ISCMetaScanSDK.ISCMetaScanSDK.GetScanConfiguration;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.Interpret_intensity;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.Interpret_length;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.Interpret_uncalibratedIntensity;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.Interpret_wavelength;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.Reference_Info;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.SaveReference;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.Scan_Config_Info;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.getStringPref;
import static com.Innospectra.MetaScan.CommonAPI.ChangeScanConfigToByte;
import static com.Innospectra.MetaScan.CommonAPI.ErrorFlash;
import static com.Innospectra.MetaScan.CommonAPI.ShouldWarmUp;
import static com.Innospectra.MetaScan.CommonAPI.UpdateWarmUpTimeInList;
import static com.Innospectra.MetaScan.CommonAPI.WriteWarmUpData;
import static com.Innospectra.MetaScan.CommonStruct.AppStatus;
import static com.Innospectra.MetaScan.CommonStruct.CurrentScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.NewRefConfigName;
import static com.Innospectra.MetaScan.CommonStruct.NewReferenceIntensity;
import static com.Innospectra.MetaScan.CommonStruct.NewRefPGA;
import static com.Innospectra.MetaScan.CommonStruct.UserRefSetting;
import static com.Innospectra.MetaScan.CommonStruct.configSelectType;
import static com.Innospectra.MetaScan.CommonStruct.currentDeviceStatus;
import static com.Innospectra.MetaScan.CommonStruct.deviceInfo;
import static com.Innospectra.MetaScan.CommonStruct.exposure_time_vlaue;
import static com.Innospectra.MetaScan.CommonStruct.listScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.widthnm;
import static com.Innospectra.MetaScan.CommonStruct.widthnm_plus;
import static com.Innospectra.MetaScan.DeviceStatusViewActivity.GetLampTimeString;

public class ScanViewActivity extends Activity {
    Context mContext;
    ScrollView scrollview_scan;
    ViewPager viewpager;
    ProgressBar calProgress;
    TextView progressBarinsideText;
    Button bt_scan;
    Button bt_set_default_config;
    Button bt_save_config;
    TextView tv_setting;
    TextView tv_currentconfig;
    Button bt_scansetting;
    Button bt_devicesetting;
    RelativeLayout rl_refsetting;
    Button btn_new;
    Button btn_previous;
    Button btn_builtin;
    EditText et_repeat_scantimes;
    EditText et_scantime_interval;
    EditText et_prefix;
    RelativeLayout rl_devicesetting;
    Button bt_excutewarmup;
    TextView lb_lampmode;
    LinearLayout ly_lampmode;
    TextView lb_lampstabletime;
    TextView lb_pga;
    Button btn_auto;
    Button btn_on;
    Button btn_off;
    EditText et_lampstabletime;
    Spinner spinner_pga;
    AlertDialog alertDialog;
    Button led_connect;
    Button led_ble;
    Button led_scan;
    Button led_error;
    private ProgressDialog barProgressDialog;
    Dialog Continous_Dialog;
    TextView tv_continuous_scaninfo;

    ReferenceType referenceType = ReferenceType.BuiltIn;
    public enum ReferenceType{
        BuiltIn,
        New,
        Previous
    }

    ISCMetaScanSDK.ScanResults Scan_Spectrum_Data;
    ISCMetaScanSDK.LampState currentLampState = ISCMetaScanSDK.LampState.AUTO;
    int tabPosition = 0;
    long MesureScanTime=0;
    String CSV[][] = new String[35][15];
    List<String[]> CSVdata = new ArrayList<String[]>();
    String CSVPath = "";
    Boolean IsFirstDeviceSetting = true;
    private Boolean ShouldWarmUpBeforeScan = false;
    private Date FinishScanCurrentTime;
    private String GraphLabel = "ISC Scan";
    private ArrayList<Entry> mIntensityFloat = new ArrayList<>();
    private ArrayList<Entry> mAbsorbanceFloat = new ArrayList<>();
    private ArrayList<Entry> mReflectanceFloat = new ArrayList<>();
    private ArrayList<Entry> mReferenceFloat = new ArrayList<>();
    private int NumberOfConfig = 0;
    int receivedConfSize;
    Boolean InitScanSettingFlag = false;
    byte SetDefaultConfigIndex;
    private Boolean Exist = false;
    ContinousScanPara continousScanPara = new ContinousScanPara();
    class ContinousScanPara
    {
        public  Boolean ContinuousScanFlag = false;
        public  Boolean StopContinuous = false;
        public int TotalScan = 0;
        public int CurrentScanCount = 1;
        public int ScanInterval = 0;
    }

    private final BroadcastReceiver ScanDataReadyReceiver = new ScanDataReadyReceiver();
    private final BroadcastReceiver GetDeviceStatusReceiver = new GetDeviceStatusReceiver();
    private final BroadcastReceiver SetLampStateCompleteReceiver = new SetLampStateCompleteReceiver();
    private final BroadcastReceiver SetLampDelayTimeCompleteReceiver = new SetLampDelayTimeCompleteReceiver();
    private final BroadcastReceiver SetPGACompleteReceiver = new SetPGACompleteReceiver();
    private final BroadcastReceiver GetPGAReceiver = new GetPGAReceiver();
    private final BroadcastReceiver SetActiveConfigCompleteReceiver = new SetActiveConfigCompleteReceiver();
    private final BroadcastReceiver WriteScanConfigStatusReceiver = new WriteScanConfigStatusReceiver();
    private final BroadcastReceiver ScanConfSizeReceiver = new ScanConfSizeReceiver();
    private final BroadcastReceiver GetActiveScanConfReceiver = new GetActiveScanConfReceiver();
    private final BroadcastReceiver ScanConfReceiver = new ScanConfReceiver();
    private final BroadcastReceiver ReturnLampRampUpADCReceiver = new ReturnLampRampUpADCReceiver();
    private final BroadcastReceiver ReturnLampADCAverageReceiver = new ReturnLampADCAverageReceiver();
    private final BroadcastReceiver ScanStartedReceiver = new ScanStartedReceiver();//Physical button scan receiver
    private final BroadcastReceiver ResetConfigCompleteReceiver = new ResetConfigCompleteReceiver();
    private final BroadcastReceiver DisconnectReceiver = new DisconnectReceiver();
    private final BroadcastReceiver HomeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();
    private final BroadcastReceiver BackgroundReciver = new BackGroundReciver();

    private final IntentFilter ScanDataReadyFilter = new IntentFilter(ISCMetaScanSDK.SCAN_DATA);
    private final IntentFilter GetDeviceStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_STATUS);
    private final IntentFilter SetLampStateCompleteFilter = new IntentFilter(ISCMetaScanSDK.SET_LAMPSTATE_COMPLETE);
    private final IntentFilter SetLampDelayTimeCompleteFilter = new IntentFilter(ISCMetaScanSDK.SET_LAMPDELAYTIME_COMPLETE);
    private final IntentFilter SetPGACompleteFilter = new IntentFilter(ISCMetaScanSDK.SET_PGA_COMPLETE);
    private final IntentFilter GetPGAFilter = new IntentFilter(ISCMetaScanSDK.SEND_PGA);
    private final IntentFilter SetActiveConfigCompleteFilter = new IntentFilter(ISCMetaScanSDK.SET_ACTIVECONFIG_COMPLETE);
    private final IntentFilter WriteScanConfigStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_RETURN_WRITE_SCAN_CONFIG_STATUS);
    private final IntentFilter GetNumberOfScanConfFilter = new IntentFilter(ISCMetaScanSDK.SCAN_CONF_SIZE);
    private final IntentFilter ScanConfFilter = new IntentFilter(ISCMetaScanSDK.SCAN_CONF_DATA);
    private final IntentFilter GetActiveScanConfFilter = new IntentFilter(ISCMetaScanSDK.SEND_ACTIVE_CONF);
    private final IntentFilter ReturnLampRampUpFilter = new IntentFilter(ISCMetaScanSDK.ACTION_RETURN_LAMP_RAMPUP_ADC);
    private final IntentFilter ReturnLampADCAverageFilter = new IntentFilter(ISCMetaScanSDK.ACTION_RETURN_LAMP_AVERAGE_ADC);
    private final IntentFilter ScanStartedFilter = new IntentFilter(ISCMetaScanSDK.ACTION_SCAN_STARTED);
    private final IntentFilter ResetConfigCompleteFilter = new IntentFilter(ISCMetaScanSDK.RESET_SCANCONFIG_COMPLETE);
    private final IntentFilter DisconnectFilter = new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED);
    public static final String NOTIFY_BACKGROUND = "com.Innospectra.MetaScan.ScanViewActivity.NotifyBackground";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.activity_scan_view);
        mContext = this;
        Exist = false;
       /* ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }*/
        setActivityTouchDisable(true);
        InitComponent();
        InitialGUI();
        ViewPagerEvent();
        UpdateWarmUpStatusButton(false);
        InivialViewPager();
        if(CommonStruct.device_supportfunction.OldVersion)
            ISCMetaScanSDK.getErrorStatus();
        else
            ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
        currentLampState = ISCMetaScanSDK.LampState.AUTO;

        String Device = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
        Device = Device.replace(":","_");
        String folderPath = Environment.DIRECTORY_DOCUMENTS + "/MetaScan_Reference/";
        String csvName = Device + "_";
        int numSections = CurrentScanConfig.getSlewNumSections();
        for(int i=0;i<numSections;i++)
        {
            csvName = csvName + CurrentScanConfig.getSectionNumRepeats()[i] + "_" +CurrentScanConfig.getSectionScanType()[i] + "_" + (CurrentScanConfig.getSectionWavelengthStartNm()[i] & 0xFFFF) + "_" + (CurrentScanConfig.getSectionWavelengthEndNm()[i] & 0xFFFF)
                    + "_" +  CurrentScanConfig.getSectionWidthPx()[i] + "_" + (CurrentScanConfig.getSectionNumPatterns()[i] & 0x0FFF) + "_" + CurrentScanConfig.getSectionExposureTime()[i] + ".csv";
        }
        if(IsLocalReference(csvName))
        {
            GetReferenceValue(csvName);
            btn_previous.setEnabled(true);
            btn_previous.setTextColor(Color.BLACK);
        }

        LocalBroadcastManager.getInstance(mContext).registerReceiver(ScanDataReadyReceiver, ScanDataReadyFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetDeviceStatusReceiver,GetDeviceStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SetLampStateCompleteReceiver, SetLampStateCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SetLampDelayTimeCompleteReceiver,SetLampDelayTimeCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SetPGACompleteReceiver, SetPGACompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetPGAReceiver, GetPGAFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SetActiveConfigCompleteReceiver, SetActiveConfigCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(WriteScanConfigStatusReceiver, WriteScanConfigStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ScanConfSizeReceiver, GetNumberOfScanConfFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ScanConfReceiver, ScanConfFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetActiveScanConfReceiver, GetActiveScanConfFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ReturnLampRampUpADCReceiver, ReturnLampRampUpFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ReturnLampADCAverageReceiver, ReturnLampADCAverageFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ResetConfigCompleteReceiver, ResetConfigCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ScanStartedReceiver, ScanStartedFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(DisconnectReceiver, DisconnectFilter);
        registerReceiver(HomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), Context.RECEIVER_NOT_EXPORTED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(BackgroundReciver, new IntentFilter(NOTIFY_BACKGROUND));
    }
    @Override
    public void onResume() {
        super.onResume();
        if(AppStatus == CommonStruct.APPStatus.Home)
            finish();
        try {
            UpdateWarmUpStatusButton(false);
        }catch (Exception e)
        {

        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanDataReadyReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetDeviceStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetLampStateCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetLampDelayTimeCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetPGACompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetPGAReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetActiveConfigCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(WriteScanConfigStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanConfSizeReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanConfReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetActiveScanConfReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnLampRampUpADCReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnLampADCAverageReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ResetConfigCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ScanStartedReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DisconnectReceiver);
        unregisterReceiver(HomeKeyEventReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(BackgroundReciver);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Exist = true;
            ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
        }
        return super.onOptionsItemSelected(item);
    }
    //region UI and Event
    private void StartScanUI()
    {
        scrollview_scan.fullScroll(ScrollView.FOCUS_UP);
        setActivityTouchDisable(true);
        led_scan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.led_y, 0, 0, 0);
        if(continousScanPara.ContinuousScanFlag)
            ContinousScan_Dialog();
        else
        {
            calProgress.setVisibility(View.VISIBLE);
            progressBarinsideText.setVisibility(View.VISIBLE);
            progressBarinsideText.setText("Scanning ...");
        }
    }
    private void EndScanUI()
    {
        setActivityTouchDisable(false);
        if(NewReferenceIntensity.size()>0)
        {
            btn_previous.setEnabled(true);
            btn_previous.setTextColor(Color.BLACK);
            if(referenceType == ReferenceType.New)
                ReferencePrevious();
        }
        if(continousScanPara.ContinuousScanFlag)
        {
            String Title;
            if(continousScanPara.StopContinuous)
                Title = "Scan Stopped by User";
            else
                Title = "Auto Continuous Scan Completed";
            Continous_Dialog.dismiss();
            ContinuousCompleteDialog(Title);
        }
        else
        {
            calProgress.setVisibility(View.GONE);
            progressBarinsideText.setVisibility(View.GONE);
        }
        InitScanSettingFlag = false;
    }
    private void setActivityTouchDisable(boolean value) {
        if (value) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    private void InivialViewPager()
    {
        //Initialize view pager
        CustomPagerAdapter pagerAdapter = new CustomPagerAdapter(this);
        viewpager.setAdapter(pagerAdapter);
        viewpager.invalidate();
        viewpager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        ActionBar ab = getActionBar();
                        if (ab != null) {
                            getActionBar().setSelectedNavigationItem(position);
                        }
                    }
                });
        viewpager.setCurrentItem(1);
    }
    private void InitialGUI()
    {
        switch (configSelectType)
        {
            case QuickScan:
                QuickScanUI();
                break;
            case SelectConfig:
                SelectConfigUI();
                break;
            case SetConfig:
                SetConfigUI();
                break;
        }
        if(CommonStruct.device_supportfunction.OldVersion || !deviceInfo.IsActivated)
            DisableDeviceSettingUI();
    }
    private void DisableDeviceSettingUI()
    {
        lb_lampmode.setVisibility(View.GONE);
        ly_lampmode.setVisibility(View.GONE);
        lb_lampstabletime.setVisibility(View.GONE);
        et_lampstabletime.setVisibility(View.GONE);
        lb_pga.setVisibility(View.GONE);
        spinner_pga.setVisibility(View.GONE);
    }
    private void QuickScanUI()
    {
        bt_set_default_config.setVisibility(View.GONE);
        tv_currentconfig.setText("(Config : " + listScanConfig.DefaultConfigName + ")");
    }
    private void SelectConfigUI()
    {
        if((byte)CurrentScanConfig.getScanConfigIndex() == listScanConfig.DefaultConfigIndex)
            bt_set_default_config.setVisibility(View.GONE);
        else
            bt_set_default_config.setVisibility(View.VISIBLE);
        tv_currentconfig.setText("(Config : " + CurrentScanConfig.getConfigName() + ")");
    }
    private void SetConfigUI()
    {
        LayoutBelowSaveConfigButton();
        bt_set_default_config.setVisibility(View.GONE);
        bt_save_config.setVisibility(View.VISIBLE);
        tv_currentconfig.setText("(Config : " + CurrentScanConfig.getConfigName() + ")");
    }
    private void LayoutBelowSaveConfigButton()
    {
        RelativeLayout.LayoutParams tv_setting_params = (RelativeLayout.LayoutParams) tv_setting.getLayoutParams();
        tv_setting_params.addRule(RelativeLayout.BELOW, R.id.bt_save_config);
        RelativeLayout.LayoutParams tv_currentconfig_params = (RelativeLayout.LayoutParams) tv_currentconfig.getLayoutParams();
        tv_currentconfig_params.addRule(RelativeLayout.BELOW, R.id.bt_save_config);
        tv_currentconfig.setLayoutParams(tv_currentconfig_params);
        tv_setting.setLayoutParams(tv_setting_params);
    }
    private void LayoutBelowSetActiveButton()
    {
        RelativeLayout.LayoutParams tv_setting_params = (RelativeLayout.LayoutParams) tv_setting.getLayoutParams();
        tv_setting_params.addRule(RelativeLayout.BELOW, R.id.bt_set_default_config);
        RelativeLayout.LayoutParams tv_currentconfig_params = (RelativeLayout.LayoutParams) tv_currentconfig.getLayoutParams();
        tv_currentconfig_params.addRule(RelativeLayout.BELOW, R.id.bt_set_default_config);
        tv_currentconfig.setLayoutParams(tv_currentconfig_params);
        tv_setting.setLayoutParams(tv_setting_params);
    }
    private void ViewPagerEvent()
    {
        //Set up title bar and  enable tab navigation
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(getString(R.string.scan));
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            viewpager = (ViewPager) findViewById(R.id.viewpager);
            viewpager.setOffscreenPageLimit(2);

            // Create a tab listener that is called when the user changes tabs.
            ActionBar.TabListener tl = new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    viewpager.setCurrentItem(tab.getPosition());
                }
                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                }
                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                }
            };
            // Add 3 tabs, specifying the tab's text and TabListener
            for (int i = 0; i < 4; i++) {
                ab.addTab(
                        ab.newTab()
                                .setText(getResources().getStringArray(R.array.graph_tab_index)[i])
                                .setTabListener(tl));
            }
        }
    }
    private void UpdateWarmUpStatusButton(Boolean PressScan)
    {
        ShouldWarmUpBeforeScan = false;
        if(currentDeviceStatus.ForceWarmUp)
        {
            if(ShouldWarmUp())
            {
                ShouldWarmUpBeforeScan = true;
                bt_excutewarmup.setTextColor(getResources().getColor(R.color.red));
                bt_excutewarmup.setBackground(getDrawable(R.drawable.button_style_not_warmup));
                if(PressScan)
                    ConfirmWarmUpDialog();
            }
            else
            {
                bt_excutewarmup.setTextColor(getResources().getColor(R.color.royalblue));
                bt_excutewarmup.setBackground(getDrawable(R.drawable.button_style));
            }
        }
        else
        {
            bt_excutewarmup.setTextColor(getResources().getColor(R.color.black));
            bt_excutewarmup.setBackground(getDrawable(R.drawable.button_style_diable_warmup));
        }
    }
    private void UpdateWarmUpTime()
    {
        Calendar c = Calendar.getInstance();
        currentDeviceStatus.WarmUpTime = c.getTime();
        UpdateWarmUpTimeInList();
        WriteWarmUpData();
        if(currentDeviceStatus.ForceWarmUp)
        {
            bt_excutewarmup.setTextColor(getResources().getColor(R.color.royalblue));
            bt_excutewarmup.setBackground(getDrawable(R.drawable.button_style));
        }
        else
        {
            bt_excutewarmup.setTextColor(getResources().getColor(R.color.black));
            bt_excutewarmup.setBackground(getDrawable(R.drawable.button_style_diable_warmup));
        }
    }
    public void ConfirmWarmUpDialog() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("Warm Up?");
        alertDialogBuilder.setMessage("The device has been idle for more than 30 minutes and needs to warm up!");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(mContext, WarmUpViewActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void InitComponent()
    {
        scrollview_scan = (ScrollView)findViewById(R.id.scrollview_scan);
        calProgress = (ProgressBar)findViewById(R.id.calProgress);
        progressBarinsideText = (TextView)findViewById(R.id.progressBarinsideText);
        bt_scan = (Button)findViewById(R.id.bt_scan);
        bt_set_default_config = (Button)findViewById(R.id.bt_set_default_config);
        bt_save_config = (Button)findViewById(R.id.bt_save_config);
        tv_setting = (TextView)findViewById(R.id.tv_setting);
        tv_currentconfig = (TextView)findViewById(R.id.tv_currentconfig);
        bt_scansetting = (Button)findViewById(R.id.bt_scansetting);
        bt_devicesetting = (Button)findViewById(R.id.bt_devicesetting);
        rl_refsetting = (RelativeLayout) findViewById(R.id.rl_refsetting);
        btn_new = (Button)findViewById(R.id.btn_new);
        btn_previous = (Button)findViewById(R.id.btn_previous);
        btn_builtin = (Button)findViewById(R.id.btn_builtin);
        et_repeat_scantimes = (EditText) findViewById(R.id.et_repeat_scantimes);
        et_scantime_interval = (EditText) findViewById(R.id.et_scantime_interval);
        et_prefix = (EditText) findViewById(R.id.et_prefix);
        rl_devicesetting = (RelativeLayout) findViewById(R.id.rl_devicesetting);
        bt_excutewarmup = (Button) findViewById(R.id.bt_excutewarmup);
        lb_lampmode = (TextView)findViewById(R.id.lb_lampmode);
        ly_lampmode = (LinearLayout)findViewById(R.id.ly_lampmode);
        btn_auto = (Button)findViewById(R.id.btn_auto);
        btn_on = (Button)findViewById(R.id.btn_on);
        btn_off = (Button)findViewById(R.id.btn_off);
        lb_lampstabletime = (TextView)findViewById(R.id.lb_lampstabletime);
        et_lampstabletime = (EditText) findViewById(R.id.et_lampstabletime);
        lb_pga = (TextView)findViewById(R.id.lb_pga);
        spinner_pga = (Spinner) findViewById(R.id.spinner_pga);
        led_connect = (Button)findViewById(R.id.led_connect);
        led_ble = (Button)findViewById(R.id.led_ble);
        led_scan = (Button)findViewById(R.id.led_scan);
        led_error = (Button)findViewById(R.id.led_error);

        bt_set_default_config.setOnClickListener(Button_Listener);
        bt_save_config.setOnClickListener(Button_Listener);
        bt_scansetting.setOnClickListener(Button_Listener);
        bt_devicesetting.setOnClickListener(Button_Listener);
        bt_excutewarmup.setOnClickListener(Button_Listener);
        btn_auto.setOnClickListener(Button_Listener);
        btn_on.setOnClickListener(Button_Listener);
        btn_off.setOnClickListener(Button_Listener);
        btn_new.setOnClickListener(Button_Listener);
        btn_previous.setOnClickListener(Button_Listener);
        btn_builtin.setOnClickListener(Button_Listener);
        bt_scan.setOnClickListener(Button_Listener);
        et_lampstabletime.setOnEditorActionListener(LampStableTime_OnEditor);
        spinner_pga.setOnItemSelectedListener(PGA_ItemSelect);
        et_repeat_scantimes.setOnEditorActionListener(NumberOfContinuousScan_OnEditor);
        et_scantime_interval.setOnEditorActionListener(ScanTimeInterval_OnEditor);
        tv_currentconfig.setOnClickListener(TextView_Listener);

        ArrayAdapter<CharSequence> adapter_pga = ArrayAdapter.createFromResource(this,
                R.array.pga_array, android.R.layout.simple_spinner_item);
        adapter_pga.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_pga.setAdapter(adapter_pga);

        bt_scansetting.setSelected(true);
        btn_new.setSelected(true);
        btn_auto.setSelected(true);
        btn_previous.setEnabled(false);

        AnimationDrawable ani = (AnimationDrawable)getResources().getDrawable(R.drawable.led_flashing);
        led_ble.setCompoundDrawablesWithIntrinsicBounds( ani, null, null, null);
        ani.start();
        led_connect.setOnClickListener(Button_Listener);
        led_ble.setOnClickListener(Button_Listener);
        led_scan.setOnClickListener(Button_Listener);
        led_error.setOnClickListener(Button_Listener);

        if(!NewRefConfigName.isEmpty() && NewRefConfigName.equals(CurrentScanConfig.getConfigName()))
        {
            btn_previous.setEnabled(true);
            btn_previous.setTextColor(Color.BLACK);
            referenceType = ReferenceType.Previous;
            ReferencePrevious();
        }
        else
        {
            NewReferenceIntensity.clear();
            NewRefConfigName = "";
        }
    }
    private Button.OnClickListener Button_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId())
            {
                case R.id.bt_set_default_config:
                    SetActiveConfig();
                    bt_set_default_config.setVisibility(View.GONE);
                    break;
                case R.id.bt_save_config:
                    if(listScanConfig.Configs.size() >= 20)
                        ConfigOutofRangeDialog();
                    else
                        SaveScanConfig();
                    break;
                case R.id.bt_scansetting:
                    ShowScanSetting();
                    break;
                case R.id.bt_devicesetting:
                    ShowDeviceSetting();
                    break;
                case R.id.bt_excutewarmup :
                    intent = new Intent(mContext, WarmUpViewActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_auto:
                    et_lampstabletime.setEnabled(true);
                    LampAuto();
                    break;
                case R.id.btn_on:
                    et_lampstabletime.setEnabled(false);
                    LampOn();
                    UpdateWarmUpTime();
                    break;
                case R.id.btn_off:
                    et_lampstabletime.setEnabled(false);
                    LampOff();
                    break;
                case R.id.btn_new:
                    ReferenceNew();
                    break;
                case R.id.btn_previous:
                    ReferencePrevious();
                    break;
                case R.id.btn_builtin:
                    ReferenceBuiltIn();
                    break;
                case R.id.bt_scan:
                    UpdateWarmUpStatusButton(true);
                    if(!ShouldWarmUpBeforeScan)
                    {
                        InitScanSettingFlag = true;
                        InitialContinuousScanPara();
                        SetReferenceType();
                        GetContinuousScanPara();
                        StartScanUI();
                        ISCMetaScanSDK.StartScan();
                    }
                    break;
                case R.id.led_connect:
                case R.id.led_ble:
                case R.id.led_scan:
                case R.id.led_error:
                    intent = new Intent(mContext, DeviceStatusViewActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
    private TextView.OnClickListener TextView_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            Intent intent;
            intent = new Intent(mContext, CurrentConfigViewActivity.class);
            startActivity(intent);
        }
    };
    private Spinner.OnItemSelectedListener PGA_ItemSelect = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            SetPGAToDevice();
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private EditText.OnEditorActionListener LampStableTime_OnEditor = new EditText.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (TextUtils.isEmpty(et_lampstabletime.getText()))
                {
                    Dialog_Pane("Warning","Lamp-Stable Time should be larger or equal to 0.",false);
                    et_lampstabletime.setText("625");
                }
                else
                {
                    setActivityTouchDisable(true);
                    int lampstabletime = Integer.parseInt(et_lampstabletime.getText().toString());
                    ISCMetaScanSDK.SetLampStableTime(lampstabletime);
                }
                return false; // consume.
            }
            return false;
        }
    };
    private EditText.OnEditorActionListener NumberOfContinuousScan_OnEditor = new EditText.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (TextUtils.isEmpty(et_repeat_scantimes.getText()))
                {
                    Dialog_Pane("Warning","The number of auto continuous scan repeats should be larger or equal to 0.",false);
                    et_repeat_scantimes.setText("0");
                }
            }
            return false;
        }
    };
    private EditText.OnEditorActionListener ScanTimeInterval_OnEditor = new EditText.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (TextUtils.isEmpty(et_scantime_interval.getText()))
                {
                    Dialog_Pane("Warning","Scan interval times setting should be larger or equal to 0.",false);
                    et_scantime_interval.setText("0");
                }
            }
            return false;
        }
    };
    private void ShowScanSetting()
    {
        bt_scansetting.setSelected(true);
        bt_devicesetting.setSelected(false);
        rl_refsetting.setVisibility(View.VISIBLE);
        rl_devicesetting.setVisibility(View.INVISIBLE);
    }
    private void ShowDeviceSetting()
    {
        bt_scansetting.setSelected(false);
        bt_devicesetting.setSelected(true);
        rl_refsetting.setVisibility(View.INVISIBLE);
        rl_devicesetting.setVisibility(View.VISIBLE);

    }
    private void LampAuto()
    {
        setActivityTouchDisable(true);
        btn_auto.setSelected(true);
        btn_on.setSelected(false);
        btn_off.setSelected(false);
        ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
        currentLampState = ISCMetaScanSDK.LampState.AUTO;
    }
    private void LampOn()
    {
        setActivityTouchDisable(true);
        btn_auto.setSelected(false);
        btn_on.setSelected(true);
        btn_off.setSelected(false);
        ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.ON);
        currentLampState = ISCMetaScanSDK.LampState.ON;
    }
    private void LampOff()
    {
        setActivityTouchDisable(true);
        btn_auto.setSelected(false);
        btn_on.setSelected(false);
        btn_off.setSelected(true);
        ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.OFF);
        currentLampState = ISCMetaScanSDK.LampState.OFF;
    }
    private void ReferenceNew()
    {
        btn_new.setSelected(true);
        btn_previous.setSelected(false);
        btn_builtin.setSelected(false);
        bt_scan.setText(getResources().getText(R.string.ref_scan));
    }
    private void ReferencePrevious()
    {
        btn_new.setSelected(false);
        btn_previous.setSelected(true);
        btn_builtin.setSelected(false);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner_pga.getAdapter();
        int position = adapter.getPosition(Integer.toString(NewRefPGA));
        spinner_pga.setSelection(position);
        bt_scan.setText(getResources().getText(R.string.scan));
    }
    private void ReferenceBuiltIn()
    {
        btn_new.setSelected(false);
        btn_previous.setSelected(false);
        btn_builtin.setSelected(true);
        bt_scan.setText(getResources().getText(R.string.scan));
    }
    private void Dialog_Pane(String title, String content, final Boolean IsFinish)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(content);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                if(IsFinish)
                    finish();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void Save_Fail_Dialog_Pane(String title,String content)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(content);

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                bt_save_config.setEnabled(true);
                calProgress.setVisibility(View.GONE);
                progressBarinsideText.setVisibility(View.GONE);
                setActivityTouchDisable(false);
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    //endregion
    public class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(mContext, R.string.nano_disconnected, Toast.LENGTH_SHORT).show();
            AppStatus = CommonStruct.APPStatus.Home;
            finish();
        }
    }
    //region Device Setting
    public class SetLampStateCompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if(Exist)
                finish();
            else if(IsFirstDeviceSetting)
            {
                int lampstabletime = Integer.parseInt(et_lampstabletime.getText().toString());
                ISCMetaScanSDK.SetLampStableTime(lampstabletime);
            }
            else
                setActivityTouchDisable(false);
        }
    }
    public class SetLampDelayTimeCompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if(IsFirstDeviceSetting)
                SetPGAToDevice();
            else
                setActivityTouchDisable(false);
        }
    }
    public class SetPGACompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if(IsFirstDeviceSetting)
                ISCMetaScanSDK.getErrorStatus();
            /*else  if(CommonStruct.device_supportfunction.SupportGetPGA)
                ISCMetaScanSDK.GetPGA();*/
            else
                setActivityTouchDisable(false);
        }
    }
    public class GetPGAReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            byte buf[] = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_PGA);
            String pgavalue = spinner_pga.getSelectedItem().toString();
            int pga;
            if(pgavalue.contains("Auto"))
            {
                setActivityTouchDisable(false);
                return;
            }
            else
                pga = Integer.parseInt(spinner_pga.getSelectedItem().toString());
            int getpga = (int) buf[0];
            if(pga != getpga)
                Dialog_Pane("Fail", "Set PGA : " + pga,false);
            setActivityTouchDisable(false);
        }
    }
    public class SetActiveConfigCompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            listScanConfig.DefaultConfigName = CurrentScanConfig.getConfigName();
            listScanConfig.DefaultConfigIndex = SetDefaultConfigIndex;
        }
    }
    public class WriteScanConfigStatusReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            byte status[] = intent.getByteArrayExtra(ISCMetaScanSDK.RETURN_WRITE_SCAN_CONFIG_STATUS);
            if((int)status[0] == 1)
            {
                if((int)status[1] == 1)
                {
                    calProgress.setVisibility(View.GONE);
                    progressBarinsideText.setVisibility(View.GONE);
                    ISCMetaScanSDK.GetNumberOfScanConfig();
                }
                else
                    Save_Fail_Dialog_Pane("Fail","Save configuration fail!");
            }
            else if((int)status[0] == -1)
                Save_Fail_Dialog_Pane("Fail","Save configuration fail!");
            else if((int)status[0] == -2)
                Save_Fail_Dialog_Pane("Fail","Save configuration fail! Hardware not compatible!");
            else if((int)status[0] == -3)
                Save_Fail_Dialog_Pane("Fail","Save configuration fail! Function is currently locked!" );
        }
    }
    /**
     * Send broadcast  GET_SCAN_CONF will  through ScanConfSizeReceiver to get the number of scan config(ISCMetaScanSDK.GetScanConfig() should be claaed)
     */
    private class ScanConfSizeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NumberOfConfig = intent.getIntExtra(ISCMetaScanSDK.EXTRA_CONF_SIZE, 0);
            if(NumberOfConfig <=2)
            {
                AppStatus = CommonStruct.APPStatus.Home;
                Dialog_Pane("Warning","The device will disconnect and back to home.",true);
            }
            else
            {
                listScanConfig.ScanConfigName = new String[NumberOfConfig];
                listScanConfig.Configs.clear();
                ISCMetaScanSDK.requestStoredConfigurationList();
                if (NumberOfConfig > 0) {
                    barProgressDialog = new ProgressDialog(ScanViewActivity.this);
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
     * Send broadcast  GET_ACTIVE_CONF will  through GetActiveScanConfReceiver to get active config(ISCMetaScanSDK.GetActiveConfig() should be called)
     */
    private class GetActiveScanConfReceiver extends BroadcastReceiver {
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
            bt_save_config.setVisibility(View.GONE);
            LayoutBelowSetActiveButton();
            bt_set_default_config.setVisibility(View.VISIBLE);
            setActivityTouchDisable(false);
        }
    }
    //region
    //region Scan
    public class ScanStartedReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if(!InitScanSettingFlag)
            {
                InitialContinuousScanPara();
                SetReferenceType();
                GetContinuousScanPara();
                StartScanUI();
                InitScanSettingFlag = true;
            }
        }
    }
    public class ScanDataReadyReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            led_scan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.leg_gray, 0, 0, 0);
            if(currentLampState != ISCMetaScanSDK.LampState.OFF)
                UpdateWarmUpTime();
            long endtime = System.currentTimeMillis();
            MesureScanTime = endtime - ISCMetaScanSDK.startScanTime;
            FinishScanCurrentTime = new Date();
            if(Interpret_length<=0)
                Dialog_Pane("Error","The scan interpret fail. Please check your device.",true);
            else
            {
                Scan_Spectrum_Data = new ISCMetaScanSDK.ScanResults(Interpret_wavelength,Interpret_intensity,Interpret_uncalibratedIntensity,Interpret_length);
                mIntensityFloat.clear();
                mAbsorbanceFloat.clear();
                mReflectanceFloat.clear();
                mReferenceFloat.clear();
                int index;
                if(referenceType == ReferenceType.Previous)
                {
                    for (index = 0; index < Scan_Spectrum_Data.getLength(); index++) {
                        int RefVal;
                        if(NewRefPGA != Scan_Config_Info.pga[0])
                            RefVal = GetRef(Scan_Config_Info.pga[0], NewReferenceIntensity.get(index));
                        else
                            RefVal = NewReferenceIntensity.get(index);
                        mReferenceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(float)RefVal));
                        mIntensityFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(float) Scan_Spectrum_Data.getUncalibratedIntensity()[index]));
                        mAbsorbanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(-1) * (float) Math.log10((double) Scan_Spectrum_Data.getUncalibratedIntensity()[index] / (double)RefVal)));
                        mReflectanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(float) Scan_Spectrum_Data.getUncalibratedIntensity()[index] / RefVal));
                    }
                }
                else //Built-in or New
                {
                    if(referenceType == ReferenceType.New)
                    {
                        NewReferenceIntensity.clear();
                        NewRefPGA = Scan_Config_Info.pga[0];
                        NewRefConfigName = CurrentScanConfig.getConfigName();
                    }
                    for (index = 0; index < Scan_Spectrum_Data.getLength(); index++) {
                        mIntensityFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(float) Scan_Spectrum_Data.getUncalibratedIntensity()[index]));
                        if(referenceType == ReferenceType.New)
                        {
                            NewReferenceIntensity.add(Scan_Spectrum_Data.getUncalibratedIntensity()[index]);
                            mAbsorbanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],0));
                            mReflectanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],1));
                            mReferenceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(float) Scan_Spectrum_Data.getUncalibratedIntensity()[index]));
                        }
                        else
                        {
                            mAbsorbanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(-1) * (float) Math.log10((double) Scan_Spectrum_Data.getUncalibratedIntensity()[index] / (double) Scan_Spectrum_Data.getIntensity()[index])));
                            mReflectanceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(float) Scan_Spectrum_Data.getUncalibratedIntensity()[index] / Scan_Spectrum_Data.getIntensity()[index]));
                            mReferenceFloat.add(new Entry((float) Scan_Spectrum_Data.getWavelength()[index],(float) Scan_Spectrum_Data.getIntensity()[index]));
                        }
                    }
                }
                tabPosition = viewpager.getCurrentItem();
                viewpager.setAdapter(viewpager.getAdapter());
                viewpager.invalidate();
                viewpager.setCurrentItem(tabPosition);
                ISCMetaScanSDK.GetDeviceStatus();
            }
        }
    }
    private int GetRef(int ScanPga,int NewRef)
    {
        return (int)Math.round(((double)NewRef / NewRefPGA) * ScanPga);
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
            if(IsFirstDeviceSetting)
            {
                IsFirstDeviceSetting = false;
                calProgress.setVisibility(View.GONE);
                progressBarinsideText.setVisibility(View.GONE);
                setActivityTouchDisable(false);
            }
            else
            {
                deviceInfo.Battery = Integer.toString( intent.getIntExtra(ISCMetaScanSDK.EXTRA_BATT, 0));
                long lamptime = intent.getLongExtra(ISCMetaScanSDK.EXTRA_LAMPTIME,0);
                deviceInfo.TotalLampTime = GetLampTimeString(lamptime);
                deviceInfo.Devbyte = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_DEV_STATUS_BYTE);
                deviceInfo.Errbyte = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_ERR_BYTE);
                if(CommonStruct.device_supportfunction.SupportReadADC)
                    ISCMetaScanSDK.GetScanLampRampUpADC();
                else
                {
                    if(referenceType != ReferenceType.New)
                        WriteCSV();
                    else
                        SetReferenceConfigValue();
                    if(referenceType == ReferenceType.New)
                        WriteReference();
                    if(continousScanPara.ContinuousScanFlag)
                    {
                        if(continousScanPara.StopContinuous || continousScanPara.TotalScan == continousScanPara.CurrentScanCount)
                            EndScanUI();
                        else
                        {
                            continousScanPara.CurrentScanCount ++;
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable(){

                                @Override
                                public void run() {
                                    tv_continuous_scaninfo.setText("Auto Scanning ...(" + continousScanPara.CurrentScanCount + " / " + continousScanPara.TotalScan + ")" + "\n" + "Scan Interval Time : " + continousScanPara.ScanInterval + " sec");
                                    ISCMetaScanSDK.StartScan();
                                }}, continousScanPara.ScanInterval*1000);
                        }
                    }
                    else
                        EndScanUI();
                }
            }
        }
    }
    /**
     *Get lamp ramp up adc data (ISCMetaScanSDK.GetScanLampRampUpADC() should be called)
     */
    private byte Lamp_RAMPUP_ADC_DATA[];
    public class ReturnLampRampUpADCReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            Lamp_RAMPUP_ADC_DATA = intent.getByteArrayExtra(ISCMetaScanSDK.LAMP_RAMPUP_DATA);
            ISCMetaScanSDK.GetLampADCAverage();
        }
    }
    /**
     *Get lamp average adc data (ISCMetaScanSDK.GetLampADCAverage() should be called)
     */
    private byte Lamp_AVERAGE_ADC_DATA[];
    public class ReturnLampADCAverageReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            Lamp_AVERAGE_ADC_DATA = intent.getByteArrayExtra(ISCMetaScanSDK.LAMP_ADC_AVERAGE_DATA);
            if(referenceType != ReferenceType.New)
                WriteCSV();
            else
                SetReferenceConfigValue();
            if(continousScanPara.ContinuousScanFlag)
            {
                if(continousScanPara.StopContinuous || continousScanPara.TotalScan == continousScanPara.CurrentScanCount)
                    EndScanUI();
                else
                {
                    continousScanPara.CurrentScanCount ++;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){

                        @Override
                        public void run() {
                            tv_continuous_scaninfo.setText("Auto Scanning ...(" + continousScanPara.CurrentScanCount + " / " + continousScanPara.TotalScan + ")" + "\n" + "Scan Interval Time : " + continousScanPara.ScanInterval + " sec");
                            ISCMetaScanSDK.StartScan();
                        }}, continousScanPara.ScanInterval*1000);
                }
            }
            else
                EndScanUI();
        }
    }
    private void  SetReferenceType()
    {
        if(btn_new.isSelected())
            referenceType = ReferenceType.New;
        else if(btn_previous.isSelected())
            referenceType = ReferenceType.Previous;
        else
            referenceType = ReferenceType.BuiltIn;
    }
    private void InitUserRefSettingValue()
    {
        UserRefSetting.ConfigName = "";
        UserRefSetting.Type = "";
        UserRefSetting.Section = "";
        UserRefSetting.ConfigType = "";
        UserRefSetting.StartWav = "";
        UserRefSetting.EndWav = "";
        UserRefSetting.Width = "";
        UserRefSetting.ExposureTime = "";
        UserRefSetting.Resolution = "";
        UserRefSetting.NumRepeat = "";
        UserRefSetting.PGA = "";
        UserRefSetting.Systemp = "";
        UserRefSetting.Humidity = "";
        UserRefSetting.Systemp = "";
        UserRefSetting.CurrentTime = "";
    }
    private void SetReferenceConfigValue()
    {
        InitUserRefSettingValue();
        UserRefSetting.ConfigName = "User Reference";
        UserRefSetting.Type = "Slew";
        UserRefSetting.Section = Integer.toString(Scan_Config_Info.numSections[0]);

        int sectionNum = Scan_Config_Info.numSections[0];
        int index;
        double temp;
        double humidity;
        for(int i=0;i<sectionNum;i++)
        {
            if(i!=0)
            {
                UserRefSetting.ConfigType+=",";
                UserRefSetting.StartWav += ",";
                UserRefSetting.EndWav += ",";
                UserRefSetting.Width+=",";
                UserRefSetting.ExposureTime += ",";
                UserRefSetting.Resolution += ",";
            }
            if(Scan_Config_Info.sectionScanType[i] ==0)
                UserRefSetting.ConfigType += "Column";
            else
                UserRefSetting.ConfigType += "Hadamard";
            UserRefSetting.StartWav += Scan_Config_Info.sectionWavelengthStartNm[i];
            UserRefSetting.EndWav += Scan_Config_Info.sectionWavelengthEndNm[i];
            index = Scan_Config_Info.sectionWidthPx[i];
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                UserRefSetting.Width += widthnm_plus[index];
            else
                UserRefSetting.Width += widthnm[index];
            index = Scan_Config_Info.sectionExposureTime[i];
            UserRefSetting.ExposureTime += exposure_time_vlaue[index];
            UserRefSetting.Resolution += Scan_Config_Info.sectionNumPatterns[i];
        }
        UserRefSetting.NumRepeat += Scan_Config_Info. sectionNumRepeats[0];
        UserRefSetting.PGA += Scan_Config_Info.pga[0];
        temp = Scan_Config_Info.systemp[0];
        temp = temp/100;
        UserRefSetting.Systemp += temp;
        humidity =  Scan_Config_Info.syshumidity[0];
        humidity =  humidity/100;
        UserRefSetting.Humidity += humidity;
        UserRefSetting.Intensity += Scan_Config_Info.lampintensity[0];
        UserRefSetting.CurrentTime = Scan_Config_Info.day[0] + "/" + Scan_Config_Info.day[1] + "/"+ Scan_Config_Info.day[2]  + "T" + Scan_Config_Info.day[3] + ":" + Scan_Config_Info.day[4] + ":" + Scan_Config_Info.day[5] + ",";
    }
    //endregion
    //region Write Report
    private void WriteCSV()
    {
        try
        {
            CheckSaveReportDirectory();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.getDefault());
            String ts = simpleDateFormat.format(FinishScanCurrentTime);
            String csvName = "";
            String FilenamePrefix = et_prefix.getText().toString();
            if(FilenamePrefix==null || FilenamePrefix.equals(""))
                csvName = deviceInfo.ModelName + "_" + deviceInfo.SerialNum + "_" + ts + ".csv";
            else
                csvName = FilenamePrefix + "_" + deviceInfo.ModelName + "_" + deviceInfo.SerialNum + "_"+ ts + ".csv";

            CSVdata = new ArrayList<>();
            for (int i = 0; i < 35; i++)
                for (int j = 0; j < 15; j++)
                    CSV[i][j] = ",";
            WriteScanConfig();
            WriteReferenceConfig();
            WriteDeviceInfo();
            String buf = "";
            for (int i = 0; i < 28; i++)
            {
                for (int j = 0; j < 15; j++)
                {
                    buf += CSV[i][j];
                    if (j == 14)
                    {
                        CSVdata.add(new String[]{buf});
                    }
                }
                buf = "";
            }
            WriteSpectrum();
            if(CommonStruct.device_supportfunction.SupportReadADC)
            {
                if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                    CSVdata = WriteADCNotTimeStamp_PLUS(CSVdata,CSV);
                else
                    CSVdata = WriteADCNotTimeStamp(CSVdata,CSV);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                MediaStoreWriteCSV(CSVdata,csvName,"/MetaScan_Report/");
            else
            {
                CSVPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/MetaScan_Report/"  + csvName;
                CSVWriter writer =  new CSVWriter(new FileWriter(CSVPath), ',', CSVWriter.NO_QUOTE_CHARACTER);
                writer.writeAll(CSVdata);
                writer.close();
            }
        }catch (Exception e)
        {

        }
    }
    private void MediaStoreWriteCSV(List<String[]> data,String csvName,String Dir)
    {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, csvName);       //file name
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/comma-separated-values");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + Dir);
            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            int Len = data.size();
            for(int i=0;i<Len;i++)
            {
                String datacontent = "";
                for(int j=0;j<data.get(i).length;j++)
                {
                    datacontent +=  data.get(i)[j];
                    if(j < data.get(i).length - 1)
                        datacontent += ",";
                }
                datacontent += "\r\n";
                outputStream.write(datacontent.getBytes());
            }
            outputStream.close();
        }catch (Exception e)
        {

        }
    }
    private void CheckSaveReportDirectory()
    {
        File mSDFile  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/MetaScan_Report");
        //No file exist
        if(!mFile.exists())
            mFile.mkdirs();
        mFile.setExecutable(true);
        mFile.setReadable(true);
        mFile.setWritable(true);
    }
    private void CheckSaveReferenceDirectory()
    {
        File mSDFile  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/MetaScan_Reference");
        //No file exist
        if(!mFile.exists())
            mFile.mkdirs();
        mFile.setExecutable(true);
        mFile.setReadable(true);
        mFile.setWritable(true);
    }
    private void WriteScanConfig()
    {
        String configname = getBytetoString(Scan_Config_Info.configName);
        int numSections = Scan_Config_Info.numSections[0];
        int index = 0;
        double temp;
        double humidity;
        byte[] MB_Ver = deviceInfo.MainBoard.getBytes(StandardCharsets.US_ASCII);
        CSV[0][0] = "***Scan Config Information***,";
        CSV[1][0] = "Scan Config Name:,";
        CSV[2][0] = "Scan Config Type:,";
        CSV[3][0] = "Section Config Type:,";
        CSV[4][0] = "Start Wavelength (nm):,";
        CSV[5][0] = "End Wavelength (nm):,";
        CSV[6][0] = "Pattern Width (nm):,";
        CSV[7][0] = "Exposure (ms):,";
        CSV[8][0] = "Digital Resolution:,";
        CSV[9][0] = "Num Repeats:,";
        CSV[10][0] = "PGA Gain:,";
        CSV[11][0] = "System Temp (C):,";
        CSV[12][0] = "Humidity (%):,";
        CSV[13][0] = "Battery Capacity (%):,";
        if (MB_Ver[0] >= 'F')
            CSV[14][0] = "Lamp ADC:,";
        else
            CSV[14][0] = "Lamp Indicator:,";
        CSV[15][0] = "Data Date-Time:,";
        CSV[16][0] = "Total Measurement Time in sec:,";

        CSV[1][1] = configname  + ",";
        CSV[2][1] = "Slew,";
        CSV[2][2] = "Num Section:,";
        CSV[2][3] = Integer.toString(numSections) + ",";

        for(int i=0;i<numSections;i++)
        {
            if(Scan_Config_Info.sectionScanType[i] ==0)
                CSV[3][i+1] = "Column,";
            else
                CSV[3][i+1] = "Hadamard,";
            CSV[4][i+1] = Scan_Config_Info.sectionWavelengthStartNm[i] + ",";
            CSV[5][i+1] = Scan_Config_Info.sectionWavelengthEndNm[i] + ",";
            index = Scan_Config_Info.sectionWidthPx[i];
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                CSV[6][i+1] = widthnm_plus[index] + ",";
            else
                CSV[6][i+1] = widthnm[index] + ",";
            index = Scan_Config_Info.sectionExposureTime[i];
            CSV[7][i+1] = exposure_time_vlaue[index] + ",";
            CSV[8][i+1] = Scan_Config_Info.sectionNumPatterns[i] + ",";
        }
        CSV[9][1] =Scan_Config_Info. sectionNumRepeats[0] + ",";
        CSV[10][1] = Scan_Config_Info.pga[0] + ",";
        if(spinner_pga.getSelectedItemPosition() == 0)
            CSV[10][2] = "(AutoPGA)" + ",";
        temp = Scan_Config_Info.systemp[0];
        temp = temp/100;
        CSV[11][1] = temp  + ",";
        humidity =  Scan_Config_Info.syshumidity[0];
        humidity =  humidity/100;
        CSV[12][1] = humidity  + ",";
        CSV[13][1] = deviceInfo.Battery + ",";
        CSV[14][1] = Scan_Config_Info.lampintensity[0] + ",";
        CSV[15][1] = Scan_Config_Info.day[0] + "/" + Scan_Config_Info.day[1] + "/"+ Scan_Config_Info.day[2]  + "T" + Scan_Config_Info.day[3] + ":" + Scan_Config_Info.day[4] + ":" + Scan_Config_Info.day[5] + ",";
        CSV[16][1] = Double.toString((double) MesureScanTime/1000);
    }
    private void WriteReferenceConfig()
    {
        int index;
        double temp;
        double humidity;
        byte[] MB_Ver = deviceInfo.MainBoard.getBytes(StandardCharsets.US_ASCII);
        CSV[0][7] = "***Reference Scan Information***,";
        CSV[1][7] = "Scan Config Name:,";
        CSV[2][7] = "Scan Config Type:,";
        CSV[3][7] = "Section Config Type:,";
        CSV[4][7] = "Start Wavelength (nm):,";
        CSV[5][7] = "End Wavelength (nm):,";
        CSV[6][7] = "Pattern Width (nm):,";
        CSV[7][7] = "Exposure (ms):,";
        CSV[8][7] = "Digital Resolution:,";
        CSV[9][7] = "Num Repeats:,";
        CSV[10][7] = "PGA Gain:,";
        CSV[11][7] = "System Temp (C):,";
        CSV[12][7] = "Humidity (%):,";
        if (MB_Ver[0] >= 'F')
            CSV[13][7] = "Lamp ADC:,";
        else
            CSV[13][7] = "Lamp Indicator:,";
        CSV[14][7] = "Data Date-Time:,";
        CSV[2][8] = "Slew,";
        CSV[2][9] = "Num Section:,";
        if(referenceType == ReferenceType.Previous)
        {
            CSV[1][8] = UserRefSetting.ConfigName;
            CSV[2][10] = UserRefSetting.Section;
            CSV[3][8] = UserRefSetting.ConfigType;
            CSV[4][8] = UserRefSetting.StartWav;
            CSV[5][8] = UserRefSetting.EndWav;
            CSV[6][8] = UserRefSetting.Width;
            CSV[7][8] = UserRefSetting.ExposureTime;
            CSV[8][8] = UserRefSetting.Resolution;
            CSV[9][8] = UserRefSetting.NumRepeat;
            CSV[10][8] = UserRefSetting.PGA;
            CSV[11][8] = UserRefSetting.Systemp;
            CSV[12][8] = UserRefSetting.Humidity;
            CSV[13][8] = UserRefSetting.Intensity;
            CSV[14][8] = UserRefSetting.CurrentTime;
        }
        else//reference built-in
        {
            CSV[14][8] =  Reference_Info.refday[0]  + "/" +Reference_Info.refday[1] + "/"+ Reference_Info.refday[2] + "T" + Reference_Info.refday[3] + ":" + Reference_Info.refday[4] + ":" + Reference_Info.refday[5];
            if(getBytetoString(Reference_Info.refconfigName).equals("SystemTest"))
                CSV[1][8] = "Built-in Factory Reference";
            else
                CSV[1][8] = "Built-in User Reference";
            CSV[2][10] = "1,";
            if(Reference_Info.refconfigtype[0] == 0)
                CSV[3][8] = "Column,";
            else
                CSV[3][8] = "Hadamard,";
            CSV[4][8] = Double.toString(Reference_Info.refstartwav[0]);
            CSV[5][8] = Double.toString(Reference_Info.refendwav[0]);
            index = Reference_Info.width[0];
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                CSV[6][8] = widthnm_plus[index] + ",";
            else
                CSV[6][8] = widthnm[index] + ",";
            index = Reference_Info.refexposuretime[0];
            CSV[7][8] = exposure_time_vlaue[index] + ",";
            CSV[8][8] = Integer.toString( Reference_Info.numpattren[0]) + ",";
            CSV[9][8] = Reference_Info.numrepeat[0] + ",";
            CSV[10][8] = Integer.toString(Reference_Info.refpga[0]);
            temp = Reference_Info.refsystemp[0];
            temp = temp/100;
            CSV[11][8] = Double.toString(temp) + ",";
            humidity =  Reference_Info.refsyshumidity[0]/100;
            CSV[12][8] = Double.toString(humidity) ;
            CSV[13][8] = Reference_Info.reflampintensity[0] +",";
        }
    }
    private void WriteDeviceInfo()
    {
        Boolean HaveError = false;
        CSV[18][0] = "***General Information***,";
        CSV[19][0] = "Model Name:,";
        CSV[20][0] = "Serial Number:,";
        CSV[21][0] = "GUI Version:,";
        CSV[22][0] = "TIVA Version:,";
        CSV[23][0] = "UUID:,";
        CSV[24][0] = "Main Board Version:,";
        CSV[25][0] = "Detector Board Version:,";

        CSV[19][1] = deviceInfo.ModelName + ",";
        CSV[20][1] = deviceInfo.SerialNum + ",";
        CSV[20][2] = "(" + deviceInfo.MFGNum + ")" + ",";
        String version = "";
        int versionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        CSV[21][1] = version + "." + Integer.toString(versionCode) + ",";
        CSV[21][2] = "(Android MetaScan)" + ",";
        CSV[22][1] = deviceInfo.TIVAVer + ",";
        CSV[23][1] = deviceInfo.UUID + ",";
        CSV[24][1] = deviceInfo.MainBoard;
        CSV[25][1] = deviceInfo.DetectBoard;

        //Calibration Coefficients
        CSV[18][7] = "***Calibration Coefficients***,";
        CSV[19][7] = "Shift Vector Coefficients:,";
        CSV[20][7] = "Pixel to Wavelength Coefficients:,";
        CSV[21][7] = "***Lamp Usage ***";
        CSV[22][7] = "Total Time(hh:mm:ss):,";
        CSV[23][7] ="***Device/Error/Activation Status***,";
        CSV[24][7] ="Device Status:,";
        CSV[24][9] ="Activation Status:,";
        CSV[25][7] ="Error status:,";
        CSV[26][9] = "Error Details:,";

        CSV[19][8] = Scan_Config_Info.shift_vector_coff[0] + ",";
        CSV[19][9] = Scan_Config_Info.shift_vector_coff[1] + ",";
        CSV[19][10] = Scan_Config_Info.shift_vector_coff[2] + ",";
        CSV[20][8] = Scan_Config_Info.pixel_coff[0] + ",";
        CSV[20][9] = Scan_Config_Info.pixel_coff[1] + ",";
        CSV[20][10] = Scan_Config_Info.pixel_coff[2] + ",";
        CSV[22][8] = deviceInfo.TotalLampTime + ",";
        final StringBuilder stringBuilder = new StringBuilder(8);
        for(int i= 3;i>= 0;i--)
            stringBuilder.append(String.format("%02X", deviceInfo.Devbyte[i]));
        CSV[24][8] ="0x" + stringBuilder.toString() + ",";
        if(deviceInfo.IsActivated)
            CSV[24][10] = "Activated";
        else
            CSV[24][10] = "Not activated";
        final StringBuilder stringBuilder_errorstatus = new StringBuilder(8);
        for(int i= 3;i>= 0;i--)
            stringBuilder_errorstatus.append(String.format("%02X", deviceInfo.Errbyte[i]));
        CSV[25][8] ="0x" + stringBuilder_errorstatus.toString() + ",";
        final StringBuilder stringBuilder_errorcode = new StringBuilder(8);
        for(int i= 4;i<20;i++)
        {
            stringBuilder_errorcode.append(String.format("%02X", deviceInfo.Errbyte[i]));
            if(deviceInfo.Errbyte[i] !=0)
                HaveError = true;
        }
        CSV[25][9] = "Error Code:,";
        CSV[25][10] ="0x" + stringBuilder_errorcode.toString() + ",";
        if(HaveError)
            CSV[26][10] = ErrorByteTransfer();
        else
            CSV[26][10] = "Not Found,";
    }
    private void WriteSpectrum()
    {
        CSVdata.add(new String[]{"***Scan Data***"});
        CSVdata.add(new String[]{"Wavelength (nm),Absorbance (AU),Reference Signal (unitless),Sample Signal (unitless)"});
        int csvIndex;
        for (csvIndex = 0; csvIndex < Scan_Spectrum_Data.getLength(); csvIndex++) {
            float absorb = 0;
            float reference = 0;
            double waves = Scan_Spectrum_Data.getWavelength()[csvIndex];
            int intens = Scan_Spectrum_Data.getUncalibratedIntensity()[csvIndex];
            if(referenceType == ReferenceType.Previous)
            {
                int RefVal;
                if(NewRefPGA != Scan_Config_Info.pga[0])
                    RefVal = GetRef(Scan_Config_Info.pga[0], NewReferenceIntensity.get(csvIndex));
                else
                    RefVal = NewReferenceIntensity.get(csvIndex);
                absorb = (-1) * (float) Math.log10((double) Scan_Spectrum_Data.getUncalibratedIntensity()[csvIndex] / (double)RefVal);
                reference = (float) RefVal;
            }
            else
            {
                absorb = (-1) * (float) Math.log10((double) Scan_Spectrum_Data.getUncalibratedIntensity()[csvIndex] / (double) Scan_Spectrum_Data.getIntensity()[csvIndex]);
                reference = (float) Scan_Spectrum_Data.getIntensity()[csvIndex];
            }
            CSVdata.add(new String[]{String.valueOf(waves), String.valueOf(absorb),String.valueOf(reference), String.valueOf(intens)});
        }
    }
    public static String getBytetoString(byte configName[]) {
        byte[] byteChars = new byte[40];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] var3 = byteChars;
        int i = byteChars.length;
        for(int var5 = 0; var5 < i; ++var5) {
            byte b = var3[var5];
            byteChars[b] = 0;
        }
        String s = null;
        for(i = 0; i < configName.length; ++i) {
            byteChars[i] = configName[i];
            if(configName[i] == 0) {
                break;
            }
            os.write(configName[i]);
        }
        try {
            s = new String(os.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException var7) {
            var7.printStackTrace();
        }
        return s;
    }
    private String ErrorByteTransfer()
    {
        String ErrorMsg = "";
        int ErrorInt = deviceInfo.Errbyte[0]&0xFF | (deviceInfo.Errbyte[1] << 8);
        if((ErrorInt & 0x00000001) > 0)//Scan Error
        {
            ErrorMsg += "Scan Error : ";
            int ErrDetailInt = deviceInfo.Errbyte[4]&0xFF;
            if ((ErrDetailInt & 0x01) > 0)
                ErrorMsg += "DLPC150 Boot Error Detected.    ";
            if ((ErrDetailInt & 0x02) > 0)
                ErrorMsg += "DLPC150 Init Error Detected.    ";
            if ((ErrDetailInt & 0x04) > 0)
                ErrorMsg += "DLPC150 Lamp Driver Error Detected.    ";
            if ((ErrDetailInt & 0x08) > 0)
                ErrorMsg += "DLPC150 Crop Image Failed.    ";
            if ((ErrDetailInt & 0x10) > 0)
                ErrorMsg += "ADC Data Error.    ";
            if ((ErrDetailInt & 0x20) > 0)
                ErrorMsg += "Scan Config Invalid.    ";
            if ((ErrDetailInt & 0x40) > 0)
                ErrorMsg += "Scan Pattern Streaming Error.    ";
            if ((ErrDetailInt & 0x80) > 0)
                ErrorMsg += "DLPC150 Read Error.    ";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000002) > 0)  // ADC Error
        {
            ErrorMsg += "ADC Error : ";
            int ErrDetailInt = deviceInfo.Errbyte[5]&0xFF;
            if (ErrDetailInt == 1)
                ErrorMsg += "Timeout Error.    ";
            else if (ErrDetailInt == 2)
                ErrorMsg += "PowerDown Error.    ";
            else if (ErrDetailInt == 3)
                ErrorMsg += "PowerUp Error.    ";
            else if (ErrDetailInt == 4)
                ErrorMsg += "Standby Error.    ";
            else if (ErrDetailInt == 5)
                ErrorMsg += "WakeUp Error.    ";
            else if (ErrDetailInt == 6)
                ErrorMsg += "Read Register Error.    ";
            else if (ErrDetailInt == 7)
                ErrorMsg += "Write Register Error.    ";
            else if (ErrDetailInt == 8)
                ErrorMsg += "Configure Error.    ";
            else if (ErrDetailInt == 9)
                ErrorMsg += "Set Buffer Error.    ";
            else if (ErrDetailInt == 10)
                ErrorMsg += "Command Error.    ";
            else if (ErrDetailInt == 11)
                ErrorMsg += "Set PGA Error.    ";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000004) > 0)  // SD Card Error
        {
            ErrorMsg += "SD Card Error.";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000008) > 0)  // EEPROM Error
        {
            ErrorMsg += "EEPROM Error.";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000010) > 0)  // BLE Error
        {
            ErrorMsg += "Bluetooth Error.";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000020) > 0)  // Spectrum Library Error
        {
            ErrorMsg += "Spectrum Library Error.";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000040) > 0)  // Hardware Error
        {
            ErrorMsg += "HW Error : ";
            int ErrDetailInt = deviceInfo.Errbyte[11]&0xFF;
            if (ErrDetailInt == 1)
                ErrorMsg += "DLPC150 Error.    ";
            else if (ErrDetailInt == 2)
                ErrorMsg += "Read UUID Error.    ";
            else if (ErrDetailInt == 3)
                ErrorMsg += "Flash Initial Error.    ";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000080) > 0)  // TMP Sensor Error
        {
            ErrorMsg += "TMP Error : ";
            int ErrDetailInt = deviceInfo.Errbyte[12]&0xFF;
            if (ErrDetailInt == 1)
                ErrorMsg += "Invalid Manufacturing ID.    ";
            else if (ErrDetailInt == 2)
                ErrorMsg += "Invalid Device ID.    ";
            else if (ErrDetailInt == 3)
                ErrorMsg += "Reset Error.    ";
            else if (ErrDetailInt == 4)
                ErrorMsg += "Read Register Error.    ";
            else if (ErrDetailInt == 5)
                ErrorMsg += "Write Register Error.    ";
            else if (ErrDetailInt == 6)
                ErrorMsg += "Timeout Error.    ";
            else if (ErrDetailInt == 7)
                ErrorMsg += "I2C Error.    ";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000100) > 0)  // HDC Sensor Error
        {
            ErrorMsg += "HDC Error : ";
            int ErrDetailInt = deviceInfo.Errbyte[13]&0xFF;
            if (ErrDetailInt == 1)
                ErrorMsg += "Invalid Manufacturing ID.    ";
            else if (ErrDetailInt == 2)
                ErrorMsg += "Invalid Device ID.    ";
            else if (ErrDetailInt == 3)
                ErrorMsg += "Reset Error.    ";
            else if (ErrDetailInt == 4)
                ErrorMsg += "Read Register Error.    ";
            else if (ErrDetailInt == 5)
                ErrorMsg += "Write Register Error.    ";
            else if (ErrDetailInt == 6)
                ErrorMsg += "Timeout Error.    ";
            else if (ErrDetailInt == 7)
                ErrorMsg += "I2C Error.    ";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000200) > 0)  // Battery Error
        {
            ErrorMsg += "Battery Error : ";
            int ErrDetailInt = deviceInfo.Errbyte[14]&0xFF;
            if (ErrDetailInt == 0x01)
                ErrorMsg += "Battery Low.    ";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000400) > 0)  // Insufficient Memory Error
        {
            ErrorMsg += "Not Enough Memory.";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00000800) > 0)  // UART Error
        {
            ErrorMsg += "UART Error.";
            ErrorMsg += ",";
        }
        if ((ErrorInt & 0x00001000) > 0)   // System Error
        {
            ErrorMsg += "System Error : ";
            int ErrDetailInt = deviceInfo.Errbyte[17]&0xFF;
            if ((ErrDetailInt & 0x01) > 0)
                ErrorMsg += "Unstable Lamp ADC.    ";
            if ((ErrDetailInt & 0x02) > 0)
                ErrorMsg += "Unstable Peak Intensity.    ";
            if ((ErrDetailInt & 0x04) > 0)
                ErrorMsg += "ADS1255 Error.    ";
            if ((ErrDetailInt & 0x08) > 0)
                ErrorMsg += "Auto PGA Error.    ";

            ErrDetailInt = deviceInfo.Errbyte[18]&0xFF;
            if ((ErrDetailInt & 0x01) > 0)
                ErrorMsg += "Unstable Scan in Repeated times.    ";
            ErrorMsg += ",";
        }
        if(ErrorMsg.equals(""))
            ErrorMsg = "Not Found";
        return ErrorMsg;
    }
    private List<String[]> WriteADCNotTimeStamp(List<String[]> data,String CSV[][] )
    {
        try {
            data.add(new String[]{""});
            data.add(new String[]{"***Lamp Ramp Up ADC***,"});
            data.add(new String[]{"ADC0,ADC1,ADC2,ADC3"});
            String[] ADC = new String[4];
            int count = 0;
            for(int i=0;i<Lamp_RAMPUP_ADC_DATA.length;i+=2)
            {
                int adc_value = (Lamp_RAMPUP_ADC_DATA[i+1]&0xff)<<8|Lamp_RAMPUP_ADC_DATA[i]&0xff;
                if(adc_value ==0)
                    break;
                ADC[count] = Integer.toString(adc_value) ;
                count ++;
                if(count ==4)
                {
                    data.add(ADC);
                    count = 0;
                    ADC = new String[4];
                }
            }
            //-----------------------------------
            data.add(new String[]{""});
            data.add(new String[]{"***Lamp ADC among repeated times***,"});
            data.add(new String[]{"ADC0,ADC1,ADC2,ADC3"});
            ADC = new String[4];
            int Average_ADC[] = new int[4];
            int cal_count =0;
            count = 0;
            for(int i=0;i<Lamp_AVERAGE_ADC_DATA.length;i+=2)
            {
                int adc_value = (Lamp_AVERAGE_ADC_DATA[i+1]&0xff)<<8|Lamp_AVERAGE_ADC_DATA[i]&0xff;
                if(adc_value ==0 )
                    break;
                ADC[count] =Integer.toString(adc_value) ;
                Average_ADC[count] +=adc_value;
                count ++;
                if(count ==4)
                {
                    data.add(ADC);
                    cal_count ++;
                    count = 0;
                    ADC = new String[4];
                }
            }
            String AverageADC = "Lamp ADC:,";

            for(int i=0;i<4;i++)
            {
                double buf_adc = (double)Average_ADC[i];
                AverageADC +=Math.round( buf_adc/cal_count) + ",";
            }
            AverageADC +=",," + CSV[14][7] + CSV[14][8];// add ref data-time data
            data.get(14)[0] = AverageADC;
        }catch (Exception e)
        {

        }
        return  data;
    }
    private List<String[]> WriteADCNotTimeStamp_PLUS(List<String[]> data,String CSV[][] )
    {
        try {
            data.add(new String[]{""});
            data.add(new String[]{"***Lamp Ramp Up ADC***,"});
            data.add(new String[]{"ADC0,ADC1,ADC2"});
            String[] ADC = new String[3];
            int count = 0;
            for(int i=0;i<Lamp_RAMPUP_ADC_DATA.length;i+=2)
            {
                int adc_value = (Lamp_RAMPUP_ADC_DATA[i+1]&0xff)<<8|Lamp_RAMPUP_ADC_DATA[i]&0xff;
                if(adc_value ==0)
                    break;
                if(count < 3)
                    ADC[count] = Integer.toString(adc_value) ;
                count ++;
                if(count ==4)
                {
                    data.add(ADC);
                    count = 0;
                    ADC = new String[3];
                }
            }
            //-----------------------------------
            data.add(new String[]{""});
            data.add(new String[]{"***Lamp ADC among repeated times***,"});
            data.add(new String[]{"ADC0,ADC1,ADC2"});
            ADC = new String[3];
            int Average_ADC[] = new int[3];
            int cal_count =0;
            count = 0;
            for(int i=0;i<Lamp_AVERAGE_ADC_DATA.length;i+=2)
            {
                int adc_value = (Lamp_AVERAGE_ADC_DATA[i+1]&0xff)<<8|Lamp_AVERAGE_ADC_DATA[i]&0xff;
                if(adc_value ==0 )
                    break;
                if(count < 3)
                {
                    ADC[count] =Integer.toString(adc_value) ;
                    Average_ADC[count] +=adc_value;
                }
                count ++;
                if(count ==4)
                {
                    data.add(ADC);
                    cal_count ++;
                    count = 0;
                    ADC = new String[3];
                }
            }
            String AverageADC = "Lamp ADC:,";

            for(int i=0;i<3;i++)
            {
                double buf_adc = (double)Average_ADC[i];
                AverageADC +=Math.round( buf_adc/cal_count) + ",";
            }
            AverageADC +=",," + CSV[14][7] + CSV[14][8];// add ref data-time data
            data.get(14)[0] = AverageADC;
        }catch (Exception e)
        {

        }
        return  data;
    }
    //endregion
    //region Write Reference
    private void GetReferenceValue(String CsvName)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            Uri collection = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + "=? AND " +
                    MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[] { Environment.DIRECTORY_DOCUMENTS + "/MetaScan_Reference/", CsvName };
            int numSections = CurrentScanConfig.getSlewNumSections();
            int count = 0;
            for(int i = 0; i< numSections; i++)
            {
                count+= CurrentScanConfig.getSectionNumPatterns()[i];
            }
            UserRefSetting = new CommonStruct.UserreferenceSetting();
            String refScanTime="";
            try (Cursor cursor = mContext.getContentResolver().query(collection, null, selection, selectionArgs, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                    long id = cursor.getLong(idColumn);
                    Uri fileUri = Uri.withAppendedPath(collection, String.valueOf(id));

                    try (InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String line;
                        NewReferenceIntensity.clear();
                        for(int i=0;i<count;i++)
                        {
                            if((line = reader.readLine())!=null)
                            {
                                NewReferenceIntensity.add(Integer.parseInt(line));
                            }
                        }
                        refScanTime = reader.readLine();
                        UserRefSetting.ConfigName = reader.readLine();
                        UserRefSetting.Type = reader.readLine();
                        UserRefSetting.Section = reader.readLine();
                        UserRefSetting.ConfigType = reader.readLine();
                        UserRefSetting.StartWav = reader.readLine();
                        UserRefSetting.EndWav = reader.readLine();
                        UserRefSetting.Width = reader.readLine();
                        UserRefSetting.ExposureTime = reader.readLine();
                        UserRefSetting.Resolution = reader.readLine();
                        UserRefSetting.NumRepeat = reader.readLine();
                        UserRefSetting.PGA = reader.readLine();
                        UserRefSetting.Systemp = reader.readLine();
                        UserRefSetting.Humidity = reader.readLine();
                        UserRefSetting.Intensity = reader.readLine();
                        UserRefSetting.CurrentTime = reader.readLine();
                        NewRefPGA = Integer.parseInt(UserRefSetting.PGA);
                        NewRefConfigName = CurrentScanConfig.getConfigName();
                        reader.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            NewReferenceIntensity.clear();
            UserRefSetting = new CommonStruct.UserreferenceSetting();
            String refScanTime="";
            int count = 0;
            int numSections = CurrentScanConfig.getSlewNumSections();
            for(int i = 0; i< numSections; i++)
            {
                count+= CurrentScanConfig.getSectionNumPatterns()[i];
            }
            try {
                String filePath = Environment.DIRECTORY_DOCUMENTS + "/MetaScan_Reference/" + CsvName;
                FileReader fr=new FileReader(filePath);
                BufferedReader br=new BufferedReader(fr);
                String line;
                for(int i=0;i<count;i++)
                {
                    if((line = br.readLine())!=null)
                    {
                        NewReferenceIntensity.add(Integer.parseInt(line));
                    }
                }
                refScanTime = br.readLine();
                UserRefSetting.ConfigName = br.readLine();
                UserRefSetting.Type = br.readLine();
                UserRefSetting.Section = br.readLine();
                UserRefSetting.ConfigType = br.readLine();
                UserRefSetting.StartWav = br.readLine();
                UserRefSetting.EndWav = br.readLine();
                UserRefSetting.Width = br.readLine();
                UserRefSetting.ExposureTime = br.readLine();
                UserRefSetting.Resolution = br.readLine();
                UserRefSetting.NumRepeat = br.readLine();
                UserRefSetting.PGA = br.readLine();
                UserRefSetting.Systemp = br.readLine();
                UserRefSetting.Humidity = br.readLine();
                UserRefSetting.Intensity = br.readLine();
                UserRefSetting.CurrentTime = br.readLine();
                NewRefPGA = Integer.parseInt(UserRefSetting.PGA);
                NewRefConfigName = CurrentScanConfig.getConfigName();
                br.close();
            }catch (Exception e)
            {

            };
        }
    }
    private Boolean IsLocalReference(String csvName)
    {
        String folderPath = Environment.DIRECTORY_DOCUMENTS + "/MetaScan_Reference/";
        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/MetaScan_Reference/";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            Uri collection = MediaStore.Files.getContentUri("external");

            String[] projection = {MediaStore.Files.FileColumns.DISPLAY_NAME};
            String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " = ? AND " +
                    MediaStore.Files.FileColumns.DISPLAY_NAME + " = ?";
            String[] selectionArgs = new String[]{folderPath, csvName};

            try (Cursor cursor = mContext.getContentResolver().query(collection, projection, selection, selectionArgs, null)) {
                return cursor != null && cursor.getCount() > 0;
            }
        }
        else
        {
            filepath += csvName;
            File file = new File(filepath);
            return file.exists();
        }
    }
    public static boolean DeleteRefFile(Context context, String fileName) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + "=? AND " +
                MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[]{
                Environment.DIRECTORY_DOCUMENTS + "/MetaScan_Reference/",
                fileName
        };

        int deletedRows = contentResolver.delete(uri, selection, selectionArgs);
        return deletedRows > 0;
    }
    private void WriteReference()
    {
        String Device = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
        Device = Device.replace(":","_");
        String csvName = Device + "_";
        int numSections = CurrentScanConfig.getSlewNumSections();
        for(int i=0;i<numSections;i++)
        {
            csvName = csvName +  CurrentScanConfig.getSectionNumRepeats()[i] + "_" + CurrentScanConfig.getSectionScanType()[i] + "_" + (CurrentScanConfig.getSectionWavelengthStartNm()[i] & 0xFFFF) + "_" + (CurrentScanConfig.getSectionWavelengthEndNm()[i] & 0xFFFF)
                    + "_" +  CurrentScanConfig.getSectionWidthPx()[i] + "_" + (CurrentScanConfig.getSectionNumPatterns()[i] & 0x0FFF) + "_" + CurrentScanConfig.getSectionExposureTime()[i] + ".csv";
        }
        try {
            CheckSaveReferenceDirectory();
            DeleteRefFile(mContext, csvName);
            List<String[]> data = new ArrayList<String[]>();
            for(int i=0;i<NewReferenceIntensity.size();i++)
                data.add(new String[]{NewReferenceIntensity.get(i).toString()});
            String currentTime = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.getDefault()).format(new Date());
            data.add(new String[]{currentTime});
            data.add(new String[]{UserRefSetting.ConfigName});
            data.add(new String[]{UserRefSetting.Type});
            data.add(new String[]{UserRefSetting.Section});
            data.add(new String[]{UserRefSetting.ConfigType});
            data.add(new String[]{UserRefSetting.StartWav});
            data.add(new String[]{UserRefSetting.EndWav});
            data.add(new String[]{UserRefSetting.Width});
            data.add(new String[]{UserRefSetting.ExposureTime});
            data.add(new String[]{UserRefSetting.Resolution});
            data.add(new String[]{UserRefSetting.NumRepeat});
            data.add(new String[]{UserRefSetting.PGA});
            data.add(new String[]{UserRefSetting.Systemp});
            data.add(new String[]{UserRefSetting.Humidity});
            data.add(new String[]{UserRefSetting.Intensity});
            data.add(new String[]{UserRefSetting.CurrentTime});
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                MediaStoreWriteCSV(data,csvName,"/MetaScan_Reference/");
            else
            {
                CSVPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/MetaScan_Reference/"  + csvName;
                File file= new File(CSVPath);
                if (file.exists()) {
                    file.delete();
                }
                CSVWriter writer =  new CSVWriter(new FileWriter(CSVPath), ',', CSVWriter.NO_QUOTE_CHARACTER);
                writer.writeAll(data);
                writer.close();
            }

        }catch (Exception e)
        {

        }
    }
    //endregion

    //region Scan Plot
    /**
     * Pager enum to control tab tile and layout resource
     */
    public enum CustomPagerEnum {
        REFLECTANCE(R.string.reflectance, R.layout.page_graph_reflectance),
        ABSORBANCE(R.string.absorbance, R.layout.page_graph_absorbance),
        INTENSITY(R.string.intensity, R.layout.page_graph_intensity),
        REFERENCE(R.string.reference_tab,R.layout.page_graph_reference);
        private final int mTitleResId;
        private final int mLayoutResId;
        CustomPagerEnum(int titleResId, int layoutResId) {
            mTitleResId = titleResId;
            mLayoutResId = layoutResId;
        }
        public int getLayoutResId() {
            return mLayoutResId;
        }
    }
    /**
     * Custom pager adapter to handle changing chart data when pager tabs are changed
     */
    public class CustomPagerAdapter extends PagerAdapter {

        private final Context mContext;

        public CustomPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(customPagerEnum.getLayoutResId(), collection, false);
            collection.addView(layout);
            LineChart mChart = (LineChart) layout.findViewById(R.id.lineChartRef);
            YAxis leftAxis = new YAxis();
            int numSections = CurrentScanConfig.getSlewNumSections();
            if (customPagerEnum.getLayoutResId() == R.layout.page_graph_reflectance) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartRef);
                leftAxis = mChart.getAxisLeft();
                //  leftAxis.setAxisMaximum(maxReflectance);
                // leftAxis.setAxisMinimum(minReflectance);
                // add data
               /* int numSections= Scan_Config_Info.numSections[0];
                if(numSections>=2 &&(Float.isNaN(minReflectance)==false && Float.isNaN(maxReflectance)==false)&& Current_Scan_Method!=ScanMethod.QuickSet)//Scan method : quickset only one section
                    setDataSlew(mChart, mReflectanceFloat,numSections);
                else if(Float.isNaN(minReflectance)==false && Float.isNaN(maxReflectance)==false)
                    setData(mChart, mReflectanceFloat, ChartType.REFLECTANCE);*/
                if(numSections == 1)
                    setData(mChart, mReflectanceFloat, ChartType.REFLECTANCE);
                else
                    setDataSlew(mChart, mReflectanceFloat,numSections);
            }
            else if (customPagerEnum.getLayoutResId() == R.layout.page_graph_absorbance) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartAbs);
                leftAxis = mChart.getAxisLeft();
                // leftAxis.setAxisMaximum(maxAbsorbance);
                // leftAxis.setAxisMinimum(minAbsorbance);
                // add data
               /* int numSections= Scan_Config_Info.numSections[0];
                if(numSections>=2 &&(Float.isNaN(minAbsorbance)==false && Float.isNaN(maxAbsorbance)==false)&& Current_Scan_Method!=ScanMethod.QuickSet)////Scan method : quickset only one section
                    setDataSlew(mChart, mAbsorbanceFloat,numSections);
                else if( Float.isNaN(minAbsorbance)==false && Float.isNaN(maxAbsorbance)==false)
                    setData(mChart, mAbsorbanceFloat, ChartType.ABSORBANCE);*/
                if(numSections == 1)
                    setData(mChart, mAbsorbanceFloat, ChartType.ABSORBANCE);
                else
                    setDataSlew(mChart, mAbsorbanceFloat,numSections);
            }
            else if (customPagerEnum.getLayoutResId() == R.layout.page_graph_intensity) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartInt);
                leftAxis = mChart.getAxisLeft();
               // leftAxis.setAxisMaximum(maxIntensity);
               // leftAxis.setAxisMinimum(minIntensity);
                // add data
                /*int numSections= Scan_Config_Info.numSections[0];
                if(numSections>=2 &&(Float.isNaN(minIntensity)==false && Float.isNaN(maxIntensity)==false) && Current_Scan_Method!= ScanMethod.QuickSet)//Scan method : quickset only one section
                    setDataSlew(mChart, mIntensityFloat,numSections); //scan data section > 1
                else if(Float.isNaN(minIntensity)==false && Float.isNaN(maxIntensity)==false)
                    setData(mChart, mIntensityFloat,ChartType.INTENSITY);//scan data section = 1*/
                if(numSections == 1)
                    setData(mChart, mIntensityFloat,ChartType.INTENSITY);//scan data section = 1
                else
                    setDataSlew(mChart, mIntensityFloat,numSections);

            } else if (customPagerEnum.getLayoutResId() == R.layout.page_graph_reference) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartReference);
                leftAxis = mChart.getAxisLeft();
              //  leftAxis.setAxisMaximum(maxReference);
              //  leftAxis.setAxisMinimum(minReference);
                // add data
               /* int numSections= Scan_Config_Info.numSections[0];
                if(numSections>=2 &&(Float.isNaN(minReference)==false && Float.isNaN(maxReference)==false)&& Current_Scan_Method!=ScanMethod.QuickSet)//Scan method : quickset only one section
                    setDataSlew(mChart, mReferenceFloat,numSections);
                else if( Float.isNaN(minReference)==false && Float.isNaN(maxReference)==false)
                    setData(mChart, mReferenceFloat, ChartType.INTENSITY);*/
                if(numSections == 1)
                    setData(mChart, mReferenceFloat, ChartType.REFERENCE);
                else
                    setDataSlew(mChart, mReferenceFloat,numSections);
            }
            //Set mchart setting
            mChart.setDrawGridBackground(false);
            // enable touch gestures
            mChart.setTouchEnabled(true);
            // enable scaling and dragging
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(true);
            mChart.setAutoScaleMinMaxEnabled(true);
            mChart.getAxisRight().setEnabled(false);
            mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
            mChart.getLegend().setEnabled(false);
            mChart.invalidate();

            // x-axis limit line
            LimitLine llXAxis = new LimitLine(10f, "Index 10");
            llXAxis.setLineWidth(4f);
            llXAxis.enableDashedLine(10f, 10f, 0f);
            llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            llXAxis.setTextSize(10f);

            XAxis xAxis = mChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            //xAxis.setAxisMaximum(maxWavelength);
           // xAxis.setAxisMinimum(minWavelength);

            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setStartAtZero(false);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawLimitLinesBehindData(true);

            // get the legend (only possible after setting data)
            Legend l = mChart.getLegend();
            // modify the legend ...
            l.setForm(Legend.LegendForm.LINE);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return CustomPagerEnum.values().length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.reflectance);
                case 1:
                    return getString(R.string.absorbance);
                case 2:
                    return getString(R.string.intensity);
                case 3:
                    return getString(R.string.reference_tab);
            }
            return null;
        }

    }

    private void setData(LineChart mChart, ArrayList<Entry> yValues, ChartType type) {

        int size = yValues.size();
        if(size == 0)
            return;
        LineDataSet set1 = new LineDataSet(yValues,GraphLabel);
        set1.setColor(Color.BLACK);
        set1.setDrawCircles(false);
        set1.setLineWidth(2f);
        set1.setValues(yValues);
        if (type == ChartType.REFLECTANCE) {
            set1.setColor(Color.RED);
        } else if (type == ChartType.ABSORBANCE) {
            set1.setColor(Color.GREEN);
        } else if (type == ChartType.INTENSITY||type == ChartType.REFERENCE) {
            set1.setColor(Color.BLUE);
        }
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.setMaxVisibleValueCount(50);
    }
    private void setDataSlew(LineChart mChart, ArrayList<Entry> yValues,int slewnum)
    {
        if(yValues.size()<=1)
            return;
        ArrayList<ArrayList<Entry>> ListyValues = new  ArrayList<ArrayList<Entry>>();
        ArrayList<Integer>ListOffset = new ArrayList<>();
        ArrayList<LineDataSet>ListlineSet = new ArrayList<>();
        for(int i=0;i<slewnum;i++)
        {
            ArrayList<Entry> bufyValues = new ArrayList<Entry>();
            ListyValues.add(bufyValues);
            int bufoffset = 0;
            for(int j=0;j<=i;j++)
                bufoffset += CurrentScanConfig.getSectionNumPatterns()[j];
            ListOffset.add(bufoffset);
        }
        for(int k=0;k<slewnum;k++)
        {
            int NumPattern = CurrentScanConfig.getSectionNumPatterns()[k];
            int offset = 0;
            if(k > 0)
                offset = ListOffset.get(k-1);
            for(int i=0;i<NumPattern;i++)
            {
                if(Float.isInfinite(yValues.get(offset+ i).getY()) == false)
                    ListyValues.get(k).add(new Entry(yValues.get(offset + i).getX(),yValues.get(offset + i).getY()));
            }
        }
        for(int i=0;i<slewnum;i++)
        {
            LineDataSet bufset = new LineDataSet(ListyValues.get(i),"Slew" + Integer.toString(i+1));
            bufset.setDrawCircles(false);
            bufset.setLineWidth(2f);
            switch (i)
            {
                case 0:
                    bufset.setColor(Color.BLUE);
                    break;
                case 1:
                    bufset.setColor(Color.RED);
                    break;
                case 2:
                    bufset.setColor(Color.GREEN);
                    break;
                case 3:
                    bufset.setColor(Color.YELLOW);
                    break;
                case 4:
                    bufset.setColor(Color.LTGRAY);
                    break;
            }
            bufset.setValues(ListyValues.get(i));
            ListlineSet.add(bufset);
        }
        LineData data = new LineData();
        switch (slewnum)
        {
            case 2:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1));
                break;
            case 3:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1), ListlineSet.get(2));
                break;
            case 4:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1), ListlineSet.get(2), ListlineSet.get(3));
                break;
            case 5:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1), ListlineSet.get(2), ListlineSet.get(3), ListlineSet.get(4));
                break;
        }
        mChart.setData(data);
        mChart.setMaxVisibleValueCount(50);
    }
    /**
     * Custom enum for chart type
     */
    public enum ChartType {
        REFLECTANCE,
        ABSORBANCE,
        INTENSITY,
        REFERENCE
    }
    //endregion
    //region API
    private  int GetPGAValue()
    {
        String pgavalue = spinner_pga.getSelectedItem().toString();
        int pga;
        if(pgavalue.contains("Auto"))
            pga = 0;
        else
            pga = Integer.parseInt(spinner_pga.getSelectedItem().toString());
        return pga;
    }
    private void SetPGAToDevice()
    {
        String pgavalue = spinner_pga.getSelectedItem().toString();
        int pga;
        if(pgavalue.contains("Auto"))
            pga = 0;
        else
            pga = Integer.parseInt(spinner_pga.getSelectedItem().toString());
        ISCMetaScanSDK.SetPGA(pga);
    }
    private void SetActiveConfig()
    {
        byte[] index = {0, 0};
        switch (configSelectType)
        {
            case SelectConfig:
                index[0] = (byte) CurrentScanConfig.getScanConfigIndex();
                //the index over 256 should calculate index[1]
                index[1] = (byte) (CurrentScanConfig.getScanConfigIndex()/256);
                SetDefaultConfigIndex = index[0];
                ISCMetaScanSDK.SetDefaultConfig(index);
                break;
            case SetConfig:
                int pos = listScanConfig.Configs.size() -1;
                index[0] = (byte) listScanConfig.Configs.get(pos).getScanConfigIndex();
                //the index over 256 should calculate index[1]
                index[1] = (byte) (listScanConfig.Configs.get(pos).getScanConfigIndex()/256);
                SetDefaultConfigIndex = index[0];
                ISCMetaScanSDK.SetDefaultConfig(index);
                break;
        }
    }
    private void  SaveScanConfig()
    {
        setActivityTouchDisable(true);
        bt_save_config.setEnabled(false);
        calProgress.setVisibility(View.VISIBLE);
        progressBarinsideText.setVisibility(View.VISIBLE);
        progressBarinsideText.setText("Save Config...");
        byte[]EXTRA_DATA = ChangeScanConfigToByte(CurrentScanConfig);
        ISCMetaScanSDK.ScanConfig(EXTRA_DATA,ISCMetaScanSDK.ScanConfig.SAVE);
    }
    //endregion
    //region Continous Scan
    private void InitialContinuousScanPara()
    {
        continousScanPara.ContinuousScanFlag = false;
        continousScanPara.StopContinuous = false;
        continousScanPara.TotalScan = 0;
        continousScanPara.CurrentScanCount = 1;
        continousScanPara.ScanInterval = 0;
    }
    private void GetContinuousScanPara()
    {
        continousScanPara.TotalScan = Integer.parseInt(et_repeat_scantimes.getText().toString());
        if(continousScanPara.TotalScan > 0)
        {
            if(referenceType!=ReferenceType.New)
            {
                continousScanPara.ContinuousScanFlag = true;
                continousScanPara.ScanInterval = Integer.parseInt(et_scantime_interval.getText().toString());
            }
        }
    }
    private void ContinousScan_Dialog()
    {
        Continous_Dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //Custom layout
        View view = View.inflate(this, R.layout.continuous_scan_dialog, null);
        Continous_Dialog.setContentView(view);
        //Remove the event that pressing outside the dialog will close the dialog
        Continous_Dialog.setCanceledOnTouchOutside(false);

        //Get the form where the current activity is located
        Window dialogWindow = Continous_Dialog.getWindow();
        //Set the dialog to pop up from the bottom of the form
        dialogWindow.setGravity(Gravity.BOTTOM);
        //Set the dialog to 10dp from the bottom
        dialogWindow.getDecorView().setPadding(0, 0, 0, 10);

        WindowManager m = getWindowManager();
        //Get screen width and height
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        //Set width
        lp.width = (int) (d.getWidth() * 0.9);
        //Set height
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //Set the transparency of the background 0~1
        lp.dimAmount=0.1f;
        dialogWindow.setAttributes(lp);
        //Set the animation effect of the dialog
        dialogWindow.setWindowAnimations(R.style.dialogWindowAnim);
        Continous_Dialog.show();

        tv_continuous_scaninfo = (TextView)Continous_Dialog.findViewById(R.id.tv_continuous_scaninfo);
        tv_continuous_scaninfo.setText("Auto Scanning ...(" + continousScanPara.CurrentScanCount + " / " + continousScanPara.TotalScan + ")" + "\n" + "Scan Interval Time : " + continousScanPara.ScanInterval + " sec");
        final Button stop = (Button) Continous_Dialog.findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continousScanPara.StopContinuous = true;
                stop.setEnabled(false);
                stop.setTextColor(Color.DKGRAY);
            }
        });
    }
    private void ContinuousCompleteDialog(String Title)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Title);
        builder.setMessage("There were totally " + continousScanPara.CurrentScanCount + " scans has been performed!\n" + "Do you want to be in auto continuous scan mode?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                et_repeat_scantimes.setText("0");
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    public void ConfigOutofRangeDialog() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.config_limitation));
        alertDialogBuilder.setMessage("The number of device config has reached maximum limitation, no more config can be saved to device.\nDo you want to reset config?");

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                ConfirmResetConigDialog();
            }
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setActivityTouchDisable(false);
                alertDialog.dismiss();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    public void ConfirmResetConigDialog() {

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
                setActivityTouchDisable(false);
                alertDialog.dismiss();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    //endregion
    private class  BackGroundReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }
    public class ResetConfigCompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Exist = true;
            ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
        }
        return super.onKeyDown(keyCode, event);
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
                        Intent NotifyBackground = new Intent(MainSelectConfigViewActivity.NOTIFY_BACKGROUND);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(NotifyBackground);
                        finish();
                        ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
                    }
                }
            }
        }
    }
}
