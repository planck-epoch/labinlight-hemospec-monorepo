package com.Innospectra.MetaScan;

import static com.Innospectra.MetaScan.CommonStruct.deviceInfo;
import static com.Innospectra.MetaScan.CommonStruct.exposure_time_vlaue;
import static com.Innospectra.MetaScan.CommonStruct.widthnm;
import static com.Innospectra.MetaScan.CommonStruct.widthnm_plus;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ISCMetaScanSDK.ISCMetaScanSDK;

import java.util.ArrayList;

class ViewHolder {
    public TextView scanType;
    public TextView rangeStart;
    public TextView rangeEnd;
    public TextView width;
    public TextView patterns;
    public TextView repeats;
    public TextView exposure;
}
public class SlewScanConfAdapter extends ArrayAdapter<ISCMetaScanSDK.SlewScanSection>{
    private final ArrayList<ISCMetaScanSDK.SlewScanSection> sections;
    public SlewScanConfAdapter(Context context, ArrayList<ISCMetaScanSDK.SlewScanSection> values) {
        super(context, -1, values);
        this.sections = values;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.row_slew_scan_configuration_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.scanType = (TextView) convertView.findViewById(R.id.tv_scantype_value);
            viewHolder.repeats = (TextView)convertView.findViewById(R.id.tv_scan_repeat_value);
            viewHolder.rangeStart = (TextView) convertView.findViewById(R.id.tv_range_start_value);
            viewHolder.rangeEnd = (TextView) convertView.findViewById(R.id.tv_range_end_value);
            viewHolder.width = (TextView) convertView.findViewById(R.id.tv_width_value);
            viewHolder.patterns = (TextView) convertView.findViewById(R.id.tv_patterns_value);
            viewHolder.exposure = (TextView)convertView.findViewById(R.id.tv_active_exposure_value);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ISCMetaScanSDK.SlewScanSection config = getItem(position);
        if (config != null) {
            int widthindex = (int)config.getWidthPx();
            int scantype = config.getSectionScanType();
            if(scantype == 0)
                viewHolder.scanType.setText("Column");
            else
                viewHolder.scanType.setText("Hadamard");
            viewHolder.repeats.setText(Integer.toString(config.getNumRepeats()));
            viewHolder.rangeStart.setText(getContext().getString(R.string.range_start_value, config.getWavelengthStartNm()));
            viewHolder.rangeEnd.setText(getContext().getString(R.string.range_end_value, config.getWavelengthEndNm()));
            if(deviceInfo.deviceWavelengthType.compareTo(CommonStruct.DeviceWavelengthType.Extend_Plus) == 0)
                viewHolder.width.setText(widthnm_plus[widthindex] + " nm");
            else
                viewHolder.width.setText(widthnm[widthindex] + " nm");
            viewHolder.patterns.setText(getContext().getString(R.string.patterns_value, config.getNumPatterns()));
            int index = config.getExposureTime();
            viewHolder.exposure.setText(exposure_time_vlaue[index] + " ms");
        }
        return convertView;
    }
}
