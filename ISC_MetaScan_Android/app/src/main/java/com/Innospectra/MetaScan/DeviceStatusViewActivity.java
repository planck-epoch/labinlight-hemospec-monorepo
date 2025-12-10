package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
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
import static com.Innospectra.MetaScan.CommonStruct.deviceInfo;
import static com.Innospectra.MetaScan.CommonStruct.deviceStatus;

public class DeviceStatusViewActivity extends Activity {
    ListView listview_devicestatus;
    ProgressBar calProgress;
    TextView progressBarinsideText;
    Button btn_status;
    Button btn_info;
    Button btn_error;
    private Menu mMenu;

    private final BroadcastReceiver DisconnectReceiver = new DisconnectReceiver();
    private final BroadcastReceiver GetDeviceStatusReceiver = new GetDeviceStatusReceiver();
    private final BroadcastReceiver HomeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();
    private final IntentFilter DisconnectFilter = new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED);
    private final IntentFilter GetDeviceStatusFilter = new IntentFilter(ISCMetaScanSDK.ACTION_STATUS);
    Context mContext;

    ButtonStatus buttonStatus = ButtonStatus.Status;
    private enum ButtonStatus
    {
        Status,
        Info,
        Error
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.devicestatus_view);
        mContext = this;
        InitialCompont();
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        LocalBroadcastManager.getInstance(mContext).registerReceiver(GetDeviceStatusReceiver, GetDeviceStatusFilter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(DisconnectReceiver, DisconnectFilter);
        registerReceiver(HomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), Context.RECEIVER_NOT_EXPORTED);

        ISCMetaScanSDK.GetDeviceStatus();
        setActivityTouchDisable(true);
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
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(GetDeviceStatusReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(DisconnectReceiver);
        unregisterReceiver(HomeKeyEventReceiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_devicestatus, menu);
        mMenu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        else if(id == R.id.action_clearerror)
        {
            ISCMetaScanSDK.ClearDeviceError();
            CommonStruct.AppStatus = CommonStruct.APPStatus.Home;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){

                @Override
                public void run() {
                    finish();
                }}, 300);
        }
        return super.onOptionsItemSelected(item);
    }
    private void InitialCompont()
    {
        listview_devicestatus = (ListView)findViewById(R.id.listview_devicestatus);
        calProgress = (ProgressBar) findViewById(R.id.calProgress);
        progressBarinsideText = (TextView) findViewById(R.id.progressBarinsideText);
        btn_status = (Button)findViewById(R.id.btn_status);
        btn_info = (Button)findViewById(R.id.btn_info);
        btn_error = (Button)findViewById(R.id.btn_error);

        btn_status.setOnClickListener(Button_Listener);
        btn_info.setOnClickListener(Button_Listener);
        btn_error.setOnClickListener(Button_Listener);
        listview_devicestatus.setOnItemClickListener(ListView_Listenster);
    }
    private Button.OnClickListener Button_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.btn_status:
                    buttonStatus = ButtonStatus.Status;
                    btn_status.setBackgroundColor(getResources().getColor(R.color.orange));
                    btn_info.setBackgroundColor(getResources().getColor(R.color.gainsboro));
                    btn_error.setBackgroundColor(getResources().getColor(R.color.gainsboro));
                    calProgress.setVisibility(View.VISIBLE);
                    progressBarinsideText.setVisibility(View.VISIBLE);
                    listview_devicestatus.setVisibility(View.GONE);
                    ISCMetaScanSDK.GetDeviceStatus();
                    setActivityTouchDisable(true);
                    break;
                case R.id.btn_info:
                    buttonStatus = ButtonStatus.Info;
                    btn_status.setBackgroundColor(getResources().getColor(R.color.gainsboro));
                    btn_info.setBackgroundColor(getResources().getColor(R.color.orange));
                    btn_error.setBackgroundColor(getResources().getColor(R.color.gainsboro));
                    GetDeviceInformationView();
                    break;
                case R.id.btn_error:
                    buttonStatus = ButtonStatus.Error;
                    btn_status.setBackgroundColor(getResources().getColor(R.color.gainsboro));
                    btn_info.setBackgroundColor(getResources().getColor(R.color.gainsboro));
                    btn_error.setBackgroundColor(getResources().getColor(R.color.orange));
                    GetErrorStatusView();
                    break;
            }
        }
    };
    public class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(mContext, R.string.nano_disconnected, Toast.LENGTH_SHORT).show();
            AppStatus = CommonStruct.APPStatus.Home;
            finish();
        }
    }
    private void setActivityTouchDisable(boolean value) {
        if (value) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    public static String GetLampTimeString(long lamptime)
    {
        String lampusage = "";
        if (lamptime / 86400 != 0)
        {
            lampusage += lamptime / 86400 + "day ";
            lamptime -= 86400 * (lamptime / 86400);
        }
        if (lamptime / 3600 != 0)
        {
            lampusage += lamptime / 3600 + "hr ";
            lamptime -= 3600 * (lamptime / 3600);
        }
        if (lamptime / 60 != 0)
        {
            lampusage += lamptime / 60 + "min ";
            lamptime -= 60 * (lamptime / 60);
        }
        lampusage += lamptime + "sec ";
        return lampusage;
    }
    public class GetDeviceStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            deviceStatus = new CommonStruct.DeviceStatus();
            int batt = intent.getIntExtra(ISCMetaScanSDK.EXTRA_BATT, 0);
            float temp = intent.getFloatExtra(ISCMetaScanSDK.EXTRA_TEMP, 0);
            float humid = intent.getFloatExtra(ISCMetaScanSDK.EXTRA_HUMID, 0);
            long lamptime = intent.getLongExtra(ISCMetaScanSDK.EXTRA_LAMPTIME,0);

            deviceStatus.Battery = getString(R.string.batt_level_value, batt);
            deviceStatus.Temperature = getString(R.string.temp_value_c, Integer.toString((int) temp));
            deviceStatus.Humidity = getString(R.string.humid_value,Integer.toString((int) humid));
            deviceStatus.LampTime = GetLampTimeString(lamptime);
            deviceStatus.DevStatus = intent.getStringExtra(ISCMetaScanSDK.EXTRA_DEV_STATUS);
            deviceStatus.ErrorStatus = intent.getStringExtra(ISCMetaScanSDK.EXTRA_ERR_STATUS);
            deviceStatus.DevByte = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_DEV_STATUS_BYTE);
            deviceStatus.ErrorByte = intent.getByteArrayExtra(ISCMetaScanSDK.EXTRA_ERR_BYTE);
            setActivityTouchDisable(false);
            DeviceStatusView();
            calProgress.setVisibility(View.GONE);
            progressBarinsideText.setVisibility(View.GONE);
            listview_devicestatus.setVisibility(View.VISIBLE);
        }
    }
    private void DeviceStatusView()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String[] title = { "Tiva", "Scanning", "BLE stack", "BLE connection", "Scan Data Interpreting", "Scan Button Pressed", "Battery in charge","","Battery Capacity","Total Lamp Time","Temperature","Humidity"};
        int[] images = { R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray,
                R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_empty, R.drawable.leg_empty, R.drawable.leg_empty, R.drawable.leg_empty, R.drawable.leg_empty};
        int data = deviceStatus.DevByte[0] | (deviceStatus.DevByte[1] << 8);
        int tiva = 0x00000001;
        for(int j=0;j<2;j++)
        {
            int ret = data & tiva;
            if(ret == tiva)
                images[j] = R.drawable.led_g;
            tiva = tiva<<1;
        }
        tiva = tiva<<2;
        for(int j=2;j<7;j++)
        {
            int ret = data & tiva;
            if(ret == tiva)
                images[j] = R.drawable.led_g;
            tiva = tiva<<1;
        }
        for (int i = 0; i < title.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("images", images[i]);
            map.put("title", title[i]);
            switch (title[i])
            {
                case "Battery Capacity":
                    map.put("value", deviceStatus.Battery);
                    break;
                case "Total Lamp Time":
                    map.put("value",deviceStatus.LampTime);
                    break;
                case "Temperature":
                    map.put("value",deviceStatus.Temperature);
                    break;
                case "Humidity":
                    map.put("value",deviceStatus.Humidity);
                    break;
                default:
                    map.put("value","");
                    break;
            }
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list,
                R.layout.devicestatus_detail, new String[] { "images", "title","value" }, new int[] {
                R.id.image, R.id.textView1,R.id.textView2 });
        listview_devicestatus.setAdapter(adapter);
    }
    private void GetDeviceInformationView()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String[] title = { "Manufacturer", "Model Number", "Serial Number", "Deivce UUID", "Hardware Rev.", "TIVA Rev."};
        int[] images = { R.drawable.leg_empty, R.drawable.leg_empty, R.drawable.leg_empty, R.drawable.leg_empty, R.drawable.leg_empty,R.drawable.leg_empty};
        for (int i = 0; i < title.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("images", images[i]);
            map.put("title", title[i]);
            switch (i)
            {
                case 0:
                    map.put("value", deviceInfo.Manufacturer);
                    break;
                case 1:
                    map.put("value",deviceInfo.ModelName);
                    break;
                case 2:
                    map.put("value",deviceInfo.SerialNum);
                    break;
                case 3:
                    map.put("value",deviceInfo.UUID);
                    break;
                case 4:
                    map.put("value",deviceInfo.HWVer);
                    break;
                case 5:
                    map.put("value",deviceInfo.TIVAVer);
                    break;
            }
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list,
                R.layout.devicestatus_detail, new String[] { "images", "title","value" }, new int[] {
                R.id.image, R.id.textView1,R.id.textView2 });
        listview_devicestatus.setAdapter(adapter);
    }
    private void GetErrorStatusView()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String[] title = { "Scan", "ADC", "EEPROM", "Bluetooth", "Spectrum Library", "Hardware","TMP006" ,"HDC1000","Battery","Memory","UART","System"};
        int[] images = { R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray,
                R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray, R.drawable.leg_gray,R.drawable.leg_gray,R.drawable.leg_gray,R.drawable.leg_gray,R.drawable.leg_gray,R.drawable.leg_gray};
        int data = deviceStatus.ErrorByte[0]&0xFF | (deviceStatus.ErrorByte[1] << 8);//0XFF avoid nagtive number
        int error_scan = 0x00000001;
        for(int j=0;j<2;j++)
        {
            int ret = data & error_scan;
            if(ret == error_scan)
            {
                images[j] = R.drawable.led_r;
            }
            error_scan = error_scan<<1;
        }
        error_scan = error_scan<<1;

        for(int j=2;j<12;j++)
        {
            int ret = data & error_scan;
            if(ret == error_scan)
            {
                images[j] = R.drawable.led_r;
            }
            error_scan = error_scan<<1;
        }
        for (int i = 0; i < images.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("images", images[i]);
            map.put("title", title[i]);
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list,
                R.layout.devicestatus_detail, new String[] { "images", "title","value" }, new int[] {
                R.id.image, R.id.textView1,R.id.textView2 });
        listview_devicestatus.setAdapter(adapter);
    }
    private AdapterView.OnItemClickListener ListView_Listenster = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(buttonStatus!= ButtonStatus.Error)
                return;
            switch (position)
            {
                case 0://Scan
                case 1://ADC
                case 5://Hardware
                case 6://TMP006
                case 7://HDC1000
                case 8://Battery
                case 11://System
                    Intent graphIntent = new Intent(mContext, AdvanceErrorStatusViewActivity.class);
                    graphIntent.putExtra("POS",position);
                    startActivity(graphIntent);
                    break;
            }
        }

    };
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
