package com.sensoro.libbleserver.ble.chipeupgrade;

import android.os.Handler;
import android.os.Looper;

import com.sensoro.libbleserver.ble.connection.BluetoothLEHelper;
import com.sensoro.libbleserver.ble.constants.CmdType;
import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.utils.LogUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ChipEUpgradeThread extends Thread {

    private final String upgradeFilePath;
    private final SensoroWriteCallback writeCallback;
    private final BluetoothLEHelper bluetoothLEHelper4;
    private final Semaphore dataWriteSemaphore;
    private final Semaphore cmdResponseSemaphore;
    private FileInputStream mFsInput;
    private int mFileSize;
    private final int AMOTA_LENGTH_SIZE_IN_PKT = 2;
    private final int AMOTA_CMD_SIZE_IN_PKT = 1;
    private final int AMOTA_CRC_SIZE_IN_PKT = 4;
    private final int MAXIMUM_APP_PAYLOAD = 20;
    private final int AMOTA_FW_PACKET_SIZE = 512;
    private final int AMOTA_HEADER_SIZE_IN_PKT = AMOTA_LENGTH_SIZE_IN_PKT + AMOTA_CMD_SIZE_IN_PKT;
    private boolean mStopOta;
    private int mFileOffset;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ChipEUpgradeThread(SensoroWriteCallback writeCallback, String upgradeFilePath, BluetoothLEHelper bluetoothLEHelper4) {
        this.upgradeFilePath = upgradeFilePath;
        this.writeCallback = writeCallback;
        this.bluetoothLEHelper4 = bluetoothLEHelper4;
        dataWriteSemaphore = new Semaphore(0);
        cmdResponseSemaphore = new Semaphore(0);
    }

    private void runOnMainThread(Runnable run) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            run.run();
        } else {
            mainHandler.post(run);
        }
    }

    @Override
    public void run() {
        if (writeCallback == null) {
            LogUtils.loge("bigbangTracker", "回调为空");
            return;
        }

        if (upgradeFilePath == null) {
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(ChipEUpgradeErrorCode.FILE_NO_EXIST, CmdType.CMD_BB_TRACKER_UPGRADE);
                }
            });
            return;
        }

        try {
            mFsInput = new FileInputStream(upgradeFilePath);

            mFileSize = mFsInput.available();
            if (mFileSize == 0) {
                mFsInput.close();
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        writeCallback.onWriteFailure(ChipEUpgradeErrorCode.FILE_SIZE_ZERO, CmdType.CMD_BB_TRACKER_UPGRADE);
                    }
                });
                return;
            }

            if (!sendFwHeader()) {
                LogUtils.loge("bigbangTracker", "send FW header failed");
                mFsInput.close();
                return;
            }

            // start to send fw data
            setFileOffset();
            if (!sendFwData()) {
                LogUtils.loge("bigbangTracker", "send FW Data failed");
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        writeCallback.onWriteFailure(ChipEUpgradeErrorCode.SEND_PACKET_ERROR, CmdType.CMD_BB_TRACKER_UPGRADE);
                    }
                });
                mFsInput.close();
                return;
            }


            if (!sendVerifyCmd()) {
                LogUtils.loge("bigbangTracker", "send FW verify cmd failed");
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        writeCallback.onWriteFailure(ChipEUpgradeErrorCode.SEND_VERIFY_ERROR, CmdType.CMD_BB_TRACKER_UPGRADE);
                    }
                });
                mFsInput.close();
                return;
            }

            // need ACK for reset command?
            sendResetCmd();

            mFsInput.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                mFsInput.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(ChipEUpgradeErrorCode.UPGRADE_ERROR, CmdType.CMD_BB_TRACKER_UPGRADE);
                }
            });

        }
        LogUtils.loge("bigbangTracker", "exit startOtaUpdate");
    }

    private boolean sendResetCmd() {
        LogUtils.loge("bigbangTracker", "send fw reset cmd");
        if (sendOtaCmd(eAmotaCommand.AMOTA_CMD_FW_RESET, null, 0)) {
            return waitCmdResponse(3000);
        }

        return false;
    }

    private boolean sendVerifyCmd() {
        LogUtils.loge("bigbangTracker", "send fw verify cmd");
        if (sendOtaCmd(eAmotaCommand.AMOTA_CMD_FW_VERIFY, null, 0)) {
            return waitCmdResponse(5000);
        }

        return false;
    }

    private boolean sendFwData() {
        final int fwDataSize = mFileSize;
        int ret = -1;
        int offset = mFileOffset;

        LogUtils.loge("bigbangTracker", "file size = " + mFileSize);

        while (offset < fwDataSize) {
            try {
                ret = sentFwDataPacket();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ret < 0) {
                LogUtils.loge("bigbangTracker", "sentFwDataPacket failed");
                return false;
            }
            if (!waitCmdResponse(3000)) {
                LogUtils.loge("bigbangTracker", "waitCmdResponse timeout");
                return false;
            }
            offset += ret;
            final int finalOffset = offset;
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteSuccess((finalOffset * 100) / fwDataSize, CmdType.CMD_BB_TRACKER_UPGRADE);
                }
            });

        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.loge("bigbangTracker", "send firmware data complete");
                writeCallback.onWriteSuccess(101, CmdType.CMD_BB_TRACKER_UPGRADE);
            }
        });
        return true;
    }

    private int sentFwDataPacket() throws IOException {
        int ret;
        int len = AMOTA_FW_PACKET_SIZE;
        byte[] fwData = new byte[len];
        ret = mFsInput.read(fwData);
        if (ret <= 0) {
            LogUtils.loge("bigbangTracker", "no data read from mFsInput");
            return -1;
        }
        if (ret < AMOTA_FW_PACKET_SIZE)
            len = ret;
        LogUtils.loge("bigbangTracker", "send fw data len = " + len);
        if (!sendOtaCmd(eAmotaCommand.AMOTA_CMD_FW_DATA, fwData, len)) {
            return -1;
        }
        return ret;
    }

    private void setFileOffset() throws IOException {
        if (mFileOffset > 0) {
            LogUtils.loge("bigbangTracker", "set file offset " + mFileOffset);
            mFsInput.skip(mFileOffset);
        }
    }

    private boolean sendFwHeader() throws IOException {
        byte[] fwHeaderRead = new byte[48];
        int ret;

        ret = mFsInput.read(fwHeaderRead);
        if (ret < 48) {
            LogUtils.loge("bigbangTracker", "invalid packed firmware length");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(ChipEUpgradeErrorCode.HEAD_PACKET_ERROR, CmdType.CMD_W_CFG);
                }
            });
            return false;
        }

        mFileSize = ((fwHeaderRead[11] & 0xFF) << 24) + ((fwHeaderRead[10] & 0xFF) << 16) +
                ((fwHeaderRead[9] & 0xFF) << 8) + (fwHeaderRead[8] & 0xFF);

        LogUtils.loge("bigbangTracker", "mFileSize = " + mFileSize);
        LogUtils.loge("bigbangTracker", "send fw header " + formatHex2String(fwHeaderRead));

        if (sendOtaCmd(eAmotaCommand.AMOTA_CMD_FW_HEADER, fwHeaderRead, fwHeaderRead.length)) {
            return waitCmdResponse(3000);
        }

        return false;
    }

    public String formatHex2String(byte[] data) {
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte byteChar : data)
            stringBuilder.append(String.format("%02X ", byteChar));
        return stringBuilder.toString();
    }

    private boolean sendOtaCmd(eAmotaCommand cmd, byte[] data, int len) {
        byte cmdData = amOtaCmd2Byte(cmd);
        int checksum = 0;
        int packetLength = AMOTA_HEADER_SIZE_IN_PKT + len + AMOTA_CRC_SIZE_IN_PKT;
        byte[] packet = new byte[packetLength];

        // fill data + checksum length
        packet[0] = (byte) (len + AMOTA_CRC_SIZE_IN_PKT);
        packet[1] = (byte) ((len + AMOTA_CRC_SIZE_IN_PKT) >> 8);
        packet[2] = cmdData;

        if (len != 0) {
            // calculate CRC
            checksum = CrcCalculator.calcCrc32(len, data);
            // copy data into packet
            System.arraycopy(data, 0, packet, AMOTA_HEADER_SIZE_IN_PKT, len);
        }

        // append crc into packet
        // crc is always 0 if there is no data only command
        packet[AMOTA_HEADER_SIZE_IN_PKT + len] = ((byte) (checksum));
        packet[AMOTA_HEADER_SIZE_IN_PKT + len + 1] = ((byte) (checksum >> 8));
        packet[AMOTA_HEADER_SIZE_IN_PKT + len + 2] = ((byte) (checksum >> 16));
        packet[AMOTA_HEADER_SIZE_IN_PKT + len + 3] = ((byte) (checksum >> 24));

        if (sendPacket(packet, packetLength))
            return true;
        else {
            LogUtils.loge("bigbangTracker", "sendPacket failed");
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(ChipEUpgradeErrorCode.SEND_HEAD_PACKET_ERROR, CmdType.CMD_W_CFG);
                }
            });
            return false;
        }
    }

    private boolean sendPacket(byte[] data, int len) {
        int idx = 0;

        while (idx < len) {
            int frameLen;
            if ((len - idx) > MAXIMUM_APP_PAYLOAD) {
                frameLen = MAXIMUM_APP_PAYLOAD;
            } else {
                frameLen = len - idx;
            }
            byte[] frame = new byte[frameLen];
            System.arraycopy(data, idx, frame, 0, frameLen);
            try {
                if (!sendOneFrame(frame)) {
                    return false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            idx += frameLen;
        }

        return true;
    }

    private boolean sendOneFrame(byte[] data) throws InterruptedException {
        if (!bluetoothLEHelper4.writeChipECharacteristic(data)) {
            LogUtils.loge("bigbangTracker", "Failed to write characteristic");
            return false;
        }

        // wait for ACTION_GATT_WRITE_RESULT
        return waitGATTWriteComplete(3000);
    }

    private boolean waitGATTWriteComplete(long timeoutMs) {
        boolean ret = false;
        try {
            ret = dataWriteSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void setGATTWriteComplete() {
        LogUtils.loge("bigbangTracker", "dataWriteSemaphore 释放");
        dataWriteSemaphore.release();
    }

    public void otaCmdResponse(byte[] response) {
        eAmotaCommand cmd = amOtaByte2Cmd(response[2] & 0xff);

        if (cmd == eAmotaCommand.AMOTA_CMD_UNKNOWN) {
            LogUtils.loge("bigbangTracker", "got unknown response" + formatHex2String(response));
            return;
        }

        // TODO : handle CRC error and some more here
        if ((response[3] & 0xff) != 0) {
            LogUtils.loge("bigbangTracker", "error occurred, response = " + formatHex2String(response));
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    writeCallback.onWriteFailure(ChipEUpgradeErrorCode.UPGRADE_CMD_ERROR, CmdType.CMD_BB_TRACKER_UPGRADE);
                }
            });
            return;
        }

        switch (cmd) {
            case AMOTA_CMD_FW_HEADER:
                mFileOffset = ((response[4] & 0xFF) + ((response[5] & 0xFF) << 8) +
                        ((response[6] & 0xFF) << 16) + ((response[7] & 0xFF) << 24));
                LogUtils.loge("bigbangTracker", "get AMOTA_CMD_FW_HEADER response, mFileOffset = " + mFileOffset);
                cmdResponseArrived();
                break;
            case AMOTA_CMD_FW_DATA:
                LogUtils.loge("bigbangTracker", "get AMOTA_CMD_FW_DATA response");
                cmdResponseArrived();
                break;
            case AMOTA_CMD_FW_VERIFY:
                LogUtils.loge("bigbangTracker", "get AMOTA_CMD_FW_VERIFY response");
                cmdResponseArrived();
                break;
            case AMOTA_CMD_FW_RESET:
                LogUtils.loge("bigbangTracker", "get AMOTA_CMD_FW_RESET response");
                cmdResponseArrived();
                break;
            default:
                LogUtils.loge("bigbangTracker", "get response from unknown command");
        }

    }

    private void cmdResponseArrived() {
        cmdResponseSemaphore.release();
    }

    private eAmotaCommand amOtaByte2Cmd(int cmd) {
        switch (cmd & 0xff) {
            case 1:
                return eAmotaCommand.AMOTA_CMD_FW_HEADER;
            case 2:
                return eAmotaCommand.AMOTA_CMD_FW_DATA;
            case 3:
                return eAmotaCommand.AMOTA_CMD_FW_VERIFY;
            case 4:
                return eAmotaCommand.AMOTA_CMD_FW_RESET;
        }

        return eAmotaCommand.AMOTA_CMD_UNKNOWN;
    }

    public enum eAmotaCommand {
        AMOTA_CMD_UNKNOWN,
        AMOTA_CMD_FW_HEADER,
        AMOTA_CMD_FW_DATA,
        AMOTA_CMD_FW_VERIFY,
        AMOTA_CMD_FW_RESET,
        AMOTA_CMD_MAX
    }

    private boolean waitCmdResponse(long timeoutMs) {
        boolean ret = false;
        try {
            ret = cmdResponseSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private byte amOtaCmd2Byte(eAmotaCommand cmd) {
        switch (cmd) {
            case AMOTA_CMD_UNKNOWN:
                return 0;
            case AMOTA_CMD_FW_HEADER:
                return 1;
            case AMOTA_CMD_FW_DATA:
                return 2;
            case AMOTA_CMD_FW_VERIFY:
                return 3;
            case AMOTA_CMD_FW_RESET:
                return 4;
        }

        return 0;
    }
}
