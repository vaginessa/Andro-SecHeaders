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

package gr.rambou.secheader.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.rambou.secheader.DatabaseHandler;
import gr.rambou.secheader.R;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PreviewColumnChartView;

public class StatsFragment extends Fragment {

    private ColumnChartView chart;
    private PreviewColumnChartView previewChart;
    private ColumnChartData data;
    private TextView stats_txt;
    private Button reload;
    //Deep copy of data.
    private ColumnChartData previewData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        chart = (ColumnChartView) rootView.findViewById(R.id.chart);
        previewChart = (PreviewColumnChartView) rootView.findViewById(R.id.chart_preview);
        stats_txt = (TextView) rootView.findViewById(R.id.stats_txt);

        reload = (Button) rootView.findViewById(R.id.reload_btn);
        reload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                loadChart();
            }
        });

        loadChart();
        return rootView;
    }

    public void loadChart() {
        // Generate data for previewed chart and copy of that data for preview chart.
        generateDefaultData();

        chart.setColumnChartData(data);
        // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
        // zoom/scroll is unnecessary.
        chart.setZoomEnabled(false);
        chart.setScrollEnabled(false);

        previewChart.setColumnChartData(previewData);
        previewChart.setViewportChangeListener(new ViewportListener());

        previewX(false);
    }

    private void generateDefaultData() {
        int numSubcolumns = 2;
        int numColumns = 10;

        //We open/create our sqlite database
        DatabaseHandler mydb = new DatabaseHandler(getActivity().getApplicationContext());
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
