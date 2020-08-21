package com.sensoro.libbleserver.ble.factory;

import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.connection.BluetoothLEHelper;
import com.sensoro.libbleserver.ble.constants.CmdType;
import com.sensoro.libbleserver.ble.constants.ResultCode;
import com.sensoro.libbleserver.ble.proto.MsgNode1V1M5;
import com.sensoro.libbleserver.ble.proto.ProtoMsgCfgV1U1;
import com.sensoro.libbleserver.ble.proto.ProtoStd1U1;
import com.sensoro.libbleserver.ble.scanner.SensoroUUID;

import java.util.Map;

import static com.sensoro.libbleserver.ble.constants.CodeConstants.DATA_VERSION_03;
import static com.sensoro.libbleserver.ble.constants.CodeConstants.DATA_VERSION_04;
import static com.sensoro.libbleserver.ble.constants.CodeConstants.DATA_VERSION_05;

public class SensorDeviceDataFactory {
    private SensorDeviceDataFactory() {
    }

    public static void writeCmd(SensoroWriteCallback writeCallback, Map<Integer, SensoroWriteCallback> writeCallbackHashMap, byte dataVersion, BluetoothLEHelper bluetoothLEHelper4) {
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
                        BluetoothLEHelper.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
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
                        BluetoothLEHelper.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
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
                        BluetoothLEHelper.GattInfo.SENSORO_DEVICE_WRITE_CHAR_UUID);
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
