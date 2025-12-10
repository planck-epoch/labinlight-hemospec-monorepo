package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import static com.Innospectra.MetaScan.CommonStruct.CurrentScanConfig;

public class ShowReportActivity extends Activity {
    Context mContext;
    String Dir;
    String CsvFilename;
    private String GraphLabel = "ISC Scan";
    int numSections;
    int SectionIndex = 0;
    private ArrayList<Entry> mIntensityFloat = new ArrayList<>();
    private ArrayList<Entry> mAbsorbanceFloat = new ArrayList<>();
    private ArrayList<Entry> mReflectanceFloat = new ArrayList<>();
    private ArrayList<Entry> mReferenceFloat = new ArrayList<>();

    ArrayList<Button>Button_Section = new ArrayList<>();
    ArrayList<String>ScanType = new ArrayList<>();
    ArrayList<String>ScanWidth = new ArrayList<>();
    ArrayList<String>ScanStart = new ArrayList<>();
    ArrayList<String>ScanEnd = new ArrayList<>();
    ArrayList<String>ScanRes = new ArrayList<>();
    ArrayList<String>ScanExTime = new ArrayList<>();

    ViewPager viewpager;
    TextView tv_referencename;
    TextView tv_config;
    TextView tv_scan_repeat;
    Button btn_section1;
    Button btn_section2;
    Button btn_section3;
    Button btn_section4;
    Button btn_section5;
    TextView tv_scantype;
    TextView tv_scan_width;
    TextView tv_scan_start;
    TextView tv_scan_end;
    TextView tv_scan_res;
    TextView tv_scan_exposuretime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.activity_report_view);
        mContext = this;
        Intent intent = getIntent();
        Dir = intent.getStringExtra("Dir");
        CsvFilename = intent.getStringExtra("CSVFileName");

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        InitComponts();
        ReadCsv();
        OpenSectionButton(numSections-1);
        ViewPagerEvent();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void InitComponts()
    {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        tv_referencename = (TextView) findViewById(R.id.tv_referencename);
        tv_config = (TextView) findViewById(R.id.tv_config);
        tv_scan_repeat = (TextView) findViewById(R.id.tv_scan_repeat);
        btn_section1 = (Button) findViewById(R.id.btn_section1);
        btn_section2 = (Button) findViewById(R.id.btn_section2);
        btn_section3 = (Button) findViewById(R.id.btn_section3);
        btn_section4 = (Button) findViewById(R.id.btn_section4);
        btn_section5 = (Button) findViewById(R.id.btn_section5);
        tv_scantype = (TextView) findViewById(R.id.tv_scantype);
        tv_scan_width = (TextView) findViewById(R.id.tv_scan_width);
        tv_scan_start = (TextView) findViewById(R.id.tv_scan_start);
        tv_scan_end = (TextView) findViewById(R.id.tv_scan_end);
        tv_scan_res = (TextView) findViewById(R.id.tv_scan_res);
        tv_scan_exposuretime = (TextView) findViewById(R.id.tv_scan_exposuretime);

        Button_Section.clear();
        Button_Section.add(btn_section1);
        Button_Section.add(btn_section2);
        Button_Section.add(btn_section3);
        Button_Section.add(btn_section4);
        Button_Section.add(btn_section5);
        for(int i=0;i<5;i++)
            Button_Section.get(i).setOnClickListener(Button_Listener);
    }
    private Button.OnClickListener Button_Listener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.btn_section1:
                    SetSectionColor(0);
                    GetSectionConfig(0);
                    break;
                case R.id.btn_section2:
                    SetSectionColor(1);
                    GetSectionConfig(1);
                    break;
                case R.id.btn_section3:
                    SetSectionColor(2);
                    GetSectionConfig(2);
                    break;
                case R.id.btn_section4:
                    SetSectionColor(3);
                    GetSectionConfig(3);
                    break;
                case R.id.btn_section5:
                    SetSectionColor(4);
                    GetSectionConfig(4);
                    break;
            }
        }
    };
    private void GetSectionConfig(int index)
    {
        tv_scantype.setText(ScanType.get(index));
        tv_scan_width.setText(ScanWidth.get(index));
        tv_scan_start.setText(ScanStart.get(index));
        tv_scan_end.setText(ScanEnd.get(index));
        tv_scan_res.setText(ScanRes.get(index));
        tv_scan_exposuretime.setText(ScanExTime.get(index));
    }
    private void OpenSectionButton(int index)
    {
        for(int i=0;i<=index;i++)
            Button_Section.get(i).setVisibility(View.VISIBLE);
        for(int i=index+1;i<5;i++)
            Button_Section.get(i).setVisibility(View.INVISIBLE);
        SetSectionColor(0);
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
    private void ViewPagerEvent()
    {
        CustomPagerAdapter pagerAdapter = new CustomPagerAdapter(mContext);
        viewpager.setAdapter(pagerAdapter);
        viewpager.invalidate();
        viewpager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between pages, select the
                // corresponding tab.
                ActionBar ab = getActionBar();
                if (ab != null) {
                    getActionBar().setSelectedNavigationItem(position);
                }
            }
        });

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            viewpager.setOffscreenPageLimit(1);
            ActionBar.TabListener tl = new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    viewpager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                }
            };
            for (int i = 0; i < 4; i++) {
                ab.addTab(ab.newTab().setText(getResources().getStringArray(R.array.graph_tab_index)[i]).setTabListener(tl));
            }
        }
    }
    //region read CSV
    private void ReadCsv()
    {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Dir + "/" + CsvFilename));
            String line;
            Boolean IsFindWav = false;
            Boolean IsFindScanInfo = false;
            int Resolution = 0;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                String[] RowData = line.split(",");
                if(RowData.length>=2 && RowData[0].contains("Scan Config Information"))
                    IsFindScanInfo = true;
                if(!IsFindScanInfo && RowData.length>=4 && RowData[0].contains("Wavelength"))
                    IsFindWav = true;
                if(IsFindScanInfo)
                {
                    if(RowData.length>=9)
                    {
                        if(RowData[0].contains("Scan Config Name")) {
                            tv_referencename.setText(RowData[8]);
                            tv_config.setText(RowData[1]);
                        }
                        else if(RowData[0].contains("Scan Config Type"))
                            numSections = Integer.parseInt(RowData[3]);
                        else if(RowData[0].contains("Section Config Type"))
                        {
                            tv_scantype.setText(RowData[1]);
                            index = 1;
                            for(int i=0;i<numSections;i++)
                            {
                                ScanType.add(RowData[index]);
                                index ++;
                            }
                        }
                        else if(RowData[0].contains("Start Wavelength"))
                        {
                            tv_scan_start.setText(RowData[1]);
                            index = 1;
                            for(int i=0;i<numSections;i++)
                            {
                                ScanStart.add(RowData[index]);
                                index ++;
                            }
                        }
                        else if(RowData[0].contains("End Wavelength"))
                        {
                            tv_scan_end.setText(RowData[1]);
                            index = 1;
                            for(int i=0;i<numSections;i++)
                            {
                                ScanEnd.add(RowData[index]);
                                index ++;
                            }
                        }
                        else if(RowData[0].contains("Pattern Width"))
                        {
                            tv_scan_width.setText(RowData[1]);
                            index = 1;
                            for(int i=0;i<numSections;i++)
                            {
                                ScanWidth.add(RowData[index]);
                                index ++;
                            }
                        }
                        else if(RowData[0].contains("Exposure"))
                        {
                            tv_scan_exposuretime.setText(RowData[1]);
                            index = 1;
                            for(int i=0;i<numSections;i++)
                            {
                                ScanExTime.add(RowData[index]);
                                index ++;
                            }
                        }
                        else if(RowData[0].contains("Digital Resolution"))
                        {
                            tv_scan_res.setText(RowData[1]);
                            Resolution = 0;
                            index = 1;
                            for(int i=0;i<numSections;i++)
                            {
                                ScanRes.add(RowData[index]);
                                Resolution += Integer.parseInt(RowData[index]);
                                index ++;
                            }
                        }
                        else if(RowData[0].contains("Num Repeats"))
                        {
                            tv_scan_repeat.setText(RowData[1]);
                            IsFindScanInfo = false;
                        }
                    }
                }
                else if(IsFindWav)
                {
                    for(int i=0;i<Resolution;i++)
                    {
                        line = reader.readLine();
                        RowData = line.split(",");
                        Float fAbsorbance = Float.parseFloat(RowData[1]);
                        Float fReference = Float.parseFloat(RowData[2]);
                        Float fIntensity = Float.parseFloat(RowData[3]);
                        Float fWavelength = Float.parseFloat(RowData[0]);
                        Float fReflect = fIntensity /fReference;
                        mIntensityFloat.add(new Entry(fWavelength, fIntensity));
                        mAbsorbanceFloat.add(new Entry(fWavelength, fAbsorbance));
                        mReferenceFloat.add(new Entry(fWavelength, fReference));
                        mReflectanceFloat.add(new Entry(fWavelength, fReflect));
                    }
                    IsFindWav = false;
                }

            }
        }catch (Exception e)
        {

        }
    }
    //endregion
    //region plot
    /**
     * Custom pager adapter to handle changing chart data when pager tabs are changed
     */
    public class CustomPagerAdapter extends PagerAdapter {

        private final Context mContext;

        public CustomPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            ScanViewActivity.CustomPagerEnum customPagerEnum = ScanViewActivity.CustomPagerEnum.values()[position];
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(customPagerEnum.getLayoutResId(), collection, false);
            collection.addView(layout);
            LineChart mChart = (LineChart) layout.findViewById(R.id.lineChartRef);
            YAxis leftAxis = new YAxis();
           // int numSections = CurrentScanConfig.getSlewNumSections();
            if (customPagerEnum.getLayoutResId() == R.layout.page_graph_reflectance) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartRef);
                leftAxis = mChart.getAxisLeft();
                if(numSections == 1)
                    setData(mChart, mReflectanceFloat, ScanViewActivity.ChartType.REFLECTANCE);
                else
                    setDataSlew(mChart, mReflectanceFloat,numSections);
            }
            else if (customPagerEnum.getLayoutResId() == R.layout.page_graph_absorbance) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartAbs);
                leftAxis = mChart.getAxisLeft();
                if(numSections == 1)
                    setData(mChart, mAbsorbanceFloat, ScanViewActivity.ChartType.ABSORBANCE);
                else
                    setDataSlew(mChart, mAbsorbanceFloat,numSections);
            }
            else if (customPagerEnum.getLayoutResId() == R.layout.page_graph_intensity) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartInt);
                leftAxis = mChart.getAxisLeft();
                if(numSections == 1)
                    setData(mChart, mIntensityFloat, ScanViewActivity.ChartType.INTENSITY);//scan data section = 1
                else
                    setDataSlew(mChart, mIntensityFloat,numSections);

            } else if (customPagerEnum.getLayoutResId() == R.layout.page_graph_reference) {
                mChart = (LineChart) layout.findViewById(R.id.lineChartReference);
                leftAxis = mChart.getAxisLeft();
                if(numSections == 1)
                    setData(mChart, mReferenceFloat, ScanViewActivity.ChartType.REFERENCE);
                else
                    setDataSlew(mChart, mReferenceFloat,numSections);
            }
            //Set mchart setting
            mChart.setDrawGridBackground(false);
            // enable touch gestures
            mChart.setTouchEnabled(true);
            // enable scaling and dragging
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(true);
            mChart.setAutoScaleMinMaxEnabled(true);
            mChart.getAxisRight().setEnabled(false);
            mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
            mChart.getLegend().setEnabled(false);

            // x-axis limit line
            LimitLine llXAxis = new LimitLine(10f, "Index 10");
            llXAxis.setLineWidth(4f);
            llXAxis.enableDashedLine(10f, 10f, 0f);
            llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            llXAxis.setTextSize(10f);

            XAxis xAxis = mChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            leftAxis.setStartAtZero(false);
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawLimitLinesBehindData(true);

            // get the legend (only possible after setting data)
            Legend l = mChart.getLegend();
            // modify the legend ...
            l.setForm(Legend.LegendForm.LINE);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return ScanViewActivity.CustomPagerEnum.values().length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.reflectance);
                case 1:
                    return getString(R.string.absorbance);
                case 2:
                    return getString(R.string.intensity);
                case 3:
                    return getString(R.string.reference_tab);
            }
            return null;
        }

    }

    private void setData(LineChart mChart, ArrayList<Entry> yValues, ScanViewActivity.ChartType type) {

        int size = yValues.size();
        if(size == 0)
            return;
        LineDataSet set1 = new LineDataSet(yValues,GraphLabel);
        set1.enableDashedLine(10f, 5f, 0f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleSize(2f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setFillAlpha(65);
        set1.setDrawFilled(true);
        set1.setValues(yValues);
        if (type == ScanViewActivity.ChartType.REFLECTANCE) {
            set1.setCircleColor(Color.RED);
            set1.setFillColor(Color.RED);
        } else if (type == ScanViewActivity.ChartType.ABSORBANCE) {
            set1.setCircleColor(Color.GREEN);
            set1.setFillColor(Color.GREEN);
        } else if (type == ScanViewActivity.ChartType.INTENSITY||type == ScanViewActivity.ChartType.REFERENCE) {
            set1.setCircleColor(Color.BLUE);
            set1.setFillColor(Color.BLUE);
        }
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.setMaxVisibleValueCount(20);
    }
    private void setDataSlew(LineChart mChart, ArrayList<Entry> yValues,int slewnum)
    {
        if(yValues.size()<=1)
            return;
        ArrayList<ArrayList<Entry>> ListyValues = new  ArrayList<ArrayList<Entry>>();
        ArrayList<Integer>ListOffset = new ArrayList<>();
        ArrayList<LineDataSet>ListlineSet = new ArrayList<>();
        for(int i=0;i<slewnum;i++)
        {
            ArrayList<Entry> bufyValues = new ArrayList<Entry>();
            ListyValues.add(bufyValues);
            int bufoffset = 0;
            for(int j=0;j<=i;j++)
                bufoffset += Integer.parseInt(ScanRes.get(j));
            ListOffset.add(bufoffset);
        }
        for(int k=0;k<slewnum;k++)
        {
            int NumPattern = Integer.parseInt(ScanRes.get(k));
            int offset = 0;
            if(k > 0)
                offset = ListOffset.get(k-1);
            for(int i=0;i<NumPattern;i++)
            {
                if(Float.isInfinite(yValues.get(offset+ i).getY()) == false)
                    ListyValues.get(k).add(new Entry(yValues.get(offset + i).getX(),yValues.get(offset + i).getY()));
            }
        }
        for(int i=0;i<slewnum;i++)
        {
            LineDataSet bufset = new LineDataSet(ListyValues.get(i),"Slew" + Integer.toString(i+1));
            bufset.enableDashedLine(10f, 5f, 0f);
            bufset.enableDashedHighlightLine(10f, 5f, 0f);
            bufset.setLineWidth(1f);
            bufset.setCircleSize(2f);
            bufset.setDrawCircleHole(false);
            bufset.setValueTextSize(9f);
            bufset.setFillAlpha(65);
            bufset.setDrawFilled(true);
            switch (i)
            {
                case 0:
                    bufset.setColor(Color.BLUE);
                    bufset.setCircleColor(Color.BLUE);
                    bufset.setFillColor(Color.BLUE);
                    break;
                case 1:
                    bufset.setColor(Color.RED);
                    bufset.setCircleColor(Color.RED);
                    bufset.setFillColor(Color.RED);
                    break;
                case 2:
                    bufset.setColor(Color.GREEN);
                    bufset.setCircleColor(Color.GREEN);
                    bufset.setFillColor(Color.GREEN);
                    break;
                case 3:
                    bufset.setColor(Color.YELLOW);
                    bufset.setCircleColor(Color.YELLOW);
                    bufset.setFillColor(Color.YELLOW);
                    break;
                case 4:
                    bufset.setColor(Color.LTGRAY);
                    bufset.setCircleColor(Color.LTGRAY);
                    bufset.setFillColor(Color.LTGRAY);
                    break;
            }
            bufset.setValues(ListyValues.get(i));
            ListlineSet.add(bufset);
        }
        LineData data = new LineData();
        switch (slewnum)
        {
            case 2:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1));
                break;
            case 3:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1), ListlineSet.get(2));
                break;
            case 4:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1), ListlineSet.get(2), ListlineSet.get(3));
                break;
            case 5:
                data = new LineData(ListlineSet.get(0), ListlineSet.get(1), ListlineSet.get(2), ListlineSet.get(3), ListlineSet.get(4));
                break;
        }
        mChart.setData(data);
        mChart.setMaxVisibleValueCount(20);
    }
    /**
     * Custom enum for chart type
     */
    public enum ChartType {
        REFLECTANCE,
        ABSORBANCE,
        INTENSITY,
        REFERENCE
    }
    //endregion

}
