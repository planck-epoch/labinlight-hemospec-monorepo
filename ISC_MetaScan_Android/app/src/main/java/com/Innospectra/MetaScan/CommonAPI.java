package com.Innospectra.MetaScan;

import static com.Innospectra.MetaScan.CommonStruct.WarmupFile;
import static com.Innospectra.MetaScan.CommonStruct.currentDeviceStatus;
import static com.Innospectra.MetaScan.CommonStruct.deviceInfo;
import static com.Innospectra.MetaScan.CommonStruct.recordDeviceWarmUpTimeList;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;

import com.ISCMetaScanSDK.ISCMetaScanSDK;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonAPI {
    public static String HaveValidScanConfig(ArrayList<ISCMetaScanSDK.SlewScanSection> sections)
    {
        String Errmsg = "";
        for(int i=0;i<sections.size();i++)
        {
            try {
                int widthindex = (int)(sections.get(i).getWidthPx());
                if(widthindex < 2)
                    Errmsg += "Not valid width for section" + (i+1) + "(" + widthindex + ")." + "\n";
                if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                {
                    if(widthindex > 61)
                        Errmsg += "Not valid width for section" + (i+1) + "(" + widthindex + ")." + "\n";
                }
                else
                {
                    if(widthindex > 52)
                        Errmsg += "Not valid width for section" + (i+1) + "(" + widthindex + ")." + "\n";
                }
            }catch (Exception e)
            {
                Errmsg += "Not valid width for section" + (i+1) + "." + "\n";
            }
            try {
                int exindex = sections.get(i).getExposureTime();
                if(exindex < 0 || exindex > 6)
                    Errmsg += "Not valid exposure time for section" + (i+1) + "(" + exindex + ")." + "\n";
            }catch (Exception e)
            {
                Errmsg += "Not valid exposure time for section" + (i+1) + "." +"\n";
            }
        }
        return Errmsg;
    }
    public static Boolean isBlankString(String string)
    { return string == null || string.trim().isEmpty(); }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    public static String filterDate(String Str) {
        String filter = "[^0-9^A-Z^a-z]"; // Specify the characters to be filtered
        Pattern p = Pattern.compile(filter);
        Matcher m = p.matcher(Str);
        return m.replaceAll("").trim(); // Replace all characters other than those set above
    }
    public static byte[] hexToBytes(String hexString) {

        char[] hex = hexString.toCharArray();
        //change to raw data, the length should divided by 2
        int length = hex.length / 2;
        byte[] rawData = new byte[length];
        for (int i = 0; i < length; i++) {
            //Convert hex data to decimal value
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            //Shift the binary value of the first value by 4 bits to the left,ex: 00001000 => 10000000 (8=>128)
            //Then concatenate with the binary value of the second value ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            //Complementary with FFFFFFFF
            if (value > 127)
                value -= 256;
            //Finally change to byte
            rawData [i] = (byte) value;
        }
        return rawData ;
    }
    //region Warmup API
    /**
     * Read the list of warmup time of device from file
     */
    public static void ReadWarmUpFile()
    {
        try {
            File mFile = new File(WarmupFile);
            if(!mFile.exists())
            {
                mFile.createNewFile();
                WriteWarmUpData();
            }
            else
            {
                ReadWarmUpData();
                SearchDeviceWarmUpTime();
            }
        }catch (Exception e)
        {

        }
    }
    public static void ReadWarmUpData()
    {
        try {
            BufferedReader ModelReader = null;
            Gson gson = new Gson();
            Type listType = new TypeToken<CommonStruct.RecordDeviceWarmUpTimeList>(){}.getType();
            ModelReader = new BufferedReader(new FileReader(WarmupFile));
            if(ModelReader != null)
            {
                String line;
                while ((line = ModelReader.readLine()) != null)
                    recordDeviceWarmUpTimeList = gson.fromJson(line, listType);
                ModelReader.close();
            }
        }catch (Exception e)
        {

        }
    }
    /**
     * Write the list of warmup time of device to the file
     */
    public static void WriteWarmUpData()
    {
        try {
                Writer writer = new FileWriter(WarmupFile);
                Gson gson = new GsonBuilder().create();
                gson.toJson(recordDeviceWarmUpTimeList, writer);
                writer.close();
        }catch (Exception e)
        {

        }
    }
    /**
     * Search the current connected device mapping the list of wamp up time
     */
    public static void SearchDeviceWarmUpTime()
    {
        int index = recordDeviceWarmUpTimeList.UUID.indexOf(currentDeviceStatus.UUID);
        if(index >=0)
            currentDeviceStatus.WarmUpTime = recordDeviceWarmUpTimeList.WarmUpTime.get(index);
    }
    /**
     * Check need to do  warm up
     */
    public static Boolean ShouldWarmUp()
    {
        Calendar c = Calendar.getInstance();
        Date current = c.getTime();
        try {
            long totalTime = current.getTime() - currentDeviceStatus.WarmUpTime.getTime();
            if(totalTime > 1800000)
                return true;
            else
                return false;
        }catch (Exception e)
        {
            return true;
        }
    }
    /**
     * Update the warm up time in the list
     */
    public static void UpdateWarmUpTimeInList()
    {
        int index = recordDeviceWarmUpTimeList.UUID.indexOf(currentDeviceStatus.UUID);
        if(index >=0)
            recordDeviceWarmUpTimeList.WarmUpTime.get(index).setTime(currentDeviceStatus.WarmUpTime.getTime());
        else
        {
            recordDeviceWarmUpTimeList.UUID.add(currentDeviceStatus.UUID);
            recordDeviceWarmUpTimeList.WarmUpTime.add(currentDeviceStatus.WarmUpTime);
        }
    }
    //endregion
    public static byte[] ChangeScanConfigToByte(ISCMetaScanSDK.ScanConfiguration ScanConfig)
    {
        ISCMetaScanSDK.ScanConfigInfo write_scan_config = new ISCMetaScanSDK.ScanConfigInfo();
        write_scan_config.configName = new byte[40];
        write_scan_config.scanConfigSerialNumber = new byte[8];
        write_scan_config.day = new int[6];
        //transfer config name to byte
        String isoString = ScanConfig.getConfigName();
        int name_size = isoString.length();
        byte[] ConfigNamebytes=isoString.getBytes();
        for(int i=0;i<name_size;i++)
            write_scan_config.configName[i] = ConfigNamebytes[i];
        write_scan_config.write_scanType = 2;
        //transfer SerialNumber to byte
        String SerialNumber = ScanConfig.getScanConfigSerialNumber();
        byte[] SerialNumberbytes=SerialNumber.getBytes();
        int SerialNumber_size = SerialNumber.length();
        for(int i=0;i<SerialNumber_size;i++)
            write_scan_config.scanConfigSerialNumber[i] = SerialNumberbytes[i];
        write_scan_config.write_scanConfigIndex = 255;
        write_scan_config.write_numSections = (byte)ScanConfig.getSlewNumSections();
        write_scan_config.write_numRepeat = ScanConfig.getSectionNumRepeats()[0];
        int numSections = ScanConfig.getSlewNumSections();

        for(int i=0;i<numSections;i++)
            write_scan_config.sectionScanType[i] = ScanConfig.getSectionScanType()[i];
        for(int i=0;i<numSections;i++)
            write_scan_config.sectionWavelengthStartNm[i] = ScanConfig.getSectionWavelengthStartNm()[i];
        for(int i=0;i<numSections;i++)
            write_scan_config.sectionWavelengthEndNm[i] = ScanConfig.getSectionWavelengthEndNm()[i];
        for(int i=0;i<numSections;i++)
            write_scan_config.sectionNumPatterns[i] = ScanConfig.getSectionNumPatterns()[i];
        for(int i=0;i<numSections;i++)
            write_scan_config.sectionWidthPx[i] = ScanConfig.getSectionWidthPx()[i];
        for(int i=0;i<numSections;i++)
            write_scan_config.sectionExposureTime[i] = ScanConfig.getSectionExposureTime()[i];
        return ISCMetaScanSDK.WriteScanConfiguration(write_scan_config);
    }
    public static Boolean Compareconfig(byte EXTRA_DATA[],ISCMetaScanSDK.ScanConfiguration ScanConfig)
    {
        if(EXTRA_DATA.length!=155)
            return false;
        ISCMetaScanSDK.ScanConfiguration config = ISCMetaScanSDK.current_scanConf;
        int numSection = config.getSlewNumSections();
        for(int i=0;i<numSection;i++)
        {
            if(config.getSectionScanType()[i] != ScanConfig.getSectionScanType()[i])
                return false;
        }
        for(int i=0;i<numSection;i++)
        {
            if(config.getSectionWavelengthStartNm()[i] != ScanConfig.getSectionWavelengthStartNm()[i])
                return false;
        }
        for(int i=0;i<numSection;i++)
        {
            if(config.getSectionWavelengthEndNm()[i] != ScanConfig.getSectionWavelengthEndNm()[i])
                return false;
        }
        for(int i=0;i<numSection;i++)
        {
            if(config.getSectionWidthPx()[i] != ScanConfig.getSectionWidthPx()[i])
                return false;
        }
        for(int i=0;i<numSection;i++)
        {
            if(config.getSectionNumPatterns()[i] != ScanConfig.getSectionNumPatterns()[i])
                return false;
        }
        for(int i=0;i<numSection;i++)
        {
            if(config.getSectionNumRepeats()[i] != ScanConfig.getSectionNumRepeats()[i])
                return false;
        }
        for(int i=0;i<numSection;i++)
        {
            if(config.getSectionExposureTime()[i] != ScanConfig.getSectionExposureTime()[i])
                return false;
        }
        return true;
    }
    public static AnimationDrawable ErrorFlash(Context mContext)
    {
        AnimationDrawable ani_error = new AnimationDrawable();
        ani_error.addFrame(mContext.getResources().getDrawable(R.drawable.led_r), 200);
        ani_error.addFrame(mContext.getResources().getDrawable(R.drawable.leg_gray), 200);
        return  ani_error;
    }
}
