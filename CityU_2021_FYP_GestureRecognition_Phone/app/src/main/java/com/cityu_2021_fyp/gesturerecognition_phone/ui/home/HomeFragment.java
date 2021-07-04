package com.cityu_2021_fyp.gesturerecognition_phone.ui.home;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cityu_2021_fyp.gesturerecognition_phone.LoadingDialog;
import com.cityu_2021_fyp.gesturerecognition_phone.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import utils.FileStorage;

public class HomeFragment extends Fragment {

    private Context mContext;
    private Button listen, listDevices;
    private ListView listView;
    private TextView msg_box, bluetoothStatus, msgStatus;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendReceive sendReceive;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "CityU_2021_FYP_GestureRecognition";
    private static final UUID MY_UUID = UUID.fromString("8ce235c0-223a-19e0-ac64-0803950c9a66");

    private LoadingDialog loadingDialog;
    private String receivedString;
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private ImageButton bluetoothSettingBtn;

    private String gesture_records;
    private ScrollView globalScrollView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        findViewByIdes(root);

//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                msg_box.setText(s);
//            }
//        });

        loadingDialog = new LoadingDialog(getActivity());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        implementListeners();
        mContext = requireActivity().getApplicationContext();
        gesture_records = readFromFile();
        msg_box.setText(gesture_records);

        return root;
    }

    private String readFromFile() {
        StringBuilder out = new StringBuilder();
        try {
            InputStream in = new FileInputStream(new File(mContext.getExternalFilesDir(null),"/gesture_records.log"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }
            reader.close();
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return out.toString();
    }

    private void checkPermission() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
//        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
//            //do something
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermission();
    }

    private void implementListeners() {

        bluetoothSettingBtn.setOnClickListener(v -> {
            if(listen.getVisibility()==View.GONE){
                bluetoothStatus.setText("State");
                bluetoothStatus.setTextColor(0xFF000000);
                listen.setVisibility(View.VISIBLE);
                listDevices.setVisibility(View.VISIBLE);
                bluetoothSettingBtn.setVisibility(View.GONE);
            }
        });

        listDevices.setOnClickListener(view -> {
            //show listDevices
            listView.setVisibility(View.VISIBLE);

            Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
            String[] strings = new String[bt.size()];
            btArray = new BluetoothDevice[bt.size()];
            int index = 0;

            if (bt.size() > 0) {
                for (BluetoothDevice device : bt) {
                    btArray[index] = device;
                    strings[index] = device.getName();
                    index++;
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, strings);
                listView.setAdapter(arrayAdapter);
            }
        });

        listen.setOnClickListener(view -> {
            ServerClass serverClass = new ServerClass();
            serverClass.start();
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            ClientClass clientClass = new ClientClass(btArray[i]);
            clientClass.start();
            bluetoothStatus.setText("Connecting");
            bluetoothStatus.setTextColor(0xFFE0AF1F);
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    bluetoothStatus.setText("Listening");
                    bluetoothStatus.setTextColor(0xFFE0AF1F);
                    break;
                case STATE_CONNECTING:
                    bluetoothStatus.setText("Connecting");
                    bluetoothStatus.setTextColor(0xFFE0AF1F);
                    break;
                case STATE_CONNECTED:
                    bluetoothStatus.setText("Connected");
                    globalScrollView.fullScroll(View.FOCUS_DOWN);
                    bluetoothStatus.setTextColor(Color.GREEN);
                    listen.setVisibility(View.GONE);
                    listDevices.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    bluetoothSettingBtn.setVisibility(View.VISIBLE);
                    showToast("Connected with Android Wear successfully");
                    break;
                case STATE_CONNECTION_FAILED:
                    bluetoothStatus.setText("Connection Failed");
                    bluetoothStatus.setTextColor(Color.RED);
                    msg_box.append("Waiting for Connection\uD83D\uDCF1\uD83D\uDD17⌚");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    Log.d("tempMsg", tempMsg);
                    if (tempMsg.equalsIgnoreCase("Start\n")) {          //start to receive message
                        msgStatus.setTextColor(0xFF74BDDD);
                        msgStatus.setText("Receiving...");
                        receivedString = "";
                        loadingDialog.startLoadingDialog();
                        showToast("Start receive data");
                    } else if (tempMsg.equalsIgnoreCase("End\n")) {     //data received, handle the message
                        msgStatus.setText("");
                        loadingDialog.dismissDialog();
                        showToast("Message receive successfully");

                        //The received string contains many data, thats mean it is data file
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Motion log file received. Do you want to save it?");
                        builder.setPositiveButton("OK", (dialog, id) -> {
                            try {
                                String filePath = FileStorage.saveMotionData(mContext, receivedString);  //mContext.getCacheDir().getAbsolutePath()
                                showToast("File saved into " + filePath + "successfully");
                            } catch (IOException e) {
                                showToast("Error occurred, detailed in Android Studio");
                                e.printStackTrace();
                            }
                        });
                        builder.setNegativeButton("Cancel", (dialog, id) -> {
                            //do noting
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else if (tempMsg.length() < 18) {    //the received string is a gesture type
                        addLog("Gesture detected: " + tempMsg);
                    } else {
                        receivedString += tempMsg;
                    }
                    break;
            }
            return true;
        }
    });

    private void addLog(String str) {
        Date date = new Date();
        String logStr = String.format("[%s %s] %s\n", getDateFormat().format(date), getTimeFormat().format(date), str);
        msg_box.append(logStr);
        globalScrollView.fullScroll(View.FOCUS_DOWN);

        try {
            File newFile = new File(mContext.getExternalFilesDir(null), "gesture_records.log");
            newFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(newFile, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            //String header = String.format("index,x,y,z\n");
            osw.write(logStr);
            osw.close();
            Log.d("mContext.getExternalFilesDir(null).getAbsolutePath()/", "gesture_records.log");
        } catch (IOException e) {
            showToast("Error, detailed in Android Studio");
            e.printStackTrace();
        }
    }

    private DateFormat getTimeFormat() {
        if (timeFormat == null) {
            timeFormat = android.text.format.DateFormat.getTimeFormat(mContext);
        }
        return timeFormat;
    }

    private DateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = android.text.format.DateFormat.getDateFormat(mContext);
        }
        return dateFormat;

    }

    private void showToast(String str) {
        Toast toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    //Server side
    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //continuously waiting for a device connection until paired with another
        public void run() {
            BluetoothSocket socket = null;

            while (true) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                //find
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void findViewByIdes(View root) {
        globalScrollView = root.findViewById(R.id.globalScrollView);
        msg_box = root.findViewById(R.id.msg_box);
        bluetoothSettingBtn = root.findViewById(R.id.bluetoothSettingBtn);
        bluetoothSettingBtn.setVisibility(View.GONE);
        listen = root.findViewById(R.id.listen);
        listDevices = root.findViewById(R.id.listDevices);
        listView = root.findViewById(R.id.listview);
        listView.setVisibility(View.GONE);
        bluetoothStatus = root.findViewById(R.id.bluetoothStatus);
        msgStatus = root.findViewById(R.id.msgStatus);
    }
}