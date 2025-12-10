package com.Innospectra.MetaScan;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ListReportActivity extends Activity {
    SwipeMenuListView ListReport;
    private ArrayAdapter mAdapter;
    SwipeMenuCreator unknownCreator = createMenu();
    Context mContext;
    ArrayList<String> ListReportCsv = new ArrayList<>();
    String Dir = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.setCustomDensity(this);
        setContentView(R.layout.activity_list_report);
        mContext = this;
        //Set up the action bar title, and enable the back button
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        InitComponts();
    }
    @Override
    public void onResume() {
        super.onResume();
        ReadCategoryReport();
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
    private void ReadCategoryReport()
    {
        try {
            ListReportCsv.clear();
            Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/MetaScan_Report";
            File folder = new File(Dir + "/");
            for (File f : folder.listFiles()) {
                if (f.getName().contains(".csv")) {
                    ListReportCsv.add(f.getName());
                }
            }
            Collections.sort(ListReportCsv);
        }catch (Exception e)
        {
        }
    }
    private void InitComponts()
    {
        ListReport = (SwipeMenuListView)findViewById(R.id.ListReport);
        mAdapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, ListReportCsv);
        ListReport.setAdapter(mAdapter);
        ListReport.setMenuCreator(unknownCreator);
        ListReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Intent intent = new Intent(mContext, ShowReportActivity.class);
                intent.putExtra("Dir", Dir);
                intent.putExtra("CSVFileName", ListReportCsv.get(index));
                startActivity(intent);
            }
        });
        ListReport.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/MetaScan_Report";
                        String CSVpath = Dir + "/" + ListReportCsv.get(position);
                        File file = new File(CSVpath);
                        if(file.exists())
                        {
                            if(file.delete())
                            {
                                String imgpath = CSVpath.replace(".csv",".jpg");
                                file = new File(imgpath);
                                if(file.exists())
                                    file.delete();
                                ListReportCsv.remove(position);
                                mAdapter= new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, ListReportCsv);
                                ListReport.setAdapter(mAdapter);
                                ListReport.setMenuCreator(unknownCreator);
                            }
                        }

                        break;
                }
                return false;
            }
        });
    }
    /**
     * Populate the stored scan listview with included files in the raw directory as well as
     * stored CSV files
     */

    private SwipeMenuCreator createMenu() {
        return new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                // create Delete item
                SwipeMenuItem item1 = new SwipeMenuItem(mContext);
                // set item width
                item1.setWidth(dp2px(90));
                // set item title
                item1.setTitle("Delete");
                // set item title fontsize
                item1.setTitleSize(18);
                // set item title font color
                item1.setTitleColor(Color.RED);
                // add to menu
                menu.addMenuItem(item1);
            }
        };
    }
    /**
     * Function to convert dip to pixels
     *
     * @param dp the number of dip to convert
     * @return the dip units converted to pixels
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
