package com.cityu_2021_fyp.gesturerecognition_phone.ui.analysis;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.cityu_2021_fyp.gesturerecognition_phone.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class AnalyticalReportFragment extends Fragment {
    private PieChart pie_chart1,pie_chart2;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_analytical_report, container, false);
        findViewByIdes(root);

        return root;
    }

    private void findViewByIdes(View root) {
        pie_chart1 = (PieChart) root.findViewById(R.id.pie_chart1);
        pie_chart2= (PieChart) root.findViewById(R.id.pie_chart2);
        showPieChart1();

        showPieChart2();
    }

    private void showPieChart1() {
        int[][] count = readFromFileAndCount();
        // Set the number of each share
        List<PieEntry> yvals = new ArrayList<>();
        yvals.add(new PieEntry(count[0][0], "Move Left"));
        yvals.add(new PieEntry(count[0][1], "Move Right"));
        yvals.add(new PieEntry(count[1][0], "Flick Wrist In"));
        yvals.add(new PieEntry(count[1][1], "Flick Wrist Out"));
        //Set the color of each copy
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#61819f"));
        colors.add(Color.parseColor("#4f2561"));
        colors.add(Color.parseColor("#f5a658"));
        colors.add(Color.parseColor("#6785f2"));

        PieChartManager pieChartManager=new PieChartManager(pie_chart1);
        pieChartManager.showSolidPieChart(yvals,colors);
    }

    private int[][] readFromFileAndCount() {
        /*
        [0]:Arm[0:MoveLeft, 1: MoveRight]
        [1]:Wrist[0:FlickWristIn, 1: FlickWristOut]
         */
        int[][] count = new int[2][4];
        try {
            Log.d("mContext.getExternalFilesDir(null)", String.valueOf(requireActivity().getExternalFilesDir(null)));
            FileInputStream in = new FileInputStream(new File(requireActivity().getExternalFilesDir(null), "/gesture_records.log"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            //The total number of: MoveLeft, MoveRight, FlickWristIn, FlickWristOut

            String line, gestureType;
            while ((line = reader.readLine()) != null) {
                gestureType = line.substring(line.lastIndexOf(" ")+1);
                Log.d("gestureType", gestureType);
                switch (gestureType){
                    case "MoveLeft":
                        count[0][0]++;
                        continue;
                    case "MoveRight":
                        count[0][1]++;
                        continue;
                    case "FlickWristIn":
                        count[1][0]++;
                        continue;
                    case "FlickWristOut":
                        count[1][1]++;
                        continue;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return count;
    }

    private void showPieChart2() {
        int[][] count = readFromFileAndCount();
        //Set the number of each share
        List<PieEntry> yvals = new ArrayList<>();
        yvals.add(new PieEntry(count[0][0] + count[0][1], "Arm"));
        yvals.add(new PieEntry(count[1][0] + count[1][1], "Wrist"));
        // Set the color of each copy
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#6785f2"));
        colors.add(Color.parseColor("#f5a658"));
        PieChartManager pieChartManager=new PieChartManager(pie_chart2);
        pieChartManager.showRingPieChart(yvals,colors);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}