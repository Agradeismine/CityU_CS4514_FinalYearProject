package com.cityu_2021_fyp.gesturerecognition_phone.ui.analysis;

import android.graphics.Color;
import android.graphics.Typeface;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.List;

public class PieChartManager {

    public PieChart pieChart;

    public PieChartManager(PieChart pieChart) {
        this.pieChart = pieChart;
        initPieChart();
    }

    //initialization
    private void initPieChart() {
        // Whether to show the middle hole
        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleRadius(40f);//Set the size of the middle hole
        // translucent circle
        pieChart.setTransparentCircleRadius(30f);
        pieChart.setTransparentCircleColor(Color.WHITE); //Set the color of the translucent circle
        pieChart.setTransparentCircleAlpha(125); //Set the transparency of the semi-transparent circle

        //You can add text in the middle of the pie chart
        pieChart.setDrawCenterText(false);
        pieChart.setCenterText("Nation"); //Set the middle text
        pieChart.setCenterTextColor(Color.parseColor("#a1a1a1")); //The color of the middle question
        pieChart.setCenterTextSizePixels(36);  //The size of the middle text px
        pieChart.setCenterTextRadiusPercent(1f);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT); //The style of the middle text
        pieChart.setCenterTextOffset(0, 0); //Offset of middle text


        pieChart.setRotationAngle(0);// Initial rotation angle
        pieChart.setRotationEnabled(true);// Can be rotated manually
        pieChart.setUsePercentValues(true);//Displayed as a percentage
        pieChart.getDescription().setEnabled(false); //Cancel the description in the lower right corner

        //Whether to display the text description of each part
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelColor(Color.RED); //The color of the description text
        pieChart.setEntryLabelTextSize(14);//Description text size
        pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD); //Description style

        //The offset of the figure relative to the up, down, left and right
        pieChart.setExtraOffsets(20, 8, 75, 8);
        //The background color of the icon
        pieChart.setBackgroundColor(Color.TRANSPARENT);
// Set the friction coefficient of pieChart chart rotation resistance [0,1]
        pieChart.setDragDecelerationFrictionCoef(0.75f);

        //Get legend
        Legend legend = pieChart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);  //Set the legend horizontal display
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP); //top
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT); //Right to it

        legend.setXEntrySpace(7f);//x axis spacing
        legend.setYEntrySpace(10f); //Y axis spacing
        legend.setYOffset(10f);  //Y offset of the legend
        legend.setXOffset(10f);  //Offset of legend x
        legend.setTextColor(Color.parseColor("#a1a1a1")); //The color of the legend text
        legend.setTextSize(13);  //The size of the legend text

    }


    /**
     * Display solid circle
     * @param yvals
     * @param colors
     */
    public void showSolidPieChart(List<PieEntry> yvals, List<Integer> colors) {
        //Data collection
        PieDataSet dataset = new PieDataSet(yvals, "");
        //Fill the color of each area
        dataset.setColors(colors);
        //Whether to display the value on the graph
        dataset.setDrawValues(true);
// The size of the text
        dataset.setValueTextSize(14);
// text color
        dataset.setValueTextColor(Color.RED);
// Style of text
        dataset.setValueTypeface(Typeface.DEFAULT_BOLD);

// When the value position is the outer edge, it means the length of the first half of the line.
        dataset.setValueLinePart1Length(0.4f);
// When the value position is the outer line, it indicates the length of the second half of the line.
        dataset.setValueLinePart2Length(0.4f);
// When ValuePosits is OutsiDice, the offset is indicated as a percentage of the slice size
        dataset.setValueLinePart1OffsetPercentage(80f);
        // When the value position is the outer line, it indicates the color of the line.
        dataset.setValueLineColor(Color.parseColor("#a1a1a1"));
// Set the position of Y value inside or outside the circle
        dataset.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
// Set the gap before each
        dataset.setSliceSpace(0);

        //Set the distance that changes when the pie item is selected
        dataset.setSelectionShift(5f);
        //Data input
        PieData pieData = new PieData(dataset);
// The formatted data is%
        pieData.setValueFormatter(new PercentFormatter());
// show attempt
        pieChart.setData(pieData);
    }


    /**
     * Display ring
     * @param yvals
     * @param colors
     */
    public void  showRingPieChart(List<PieEntry> yvals, List<Integer> colors){
        //Show as ring
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(85f);//Set the size of the middle hole

        //Data collection
        PieDataSet dataset = new PieDataSet(yvals, "");
        //Fill the color of each area
        dataset.setColors(colors);
        //Whether to display the value on the graph
        dataset.setDrawValues(true);
// The size of the text
        dataset.setValueTextSize(14);
// The color of the text
        dataset.setValueTextColor(Color.RED);
// Style of text
        dataset.setValueTypeface(Typeface.DEFAULT_BOLD);

// When the value position is the outer edge, it means the length of the first half of the line.
        dataset.setValueLinePart1Length(0.4f);
// When the value position is the outer line, it indicates the length of the second half of the line.
        dataset.setValueLinePart2Length(0.4f);
// When ValuePosits is OutsiDice, the offset is indicated as a percentage of the slice size
        dataset.setValueLinePart1OffsetPercentage(80f);
        // When the value position is the outer line, it indicates the color of the line.
        dataset.setValueLineColor(Color.parseColor("#a1a1a1"));
// Set the position of Y value inside or outside the circle
        dataset.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
// Set the gap before each
        dataset.setSliceSpace(0);

        //Set the distance that changes when the pie item is selected
        dataset.setSelectionShift(5f);
        //Data input
        PieData pieData = new PieData(dataset);
// The formatted data is%
        pieData.setValueFormatter(new PercentFormatter());
// show attempt
        pieChart.setData(pieData);

    }


}