package com.sensoro.libbleserver.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import com.sensoro.libbleserver.ble.callback.ConnectionCallback;
import com.sensoro.libbleserver.ble.callback.WriteCallback;
import com.sensoro.libbleserver.ble.connection.BluetoothLEHelper;
import com.sensoro.libbleserver.ble.connection.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.constants.CmdType;
import com.sensoro.libbleserver.ble.constants.ResultCode;
import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.libbleserver.ble.scanner.SensoroUUID;
import com.sensoro.libbleserver.ble.utils.LogUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by fangping on 2017/4/17.
 */

public class SensoroDeviceSession {
    private static final String TAG = SensoroDeviceSession.class.getSimpleName();
    private Context context;
    private SensoroDevice sensoroDevice;
    private ConnectionCallback callback;
    private boolean isConnected;
    private Handler handler;
    private TimeOutRunnable timeOutRunnable;
    private ConnectTimeOutRunnable connectTimeOutRunnable;
    private BluetoothLEHelper bluetoothLEHelper;
    private String password;
    private Map<Integer, WriteCallback> writeCallbackHashMap;
    private ByteBuffer byteBuffer = null;
    private int buffer_total_length = 0;
    private int buffer_data_length = 0;
    private boolean isBodyData = false;
    private volatile SensoroDeviceConnection mSensoroDeviceConnection;
    private String tempAddress;

    public SensoroDeviceSession(Context context, SensoroDevice sensoroDevice) {
        this.context = context.getApplicationContext();
        handler = new Handler();
        this.sensoroDevice = sensoroDevice;
        bluetoothLEHelper = new BluetoothLEHelper(context);
        writeCallbackHashMap = new HashMap<>();
    }

    public void startSession(String password, final ConnectionCallback callback) {
        this.callback = callback;
        this.password = password;
//        runOnMainThread(new Runnable() {
//            @Override
//            public void run() {
//                if (!bluetoothLEHelper.initialize()) {
//                    callback.onConnectFailed(ResultCode.BLUETOOTH_ERROR);
//                    disconnect();
//                }
//                if (!bluetoothLEHelper.connect(sensoroDevice.getMacAddress(), bluetoothGattCallback)) {
//                    callback.onConnectFailed(ResultCode.INVALID_PARAM);
//                    disconnect();
//                }
//            }
//        });
    }

    public void write(byte[] data, WriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        int data_length = data.length;
        int total_length = data_length + 3;
        byte[] total_data = new byte[total_length];
        byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
        System.arraycopy(length_data, 0, total_data, 0, 2);
        byte[] version_data = SensoroUUID.intToByteArray(1, 1);
        System.arraycopy(version_data, 0, total_data, 2, 1);
        System.arraycopy(data, 0, total_data, 3, data_length);
        int resultCode = bluetoothLEHelper.writeConfiguration(total_data, CmdType.CMD_W_CFG);
        if (resultCode != ResultCode.SUCCESS) {
            writeCallback.onWriteFailure(resultCode);
        }
    }


    private void parseChangeData(BluetoothGattCharacteristic characteristic) {
        if (!isBodyData) {
            isBodyData = true;
            byte value[] = characteristic.getValue();
            //如果value 长度小于20,说明是个完整短包
            byte[] total_data = new byte[2];
            System.arraycopy(value, 0, total_data, 0, total_data.length);
            buffer_total_length = SensoroUUID.bytesToInt(total_data, 0) - 1;//数据包长度
            byte[] data = new byte[value.length - 3];//第一包数据
            System.arraycopy(value, 3, data, 0, data.length);
            byteBuffer = ByteBuffer.allocate(buffer_total_length); //减去version一个字节长度
            byteBuffer.put(data);
            if (buffer_total_length == (value.length - 3)) { //一次性数据包检验
                try {
                    callback.onNotify(data);
                    byteBuffer.clear();
                    isBodyData = false;
                } catch (Exception e) {
                    e.printStackTrace();

                }
            } else {
                buffer_data_length += (value.length - 3);
            }
        } else {
            if (byteBuffer != null) {
                try {
                    byte value[] = characteristic.getValue();
                    byteBuffer.put(value);
                    buffer_data_length += value.length;
                    if (buffer_data_length == buffer_total_length) {
                        final byte array[] = byteBuffer.array();
                        callback.onNotify(array);
                        isBodyData = false;
                        byteBuffer.clear();
                        buffer_data_length = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disconnect() {
        handler.removeCallbacks(connectTimeoutRunnable);
        bluetoothLEHelper.close();
    }

    /**
     * Close the connection of the beacon.
     */
    private boolean close() {
        return bluetoothLEHelper.close();
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            bluetoothLEHelper.bluetoothGatt = gatt;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        gatt.discoverServices();
                    }
                } else {
                    callback.onConnectFailed(ResultCode.BLUETOOTH_ERROR);
                    disconnect();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            bluetoothLEHelper.bluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> gattServiceList = gatt.getServices();
                if (bluetoothLEHelper.checkGattServices(gattServiceList)) {
                    bluetoothLEHelper.listenNotifyChar();

                } else {
                    callback.onConnectFailed(ResultCode.SYSTEM_ERROR);
                    disconnect();
                }
            } else {
                callback.onConnectFailed(ResultCode.SYSTEM_ERROR);
                disconnect();
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            bluetoothLEHelper.bluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                int resultCode = bluetoothLEHelper.requireWritePermission(password);
                if (resultCode != ResultCode.SUCCESS) {
                    callback.onConnectFailed(resultCode);
                    disconnect();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            bluetoothLEHelper.bluetoothGatt = gatt;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            bluetoothLEHelper.bluetoothGatt = gatt;
            LogUtils.logd(TAG, "==>onCharacteristicWrite");
            if (characteristic.getUuid().equals(BluetoothLEHelper.GattInfo.SENSORO_AUTHORIZATION_CHAR_UUID)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    handler.removeCallbacks(connectTimeoutRunnable);
                    callback.onConnectSuccess();
                }
            }
            // flow write
            if (characteristic.getUuid().equals(BluetoothLEHelper.GattInfo.SENSORO_SENSOR_WRITE_UUID)) {
                LogUtils.logd(TAG, "==>onCharacteristicWrite success");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (bluetoothLEHelper.getSendPacketNumber() == bluetoothLEHelper.getWritePackets().size()) {
                        writeCallbackHashMap.get(CmdType.CMD_W_CFG).onWriteSuccess();
                    }
                    bluetoothLEHelper.sendPacket(characteristic);
                } else {
                    LogUtils.logd(TAG, "==>onCharacteristicWrite failure" + status);
                    // failure
                    writeCallbackHashMap.get(CmdType.CMD_W_CFG).onWriteFailure(ResultCode.SYSTEM_ERROR);
                    bluetoothLEHelper.resetSendPacket();
                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            bluetoothLEHelper.bluetoothGatt = gatt;
            LogUtils.logd(TAG, "==>onCharacteristicChanged");
            if (characteristic.getUuid().equals(BluetoothLEHelper.GattInfo.SENSORO_SENSOR_INDICATE_UUID)) {
                parseChangeData(characteristic);
            }
        }
    };


    private Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            callback.onConnectFailed(ResultCode.TASK_TIME_OUT);
            disconnect();
        }
    };

    class TimeOutRunnable implements Runnable {
        @Override
        public void run() {
            if (!isConnected) {
                close();
                callback.onConnectFailed(ResultCode.TASK_TIME_OUT);
                LogUtils.logd(TAG, "TimeOutRunnable---callback connect failure:connect time out");
            }
        }
    }

    class ConnectTimeOutRunnable implements Runnable {
        @Override
        public void run() {
            if (!isConnected) {
                close();
                LogUtils.logd(TAG, "ConnectTimeOutRunnable---callback connect failure:connect time out");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
