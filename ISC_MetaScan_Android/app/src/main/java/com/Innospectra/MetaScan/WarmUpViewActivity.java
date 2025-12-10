package com.Innospectra.MetaScan;

import static com.ISCMetaScanSDK.ISCMetaScanSDK.getBooleanPref;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.getIntegerPref;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.storeBooleanPref;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.storeIntegerPref;
import static com.Innospectra.MetaScan.CommonAPI.UpdateWarmUpTimeInList;
import static com.Innospectra.MetaScan.CommonAPI.WriteWarmUpData;
import static com.Innospectra.MetaScan.CommonStruct.AppStatus;
import static com.Innospectra.MetaScan.CommonStruct.PreferenceKey.WarmUpSetting;
import static com.Innospectra.MetaScan.CommonStruct.PreferenceKey.WarmUpSettingTime;
import static com.Innospectra.MetaScan.CommonStruct.currentDeviceStatus;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WarmUpViewActivity extends Activity {

    private static Context mContext;
    CheckBox cb_warmupdetect;
    Spinner spinner_warmuptime;
    ProgressBar progress_warmup;
    TextView tv_warmupstatus;
    Button bt_warmup_start;
    TextView tv_lastwarmuptime;
    private int WarmUpTimeProcess = 0;
    private Handler WarmUpHandler;
    private int ShouldWarmSec = 0;
    private Boolean StartWarmUp = false;
    private Boolean Exist = false;
    private Boolean FinishWarmUp = false;

    private final BroadcastReceiver SetLampStateCompleteReceiver = new SetLampStateCompleteReceiver();
    private final BroadcastReceiver HomeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();
    private final BroadcastReceiver DisconnectReceiver = new DisconnectReceiver();
    private final IntentFilter SetLampStateCompleteFilter = new IntentFilter(ISCMetaScanSDK.SET_LAMPSTATE_COMPLETE);
    private final IntentFilter DisconnectFilter = new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.activity_warmup);
        mContext = this;
        StartWarmUp = false;
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        initComponent();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(SetLampStateCompleteReceiver, SetLampStateCompleteFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(DisconnectReceiver, DisconnectFilter);
        registerReceiver(HomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), Context.RECEIVER_NOT_EXPORTED);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(AppStatus == CommonStruct.APPStatus.Home)
            finish();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
   
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(SetLampStateCompleteReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DisconnectReceiver);
        unregisterReceiver(HomeKeyEventReceiver);
    }
    /**
     * Inflate the options menu
     * In this case, there is no menu and only an up indicator,
     * so the function should always return true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Handle the selection of a menu item.
     * In this case, there is are two items, the up indicator, and the settings button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            storeBooleanPref(mContext, WarmUpSetting,cb_warmupdetect.isChecked());
            storeIntegerPref(mContext, WarmUpSettingTime,spinner_warmuptime.getSelectedItemPosition());
            currentDeviceStatus.ForceWarmUp = cb_warmupdetect.isChecked();
            if(StartWarmUp)
            {
                Exist = true;
                ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
            }
            else
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void initComponent()
    {
        cb_warmupdetect = (CheckBox) findViewById(R.id.cb_warmupdetect);
        spinner_warmuptime = (Spinner) findViewById(R.id.spinner_warmuptime);
        progress_warmup = (ProgressBar) findViewById(R.id.progress_warmup);
        tv_warmupstatus = (TextView) findViewById(R.id.tv_warmupstatus);
        bt_warmup_start = (Button) findViewById(R.id.bt_warmup_start);
        tv_lastwarmuptime = (TextView) findViewById(R.id.tv_lastwarmuptime);

        bt_warmup_start.setOnClickListener(Button_Listener);
        ArrayAdapter<CharSequence> adapter_warmuptime = ArrayAdapter.createFromResource(this,
                R.array.warmup_time_array, android.R.layout.simple_spinner_item);
        adapter_warmuptime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_warmuptime.setAdapter(adapter_warmuptime);
        spinner_warmuptime.setOnItemSelectedListener(Spinner_Listener);
        spinner_warmuptime.setSelection(getIntegerPref(mContext, WarmUpSettingTime,1));

        progress_warmup.setMax(100);
        progress_warmup.setProgress(100);
        tv_warmupstatus.setText(GetWarmUpSec(Integer.parseInt(spinner_warmuptime.getSelectedItem().toString())*60));

        currentDeviceStatus.ForceWarmUp = getBooleanPref(mContext, WarmUpSetting,true);
        cb_warmupdetect.setChecked(currentDeviceStatus.ForceWarmUp);
        if(currentDeviceStatus.WarmUpTime == null)
            tv_lastwarmuptime.setVisibility(View.INVISIBLE);
        else
            ShowWarmUpTime();
    }
    private void ShowWarmUpTime()
    {
        tv_lastwarmuptime.setVisibility(View.VISIBLE);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        tv_lastwarmuptime.setText("Last Warm Up Time : " + simpleDateFormat.format(currentDeviceStatus.WarmUpTime));
    }
    private Spinner.OnItemSelectedListener Spinner_Listener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            progress_warmup.setMax(100);
            progress_warmup.setProgress(100);
            tv_warmupstatus.setText(GetWarmUpSec(Integer.parseInt(spinner_warmuptime.getSelectedItem().toString())*60));
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    private Button.OnClickListener Button_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            bt_warmup_start.setVisibility(View.INVISIBLE);
            StartWarmUp = true;
            FinishWarmUp = false;
            ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.ON);
            ShouldWarmSec = Integer.parseInt(spinner_warmuptime.getSelectedItem().toString())*60;
            progress_warmup.setMax(ShouldWarmSec);
            progress_warmup.setProgress(ShouldWarmSec);
            WarmUpTimeProcess = ShouldWarmSec;
            WarmUpHandler = new Handler();
            WarmUpHandler.post(WarmUprunnable);
        }
    };
    //region Warm Up
    final Runnable WarmUprunnable = new Runnable() {
        public void run() {
            WarmUpTimeProcess--;
            if (WarmUpTimeProcess >=0) {
                tv_warmupstatus.setText(GetWarmUpSec(WarmUpTimeProcess));
                WarmUpHandler.postDelayed(WarmUprunnable, 1000);
                progress_warmup.setProgress(WarmUpTimeProcess);
            }else{
                Calendar c = Calendar.getInstance();
                currentDeviceStatus.WarmUpTime = c.getTime();
                StartWarmUp = false;
                FinishWarmUp = true;
                ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
                WarmUpHandler.removeCallbacks(WarmUprunnable);
            }
        }
    };
    private String GetWarmUpSec(int sec)
    {
        String DisplaySec = "";
        DisplaySec += "0";
        if(sec >= 60)
        {
            int min = sec/60;
            DisplaySec+=min;
            DisplaySec+=":";
            sec -= 60*min;
        }
        else
            DisplaySec += "0:";
        if(sec < 10)
        {
            DisplaySec += "0";
            DisplaySec += sec;
        }
        else
            DisplaySec += sec;
        return DisplaySec;
    }
    //endregion
    public class SetLampStateCompleteReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
           if(Exist)
               finish();
           else if(FinishWarmUp)
           {
               bt_warmup_start.setVisibility(View.VISIBLE);
               ShowWarmUpTime();
               UpdateWarmUpTimeInList();
               WriteWarmUpData();
               finish();
           }
        }
    }
    public class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(mContext, R.string.nano_disconnected, Toast.LENGTH_SHORT).show();
            CommonStruct.AppStatus = CommonStruct.APPStatus.Home;
            finish();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            storeBooleanPref(mContext, WarmUpSetting,cb_warmupdetect.isChecked());
            storeIntegerPref(mContext, WarmUpSettingTime,spinner_warmuptime.getSelectedItemPosition());
            currentDeviceStatus.ForceWarmUp = cb_warmupdetect.isChecked();
            if(StartWarmUp)
            {
                Exist = true;
                ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
            }
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
                        if(StartWarmUp)
                            ISCMetaScanSDK.ControlLamp(ISCMetaScanSDK.LampState.AUTO);
                    }
                }
            }
        }
    }
}
