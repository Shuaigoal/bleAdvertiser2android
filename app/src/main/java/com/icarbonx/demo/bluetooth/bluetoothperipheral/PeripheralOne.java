package com.icarbonx.demo.bluetooth.bluetoothperipheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.widget.Toast;

import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

public class PeripheralOne {
    public static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARACTERISTIC = "0000000-0000-0000-8000-00805f9b0000";
    public static final String UUID_CHARACTERISTIC_CONFIG = "0000fff3-0000-1000-8000-00805f9b34fb";

    BluetoothManager bluetoothManager = null;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothLeAdvertiser bluetoothLeAdvertiser = null;
    BluetoothGattServer bluetoothGattServer = null;

    private Context mContext;
    public PeripheralOne(Context context) {
        this.mContext = context;

        checkBLE();

    }

    public void startBroadcast(String data) {
        settingBleParam(data);
    }

    public void stopBroadcast() {
        if (bluetoothLeAdvertiser != null) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            bluetoothLeAdvertiser = null;
            mListenering.showInfo("停止广播");
        }
    }

    IshowInfo mListenering;
    public interface IshowInfo {
        public void showInfo(String infomation);
    }

    public void setListenering(IshowInfo listenering) {
        mListenering = listenering;
    }

    /**
     * 设置广播参数并发送数据
     * @param data
     */
    private void settingBleParam(String data) {
        //广播设置
        AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
        settingBuilder.setConnectable(true);
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED); //设置广播的模式，低功耗，平衡和低延迟三种模式
        settingBuilder.setTimeout(0); //我填过别的，但是不能广播。后来我就坚定的0了
        settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);  //设置广播的信号强度
        AdvertiseSettings settings = settingBuilder.build();


        //广播参数
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        bluetoothAdapter.setName("I1"); //你想叫啥名字，你愿意就好
        dataBuilder.setIncludeDeviceName(true);     //是否广播设备名称
        dataBuilder.setIncludeTxPowerLevel(true);   //是否广播信号强度

        //dataBuilder.addServiceUuid(ParcelUuid.fromString(UUID_SERVICE)); //可自定义UUID
        dataBuilder.addServiceData(ParcelUuid.fromString(UUID_SERVICE), data.getBytes());

        AdvertiseData datas = dataBuilder.build();

        bluetoothLeAdvertiser.startAdvertising(settings, datas, advertiseCallback);
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);

            mListenering.showInfo("1.1 AdvertiseCallback-onStartSuccess");


            bluetoothGattServer = bluetoothManager.openGattServer(mContext,
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

            mListenering.showInfo("1.2. Service Builded ok");

        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            mListenering.showInfo("onStartFailure errorCode" + errorCode);

            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {

                mListenering.showInfo("广播开启错误,数据大于31个字节");

            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {

                mListenering.showInfo("未能开始广播，没有广播实例");

            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {

                mListenering.showInfo("正在连接的，无法再次连接");

            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {

                mListenering.showInfo("由于内部错误操作失败");

            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {

                mListenering.showInfo("在这个平台上不支持此功能");
            }
        }
    };

    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);

            final String info = service.getUuid().toString();

            mListenering.showInfo("1.3 BluetoothGattServerCallback-onServiceAdded " + info);


        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            final String info = device.getAddress() + "|" + status + "->" + newState;

            mListenering.showInfo("1.4 onConnectionStateChange " + info);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);


            final String deviceInfo = "Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + characteristic.getUuid() + "|Value:" +
                    Util.bytes2HexString(characteristic.getValue());

            mListenering.showInfo("=============================================");
            mListenering.showInfo("设备信息 " + deviceInfo);
            mListenering.showInfo("数据信息 " + info);
            mListenering.showInfo("=========onCharacteristicReadRequest=========");

            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic,
                    preparedWrite, responseNeeded, offset, value);

            final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + characteristic.getUuid() + "|Value:" + Util.bytes2HexString(value);


            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

            mListenering.showInfo("=============================================");
            mListenering.showInfo("设备信息 " + deviceInfo);
            mListenering.showInfo("数据信息 " + info);
            mListenering.showInfo("=========onCharacteristicWriteRequest=========");


        }


        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);

            final String info = "Address:" + device.getAddress() + "|status:" + status;

            mListenering.showInfo("onNotificationSent " + info);
        }


        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + descriptor.getUuid() + "|Value:" + Util.bytes2HexString(value);

            mListenering.showInfo("=============================================");
            mListenering.showInfo("设备信息 " + deviceInfo);
            mListenering.showInfo("数据信息 " + info);
            mListenering.showInfo("=========onDescriptorWriteRequest=========");


            // 告诉连接设备做好了
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {

            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

            final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
            final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + descriptor.getUuid();

            mListenering.showInfo("=============================================");
            mListenering.showInfo("设备信息 " + deviceInfo);
            mListenering.showInfo("数据信息 " + info);
            mListenering.showInfo("=========onDescriptorReadRequest=========");

            // 告诉连接设备做好了
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);


        }

    };

    private void showToast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    private void checkBLE() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("该设备不支持蓝牙低功耗通讯");
            return;
        }

        bluetoothManager = (BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            showToast("该设备不支持蓝牙低功耗通讯");
            return;
        }

        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (bluetoothLeAdvertiser == null) {
            showToast("该设备不支持蓝牙低功耗从设备通讯");
            return;
        }
    }





}
