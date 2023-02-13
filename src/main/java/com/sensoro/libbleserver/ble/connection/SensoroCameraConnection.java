package com.sensoro.libbleserver.ble.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sensoro.libbleserver.ble.callback.SensoroCameraNetConfigListener;
import com.sensoro.libbleserver.ble.callback.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.constants.CmdType;
import com.sensoro.libbleserver.ble.constants.ResultCode;
import com.sensoro.libbleserver.ble.entity.BLEDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by fangping on 2016/7/25.
 */

public class SensoroCameraConnection {
    private static final String TAG = SensoroCameraConnection.class.getSimpleName();
    private static final long CONNECT_TIME_OUT = 60000; // 60s connect timeout
    private static final long DATA_SEND_TIME_OUT = 30000; //
    private static final long DATA_RECEIVE_TIME_OUT = 60000; //
    private Context context;
    private Handler handler;
    private BLEDevice bleDevice;
    private SensoroConnectionCallback sensoroConnectionCallback;
    private Map<Integer, SensoroWriteCallback> writeCallbackHashMap;
    private String password;
    private BluetoothLEHelper bluetoothLEHelper4;
    private SensoroCameraNetConfigListener mSensoroCameraNetConfigListener;
    private int retryCount = 0;
    private static final int GATT_CONNECT_RETRY_MAX_COUNT = 3;

    public SensoroCameraConnection(Context context, BLEDevice bleDevice) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        this.bleDevice = bleDevice;
        bluetoothLEHelper4 = new BluetoothLEHelper(context);
        writeCallbackHashMap = new HashMap<>();
    }

    private void initData() {
    }


    /**
     * Connect to beacon.
     *
     * @param password           If beacon has no password, set value null.
     * @param connectionCallback The callback of beacon connection.
     * @throws Exception
     */
    public void connect(String password, SensoroConnectionCallback connectionCallback) throws Exception {
        if (context == null) {
            throw new Exception("Context is null");
        }
        if (bleDevice == null) {
            throw new Exception("sensoroDevice is null");
        }
        if (connectionCallback == null) {
            throw new Exception("SensoroConnectionCallback is null");
        }

        if (password != null) {
            this.password = password;
        }

        initData();

        if (bluetoothLEHelper4 != null) {
            bluetoothLEHelper4.close();
        }

        // 开始连接，启动连接超时
        handler.postDelayed(connectTimeoutRunnable, CONNECT_TIME_OUT);

        this.sensoroConnectionCallback = connectionCallback;

       /* runOnMainThread(new Runnable() {
            @Override
            public void run() {*/
        if (!bluetoothLEHelper4.initialize()) {
            sensoroConnectionCallback.onConnectedFailure(ResultCode.BLUETOOTH_ERROR);
            disconnect();
        }

        if (!bluetoothLEHelper4.connect(bleDevice.getMacAddress(), bluetoothGattCallback)) {
            sensoroConnectionCallback.onConnectedFailure(ResultCode.INVALID_PARAM);
            disconnect();
        }
        /*    }
        });*/
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {//连接成功
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    gatt.discoverServices();
                } else {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.BLUETOOTH_ERROR);
                    disconnect();
                }
            } else {
                if (status == 133 && retryCount < GATT_CONNECT_RETRY_MAX_COUNT) {
                    retryCount++;
                    bluetoothLEHelper4.connect(bleDevice.getMacAddress(), bluetoothGattCallback);
                } else {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.BLUETOOTH_ERROR);
                    disconnect();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {//发现服务
                List<BluetoothGattService> gattServiceList = gatt.getServices();
                if (bluetoothLEHelper4.checkGattServices(gattServiceList, BluetoothLEHelper.GattInfo
                        .SENSORO_CAMERA_DEVICE_SERVICE_UUID)) {
                    bluetoothLEHelper4.listenDescriptor(BluetoothLEHelper.GattInfo.SENSORO_CAMERA_WRITE_CHAR_UUID);
                } else {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.SYSTEM_ERROR);
                    disconnect();
                }
            } else {
                sensoroConnectionCallback.onConnectedFailure(ResultCode.SYSTEM_ERROR);
                disconnect();
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (descriptor.getUuid().equals(BluetoothLEHelper.GattInfo.CLIENT_CHARACTERISTIC_CONFIG)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    UUID auth_uuid = BluetoothLEHelper.GattInfo.SENSORO_CAMERA_AUTH_CHAR_UUID;
                    int resultCode = bluetoothLEHelper4.requireWritePermission(password, auth_uuid);
//                    Log.d("SensoroCameraConnection","------发送密码------>"+(resultCode == ResultCode.SUCCESS));
                    if (resultCode != ResultCode.SUCCESS) {
                        sensoroConnectionCallback.onConnectedFailure(resultCode);
                        disconnect();
                    }
                }
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            parseCharacteristicWrite(characteristic, status);

        }

        private void parseCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
            // check pwd
            if (characteristic.getUuid().equals(BluetoothLEHelper.GattInfo.SENSORO_CAMERA_AUTH_CHAR_UUID)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    handler.removeCallbacks(connectTimeoutRunnable);
                    sensoroConnectionCallback.onConnectedSuccess(bleDevice, CmdType.CMD_NULL);
                } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.PASSWORD_ERR);
                    disconnect();
                } else {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.INVALID_PARAM);
                    disconnect();
                }
            }

            if (characteristic.getUuid().equals(BluetoothLEHelper.GattInfo.SENSORO_CAMERA_WRITE_CHAR_UUID)) {
                Log.v(TAG, "onCharacteristicWrite status::" + status);
                Log.v(TAG, "onCharacteristicWrite data::" + characteristic.getValue().length);
                int cmdType = bluetoothLEHelper4.getSendCmdType();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Boolean result = bluetoothLEHelper4.sendPacket(characteristic);
                    if (result) {
                        writeCallbackHashMap.get(CmdType.CMD_CAMERA_NETCONFIG).onWriteSuccess(null, CmdType.CMD_CAMERA_NETCONFIG);
                        handler.postDelayed(dataReceiveTimeoutRunnable, DATA_RECEIVE_TIME_OUT);
                    }
                    handler.removeCallbacks(dataSendTimeoutRunnable);
                } else {
                    bluetoothLEHelper4.resetSendPacket();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            parseChangedData(characteristic);
        }
    };


    private void parseChangedData(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        UUID uuid = characteristic.getUuid();
        if (uuid.equals(BluetoothLEHelper.GattInfo.SENSORO_CAMERA_WRITE_CHAR_UUID)) { //出现先change后read情况
            if (mSensoroCameraNetConfigListener != null) {
                boolean isReceiveComplete = mSensoroCameraNetConfigListener.onReceiveCameraNetConfigResult(data);
                if (isReceiveComplete) {
                    handler.removeCallbacks(dataReceiveTimeoutRunnable);
                }
            }
        }

    }


    public void writeBytes(byte[] totalData, SensoroWriteCallback writeCallback) {
        handler.postDelayed(dataSendTimeoutRunnable, DATA_SEND_TIME_OUT);
        writeCallbackHashMap.put(CmdType.CMD_CAMERA_NETCONFIG, writeCallback);
        int resultCode = bluetoothLEHelper4.writeConfigurations(totalData, CmdType.CMD_CAMERA_NETCONFIG,
                BluetoothLEHelper.GattInfo.SENSORO_CAMERA_WRITE_CHAR_UUID);
        if (resultCode != ResultCode.SUCCESS) {
            writeCallback.onWriteFailure(resultCode, CmdType.CMD_CAMERA_NETCONFIG);
        } else {
            Log.e(TAG, "-----writeBytes 成功--------");
        }
    }

    public void setOnSensoroCameraNetConfigListener(SensoroCameraNetConfigListener listener) {
        mSensoroCameraNetConfigListener = listener;
    }

    /**
     * Disconnect from beacon.
     */
    public void disconnect() {
        handler.removeCallbacks(connectTimeoutRunnable);

        if (bluetoothLEHelper4 != null) {
            bluetoothLEHelper4.close();
        }
        if (sensoroConnectionCallback != null) {
            sensoroConnectionCallback.onDisconnected();
        }

    }

    private Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            sensoroConnectionCallback.onConnectedFailure(ResultCode.TASK_TIME_OUT);
            disconnect();
        }
    };

    private Runnable dataSendTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            SensoroWriteCallback sensoroWriteCallback = writeCallbackHashMap.get(CmdType.CMD_CAMERA_NETCONFIG);
            if (sensoroWriteCallback != null) {
                sensoroWriteCallback.onWriteFailure(ResultCode.TASK_TIME_OUT, CmdType.CMD_CAMERA_NETCONFIG);
            }
        }
    };

    private Runnable dataReceiveTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSensoroCameraNetConfigListener != null) {
                mSensoroCameraNetConfigListener.onReceiveCameraNetConfigFail(ResultCode.TASK_TIME_OUT, CmdType.CMD_CAMERA_NETCONFIG);
            }
        }
    };

}
