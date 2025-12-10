package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.Innospectra.MetaScan.CommonStruct.AppStatus;
import static com.Innospectra.MetaScan.CommonStruct.deviceStatus;

public class AdvanceErrorStatusViewActivity extends Activity {
    Context mContext;
    ListView listview_advance_errorstatus;
    private final BroadcastReceiver DisconnectReceiver = new DisconnectReceiver();
    private final BroadcastReceiver HomeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();
    private final IntentFilter DisconnectFilter = new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED);
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.advance_errorstatus_view);
        mContext = this;
        InitialCompont();
        Bundle bundle = getIntent().getExtras();
        int pos =  bundle.getInt("POS");
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            switch (pos)
            {
                case 0://Scan
                    ab.setTitle(getString(R.string.detail_scan_error_status));
                    break;
                case 1://ADC
                    ab.setTitle(getString(R.string.detail_adc_error_status));
                    break;
                case 5://Hardware
                    ab.setTitle(getString(R.string.detail_hw_error_status));
                    break;
                case 6://TMP006
                    ab.setTitle(getString(R.string.detail_tmp006_error_status));
                    break;
                case 7://HDC1000
                    ab.setTitle(getString(R.string.detail_hdc1000_error_status));
                    break;
                case 8://Battery
                    ab.setTitle(getString(R.string.detail_battery_error_status));
                    break;
                case 11://System
                    ab.setTitle(getString(R.string.detail_system_error_status));
                    break;
            }

        }
        GetAdvanceErrorStatusView(pos);
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
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DisconnectReceiver);
        unregisterReceiver(HomeKeyEventReceiver);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void InitialCompont()
    {
        listview_advance_errorstatus = (ListView)findViewById(R.id.listview_advance_errorstatus);
    }
    public class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(mContext, R.string.nano_disconnected, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void GetAdvanceErrorStatusView(int pos)
    {
        String[] title0 = {"DLPC150 Boot Error", "DLPC150 Init Error", "DLPC150 Lamp Driver Error", "DLPC150 Crop Image Failed", "ADC Data Error", "CFG Invalid", "Scan Pattern Streaming", "DLPC150 Read Error"};
        String[] title1 = {"Timeout", "Power Down", "Power Up", "Standby", "Wake up", "Read Register", "Write Register", "Configure", "Set Buffer","Command","Set PGA"};
        String[] title5 = {"DLPC150","UUID","Flash init"};
        String[] title6 = {"Manufacturing Id", "Device Id", "Reset", "Read Register", "Write Register", "Timeout", "I2C"};
        String[] title7 = {"Manufacturing Id", "Device Id", "Reset", "Read Register", "Write Register", "Timeout", "I2C"};
        String[] title8 = {"Under Voltage"};
        String[] title11 = {"Unstable Lamp ADC", "Unstable Peak Intensity", "ADS1255 Error", "Auto PGA Error","Unstable Scan In Repeated times"};

        int[]  images0 = {R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray,
                R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray};
        int[]  images1 = {R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray,
                R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray};
        int[]  images5 = {R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray};
        int[]  images6 = {R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray,
                R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray};
        int[]  images7 = {R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray,
                R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray};
        int[]  images8 = {R.drawable.leg_gray};
        int[]  images11 = {R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray};

        switch (pos) {
            case 0://Scan
                int data0 = deviceStatus.ErrorByte[4]&0xFF;//avoid negative number
                int error_scan0 = 0x00000001;
                for (int j = 0; j < 8; j++) {
                    int ret = data0 & error_scan0;
                    if (ret == error_scan0) {
                        images0[j] = R.drawable.led_r;
                    }
                    error_scan0 = error_scan0 << 1;
                }
                SetListView(title0,images0);
                break;
            case 1://ADC
                int data1 = deviceStatus.ErrorByte[5];
                for(int i=10;i>=0;i--)
                {
                    if(data1>=(i+1))
                    {
                        images1[i] = R.drawable.led_r;
                        data1 -= (i+1);
                    }
                }
                SetListView(title1,images1);
                break;
            case 5://Hardware
                int data5 = deviceStatus.ErrorByte[11];
                for(int i=2;i>=0;i--)
                {
                    if(data5>=(i+1))
                    {
                        images5[i] = R.drawable.led_r;
                        data5 -= (i+1);
                    }
                }
                SetListView(title5,images5);
                break;
            case 6://TMP006
                int data6 = deviceStatus.ErrorByte[12];
                for(int i=6;i>=0;i--)
                {
                    if(data6>=(i+1))
                    {
                        images6[i] = R.drawable.led_r;
                        data6 -= (i+1);
                    }
                }
                SetListView(title6,images6);
                break;
            case 7://HDC1000
                int data7 = deviceStatus.ErrorByte[13];
                for(int i=6;i>=0;i--)
                {
                    if(data7>=(i+1))
                    {
                        images7[i] = R.drawable.led_r;
                        data7 -= (i+1);
                    }
                }
                SetListView(title7,images7);
                break;
            case 8://Battery
                int data8 = deviceStatus.ErrorByte[14];
                if(data8 == 1)
                {
                    images8[0] = R.drawable.led_r;
                }
                SetListView(title8,images8);
                break;
            case 11://System
                int data11 = deviceStatus.ErrorByte[17]&0xFF;//avoid negative number
                int error_scan11 = 0x00000001;
                for (int j = 0; j < 5; j++) {
                    int ret = data11 & error_scan11;
                    if (ret == error_scan11) {
                        images11[j] = R.drawable.led_r;
                    }
                    error_scan11 = error_scan11 << 1;
                }
                //Unstable scan in repeated times
                data11 = deviceStatus.ErrorByte[18]&0xFF;
                if ((data11 & 0x01) > 0)
                    images11[4] = R.drawable.led_r;
                SetListView(title11,images11);
                break;
        }
    }
    private void SetListView(String[] title,int[]image)
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < image.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("images", image[i]);
            map.put("title", title[i]);
            list.add(map);

        }
        SimpleAdapter adapter = new SimpleAdapter(this, list,
                R.layout.devicestatus_detail, new String[] { "images", "title","value" }, new int[] {
                R.id.image, R.id.textView1,R.id.textView2 });
        listview_advance_errorstatus.setAdapter(adapter);
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
