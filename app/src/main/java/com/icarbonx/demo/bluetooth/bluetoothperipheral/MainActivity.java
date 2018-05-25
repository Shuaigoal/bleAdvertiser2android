package com.icarbonx.demo.bluetooth.bluetoothperipheral;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.icarbonx.demo.bluetooth.bluetoothperipheral.PeripheralOne.IshowInfo;

public class MainActivity extends AppCompatActivity implements IshowInfo, PeripheralTwo.IshowInfo2 {

    TextView mTvInfo;
    PeripheralOne p1;
//    PeripheralTwo p2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvInfo = (TextView)findViewById(R.id.tvInfo);

//        p1 = new PeripheralOne(this);
//        p1.setListenering(this);
//        p2 = new PeripheralTwo(this);
//        p2.setListenering(this);

        Button btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p1 = new PeripheralOne(getApplicationContext());
                p1.setListenering(MainActivity.this);
                p1.startBroadcast("AAAAABBBBBCCCCCDD");
//                p2.startBroadcast("CCCCCC");
            }
        });

        Button btnStop = (Button)findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p1.stopBroadcast();
//                p2.stopAdvertise();
            }
        });

//        checkBLE();
//
//        settingBleParam();
    }

    @Override
    public void showInfo(final String infomation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuffer sb = new StringBuffer();
                sb.append(mTvInfo.getText().toString());
                sb.append("\n");
                sb.append(infomation);
                mTvInfo.setText(sb.toString());
            }
        });
    }

    @Override
    public void showInfo2(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuffer sb = new StringBuffer();
                sb.append(mTvInfo.getText().toString());
                sb.append("\n");
                sb.append(value);
                mTvInfo.setText(sb.toString());
            }
        });
    }



    /*
    BluetoothManager bluetoothManager = null;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothLeAdvertiser bluetoothLeAdvertiser = null;

    private void checkBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("该设备不支持蓝牙低功耗通讯");
            this.finish();
            return;
        }

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            showToast("该设备不支持蓝牙低功耗通讯");
            this.finish();
            return;
        }

        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (bluetoothLeAdvertiser == null) {
            showToast("该设备不支持蓝牙低功耗从设备通讯");
            this.finish();
            return;
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private void settingBleParam() {
        //广播设置
        AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
        settingBuilder.setConnectable(true);
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingBuilder.setTimeout(0); //我填过别的，但是不能广播。后来我就坚定的0了
        settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings settings = settingBuilder.build();


        //广播参数
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        bluetoothAdapter.setName("ICX-Test11"); //你想叫啥名字，你愿意就好
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.setIncludeTxPowerLevel(true);

        //dataBuilder.addServiceUuid(ParcelUuid.fromString(UUID_SERVICE)); //可自定义UUID，看看官方有没有定义哦
        dataBuilder.addServiceData(ParcelUuid.fromString(UUID_SERVICE), "aaaaaaaa".getBytes());

        AdvertiseData data = dataBuilder.build();

        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
    }


    BluetoothGattServer bluetoothGattServer = null;

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfo("1.1 AdvertiseCallback-onStartSuccess");
                }
            });


            bluetoothGattServer = bluetoothManager.openGattServer(getApplicationContext(),
                    bluetoothGattServerCallback);

            BluetoothGattService service = new BluetoothGattService(UUID.fromString(UUID_SERVICE),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            UUID UUID_CHARREAD = UUID.fromString(UUID_CHARACTERISTIC);

            //特征值读写设置
            BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHARREAD,
                    BluetoothGattCharacteristic.PROPERTY_WRITE |
                            BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

            UUID UUID_DESCRIPTOR = UUID.fromString(UUID_CHARACTERISTIC_CONFIG);

            BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE);
            characteristicWrite.addDescriptor(descriptor);
            service.addCharacteristic(characteristicWrite);

            bluetoothGattServer.addService(service);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfo("1.2. Service Builded ok");
                }
            });

        }
    };

    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);

            final String info = service.getUuid().toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfo("1.3 BluetoothGattServerCallback-onServiceAdded " + info);
                }
            });


        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            final String info = device.getAddress() + "|" + status + "->" + newState;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfo("1.4 onConnectionStateChange " + info);
                }
            });
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);


            final String deviceInfo = "Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + characteristic.getUuid() + "|Value:" +
                    Util.bytes2HexString(characteristic.getValue());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showInfo("=============================================");
                    showInfo("设备信息 " + deviceInfo);
                    showInfo("数据信息 " + info);
                    showInfo("=========onCharacteristicReadRequest=========");

                }
            });

            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic,
                    preparedWrite, responseNeeded, offset, value);

            final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + characteristic.getUuid() + "|Value:" + Util.bytes2HexString(value);


            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            //TODO:你做数据处理


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showInfo("=============================================");
                    showInfo("设备信息 " + deviceInfo);
                    showInfo("数据信息 " + info);
                    showInfo("=========onCharacteristicWriteRequest=========");

                }
            });


        }


        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);

            final String info = "Address:" + device.getAddress() + "|status:" + status;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showInfo("onNotificationSent " + info);
                }
            });
        }


        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + descriptor.getUuid() + "|Value:" + Util.bytes2HexString(value);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showInfo("=============================================");
                    showInfo("设备信息 " + deviceInfo);
                    showInfo("数据信息 " + info);
                    showInfo("=========onDescriptorWriteRequest=========");

                }
            });


            // 告诉连接设备做好了
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {

            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

            final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + descriptor.getUuid();


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showInfo("=============================================");
                    showInfo("设备信息 " + deviceInfo);
                    showInfo("数据信息 " + info);
                    showInfo("=========onDescriptorReadRequest=========");

                }
            });

            // 告诉连接设备做好了
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);


        }

    };*/



}
