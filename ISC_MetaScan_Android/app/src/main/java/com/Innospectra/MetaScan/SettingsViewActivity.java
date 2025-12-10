package com.Innospectra.MetaScan;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import static com.ISCMetaScanSDK.ISCMetaScanSDK.getStringPref;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.storeStringPref;

/**
 * This activity controls the view for global settings. These settings do not require a Nano
 * to be connected.
 *
 * The user can change temperature and spatial frequency units, as well as set and clear a
 * preferred Nano device
 *
 * @author collinmast
 */
public class SettingsViewActivity extends Activity {

    private TextView tv_version;
    private Button btn_set;
    private Button btn_forget;
    private AlertDialog alertDialog;
    private TextView tv_pref_nano;
    private EditText et_devicefilter;
    private EditText et_rssifilter;
    private Switch switch_actconfig_reminder;
    private String preferredNano;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.activity_settings);
        mContext = this;
        InitComponent();

        //Set up action bar up indicator
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }
    private void InitComponent()
    {
        tv_version = (TextView) findViewById(R.id.tv_version);
        btn_set = (Button) findViewById(R.id.btn_set);
        btn_forget = (Button) findViewById(R.id.btn_forget);
        tv_pref_nano = (TextView) findViewById(R.id.tv_pref_nano);
        et_devicefilter = (EditText)findViewById(R.id.et_devicefilter);
        et_rssifilter = (EditText)findViewById(R.id.et_rssifilter);
        switch_actconfig_reminder = (Switch)findViewById(R.id.switch_actconfig_reminder);

        String devicename = ISCMetaScanSDK.getStringPref(mContext,ISCMetaScanSDK.SharedPreferencesKeys.DeviceFilter,"NIR");
        et_devicefilter.setText(devicename);
        et_devicefilter.setOnEditorActionListener(Device_Filter_OnEditor);
        String rssi = ISCMetaScanSDK.getStringPref(mContext,ISCMetaScanSDK.SharedPreferencesKeys.RSSIFilter,"-100");
        et_rssifilter.setText(rssi);
        et_rssifilter.setOnEditorActionListener(RSSI_Filter_OnEditor);
        Boolean IsReminder = ISCMetaScanSDK.getBooleanPref(mContext,ISCMetaScanSDK.SharedPreferencesKeys.ACTConfigReminder,true);
        switch_actconfig_reminder.setChecked(IsReminder);
        switch_actconfig_reminder.setOnClickListener(ACTConfigReminder_OnClick);
    }
    private Switch.OnClickListener ACTConfigReminder_OnClick = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            ISCMetaScanSDK.storeBooleanPref(mContext,ISCMetaScanSDK.SharedPreferencesKeys.ACTConfigReminder,switch_actconfig_reminder.isChecked());
        }
    };
    private EditText.OnEditorActionListener Device_Filter_OnEditor = new EditText.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                ISCMetaScanSDK.storeStringPref(mContext,ISCMetaScanSDK.SharedPreferencesKeys.DeviceFilter,et_devicefilter.getText().toString());
            }
            return false;
        }
    };
    private EditText.OnEditorActionListener RSSI_Filter_OnEditor = new EditText.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                int rssi = -100;
                try {
                    rssi = Integer.parseInt(et_rssifilter.getText().toString());
                    if(rssi > 0)
                        rssi *=-1;
                }catch (Exception e)
                {
                }
                et_rssifilter.setText(Integer.toString(rssi));
                ISCMetaScanSDK.storeStringPref(mContext,ISCMetaScanSDK.SharedPreferencesKeys.RSSIFilter,et_rssifilter.getText().toString());
            }
            return false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //Initialize preferred device
        preferredNano = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
        //Retrieve package information for displaying version info
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            tv_version.setText(getString(R.string.version, version, versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            tv_version.setText("");
        }

        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, SelectDeviceViewActivity.class));
            }
        });
        tv_pref_nano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, SelectDeviceViewActivity.class));
            }
        });

        if(preferredNano == null){
            btn_forget.setEnabled(false);
        }else{
            btn_forget.setEnabled(true);
        }
        btn_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferredNano != null) {
                    confirmationDialog(preferredNano);
                }
            }
        });
        //Update set button and field based on whether a preferred nano has been set or not
        if (preferredNano != null) {
            btn_set.setVisibility(View.INVISIBLE);
            tv_pref_nano.setText(getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDeviceModel, null));
            tv_pref_nano.setVisibility(View.VISIBLE);
        } else {
            btn_set.setVisibility(View.VISIBLE);
            tv_pref_nano.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * When the activity is destroyed, make a call to super class
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
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
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Function for displaying the dialog to confirm clearing the stored Nano
     * @param mac the mac address of the stored Nano
     */
    public void confirmationDialog(String mac) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.nano_confirmation_title));
        alertDialogBuilder.setMessage(mContext.getResources().getString(R.string.nano_forget_msg, mac));

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                storeStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
                storeStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDeviceModel, null);
                btn_set.setVisibility(View.VISIBLE);
                tv_pref_nano.setVisibility(View.INVISIBLE);
                btn_forget.setEnabled(false);
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
}
