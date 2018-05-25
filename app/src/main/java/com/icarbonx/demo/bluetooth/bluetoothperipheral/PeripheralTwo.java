package com.icarbonx.demo.bluetooth.bluetoothperipheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;

import static android.content.Context.BLUETOOTH_SERVICE;

public class PeripheralTwo {
    private BluetoothAdapter mBluetoothAdapter;
    private static final String TAG = "bleperipheral";
    private static final String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private Context mContext;
    public PeripheralTwo(Context context) {
        mContext = context;
    }

    IshowInfo2 mListenering;
    public interface IshowInfo2 {
        public void showInfo2(String value);
    }

    public void setListenering(IshowInfo2 listenering) {
        mListenering = listenering;
    }


    public void startBroadcast(String data) {
        final BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();//判断你的设备到底支持不支持BLE Peripheral。假如此返回值非空，你才可以继续有机会开发
        if (mBluetoothLeAdvertiser == null) {
//            Toast.makeText(this, "不支持BLE Peripheral", Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "不支持BLE Peripheral");
//            finish();
            mListenering.showInfo2("不支持BLE Peripheral");
            return;
        }


        //开启蓝牙广播  一个是广播设置参数，一个是广播数据，还有一个是Callback
        mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 0), createAdvertiseData(data), mAdvertiseCallback);
//        Toast.makeText(Utils.getContext(), "开启广播", Toast.LENGTH_LONG).show();
//        Log.e(TAG, "开启广播");
        mListenering.showInfo2("开启广播");

    }

    public void stopAdvertise() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mBluetoothLeAdvertiser = null;
//            Log.e(TAG, "停止广播");
            mListenering.showInfo2("停止广播");
        }
    }

    /**
     * 初始化蓝牙类
     * AdvertisingSettings.Builder 用于创建AdvertiseSettings
     * AdvertiseSettings中包含三种数据：AdvertiseMode, Advertise TxPowerLevel和AdvertiseType，其测试结果如下：
     * AdvertiseMode:
     * Advertise Mode                           Logcat频率                   检测到的频率
     * ADVERTISE_MODE_LOW_LATENCY          1/1600 milliseconds                1/1068 milliseconds
     * ADVERTISE_MODE_BALANCED             1/400 milliseconds                 1/295 milliseconds
     * ADVERTISE_MODE_LOW_POWER            1/160 milliseconds                 1/142 milliseconds
     */
    private AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        //设置广播的模式,应该是跟功耗相关
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        mBluetoothAdapter.setName("ICX-Hunter");
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        mSettingsbuilder.setConnectable(connectable);
        mSettingsbuilder.setTimeout(timeoutMillis);
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();
        if (mAdvertiseSettings == null) {
//            Toast.makeText(Utils.getContext(), "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
//            Log.e(TAG, "mAdvertiseSettings == null");
            mListenering.showInfo2("mAdvertiseSettings == null");
        }
        return mAdvertiseSettings;
    }

    //设置一下FMP广播数据
    private AdvertiseData createAdvertiseData(String data) {
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        // mDataBuilder.addServiceUuid(ParcelUuid.fromString(HEART_RATE_SERVICE));
        //添加的数据
        mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), data.getBytes());
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        if (mAdvertiseData == null) {
//            Toast.makeText(Utils.getContext(), "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
//            Log.e(TAG, "mAdvertiseSettings == null");
            mListenering.showInfo2("mAdvertiseData == null");
        }
        return mAdvertiseData;
    }



    //回调
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        //成功
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            if (settingsInEffect != null) {
//                Log.d(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode()
//                        + " timeout=" + settingsInEffect.getTimeout());
                mListenering.showInfo2("onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel()
                                + " mode=" + settingsInEffect.getMode() + " timeout=" + settingsInEffect.getTimeout());
            } else {
//                Log.e(TAG, "onStartSuccess, settingInEffect is null");
                mListenering.showInfo2("onStartSuccess, settingInEffect is null");
            }
//            Log.e(TAG, "onStartSuccess settingsInEffect" + settingsInEffect);
            mListenering.showInfo2("onStartSuccess settingsInEffect" + settingsInEffect);
        }

        //失败
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
//            Log.e(TAG, "onStartFailure errorCode" + errorCode);//返回的错误码
            mListenering.showInfo2("onStartFailure errorCode" + errorCode);

            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
//                Toast.makeText(Utils.getContext(), "广播开启错误,数据大于31个字节", Toast.LENGTH_LONG).show();
//                Log.e(TAG, "广播开启错误,数据大于31个字节");
                mListenering.showInfo2("广播开启错误,数据大于31个字节");

            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
//                Toast.makeText(Utils.getContext(), "未能开始广播，没有广播实例", Toast.LENGTH_LONG).show();
//                Log.e(TAG, "未能开始广播，没有广播实例");
                mListenering.showInfo2("未能开始广播，没有广播实例");

            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {

//                Toast.makeText(Utils.getContext(), "正在连接的，无法再次连接", Toast.LENGTH_LONG).show();
//                Log.e(TAG, "正在连接的，无法再次连接");
                mListenering.showInfo2("正在连接的，无法再次连接");

            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {

//                Toast.makeText(Utils.getContext(), "由于内部错误操作失败", Toast.LENGTH_LONG).show();
//                Log.e(TAG, "由于内部错误操作失败");
                mListenering.showInfo2("由于内部错误操作失败");

            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {

//                Toast.makeText(Utils.getContext(), "在这个平台上不支持此功能", Toast.LENGTH_LONG).show();
//                Log.e(TAG, "在这个平台上不支持此功能");
                mListenering.showInfo2("在这个平台上不支持此功能");
            }
        }
    };

}
