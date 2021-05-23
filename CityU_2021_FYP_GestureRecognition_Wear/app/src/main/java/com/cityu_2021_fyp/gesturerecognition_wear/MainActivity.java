package com.cityu_2021_fyp.gesturerecognition_wear;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import utils.TimestampAxisFormatter;

public class MainActivity extends WearableActivity {

    private static final String[] LINE_DESCRIPTIONS = {"X", "Y", "Z"};
    private Button listen, listDevices, saveMotion;  //,send
    private ToggleButton recMotion;
    private ListView listView;
    private TextView msg_box,status;
    LinearLayout linLayoutforBlueTooth, linLayoutForRecording;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendReceive sendReceive;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;
    static final int REQUEST_ENABLE_BLUETOOTH=1;

    private LineChart chart;
    private long firstTimestamp = -1;
    private int selectedEntryIndex = -1;
    private static final int GESTURE_SAMPLES = 128;
    private static final int GESTURE_DURATION_MS = 1280000; // 1.28 sec


    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean recStarted = false;
    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;
    private static final int Z_INDEX = 2;
    private static final int[] LINE_COLORS = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF};  //Transparency is 0xFF, which is completely opaque, Red, Green, Blue



    private static final String APP_NAME = "CityU_2021_FYP_GestureRecognition";
    private static final UUID MY_UUID=UUID.fromString("8ce235c0-223a-19e0-ac64-0803950c9a66");
    private long fileNameTimestamp= -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enables Always-on
        setAmbientEnabled();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        findViewByIdes();

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        //need to comment the following for opening in simulator
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BLUETOOTH);
        }

        implementViewListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findViewByIdes() {
        listen = findViewById(R.id.listen);
        listDevices = findViewById(R.id.listDevices);
        listView = findViewById(R.id.listview);
        msg_box = findViewById(R.id.msg_box);
        status = findViewById(R.id.status);
        recMotion = findViewById(R.id.recMotion);
        saveMotion = findViewById(R.id.saveMotion); //idea: auto send the file to phone by using bluetooth
        linLayoutforBlueTooth = findViewById(R.id.linLayoutforBlueTooth);

        chart = findViewById(R.id.chart);

        //chart.setLogEnabled(true);
        chart.setTouchEnabled(true);
        chart.setOnChartValueSelectedListener(chartValueSelectedListener);
        chart.setData(new LineData());
        getLineData().setValueTextColor(Color.WHITE);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextColor(Color.WHITE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);

        xAxis.setValueFormatter(new TimestampAxisFormatter());

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setAxisMaximum(10f);
        rightAxis.setAxisMinimum(-10f);
        rightAxis.setDrawGridLines(true);
    }

    private void implementViewListeners() {

        listDevices.setOnClickListener(view -> {
            //show listview
            listView.setVisibility(View.VISIBLE);

            Set<BluetoothDevice> bt=bluetoothAdapter.getBondedDevices();
            String[] strings=new String[bt.size()];
            btArray=new BluetoothDevice[bt.size()];
            int index=0;

            if( bt.size()>0)
            {
                for(BluetoothDevice device : bt)
                {
                    btArray[index]= device;
                    strings[index]=device.getName();
                    index++;
                }
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                listView.setAdapter(arrayAdapter);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass=new ClientClass(btArray[i]);
                clientClass.start();
                status.setText("Connecting");
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerClass serverClass=new ServerClass();
                serverClass.start();
            }
        });

        recMotion.setOnClickListener(view -> {
            if (recStarted) {
                stopRec();
            } else {
                startRec();
            }
        });

//        send.setOnClickListener(new View.OnClickListener() {    //no send button at this time.
//            @Override
//            public void onClick(View view) {
//                String string= String.valueOf(writeMsg.getText());
//                sendReceive.write(string.getBytes());
//            }
//        });
    }

    private void stopRec() {
        if (recStarted) {
            sensorManager.unregisterListener(sensorEventListener);
            recStarted = false;
        }
    }

    private void startRec() {
        if (isStartRec()) {
            getLineData().clearValues();
        }
        else {
            Toast.makeText(this, R.string.sensor_failed, Toast.LENGTH_SHORT).show();    //error may occurs in line 286->(recStarted = sensorManager.registerListener...)
            recMotion.setChecked(false);
        }
    }

    private boolean isStartRec() {
        if (!recStarted) {
            firstTimestamp = -1;
            fileNameTimestamp = System.currentTimeMillis();
            chart.highlightValue(null, true);
            recStarted = sensorManager.registerListener(sensorEventListener, accelerometer, GESTURE_DURATION_MS / GESTURE_SAMPLES);
        }
        return recStarted;
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (firstTimestamp == -1) firstTimestamp = event.timestamp;
            long entryTimestampFixed = event.timestamp - firstTimestamp;

            final float floatTimestampMicros = entryTimestampFixed / 1000000f;
            final float x = event.values[0];
            final float y = event.values[1];
            final float z = event.values[2];

            addPoint(getLineData(), X_INDEX, floatTimestampMicros, x);
            addPoint(getLineData(), Y_INDEX, floatTimestampMicros, y);
            addPoint(getLineData(), Z_INDEX, floatTimestampMicros, z);
            Log.d("floatTimestampMicros", String.valueOf(floatTimestampMicros));
            chart.notifyDataSetChanged();
            chart.invalidate();

            //supportInvalidateOptionsMenu();
            fillStatus();
        }
    };

    private static void addPoint(LineData data, int dataSetIndex, float timestamp, float value) {
        ILineDataSet set = data.getDataSetByIndex(dataSetIndex);

        if (set == null) {
            set = createLineDataSet(LINE_DESCRIPTIONS[dataSetIndex], LINE_COLORS[dataSetIndex]);
            data.addDataSet(set);
        }

        data.addEntry(new Entry(timestamp, value), dataSetIndex);

        data.notifyDataChanged();
    }

    //ILineDataset is a dataset interface that can be set by its implementation class LineDataset.
    private static LineDataSet createLineDataSet(String lineDescription, int lineColor) {
        LineDataSet set = new LineDataSet(null, lineDescription);
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColor(lineColor);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setLineWidth(1f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.WHITE);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setDrawHighlightIndicators(true);
        set.setDrawIcons(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawFilled(false);
        return set;
    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    listView.setVisibility(View.GONE);
                    linLayoutforBlueTooth.setVisibility(View.GONE);
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                serverSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket=null;

            while (socket==null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive=new SendReceive(socket);
                    sendReceive.start();
                    Log.d("isRunning", "Hi, socket is fine!");
                    break;
                }else{
                    Log.d("isRunning", "Hi, socket is null! :(");
                }
            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1)
        {
            device=device1;

            try {
                socket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive=new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {   //Problem found: only success first time, then always listening the connection and can't change to "connected" status
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setLineData(LineData lineData) {
        chart.setData(lineData);
    }

    private LineData getLineData() {
        return chart.getLineData();
    }

    private final OnChartValueSelectedListener chartValueSelectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {
            ILineDataSet set = getLineData().getDataSetByIndex(h.getDataSetIndex());
            selectedEntryIndex = set.getEntryIndex(e);
            //supportInvalidateOptionsMenu();
            //return current selected part of information
            fillStatus();

            // highlight ending line
            Entry endEntry = getSelectionEndEntry();
            if (endEntry != null) {
                Highlight endHighlight = new Highlight(endEntry.getX(), endEntry.getY(), h.getDataSetIndex());
                chart.highlightValues(new Highlight[]{h, endHighlight});
            }
        }

        @Override
        public void onNothingSelected() {
            selectedEntryIndex = -1;
            //supportInvalidateOptionsMenu();
            fillStatus();
        }
    };

    private Entry getSelectionEndEntry() {
        int index = selectedEntryIndex + GESTURE_SAMPLES;
        //@return null if index of endEntry is not exist the chart
        if (index >= chart.getLineData().getDataSetByIndex(0).getEntryCount())
            return null;

        return chart.getLineData().getDataSetByIndex(0).getEntryForIndex(index);
    }

    private void fillStatus() {
        msg_box.setText(formatStatsText());
    }

    private String formatStatsText() {
        return String.format("Pos: %s/%s s\nSamples: %d", getXLabelAtHighlight(), getXLabelAtEnd(), getSamplesCount());
    }

    private String getXLabelAtHighlight() {
        if ((selectedEntryIndex == -1) || (getLineData().getDataSetCount() == 0)) return "-";
        return String.format("%.03f", getLineData().getDataSetByIndex(0).getEntryForIndex(selectedEntryIndex).getX() / 1000f);
    }

    private String getXLabelAtEnd() {
        if ((getLineData().getDataSetCount() == 0) || (getLineData().getDataSetByIndex(0).getEntryCount() == 0)) return "-";
        return String.format("%.03f", getLineData().getDataSetByIndex(0).getEntryForIndex(getLineData().getDataSetByIndex(0).getEntryCount() - 1).getX() / 1000f);
    }

    private int getSamplesCount() {
        if (getLineData().getDataSetCount() == 0) return 0;
        return getLineData().getDataSetByIndex(0).getEntryCount();
    }


}
