package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.Innospectra.MetaScan.CommonAPI.ErrorFlash;
import static com.Innospectra.MetaScan.CommonAPI.isBlankString;
import static com.Innospectra.MetaScan.CommonAPI.isNumeric;
import static com.Innospectra.MetaScan.CommonStruct.AppStatus;
import static com.Innospectra.MetaScan.CommonStruct.CurrentScanConfig;
import static com.Innospectra.MetaScan.CommonStruct.configSelectType;
import static com.Innospectra.MetaScan.CommonStruct.deviceInfo;
import static com.Innospectra.MetaScan.CommonStruct.listScanConfig;

public class QuicksetViewActivity extends Activity {
    EditText et_configname;
    EditText et_scan_repeats;
    Spinner spinner_sections;
    Button btn_section1;
    Button btn_section2;
    Button btn_section3;
    Button btn_section4;
    Button btn_section5;
    Spinner spinner_scan_type;
    Spinner spinner_scan_width;
    EditText et_scan_start;
    EditText et_scan_end;
    EditText et_scan_res;
    Spinner spinner_scan_exposuretime;
    TextView lb_section_res_validation_value;
    TextView lbtotal_res_validation_value;
    TextView lb_oversampling;
    TextView lb_oversampling_value;
    ImageButton img_tooltip;
    ArrayList<Button>Button_Section = new ArrayList<>();
    ArrayAdapter<CharSequence> adapter_scantype;
    ArrayAdapter<CharSequence> adapter_width;
    ArrayAdapter<CharSequence> adapter_exposuretime;
    Button led_connect;
    Button led_ble;
    Button led_scan;
    Button led_error;
    ProgressBar calProgress;
    TextView progressBarinsideText;

    ArrayList<String>ScanType = new ArrayList<>();
    ArrayList<String>ScanWidth = new ArrayList<>();
    ArrayList<String>ScanStart = new ArrayList<>();
    ArrayList<String>ScanEnd = new ArrayList<>();
    ArrayList<String>ScanRes = new ArrayList<>();
    ArrayList<String>ScanExTime = new ArrayList<>();

    int SectionIndex = 0;
    int MaxPattern = 228;
    int TotalMaxPatternTol = 0;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.quickset_slewconfig_view);
        mContext = this;
        setActivityTouchDisable(true);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        InitComponent();
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
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        else if(id == R.id.action_righttop_button) {
            setActivityTouchDisable(true);
            if(!CheckConfigName())
                setActivityTouchDisable(false);
            else if(!CheckScanRepeat())
                setActivityTouchDisable(false);
            else
            {
                calProgress.setVisibility(View.VISIBLE);
                progressBarinsideText.setVisibility(View.VISIBLE);
                configSelectType = CommonStruct.ConfigSelectType.SetConfig;
                SetScanConfig();
                byte[] EXTRA_DATA = CommonAPI.ChangeScanConfigToByte(CurrentScanConfig);
                ISCMetaScanSDK.ScanConfig(EXTRA_DATA,ISCMetaScanSDK.ScanConfig.SET);
            }
        }
        return super.onOptionsItemSelected(item);
    }
    //region quickset scan config
    private void  SetScanConfig()
    {
        byte[] ConfigNamebytes = et_configname.getText().toString().getBytes();
        byte[] SerialNumberbytes = deviceInfo.SerialNum.getBytes();
        byte[] SectionScanType = new byte[5];
        int[] SectionWavStart = new int[5];
        int[] SectionWavEnd = new int[5];
        int[] SectionNumPatterns = new int[5];
        int[] SectionNumRepeats = new int[5];
        byte[] SectionWidthPx = new byte[5];
        int[] SectionExTime = new int[5];
        int section = Integer.parseInt(spinner_sections.getSelectedItem().toString());
        CurrentScanConfig = new ISCMetaScanSDK.ScanConfiguration();
        CurrentScanConfig.setScanType(2);
        CurrentScanConfig.setScanConfigIndex(255);
        CurrentScanConfig.setScanConfigSerialNumber(SerialNumberbytes);
        CurrentScanConfig.setConfigName(ConfigNamebytes);
        CurrentScanConfig.setSlewNumSections((byte)section);
        CurrentScanConfig.setNumRepeats(Integer.parseInt(et_scan_repeats.getText().toString()));
        for(int i=0;i<section;i++)
        {
            int ScanTypeIndex = adapter_scantype.getPosition(ScanType.get(i));
            SectionScanType[i] = (byte) ScanTypeIndex;
            SectionWavStart[i] = Integer.parseInt(ScanStart.get(i));
            SectionWavEnd[i] = Integer.parseInt(ScanEnd.get(i));
            SectionNumPatterns[i] = Integer.parseInt(ScanRes.get(i));
            SectionNumRepeats[i] = Integer.parseInt(et_scan_repeats.getText().toString());
            SectionExTime[i] = adapter_exposuretime.getPosition(ScanExTime.get(i));
            SectionWidthPx[i] = (byte) (adapter_width.getPosition(ScanWidth.get(i)) + 2);
        }
        CurrentScanConfig.setSectionScanType(SectionScanType);
        CurrentScanConfig.setSectionWavelengthStartNm(SectionWavStart);
        CurrentScanConfig.setSectionWavelengthEndNm(SectionWavEnd);
        CurrentScanConfig.setSectionNumPatterns(SectionNumPatterns);
        CurrentScanConfig.setSectionExposureTime(SectionExTime);
        CurrentScanConfig.setSectionWidthPx(SectionWidthPx);
        CurrentScanConfig.setSectionNumRepeats(SectionNumRepeats);
    }
    //endregion
    //region GUI and Event
    private void InitComponent()
    {
        et_configname = (EditText)findViewById(R.id.et_configname);
        et_scan_repeats = (EditText)findViewById(R.id.et_scan_repeats);
        spinner_sections = (Spinner) findViewById(R.id.spinner_sections);
        btn_section1 = (Button) findViewById(R.id.btn_section1);
        btn_section2 = (Button) findViewById(R.id.btn_section2);
        btn_section3 = (Button) findViewById(R.id.btn_section3);
        btn_section4 = (Button) findViewById(R.id.btn_section4);
        btn_section5 = (Button) findViewById(R.id.btn_section5);
        spinner_scan_type = (Spinner) findViewById(R.id.spinner_scan_type);
        spinner_scan_width = (Spinner) findViewById(R.id.spinner_scan_width);
        et_scan_start = (EditText)findViewById(R.id.et_scan_start);
        et_scan_end = (EditText)findViewById(R.id.et_scan_end);
        et_scan_res = (EditText)findViewById(R.id.et_scan_res);
        spinner_scan_exposuretime = (Spinner) findViewById(R.id.spinner_scan_exposuretime);
        lb_section_res_validation_value = (TextView)findViewById(R.id.lb_section_res_validation_value);
        lbtotal_res_validation_value = (TextView)findViewById(R.id.lbtotal_res_validation_value);
        lb_oversampling = (TextView)findViewById(R.id.lb_oversampling);
        lb_oversampling_value = (TextView)findViewById(R.id.lb_oversampling_value);
        img_tooltip = (ImageButton)findViewById(R.id.img_tooltip);
        led_connect = (Button)findViewById(R.id.led_connect);
        led_ble = (Button)findViewById(R.id.led_ble);
        led_scan = (Button)findViewById(R.id.led_scan);
        led_error = (Button)findViewById(R.id.led_error);
        calProgress = (ProgressBar)findViewById(R.id.calProgress);
        progressBarinsideText = (TextView)findViewById(R.id.progressBarinsideText);

        AnimationDrawable ani = (AnimationDrawable)getResources().getDrawable(R.drawable.led_flashing);
        led_ble.setCompoundDrawablesWithIntrinsicBounds( ani, null, null, null);
        ani.start();
        led_connect.setOnClickListener(Button_Listener);
        led_ble.setOnClickListener(Button_Listener);
        led_scan.setOnClickListener(Button_Listener);
        led_error.setOnClickListener(Button_Listener);
        img_tooltip.setOnClickListener(ImageButton_Listenser);

        ArrayAdapter<CharSequence> adapter_section = ArrayAdapter.createFromResource(this,
                R.array.section_array, android.R.layout.simple_spinner_item);
        adapter_section.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_sections.setAdapter(adapter_section);

        adapter_scantype = ArrayAdapter.createFromResource(this,
                R.array.scan_method_array, android.R.layout.simple_spinner_item);
        adapter_scantype.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_scan_type.setAdapter(adapter_scantype);

        if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
            adapter_width = ArrayAdapter.createFromResource(this,
                    R.array.scan_width_plus, android.R.layout.simple_spinner_item);
        else
            adapter_width = ArrayAdapter.createFromResource(this,
                R.array.scan_width, android.R.layout.simple_spinner_item);
        adapter_width.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_scan_width.setAdapter(adapter_width);
        spinner_scan_width.setSelection(4);

        adapter_exposuretime = ArrayAdapter.createFromResource(this,
                R.array.exposure_time, android.R.layout.simple_spinner_item);
        adapter_exposuretime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_scan_exposuretime.setAdapter(adapter_exposuretime);

        spinner_sections.setOnItemSelectedListener(Spinner_Section_ItemSelect);
        spinner_scan_type.setOnItemSelectedListener(Spinner_ScanType_ItemSelect);
        spinner_scan_width.setOnItemSelectedListener(Spinner_Width_ItemSelect);
        spinner_scan_exposuretime.setOnItemSelectedListener(Spinner_ExTime_ItemSelect);
        et_scan_start.setOnEditorActionListener(EditText_ScanStart_Listener);
        et_scan_start.setOnFocusChangeListener(EditText_ScanStart_FocusListener);
        et_scan_end.setOnEditorActionListener(EditText_ScanEnd_Listener);
        et_scan_end.setOnFocusChangeListener(EditText_ScanEnd_FocusListener);
        et_scan_res.setOnEditorActionListener(EditText_ScanRes_Listener);
        et_scan_res.setOnFocusChangeListener(EditText_ScanRes_FocusListener);
        et_configname.setOnEditorActionListener(EditText_ConfigName_Listener);
        et_scan_repeats.setOnEditorActionListener(EditText_ScanRepeats_Listener);

        Button_Section.clear();
        Button_Section.add(btn_section1);
        Button_Section.add(btn_section2);
        Button_Section.add(btn_section3);
        Button_Section.add(btn_section4);
        Button_Section.add(btn_section5);
        SetSectionColor(0);
        OpenSectionButton(0);

        for(int i=0;i<5;i++)
            Button_Section.get(i).setOnClickListener(Button_Listener);

        ScanType.clear();
        ScanWidth.clear();
        ScanStart.clear();
        ScanEnd.clear();
        ScanRes.clear();
        ScanExTime.clear();
        et_scan_start.setText(Integer.toString(deviceInfo.MINWAV));
        et_scan_end.setText(Integer.toString(deviceInfo.MAXWAV));
        for(int i=0;i<5;i++)
        {
            ScanType.add(spinner_scan_type.getSelectedItem().toString());
            ScanWidth.add(spinner_scan_width.getSelectedItem().toString());
            ScanStart.add(et_scan_start.getText().toString());
            ScanEnd.add(et_scan_end.getText().toString());
            if(i == 0)
                ScanRes.add(et_scan_res.getText().toString());
            else
                ScanRes.add("3");
            ScanExTime.add(spinner_scan_exposuretime.getSelectedItem().toString());
        }
    }
    private ImageButton.OnClickListener ImageButton_Listenser = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            Dialog_Pane_OverSampling("",ToolTip_Msg);
        }
    };
    private Button.OnClickListener Button_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.btn_section1:
                    SetSectionColor(0);
                    GetSectionConfig(0);
                    GetMaxPattern(0);
                    GetTotalResolution();
                    CalOverSampling(0);
                    break;
                case R.id.btn_section2:
                    SetSectionColor(1);
                    GetSectionConfig(1);
                    GetMaxPattern(1);
                    GetTotalResolution();
                    CalOverSampling(1);
                    break;
                case R.id.btn_section3:
                    SetSectionColor(2);
                    GetSectionConfig(2);
                    GetMaxPattern(2);
                    GetTotalResolution();
                    CalOverSampling(2);
                    break;
                case R.id.btn_section4:
                    SetSectionColor(3);
                    GetSectionConfig(3);
                    GetMaxPattern(3);
                    GetTotalResolution();
                    CalOverSampling(3);
                    break;
                case R.id.btn_section5:
                    SetSectionColor(4);
                    GetSectionConfig(4);
                    GetMaxPattern(4);
                    GetTotalResolution();
                    CalOverSampling(4);
                    break;
                case R.id.led_connect:
                case R.id.led_ble:
                case R.id.led_scan:
                case R.id.led_error:
                    Intent intent = new Intent(mContext, DeviceStatusViewActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
    private Spinner.OnItemSelectedListener Spinner_Section_ItemSelect = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            OpenSectionButton(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    private Spinner.OnItemSelectedListener Spinner_ScanType_ItemSelect = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            /*String BeforeScanType = ScanType.get(SectionIndex);
            String BeforeSRes = lb_section_res_validation_value.getText().toString();
            String BeforeTotalRes = lbtotal_res_validation_value.getText().toString();*/
            ScanType.set(SectionIndex,spinner_scan_type.getSelectedItem().toString());
            GetMaxPattern(SectionIndex);
            GetTotalResolution();
            CalOverSampling(SectionIndex);

            /*if(Integer.parseInt(ScanRes.get(SectionIndex)) > MaxPattern)
            {
                Dialog_Pane("Error","Resolution range is 3~" + MaxPattern + " pts!",false);
                ScanType.set(SectionIndex,BeforeScanType);
                spinner_scan_type.setSelection(adapter_scantype.getPosition(BeforeScanType));
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
                return;
            }
            int res = GetUsedNumOfRes(SectionIndex);
            if(res > TotalMaxPatternTol)
            {
                Dialog_Pane("Error","Total scan pattern num is over " + TotalMaxPatternTol + "!",false);
                ScanType.set(SectionIndex,BeforeScanType);
                spinner_scan_type.setSelection(adapter_scantype.getPosition(BeforeScanType));
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    private Spinner.OnItemSelectedListener Spinner_Width_ItemSelect = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String BeforeWidth = ScanWidth.get(SectionIndex);
            String BeforeSRes = lb_section_res_validation_value.getText().toString();
            String BeforeTotalRes = lbtotal_res_validation_value.getText().toString();
            ScanWidth.set(SectionIndex,spinner_scan_width.getSelectedItem().toString());
            GetMaxPattern(SectionIndex);
            GetTotalResolution();
            CalOverSampling(SectionIndex);
           /* if(Integer.parseInt(ScanRes.get(SectionIndex)) > MaxPattern)
            {
                Dialog_Pane("Error","Resolution range is 3~" + MaxPattern + " pts!",false);
                ScanWidth.set(SectionIndex,BeforeWidth);
                spinner_scan_width.setSelection(adapter_width.getPosition(BeforeWidth));
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
                return;
            }
            int res = GetUsedNumOfRes(SectionIndex);
            if(res > TotalMaxPatternTol)
            {
                Dialog_Pane("Error","Total scan pattern num is over " + TotalMaxPatternTol + "!",false);
                ScanWidth.set(SectionIndex,BeforeWidth);
                spinner_scan_width.setSelection(adapter_width.getPosition(BeforeWidth));
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    private Spinner.OnItemSelectedListener Spinner_ExTime_ItemSelect = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            ScanExTime.set(SectionIndex,spinner_scan_exposuretime.getSelectedItem().toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    private View.OnFocusChangeListener EditText_ScanStart_FocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (!b) {
                if(!et_scan_start.getText().toString().equals(ScanStart.get(SectionIndex)))
                    et_scan_start.setText(ScanStart.get(SectionIndex));
            }
        }
    };
    private EditText.OnEditorActionListener EditText_ScanStart_Listener = new EditText.OnEditorActionListener()
    {

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if(et_scan_start.getText().toString().matches("")|| Integer.parseInt(et_scan_start.getText().toString())> Integer.parseInt(et_scan_end.getText().toString()) || Integer.parseInt(et_scan_start.getText().toString())<deviceInfo.minWavelength)
                {
                    et_scan_start.setText(ScanStart.get(SectionIndex));
                    Dialog_Pane("Error","Start wavelength should be between " + deviceInfo.MINWAV + " nm and end wavelength!",false);
                    return false; // consume.
                }
            }
            /*String BeforeScanStart = ScanStart.get(SectionIndex);
            String BeforeSRes = lb_section_res_validation_value.getText().toString();
            String BeforeTotalRes = lbtotal_res_validation_value.getText().toString();
            ScanStart.set(SectionIndex,et_scan_start.getText().toString());
            GetMaxPattern(SectionIndex);
            GetTotalResolution();
            CalOverSampling(SectionIndex);
            if(Integer.parseInt(ScanRes.get(SectionIndex)) > MaxPattern)
            {
                Dialog_Pane("Error","Resolution range is 3~" + MaxPattern + " pts!",false);
                ScanStart.set(SectionIndex,BeforeScanStart);
                et_scan_start.setText(BeforeScanStart);
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
                GetMaxPattern(SectionIndex);
                GetTotalResolution();
                CalOverSampling(SectionIndex);
                return false;
            }
            int res = GetUsedNumOfRes(SectionIndex);
            if(res > TotalMaxPatternTol)
            {
                Dialog_Pane("Error","Total scan pattern num is over " + TotalMaxPatternTol + "!",false);
                ScanStart.set(SectionIndex,BeforeScanStart);
                et_scan_start.setText(BeforeScanStart);
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
                GetMaxPattern(SectionIndex);
                GetTotalResolution();
                CalOverSampling(SectionIndex);
            }*/
            ScanStart.set(SectionIndex,et_scan_start.getText().toString());
            GetMaxPattern(SectionIndex);
            GetTotalResolution();
            CalOverSampling(SectionIndex);
            return false;
        }
    };
    private View.OnFocusChangeListener EditText_ScanEnd_FocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (!b) {
                if(!et_scan_end.getText().toString().equals(ScanEnd.get(SectionIndex)))
                    et_scan_end.setText(ScanEnd.get(SectionIndex));
            }
        }
    };
    private EditText.OnEditorActionListener EditText_ScanEnd_Listener = new EditText.OnEditorActionListener()
    {

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if(et_scan_end.getText().toString().matches("")|| Integer.parseInt(et_scan_end.getText().toString())< Integer.parseInt(et_scan_start.getText().toString()) || Integer.parseInt(et_scan_end.getText().toString())>deviceInfo.maxWavelength)
                {
                    et_scan_end.setText(ScanEnd.get(SectionIndex));
                    Dialog_Pane("Error","End wavelength should be between start wavelength and " + deviceInfo.MAXWAV + " nm!",false);
                    return false; // consume.
                }
            }
            /*String BeforeScanEnd = ScanEnd.get(SectionIndex);
            String BeforeSRes = lb_section_res_validation_value.getText().toString();
            String BeforeTotalRes = lbtotal_res_validation_value.getText().toString();
            ScanEnd.set(SectionIndex,et_scan_end.getText().toString());
            GetMaxPattern(SectionIndex);
            GetTotalResolution();
            CalOverSampling(SectionIndex);

            if(Integer.parseInt(ScanRes.get(SectionIndex)) > MaxPattern)
            {
                Dialog_Pane("Error","Resolution range is 3~" + MaxPattern + " pts!",false);
                ScanEnd.set(SectionIndex,BeforeScanEnd);
                et_scan_end.setText(BeforeScanEnd);
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
                GetMaxPattern(SectionIndex);
                GetTotalResolution();
                CalOverSampling(SectionIndex);
                return false;
            }
            int res = GetUsedNumOfRes(SectionIndex);
            if(res > TotalMaxPatternTol)
            {
                Dialog_Pane("Error","Total scan pattern num is over " + TotalMaxPatternTol + "!",false);
                ScanEnd.set(SectionIndex,BeforeScanEnd);
                et_scan_end.setText(BeforeScanEnd);
                lb_section_res_validation_value.setText(BeforeSRes);
                lbtotal_res_validation_value.setText(BeforeTotalRes);
                GetMaxPattern(SectionIndex);
                GetTotalResolution();
                CalOverSampling(SectionIndex);
            }*/
            ScanEnd.set(SectionIndex,et_scan_end.getText().toString());
            GetMaxPattern(SectionIndex);
            GetTotalResolution();
            CalOverSampling(SectionIndex);
            return false;
        }
    };
    private EditText.OnEditorActionListener EditText_ConfigName_Listener = new EditText.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if(!CheckConfigName())
                    et_configname.setBackground(mContext.getDrawable(R.drawable.edittext_style_error));
                else
                    et_configname.setBackground(mContext.getDrawable(R.drawable.edittext_style));
            }
            return false;
        }
    };
    private EditText.OnEditorActionListener EditText_ScanRepeats_Listener = new EditText.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if(isBlankString(et_scan_repeats.getText().toString()) || !isNumeric(et_scan_repeats.getText().toString()))
                {
                    et_scan_repeats.setBackground(mContext.getDrawable(R.drawable.edittext_style_error));
                    return false;
                }
                if(Integer.parseInt(et_scan_repeats.getText().toString())<=0)
                    et_scan_repeats.setBackground(mContext.getDrawable(R.drawable.edittext_style_error));
                else
                    et_scan_repeats.setBackground(mContext.getDrawable(R.drawable.edittext_style));
            }
            return false;
        }
    };
    private View.OnFocusChangeListener EditText_ScanRes_FocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (!b) {
                if(!et_scan_res.getText().toString().equals(ScanRes.get(SectionIndex)))
                    et_scan_res.setText(ScanRes.get(SectionIndex));
            }
        }
    };
    private EditText.OnEditorActionListener EditText_ScanRes_Listener = new EditText.OnEditorActionListener()
    {

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if(et_scan_res.getText().toString().matches("")|| Integer.parseInt(et_scan_res.getText().toString())< 3)
                {
                    et_scan_res.setText(ScanRes.get(SectionIndex));
                    Dialog_Pane("Error","Resolution should > 3!",false);
                    return false; // consume.

                }
                /*int res = GetUsedNumOfRes(SectionIndex);
                if(res > TotalMaxPatternTol)
                {
                    et_scan_res.setText(ScanRes.get(SectionIndex));
                    Dialog_Pane("Error","Total scan pattern num is over " + TotalMaxPatternTol + "!",false);
                    return false; // consume.
                }*/
            }
            ScanRes.set(SectionIndex,et_scan_res.getText().toString());
            GetMaxPattern(SectionIndex);
            GetTotalResolution();
            CalOverSampling(SectionIndex);
            return false;
        }
    };
    private void OpenSectionButton(int index)
    {
        for(int i=0;i<=index;i++)
            Button_Section.get(i).setVisibility(View.VISIBLE);
        for(int i=index+1;i<5;i++)
            Button_Section.get(i).setVisibility(View.INVISIBLE);
        SetSectionColor(0);
        GetSectionConfig(0);
        GetMaxPattern(0);
        GetTotalResolution();
        CalOverSampling(0);
    }
    private void SetSectionColor(int index)
    {
        Button_Section.get(index).setBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
        SectionIndex = index;
        for(int i=0;i<5;i++)
        {
            if(i!=index)
                Button_Section.get(i).setBackgroundColor(0xFF0099CC);
        }
    }
    private void GetSectionConfig(int index)
    {
        if(ScanType.size() > 0)
        {
            spinner_scan_type.setSelection(adapter_scantype.getPosition(ScanType.get(index)));
            spinner_scan_width.setSelection(adapter_width.getPosition(ScanWidth.get(index)));
            et_scan_start.setText(ScanStart.get(index));
            et_scan_end.setText(ScanEnd.get(index));
            et_scan_res.setText(ScanRes.get(index));
            spinner_scan_exposuretime.setSelection(adapter_exposuretime.getPosition(ScanExTime.get(index)));
        }
    }
    private int GetUsedNumOfRes(int index)
    {
        int totalsection = Integer.parseInt(spinner_sections.getSelectedItem().toString());
        int res = 0;
        for(int i=0;i<totalsection;i++)
        {
            if(i!=index)
                res += Integer.parseInt(ScanRes.get(i));
            else
                res += Integer.parseInt(et_scan_res.getText().toString());
        }
        return res;
    }
    /**
     *Get max pattern that user can set
     */
    private void GetMaxPattern(int index)
    {
        if(ScanStart.size() > 0)
        {
            if(!et_scan_start.getText().toString().equals(ScanStart.get(index)))
                et_scan_start.setText(ScanStart.get(index));
            if(!et_scan_end.getText().toString().equals(ScanEnd.get(index)))
                et_scan_end.setText(ScanEnd.get(index));
            if(!et_scan_res.getText().toString().equals(ScanRes.get(index)))
                et_scan_res.setText(ScanRes.get(index));

            int start_nm = Integer.parseInt(ScanStart.get(index));
            int end_nm =  Integer.parseInt(ScanEnd.get(index));
            int width_index = adapter_width.getPosition(ScanWidth.get(index))+2;
            int num_repeat = Integer.parseInt(et_scan_repeats.getText().toString());
            int scan_type = adapter_scantype.getPosition(ScanType.get(index));
            int isEXTVer = 0;
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                isEXTVer = 2;
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend) == 0)
                isEXTVer = 1;
            MaxPattern = ISCMetaScanSDK.GetMaxPatternJNI(scan_type,start_nm,end_nm,width_index,num_repeat, deviceInfo.SpectrumCalCoefficients,isEXTVer);
            lb_section_res_validation_value.setText(ScanRes.get(index) + "/" + MaxPattern);
        }
    }
    private int GetHadPattern(int index,int numpattern)
    {
        int HadPattern = -1;
        if(ScanStart.size() > 0)
        {
            int start_nm = Integer.parseInt(ScanStart.get(index));
            int end_nm =  Integer.parseInt(ScanEnd.get(index));
            int width_index = adapter_width.getPosition(ScanWidth.get(index))+2;
            int num_repeat = Integer.parseInt(et_scan_repeats.getText().toString());
            int scan_type = adapter_scantype.getPosition(ScanType.get(index));
            int isEXTVer = 0;
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                isEXTVer = 2;
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend) == 0)
                isEXTVer = 1;
            HadPattern = ISCMetaScanSDK.GetHadamardUsedPatternsJNI(start_nm,end_nm,width_index,num_repeat,numpattern,deviceInfo.SpectrumCalCoefficients,isEXTVer);
        }
        return HadPattern;
    }
    private void GetTotalResolution()
    {
        if(ScanStart.size() > 0)
        {
            int totalresolution = 0;
            int maxtolerence_resolution = 0;
            int section = Integer.parseInt(spinner_sections.getSelectedItem().toString());
            for(int i=0;i<section;i++)
            {
                totalresolution += Integer.parseInt(ScanRes.get(i).toString());

                int start_nm = Integer.parseInt(ScanStart.get(i));
                int end_nm =  Integer.parseInt(ScanEnd.get(i));
                int width_index = adapter_width.getPosition(ScanWidth.get(i))+2;
                int num_repeat = Integer.parseInt(et_scan_repeats.getText().toString());
                int scan_type = adapter_scantype.getPosition(ScanType.get(i));
                int isEXTVer = 0;
                if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend) == 0)
                    isEXTVer = 1;
                else if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                    isEXTVer = 2;
                maxtolerence_resolution += ISCMetaScanSDK.GetMaxPatternJNI(scan_type,start_nm,end_nm,width_index,num_repeat, deviceInfo.SpectrumCalCoefficients,isEXTVer);
            }
            if(maxtolerence_resolution < 624)
            {
                TotalMaxPatternTol = maxtolerence_resolution;
                lbtotal_res_validation_value.setText(totalresolution+"/" + maxtolerence_resolution);
            }
            else
            {
                TotalMaxPatternTol = 624;
                lbtotal_res_validation_value.setText(totalresolution+"/624");
            }
        }
    }
    String ToolTip_Msg = "";
    private void CalOverSampling(int i)
    {
        int patWidth = 0;
        double baseOverSampleRate = 0.0;
        double overSampleRate = 0.0;
        int inputDigitalResolution = 3;
        if(ScanRes.size() > 0)
            inputDigitalResolution = Integer.parseInt(ScanRes.get(i));
        DecimalFormat df = new DecimalFormat("#.#");
        DecimalFormat df2 = new DecimalFormat("#");
        String Msg = "";

        int MaxResolution = 0;
        int HadPattern = Integer.MAX_VALUE;
        if(ScanStart.size() > 0)
        {
            GetMaxPattern(i);
            MaxResolution = MaxPattern;
            if(ScanType.get(i).contains("Had") && MaxResolution > 0)
            {
                while (HadPattern > 624)
                {
                    HadPattern = GetHadPattern(i,MaxResolution);
                    MaxResolution--;
                }
                MaxResolution++;
            }
        }

        if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
        {
            patWidth = (int)Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
            baseOverSampleRate = (double)(Math.ceil((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
            overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
        }
        else
        {
            patWidth = (int)Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
            baseOverSampleRate = (double)(Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
            overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
        }
        if(ScanType.size() > 0 && ScanType.get(i).contains("Had") && MaxResolution > 0 && inputDigitalResolution > MaxResolution)
        {
            ScanRes.set(SectionIndex,Integer.toString(MaxResolution));
            et_scan_res.setText(Integer.toString(MaxResolution));
            Msg = "Resolution exceeds maximum value!";
            inputDigitalResolution = MaxResolution;

            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
            {
                patWidth = (int)Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
                baseOverSampleRate = (double)(Math.ceil((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
                overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
            }
            else
            {
                patWidth = (int)Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
                baseOverSampleRate = (double)(Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
                overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
            }
        }
        else if(inputDigitalResolution > MaxPattern)
        {
            ScanRes.set(SectionIndex,Integer.toString(MaxPattern));
            et_scan_res.setText(Integer.toString(MaxPattern));
            Msg = "Resolution exceeds maximum value!";
            inputDigitalResolution = MaxPattern;

            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
            {
                patWidth = (int)Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
                baseOverSampleRate = (double)(Math.ceil((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
                overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
            }
            else
            {
                patWidth = (int)Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
                baseOverSampleRate = (double)(Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
                overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
            }
        }
        int res = GetUsedNumOfRes(SectionIndex);
        if(res > TotalMaxPatternTol && ScanRes.size() > 0)
        {
            int shift  = Integer.parseInt(et_scan_res.getText().toString()) - (res - TotalMaxPatternTol);
            ScanRes.set(SectionIndex,Integer.toString(shift));
            et_scan_res.setText(Integer.toString(shift));
            Msg = "Total resolution exceeds maximum value! \n Left pattern number is " +shift +".";
            inputDigitalResolution = shift;

            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
            {
                patWidth = (int)Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
                baseOverSampleRate = (double)(Math.ceil((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
                overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.ceil(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
            }
            else
            {
                patWidth = (int)Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString()));
                baseOverSampleRate = (double)(Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / patWidth));
                overSampleRate = (double)inputDigitalResolution / Math.floor((double)(Integer.parseInt(et_scan_end.getText().toString()) - Integer.parseInt(et_scan_start.getText().toString())) / Math.floor(Float.valueOf(spinner_scan_width.getSelectedItem().toString())));
            }
        }
        if ((inputDigitalResolution > MaxPattern) || (inputDigitalResolution > Math.floor(baseOverSampleRate * 4.5)))
        {
            Msg = "Exceed the max oversampling rate! (max = 4.5, set = " + df.format(overSampleRate) + "x";
            et_scan_res.setText(Integer.toString((int)Math.floor(baseOverSampleRate * 4.5)));
            ScanRes.set(SectionIndex,et_scan_res.getText().toString());
            GetMaxPattern(i);
            GetTotalResolution();
            CalOverSampling(i);
        }
        else
        {
            overSampleRate = Double.parseDouble(df.format(overSampleRate));
            lb_oversampling_value.setText(Double.toString(overSampleRate));
            if ((overSampleRate > 3 || overSampleRate < 2) && patWidth > 7)
            {
                int upperLimit = (int)baseOverSampleRate * 3;
                upperLimit = upperLimit > MaxPattern ? MaxPattern : upperLimit;
                lb_oversampling.setTextColor(Color.RED);
                lb_oversampling_value.setTextColor(Color.RED);
                img_tooltip.setVisibility(View.VISIBLE);
                ToolTip_Msg = "The recommended oversampling is between 2.0 ~ 3.0 for this pattern width setting\ni.e. Digital resolution should be between " + df2.format(baseOverSampleRate * 2) + " ~ " + df2.format(upperLimit);
            }
            else if (overSampleRate < 2 && patWidth > 4 && patWidth < 8)
            {
                lb_oversampling.setTextColor(Color.RED);
                lb_oversampling_value.setTextColor(Color.RED);
                img_tooltip.setVisibility(View.VISIBLE);
                ToolTip_Msg = "The recommended oversampling is above 2.0 for this pattern width setting\ni.e. Digital resolution should be between " + df2.format(baseOverSampleRate * 2) + " ~ " + df2.format(MaxPattern);
            }
            else
            {
                lb_oversampling.setTextColor(Color.BLUE);
                lb_oversampling_value.setTextColor(Color.BLUE);
                img_tooltip.setVisibility(View.INVISIBLE);
            }
        }
        if(!Msg.isEmpty())
            Dialog_Pane("Warning",Msg,true);
    }
    private Boolean CheckConfigName()
    {
        String ConfigName = et_configname.getText().toString();
        if(isBlankString(et_configname.getText().toString()))
        {
            Dialog_Pane("Error","The config name can not be empty.",false);
            et_configname.setBackground(mContext.getDrawable(R.drawable.edittext_style_error));
            return false;
        }
        else if(et_configname.getText().toString().length()>40)
        {
            Dialog_Pane("Error","The length of config name should less or equal to 40.",false);
            et_configname.setBackground(mContext.getDrawable(R.drawable.edittext_style_error));
            return false;
        }
        for(int i=0;i< listScanConfig.ScanConfigName.length;i++)
        {
            if(listScanConfig.ScanConfigName[i].equals((ConfigName)))
            {
                Dialog_Pane("Error","Duplicate config name in the device.",false);
                return false;
            }
        }
        return true;
    }
    private Boolean CheckScanRepeat()
    {
        if(isBlankString(et_scan_repeats.getText().toString()))
        {
            Dialog_Pane("Error","The scan repeats can not be empty.",false);
            et_scan_repeats.setBackground(mContext.getDrawable(R.drawable.edittext_style_error));
            return false;
        }
        return true;
    }
    private void Dialog_Pane(String title, String content, final Boolean NotShowCalProgress)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(content);

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                if(NotShowCalProgress)
                {
                    setActivityTouchDisable(false);
                    calProgress.setVisibility(View.GONE);
                    progressBarinsideText.setVisibility(View.GONE);
                }
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void Dialog_Pane_OverSampling(String title, String content)
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
    //endregion
    public class WriteScanConfigStatusReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            byte status[] = intent.getByteArrayExtra(ISCMetaScanSDK.RETURN_WRITE_SCAN_CONFIG_STATUS);
            if((int)status[0] == 1)
            {
                if((int)status[2] == -1 && (int)status[3]==-1)
                    Dialog_Pane("Fail","Set configuration fail!",true);
                else
                    ISCMetaScanSDK.ReadCurrentScanConfig();
            }
            else if((int)status[0] == -1)
                Dialog_Pane("Fail","Set configuration fail!",true);
            else if((int)status[0] == -2)
                Dialog_Pane("Fail","Set configuration fail! Hardware not compatible!",true);
            else if((int)status[0] == -3)
                Dialog_Pane("Fail","Set configuration fail! Function is currently locked!",true);
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
                Dialog_Pane("Fail","Set configuration fail.",true);
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
            setActivityTouchDisable(false);
        }
    }
    /**
     * Broadcast Receiver handling the disconnect event. If the Nano disconnects,
     * this activity should finish so that the user is taken back to the {@link HomeViewActivity}
     */
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
                    if (reason.equals(SYSTEM_HOME_KEY)||reason.equals(SYSTEM_RECENT_APPS)) {
                        Intent NotifyBackground = new Intent(MainSelectConfigViewActivity.NOTIFY_BACKGROUND);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(NotifyBackground);
                        finish();
                    }
                }
            }
        }
    }
}
