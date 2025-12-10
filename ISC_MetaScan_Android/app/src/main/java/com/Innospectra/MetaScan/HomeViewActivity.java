package com.Innospectra.MetaScan;

import static com.ISCMetaScanSDK.ISCMetaScanSDK.getStringPref;
import static com.ISCMetaScanSDK.ISCMetaScanSDK.storeStringPref;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by iris.lin on 2018/2/2.
 */

public class HomeViewActivity  extends Activity {
    private static Context mContext;
    private ImageButton main_connect;
    private ImageButton main_info;
    private ImageButton main_setting;
    AlertDialog alertDialog;
    private static final int REQUEST_WRITE_STORAGE = 112;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.home_page);
        mContext = this;
        /*ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }*/
        initComponent();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            boolean hasPermission1 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            if(!hasPermission || !hasPermission1)
                DialogPane_LocationPermission();
            else
            {
                boolean hasPermission2 = (ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                if (!hasPermission2) {
                    ActivityCompat.requestPermissions(HomeViewActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.INTERNET
                            },
                            REQUEST_WRITE_STORAGE);
                }
            }
        }
        else if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(HomeViewActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.INTERNET,
                        },
                        REQUEST_WRITE_STORAGE);
            }
            hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
            boolean hasPermission2 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED);
            boolean hasPermission3 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission || !hasPermission2 || !hasPermission3) {
                ActivityCompat.requestPermissions(HomeViewActivity.this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.INTERNET,
                        },
                        REQUEST_WRITE_STORAGE);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        CommonStruct.AppStatus = CommonStruct.APPStatus.None;
    }
   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_right_top_text, menu);
        MenuItem action_righttop_button = menu.findItem(R.id.action_righttop_button);
        SpannableString s = new SpannableString(getResources().getString(R.string.action_reports));
        action_righttop_button.setTitle(s);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_righttop_button)
        {
            Intent reportIntent = new Intent(mContext, ListReportActivity.class);
            startActivity(reportIntent);
        }
        return super.onOptionsItemSelected(item);
    }*/

    private void initComponent()
    {
        main_connect = (ImageButton)findViewById(R.id.main_connect);
        main_info = (ImageButton)findViewById(R.id.main_info);
        main_setting = (ImageButton)findViewById(R.id.main_setting);

        main_connect.setOnClickListener(main_connect_listenser);
        main_info.setOnClickListener(main_info_listenser);
        main_setting.setOnClickListener(main_setting_listenser);
    }
    private Button.OnClickListener main_connect_listenser = new Button.OnClickListener()
    {

        @Override
        public void onClick(View view) {
            CheckPermission();
        }
    };

    private Button.OnClickListener main_info_listenser = new Button.OnClickListener()
    {

        @Override
        public void onClick(View view) {
            Intent infoIntent = new Intent(mContext, InformationViewActivity.class);
            startActivity(infoIntent);
        }
    };

    private Button.OnClickListener main_setting_listenser = new Button.OnClickListener()
    {

        @Override
        public void onClick(View view) {
            Intent settingsIntent = new Intent(mContext, SettingsViewActivity.class);
            startActivity(settingsIntent);
        }
    };
    private void CheckPermission()
    {
        boolean hasPermission1;
        boolean hasPermission2;
        boolean hasPermission3;
        boolean hasPermission4;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            hasPermission1 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            hasPermission2 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            hasPermission3 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            hasPermission4 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

            if(!hasPermission1 || !hasPermission2 || !hasPermission3 || !hasPermission4)
            {
                Dialog_Pane("Warning","Will go to the application information page.\nRequiring permission for location and storage.");
            }
            else
            {
                String DeviceName = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
                if(DeviceName == null || TextUtils.isEmpty(DeviceName))
                    Dialog_Pane_GoToSettingPage("Warning","The device has not been selected yet, it will automatically go to the settings page.");
                else
                {
                    Intent intent = new Intent(mContext, MainSelectConfigViewActivity.class);
                    startActivity(intent);
                }
            }
        }
        else if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            hasPermission1 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
            hasPermission2 = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED);
            if(!hasPermission1 || !hasPermission2)
            {
                Dialog_Pane("Warning","Will go to the application information page.\nShould allow nearby devices permission.");
            }
            else
            {
                String DeviceName = getStringPref(mContext, ISCMetaScanSDK.SharedPreferencesKeys.preferredDevice, null);
                if(DeviceName == null || TextUtils.isEmpty(DeviceName))
                    Dialog_Pane_GoToSettingPage("Warning","The device has not been selected yet, it will automatically go to the settings page.");
                else
                {
                    Intent intent = new Intent(mContext, MainSelectConfigViewActivity.class);
                    startActivity(intent);
                }
            }
        }
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
                Intent localIntent = new Intent();
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    localIntent.setData(Uri.fromParts("package", HomeViewActivity.this.getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    localIntent.setAction(Intent.ACTION_VIEW);
                    localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    localIntent.putExtra("com.android.settings.ApplicationPkgName", HomeViewActivity.this.getPackageName());
                }
                startActivity(localIntent);
                alertDialog.dismiss();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void DialogPane_AllowPermission(String Title, String Content) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.not_connected_title));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle(Title);
        alertDialogBuilder.setMessage(Content);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }
    private void Dialog_Pane_GoToSettingPage(String title,String content)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(content);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                Intent settingsIntent = new Intent(mContext, SettingsViewActivity.class);
                startActivity(settingsIntent);
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void DialogPane_LocationPermission()
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle("Permission");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("ISC MetaScan App collect location data to enable BLE scan device normally even when the app is closed or not in use.");

        alertDialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
                boolean hasPermission1 = (ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
                if (!hasPermission || !hasPermission1) {
                    ActivityCompat.requestPermissions(HomeViewActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION
                                    , Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            REQUEST_WRITE_STORAGE);
                }
                alertDialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                if (!hasPermission) {
                    ActivityCompat.requestPermissions(HomeViewActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.INTERNET
                            },
                            REQUEST_WRITE_STORAGE);
                }
                alertDialog.dismiss();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
