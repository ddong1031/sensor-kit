/*
 * Copyright (c) 2014. Sensoro Inc.
 * All rights reserved.
 */

package com.sensoro.libbleserver.ble.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.sensoro.libbleserver.ble.constants.ResultCode;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLEHelper implements Serializable {
    private static final long serialVersionUID = -3875124305428095694L;

    private Context context = null;
    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private String bluetoothDeviceAddress = null;
    public BluetoothGatt bluetoothGatt = null;

    private BluetoothGattService baseSettingsService = null;
    private BluetoothGattCharacteristic baseSettingsChar = null;
    private BluetoothGattService sensoSettingsService = null;
    private BluetoothGattCharacteristic sensoSettingsChar = null;
    private BluetoothGattCharacteristic dynamicMMChar = null;
    private BluetoothGattCharacteristic workModeChar = null;
    private BluetoothGattCharacteristic acceleratorChar = null;
    protected int sendPacketNumber = 1;
    protected ArrayList<byte[]> writePackets;
    private int sendCmdType = -1;

    public BluetoothLEHelper(Context context) {
        this.context = context;
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
//                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
//            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address, BluetoothGattCallback gattCallback) {
        if (bluetoothAdapter == null || address == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (bluetoothDeviceAddress != null && address.equals(bluetoothDeviceAddress) && bluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return bluetoothGatt.connect();
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        bluetoothDeviceAddress = address;
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
//        Log.d(TAG, "Trying to create a new connection.");
        System.out.println("device.getBondState==" + device.getBondState());
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            return true;
        } else {
            return false;
        }
    }

    /**
     * close gatt connection
     */
    public boolean close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
            return true;
        } else {
            return false;
        }
    }

    //check gatt services.
    public boolean checkGattServices(String hardwareVersion, String firmwareVersion, List<BluetoothGattService> gattServiceList) {
        for (BluetoothGattService bluetoothGattService : gattServiceList) {
            UUID serviceUUID = bluetoothGattService.getUuid();

            if (serviceUUID.equals(GattInfo.BASE_SERVICE_UUID)) {
                baseSettingsService = bluetoothGattService;
            } else if (serviceUUID.equals(GattInfo.SENSO_SERVICE_UUID)) {
                sensoSettingsService = bluetoothGattService;
            }
        }

        return true;
    }

    /**
     * check beacon service.
     *
     * @param gattServiceList
     * @return
     */
    public boolean checkGattServices(List<BluetoothGattService> gattServiceList) {
        for (BluetoothGattService bluetoothGattService : gattServiceList) {
            UUID serviceUUID = bluetoothGattService.getUuid();
            if (serviceUUID.equals(GattInfo.SENSORO_SENSOR_SERVICE_UUID)) {
                baseSettingsService = bluetoothGattService;
            }
        }

        return baseSettingsService != null;
    }

    /**
     * reset to factory.
     */
    public boolean resetToFactorySettings() {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic resetToFactoryChar = baseSettingsService.getCharacteristic(GattInfo.BASE_WORK_MODE_UUID);
            if (resetToFactoryChar != null) {
                resetToFactoryChar.setValue(new byte[]{(byte) 0xe0});  //恢复出厂设置
                bluetoothGatt.writeCharacteristic(resetToFactoryChar);
                return true;
            }
        }
        return false;
    }

    /**
     * reset accelerometer reConnectCount.
     *
     * @return result.
     */
    public boolean resetAccelerometerCount() {
        if (sensoSettingsService != null) {
            acceleratorChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_ACCELERATOR_UUID);
            if (acceleratorChar != null) {
                acceleratorChar.setValue(new byte[]{0x00, 0x00, 0x00, 0x00});  //重制加速度计数器为0
                bluetoothGatt.writeCharacteristic(acceleratorChar);
                return true;
            }
        }
        return false;
    }

    /**
     * update password.
     *
     * @param password password
     * @return result
     */
    public boolean updateWritePassword(byte[] password) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic updateWritePwdChar = baseSettingsService.getCharacteristic(GattInfo.BASE_CHANGE_PWD_UUID);
            if (updateWritePwdChar != null) {
                updateWritePwdChar.setValue(password);
                bluetoothGatt.writeCharacteristic(updateWritePwdChar);
                return true;
            }
        }
        return false;
    }

    /**
     * check password
     *
     * @param password password
     * @return result.
     */
    public boolean requireWritePermission(byte[] password) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic writePermissionChar = baseSettingsService.getCharacteristic(GattInfo.BASE_CHECK_PWD_UUID);
            if (writePermissionChar != null) {
                writePermissionChar.setValue(password);
                bluetoothGatt.writeCharacteristic(writePermissionChar);
                return true;
            }
        }
        return false;
    }

    public int requireWritePermission(String password) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic authorizationChar = baseSettingsService.getCharacteristic(GattInfo
                    .SENSORO_AUTHORIZATION_CHAR_UUID);
            if (authorizationChar != null) {
                byte[] passwordBytes = convertPassword2Bytes(password);
                if (passwordBytes == null) {
                    return ResultCode.INVALID_PARAM;
                }
                return writeCharacteristic(authorizationChar, passwordBytes);
            }
        }
        return ResultCode.SYSTEM_ERROR;
    }

    /**
     * write bytes to char.
     *
     * @param writeChar
     * @param writeBytes
     * @return
     */
    private int writeCharacteristic(BluetoothGattCharacteristic writeChar, byte[] writeBytes) {
        if (writeChar == null || writeBytes == null || writeBytes.length < 0 || writeBytes.length > 20) {
            return ResultCode.SYSTEM_ERROR;
        } else {
            writeChar.setValue(writeBytes);
            bluetoothGatt.writeCharacteristic(writeChar);
            return ResultCode.SUCCESS;
        }
    }

    private byte[] convertPassword2Bytes(String password) {
        // encrypt paasword by HMAC-SHA512，get 16 bytes before.
        byte[] newPassword = new byte[16];
        if (password == null) {
            for (int i = 0; i < newPassword.length; i++) {
                newPassword[i] = 0x00;
            }
            return newPassword;
        }
        byte[] passwordBytes = null;
        try {
            passwordBytes = password.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return passwordBytes;
    }

    public boolean getBaseSettings() {
        if (baseSettingsService != null) {
            baseSettingsChar = baseSettingsService.getCharacteristic(GattInfo.BASE_PARAMS_SETTINGS_UUID);
            if (baseSettingsChar != null) {
                readCharacteristic(baseSettingsChar);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * read all parameters in SensorSettings
     */
    public boolean getSensoroSettings() {
        if (sensoSettingsService != null) {
            sensoSettingsChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_PARAMS_SETTINGS_UUID);
            if (sensoSettingsChar != null) {
                readCharacteristic(sensoSettingsChar);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * read rotation broadcast period.
     */
    public boolean getSecureBroadcastRotation() {
        if (baseSettingsService != null) {
            dynamicMMChar = baseSettingsService.getCharacteristic(GattInfo.BASE_SECURE_BROADCAST_UUID);
            if (dynamicMMChar != null) {
                readCharacteristic(dynamicMMChar);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * listen temperature changing.
     */
    public void listenTemperatureChar() {
        BluetoothGattCharacteristic temperatureChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_TEMPERATURE_UUID);
        bluetoothGatt.setCharacteristicNotification(temperatureChar, true);
        BluetoothGattDescriptor temperatureDescriptor = temperatureChar.getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        temperatureDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(temperatureDescriptor);
    }

    /**
     * listen light changing.
     */
    public void listenBrightnessChar() {
        BluetoothGattCharacteristic brightChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_BRIGHT_UUID);
        bluetoothGatt.setCharacteristicNotification(brightChar, true);
        BluetoothGattDescriptor brightDescriptor = brightChar.getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        brightDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(brightDescriptor);
    }

    /**
     * listen accelerometer reConnectCount changing.
     */
    public void listenAcceleratorCountChar() {
        BluetoothGattCharacteristic isMovingChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_IS_MOVING_UUID);
        bluetoothGatt.setCharacteristicNotification(isMovingChar, true);
        BluetoothGattDescriptor brightDescriptor = isMovingChar.getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        brightDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(brightDescriptor);
    }

    /**
     * listen moving state changing.
     */
    public void listenAcceleratorMovingChar() {
        BluetoothGattCharacteristic isMovingChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_ACCELERATOR_UUID);
        bluetoothGatt.setCharacteristicNotification(isMovingChar, true);
        BluetoothGattDescriptor brightDescriptor = isMovingChar.getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
        brightDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(brightDescriptor);
    }

    public boolean writeBaseSettings(byte[] baseSetiings) {
        if (baseSettingsService != null) {
            baseSettingsChar = baseSettingsService.getCharacteristic(GattInfo.BASE_PARAMS_SETTINGS_UUID);
            if (baseSettingsChar != null) {
                baseSettingsChar.setValue(baseSetiings);
                bluetoothGatt.writeCharacteristic(baseSettingsChar);
                return true;
            }
        }
        return false;
    }

    public boolean writeSensoSettings(byte[] sensoSettings) {
        if (sensoSettingsService != null) {
            sensoSettingsChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_PARAMS_SETTINGS_UUID);
            if (sensoSettingsChar != null) {
                sensoSettingsChar.setValue(sensoSettings);
                bluetoothGatt.writeCharacteristic(sensoSettingsChar);
                return true;
            }
        }
        return false;
    }

    public boolean writeProximityUUID(byte[] uuid) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic uuidChar = baseSettingsService.getCharacteristic(GattInfo.BASE_UUID_UUID);
            if (uuidChar != null) {
                uuidChar.setValue(uuid);
                bluetoothGatt.writeCharacteristic(uuidChar);
                return true;
            }
        }
        return false;
    }

    public boolean writeMajorMinor(byte[] majorMinor) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic majorMinorChar = baseSettingsService.getCharacteristic(GattInfo.BASE_MAJOR_MINOR_UUID);
            if (majorMinorChar != null) {
                majorMinorChar.setValue(majorMinor);
                bluetoothGatt.writeCharacteristic(majorMinorChar);
                return true;
            }
        }
        return false;
    }

    public boolean reloadSensoroData() {
        if (sensoSettingsService != null) {
            BluetoothGattCharacteristic forceUpdateSeneoChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_FORCE_UPDATE_UUID);
            if (forceUpdateSeneoChar != null) {
                forceUpdateSeneoChar.setValue(new byte[]{(byte) 0xff});
                bluetoothGatt.writeCharacteristic(forceUpdateSeneoChar);
                return true;
            }
        }
        return false;
    }

    public boolean writeSecureBroadcastInterval(byte[] interval) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic dinamicMMChar = baseSettingsService.getCharacteristic(GattInfo.BASE_SECURE_BROADCAST_UUID);
            if (dinamicMMChar != null) {
                dinamicMMChar.setValue(interval);
                bluetoothGatt.writeCharacteristic(dinamicMMChar);
                return true;
            }
        }
        return false;
    }

    public boolean onFlashLightWitCommand(byte[] cmdLED) {
        if (sensoSettingsService != null) {
            BluetoothGattCharacteristic writeLedChar = sensoSettingsService.getCharacteristic(GattInfo.SENSO_LED_UUID);
            if (writeLedChar != null) {
                writeLedChar.setValue(cmdLED);
                bluetoothGatt.writeCharacteristic(writeLedChar);
                return true;
            }
        }
        return false;
    }

    public boolean writeBroadcastKey(byte[] broadcastKey) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic broadcastKeyChar = baseSettingsService.getCharacteristic(GattInfo.BASE_BROADCAST_KEY_UUID);
            if (broadcastKeyChar != null) {
                broadcastKeyChar.setValue(broadcastKey);
                bluetoothGatt.writeCharacteristic(broadcastKeyChar);
                return true;
            }
        }
        return false;
    }

    public boolean enableIBeacon(byte[] workMode) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic workModeChar = baseSettingsService.getCharacteristic(GattInfo.BASE_ENABLE_IBEACON_UUID);
            if (workModeChar != null) {
                workModeChar.setValue(workMode);
                bluetoothGatt.writeCharacteristic(workModeChar);
                return true;
            }
        }
        return false;
    }

    public boolean enableAliBeacon(byte[] enableAliBeacon) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic writeAliBeaconChar = baseSettingsService.getCharacteristic(GattInfo.BASE_ENABLE_ALIBEACON_UUID);
            if (writeAliBeaconChar != null) {
                writeAliBeaconChar.setValue(enableAliBeacon);
                bluetoothGatt.writeCharacteristic(writeAliBeaconChar);
                return true;
            }
        }
        return false;
    }


    public boolean enableBackgroundEnhancement(byte[] enableBackgroundEnhancement) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic writeBackgroundEnhancementChar = baseSettingsService.getCharacteristic(GattInfo.BASE_ENABLE_BACKGROUND_ENHANCEMENT_UUID);
            if (writeBackgroundEnhancementChar != null) {
                writeBackgroundEnhancementChar.setValue(enableBackgroundEnhancement);
                bluetoothGatt.writeCharacteristic(writeBackgroundEnhancementChar);
                return true;
            }
        }
        return false;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
//            Log.w(TAG, "bluetoothGatt not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public static class GattInfo {
        public static final UUID BASE_SERVICE_UUID = UUID.fromString("DEAE0000-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_CHECK_PWD_UUID = UUID.fromString("DEAE0001-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_CHANGE_PWD_UUID = UUID.fromString("DEAE0002-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_PARAMS_SETTINGS_UUID = UUID.fromString("DEAE0003-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_UUID_UUID = UUID.fromString("DEAE0004-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_MAJOR_MINOR_UUID = UUID.fromString("DEAE0005-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_WORK_MODE_UUID = UUID.fromString("DEAE0006-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_SECURE_BROADCAST_UUID = UUID.fromString("DEAE0007-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_BROADCAST_KEY_UUID = UUID.fromString("DEAE0008-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_ENABLE_IBEACON_UUID = UUID.fromString("DEAE0009-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_ENABLE_ALIBEACON_UUID = UUID.fromString("DEAE000A-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID BASE_ENABLE_BACKGROUND_ENHANCEMENT_UUID = UUID.fromString("DEAE000B-7A4E-1BA2-834A-50A30CCAE0E4");

        public static final UUID SENSO_SERVICE_UUID = UUID.fromString("DEAE0100-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSO_PARAMS_SETTINGS_UUID = UUID.fromString("DEAE0101-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSO_TEMPERATURE_UUID = UUID.fromString("DEAE0102-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSO_BRIGHT_UUID = UUID.fromString("DEAE0103-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSO_ACCELERATOR_UUID = UUID.fromString("DEAE0104-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSO_FORCE_UPDATE_UUID = UUID.fromString("DEAE0105-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSO_IS_MOVING_UUID = UUID.fromString("DEAE0106-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSO_LED_UUID = UUID.fromString("DEAE0107-7A4E-1BA2-834A-50A30CCAE0E4");

        public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        //
        public static final UUID SENSORO_SENSOR_SERVICE_UUID = UUID.fromString("DEAE0500-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSORO_AUTHORIZATION_CHAR_UUID = UUID.fromString
                ("DEAE0503-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSORO_SENSOR_WRITE_UUID = UUID.fromString("DEAE0501-7A4E-1BA2-834A-50A30CCAE0E4");
        public static final UUID SENSORO_SENSOR_INDICATE_UUID = UUID.fromString("DEAE0502-7A4E-1BA2-834A-50A30CCAE0E4");
    }

    public void listenNotifyChar() {
        if (baseSettingsService!=null){
            BluetoothGattCharacteristic notifyChar = baseSettingsService.getCharacteristic(GattInfo
                    .SENSORO_SENSOR_INDICATE_UUID);
            bluetoothGatt.setCharacteristicNotification(notifyChar, true);
            BluetoothGattDescriptor notifyDescriptor = notifyChar.getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
            notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            bluetoothGatt.writeDescriptor(notifyDescriptor);
        }

    }
    public int getSendPacketNumber() {
        return sendPacketNumber;
    }
    public void resetSendPacket() {
        writePackets = null;
        sendPacketNumber = 1;
    }
    /**
     * series to write package.
     *
     * @param characteristic
     */
    public void sendPacket(BluetoothGattCharacteristic characteristic) {
        if (writePackets != null && sendPacketNumber == writePackets.size()) {
            // packets are all writen.
            resetSendPacket();
        } else {
            byte[] writePacket = writePackets.get(sendPacketNumber);
            // packet not write over.
            writeCharacteristic(characteristic, writePacket);
            sendPacketNumber++;
        }
    }
    public ArrayList<byte[]> getWritePackets() {
        return writePackets;
    }
    /**
     * write all bytes to char.
     *
     * @param writeChar
     * @param writeBytes
     * @return
     */
    private int writeCharAllBytes(BluetoothGattCharacteristic writeChar, byte[] writeBytes, int sendCmdType) {
        if (writeChar == null || writeBytes == null || writeBytes.length < 0) {
            return ResultCode.SYSTEM_ERROR;
        }
        if (writePackets == null) {
            this.sendCmdType = sendCmdType;
            writePackets = createWritePackets(writeBytes);
            if (writePackets == null || writePackets.size() == 0) {
                return ResultCode.SYSTEM_ERROR;
            }
            return writeCharacteristic(writeChar, writePackets.get(0));
        } else {
            return ResultCode.MCU_BUSY;
        }
    }
    /**
     * package all writing bytes.
     *
     * @param writeBytes
     * @return
     */
    private ArrayList<byte[]> createWritePackets(byte[] writeBytes) {
        if (writeBytes == null) {
            return null;
        }

        ArrayList<byte[]> writePackages = new ArrayList<byte[]>();
        for (int i = 0; i < writeBytes.length; i = i + 20) {
            if (writeBytes.length <= i + 20) {
                byte[] onePackage = new byte[writeBytes.length - i];
                System.arraycopy(writeBytes, i, onePackage, 0, writeBytes.length - i);
                writePackages.add(onePackage);
            } else {
                byte[] onePackage = new byte[20];
                System.arraycopy(writeBytes, i, onePackage, 0, 20);
                writePackages.add(onePackage);
            }
        }
        return writePackages;
    }
    public int writeConfiguration(byte[] data, int sendCmdType) {
        if (baseSettingsService != null) {
            BluetoothGattCharacteristic writeChar = baseSettingsService.getCharacteristic(GattInfo
                    .SENSORO_SENSOR_WRITE_UUID);
            if (writeChar != null) {
                // check beaconConfiguration
                return writeCharAllBytes(writeChar, data, sendCmdType);
            }
        }
        return ResultCode.SYSTEM_ERROR;
    }
    public int getSendCmdType() {
        return sendCmdType;
    }
    private int writeCharSingleBytes(BluetoothGattCharacteristic writeChar, byte[] writeBytes, int sendCmdType) {
        this.sendCmdType = sendCmdType;
        return writeCharacteristic(writeChar, writeBytes);
    }
}
