package com.sensoro.libbleserver.ble.connection;

import android.app.Activity;
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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sensoro.libbleserver.ble.utils.SensoroUtils;
import com.sensoro.libbleserver.ble.callback.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.callback.SensoroDirectWriteDfuCallBack;
import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.entity.SensoroAcrelFires;
import com.sensoro.libbleserver.ble.entity.SensoroBaymax;
import com.sensoro.libbleserver.ble.entity.SensoroCayManData;
import com.sensoro.libbleserver.ble.entity.SensoroChannel;
import com.sensoro.libbleserver.ble.constants.CmdType;
import com.sensoro.libbleserver.ble.constants.ResultCode;
import com.sensoro.libbleserver.ble.entity.BLEDevice;
import com.sensoro.libbleserver.ble.entity.SensoroData;
import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.libbleserver.ble.entity.SensoroFireData;
import com.sensoro.libbleserver.ble.entity.SensoroMantunData;
import com.sensoro.libbleserver.ble.entity.SensoroSensor;
import com.sensoro.libbleserver.ble.entity.SensoroSensorConfiguration;
import com.sensoro.libbleserver.ble.entity.SensoroSlot;
import com.sensoro.libbleserver.ble.proto.MsgNode1V1M5;
import com.sensoro.libbleserver.ble.scanner.SensoroUUID;
import com.sensoro.libbleserver.ble.proto.ProtoMsgCfgV1U1;
import com.sensoro.libbleserver.ble.proto.ProtoMsgTest1U1;
import com.sensoro.libbleserver.ble.proto.ProtoStd1U1;
import com.sensoro.libbleserver.ble.chipeupgrade.ChipEUpgradeThread;
import com.sensoro.libbleserver.ble.utils.LogUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by fangping on 2016/7/25.
 */

public class SensoroDeviceConnection {
    public static final byte DATA_VERSION_03 = 0x03;
    public static final byte DATA_VERSION_04 = 0x04;
    public static final byte DATA_VERSION_05 = 0x05;
    private static final String TAG = SensoroDeviceConnection.class.getSimpleName();
    private static final long CONNECT_TIME_OUT = 8 * 1000; // 1 minute connect timeout
    private boolean isChipE = false;
    private boolean isDfu = false;
    private Context context;
    private final Handler taskHandler = new Handler(Looper.getMainLooper());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SensoroConnectionCallback sensoroConnectionCallback;
    private Map<Integer, SensoroWriteCallback> writeCallbackHashMap;
    private boolean isBodyData = false;
    private String password;
    private BluetoothLEHelper4 bluetoothLEHelper4;
    private ListenType listenType = ListenType.UNKNOWN;
    private ByteBuffer byteBuffer = null;
    private ByteBuffer signalByteBuffer = null;
    private ByteBuffer tempBuffer = null;
    private int buffer_total_length = 0;
    private int buffer_data_length = 0;
    private int signalBuffer_total_length = 0;
    private int signalBuffer_data_length = 0;
    private byte dataVersion = DATA_VERSION_03;
    private boolean isContainSignal;
    private String macAddress;
    public int count;
    private final Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            reConnectDevice(ResultCode.TASK_TIME_OUT, "连接超时");
        }
    };

    private void runOnMainThread(Runnable run) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            run.run();
        } else {
            mainHandler.post(run);
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            bluetoothLEHelper4.bluetoothGatt = gatt;
            LogUtils.loge("连接状态改变");
            if (newState == BluetoothProfile.STATE_CONNECTED) {//连接成功
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    taskHandler.removeCallbacks(connectTimeoutRunnable);
                    LogUtils.loge("连接成功了");
                    if (sensoroDirectWriteDfuCallBack != null && isDfu) {
                        LogUtils.loge("可以直接升级");
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                sensoroDirectWriteDfuCallBack.OnDirectWriteDfuCallBack();
                            }
                        });
                        return;
                    }
                    trySleepThread(50);
                    gatt.discoverServices();
                    count = 0;
                } else {
                    LogUtils.loge("连接状态connected 没有成功");
                    if (count < 3) {
                        reConnectDevice(ResultCode.BLUETOOTH_ERROR, "连接失败 bluetoothgatt");
                    } else {
                        count = 0;
                    }


                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (count < 3) {
                    reConnectDevice(ResultCode.BLUETOOTH_ERROR, "连接失败 disconnect");
                } else {
                    count = 0;
                }
                LogUtils.loge("连接失败 disconnect");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            LogUtils.loge("发现服务了");
            bluetoothLEHelper4.bluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {//发现服务
                List<BluetoothGattService> gattServiceList = gatt.getServices();

                if (bluetoothLEHelper4.checkGattServices(gattServiceList, BluetoothLEHelper4.GattInfo
                        .SENSORO_DEVICE_SERVICE_UUID)) {
                    trySleepThread(10);
                    if (isChipE) {
                        if (bluetoothLEHelper4.initChipEServices(gattServiceList)) {
                            LogUtils.loge("初始化chipe服务成功");
                            listenType = ListenType.SENSOR_CHIP_E;
                        } else {
                            LogUtils.loge("初始化chipe服务失败");
                            listenType = ListenType.UNKNOWN;
                            disconnect();
                        }
                    } else if (!isContainSignal) {
                        listenType = ListenType.READ_CHAR;
                        bluetoothLEHelper4.listenDescriptor(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_READ_CHAR_UUID);
                    } else {
                        listenType = ListenType.SIGNAL_CHAR;
                        bluetoothLEHelper4.listenSignalChar(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_SIGNAL_UUID);
                    }
                } else {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            sensoroConnectionCallback.onConnectedFailure(ResultCode.SYSTEM_ERROR);
                            LogUtils.loge("不能升级");
                            freshCache();
                        }
                    });
                }
            } else {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        sensoroConnectionCallback.onConnectedFailure(ResultCode.SYSTEM_ERROR);
                        freshCache();
                        LogUtils.loge("服务校验失败");
                    }
                });

            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            bluetoothLEHelper4.bluetoothGatt = gatt;
            if (descriptor.getUuid().equals(BluetoothLEHelper4.GattInfo.CLIENT_CHARACTERISTIC_CONFIG)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    switch (listenType) {
                        case SIGNAL_CHAR:
                            // 监听读特征成功
//                            listenType = ListenType.READ_CHAR;
//                            bluetoothLEHelper4.listenDescriptor(BluetoothLEHelper4.GattInfo
//                                    .SENSORO_DEVICE_READ_CHAR_UUID);
                            UUID auth_uu = BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_AUTHORIZATION_CHAR_UUID;
                            final int resultC = bluetoothLEHelper4.requireWritePermission(password, auth_uu);
                            if (resultC != ResultCode.SUCCESS) {
                                runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sensoroConnectionCallback.onConnectedFailure(resultC);
                                        LogUtils.loge("写密码失败");
                                        freshCache();
                                    }
                                });

                            }
                            break;
                        case READ_CHAR:
                            UUID auth_uuid = BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_AUTHORIZATION_CHAR_UUID;
                            LogUtils.loge("密码是" + password);
                            final int resultCode = bluetoothLEHelper4.requireWritePermission(password, auth_uuid);
                            if (resultCode != ResultCode.SUCCESS) {
                                runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sensoroConnectionCallback.onConnectedFailure(resultCode);
                                        LogUtils.loge("写密码失败");
                                        freshCache();
                                    }
                                });

                            }
                            break;
                        case SENSOR_CHIP_E:
                            //chipe_升级 只是这种情况下，bledevice 回传null
                            LogUtils.loge("onDescriptorWrite chip_e");
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    sensoroConnectionCallback.onConnectedSuccess(null, 0);
                                }
                            });
                            break;
                        default:
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    sensoroConnectionCallback.onConnectedFailure(ResultCode.SYSTEM_ERROR);
                                    LogUtils.loge("写失败");
                                    freshCache();
                                }
                            });
                            break;
                    }
                }
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            bluetoothLEHelper4.bluetoothGatt = gatt;
            LogUtils.loge("onCharacteristicWrite");
            parseCharacteristicWrite(characteristic, status);

        }

        private void parseCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
            // check pwd
            if (isChipE) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    chipEUpgradeThread.setGATTWriteComplete();
                } else {
                    LogUtils.loge("chipe write 失败");
                }

            }
            if (characteristic.getUuid().equals(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_AUTHORIZATION_CHAR_UUID)) {
                switch (status) {
                    case BluetoothGatt.GATT_SUCCESS:
                        if (isContainSignal) {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    sensoroConnectionCallback.onConnectedSuccess(null, CmdType.CMD_NULL);
                                }
                            });

                        } else {
                            bluetoothLEHelper4.listenOnCharactertisticRead(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_READ_CHAR_UUID);
                        }
                        break;
                    case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                sensoroConnectionCallback.onConnectedFailure(ResultCode.PASSWORD_ERR);
                                LogUtils.loge("parseCharacteristicWrite，密码错误");
                                freshCache();
                            }
                        });
                        break;
                    default:
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                sensoroConnectionCallback.onConnectedFailure(ResultCode.INVALID_PARAM);
                                LogUtils.loge("不可用参数");
                                freshCache();
                            }
                        });
                        break;
                }
            }

            // flow write
            if (characteristic.getUuid().equals(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID)) {
                Log.v(TAG, "onCharacteristicWrite success");
                final int cmdType = bluetoothLEHelper4.getSendCmdType();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    bluetoothLEHelper4.sendPacket(characteristic);
                    LogUtils.loge("onCharacteristicWrite char sendPacket");
                } else {
                    Log.v(TAG, "onCharacteristicWrite failure" + status);
                    // failure
                    switch (cmdType) {
                        case CmdType.CMD_R_CFG:
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    sensoroConnectionCallback.onConnectedFailure(ResultCode.SYSTEM_ERROR);
                                    LogUtils.loge("parseCharacteristicWrite cmd_cfg");
                                    freshCache();
                                }
                            });
                            break;
                        case CmdType.CMD_W_CFG:
                            LogUtils.loge("parseCharacteristicWrite CMD_W_CFG");
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteFailure(ResultCode.SYSTEM_ERROR, CmdType.CMD_NULL);
                                }
                            });
                            break;
                        default:
                            break;
                    }
                    bluetoothLEHelper4.resetSendPacket();
                }
            }
            if (characteristic.getUuid().equals(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_SIGNAL_UUID)) {
                Log.v(TAG, "onCharacteristicWrite success");
                final int cmdType = bluetoothLEHelper4.getSendCmdType();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    bluetoothLEHelper4.sendPacket(characteristic);
                    LogUtils.loge("onCharacteristicWrite single sendPacket");

                } else {
                    Log.v(TAG, "onCharacteristicWrite failure" + status);
                    // failure
                    switch (cmdType) {
                        case CmdType.CMD_R_CFG:
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    sensoroConnectionCallback.onConnectedFailure(ResultCode.SYSTEM_ERROR);
                                    LogUtils.loge("没有成功 99999");
                                    freshCache();
                                }
                            });
                            break;
                        case CmdType.CMD_W_CFG:
                            LogUtils.loge("没有成功 666");
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteFailure(ResultCode.SYSTEM_ERROR, CmdType.CMD_NULL);
                                }
                            });
                            break;
                        default:
                            break;
                    }
                    bluetoothLEHelper4.resetSendPacket();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            bluetoothLEHelper4.bluetoothGatt = gatt;
            LogUtils.loge("onCharacteristicRead");
            parseCharacteristicRead(characteristic, status);
        }

        private void parseCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
            if (isChipE) {
                chipEUpgradeThread.otaCmdResponse(characteristic.getValue());
            } else if (characteristic.getUuid().equals(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_READ_CHAR_UUID)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    byte value[] = characteristic.getValue();
                    //如果value 长度小于20,说明是个完整短包
                    LogUtils.loge("read GATT_SUCCESS");
                    byte[] total_data = new byte[2];
                    System.arraycopy(value, 0, total_data, 0, total_data.length);
                    buffer_total_length = SensoroUUID.bytesToInt(total_data, 0) - 1;//数据包长度
                    byte[] version_data = new byte[1];
                    System.arraycopy(value, 2, version_data, 0, version_data.length);
                    dataVersion = version_data[0];
                    LogUtils.loge("version = " + dataVersion);
                    byteBuffer = ByteBuffer.allocate(buffer_total_length); //减去version一个字节长度
                    byte[] data = new byte[value.length - 3];//第一包数据
                    System.arraycopy(value, 3, data, 0, data.length);
                    byteBuffer.put(data);
                    if (buffer_total_length <= (value.length - 3)) { //一次性数据包
                        LogUtils.loge("一包数据");
                        try {
                            byte[] final_data = byteBuffer.array();
                            parseData(final_data);
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    sensoroConnectionCallback.onConnectedFailure(ResultCode.PARSE_ERROR);
                                    LogUtils.loge("parseCharacteristicRead onConnectedFailure");
                                }
                            });

                        } finally {
                            taskHandler.removeCallbacks(connectTimeoutRunnable);
                        }
                    } else {
                        //多包数据
                        LogUtils.loge("多包数据");
                        if (tempBuffer != null) {//先出现change再出现read情况
                            byteBuffer.put(tempBuffer.array());
                            tempBuffer.clear();
                            tempBuffer = null;
                        }
                        buffer_data_length += (value.length - 3);
                    }
                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            bluetoothLEHelper4.bluetoothGatt = gatt;
            try {
                LogUtils.loge("onCharacteristicChanged");
                byte[] value = characteristic.getValue();
                StringBuilder s = new StringBuilder();
                for (byte aValue : value) {
                    s.append(String.format("%02x", aValue));
                }
                LogUtils.loge("onCharacteristicChanged :" + s);
                parseChangedData(characteristic);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    };
    private SensoroDirectWriteDfuCallBack sensoroDirectWriteDfuCallBack;
    private ChipEUpgradeThread chipEUpgradeThread;

    private void reConnectDevice(final int resultCode, final String msg) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                freshCache();
            }
        });
        if (count < 3) {
            count++;
            LogUtils.loge("count:::" + count);
            taskHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectDevice();
                }
            }, 500);
        } else {
            taskHandler.removeCallbacksAndMessages(connectTimeoutRunnable);
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    sensoroConnectionCallback.onConnectedFailure(resultCode);
                    LogUtils.loge(msg);
                }
            });
        }
    }

    private SensoroDeviceConnection(Context context, BLEDevice bleDevice) {
        this.context = context;
        bluetoothLEHelper4 = new BluetoothLEHelper4(context);
        writeCallbackHashMap = new HashMap<>();
        this.isContainSignal = false;
    }

    public SensoroDeviceConnection(Context context, String macAddress) {
        this.context = context;
        bluetoothLEHelper4 = new BluetoothLEHelper4(context);
        writeCallbackHashMap = new HashMap<>();
        this.isContainSignal = false;
        this.macAddress = macAddress;
    }

    public SensoroDeviceConnection(Activity mActivity, String macAddress, boolean isContainSignal, boolean isDfu, boolean isChipE) {
        this(mActivity, macAddress, isContainSignal, isDfu);
        this.isChipE = isChipE;
    }

    public SensoroDeviceConnection(Context context, String macAddress, boolean isContainSignal, boolean isDfu) {
        this.context = context;
        bluetoothLEHelper4 = new BluetoothLEHelper4(context);
        writeCallbackHashMap = new HashMap<>();
        this.macAddress = macAddress;
        this.isContainSignal = isContainSignal;
        this.isDfu = isDfu;
    }


    public SensoroDeviceConnection(Context context, String macAddress, boolean isContainSignal) {
        this.context = context;
        bluetoothLEHelper4 = new BluetoothLEHelper4(context);
        writeCallbackHashMap = new HashMap<>();
        this.isContainSignal = isContainSignal;
        this.macAddress = macAddress;
    }

    private void initData() {
        buffer_data_length = 0;
        buffer_total_length = 0;
    }

    /**
     * Connect to beacon.
     *
     * @param password                  If beacon has no password, set value null.
     * @param sensoroConnectionCallback The callback of beacon connection.
     * @throws Exception
     */
    public void connect(String password, final SensoroConnectionCallback sensoroConnectionCallback) throws Exception {
        if (context == null) {
            throw new Exception("Context is null");
        }
        if (sensoroConnectionCallback == null) {
            throw new Exception("SensoroConnectionCallback is null");
        }
        LogUtils.loge("赋值密码" + password);
        if (password != null) {
            this.password = password;
        }

        initData();
        // 开始连接，启动连接超时
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (bluetoothLEHelper4 != null) {
                    bluetoothLEHelper4.close();
                }
            }
        });
        this.sensoroConnectionCallback = sensoroConnectionCallback;

        if (bluetoothLEHelper4 != null) {
            if (!bluetoothLEHelper4.initialize()) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        sensoroConnectionCallback.onConnectedFailure(ResultCode.BLUETOOTH_ERROR);
                        LogUtils.loge("初始化失败");
                        freshCache();
                    }
                });

            } else {
                connectDevice(sensoroConnectionCallback);
            }
        }


    }

    private void connectDevice(final SensoroConnectionCallback sensoroConnectionCallback) {
        taskHandler.postDelayed(connectTimeoutRunnable, CONNECT_TIME_OUT);
        LogUtils.loge("连接超时发送222");
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (!bluetoothLEHelper4.connect(macAddress, bluetoothGattCallback)) {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.INVALID_PARAM);
                    LogUtils.loge("连接失败");
                    freshCache();
                }
            }
        });
    }

    private void connectDevice() {
        taskHandler.postDelayed(connectTimeoutRunnable, CONNECT_TIME_OUT);
        LogUtils.loge("超时连接发送 333");
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (!bluetoothLEHelper4.connect(macAddress, bluetoothGattCallback)) {
                    LogUtils.loge("连接失败 无参数");
                    freshCache();
                }
            }
        });
    }

    private void parseChangedData(BluetoothGattCharacteristic characteristic) throws InvalidProtocolBufferException {
        byte[] data = characteristic.getValue();
        if (isChipE) {
            chipEUpgradeThread.otaCmdResponse(data);
        } else {
            UUID uuid = characteristic.getUuid();
            if (uuid.equals(BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_READ_CHAR_UUID)) { //出现先change后read情况
                if (byteBuffer == null) {
                    buffer_data_length += data.length;
                    tempBuffer = ByteBuffer.allocate(data.length);
                    tempBuffer.put(data);
                }
            }
            final int cmdType = bluetoothLEHelper4.getSendCmdType();
            LogUtils.loge("parseChangedData cmdType" + cmdType + " 大小 " + data.length);
            switch (cmdType) {
                case CmdType.CMD_SET_ELEC_CMD:
                    parseElectData(characteristic);
                    break;
                case CmdType.CMD_SET_SMOKE:
                    parseSmokeData(characteristic);
                    break;
                case CmdType.CMD_SIGNAL:
                    parseSignalData(characteristic);
                    break;
                case CmdType.CMD_SET_ZERO:
                    if (data.length >= 4) {
                        final byte retCode = data[3];
                        if (retCode == ResultCode.CODE_DEVICE_SUCCESS) {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteSuccess(null, CmdType.CMD_SET_ZERO);
                                }
                            });
                        } else {
                            LogUtils.loge("CMD_SET_ZERO失败");
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteFailure(retCode, CmdType.CMD_SET_ZERO);
                                }
                            });

                        }
                    }
                    break;
                case CmdType.CMD_W_CFG:
                    if (data.length >= 4) {
                        final byte retCode = data[3];
                        if (retCode == ResultCode.CODE_DEVICE_SUCCESS) {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteSuccess(null, CmdType.CMD_NULL);
                                }
                            });
                        } else {
                            LogUtils.loge("CMD_W_CFG 失败");
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteFailure(retCode, CmdType.CMD_NULL);
                                }
                            });

                        }
                    }
                    break;
                case CmdType.CMD_R_CFG:
                    //格式: length + version + retCode, 当数据为多包的情况下,onCharacteristicRead接收的第一个包数据不完整,因此,
                    // onCharacteristicChanged会不断被接收到数据,直到每次接收到的数据累加等于length
                    //多包的情况下,可将第一次包的数据放到BufferByte里
                    //数据是否写入成功
                    LogUtils.loge("CMD_R_CFG");
                    if (byteBuffer != null) {
                        try {
                            byteBuffer.put(data);
                            buffer_data_length += data.length;
                            if (buffer_data_length == buffer_total_length) {
                                LogUtils.loge("CMD_R_CFG 解析");
                                byte array[] = byteBuffer.array();
                                parseData(array);
                                taskHandler.removeCallbacks(connectTimeoutRunnable);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtils.loge("数据写入 catch");
                                    freshCache();
                                    sensoroConnectionCallback.onConnectedFailure(ResultCode.PARSE_ERROR);
                                }
                            });

                        }
                    }
                    break;
                case CmdType.CMD_SET_BAYMAX_CMD:
                    LogUtils.loge("进入baymax");
                    if (data.length >= 4) {
                        LogUtils.loge("进来了");
                        final byte retCode = data[3];
                        if (retCode == ResultCode.CODE_DEVICE_SUCCESS) {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteSuccess(null, CmdType.CMD_SET_BAYMAX_CMD);
                                }
                            });
                        } else {
                            LogUtils.loge("CMD_SET_ZERO失败");
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(cmdType).onWriteFailure(retCode, CmdType.CMD_SET_BAYMAX_CMD);
                                }
                            });

                        }
                    }
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 解析电表命令返回
     *
     * @param characteristic
     */
    private void parseElectData(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        final byte retCode = data[3];
        if (retCode == ResultCode.CODE_DEVICE_SUCCESS) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallbackHashMap.get(CmdType.CMD_SET_ELEC_CMD).onWriteSuccess(null, CmdType.CMD_SET_ELEC_CMD);
                }
            });

        } else {
            LogUtils.loge("解析电表命令失败");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallbackHashMap.get(CmdType.CMD_SET_ELEC_CMD).onWriteFailure(retCode, CmdType.CMD_SET_ELEC_CMD);
                }
            });

        }
    }

    private void parseSmokeData(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        final byte retCode = data[3];
        if (retCode == ResultCode.CODE_DEVICE_SUCCESS) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallbackHashMap.get(CmdType.CMD_SET_SMOKE).onWriteSuccess(null, CmdType.CMD_SET_SMOKE);
                }
            });

        } else {
            LogUtils.loge("解析烟感失败");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallbackHashMap.get(CmdType.CMD_SET_SMOKE).onWriteFailure(retCode, CmdType.CMD_SET_SMOKE);
                }
            });

        }
    }

    private void parseSignalData(BluetoothGattCharacteristic characteristic) {
        if (!isBodyData) {
            LogUtils.loge("信号数据解析");
            isBodyData = true;
            byte value[] = characteristic.getValue();
            StringBuilder s = new StringBuilder();
            for (byte aValue : value) {
                s.append(String.format("%x", aValue));
            }
            LogUtils.loge("信号数据" + s);
            //如果value 长度小于20,说明是个完整短包
            byte[] total_data = new byte[2];
            System.arraycopy(value, 0, total_data, 0, total_data.length);
            signalBuffer_total_length = SensoroUUID.bytesToInt(total_data, 0) - 1;//数据包长度
            LogUtils.loge("signalBuffer_total_length" + signalBuffer_total_length);
            byte[] data = new byte[value.length - 3];//第一包数据
            System.arraycopy(value, 3, data, 0, data.length);
            signalByteBuffer = ByteBuffer.allocate(signalBuffer_total_length); //减去version一个字节长度
            signalByteBuffer.put(data);
            if (signalBuffer_total_length == (value.length - 3)) { //一次性数据包检验
                try {
                    final ProtoMsgTest1U1.MsgTest msgCfg = ProtoMsgTest1U1.MsgTest.parseFrom(data);
                    LogUtils.loge("packetNUm:::" + msgCfg.getPacketNumber());
                    if (msgCfg.hasRetCode()) {
                        if (msgCfg.getRetCode() == 0) {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(CmdType.CMD_SIGNAL).onWriteSuccess(null, CmdType.CMD_SIGNAL);
                                }
                            });
                            LogUtils.loge("信号数据成功");
                            //指令发送成功,可以正常接收数据
                        } else {
                            LogUtils.loge("信号失败");
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    writeCallbackHashMap.get(CmdType.CMD_SIGNAL).onWriteFailure(0, CmdType.CMD_SIGNAL);
                                }
                            });

                        }
                    } else {
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                writeCallbackHashMap.get(CmdType.CMD_SIGNAL).onWriteSuccess(msgCfg, CmdType.CMD_SIGNAL);
                            }
                        });
                    }


                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    StringBuilder d = new StringBuilder();
                    for (byte datum : data) {
                        d.append(String.format("%x" + datum));
                    }
                    LogUtils.loge("信号catch" + d);
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallbackHashMap.get(CmdType.CMD_SIGNAL).onWriteFailure(0, CmdType.CMD_SIGNAL);
                            freshCache();
                        }
                    });
                    LogUtils.loge("parseSignalData catch");
                } finally {
                    signalByteBuffer.clear();
                    isBodyData = false;
                }
            } else {
                signalBuffer_data_length += (value.length - 3);
                LogUtils.loge("信号数据长度不对");
            }
        } else {
            if (signalByteBuffer != null) {
                try {
                    byte value[] = characteristic.getValue();
                    signalByteBuffer.put(value);
                    LogUtils.loge("拼接信号数据");
                    signalBuffer_data_length += value.length;
                    LogUtils.loge("signalBuffer_data_length = " + signalBuffer_data_length);
                    if (signalBuffer_data_length == signalBuffer_total_length) {
                        final byte array[] = signalByteBuffer.array();
                        final ProtoMsgTest1U1.MsgTest msgCfg = ProtoMsgTest1U1.MsgTest.parseFrom(array);
                        LogUtils.loge("packetNUm:::" + msgCfg.getPacketNumber());
                        isBodyData = false;
                        signalByteBuffer.clear();
                        signalBuffer_data_length = 0;
                        LogUtils.loge("信号数据清空");
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                writeCallbackHashMap.get(CmdType.CMD_SIGNAL).onWriteSuccess(msgCfg, CmdType.CMD_SIGNAL);
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.loge("数据校验失败");
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            freshCache();
                            writeCallbackHashMap.get(CmdType.CMD_SIGNAL).onWriteFailure(ResultCode.PARSE_ERROR, CmdType
                                    .CMD_SIGNAL);
                        }
                    });

                }
            }
        }
    }

    private void parseData03(byte[] data) {
        final SensoroDevice sensoroDevice = new SensoroDevice();
        try {
            ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = ProtoMsgCfgV1U1.MsgCfgV1u1.parseFrom(data);
            if (msgCfg.getSlotList().size() > 4) {
                byte ibeacon_data[] = msgCfg.getSlot(4).getFrame().toByteArray();
                byte uuid_data[] = new byte[16];
                System.arraycopy(ibeacon_data, 0, uuid_data, 0, 16);
                byte major_data[] = new byte[2];
                System.arraycopy(ibeacon_data, 16, major_data, 0, 2);
                byte minor_data[] = new byte[2];
                System.arraycopy(ibeacon_data, 18, minor_data, 0, 2);
                sensoroDevice.setProximityUUID(SensoroUtils.bytesToHexString(uuid_data));
                sensoroDevice.setMajor(SensoroUUID.byteArrayToInt(major_data));
                sensoroDevice.setMinor(SensoroUUID.byteArrayToInt(minor_data));
            }
            sensoroDevice.setAppEui(SensoroUtils.bytesToHexString(msgCfg.getAppEui().toByteArray()));
            sensoroDevice.setDevEui(SensoroUtils.bytesToHexString(msgCfg.getDevEui().toByteArray()));
            sensoroDevice.setAppKey(SensoroUtils.bytesToHexString(msgCfg.getAppKey().toByteArray()));
            sensoroDevice.setAppSkey(SensoroUtils.bytesToHexString(msgCfg.getAppSkey().toByteArray()));
            sensoroDevice.setNwkSkey(SensoroUtils.bytesToHexString(msgCfg.getNwkSkey().toByteArray()));
            sensoroDevice.setBleInt(msgCfg.getBleInt());
            sensoroDevice.setBleOnTime(msgCfg.getBleOnTime());
            sensoroDevice.setBleOffTime(msgCfg.getBleOffTime());
            sensoroDevice.setBleTxp(msgCfg.getBleTxp());
            sensoroDevice.setLoraInt(msgCfg.getLoraInt());
            sensoroDevice.setLoraDr(msgCfg.getLoraDr());
            sensoroDevice.setLoraAdr(msgCfg.getLoraAdr());
            sensoroDevice.setLoraTxp(msgCfg.getLoraTxp());
            sensoroDevice.setDevAdr(msgCfg.getDevAddr());
            sensoroDevice.setLoraAdr(msgCfg.getLoraAdr());
            sensoroDevice.setTempInterval(msgCfg.getTempInt());
            sensoroDevice.setLightInterval(msgCfg.getLightInt());
            sensoroDevice.setHumidityInterval(msgCfg.getHumiInt());
            List<ProtoMsgCfgV1U1.Slot> slotList = msgCfg.getSlotList();
            int slot_size = slotList.size();
            SensoroSlot sensoroSlotArray[] = new SensoroSlot[slot_size];
            for (int i = 0; i < slot_size; i++) {
                SensoroSlot sensoroSlot = new SensoroSlot();
                ProtoMsgCfgV1U1.Slot slot = slotList.get(i);
                sensoroSlot.setActived(slot.getActived());
                sensoroSlot.setIndex(slot.getIndex());
                sensoroSlot.setType(slot.getType().getNumber());
                if (slot.getType() == ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL) {
                    byte[] url_data = slot.getFrame().toByteArray();

                    sensoroSlot.setFrame(SensoroUtils.decodeUrl(url_data));
                } else {
                    sensoroSlot.setFrame(SensoroUtils.bytesToHexString(slot.getFrame().toByteArray()));
                }

                sensoroSlotArray[i] = sensoroSlot;
            }
            sensoroDevice.setSlotArray(sensoroSlotArray);
            sensoroDevice.setDataVersion(DATA_VERSION_03);
            sensoroDevice.setHasAppParam(false);
            sensoroDevice.setHasSensorParam(false);
            sensoroDevice.setHasEddyStone(true);
            sensoroDevice.setHasBleParam(true);
            sensoroDevice.setHasIbeacon(true);
            sensoroDevice.setHasLoraParam(true);
            sensoroDevice.setHasAdr(true);
            sensoroDevice.setHasAppKey(true);
            sensoroDevice.setHasAppSkey(true);
            sensoroDevice.setHasNwkSkey(true);
            sensoroDevice.setHasDevAddr(true);
            sensoroDevice.setHasDevEui(true);
            sensoroDevice.setHasAppEui(true);
            sensoroDevice.setHasAppKey(true);
            sensoroDevice.setHasAppSkey(true);
            sensoroDevice.setHasDataRate(true);
            sensoroDevice.setHasNwkSkey(true);
            sensoroDevice.setHasLoraInterval(true);
            sensoroDevice.setHasSensorBroadcast(true);
            sensoroDevice.setHasCustomPackage(true);

        } catch (Exception e) {
            e.printStackTrace();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.PARSE_ERROR);
                }
            });

            return;
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                sensoroConnectionCallback.onConnectedSuccess(sensoroDevice, CmdType.CMD_NULL);
            }
        });

    }

    private void parseData04(byte[] data) {
        final SensoroDevice sensoroDevice = new SensoroDevice();
        try {
            ProtoStd1U1.MsgStd msgStd = ProtoStd1U1.MsgStd.parseFrom(data);
            sensoroDevice.setClassBEnabled(msgStd.getEnableClassB());
            sensoroDevice.setClassBDataRate(msgStd.getClassBDataRate());
            sensoroDevice.setClassBPeriodicity(msgStd.getClassBPeriodicity());

            byte[] custom_data = msgStd.getCustomData().toByteArray();

            ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = ProtoMsgCfgV1U1.MsgCfgV1u1.parseFrom(custom_data);
            if (msgCfg.getSlotList().size() > 4) {
                byte ibeacon_data[] = msgCfg.getSlot(4).getFrame().toByteArray();
                byte uuid_data[] = new byte[16];
                System.arraycopy(ibeacon_data, 0, uuid_data, 0, 16);
                byte major_data[] = new byte[2];
                System.arraycopy(ibeacon_data, 16, major_data, 0, 2);
                byte minor_data[] = new byte[2];
                System.arraycopy(ibeacon_data, 18, minor_data, 0, 2);
                sensoroDevice.setProximityUUID(SensoroUtils.bytesToHexString(uuid_data));
                sensoroDevice.setMajor(SensoroUUID.byteArrayToInt(major_data));
                sensoroDevice.setMinor(SensoroUUID.byteArrayToInt(minor_data));
            }

            sensoroDevice.setAppEui(SensoroUtils.bytesToHexString(msgCfg.getAppEui().toByteArray()));
            sensoroDevice.setDevEui(SensoroUtils.bytesToHexString(msgCfg.getDevEui().toByteArray()));
            sensoroDevice.setAppKey(SensoroUtils.bytesToHexString(msgCfg.getAppKey().toByteArray()));
            sensoroDevice.setAppSkey(SensoroUtils.bytesToHexString(msgCfg.getAppSkey().toByteArray()));
            sensoroDevice.setNwkSkey(SensoroUtils.bytesToHexString(msgCfg.getNwkSkey().toByteArray()));
            sensoroDevice.setBleInt(msgCfg.getBleInt());
            sensoroDevice.setBleOnTime(msgCfg.getBleOnTime());
            sensoroDevice.setBleOffTime(msgCfg.getBleOffTime());
            sensoroDevice.setBleTxp(msgCfg.getBleTxp());
            sensoroDevice.setLoraInt(msgCfg.getLoraInt());
            sensoroDevice.setLoraDr(msgCfg.getLoraDr());
            sensoroDevice.setLoraAdr(msgCfg.getLoraAdr());
            sensoroDevice.setLoraTxp(msgCfg.getLoraTxp());
            sensoroDevice.setDevAdr(msgCfg.getDevAddr());
            sensoroDevice.setLoraAdr(msgCfg.getLoraAdr());
            sensoroDevice.setTempInterval(msgCfg.getTempInt());
            sensoroDevice.setLightInterval(msgCfg.getLightInt());
            sensoroDevice.setHumidityInterval(msgCfg.getHumiInt());
            List<ProtoMsgCfgV1U1.Slot> slotList = msgCfg.getSlotList();
            int slot_size = slotList.size();
            SensoroSlot sensoroSlotArray[] = new SensoroSlot[slot_size];
            for (int i = 0; i < slot_size; i++) {
                SensoroSlot sensoroSlot = new SensoroSlot();
                ProtoMsgCfgV1U1.Slot slot = slotList.get(i);
                sensoroSlot.setActived(slot.getActived());
                sensoroSlot.setIndex(slot.getIndex());
                sensoroSlot.setType(slot.getType().getNumber());
                if (slot.getType() == ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL) {
                    byte[] url_data = slot.getFrame().toByteArray();
                    sensoroSlot.setFrame(SensoroUtils.decodeUrl(url_data));
                } else {
                    sensoroSlot.setFrame(SensoroUtils.bytesToHexString(slot.getFrame().toByteArray()));
                }

                sensoroSlotArray[i] = sensoroSlot;
            }
            sensoroDevice.setHasAppParam(false);
            sensoroDevice.setHasSensorParam(false);
            sensoroDevice.setHasEddyStone(true);
            sensoroDevice.setHasBleParam(true);
            sensoroDevice.setHasIbeacon(true);
            sensoroDevice.setHasLoraParam(true);
            sensoroDevice.setHasAdr(true);
            sensoroDevice.setHasAppKey(true);
            sensoroDevice.setHasAppSkey(true);
            sensoroDevice.setHasNwkSkey(true);
            sensoroDevice.setHasDevAddr(true);
            sensoroDevice.setHasDevEui(true);
            sensoroDevice.setHasAppEui(true);
            sensoroDevice.setHasAppKey(true);
            sensoroDevice.setHasAppSkey(true);
            sensoroDevice.setHasDataRate(true);
            sensoroDevice.setHasNwkSkey(true);
            sensoroDevice.setHasSensorBroadcast(true);
            sensoroDevice.setHasCustomPackage(true);
            sensoroDevice.setHasLoraInterval(true);
            sensoroDevice.setSlotArray(sensoroSlotArray);
            sensoroDevice.setDataVersion(DATA_VERSION_04);
        } catch (Exception e) {
            e.printStackTrace();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.PARSE_ERROR);
                }
            });

            return;
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                sensoroConnectionCallback.onConnectedSuccess(sensoroDevice, CmdType.CMD_NULL);
            }
        });

    }

    private void parseData05(byte[] data) {

        final SensoroDevice sensoroDevice = new SensoroDevice();
        try {
            MsgNode1V1M5.MsgNode msgNode = MsgNode1V1M5.MsgNode.parseFrom(data);
            boolean hasAppParam = msgNode.hasAppParam();
            sensoroDevice.setHasAppParam(hasAppParam);
            if (hasAppParam) {
                MsgNode1V1M5.AppParam appParam = msgNode.getAppParam();
                boolean hasUploadInterval = appParam.hasUploadInterval();
                sensoroDevice.setHasUploadInterval(hasUploadInterval);
                if (hasUploadInterval) {
                    sensoroDevice.setUploadInterval(appParam.getUploadInterval());
                }
                boolean hasConfirm = appParam.hasConfirm();
                sensoroDevice.setHasConfirm(hasConfirm);
                if (hasConfirm) {
                    sensoroDevice.setConfirm(appParam.getConfirm());
                }
            }
            boolean hasBleParam = msgNode.hasBleParam();
            sensoroDevice.setHasBleParam(hasBleParam);
            if (hasBleParam) {
                MsgNode1V1M5.BleParam bleParam = msgNode.getBleParam();
                boolean hasBleInterval = bleParam.hasBleInterval();
                sensoroDevice.setHasBleInterval(hasBleInterval);
                if (hasBleInterval) {
                    sensoroDevice.setBleInt(bleParam.getBleInterval());
                }
                boolean hasBleOffTime = bleParam.hasBleOffTime();
                sensoroDevice.setHasBleOffTime(hasBleOffTime);
                if (hasBleOffTime) {
                    sensoroDevice.setBleOffTime(bleParam.getBleOffTime());
                }
                boolean hasBleOnTime = bleParam.hasBleOnTime();
                sensoroDevice.setHasBleOnTime(hasBleOnTime);
                if (hasBleOnTime) {
                    sensoroDevice.setBleOnTime(bleParam.getBleOnTime());
                }
                sensoroDevice.setBleTxp(bleParam.getBleTxp());
                boolean hasBleTxp = bleParam.hasBleTxp();
                sensoroDevice.setHasBleTxp(hasBleTxp);
                if (hasBleTxp) {
                    sensoroDevice.setHasBleOnOff(bleParam.hasBleOnOff());
                }
            }
            boolean hasLpwanParam = msgNode.hasLpwanParam();
            sensoroDevice.setHasLoraParam(hasLpwanParam);
            if (hasLpwanParam) {
                MsgNode1V1M5.LpwanParam lpwanParam = msgNode.getLpwanParam();
                boolean hasAdr = lpwanParam.hasAdr();
                sensoroDevice.setHasAdr(hasAdr);
                if (hasAdr) {
                    sensoroDevice.setLoraAdr(lpwanParam.getAdr());
                }
                boolean hasAppKey = lpwanParam.hasAppKey();
                sensoroDevice.setHasAppKey(hasAppKey);
                if (hasAppKey) {
                    sensoroDevice.setAppKey(SensoroUtils.bytesToHexString(lpwanParam.getAppKey().toByteArray()));
                }
                boolean hasAppSkey = lpwanParam.hasAppSkey();
                sensoroDevice.setHasAppSkey(hasAppSkey);
                if (hasAppSkey) {
                    sensoroDevice.setAppSkey(SensoroUtils.bytesToHexString(lpwanParam.getAppSkey().toByteArray()));
                }
                boolean hasNwkSkey = lpwanParam.hasNwkSkey();
                sensoroDevice.setHasNwkSkey(hasNwkSkey);
                if (hasNwkSkey) {
                    sensoroDevice.setNwkSkey(SensoroUtils.bytesToHexString(lpwanParam.getNwkSkey().toByteArray()));
                }
                boolean hasDevAddr = lpwanParam.hasDevAddr();
                sensoroDevice.setHasDevAddr(hasDevAddr);
                if (hasDevAddr) {
                    sensoroDevice.setDevAdr(lpwanParam.getDevAddr());
                }
                //TODO LoraDr?
                sensoroDevice.setLoraDr(lpwanParam.getDatarate());

                boolean hasDevEui = lpwanParam.hasDevEui();
                sensoroDevice.setHasDevEui(hasDevEui);
                if (hasDevEui) {
                    sensoroDevice.setDevEui(SensoroUtils.bytesToHexString(lpwanParam.getDevEui().toByteArray()));
                }

                boolean hasAppEui = lpwanParam.hasAppEui();
                sensoroDevice.setHasAppEui(hasAppEui);
                if (hasAppEui) {
                    sensoroDevice.setAppEui(SensoroUtils.bytesToHexString(lpwanParam.getAppEui().toByteArray()));
                }

                boolean hasDataRate = lpwanParam.hasDatarate();
                sensoroDevice.setHasDataRate(hasDataRate);
                //TODO classB?
                if (hasDataRate) {
                    sensoroDevice.setClassBDataRate(lpwanParam.getDatarate());
                }

                boolean hasTxPower = lpwanParam.hasTxPower();
                sensoroDevice.setHasLoraTxp(hasTxPower);
                if (hasTxPower) {
                    sensoroDevice.setLoraTxp(lpwanParam.getTxPower());
                }

                boolean hasActivation = lpwanParam.hasActivition();
                sensoroDevice.setHasActivation(hasActivation);
                if (hasActivation) {
                    sensoroDevice.setActivation(lpwanParam.getActivition().getNumber());
                }

                boolean hasDelay = lpwanParam.hasDelay();
                sensoroDevice.setHasDelay(hasDelay);
                if (hasDelay) {
                    sensoroDevice.setDelay(lpwanParam.getDelay());
                }
                //
                sensoroDevice.setChannelMaskList(lpwanParam.getChannelMaskList());
                boolean hasMaxEIRP = lpwanParam.hasMaxEIRP();
                sensoroDevice.setHasMaxEirp(hasMaxEIRP);
                if (hasMaxEIRP) {
                    sensoroDevice.setMaxEirp(lpwanParam.getMaxEIRP());
                }
                boolean hasSglStatus = lpwanParam.hasSglStatus();
                sensoroDevice.setHasSglStatus(hasSglStatus);
                if (hasSglStatus) {
                    sensoroDevice.setSglStatus(lpwanParam.getSglStatus());
                }
                boolean hasSglDatarate = lpwanParam.hasSglDatarate();
                sensoroDevice.setHasSglDatarate(hasSglDatarate);
                if (hasSglDatarate) {
                    sensoroDevice.setSglDatarate(lpwanParam.getSglDatarate());
                }
                boolean hasSglFrequency = lpwanParam.hasSglFrequency();
                sensoroDevice.setHasSglFrequency(hasSglFrequency);
                if (hasSglFrequency) {
                    sensoroDevice.setSglFrequency(lpwanParam.getSglFrequency());
                }

                //lpwanParam.getChannelsList() 一定不为null，只不过数量会为空
                List<MsgNode1V1M5.Channel> channelsList = lpwanParam.getChannelsList();
                ArrayList<SensoroChannel> list = new ArrayList<>();
                if (channelsList.size() > 0) {
                    for (MsgNode1V1M5.Channel channel : channelsList) {
                        SensoroChannel sensoroChannel = new SensoroChannel();
                        sensoroChannel.hasFrequency = channel.hasFrequency();
                        if (channel.hasFrequency()) {
                            sensoroChannel.frequency = channel.getFrequency();
                        }

                        sensoroChannel.hasRx1Frequency = channel.hasRx1Frequency();
                        if (channel.hasRx1Frequency()) {
                            sensoroChannel.rx1Frequency = channel.getRx1Frequency();
                        }
                        list.add(sensoroChannel);
                    }
                }
                sensoroDevice.setChannelList(list);


            }
            //
//            SensoroSensor sensoroSensor = new SensoroSensor();
            SensoroSensor sensoroSensorTest = new SensoroSensor();
            boolean hasFlame = msgNode.hasFlame();
            sensoroSensorTest.hasFlame = hasFlame;
            if (hasFlame) {//aae7e4 ble on off temp lower disable
                MsgNode1V1M5.SensorDataInt flame = msgNode.getFlame();
                sensoroSensorTest.flame = new SensoroData();
                boolean hasData = flame.hasData();
                sensoroSensorTest.flame.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.flame.data_int = flame.getData();
                }
            }
            boolean hasPitch = msgNode.hasPitch();
            sensoroSensorTest.hasPitch = hasPitch;
            if (hasPitch) {
                MsgNode1V1M5.SensorData pitch = msgNode.getPitch();
                sensoroSensorTest.pitch = new SensoroData();
                boolean hasAlarmHigh = pitch.hasAlarmHigh();
                sensoroSensorTest.pitch.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.pitch.alarmHigh_float = pitch.getAlarmHigh();
                }
                boolean hasAlarmLow = pitch.hasAlarmLow();
                sensoroSensorTest.pitch.has_alarmLow = hasAlarmLow;
                if (hasAlarmLow) {
                    sensoroSensorTest.pitch.alarmLow_float = pitch.getAlarmLow();
                }
                boolean hasData = pitch.hasData();
                sensoroSensorTest.pitch.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.pitch.data_float = pitch.getData();
                }
            }
            boolean hasRoll = msgNode.hasRoll();
            sensoroSensorTest.hasRoll = hasRoll;
            if (hasRoll) {
                MsgNode1V1M5.SensorData roll = msgNode.getRoll();
                sensoroSensorTest.roll = new SensoroData();
                boolean hasAlarmHigh = roll.hasAlarmHigh();
                sensoroSensorTest.roll.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.roll.alarmHigh_float = roll.getAlarmHigh();
                }
                boolean hasAlarmLow = roll.hasAlarmLow();
                sensoroSensorTest.roll.has_alarmLow = hasAlarmLow;
                if (hasAlarmLow) {
                    sensoroSensorTest.roll.alarmLow_float = roll.getAlarmLow();
                }
                boolean hasData = roll.hasData();
                sensoroSensorTest.roll.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.roll.data_float = roll.getData();
                }
            }
            boolean hasYaw = msgNode.hasYaw();
            sensoroSensorTest.hasYaw = hasYaw;
            if (hasYaw) {
                MsgNode1V1M5.SensorData yaw = msgNode.getYaw();
                sensoroSensorTest.yaw = new SensoroData();
                boolean hasAlarmHigh = yaw.hasAlarmHigh();
                sensoroSensorTest.yaw.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.yaw.alarmHigh_float = yaw.getAlarmHigh();
                }
                boolean hasAlarmLow = yaw.hasAlarmLow();
                sensoroSensorTest.yaw.has_alarmLow = hasAlarmLow;
                if (hasAlarmLow) {
                    sensoroSensorTest.yaw.alarmLow_float = yaw.getAlarmLow();
                }
                boolean hasData = yaw.hasData();
                sensoroSensorTest.yaw.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.yaw.data_float = yaw.getData();
                }
            }
            boolean hasWaterPressure = msgNode.hasWaterPressure();
            sensoroSensorTest.hasWaterPressure = hasWaterPressure;
            if (hasWaterPressure) {

                MsgNode1V1M5.SensorData waterPressure = msgNode.getWaterPressure();
                sensoroSensorTest.waterPressure = new SensoroData();
                boolean hasAlarmHigh = waterPressure.hasAlarmHigh();
                sensoroSensorTest.waterPressure.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.waterPressure.alarmHigh_float = waterPressure.getAlarmHigh();
                }
                boolean hasAlarmLow = waterPressure.hasAlarmLow();
                sensoroSensorTest.waterPressure.has_alarmLow = hasAlarmLow;
                if (hasAlarmLow) {
                    sensoroSensorTest.waterPressure.alarmLow_float = waterPressure.getAlarmLow();
                }
                boolean hasData = waterPressure.hasData();
                sensoroSensorTest.waterPressure.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.waterPressure.data_float = waterPressure.getData();
                }
            }
            boolean hasCh2O = msgNode.hasCh2O();
            sensoroSensorTest.hasCh2O = hasCh2O;
            if (hasCh2O) {
                MsgNode1V1M5.SensorData ch2O = msgNode.getCh2O();
                sensoroSensorTest.ch20 = new SensoroData();
                boolean hasData = ch2O.hasData();
                sensoroSensorTest.ch20.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.ch20.data_float = ch2O.getData();
                }
            }
            boolean hasCh4 = msgNode.hasCh4();
            sensoroSensorTest.hasCh4 = hasCh4;
            if (hasCh4) {
                MsgNode1V1M5.SensorData ch4 = msgNode.getCh4();
                sensoroSensorTest.ch4 = new SensoroData();
                boolean hasData = ch4.hasData();
                sensoroSensorTest.ch4.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.ch4.data_float = ch4.getData();
                }
                boolean hasAlarmHigh = ch4.hasAlarmHigh();
                sensoroSensorTest.ch4.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.ch4.alarmHigh_float = ch4.getAlarmHigh();
                }
            }
            boolean hasCover = msgNode.hasCover();
            sensoroSensorTest.hasCover = hasCover;
            if (hasCover) {
                //TODO
//                sensoroSensor.setCoverStatus(cover.getData());
                MsgNode1V1M5.SensorData cover = msgNode.getCover();
                sensoroSensorTest.coverStatus = new SensoroData();
                boolean hasData = cover.hasData();
                sensoroSensorTest.coverStatus.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.coverStatus.data_float = cover.getData();
                }
            }
            boolean hasCo = msgNode.hasCo();
            sensoroSensorTest.hasCo = hasCo;
            if (hasCo) {
                MsgNode1V1M5.SensorData co = msgNode.getCo();
                sensoroSensorTest.co = new SensoroData();
                boolean hasData = co.hasData();
                sensoroSensorTest.co.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.co.data_float = co.getData();
                }
                boolean hasAlarmHigh = co.hasAlarmHigh();
                sensoroSensorTest.co.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.co.alarmHigh_float = co.getAlarmHigh();
                }
            }
            boolean hasCo2 = msgNode.hasCo2();
            sensoroSensorTest.hasCo2 = hasCo2;
            if (hasCo2) {
                MsgNode1V1M5.SensorData co2 = msgNode.getCo2();
                sensoroSensorTest.co2 = new SensoroData();
                boolean hasData = co2.hasData();
                sensoroSensorTest.co2.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.co2.data_float = co2.getData();
                }
                boolean hasAlarmHigh = co2.hasAlarmHigh();
                sensoroSensorTest.co2.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.co2.alarmHigh_float = co2.getAlarmHigh();
                }
            }
            boolean hasNo2 = msgNode.hasNo2();
            sensoroSensorTest.hasNo2 = hasNo2;
            if (hasNo2) {
                MsgNode1V1M5.SensorData no2 = msgNode.getNo2();
                sensoroSensorTest.no2 = new SensoroData();
                boolean hasData = no2.hasData();
                sensoroSensorTest.no2.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.no2.data_float = no2.getData();
                }
                boolean hasAlarmHigh = no2.hasAlarmHigh();
                sensoroSensorTest.no2.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.no2.alarmHigh_float = no2.getAlarmHigh();
                }

            }
            boolean hasSo2 = msgNode.hasSo2();
            sensoroSensorTest.hasSo2 = hasSo2;
            if (hasSo2) {
                MsgNode1V1M5.SensorData so2 = msgNode.getSo2();
                sensoroSensorTest.so2 = new SensoroData();
                boolean hasData = so2.hasData();
                sensoroSensorTest.so2.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.so2.data_float = so2.getData();
                }
                boolean hasAlarmHigh = so2.hasAlarmHigh();
                sensoroSensorTest.so2.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.so2.alarmHigh_float = so2.getAlarmHigh();
                }
//                sensoroSensor.setHasSo2(msgNode.hasSo2());
            }

            boolean hasHumidity = msgNode.hasHumidity();
            sensoroSensorTest.hasHumidity = hasHumidity;
            if (hasHumidity) {
                MsgNode1V1M5.SensorData humidity = msgNode.getHumidity();
                sensoroSensorTest.humidity = new SensoroData();
                boolean hasData = humidity.hasData();
                sensoroSensorTest.humidity.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.humidity.data_float = humidity.getData();
                }
                boolean hasAlarmHigh = humidity.hasAlarmHigh();
                sensoroSensorTest.humidity.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.humidity.alarmHigh_float = humidity.getAlarmHigh();
                }
                boolean hasAlarmLow = humidity.hasAlarmLow();
                sensoroSensorTest.humidity.has_alarmLow = hasAlarmLow;
                if (hasAlarmLow) {
                    sensoroSensorTest.humidity.alarmLow_float = humidity.getAlarmLow();
                }
            }
            boolean hasTemperature = msgNode.hasTemperature();
            sensoroSensorTest.hasTemperature = hasTemperature;
            if (hasTemperature) {
                MsgNode1V1M5.SensorData temperature = msgNode.getTemperature();
                sensoroSensorTest.temperature = new SensoroData();
                boolean hasData = temperature.hasData();
                sensoroSensorTest.temperature.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.temperature.data_float = temperature.getData();
                }
                boolean hasAlarmHigh = temperature.hasAlarmHigh();
                sensoroSensorTest.temperature.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.temperature.alarmHigh_float = temperature.getAlarmHigh();
                }
                boolean hasAlarmLow = temperature.hasAlarmLow();
                sensoroSensorTest.temperature.has_alarmLow = hasAlarmLow;
                if (hasAlarmLow) {
                    sensoroSensorTest.temperature.alarmLow_float = temperature.getAlarmLow();
                }
            }
            boolean hasLight = msgNode.hasLight();
            sensoroSensorTest.hasLight = hasLight;
            if (hasLight) {
                MsgNode1V1M5.SensorData light = msgNode.getLight();
                sensoroSensorTest.light = new SensoroData();
                boolean hasData = light.hasData();
                sensoroSensorTest.light.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.light.data_float = light.getData();
                }
            }
            boolean hasLevel = msgNode.hasLevel();
            sensoroSensorTest.hasLevel = hasLevel;
            if (hasLevel) {
                MsgNode1V1M5.SensorData level = msgNode.getLevel();
                sensoroSensorTest.level = new SensoroData();
                boolean hasData = level.hasData();
                sensoroSensorTest.level.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.level.data_float = level.getData();
                }

            }
            boolean hasLpg = msgNode.hasLpg();
            sensoroSensorTest.hasLpg = hasLpg;
            if (hasLpg) {
                MsgNode1V1M5.SensorData lpg = msgNode.getLpg();
                sensoroSensorTest.lpg = new SensoroData();
                boolean hasData = lpg.hasData();
                sensoroSensorTest.lpg.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.lpg.data_float = lpg.getData();
                }
                boolean hasAlarmHigh = lpg.hasAlarmHigh();
                sensoroSensorTest.lpg.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.lpg.alarmHigh_float = lpg.getAlarmHigh();
                }
            }
            boolean hasO3 = msgNode.hasO3();
            sensoroSensorTest.hasO3 = hasO3;
            if (hasO3) {
                MsgNode1V1M5.SensorData o3 = msgNode.getO3();
                sensoroSensorTest.o3 = new SensoroData();
                boolean hasData = o3.hasData();
                sensoroSensorTest.o3.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.o3.data_float = o3.getData();
                }
            }
            boolean hasPm1 = msgNode.hasPm1();
            sensoroSensorTest.hasPm1 = hasPm1;
            if (hasPm1) {
                MsgNode1V1M5.SensorData pm1 = msgNode.getPm1();
                sensoroSensorTest.pm1 = new SensoroData();
                boolean hasData = pm1.hasData();
                sensoroSensorTest.pm1.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.pm1.data_float = pm1.getData();
                }
            }
            boolean hasPm25 = msgNode.hasPm25();
            sensoroSensorTest.hasPm25 = hasPm25;
            if (hasPm25) {
                MsgNode1V1M5.SensorData pm25 = msgNode.getPm25();
                sensoroSensorTest.pm25 = new SensoroData();
                boolean hasData = pm25.hasData();
                sensoroSensorTest.pm25.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.pm25.data_float = pm25.getData();
                }
                boolean hasAlarmHigh = pm25.hasAlarmHigh();
                sensoroSensorTest.pm25.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.pm25.alarmHigh_float = pm25.getAlarmHigh();
                }
            }
            boolean hasPm10 = msgNode.hasPm10();
            sensoroSensorTest.hasPm10 = hasPm10;
            if (hasPm10) {
                MsgNode1V1M5.SensorData pm10 = msgNode.getPm10();
                sensoroSensorTest.pm10 = new SensoroData();
                boolean hasData = pm10.hasData();
                sensoroSensorTest.pm10.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.pm10.data_float = pm10.getData();
                }
                boolean hasAlarmHigh = pm10.hasAlarmHigh();
                sensoroSensorTest.pm10.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.pm10.alarmHigh_float = pm10.getAlarmHigh();
                }
            }
            boolean hasSmoke = msgNode.hasSmoke();
            sensoroSensorTest.hasSmoke = hasSmoke;
            if (hasSmoke) {
                MsgNode1V1M5.SensorData smoke = msgNode.getSmoke();
                sensoroSensorTest.smoke = new SensoroData();
                boolean hasData = smoke.hasData();
                sensoroSensorTest.smoke.has_data = hasData;
                if (hasData) {
                    sensoroSensorTest.smoke.data_float = smoke.getData();
                }
                sensoroSensorTest.smoke.has_status = true;
                sensoroSensorTest.smoke.status = smoke.getError().getNumber();
            }
            boolean hasMultiTemp = msgNode.hasMultiTemp();
            sensoroSensorTest.hasMultiTemp = hasMultiTemp;
            if (hasMultiTemp) {
                MsgNode1V1M5.MultiSensorDataInt multiTemp = msgNode.getMultiTemp();
                sensoroSensorTest.multiTemperature = new SensoroData();
                boolean hasAlarmHigh = multiTemp.hasAlarmHigh();
                sensoroSensorTest.multiTemperature.has_alarmHigh = hasAlarmHigh;
                if (hasAlarmHigh) {
                    sensoroSensorTest.multiTemperature.alarmHigh_int = multiTemp.getAlarmHigh();
                }
                boolean hasAlarmLow = multiTemp.hasAlarmLow();
                sensoroSensorTest.multiTemperature.has_alarmLow = hasAlarmLow;
                if (hasAlarmLow) {
                    sensoroSensorTest.multiTemperature.alarmLow_int = multiTemp.getAlarmLow();
                }
                boolean hasAlarmStepHigh = multiTemp.hasAlarmStepHigh();
                sensoroSensorTest.multiTemperature.has_alarmStepHigh = hasAlarmStepHigh;
                if (hasAlarmStepHigh) {
                    sensoroSensorTest.multiTemperature.alarmStepHigh_int = multiTemp.getAlarmStepHigh();
                }
                boolean hasAlarmStepLow = multiTemp.hasAlarmStepLow();
                sensoroSensorTest.multiTemperature.has_alarmStepLow = hasAlarmStepLow;
                if (hasAlarmStepLow) {
                    sensoroSensorTest.multiTemperature.alarmStepLow_int = multiTemp.getAlarmStepLow();
                }
            }
            boolean hasFireData = msgNode.hasFireData();
            sensoroSensorTest.hasFireData = hasFireData;
            if (hasFireData) {
                MsgNode1V1M5.ElecFireData fireData = msgNode.getFireData();
                sensoroSensorTest.elecFireData = new SensoroFireData();
                boolean hasSensorPwd = fireData.hasSensorPwd();
                sensoroSensorTest.elecFireData.hasSensorPwd = hasSensorPwd;
                if (hasSensorPwd) {
                    sensoroSensorTest.elecFireData.sensorPwd = fireData.getSensorPwd();
                }
                boolean hasLeakageTh = fireData.hasLeakageTh();
                sensoroSensorTest.elecFireData.hasLeakageTh = hasLeakageTh;
                if (hasLeakageTh) {
                    sensoroSensorTest.elecFireData.leakageTh = fireData.getLeakageTh();
                }
                boolean hasTempTh = fireData.hasTempTh();
                sensoroSensorTest.elecFireData.hasTempTh = hasTempTh;
                if (hasTempTh) {
                    sensoroSensorTest.elecFireData.tempTh = fireData.getTempTh();
                }
                boolean hasCurrentTh = fireData.hasCurrentTh();
                sensoroSensorTest.elecFireData.hasCurrentTh = hasCurrentTh;
                if (hasCurrentTh) {
                    sensoroSensorTest.elecFireData.currentTh = fireData.getCurrentTh();
                }
                boolean hasLoadTh = fireData.hasLoadTh();
                sensoroSensorTest.elecFireData.hasLoadTh = hasLoadTh;
                if (hasLoadTh) {
                    sensoroSensorTest.elecFireData.loadTh = fireData.getLoadTh();
                }
                boolean hasVolHighTh = fireData.hasVolHighTh();
                sensoroSensorTest.elecFireData.hasVolHighTh = hasVolHighTh;
                if (hasVolHighTh) {
                    sensoroSensorTest.elecFireData.volHighTh = fireData.getVolHighTh();
                }
                boolean hasVolLowTh = fireData.hasVolLowTh();
                sensoroSensorTest.elecFireData.hasVolLowTh = hasVolLowTh;
                if (hasVolLowTh) {
                    sensoroSensorTest.elecFireData.volLowTh = fireData.getVolLowTh();
                }
                boolean hasCmd = fireData.hasCmd();
                sensoroSensorTest.elecFireData.hasCmd = hasCmd;
                if (hasCmd) {
                    sensoroSensorTest.elecFireData.cmd = fireData.getCmd();
                }

            }

            //曼顿电气火灾
            parseMantunData(msgNode, sensoroSensorTest);

            //安科瑞三相电
            parseAcrelFires(msgNode, sensoroSensorTest);
            //嘉德 自研烟感
            parseCaymanData(msgNode, sensoroSensorTest);
            //baymax ch4 lpg
            parseBaymaxCh4Lpg(msgNode, sensoroSensorTest);
            sensoroDevice.setSensoroSensorTest(sensoroSensorTest);
            sensoroDevice.setDataVersion(DATA_VERSION_05);
            sensoroDevice.setHasSensorParam(true);
            sensoroDevice.setHasEddyStone(false);
            sensoroDevice.setHasIbeacon(false);
            sensoroDevice.setHasLoraInterval(false);
            sensoroDevice.setHasSensorBroadcast(false);
            sensoroDevice.setHasCustomPackage(false);
            /////////////
//            SensoroSensor sensoroSensor = new SensoroSensor();
//            boolean hasFlame = msgNode.hasFlame();
//            sensoroSensor.setHasFlame(hasFlame);
//            if (hasFlame) {//aae7e4 ble on off temp lower disable
//                sensoroSensor.setFlame(msgNode.getFlame().getData());
//            }
//            boolean hasPitch = msgNode.hasPitch();
//            sensoroSensor.setHasPitchAngle(hasPitch);
//            if (hasPitch) {
//                MsgNode1V1M5.SensorData pitch = msgNode.getPitch();
//                boolean hasAlarmHigh = pitch.hasAlarmHigh();
////                sensoroSensor.
//                sensoroSensor.setPitchAngleAlarmHigh(pitch.getAlarmHigh());
//                sensoroSensor.setPitchAngleAlarmLow(pitch.getAlarmLow());
//                sensoroSensor.setPitchAngle(pitch.getData());
//            }
//            if (msgNode.hasRoll()) {
//                sensoroSensor.setRollAngleAlarmHigh(msgNode.getRoll().getAlarmHigh());
//                sensoroSensor.setRollAngleAlarmLow(msgNode.getRoll().getAlarmLow());
//                sensoroSensor.setRollAngle(msgNode.getRoll().getData());
//                sensoroSensor.setHasRollAngle(msgNode.hasRoll());
//            }
//            if (msgNode.hasYaw()) {
//                sensoroSensor.setYawAngleAlarmHigh(msgNode.getYaw().getAlarmHigh());
//                sensoroSensor.setYawAngleAlarmLow(msgNode.getYaw().getAlarmLow());
//                sensoroSensor.setYawAngle(msgNode.getYaw().getData());
//                sensoroSensor.setHasYawAngle(msgNode.hasYaw());
//            }
//            if (msgNode.hasWaterPressure()) {
//                sensoroSensor.setWaterPressureAlarmHigh(msgNode.getWaterPressure().getAlarmHigh());
//                sensoroSensor.setWaterPressureAlarmLow(msgNode.getWaterPressure().getAlarmLow());
//                sensoroSensor.setWaterPressure(msgNode.getWaterPressure().getData());
//                sensoroSensor.setHasWaterPressure(msgNode.hasWaterPressure());
//            }
//            sensoroDevice.setHasLoraParam(hasLoraParam);
//            sensoroSensor.setCh20(msgNode.getCh2O().getData());
//            sensoroSensor.setHasCh2O(msgNode.hasCh2O());
//            sensoroSensor.setCh4(msgNode.getCh4().getData());
//            sensoroSensor.setCh4AlarmHigh(msgNode.getCh4().getAlarmHigh());
//            sensoroSensor.setHasCh4(msgNode.hasCh4());
//            sensoroSensor.setCoverStatus(msgNode.getCover().getData());
//            sensoroSensor.setHasCover(msgNode.hasCover());
//            sensoroSensor.setCo(msgNode.getCo().getData());
//            sensoroSensor.setCoAlarmHigh(msgNode.getCo().getAlarmHigh());
//            sensoroSensor.setHasCo(msgNode.hasCo());
//            sensoroSensor.setCo2(msgNode.getCo2().getData());
//            sensoroSensor.setCo2AlarmHigh(msgNode.getCo2().getAlarmHigh());
//            sensoroSensor.setHasCo2(msgNode.hasCo2());
//            sensoroSensor.setNo2(msgNode.getNo2().getData());
//            sensoroSensor.setNo2AlarmHigh(msgNode.getNo2().getAlarmHigh());
//            sensoroSensor.setHasNo2(msgNode.hasNo2());
//            sensoroSensor.setSo2(msgNode.getSo2().getData());
//            sensoroSensor.setHasSo2(msgNode.hasSo2());
//            sensoroSensor.setHumidity(msgNode.getHumidity().getData());
//            sensoroSensor.setHasHumidity(msgNode.hasHumidity());
//            sensoroSensor.setTemperature(msgNode.getTemperature().getData());
//            sensoroSensor.setHasTemperature(msgNode.hasTemperature());
//            sensoroSensor.setLight(msgNode.getLight().getData());
//            sensoroSensor.setHasLight(msgNode.hasLight());
//            sensoroSensor.setLevel(msgNode.getLevel().getData());
//            sensoroSensor.setHasLevel(msgNode.hasLevel());
//            sensoroSensor.setLpg(msgNode.getLpg().getData());
//            sensoroSensor.setLpgAlarmHigh(msgNode.getLpg().getAlarmHigh());
//            sensoroSensor.setHasLpg(msgNode.hasLpg());
//            sensoroSensor.setO3(msgNode.getO3().getData());
//            sensoroSensor.setHasO3(msgNode.hasO3());
//            sensoroSensor.setPm1(msgNode.getPm1().getData());
//            sensoroSensor.setHasPm1(msgNode.hasPm1());
//            sensoroSensor.setPm25(msgNode.getPm25().getData());
//            sensoroSensor.setPm25AlarmHigh(msgNode.getPm25().getAlarmHigh());
//            sensoroSensor.setHasPm25(msgNode.hasPm25());
//            sensoroSensor.setPm10(msgNode.getPm10().getData());
//            sensoroSensor.setPm10AlarmHigh(msgNode.getPm10().getAlarmHigh());
//            sensoroSensor.setHasPm10(msgNode.hasPm10());
//            sensoroSensor.setTempAlarmHigh(msgNode.getTemperature().getAlarmHigh());
//            sensoroSensor.setTempAlarmLow(msgNode.getTemperature().getAlarmLow());
//            sensoroSensor.setHumidityAlarmHigh(msgNode.getHumidity().getAlarmHigh());
//            sensoroSensor.setHumidityAlarmLow(msgNode.getHumidity().getAlarmLow());
//            sensoroSensor.setSmoke(msgNode.getSmoke().getData());
//            sensoroSensor.setSmokeStatus(msgNode.getSmoke().getError().getNumber());//None Noraml, Unknown fault
//            sensoroSensor.setHasSmoke(msgNode.hasSmoke());
//            boolean hasMultiTemp = msgNode.hasMultiTemp();
//            sensoroDevice.setHasMultiTemperature(hasMultiTemp);
//            if (hasMultiTemp) {
//                MsgNode1V1M5.MultiSensorDataInt multiTemperature = msgNode.getMultiTemp();
//                boolean hasAlarmStepHigh = multiTemperature.hasAlarmStepHigh();
//                boolean hasAlarmStepLow = multiTemperature.hasAlarmStepLow();
//                boolean hasAlarmHigh = multiTemperature.hasAlarmHigh();
//                boolean hasAlarmLow = multiTemperature.hasAlarmLow();
//                sensoroDevice.setHasAlarmHigh(hasAlarmHigh);
//                sensoroDevice.setHasAlarmLow(hasAlarmLow);
//                sensoroDevice.setHasAlarmStepHigh(hasAlarmStepHigh);
//                sensoroDevice.setHasAlarmStepLow(hasAlarmStepLow);
//                if (hasAlarmStepHigh) {
//                    sensoroDevice.setAlarmStepHigh(multiTemperature.getAlarmStepHigh());
//                }
//                if (hasAlarmStepLow) {
//                    sensoroDevice.setAlarmStepLow(multiTemperature.getAlarmStepLow());
//                }
//                if (hasAlarmHigh) {
//                    sensoroDevice.setAlarmHigh(multiTemperature.getAlarmHigh());
//                }
//                if (hasAlarmLow) {
//                    sensoroDevice.setAlarmLow(multiTemperature.getAlarmLow());
//                }
//            }
//            sensoroDevice.setSensoroSensor(sensoroSensor);
//            sensoroDevice.setDataVersion(DATA_VERSION_05);
//            sensoroDevice.setHasSensorParam(true);
//            sensoroDevice.setHasEddyStone(false);
//            sensoroDevice.setHasIbeacon(false);
//            sensoroDevice.setHasLoraInterval(false);
//            sensoroDevice.setHasSensorBroadcast(false);
//            sensoroDevice.setHasCustomPackage(false);


        } catch (Exception e) {
            e.printStackTrace();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    sensoroConnectionCallback.onConnectedFailure(ResultCode.PARSE_ERROR);
                }
            });
            return;
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.loge("parseData05  onConnectedSuccess");
                sensoroConnectionCallback.onConnectedSuccess(sensoroDevice, CmdType.CMD_NULL);
            }
        });
    }

    private void parseBaymaxCh4Lpg(MsgNode1V1M5.MsgNode msgNode, SensoroSensor sensoroSensorTest) {
        boolean hasBaymaxData = msgNode.hasBaymaxData();
        LogUtils.loge("parseData05 dd " + hasBaymaxData);
        sensoroSensorTest.hasBaymax = hasBaymaxData;
        if (hasBaymaxData) {
            MsgNode1V1M5.Baymax baymaxData = msgNode.getBaymaxData();
            sensoroSensorTest.baymax = new SensoroBaymax();
            sensoroSensorTest.baymax.hasGasDensity = baymaxData.hasGasDensity();
            if (baymaxData.hasGasDensity()) {
                sensoroSensorTest.baymax.gasDensity = baymaxData.getGasDensity();
            }
            sensoroSensorTest.baymax.hasGasDensityL1 = baymaxData.hasGasDensityL1();
            if (baymaxData.hasGasDensityL1()) {
                sensoroSensorTest.baymax.gasDensityL1 = baymaxData.getGasDensityL1();
            }
            sensoroSensorTest.baymax.hasGasDensityL2 = baymaxData.hasGasDensityL2();
            if (baymaxData.hasGasDensityL2()) {
                sensoroSensorTest.baymax.gasDensityL2 = baymaxData.getGasDensityL2();
            }
            sensoroSensorTest.baymax.hasGasDensityL3 = baymaxData.hasGasDensityL3();
            if (baymaxData.hasGasDensityL3()) {
                sensoroSensorTest.baymax.gasDensityL3 = baymaxData.getGasDensityL3();
            }
            sensoroSensorTest.baymax.hasGasDisassembly = baymaxData.hasGasDisassembly();
            if (baymaxData.hasGasDisassembly()) {
                sensoroSensorTest.baymax.gasDisassembly = baymaxData.getGasDisassembly();
            }
            sensoroSensorTest.baymax.hasGasLosePwr = baymaxData.hasGasLosePwr();
            if (baymaxData.hasGasLosePwr()) {
                sensoroSensorTest.baymax.gasLosePwr = baymaxData.getGasLosePwr();
            }
            sensoroSensorTest.baymax.hasGasEMValve = baymaxData.hasGasEMValve();
            if (baymaxData.hasGasEMValve()) {
                sensoroSensorTest.baymax.gasEMValve = baymaxData.getGasEMValve();
            }
            sensoroSensorTest.baymax.hasGasDeviceStatus = baymaxData.hasGasDeviceStatus();
            if (baymaxData.hasGasDeviceStatus()) {
                sensoroSensorTest.baymax.gasDeviceStatus = baymaxData.getGasDeviceStatus();
            }
            sensoroSensorTest.baymax.hasGasDeviceOpState = baymaxData.hasGasDeviceOpState();
            if (baymaxData.hasGasDeviceOpState()) {
                sensoroSensorTest.baymax.gasDeviceOpState = baymaxData.getGasDeviceOpState();
            }
            sensoroSensorTest.baymax.hasGasDeviceComsDown = baymaxData.hasGasDeviceComsDown();
            if (baymaxData.hasGasDeviceComsDown()) {
                sensoroSensorTest.baymax.gasDeviceComsDown = baymaxData.getGasDeviceComsDown();
            }
            sensoroSensorTest.baymax.hasGasDeviceCMD = baymaxData.hasGasDeviceCMD();
            if (baymaxData.hasGasDeviceCMD()) {
                sensoroSensorTest.baymax.gasDeviceCMD = baymaxData.getGasDeviceCMD();
            }
        }
    }

    private void parseCaymanData(MsgNode1V1M5.MsgNode msgNode, SensoroSensor sensoroSensorTest) {
        boolean hasCaymanData = msgNode.hasCaymanData();
        LogUtils.loge("parseData05 ww " + hasCaymanData);
        sensoroSensorTest.hasCayMan = hasCaymanData;
        if (hasCaymanData) {
            MsgNode1V1M5.Cayman caymanData = msgNode.getCaymanData();
            sensoroSensorTest.cayManData = new SensoroCayManData();
            sensoroSensorTest.cayManData.hasIsSmoke = caymanData.hasIsSmoke();
            if (caymanData.hasIsSmoke()) {
                sensoroSensorTest.cayManData.isSmoke = caymanData.getIsSmoke();
            }
            sensoroSensorTest.cayManData.hasIsMoved = caymanData.hasIsMoved();
            if (caymanData.hasIsMoved()) {
                sensoroSensorTest.cayManData.isMoved = caymanData.getIsMoved();
            }
            sensoroSensorTest.cayManData.hasValueOfTem = caymanData.hasValueOfTem();
            if (caymanData.hasValueOfTem()) {
                sensoroSensorTest.cayManData.valueOfTem = caymanData.getValueOfTem();
            }
            sensoroSensorTest.cayManData.hasValueOfHum = caymanData.hasValueOfHum();
            if (caymanData.hasValueOfHum()) {
                sensoroSensorTest.cayManData.valueOfHum = caymanData.getValueOfHum();
            }
            sensoroSensorTest.cayManData.hasAlarmOfHighTem = caymanData.hasAlarmOfHighTem();
            if (caymanData.hasAlarmOfHighTem()) {
                sensoroSensorTest.cayManData.alarmOfHighTem = caymanData.getAlarmOfHighTem();
            }
            sensoroSensorTest.cayManData.hasAlarmOfLowTem = caymanData.hasAlarmOfLowTem();
            if (caymanData.hasAlarmOfLowTem()) {
                sensoroSensorTest.cayManData.alarmOfLowTem = caymanData.getAlarmOfLowTem();
            }
            sensoroSensorTest.cayManData.hasAlarmOfHighHum = caymanData.hasAlarmOfHighHum();
            if (caymanData.hasAlarmOfHighHum()) {
                sensoroSensorTest.cayManData.alarmOfHighHum = caymanData.getAlarmOfHighHum();
            }
            sensoroSensorTest.cayManData.hasAlarmOfLowHum = caymanData.hasAlarmOfLowHum();
            if (caymanData.hasAlarmOfLowHum()) {
                sensoroSensorTest.cayManData.alarmOfLowHum = caymanData.getAlarmOfLowHum();
            }
            sensoroSensorTest.cayManData.hasCmd = caymanData.hasCmd();
            if (caymanData.hasCmd()) {
                sensoroSensorTest.cayManData.cmd = caymanData.getCmd();
            }

        }
    }

    private void parseAcrelFires(MsgNode1V1M5.MsgNode msgNode, SensoroSensor sensoroSensorTest) {
        boolean hasAcrelData = msgNode.hasAcrelData();
        sensoroSensorTest.hasAcrelFires = hasAcrelData;
        if (hasAcrelData) {
            MsgNode1V1M5.AcrelData acrelData = msgNode.getAcrelData();
            sensoroSensorTest.acrelFires = new SensoroAcrelFires();
            sensoroSensorTest.acrelFires.hasConnectSw = acrelData.hasConnectSw();
            if (acrelData.hasConnectSw()) {
                sensoroSensorTest.acrelFires.connectSw = acrelData.getConnectSw();
            }
            sensoroSensorTest.acrelFires.hasChEnable = acrelData.hasChEnable();
            if (acrelData.hasChEnable()) {
                sensoroSensorTest.acrelFires.chEnable = acrelData.getChEnable();
            }
            sensoroSensorTest.acrelFires.hasLeakageTh = acrelData.hasLeakageTh();
            if (acrelData.hasLeakageTh()) {
                sensoroSensorTest.acrelFires.leakageTh = acrelData.getLeakageTh();
            }
            sensoroSensorTest.acrelFires.hasT1Th = acrelData.hasT1Th();
            if (acrelData.hasT1Th()) {
                sensoroSensorTest.acrelFires.t1Th = acrelData.getT1Th();
            }
            sensoroSensorTest.acrelFires.hasT2Th = acrelData.hasT2Th();
            if (acrelData.hasT2Th()) {
                sensoroSensorTest.acrelFires.t2Th = acrelData.getT2Th();
            }
            sensoroSensorTest.acrelFires.hasT3Th = acrelData.hasT3Th();
            if (acrelData.hasT3Th()) {
                sensoroSensorTest.acrelFires.t3Th = acrelData.getT3Th();
            }
            sensoroSensorTest.acrelFires.hasT4Th = acrelData.hasT4Th();
            if (acrelData.hasT4Th()) {
                sensoroSensorTest.acrelFires.t4Th = acrelData.getT4Th();
            }
            sensoroSensorTest.acrelFires.hasPasswd = acrelData.hasPasswd();
            if (acrelData.hasPasswd()) {
                sensoroSensorTest.acrelFires.passwd = acrelData.getPasswd();
            }
            sensoroSensorTest.acrelFires.hasValHighSet = acrelData.hasValHighSet();
            if (acrelData.hasValHighSet()) {
                sensoroSensorTest.acrelFires.valHighSet = acrelData.getValHighSet();
            }
            sensoroSensorTest.acrelFires.hasValLowSet = acrelData.hasValLowSet();
            if (acrelData.hasValLowSet()) {
                sensoroSensorTest.acrelFires.valLowSet = acrelData.getValLowSet();
            }
            sensoroSensorTest.acrelFires.hasCurrHighSet = acrelData.hasCurrHighSet();
            if (acrelData.hasCurrHighSet()) {
                sensoroSensorTest.acrelFires.currHighSet = acrelData.getCurrHighSet();
            }
            sensoroSensorTest.acrelFires.hasValHighType = acrelData.hasValHighType();
            if (acrelData.hasValHighType()) {
                sensoroSensorTest.acrelFires.valHighType = acrelData.getValHighType();
            }
            sensoroSensorTest.acrelFires.hasValLowType = acrelData.hasValLowType();
            if (acrelData.hasValLowType()) {
                sensoroSensorTest.acrelFires.valLowType = acrelData.getValLowType();
            }
            sensoroSensorTest.acrelFires.hasCurrHighType = acrelData.hasCurrHighType();
            if (acrelData.hasCurrHighType()) {
                sensoroSensorTest.acrelFires.currHighType = acrelData.getCurrHighType();
            }
            sensoroSensorTest.acrelFires.hasCmd = acrelData.hasCmd();
            if (acrelData.hasCmd()) {
                sensoroSensorTest.acrelFires.cmd = acrelData.getCmd();
            }
            sensoroSensorTest.acrelFires.hasIct = acrelData.hasIct();
            if (acrelData.hasIct()) {
                sensoroSensorTest.acrelFires.ict = acrelData.getIct();
            }
            sensoroSensorTest.acrelFires.hasCt = acrelData.hasCt();
            if (acrelData.hasCt()) {
                sensoroSensorTest.acrelFires.ct = acrelData.getCt();
            }
        }
    }

    private void parseMantunData(MsgNode1V1M5.MsgNode msgNode, SensoroSensor sensoroSensorTest) {
        List<MsgNode1V1M5.MantunData> mtunDataList = msgNode.getMtunDataList();
        if (mtunDataList != null && mtunDataList.size() > 0) {
            ArrayList<SensoroMantunData> sensoroMantunDatas = new ArrayList<>();
            for (MsgNode1V1M5.MantunData mantunData : mtunDataList) {
                SensoroMantunData smd = new SensoroMantunData();
                boolean hasVolVal = mantunData.hasVolVal();
                smd.hasVolVal = hasVolVal;
                if (hasVolVal) {
                    smd.volVal = mantunData.getVolVal();
                }
                boolean hasCurrVal = mantunData.hasCurrVal();
                smd.hasCurrVal = hasCurrVal;
                if (hasCurrVal) {
                    smd.currVal = mantunData.getCurrVal();
                }
                boolean hasLeakageVal = mantunData.hasLeakageVal();
                smd.hasLeakageVal = hasLeakageVal;
                if (hasLeakageVal) {
                    smd.leakageVal = mantunData.getLeakageVal();
                }
                boolean hasPowerVal = mantunData.hasPowerVal();
                smd.hasPowerVal = hasPowerVal;
                if (hasPowerVal) {
                    smd.powerVal = mantunData.getPowerVal();
                }
                boolean hasKwhVal = mantunData.hasKwhVal();
                smd.hasKwhVal = hasKwhVal;
                if (hasKwhVal) {
                    smd.kwhVal = mantunData.getKwhVal();
                }

                boolean hasTempVal = mantunData.hasTempVal();
                smd.hasTempVal = hasTempVal;
                if (hasTempVal) {
                    smd.tempVal = mantunData.getTempVal();
                }
                boolean hasStatus = mantunData.hasStatus();
                smd.hasStatus = hasStatus;
                if (hasStatus) {
                    smd.status = mantunData.getStatus();
                }
                boolean hasSwOnOff = mantunData.hasSwOnOff();
                smd.hasSwOnOff = hasSwOnOff;
                if (hasSwOnOff) {
                    smd.swOnOff = mantunData.getSwOnOff();
                }
                boolean hasId = mantunData.hasId();
                smd.hasId = hasId;
                if (hasId) {
                    smd.id = mantunData.getId();
                }
                boolean hasVolHighTh = mantunData.hasVolHighTh();
                smd.hasVolHighTh = hasVolHighTh;
                if (hasVolHighTh) {
                    smd.volHighTh = mantunData.getVolHighTh();
                }
                boolean hasVolLowTh = mantunData.hasVolLowTh();
                smd.hasVolLowTh = hasVolLowTh;
                if (hasVolLowTh) {
                    smd.volLowTh = mantunData.getVolLowTh();
                }
                boolean hasLeakageTh = mantunData.hasLeakageTh();
                smd.hasLeakageTh = hasLeakageTh;
                if (hasLeakageTh) {
                    smd.leakageTh = mantunData.getLeakageTh();
                }
                boolean hasTempTh = mantunData.hasTempTh();
                smd.hasTempTh = hasTempTh;
                if (hasTempTh) {
                    smd.tempTh = mantunData.getTempTh();
                }
                boolean hasCurrentTh = mantunData.hasCurrentTh();
                smd.hasCurrentTh = hasCurrentTh;
                if (hasCurrentTh) {
                    smd.currentTh = mantunData.getCurrentTh();
                }
                boolean hasPowerTh = mantunData.hasPowerTh();
                smd.hasPowerTh = hasPowerTh;
                if (hasPowerTh) {
                    smd.powerTh = mantunData.getPowerTh();
                }

                boolean hasAttribute = mantunData.hasAttribute();
                smd.hasAttribute = hasAttribute;
                if (hasAttribute) {
                    smd.attribute = mantunData.getAttribute();
                }
                boolean hasCmd = mantunData.hasCmd();
                smd.hasCmd = hasCmd;
                if (hasCmd) {
                    smd.cmd = mantunData.getCmd();
                }
                sensoroMantunDatas.add(smd);
            }
            sensoroSensorTest.mantunDatas = sensoroMantunDatas;
        }
    }

    private void parseData(byte[] data) {
        switch (dataVersion) {
            case DATA_VERSION_03:
                parseData03(data);
                break;
            case DATA_VERSION_04:
                parseData04(data);
                break;
            case DATA_VERSION_05:
                parseData05(data);
                break;
        }
    }

    public void writeDeviceAdvanceConfiguration(SensoroDeviceConfiguration deviceConfiguration, final SensoroWriteCallback
            writeCallback) throws InvalidProtocolBufferException {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        switch (dataVersion) {
            case DATA_VERSION_03: {
                ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
                msgCfgBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.devEui)
                )));
                msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.appEui)
                )));
                msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.appKey)));
                msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration
                        .appSkey)));
                msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration
                        .nwkSkey)));
                msgCfgBuilder.setDevAddr(deviceConfiguration.devAdr);
                msgCfgBuilder.setLoraDr(deviceConfiguration.getLoraDr());
                msgCfgBuilder.setLoraAdr(deviceConfiguration.loadAdr);
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();

                byte[] data = msgCfg.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(3, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            case DATA_VERSION_04: {
                ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
                ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
                msgCfgBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.devEui)
                )));
                msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.appEui)
                )));
                msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.appKey)));
                msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration
                        .appSkey)));
                msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration
                        .nwkSkey)));
                msgCfgBuilder.setDevAddr(deviceConfiguration.devAdr);
                msgCfgBuilder.setLoraDr(deviceConfiguration.getLoraDr());
                msgCfgBuilder.setLoraAdr(deviceConfiguration.loadAdr);
                msgStdBuilder.setCustomData(msgCfgBuilder.build().toByteString());
                msgStdBuilder.setEnableClassB(deviceConfiguration.classBEnabled);
                msgStdBuilder.setClassBDataRate(deviceConfiguration.classBDateRate);
                msgStdBuilder.setClassBPeriodicity(deviceConfiguration.classBPeriodicity);
                ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();

                byte[] data = msgStd.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(4, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            case DATA_VERSION_05: {
                MsgNode1V1M5.MsgNode.Builder builder = MsgNode1V1M5.MsgNode.newBuilder();
                MsgNode1V1M5.LpwanParam.Builder loraParamBuilder = MsgNode1V1M5.LpwanParam.newBuilder();
                if (deviceConfiguration.hasDevEui()) {
                    loraParamBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration
                            .devEui))));
                }
                if (deviceConfiguration.hasAppEui()) {
                    loraParamBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration
                            .appEui))));
                }
                if (deviceConfiguration.hasAppKey()) {
                    loraParamBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration
                            .appKey)));
                }
                if (deviceConfiguration.hasAppSkey()) {
                    loraParamBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration
                            .appSkey)));
                }
                if (deviceConfiguration.hasNwkSkey()) {
                    loraParamBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration
                            .nwkSkey)));
                }
                if (deviceConfiguration.hasDevAddr()) {
                    loraParamBuilder.setDevAddr(deviceConfiguration.devAdr);
                }
                if (deviceConfiguration.hasDelay()) {
                    loraParamBuilder.setDelay(deviceConfiguration.delay);
                }


//                if (deviceConfiguration.hasSglStatus()) {
//                    loraParamBuilder.setSglStatus(deviceConfiguration.sglStatus);
//                }
//
//                if (deviceConfiguration.hasSglDataRate()) {
//                    loraParamBuilder.setSglDatarate(deviceConfiguration.sglDatarate);
//                }
//
//                if (deviceConfiguration.hasSglFrequency()) {
//                    loraParamBuilder.setSglFrequency(deviceConfiguration.sglFrequency);
//                }
                List<Integer> channelList = deviceConfiguration.getChannelList();
                loraParamBuilder.addAllChannelMask(channelList);
                loraParamBuilder.setAdr(deviceConfiguration.getLoraAdr());
                loraParamBuilder.setDatarate(deviceConfiguration.getLoraDr());
                if (deviceConfiguration.hasActivation()) {
                    loraParamBuilder.setActivition(MsgNode1V1M5.Activtion.valueOf(deviceConfiguration.activation));
                }
                List<SensoroChannel> channels = deviceConfiguration.getChannels();
                for (int i = 0; i < channels.size(); i++) {
                    MsgNode1V1M5.Channel.Builder builder1 = MsgNode1V1M5.Channel.newBuilder();
                    SensoroChannel sensoroChannel = channels.get(i);
                    builder1.setFrequency(sensoroChannel.frequency);
                    builder1.setRx1Frequency(sensoroChannel.rx1Frequency);
//                    loraParamBuilder.setChannels(i,builder1);
                    loraParamBuilder.addChannels(builder1);
                }

                builder.setLpwanParam(loraParamBuilder);
                byte[] data = builder.build().toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(5, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            default:
                break;
        }


    }

    public void writeModuleConfiguration(SensoroDeviceConfiguration deviceConfiguration, final SensoroWriteCallback
            writeCallback) throws InvalidProtocolBufferException {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
        msgCfgBuilder.setLoraTxp(deviceConfiguration.loraTxp);
        ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
        ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
        msgStdBuilder.setCustomData(msgCfg.toByteString());
        ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();
        byte[] data = msgStd.toByteArray();
        int data_length = data.length;

        int total_length = data_length + 3;

        byte[] total_data = new byte[total_length];

        byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
        System.arraycopy(length_data, 0, total_data, 0, 2);
        byte[] version_data = SensoroUUID.intToByteArray(4, 1);
        System.arraycopy(version_data, 0, total_data, 2, 1);
        System.arraycopy(data, 0, total_data, 3, data_length);
        final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
        if (resultCode != ResultCode.SUCCESS) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                }
            });

        }
    }

    public void writeData05Configuration(SensoroDeviceConfiguration sensoroDeviceConfiguration, final SensoroWriteCallback
            writeCallback) throws InvalidProtocolBufferException {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        SensoroSensorConfiguration sensorConfiguration = sensoroDeviceConfiguration.getSensorConfiguration();
        if (sensorConfiguration.hasCh4()) {
            MsgNode1V1M5.SensorData.Builder ch4Builder = MsgNode1V1M5.SensorData.newBuilder();
            ch4Builder.setAlarmHigh(sensorConfiguration.getCh4AlarmHigh());
            ch4Builder.setData(sensorConfiguration.getCh4Data());
            msgNodeBuilder.setCh4(ch4Builder);
        }
        if (sensorConfiguration.hasCo()) {
            MsgNode1V1M5.SensorData.Builder coBuilder = MsgNode1V1M5.SensorData.newBuilder();
            coBuilder.setAlarmHigh(sensorConfiguration.getCoAlarmHigh());
            coBuilder.setData(sensorConfiguration.getCoData());
            msgNodeBuilder.setCo(coBuilder);
        }

        if (sensorConfiguration.hasCo2()) {
            MsgNode1V1M5.SensorData.Builder co2Builder = MsgNode1V1M5.SensorData.newBuilder();
            co2Builder.setAlarmHigh(sensorConfiguration.getCo2AlarmHigh());
            co2Builder.setData(sensorConfiguration.getCo2Data());
            msgNodeBuilder.setCo2(co2Builder);
        }
        if (sensorConfiguration.hasNo2()) {
            MsgNode1V1M5.SensorData.Builder no2Builder = MsgNode1V1M5.SensorData.newBuilder();
            no2Builder.setAlarmHigh(sensorConfiguration.getNo2AlarmHigh());
            no2Builder.setData(sensorConfiguration.getNo2Data());
            msgNodeBuilder.setNo2(no2Builder);
        }
        if (sensorConfiguration.hasLpg()) {
            MsgNode1V1M5.SensorData.Builder lpgBuilder = MsgNode1V1M5.SensorData.newBuilder();
            lpgBuilder.setAlarmHigh(sensorConfiguration.getLpgAlarmHigh());
            lpgBuilder.setData(sensorConfiguration.getLpgData());
            msgNodeBuilder.setLpg(lpgBuilder);
        }

        if (sensorConfiguration.hasPm10()) {
            MsgNode1V1M5.SensorData.Builder pm10Builder = MsgNode1V1M5.SensorData.newBuilder();
            pm10Builder.setAlarmHigh(sensorConfiguration.getPm10AlarmHigh());
            pm10Builder.setData(sensorConfiguration.getPm10Data());
            msgNodeBuilder.setPm10(pm10Builder);
        }
        if (sensorConfiguration.hasPm25()) {
            MsgNode1V1M5.SensorData.Builder pm25Builder = MsgNode1V1M5.SensorData.newBuilder();
            pm25Builder.setAlarmHigh(sensorConfiguration.getPm25AlarmHigh());
            pm25Builder.setData(sensorConfiguration.getPm25Data());
            msgNodeBuilder.setPm25(pm25Builder);
        }
        if (sensorConfiguration.hasTemperature()) {
            MsgNode1V1M5.SensorData.Builder tempBuilder = MsgNode1V1M5.SensorData.newBuilder();
            tempBuilder.setAlarmHigh(sensorConfiguration.getTempAlarmHigh());
            tempBuilder.setAlarmLow(sensorConfiguration.getTempAlarmLow());
            msgNodeBuilder.setTemperature(tempBuilder);
        }
        if (sensorConfiguration.hasHumidity()) {
            MsgNode1V1M5.SensorData.Builder humidityBuilder = MsgNode1V1M5.SensorData.newBuilder();
            humidityBuilder.setAlarmHigh(sensorConfiguration.getHumidityHigh());
            humidityBuilder.setAlarmLow(sensorConfiguration.getHumidityLow());
            msgNodeBuilder.setHumidity(humidityBuilder);
        }
        if (sensorConfiguration.hasPitchAngle()) {
            MsgNode1V1M5.SensorData.Builder pitchBuilder = MsgNode1V1M5.SensorData.newBuilder();
            pitchBuilder.setAlarmHigh(sensorConfiguration.getPitchAngleAlarmHigh());
            pitchBuilder.setAlarmLow(sensorConfiguration.getPitchAngleAlarmLow());
            msgNodeBuilder.setPitch(pitchBuilder);
        }
        if (sensorConfiguration.hasRollAngle()) {
            MsgNode1V1M5.SensorData.Builder rollAngleBuilder = MsgNode1V1M5.SensorData.newBuilder();
            rollAngleBuilder.setAlarmHigh(sensorConfiguration.getRollAngleAlarmHigh());
            rollAngleBuilder.setAlarmLow(sensorConfiguration.getRollAngleAlarmLow());
            msgNodeBuilder.setRoll(rollAngleBuilder);
        }
        if (sensorConfiguration.hasYawAngle()) {
            MsgNode1V1M5.SensorData.Builder yawAngleBuilder = MsgNode1V1M5.SensorData.newBuilder();
            yawAngleBuilder.setAlarmHigh(sensorConfiguration.getYawAngleAlarmHigh());
            yawAngleBuilder.setAlarmLow(sensorConfiguration.getYawAngleAlarmLow());
            msgNodeBuilder.setYaw(yawAngleBuilder);
        }
        if (sensorConfiguration.hasWaterPressure()) {
            MsgNode1V1M5.SensorData.Builder waterPressureBuilder = MsgNode1V1M5.SensorData.newBuilder();
            waterPressureBuilder.setAlarmHigh(sensorConfiguration.getWaterPressureAlarmHigh());
            waterPressureBuilder.setAlarmLow(sensorConfiguration.getWaterPressureAlarmLow());
            msgNodeBuilder.setWaterPressure(waterPressureBuilder);
        }
        if (sensoroDeviceConfiguration.hasAppParam()) {
            MsgNode1V1M5.AppParam.Builder appBuilder = MsgNode1V1M5.AppParam.newBuilder();
            if (sensoroDeviceConfiguration.hasUploadInterval()) {
                appBuilder.setUploadInterval(sensoroDeviceConfiguration.getUploadIntervalData());
            }

            if (sensoroDeviceConfiguration.hasConfirm()) {
                appBuilder.setConfirm(sensoroDeviceConfiguration.getConfirmData());
            }
            msgNodeBuilder.setAppParam(appBuilder);
        }
        //添加单通道温度传感器支持
        if (sensoroDeviceConfiguration.hasMultiTemperature) {
            MsgNode1V1M5.MultiSensorDataInt.Builder builder = MsgNode1V1M5.MultiSensorDataInt.newBuilder();
            if (sensoroDeviceConfiguration.hasAlarmHigh) {
                builder.setAlarmHigh(sensoroDeviceConfiguration.alarmHigh);
            }
            if (sensoroDeviceConfiguration.hasAlarmLow) {
                builder.setAlarmLow(sensoroDeviceConfiguration.alarmLow);
            }
            if (sensoroDeviceConfiguration.hasAlarmStepHigh) {
                builder.setAlarmStepHigh(sensoroDeviceConfiguration.alarmStepHigh);
            }
            if (sensoroDeviceConfiguration.hasAlarmStepLow) {
                builder.setAlarmStepLow(sensoroDeviceConfiguration.alarmStepLow);
            }
            msgNodeBuilder.setMultiTemp(builder);
        }
        MsgNode1V1M5.LpwanParam.Builder loraBuilder = MsgNode1V1M5.LpwanParam.newBuilder();
        loraBuilder.setTxPower(sensoroDeviceConfiguration.getLoraTxp());
//        loraBuilder.setMaxEIRP(sensoroDeviceConfiguration.getLoraEirp());
//        loraBuilder.setSglStatus(sensoroDeviceConfiguration.getSglStatus());
//        loraBuilder.setSglFrequency(sensoroDeviceConfiguration.getSglFrequency());
//        loraBuilder.setSglDatarate(sensoroDeviceConfiguration.getSglDatarate());

        MsgNode1V1M5.BleParam.Builder bleBuilder = MsgNode1V1M5.BleParam.newBuilder();
        bleBuilder.setBleInterval(sensoroDeviceConfiguration.getBleInt());
        bleBuilder.setBleOffTime(sensoroDeviceConfiguration.getBleTurnOffTime());
        bleBuilder.setBleOnTime(sensoroDeviceConfiguration.getBleTurnOnTime());
        bleBuilder.setBleTxp(sensoroDeviceConfiguration.getBleTxp());
        msgNodeBuilder.setBleParam(bleBuilder);
        msgNodeBuilder.setLpwanParam(loraBuilder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        int data_length = data.length;

        int total_length = data_length + 3;

        byte[] total_data = new byte[total_length];

        byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
        System.arraycopy(length_data, 0, total_data, 0, 2);
        byte[] version_data = SensoroUUID.intToByteArray(5, 1);
        System.arraycopy(version_data, 0, total_data, 2, 1);
        System.arraycopy(data, 0, total_data, 3, data_length);
        final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
        if (resultCode != ResultCode.SUCCESS) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                }
            });

        }
    }

    public void writeData05Configuration(SensoroDevice sensoroDevice, final SensoroWriteCallback
            writeCallback) {
        LogUtils.loge("writeData05Configuration，开始写数据");
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        SensoroSensor sensoroSensorTest = sensoroDevice.getSensoroSensorTest();
        if (sensoroSensorTest.hasCh4) {
            MsgNode1V1M5.SensorData.Builder ch4Builder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.ch4.has_data) {
                ch4Builder.setData(sensoroSensorTest.ch4.data_float);
            }
            if (sensoroSensorTest.ch4.has_alarmHigh) {
                ch4Builder.setAlarmHigh(sensoroSensorTest.ch4.alarmHigh_float);
            }
            msgNodeBuilder.setCh4(ch4Builder);
        }
        if (sensoroSensorTest.hasCo) {
            MsgNode1V1M5.SensorData.Builder coBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.co.has_data) {
                coBuilder.setData(sensoroSensorTest.co.data_float);
            }
            if (sensoroSensorTest.co.has_alarmHigh) {
                coBuilder.setAlarmHigh(sensoroSensorTest.co.alarmHigh_float);
            }
            msgNodeBuilder.setCo(coBuilder);
        }

        if (sensoroSensorTest.hasCo2) {
            MsgNode1V1M5.SensorData.Builder co2Builder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.co2.has_data) {
                co2Builder.setData(sensoroSensorTest.co2.data_float);
            }
            if (sensoroSensorTest.co2.has_alarmHigh) {
                co2Builder.setAlarmHigh(sensoroSensorTest.co2.alarmHigh_float);
            }
            msgNodeBuilder.setCo2(co2Builder);
        }
        if (sensoroSensorTest.hasNo2) {
            MsgNode1V1M5.SensorData.Builder no2Builder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.no2.has_data) {
                no2Builder.setData(sensoroSensorTest.no2.data_float);
            }
            if (sensoroSensorTest.no2.has_alarmHigh) {
                no2Builder.setAlarmHigh(sensoroSensorTest.no2.alarmHigh_float);
            }
            msgNodeBuilder.setNo2(no2Builder);
        }

        if (sensoroSensorTest.hasLpg) {
            MsgNode1V1M5.SensorData.Builder lpgBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.lpg.has_data) {
                lpgBuilder.setData(sensoroSensorTest.lpg.data_float);
            }
            if (sensoroSensorTest.lpg.has_alarmHigh) {
                lpgBuilder.setAlarmHigh(sensoroSensorTest.lpg.alarmHigh_float);
            }
            msgNodeBuilder.setLpg(lpgBuilder);
        }

        if (sensoroSensorTest.hasPm10) {
            MsgNode1V1M5.SensorData.Builder pm10Builder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.pm10.has_data) {
                pm10Builder.setData(sensoroSensorTest.pm10.data_float);
            }
            if (sensoroSensorTest.pm10.has_alarmHigh) {
                pm10Builder.setAlarmHigh(sensoroSensorTest.pm10.alarmHigh_float);
            }
            msgNodeBuilder.setPm10(pm10Builder);
        }
        if (sensoroSensorTest.hasPm25) {
            MsgNode1V1M5.SensorData.Builder pm25Builder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.pm25.has_data) {
                pm25Builder.setData(sensoroSensorTest.pm25.data_float);
            }
            if (sensoroSensorTest.pm25.has_alarmHigh) {
                pm25Builder.setAlarmHigh(sensoroSensorTest.pm25.alarmHigh_float);
            }
            msgNodeBuilder.setPm25(pm25Builder);
        }
        if (sensoroSensorTest.hasTemperature) {
            MsgNode1V1M5.SensorData.Builder tempBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.temperature.has_data) {
                tempBuilder.setData(sensoroSensorTest.temperature.data_float);
            }
            if (sensoroSensorTest.temperature.has_alarmHigh) {
                tempBuilder.setAlarmHigh(sensoroSensorTest.temperature.alarmHigh_float);
            }
            if (sensoroSensorTest.temperature.has_alarmLow) {
                tempBuilder.setAlarmLow(sensoroSensorTest.temperature.alarmLow_float);
            }
            msgNodeBuilder.setTemperature(tempBuilder);
        }
        if (sensoroSensorTest.hasHumidity) {
            MsgNode1V1M5.SensorData.Builder humidityBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.humidity.has_data) {
                humidityBuilder.setData(sensoroSensorTest.humidity.data_float);
            }
            if (sensoroSensorTest.humidity.has_alarmHigh) {
                humidityBuilder.setAlarmHigh(sensoroSensorTest.humidity.alarmHigh_float);
            }
            if (sensoroSensorTest.humidity.has_alarmLow) {
                humidityBuilder.setAlarmLow(sensoroSensorTest.humidity.alarmLow_float);
            }
            msgNodeBuilder.setHumidity(humidityBuilder);
        }
        if (sensoroSensorTest.hasPitch) {
            MsgNode1V1M5.SensorData.Builder pitchBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.pitch.has_data) {
                pitchBuilder.setData(sensoroSensorTest.pitch.data_float);
            }
            if (sensoroSensorTest.pitch.has_alarmHigh) {
                pitchBuilder.setAlarmHigh(sensoroSensorTest.pitch.alarmHigh_float);
            }
            if (sensoroSensorTest.pitch.has_alarmLow) {
                pitchBuilder.setAlarmLow(sensoroSensorTest.pitch.alarmLow_float);
            }
            msgNodeBuilder.setPitch(pitchBuilder);
        }
        if (sensoroSensorTest.hasRoll) {
            MsgNode1V1M5.SensorData.Builder rollAngleBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.roll.has_data) {
                rollAngleBuilder.setData(sensoroSensorTest.roll.data_float);
            }
            if (sensoroSensorTest.roll.has_alarmHigh) {
                rollAngleBuilder.setAlarmHigh(sensoroSensorTest.roll.alarmHigh_float);
            }
            if (sensoroSensorTest.roll.has_alarmLow) {
                rollAngleBuilder.setAlarmLow(sensoroSensorTest.roll.alarmLow_float);
            }
            msgNodeBuilder.setRoll(rollAngleBuilder);
        }
        if (sensoroSensorTest.hasYaw) {
            MsgNode1V1M5.SensorData.Builder yawAngleBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.yaw.has_data) {
                yawAngleBuilder.setData(sensoroSensorTest.yaw.data_float);
            }
            if (sensoroSensorTest.yaw.has_alarmHigh) {
                yawAngleBuilder.setAlarmHigh(sensoroSensorTest.yaw.alarmHigh_float);
            }
            if (sensoroSensorTest.yaw.has_alarmLow) {
                yawAngleBuilder.setAlarmLow(sensoroSensorTest.yaw.alarmLow_float);
            }
            msgNodeBuilder.setYaw(yawAngleBuilder);
        }
        if (sensoroSensorTest.hasWaterPressure) {
            MsgNode1V1M5.SensorData.Builder waterPressureBuilder = MsgNode1V1M5.SensorData.newBuilder();
            if (sensoroSensorTest.waterPressure.has_data) {
                waterPressureBuilder.setData(sensoroSensorTest.waterPressure.data_float);
            }
            if (sensoroSensorTest.waterPressure.has_alarmHigh) {
                waterPressureBuilder.setAlarmHigh(sensoroSensorTest.waterPressure.alarmHigh_float);
            }
            if (sensoroSensorTest.waterPressure.has_alarmLow) {
                waterPressureBuilder.setAlarmLow(sensoroSensorTest.waterPressure.alarmLow_float);
            }
            msgNodeBuilder.setWaterPressure(waterPressureBuilder);
        }
        //添加单通道温度传感器支持
        if (sensoroSensorTest.hasMultiTemp) {
            MsgNode1V1M5.MultiSensorDataInt.Builder builder = MsgNode1V1M5.MultiSensorDataInt.newBuilder();
            if (sensoroSensorTest.multiTemperature.has_alarmHigh) {
                builder.setAlarmHigh(sensoroSensorTest.multiTemperature.alarmHigh_int);
            }
            if (sensoroSensorTest.multiTemperature.has_alarmLow) {
                builder.setAlarmLow(sensoroSensorTest.multiTemperature.alarmLow_int);
            }
            if (sensoroSensorTest.multiTemperature.has_alarmStepHigh) {
                builder.setAlarmStepHigh(sensoroSensorTest.multiTemperature.alarmStepHigh_int);
            }
            if (sensoroSensorTest.multiTemperature.has_alarmStepLow) {
                builder.setAlarmStepLow(sensoroSensorTest.multiTemperature.alarmStepLow_int);
            }
            msgNodeBuilder.setMultiTemp(builder);
        }
        //电表支持
        if (sensoroSensorTest.hasFireData) {
            MsgNode1V1M5.ElecFireData.Builder builder = MsgNode1V1M5.ElecFireData.newBuilder();
            if (sensoroSensorTest.elecFireData.hasSensorPwd) {
                builder.setSensorPwd(sensoroSensorTest.elecFireData.sensorPwd);
            }
            if (sensoroSensorTest.elecFireData.hasLeakageTh) {
                builder.setLeakageTh(sensoroSensorTest.elecFireData.leakageTh);
            }
            if (sensoroSensorTest.elecFireData.hasTempTh) {
                builder.setTempTh(sensoroSensorTest.elecFireData.tempTh);
            }
            if (sensoroSensorTest.elecFireData.hasCurrentTh) {
                builder.setCurrentTh(sensoroSensorTest.elecFireData.currentTh);
            }
            if (sensoroSensorTest.elecFireData.hasLoadTh) {
                builder.setLoadTh(sensoroSensorTest.elecFireData.loadTh);
            }
            if (sensoroSensorTest.elecFireData.hasVolHighTh) {
                builder.setVolHighTh(sensoroSensorTest.elecFireData.volHighTh);
            }
            if (sensoroSensorTest.elecFireData.hasVolLowTh) {
                builder.setVolLowTh(sensoroSensorTest.elecFireData.volLowTh);
            }
            if (sensoroSensorTest.elecFireData.hasCmd) {
                builder.setCmd(sensoroSensorTest.elecFireData.cmd);
            }
            msgNodeBuilder.setFireData(builder);
        }
        //曼顿电气火灾传感器支持
        if (sensoroSensorTest.mantunDatas != null && sensoroSensorTest.mantunDatas.size() > 0) {
            for (SensoroMantunData mantunData : sensoroSensorTest.mantunDatas) {
                MsgNode1V1M5.MantunData.Builder builder = MsgNode1V1M5.MantunData.newBuilder();
                if (mantunData.hasVolVal) {
                    builder.setVolVal(mantunData.volVal);
                }
                if (mantunData.hasCurrVal) {
                    builder.setCurrVal(mantunData.currVal);
                }
                if (mantunData.hasLeakageVal) {
                    builder.setLeakageVal(mantunData.leakageVal);
                }
                if (mantunData.hasPowerVal) {
                    builder.setPowerVal(mantunData.powerVal);
                }
                if (mantunData.hasKwhVal) {
                    builder.setKwhVal(mantunData.kwhVal);
                }
                if (mantunData.hasTempVal) {
                    builder.setTempVal(mantunData.tempVal);
                }
                if (mantunData.hasStatus) {
                    builder.setStatus(mantunData.status);
                }
                if (mantunData.hasSwOnOff) {
                    builder.setSwOnOff(mantunData.swOnOff);
                }
                if (mantunData.hasId) {
                    builder.setId(mantunData.id);
                }
                if (mantunData.hasVolHighTh) {
                    builder.setVolHighTh(mantunData.volHighTh);
                }
                if (mantunData.hasVolLowTh) {
                    builder.setVolLowTh(mantunData.volLowTh);
                }
                if (mantunData.hasLeakageTh) {
                    builder.setLeakageTh(mantunData.leakageTh);
                }
                if (mantunData.hasTempTh) {
                    builder.setTempTh(mantunData.tempTh);
                }
                if (mantunData.hasCurrentTh) {
                    builder.setCurrentTh(mantunData.currentTh);
                }
                if (mantunData.hasPowerTh) {
                    builder.setPowerTh(mantunData.powerTh);
                }

                if (mantunData.hasAttribute) {
                    builder.setAttribute(mantunData.attribute);
                }
                if (mantunData.hasCmd) {
                    builder.setCmd(mantunData.cmd);
                }

                msgNodeBuilder.addMtunData(builder);
            }
        }

        //安科瑞三相电
        if (sensoroSensorTest.hasAcrelFires) {
            MsgNode1V1M5.AcrelData.Builder builder = MsgNode1V1M5.AcrelData.newBuilder();
            if (sensoroSensorTest.acrelFires.hasConnectSw) {
                builder.setConnectSw(sensoroSensorTest.acrelFires.connectSw);
            }
            if (sensoroSensorTest.acrelFires.hasChEnable) {
                builder.setChEnable(sensoroSensorTest.acrelFires.chEnable);
            }
            if (sensoroSensorTest.acrelFires.hasLeakageTh) {
                builder.setLeakageTh(sensoroSensorTest.acrelFires.leakageTh);
            }
            if (sensoroSensorTest.acrelFires.hasPasswd) {
                builder.setPasswd(sensoroSensorTest.acrelFires.passwd);
            }
            if (sensoroSensorTest.acrelFires.hasT1Th) {
                int t1Th = sensoroSensorTest.acrelFires.t1Th;
                builder.setT1Th(sensoroSensorTest.acrelFires.t1Th);
            }
            if (sensoroSensorTest.acrelFires.hasT2Th) {
                builder.setT2Th(sensoroSensorTest.acrelFires.t2Th);
            }
            if (sensoroSensorTest.acrelFires.hasT3Th) {
                builder.setT3Th(sensoroSensorTest.acrelFires.t3Th);
            }
            if (sensoroSensorTest.acrelFires.hasT4Th) {
                builder.setT4Th(sensoroSensorTest.acrelFires.t4Th);
            }
            if (sensoroSensorTest.acrelFires.hasPasswd) {
                builder.setPasswd(sensoroSensorTest.acrelFires.passwd);
            }
            if (sensoroSensorTest.acrelFires.hasValHighSet) {
                builder.setValHighSet(sensoroSensorTest.acrelFires.valHighSet);
            }
            if (sensoroSensorTest.acrelFires.hasValLowSet) {
                builder.setValLowSet(sensoroSensorTest.acrelFires.valLowSet);
            }
            if (sensoroSensorTest.acrelFires.hasCurrHighSet) {
                builder.setCurrHighSet(sensoroSensorTest.acrelFires.currHighSet);
            }

            if (sensoroSensorTest.acrelFires.hasValHighType) {
                builder.setValHighType(sensoroSensorTest.acrelFires.valHighType);
            }
            if (sensoroSensorTest.acrelFires.hasValLowType) {
                builder.setValLowType(sensoroSensorTest.acrelFires.valLowType);
            }
            if (sensoroSensorTest.acrelFires.hasCurrHighType) {
                builder.setCurrHighType(sensoroSensorTest.acrelFires.currHighType);
            }
            if (sensoroSensorTest.acrelFires.hasIct) {
                builder.setIct(sensoroSensorTest.acrelFires.ict);
            }
            if (sensoroSensorTest.acrelFires.hasCt) {
                builder.setCt(sensoroSensorTest.acrelFires.ct);
            }
//            if (sensoroSensorTest.acrelFires.hasCmd) {
            builder.setCmd(sensoroSensorTest.acrelFires.cmd);
//            }
            msgNodeBuilder.setAcrelData(builder);

        }

        //嘉德 自研烟感
        if (sensoroSensorTest.hasCayMan) {
            MsgNode1V1M5.Cayman.Builder builder = MsgNode1V1M5.Cayman.newBuilder();
            if (sensoroSensorTest.cayManData.hasIsSmoke) {
                builder.setIsSmoke(sensoroSensorTest.cayManData.isSmoke);
            }
            if (sensoroSensorTest.cayManData.hasIsMoved) {
                builder.setIsMoved(sensoroSensorTest.cayManData.isMoved);
            }
            if (sensoroSensorTest.cayManData.hasValueOfTem) {
                builder.setValueOfTem(sensoroSensorTest.cayManData.valueOfTem);
            }
            if (sensoroSensorTest.cayManData.hasValueOfHum) {
                builder.setValueOfHum(sensoroSensorTest.cayManData.valueOfHum);
            }
            if (sensoroSensorTest.cayManData.hasAlarmOfHighTem) {
                builder.setAlarmOfHighTem(sensoroSensorTest.cayManData.alarmOfHighTem);
            }
            if (sensoroSensorTest.cayManData.hasAlarmOfLowTem) {
                builder.setAlarmOfLowTem(sensoroSensorTest.cayManData.alarmOfLowTem);
            }
            if (sensoroSensorTest.cayManData.hasAlarmOfHighHum) {
                builder.setAlarmOfHighHum(sensoroSensorTest.cayManData.alarmOfHighHum);
            }
            if (sensoroSensorTest.cayManData.hasAlarmOfLowHum) {
                builder.setAlarmOfLowHum(sensoroSensorTest.cayManData.alarmOfLowHum);
            }
            if (sensoroSensorTest.cayManData.hasCmd) {
                builder.setCmd(sensoroSensorTest.cayManData.cmd);
            }
            msgNodeBuilder.setCaymanData(builder);
        }

        //baymax ch4 lpg
        if (sensoroSensorTest.hasBaymax) {
            MsgNode1V1M5.Baymax.Builder builder = MsgNode1V1M5.Baymax.newBuilder();
            if (sensoroSensorTest.baymax.hasGasDensity) {
                builder.setGasDensity(sensoroSensorTest.baymax.gasDensity);
            }
            if (sensoroSensorTest.baymax.hasGasDensityL1) {
                builder.setGasDensityL1(sensoroSensorTest.baymax.gasDensityL1);
            }
            if (sensoroSensorTest.baymax.hasGasDensityL2) {
                builder.setGasDensityL2(sensoroSensorTest.baymax.gasDensityL2);
            }
            if (sensoroSensorTest.baymax.hasGasDensityL3) {
                builder.setGasDensityL3(sensoroSensorTest.baymax.gasDensityL3);
            }
            if (sensoroSensorTest.baymax.hasGasDisassembly) {
                builder.setGasDisassembly(sensoroSensorTest.baymax.gasDisassembly);
            }
            if (sensoroSensorTest.baymax.hasGasLosePwr) {
                builder.setGasLosePwr(sensoroSensorTest.baymax.gasLosePwr);
            }
            if (sensoroSensorTest.baymax.hasGasEMValve) {
                builder.setGasEMValve(sensoroSensorTest.baymax.gasEMValve);
            }
            if (sensoroSensorTest.baymax.hasGasDeviceStatus) {
                builder.setGasDeviceStatus(sensoroSensorTest.baymax.gasDeviceStatus);
            }
            if (sensoroSensorTest.baymax.hasGasDeviceOpState) {
                builder.setGasDeviceOpState(sensoroSensorTest.baymax.gasDeviceOpState);
            }
            if (sensoroSensorTest.baymax.hasGasDeviceComsDown) {
                builder.setGasDeviceComsDown(sensoroSensorTest.baymax.gasDeviceComsDown);
            }
            if (sensoroSensorTest.baymax.hasGasDeviceCMD) {
                builder.setGasDeviceCMD(sensoroSensorTest.baymax.gasDeviceCMD);
            }
            msgNodeBuilder.setBaymaxData(builder);
        }

        if (sensoroDevice.hasAppParam()) {
            MsgNode1V1M5.AppParam.Builder appBuilder = MsgNode1V1M5.AppParam.newBuilder();
            if (sensoroDevice.hasUploadInterval()) {
                appBuilder.setUploadInterval(sensoroDevice.getUploadInterval());
            }

            if (sensoroDevice.hasConfirm()) {
                appBuilder.setConfirm(sensoroDevice.getConfirm());
            }
            msgNodeBuilder.setAppParam(appBuilder);
        }


        MsgNode1V1M5.LpwanParam.Builder loraBuilder = MsgNode1V1M5.LpwanParam.newBuilder();
        loraBuilder.setTxPower(sensoroDevice.getLoraTxp());
//        loraBuilder.setMaxEIRP(sensoroDeviceConfiguration.getLoraEirp());
//        loraBuilder.setSglStatus(sensoroDeviceConfiguration.getSglStatus());
//        loraBuilder.setSglFrequency(sensoroDeviceConfiguration.getSglFrequency());
//        loraBuilder.setSglDatarate(sensoroDeviceConfiguration.getSglDatarate());

        MsgNode1V1M5.BleParam.Builder bleBuilder = MsgNode1V1M5.BleParam.newBuilder();
        bleBuilder.setBleInterval(sensoroDevice.getBleInt());
        bleBuilder.setBleOffTime(sensoroDevice.getBleOffTime());
        bleBuilder.setBleOnTime(sensoroDevice.getBleOnTime());
        bleBuilder.setBleTxp(sensoroDevice.getBleTxp());
        msgNodeBuilder.setBleParam(bleBuilder);
        msgNodeBuilder.setLpwanParam(loraBuilder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        int data_length = data.length;

        int total_length = data_length + 3;

        byte[] total_data = new byte[total_length];

        byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
        System.arraycopy(length_data, 0, total_data, 0, 2);
        byte[] version_data = SensoroUUID.intToByteArray(5, 1);
        System.arraycopy(version_data, 0, total_data, 2, 1);
        System.arraycopy(data, 0, total_data, 3, data_length);
        final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
        if (resultCode != ResultCode.SUCCESS) {
            LogUtils.loge("写数据失败 writeData05Configuration");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                }
            });

        }
        LogUtils.loge("写入到最后一步");

    }

    public void writeSmokeCmd(MsgNode1V1M5.AppParam.Builder builder, SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SET_SMOKE, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        msgNodeBuilder.setAppParam(builder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        writeData05Cmd(data, CmdType.CMD_SET_SMOKE, writeCallback);
    }

    /**
     * 写入电表命令
     *
     * @param builder
     * @param writeCallback
     */
    public void writeElecCmd(MsgNode1V1M5.ElecFireData.Builder builder, SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SET_ELEC_CMD, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        msgNodeBuilder.setFireData(builder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        writeData05Cmd(data, CmdType.CMD_SET_ELEC_CMD, writeCallback);
    }

    public void writeZeroCmd(SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SET_ZERO, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        MsgNode1V1M5.SensorData.Builder sensorDataBuilder = MsgNode1V1M5.SensorData.newBuilder();
        sensorDataBuilder.setCalibration(1);
        msgNodeBuilder.setPitch(sensorDataBuilder.build());
        msgNodeBuilder.setRoll(sensorDataBuilder.build());
        msgNodeBuilder.setYaw(sensorDataBuilder.build());
        byte[] data = msgNodeBuilder.build().toByteArray();
        writeData05Cmd(data, CmdType.CMD_SET_ZERO, writeCallback);
    }

    private void writeData05Cmd(byte data[], int cmdType, final SensoroWriteCallback writeCallback) {
        int data_length = data.length;

        int total_length = data_length + 3;

        byte[] total_data = new byte[total_length];

        byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
        System.arraycopy(length_data, 0, total_data, 0, 2);
        byte[] version_data = SensoroUUID.intToByteArray(5, 1);
        System.arraycopy(version_data, 0, total_data, 2, 1);
        System.arraycopy(data, 0, total_data, 3, data_length);
        final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, cmdType, BluetoothLEHelper4.GattInfo
                .SENSORO_DEVICE_WRITE_CHAR_UUID);
        if (resultCode != ResultCode.SUCCESS) {
            LogUtils.loge("writeData05Cmd 失败");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                }
            });

        }
    }

    public void writeDataConfiguration(SensoroDeviceConfiguration deviceConfiguration, final SensoroWriteCallback
            writeCallback) throws InvalidProtocolBufferException {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
        msgCfgBuilder.setLoraInt(deviceConfiguration.loraInt.intValue());
        msgCfgBuilder.setLoraTxp(deviceConfiguration.loraTxp);

        msgCfgBuilder.setBleTxp(deviceConfiguration.bleTxp);
        msgCfgBuilder.setBleInt(deviceConfiguration.bleInt.intValue());
        msgCfgBuilder.setBleOnTime(deviceConfiguration.bleTurnOnTime);
        msgCfgBuilder.setBleOffTime(deviceConfiguration.bleTurnOffTime);

        SensoroSlot[] sensoroSlots = deviceConfiguration.sensoroSlots;
        for (int i = 0; i < sensoroSlots.length; i++) {
            ProtoMsgCfgV1U1.Slot.Builder builder = ProtoMsgCfgV1U1.Slot.newBuilder();
            SensoroSlot sensoroSlot = sensoroSlots[i];
            if (sensoroSlot.isActived() == 1) {
                switch (i) {
                    case 4:
                        byte uuid_data[] = SensoroUtils.HexString2Bytes(deviceConfiguration.proximityUUID);
                        byte major_data[] = SensoroUUID.intToByteArray(deviceConfiguration.major, 2);
                        byte minor_data[] = SensoroUUID.intToByteArray(deviceConfiguration.minor, 2);
                        byte ibeacon_data[] = new byte[20];
                        System.arraycopy(uuid_data, 0, ibeacon_data, 0, 16);
                        System.arraycopy(major_data, 0, ibeacon_data, 16, 2);
                        System.arraycopy(minor_data, 0, ibeacon_data, 18, 2);
                        builder.setFrame(ByteString.copyFrom(ibeacon_data));
                        break;
                    case 5:
                    case 6:
                    case 7:
                        String frameString = sensoroSlot.getFrame();
                        if (frameString != null) {
                            builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(frameString)));
                        }

                        break;
                    default:
                        switch (sensoroSlot.getType()) {
                            case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE:
                                builder.setFrame(ByteString.copyFrom(SensoroUtils.encodeUrl(sensoroSlot.getFrame())));
                                break;
                            default:
                                builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(sensoroSlot
                                        .getFrame())));
                                break;
                        }
                        break;
                }

            }
            builder.setIndex(i);
            builder.setType(ProtoMsgCfgV1U1.SlotType.valueOf(sensoroSlot.getType()));
            builder.setActived(sensoroSlot.isActived());
            msgCfgBuilder.addSlot(i, builder.build());
        }

        switch (dataVersion) {
            case DATA_VERSION_03: {
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();

                byte[] data = msgCfg.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
                System.arraycopy(length_data, 0, total_data, 0, 2);
                byte[] version_data = SensoroUUID.intToByteArray(3, 1);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);
                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            case DATA_VERSION_04: {
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
                ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
                msgStdBuilder.setCustomData(msgCfg.toByteString());
                ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();
                byte[] data = msgStd.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
                System.arraycopy(length_data, 0, total_data, 0, 2);
                byte[] version_data = SensoroUUID.intToByteArray(4, 1);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);
                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            default:
                break;
        }
    }

    public void writeMultiData05Configuration(SensoroDeviceConfiguration deviceConfiguration, final SensoroWriteCallback
            writeCallback) throws InvalidProtocolBufferException {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        if (deviceConfiguration.hasLoraParam()) {
            MsgNode1V1M5.LpwanParam.Builder loraParamBuilder = MsgNode1V1M5.LpwanParam.newBuilder();
            loraParamBuilder.setTxPower(deviceConfiguration.loraTxp);
            msgNodeBuilder.setLpwanParam(loraParamBuilder);
        }
        if (deviceConfiguration.hasBleParam()) {
            MsgNode1V1M5.BleParam.Builder bleParamBuilder = MsgNode1V1M5.BleParam.newBuilder();
            bleParamBuilder.setBleTxp(deviceConfiguration.bleTxp);
            bleParamBuilder.setBleInterval(deviceConfiguration.bleInt.intValue());
            bleParamBuilder.setBleOnTime(deviceConfiguration.bleTurnOnTime);
            bleParamBuilder.setBleOffTime(deviceConfiguration.bleTurnOffTime);
            msgNodeBuilder.setBleParam(bleParamBuilder);
        }

        SensoroSensorConfiguration sensorConfiguration = deviceConfiguration.getSensorConfiguration();
        if (sensorConfiguration.hasCh4()) {
            MsgNode1V1M5.SensorData.Builder ch4Builder = MsgNode1V1M5.SensorData.newBuilder();
            ch4Builder.setAlarmHigh(sensorConfiguration.getCh4AlarmHigh());
            ch4Builder.setData(sensorConfiguration.getCh4Data());
            msgNodeBuilder.setCh4(ch4Builder);
        }
        if (sensorConfiguration.hasCo()) {
            MsgNode1V1M5.SensorData.Builder coBuilder = MsgNode1V1M5.SensorData.newBuilder();
            coBuilder.setAlarmHigh(sensorConfiguration.getCoAlarmHigh());
            coBuilder.setData(sensorConfiguration.getCoData());
            msgNodeBuilder.setCo(coBuilder);
        }

        if (sensorConfiguration.hasCo2()) {
            MsgNode1V1M5.SensorData.Builder co2Builder = MsgNode1V1M5.SensorData.newBuilder();
            co2Builder.setAlarmHigh(sensorConfiguration.getCo2AlarmHigh());
            co2Builder.setData(sensorConfiguration.getCo2Data());
            msgNodeBuilder.setCo2(co2Builder);
        }
        if (sensorConfiguration.hasNo2()) {
            MsgNode1V1M5.SensorData.Builder no2Builder = MsgNode1V1M5.SensorData.newBuilder();
            no2Builder.setAlarmHigh(sensorConfiguration.getNo2AlarmHigh());
            no2Builder.setData(sensorConfiguration.getNo2Data());
            msgNodeBuilder.setNo2(no2Builder);
        }
        if (sensorConfiguration.hasLpg()) {
            MsgNode1V1M5.SensorData.Builder lpgBuilder = MsgNode1V1M5.SensorData.newBuilder();
            lpgBuilder.setAlarmHigh(sensorConfiguration.getLpgAlarmHigh());
            lpgBuilder.setData(sensorConfiguration.getLpgData());
            msgNodeBuilder.setLpg(lpgBuilder);
        }

        if (sensorConfiguration.hasPm10()) {
            MsgNode1V1M5.SensorData.Builder pm10Builder = MsgNode1V1M5.SensorData.newBuilder();
            pm10Builder.setAlarmHigh(sensorConfiguration.getPm10AlarmHigh());
            pm10Builder.setData(sensorConfiguration.getPm10Data());
            msgNodeBuilder.setPm10(pm10Builder);
        }
        if (sensorConfiguration.hasPm25()) {
            MsgNode1V1M5.SensorData.Builder pm25Builder = MsgNode1V1M5.SensorData.newBuilder();
            pm25Builder.setAlarmHigh(sensorConfiguration.getPm25AlarmHigh());
            pm25Builder.setData(sensorConfiguration.getPm25Data());
            msgNodeBuilder.setPm25(pm25Builder);
        }
        if (sensorConfiguration.hasTemperature()) {
            MsgNode1V1M5.SensorData.Builder tempBuilder = MsgNode1V1M5.SensorData.newBuilder();
            tempBuilder.setAlarmHigh(sensorConfiguration.getTempAlarmHigh());
            tempBuilder.setAlarmLow(sensorConfiguration.getTempAlarmLow());
            msgNodeBuilder.setTemperature(tempBuilder);
        }
        if (sensorConfiguration.hasHumidity()) {
            MsgNode1V1M5.SensorData.Builder humidityBuilder = MsgNode1V1M5.SensorData.newBuilder();
            humidityBuilder.setAlarmHigh(sensorConfiguration.getHumidityHigh());
            humidityBuilder.setAlarmLow(sensorConfiguration.getHumidityLow());
            msgNodeBuilder.setHumidity(humidityBuilder);
        }
        if (sensorConfiguration.hasWaterPressure()) {
            MsgNode1V1M5.SensorData.Builder waterPressureBuilder = MsgNode1V1M5.SensorData.newBuilder();
            waterPressureBuilder.setAlarmHigh(sensorConfiguration.getWaterPressureAlarmHigh());
            waterPressureBuilder.setAlarmLow(sensorConfiguration.getWaterPressureAlarmLow());
            msgNodeBuilder.setWaterPressure(waterPressureBuilder);
        }
        if (deviceConfiguration.hasAppParam()) {//BB65
            if (deviceConfiguration.hasUploadInterval()) {
                MsgNode1V1M5.AppParam.Builder appBuilder = MsgNode1V1M5.AppParam.newBuilder();
                appBuilder.setUploadInterval(deviceConfiguration.getUploadIntervalData());
                msgNodeBuilder.setAppParam(appBuilder);
            }

            if (deviceConfiguration.hasConfirm()) {
                MsgNode1V1M5.AppParam.Builder appBuilder = MsgNode1V1M5.AppParam.newBuilder();
                appBuilder.setConfirm(deviceConfiguration.getConfirmData());
                msgNodeBuilder.setAppParam(appBuilder);
            }
        }
        byte[] data = msgNodeBuilder.build().toByteArray();
        int data_length = data.length;

        int total_length = data_length + 3;

        byte[] total_data = new byte[total_length];

        byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
        System.arraycopy(length_data, 0, total_data, 0, 2);
        byte[] version_data = SensoroUUID.intToByteArray(5, 1);
        System.arraycopy(version_data, 0, total_data, 2, 1);
        System.arraycopy(data, 0, total_data, 3, data_length);
        final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
        if (resultCode != ResultCode.SUCCESS) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                }
            });

        }
    }

    public void writeMultiDataConfiguration(SensoroDeviceConfiguration deviceConfiguration, final SensoroWriteCallback
            writeCallback) throws InvalidProtocolBufferException {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();

        msgCfgBuilder.setLoraTxp(deviceConfiguration.loraTxp);
        msgCfgBuilder.setLoraInt(deviceConfiguration.loraInt.intValue());

        msgCfgBuilder.setBleTxp(deviceConfiguration.bleTxp);
        msgCfgBuilder.setBleInt(deviceConfiguration.bleInt.intValue());
        msgCfgBuilder.setBleOnTime(deviceConfiguration.bleTurnOnTime);
        msgCfgBuilder.setBleOffTime(deviceConfiguration.bleTurnOffTime);

        SensoroSlot[] sensoroSlots = deviceConfiguration.sensoroSlots;
        for (int i = 0; i < sensoroSlots.length; i++) {
            ProtoMsgCfgV1U1.Slot.Builder builder = ProtoMsgCfgV1U1.Slot.newBuilder();
            SensoroSlot sensoroSlot = sensoroSlots[i];
            if (sensoroSlot.isActived() == 1) {
                switch (i) {
                    case 4:
                        byte uuid_data[] = SensoroUtils.HexString2Bytes(deviceConfiguration.proximityUUID);
                        byte major_data[] = SensoroUUID.intToByteArray(deviceConfiguration.major, 2);
                        byte minor_data[] = SensoroUUID.intToByteArray(deviceConfiguration.minor, 2);
                        byte ibeacon_data[] = new byte[20];
                        System.arraycopy(uuid_data, 0, ibeacon_data, 0, 16);
                        System.arraycopy(major_data, 0, ibeacon_data, 16, 2);
                        System.arraycopy(minor_data, 0, ibeacon_data, 18, 2);
                        builder.setFrame(ByteString.copyFrom(ibeacon_data));
                        break;
                    case 5:
                    case 6:
                    case 7:
                        String frameString = sensoroSlot.getFrame();
                        if (frameString != null) {
                            builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(frameString)));
                        }

                        break;
                    default:
                        switch (sensoroSlot.getType()) {
                            case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE:
                                builder.setFrame(ByteString.copyFrom(SensoroUtils.encodeUrl(sensoroSlot.getFrame())));
                                break;
                            default:
                                builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(sensoroSlot
                                        .getFrame())));
                                break;
                        }
                        break;
                }

            }
            builder.setIndex(i);
            builder.setType(ProtoMsgCfgV1U1.SlotType.valueOf(sensoroSlot.getType()));
            builder.setActived(sensoroSlot.isActived());
            msgCfgBuilder.addSlot(i, builder.build());

        }

        switch (dataVersion) {
            case DATA_VERSION_03: {
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();

                byte[] data = msgCfg.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
                System.arraycopy(length_data, 0, total_data, 0, 2);
                byte[] version_data = SensoroUUID.intToByteArray(3, 1);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);
                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            case DATA_VERSION_04: {
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
                ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
                msgStdBuilder.setCustomData(msgCfg.toByteString());
                ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();
                byte[] data = msgStd.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);
                System.arraycopy(length_data, 0, total_data, 0, 2);
                byte[] version_data = SensoroUUID.intToByteArray(4, 1);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);
                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            default:
                break;
        }

    }

    public void writeUpgradeCmd(String filePath, int status, SensoroWriteCallback writeCallback) {
//        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        switch (status) {
            case 1:
                chipEUpgradeThread = new ChipEUpgradeThread(writeCallback, filePath, bluetoothLEHelper4);
                chipEUpgradeThread.start();
                break;
        }
    }

    public void writeUpgradeCmd(final SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        switch (dataVersion) {
            case DATA_VERSION_03: {
                ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
                msgCfgBuilder.setCmd(2);
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
                byte[] data = msgCfg.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(3, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(ResultCode.CODE_DEVICE_DFU_ERROR, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            case DATA_VERSION_04: {
                ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
                msgCfgBuilder.setCmd(2);
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();

                ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
                msgStdBuilder.setCustomData(msgCfg.toByteString());
                ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();
                byte[] data = msgStd.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(4, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(ResultCode.CODE_DEVICE_DFU_ERROR, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            case DATA_VERSION_05: {
                MsgNode1V1M5.MsgNode.Builder nodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
                MsgNode1V1M5.AppParam.Builder appBuilder = MsgNode1V1M5.AppParam.newBuilder();
                appBuilder.setCmd(MsgNode1V1M5.AppCmd.APP_CMD_DFU);
                nodeBuilder.setAppParam(appBuilder);
                byte[] data = nodeBuilder.build().toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(5, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(ResultCode.CODE_DEVICE_DFU_ERROR, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
            default:
                break;
        }
    }

    public void writeSignalData(int freq, int dr, int txPower, int interval, final SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SIGNAL, writeCallback);
        switch (dataVersion) {
            default: {
                ProtoMsgTest1U1.MsgTest.Builder builder = ProtoMsgTest1U1.MsgTest.newBuilder();
                builder.setUplinkFreq(freq);
//                builder.setUplinkDR(dr);
//                builder.setUplinkTxPower(txPower);
//                builder.setUplinkInterval(interval);
                ProtoMsgTest1U1.MsgTest msgTest = builder.build();
                byte[] data = msgTest.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                final byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(1, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);
                LogUtils.loge("信号测试发送");
                final int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_SIGNAL,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_SIGNAL_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onWriteFailure(resultCode, CmdType.CMD_NULL);
                        }
                    });

                }
            }
            break;
        }

    }

    /**
     * Disconnect from beacon.
     */
    public void disconnect() {
        taskHandler.removeCallbacksAndMessages(null);
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                freshCache();
                if (sensoroConnectionCallback != null) {
                    LogUtils.loge("sensoroDeviceConnectio 调用disconnect");
                    sensoroConnectionCallback.onDisconnected();
                }
            }
        });


    }

    public void freshCache() {
        trySleepThread(200);
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (bluetoothLEHelper4 != null) {
                    bluetoothLEHelper4.close();
                }
            }
        });
//        if (sensoroConnectionCallback != null) {
//            LogUtils.loge("失败 调用disconnect");
//            sensoroConnectionCallback.onDisconnected();
//        }

    }

    private void trySleepThread(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeMantunCmd(MsgNode1V1M5.MantunData.Builder builder, SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SET_MANTUN_CMD, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        msgNodeBuilder.addMtunData(builder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        writeData05Cmd(data, CmdType.CMD_SET_MANTUN_CMD, writeCallback);
    }

    public void writeAcrelCmd(MsgNode1V1M5.AcrelData.Builder builder, SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SET_ACREL_CMD, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        msgNodeBuilder.setAcrelData(builder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        writeData05Cmd(data, CmdType.CMD_SET_ACREL_CMD, writeCallback);
    }

    public void writeCaymanCmd(MsgNode1V1M5.Cayman.Builder builder, SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SET_CAYMAN_CMD, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        msgNodeBuilder.setCaymanData(builder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        writeData05Cmd(data, CmdType.CMD_SET_CAYMAN_CMD, writeCallback);
    }

    public void setOnSensoroDirectWriteDfuCallBack(SensoroDirectWriteDfuCallBack sensoroDirectWriteDfuCallBack) {
        this.sensoroDirectWriteDfuCallBack = sensoroDirectWriteDfuCallBack;
    }

    public void writeData05ChannelMask(List<Integer> channelMask, final SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        MsgNode1V1M5.MsgNode.Builder nodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        MsgNode1V1M5.LpwanParam.Builder loraParamBuild = MsgNode1V1M5.LpwanParam.newBuilder();
        loraParamBuild.addAllChannelMask(channelMask);
        nodeBuilder.setLpwanParam(loraParamBuild);
        byte[] data = nodeBuilder.build().toByteArray();
        int data_length = data.length;

        int total_length = data_length + 3;

        byte[] total_data = new byte[total_length];

        byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

        byte[] version_data = SensoroUUID.intToByteArray(5, 1);

        System.arraycopy(length_data, 0, total_data, 0, 2);
        System.arraycopy(version_data, 0, total_data, 2, 1);
        System.arraycopy(data, 0, total_data, 3, data_length);

        int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
        if (resultCode != ResultCode.SUCCESS) {
            LogUtils.loge("writeData05ChannelMask失败");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(ResultCode.CODE_DEVICE_DFU_ERROR, CmdType.CMD_NULL);
                }
            });

        }
    }

    public void writeBaymaxCmd(MsgNode1V1M5.Baymax.Builder builder, SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_SET_BAYMAX_CMD, writeCallback);
        MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        msgNodeBuilder.setBaymaxData(builder);
        byte[] data = msgNodeBuilder.build().toByteArray();
        writeData05Cmd(data, CmdType.CMD_SET_BAYMAX_CMD, writeCallback);
    }


    enum ListenType implements Serializable {
        SENSOR_CHAR, READ_CHAR, SIGNAL_CHAR, SENSOR_CHIP_E, UNKNOWN
    }
    public void writeCmd(SensoroWriteCallback writeCallback) {
        writeCallbackHashMap.put(CmdType.CMD_W_CFG, writeCallback);
        switch (dataVersion) {
            case DATA_VERSION_03: {
                ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
                msgCfgBuilder.setCmd(2);
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
                byte[] data = msgCfg.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(3, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    writeCallback.onWriteFailure(ResultCode.CODE_DEVICE_DFU_ERROR, CmdType.CMD_NULL);
                }
            }
            break;
            case DATA_VERSION_04: {
                ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
                msgCfgBuilder.setCmd(2);
                ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();

                ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
                msgStdBuilder.setCustomData(msgCfg.toByteString());
                ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();
                byte[] data = msgStd.toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(4, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    writeCallback.onWriteFailure(ResultCode.CODE_DEVICE_DFU_ERROR, CmdType.CMD_NULL);
                }
            }
            break;
            case DATA_VERSION_05: {
                MsgNode1V1M5.MsgNode.Builder nodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
                MsgNode1V1M5.AppParam.Builder appBuilder = MsgNode1V1M5.AppParam.newBuilder();
                appBuilder.setCmd(MsgNode1V1M5.AppCmd.APP_CMD_DFU);
                nodeBuilder.setAppParam(appBuilder);
                byte[] data = nodeBuilder.build().toByteArray();
                int data_length = data.length;

                int total_length = data_length + 3;

                byte[] total_data = new byte[total_length];

                byte[] length_data = SensoroUUID.intToByteArray(data_length + 1, 2);

                byte[] version_data = SensoroUUID.intToByteArray(5, 1);

                System.arraycopy(length_data, 0, total_data, 0, 2);
                System.arraycopy(version_data, 0, total_data, 2, 1);
                System.arraycopy(data, 0, total_data, 3, data_length);

                int resultCode = bluetoothLEHelper4.writeConfigurations(total_data, CmdType.CMD_W_CFG,
                        BluetoothLEHelper4.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
                if (resultCode != ResultCode.SUCCESS) {
                    writeCallback.onWriteFailure(ResultCode.CODE_DEVICE_DFU_ERROR, CmdType.CMD_NULL);
                }
            }
            break;
            default:
                break;
        }
    }
}