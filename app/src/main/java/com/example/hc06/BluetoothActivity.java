package com.example.hc06;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;

    final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    ListView deviceListView;
    ArrayAdapter<String> deviceListAdapter;
    ArrayList<String> deviceList;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // Bluetooth 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_PERMISSION);
        }

        // 위치 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // 기타 초기화 코드


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Bluetooth 활성화
            bluetoothOn();
            // 페어링된 장치 목록 표시
            showPairedDevices();
        }
    }

    // 블루투스 활성화 메서드
// 블루투스 활성화 메서드
    void bluetoothOn() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, 1);
        }
    }


    // 페어링된 장치 목록 표시 메서드
    @SuppressLint("WrongViewCast")
    void showPairedDevices() {
        deviceListView = findViewById(R.id.deviceListView);
        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(deviceListAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
            }
            deviceListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        // ListView의 아이템을 클릭했을 때 연결 시도
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = deviceList.get(position);
            String deviceName = deviceInfo.substring(0, deviceInfo.indexOf("\n")); // 디바이스 이름 추출
            connectDevice(deviceName); // 선택한 디바이스와 연결 시도
        });
    }


    // 블루투스 기기 연결을 위한 메서드
    void connectDevice(String deviceName) {
        if (bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    bluetoothDevice = device;
                    break;
                }
            }
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
                bluetoothSocket.connect();
                sendMessage("Hello from Android!");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "블루투스가 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 메시지 전송 메서드
    void sendMessage(String message) {
        try {
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            outputStream.write(message.getBytes());
            Toast.makeText(this, "메시지 전송 완료", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "메시지 전송 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 블루투스 권한 허용됨
            } else {
                // 블루투스 권한 거부됨
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한 허용됨
            } else {
                // 위치 권한 거부됨
            }
        }
    }

    void listPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> deviceNames = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceNames.add(device.getName());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a device");
            builder.setItems(deviceNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String selectedDeviceName = deviceNames.get(which);
                    connectDevice(selectedDeviceName); // 선택한 디바이스와 연결 시도
                }
            });
            builder.show();
        } else {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
        }
    }



}
