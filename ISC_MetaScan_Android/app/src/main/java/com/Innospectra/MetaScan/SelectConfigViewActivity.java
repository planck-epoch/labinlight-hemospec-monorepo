package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.util.ArrayList;

import static com.Innospectra.MetaScan.CommonAPI.ErrorFlash;
import static com.Innospectra.MetaScan.CommonAPI.HaveValidScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.AppStatus;
import static com.Innospectra.MetaScan.CommonStruct.CurrentScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.configSelectType;
import static com.Innospectra.MetaScan.CommonStruct.exposure_time_vlaue;
import static com.Innospectra.MetaScan.CommonStruct.listScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.widthnm;

public class SelectConfigViewActivity extends Activity {
    Button led_connect;
    Button led_ble;
    Button led_scan;
    Button led_error;
    TextView tv_active_configname;
    Spinner spinner_configname;
    ListView lv_configs;
    ProgressBar calProgress;
    TextView progressBarinsideText;
    private Menu mMenu;
    private AlertDialog alertDialog;

    Context mContext;
    private final BroadcastReceiver DisconnectReceiver = new DisconnectReceiver();
    private final BroadcastReceiver GetDeviceStatusReceiver = new GetDeviceStatusReceiver();
    private final BroadcastReceiver WriteScanConfigStatusReceiver = new WriteScanConfigStatusReceiver();
    private final BroadcastReceiver ReturnCurrentScanConfigurationDataReceiver = new ReturnCurrentScanConfigurationDataReceiver();
    private final BroadcastReceiver HomeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();
    private final IntentFilter DisconnectFilter = new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED);
    private final IntentFilter GetDeviceStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_STATUS);
    private final IntentFilter WriteScanConfigStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_RETURN_WRITE_SCAN_CONFIG_STATUS);
    private final IntentFilter  ReturnCurrentScanConfigurationDataFilter = new IntentFilter(ISCMetaScanSDK.RETURN_CURRENT_CONFIG_DATA);
    private ArrayList<ISCMetaScanSDK.SlewScanSection> sections = new ArrayList<>();
    private SlewScanConfAdapter slewScanConfAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.select_config_view);
        setActivityTouchDisable(true);
        mContext = this;
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        InitComponent();
        DisplayScanConfig();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(DisconnectReceiver, DisconnectFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetDeviceStatusReceiver,GetDeviceStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(WriteScanConfigStatusReceiver,WriteScanConfigStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(ReturnCurrentScanConfigurationDataReceiver,ReturnCurrentScanConfigurationDataFilter);
        registerReceiver(HomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), Context.RECEIVER_NOT_EXPORTED);
        ISCMetaScanSDK.getErrorStatus();
    }
    @Override
    public void onResume() {
        super.onResume();
        if(AppStatus == CommonStruct.APPStatus.Home)
            finish();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DisconnectReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetDeviceStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(WriteScanConfigStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(ReturnCurrentScanConfigurationDataReceiver);
        unregisterReceiver(HomeKeyEventReceiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_right_top_text, menu);
        mMenu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        else if(id == R.id.action_righttop_button) {
            calProgress.setVisibility(View.VISIBLE);
            progressBarinsideText.setVisibility(View.VISIBLE);
            CurrentScanConfig = listScanConfig.Configs.get(spinner_configname.getSelectedItemPosition());
            configSelectType = CommonStruct.ConfigSelectType.SelectConfig;
            setActivityTouchDisable(true);
            byte[] EXTRA_DATA = CommonAPI.ChangeScanConfigToByte(CurrentScanConfig);
            ISCMetaScanSDK.ScanConfig(EXTRA_DATA,ISCMetaScanSDK.ScanConfig.SET);
        }
        return super.onOptionsItemSelected(item);
    }
    private void InitComponent()
    {
        led_connect = (Button)findViewById(R.id.led_connect);
        led_ble = (Button)findViewById(R.id.led_ble);
        led_scan = (Button)findViewById(R.id.led_scan);
        led_error = (Button)findViewById(R.id.led_error);
        tv_active_configname = (TextView)findViewById(R.id.tv_active_configname);
        spinner_configname = (Spinner) findViewById(R.id.spinner_configname);
        lv_configs = (ListView) findViewById(R.id.lv_configs);
        calProgress = (ProgressBar)findViewById(R.id.calProgress);
        progressBarinsideText = (TextView)findViewById(R.id.progressBarinsideText);

        spinner_configname.setOnItemSelectedListener(Configname_ItemSelect_Listener);
        AnimationDrawable ani = (AnimationDrawable)getResources().getDrawable(R.drawable.led_flashing);
        led_ble.setCompoundDrawablesWithIntrinsicBounds( ani, null, null, null);
        ani.start();
        led_connect.setOnClickListener(Button_Listener);
        led_ble.setOnClickListener(Button_Listener);
        led_scan.setOnClickListener(Button_Listener);
        led_error.setOnClickListener(Button_Listener);
    }
    private Button.OnClickListener Button_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId())
            {
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
            setActivityTouchDisable(false);
        }
    }
    public class WriteScanConfigStatusReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            byte status[] = intent.getByteArrayExtra(ISCMetaScanSDK.RETURN_WRITE_SCAN_CONFIG_STATUS);
            if((int)status[0] == 1)
            {
                if((int)status[2] == -1 && (int)status[3]==-1)
                    Dialog_Pane("Fail","Set configuration fail!");
                else
                    ISCMetaScanSDK.ReadCurrentScanConfig();
            }
            else if((int)status[0] == -1)
                Dialog_Pane("Fail","Set configuration fail!");
            else if((int)status[0] == -2)
                Dialog_Pane("Fail","Set configuration fail! Hardware not compatible!");
            else if((int)status[0] == -3)
                Dialog_Pane("Fail","Set configuration fail! Function is currently locked!" );
        }
    }
    public class ReturnCurrentScanConfigurationDataReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Boolean flag = CommonAPI.Compareconfig(intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_CURRENT_CONFIG_DATA),CurrentScanConfig);
            if(flag)
            {
                calProgress.setVisibility(View.GONE);
                progressBarinsideText.setVisibility(View.GONE);
                intent = new Intent(mContext, ScanViewActivity.class);
                startActivity(intent);
                finish();
            }
            else
                Dialog_Pane("Fail","Set configuration fail.");
        }
    }
    private void setActivityTouchDisable(boolean value) {
        if (value) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    private void  DisplayScanConfig()
    {
        SetConfigSpinner();
        //tv_active_configname.setText(listScanConfig.ActiveConfigName);
        int ConfigIndex = spinner_configname.getSelectedItemPosition();
        ShowtheConfigDetail(ConfigIndex);
    }
    /**
     * Broadcast Receiver handling the disconnect event. If the Nano disconnects,
     * this activity should finish so that the user is taken back to the {@link HomeViewActivity}
     */
    public class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //.makeText(mContext, R.string.nano_disconnected, Toast.LENGTH_SHORT).show();
            AppStatus = CommonStruct.APPStatus.Home;
            finish();
        }
    }
    private void SetConfigSpinner()
    {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, listScanConfig.ScanConfigName);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_configname.setAdapter(spinnerArrayAdapter);
    }
    private void ShowtheConfigDetail(int index)
    {
        sections.clear();
        int numSections = listScanConfig.Configs.get(index).getSlewNumSections();
        int i;
        for(i = 0; i < numSections; i++){
            sections.add(new ISCMetaScanSDK.SlewScanSection(listScanConfig.Configs.get(index).getSectionScanType()[i],
                    listScanConfig.Configs.get(index).getSectionWidthPx()[i],
                    (listScanConfig.Configs.get(index).getSectionWavelengthStartNm()[i] & 0xFFFF),
                    (listScanConfig.Configs.get(index).getSectionWavelengthEndNm()[i] & 0xFFFF),
                    listScanConfig.Configs.get(index).getSectionNumPatterns()[i] & 0x0FFF,
                    listScanConfig.Configs.get(index).getSectionNumRepeats()[i],
                    listScanConfig.Configs.get(index).getSectionExposureTime()[i]  & 0x000F));
        }
        String Msg = HaveValidScanConfig(sections);
        if(Msg.length() == 0)
        {
            slewScanConfAdapter = new SlewScanConfAdapter(mContext, sections);
            slewScanConfAdapter.notifyDataSetChanged();
            lv_configs.setAdapter(slewScanConfAdapter);
            lv_configs.invalidateViews();
            lv_configs.refreshDrawableState();
        }
        else
            Dialog_Pane("Warning",Msg);
    }
    private Spinner.OnItemSelectedListener Configname_ItemSelect_Listener = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            ShowtheConfigDetail(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
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
                setActivityTouchDisable(false);
                calProgress.setVisibility(View.GONE);
                progressBarinsideText.setVisibility(View.GONE);
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
                    }
                }
            }
        }
    }
}
