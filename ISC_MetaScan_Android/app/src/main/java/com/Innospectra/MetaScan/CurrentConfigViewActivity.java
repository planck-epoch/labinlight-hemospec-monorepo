package com.Innospectra.MetaScan;

import static com.Innospectra.MetaScan.CommonAPI.HaveValidScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.CurrentScanConfig;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.util.ArrayList;

public class CurrentConfigViewActivity extends Activity {
    Button led_connect;
    Button led_ble;
    Button led_scan;
    Button led_error;
    TextView tv_active_configname;
    TextView lb_active_configname;
    TextView lb_configname;
    Spinner spinner_configname;
    ListView lv_configs;
    Context mContext;
    private AlertDialog alertDialog;

    private final BroadcastReceiver DisconnectReceiver = new DisconnectReceiver();
    private final BroadcastReceiver HomeKeyEventReceiver = new HomeKeyEventBroadCastReceiver();
    private final IntentFilter DisconnectFilter = new IntentFilter(ISCMetaScanSDK.ACTION_GATT_DISCONNECTED);

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.select_config_view);
        mContext = this;
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        InitComponent();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(DisconnectReceiver, DisconnectFilter);
        registerReceiver(HomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), Context.RECEIVER_NOT_EXPORTED);
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
    private void InitComponent()
    {
        led_connect = (Button)findViewById(R.id.led_connect);
        led_ble = (Button)findViewById(R.id.led_ble);
        led_scan = (Button)findViewById(R.id.led_scan);
        led_error = (Button)findViewById(R.id.led_error);
        lb_active_configname = (TextView)findViewById(R.id.lb_active_configname);
        tv_active_configname = (TextView)findViewById(R.id.tv_active_configname);
        lb_configname = (TextView)findViewById(R.id.lb_configname);
        spinner_configname = (Spinner) findViewById(R.id.spinner_configname);
        lv_configs = (ListView) findViewById(R.id.lv_configs);

        led_connect.setVisibility(View.GONE);
        led_ble.setVisibility(View.GONE);
        led_scan.setVisibility(View.GONE);
        led_error.setVisibility(View.GONE);
        spinner_configname.setVisibility(View.GONE);
        lb_configname.setVisibility(View.GONE);
        lb_active_configname.setVisibility(View.VISIBLE);
        lb_active_configname.setText(getResources().getString(R.string.title_currentconfig) + " :");
        tv_active_configname.setVisibility(View.VISIBLE);
        tv_active_configname.setText(CurrentScanConfig.getConfigName());
        ShowtheConfigDetail();
    }
    private void ShowtheConfigDetail()
    {
        SlewScanConfAdapter slewScanConfAdapter;
        ArrayList<ISCMetaScanSDK.SlewScanSection> sections = new ArrayList<>();
        int numSections = CurrentScanConfig.getSlewNumSections();
        int i;
        for(i = 0; i < numSections; i++){
            sections.add(new ISCMetaScanSDK.SlewScanSection(CurrentScanConfig.getSectionScanType()[i],
                    CurrentScanConfig.getSectionWidthPx()[i],
                    (CurrentScanConfig.getSectionWavelengthStartNm()[i] & 0xFFFF),
                    (CurrentScanConfig.getSectionWavelengthEndNm()[i] & 0xFFFF),
                    CurrentScanConfig.getSectionNumPatterns()[i] & 0x0FFF,
                    CurrentScanConfig.getSectionNumRepeats()[i],
                    CurrentScanConfig.getSectionExposureTime()[i]  & 0x000F));
        }
        String Msg = HaveValidScanConfig(sections);
        if(Msg.length()==0)
        {
            slewScanConfAdapter = new SlewScanConfAdapter(mContext,sections);
            slewScanConfAdapter.notifyDataSetChanged();
            lv_configs.setAdapter(slewScanConfAdapter);
            lv_configs.invalidateViews();
            lv_configs.refreshDrawableState();
        }
        else
            Dialog_Pane("Warning",Msg);
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
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    public class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(mContext, R.string.nano_disconnected, Toast.LENGTH_SHORT).show();
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
                        Intent NotifyBackground = new Intent(ScanViewActivity.NOTIFY_BACKGROUND);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(NotifyBackground);
                        finish();
                    }
                }
            }
        }
    }
}
