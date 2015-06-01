/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Nikos Bousios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package gr.rambou.secheader.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.rambou.secheader.DatabaseHandler;
import gr.rambou.secheader.R;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;
import lecho.lib.hellocharts.view.PreviewColumnChartView;

/**
 * Created by Nickos on 24/3/2015.
 */
public class StatsAdapter {

    private Context context;
    private int layout;
    private ColumnChartView chart;
    private PreviewColumnChartView previewChart;
    private ColumnChartData data;
    private PieChartView pchart;
    private PieChartData pdata;
    private TextView stats_txt;
    private Button reload;
    //Deep copy of data.
    private ColumnChartData previewData;

    public StatsAdapter(Context context, int layout) {
        //super(context, layout, "");
        this.layout = layout;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        chart = (ColumnChartView) row.findViewById(R.id.chart);
        previewChart = (PreviewColumnChartView) row.findViewById(R.id.chart_preview);

        //Initialize Pie Chart and its touch listener
        pchart = (PieChartView) row.findViewById(R.id.piechart);

        loadChart();
        return row;
    }

    public void loadChart() {
        // Generate data for previewed chart and copy of that data for preview chart.
        generateChartData();
        generatePieChartData();

        chart.setColumnChartData(data);
        // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
        // zoom/scroll is unnecessary.
        chart.setZoomEnabled(false);
        chart.setScrollEnabled(false);

        previewChart.setColumnChartData(previewData);
        previewChart.setViewportChangeListener(new ViewportListener());

        previewX(false);
    }

    private void generateChartData() {
        int numSubcolumns = 2;
        int numColumns = 10;

        //We open/create our sqlite database
        DatabaseHandler mydb = new DatabaseHandler(context.getApplicationContext());
        //We get the column with websites
        String[] Websites = mydb.getColumnValues(DatabaseHandler.Type.KEY_WEBSITE);

        numColumns = Websites.length;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {
            //Get All headers for specific website
            HashMap<String, String> website_headers = mydb.getHeader(Websites[i]);

            int all_headers = 0, secure = 0;
            for (Map.Entry<String, String> entry : website_headers.entrySet()) {
                secure += Integer.parseInt(entry.getValue());
                all_headers++;
            }

            values = new ArrayList<SubcolumnValue>();
            //The first column is the secure ones and the second is the non secure
            values.add(new SubcolumnValue(all_headers, ChartUtils.pickColor()));
            values.add(new SubcolumnValue(secure, ChartUtils.pickColor()));

            axisValues.add(new AxisValue(i).setLabel(Websites[i]));
            columns.add(new Column(values));
        }

        data = new ColumnChartData(columns);
        data.setAxisXBottom(new Axis(axisValues).setHasLines(true));
        data.setAxisYLeft(new Axis().setHasLines(true));

        // prepare preview data, is better to use separate deep copy for preview chart.
        // set color to grey to make preview area more visible.
        previewData = new ColumnChartData(data);
        for (Column column : previewData.getColumns()) {
            for (SubcolumnValue value : column.getValues()) {
                value.setColor(ChartUtils.DEFAULT_DARKEN_COLOR);
            }
        }

    }

    private void generatePieChartData() {
        //hasLabelsOutside
        pchart.setCircleFillRatio(0.7f);

        //We open/create our sqlite database
        DatabaseHandler mydb = new DatabaseHandler(context.getApplicationContext());
        //We get the column with websites
        HashMap<String, String[]> HeaderStats = mydb.getAllHeadersStats();
        Set<Map.Entry<String, String[]>> keys = HeaderStats.entrySet();

        List<SliceValue> values = new ArrayList<>();
        for (Map.Entry<String, String[]> key : keys) {
            int occurred = Integer.parseInt(key.getValue()[1]);
            int secure = Integer.parseInt(key.getValue()[0]);

            SliceValue sliceValue = new SliceValue(occurred, ChartUtils.pickColor());
            float secure_percent = (float) (secure / occurred) * 100;
            values.add(sliceValue.setLabel(key.getKey() + " " + secure_percent + "%"));
        }

        pdata = new PieChartData(values);
        pdata.setHasLabels(true);
        pdata.setHasLabelsOnlyForSelected(false);
        pdata.setHasLabelsOutside(true);

        //set pieChart's data
        pchart.setPieChartData(pdata);
    }

    private void previewX(boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    static class SiteHolder {
        TextView name;
        TextView info;
    }

    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart because usually viewport changes
            // happens to often.
            chart.setCurrentViewport(newViewport);
        }

    }
}
