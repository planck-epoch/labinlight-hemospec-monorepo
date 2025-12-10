package com.Innospectra.MetaScan;

import android.os.Environment;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommonStruct {
    public enum DeviceWavelengthType
    {
        Standard,
        Extend,
        Extend_Plus,
    }
    public static String widthnm[] ={"","","2.34","3.51","4.68","5.85","7.03","8.20","9.37","10.54","11.71","12.88","14.05","15.22","16.39","17.56","18.74"
            ,"19.91","21.08","22.25","23.42","24.59","25.76","26.93","28.10","29.27","30.44","31.62","32.79","33.96","35.13","36.30","37.47","38.64","39.81"
            ,"40.98","42.15","43.33","44.50","45.67","46.84","48.01","49.18","50.35","51.52","52.69","53.86","55.04","56.21","57.38","58.55","59.72","60.89"};
    public static String widthnm_plus[] ={"","","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16"
            ,"17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34"
            ,"35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","60","61"};
    public static String exposure_time_vlaue[] ={"0.635","1.27","2.54","5.08","15.24","30.48","60.96"};
    public  static  ISCMetaScanSDK.SupportFunction device_supportfunction;
    public  static DeviceInfo deviceInfo;
    public static class DeviceInfo
    {
        public String Manufacturer;
        public String ModelName;
        public String SerialNum;
        public String UUID;
        public String MFGNum;
        public String HWVer;
        public String TIVAVer;
        public String MainBoard;
        public String DetectBoard;

        public String Battery;
        public String TotalLampTime;
        public byte[] Devbyte;
        public byte[] Errbyte;

        public Boolean IsActivated = false;

        public byte []SpectrumCalCoefficients = new byte[144];
        public DeviceWavelengthType deviceWavelengthType = DeviceWavelengthType.Standard;
        public float minWavelength=900;
        public float maxWavelength=1700;
        public int MINWAV=900;
        public int MAXWAV=1700;
    }
    public static DeviceStatus deviceStatus;
    public static class DeviceStatus
    {
        public String Battery;
        public String LampTime;
        public String Temperature;
        public String Humidity;
        public String DevStatus;
        public String ErrorStatus;
        public byte[] DevByte;
        public byte[] ErrorByte;
    }
    public static APPStatus AppStatus = APPStatus.None;
    public enum APPStatus{
        Home,//Go to main page
        None
    }
    public static ConfigSelectType configSelectType;
    public enum ConfigSelectType{
        QuickScan,
        SelectConfig,
        SetConfig
    }
    public static ISCMetaScanSDK.ScanConfiguration CurrentScanConfig;
    public static ListScanConfig listScanConfig = new ListScanConfig();
    public static class ListScanConfig
    {
        public String[]ScanConfigName;
        public ArrayList<ISCMetaScanSDK.ScanConfiguration> Configs = new ArrayList<>();
        public int DefaultConfigIndex;
        public String DefaultConfigName;
    }
    public static UserreferenceSetting UserRefSetting = new UserreferenceSetting();
    public static class UserreferenceSetting
    {
        public String ConfigName = "";
        public String Type = "";
        public String Section = "";
        public String ConfigType = "";
        public String StartWav = "";
        public String EndWav = "";
        public String Width = "";
        public String ExposureTime = "";
        public String Resolution = "";
        public String NumRepeat = "";
        public String PGA = "";
        public String Systemp = "";
        public String Humidity = "";
        public String Intensity = "";
        public String CurrentTime = "";
    }
    //region Warm up
    public static File mSDFile  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    public static String WarmupFile = mSDFile.getParent() + "/" + mSDFile.getName() + "/WarmUpDevice.json";

    public  static CurrentDeviceStatus currentDeviceStatus = new CurrentDeviceStatus();
    public static class CurrentDeviceStatus
    {
        public String UUID;
        public Date WarmUpTime = null;
        public Boolean ForceWarmUp = true;
    }

    public static RecordDeviceWarmUpTimeList recordDeviceWarmUpTimeList = new RecordDeviceWarmUpTimeList();
    public static class RecordDeviceWarmUpTimeList
    {
        List<String>UUID = new ArrayList<>();
        List<Date>WarmUpTime = new ArrayList<>();
    }
    //endregion
    public class PreferenceKey {
        public static final String WarmUpSetting = "WarmUpSetting";
        public static final String WarmUpSettingTime = "WarmUpSettingTime";
    }
    public static List<Integer> NewReferenceIntensity =  new ArrayList<Integer>();
    public static int NewRefPGA;
    public static String NewRefConfigName = "";
}
