package com.cityu_2021_fyp.gesturerecognition_phone.ui.home;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.cityu_2021_fyp.gesturerecognition_phone.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        implementListeners();
        mContext = requireActivity().getApplicationContext();

        return root;
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
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, strings);
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
            bluetoothStatus.setTextColor(0xe0af1f);
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
                    bluetoothStatus.setTextColor(Color.GREEN);
                    listView.setVisibility(View.GONE);
                    msg_box.setText("Waiting for message...");
                    break;
                case STATE_CONNECTION_FAILED:
                    bluetoothStatus.setText("Connection Failed");
                    bluetoothStatus.setTextColor(Color.RED);
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    msgStatus.setTextColor(0xFF74BDDD);
                    msgStatus.setText("Receiving...");
                    if (tempMsg.equalsIgnoreCase("start\n")) {
                        msg_box.setText("");
                    } else if (tempMsg.equalsIgnoreCase("end\n")) {
                        msgStatus.setText("");
                        Toast.makeText(mContext, "Message receive successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        msg_box.append(tempMsg);
                    }
                    break;
            }
            return true;
        }
    });
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
        msg_box = root.findViewById(R.id.msg);
        listen = root.findViewById(R.id.listen);
        listDevices = root.findViewById(R.id.listDevices);
        listView = root.findViewById(R.id.listview);
        listView.setVisibility(View.INVISIBLE);
        bluetoothStatus = root.findViewById(R.id.bluetoothStatus);
        msgStatus = root.findViewById(R.id.msgStatus);
    }
}