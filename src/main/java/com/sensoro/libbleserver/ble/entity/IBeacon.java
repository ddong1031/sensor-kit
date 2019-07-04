package com.sensoro.libbleserver.ble.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.sensoro.libbleserver.ble.scanner.BLEFilter;
import com.sensoro.libbleserver.ble.utils.SensoroUtils;

import java.io.Serializable;

/**
 * Created by Sensoro on 15/9/18.
 */
public class IBeacon implements Parcelable, Cloneable, Serializable {
    private static final int DATA_START_INDEX = 0;
    String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getMeasuredPower() {
        return measuredPower;
    }

    public void setMeasuredPower(int measuredPower) {
        this.measuredPower = measuredPower;
    }


    int major;
    int minor;
    int measuredPower;
    public String macAddress; // MAC

    public static IBeacon createIBeacon(ScanBLEResult scanBLEResult) {
        SparseArray<byte[]> manufacturerSpecificData = scanBLEResult.getScanRecord().getManufacturerSpecificData();
        if (manufacturerSpecificData == null) {
            return null;
        }

        byte[] iBeaconBytes = scanBLEResult.getScanRecord().getManufacturerSpecificData(BLEFilter.MANUFACTURER_ID_APPLE);
        if (iBeaconBytes != null) {
            return parseIBeacon(iBeaconBytes, scanBLEResult);
        }
        return null;
    }

    private static IBeacon parseIBeacon(byte[] iBeaconBytes, ScanBLEResult scanBLEResult) {
        if (iBeaconBytes.length < 23) {
            return null;
        }
        if (iBeaconBytes[DATA_START_INDEX] != 0x02 || iBeaconBytes[DATA_START_INDEX + 1] != 0x15) {
            return null;
        }

        IBeacon iBeacon = new IBeacon();

        // proximityUUID
        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(iBeaconBytes, DATA_START_INDEX + 2, proximityUuidBytes, 0, 16);
        String hexString = SensoroUtils.bytesToHex(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0, 8));
        sb.append("-");
        sb.append(hexString.substring(8, 12));
        sb.append("-");
        sb.append(hexString.substring(12, 16));
        sb.append("-");
        sb.append(hexString.substring(16, 20));
        sb.append("-");
        sb.append(hexString.substring(20, 32));
        iBeacon.uuid = sb.toString().toUpperCase();
        iBeacon.macAddress = scanBLEResult.getDevice().getAddress();

        // major
        iBeacon.major = ((iBeaconBytes[DATA_START_INDEX + 18] & 0xff) << 8) + (iBeaconBytes[DATA_START_INDEX + 19] & 0xff);
        // minor
        iBeacon.minor = ((iBeaconBytes[DATA_START_INDEX + 20] & 0xff) << 8) + (iBeaconBytes[DATA_START_INDEX + 21] & 0xff);
        // mrssi
        iBeacon.measuredPower = (int) iBeaconBytes[DATA_START_INDEX + 22];

        return iBeacon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.measuredPower);
    }

    public IBeacon() {
    }

    protected IBeacon(Parcel in) {
        this.uuid = in.readString();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.measuredPower = in.readInt();
    }

    public static final Creator<IBeacon> CREATOR = new Creator<IBeacon>() {
        @Override
        public IBeacon createFromParcel(Parcel source) {
            return new IBeacon(source);
        }

        @Override
        public IBeacon[] newArray(int size) {
            return new IBeacon[size];
        }
    };
}
