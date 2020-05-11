package com.sensoro.libbleserver.ble.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by sensoro on 16/8/12.
 */

public class BLEDevice implements Parcelable, Cloneable {

    public static final int TYPE_DEVICE = 0x00;
    public static final int TYPE_SENSOR = 0x01;
    public static final int TYPE_STATION = 0x02;
    public static final int TYPE_CAMERA = 0x03;
    public String sn;
    public String hardwareVersion;// hardware version.
    public String firmwareVersion;// firmware version.
    public String macAddress; // MAC
    public int batteryLevel;// battery left
    public int rssi;
    public int type;
    public long lastFoundTime;
    public IBeacon iBeacon;

    protected BLEDevice() {
        lastFoundTime = System.currentTimeMillis();
    }


    @Override
    public BLEDevice clone() throws CloneNotSupportedException {
        BLEDevice newDevice = null;
        try {
            newDevice = (BLEDevice) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return newDevice;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public boolean equals(Object that) {
        if ((that instanceof BLEDevice)) {
            BLEDevice thatDevice = (BLEDevice) that;
            return thatDevice.sn.equals(this.sn) || thatDevice.macAddress.equals(this.macAddress);
        }
        return false;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        if (!TextUtils.isEmpty(sn) && !TextUtils.isEmpty(macAddress)) {
            return sn.hashCode() ^ this.macAddress.hashCode();
        } else if (!TextUtils.isEmpty(sn)) {
            return sn.hashCode();
        } else if (!TextUtils.isEmpty(macAddress)) {
            return this.macAddress.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sn);
        dest.writeString(this.hardwareVersion);
        dest.writeString(this.firmwareVersion);
        dest.writeString(this.macAddress);
        dest.writeInt(this.batteryLevel);
        dest.writeInt(this.rssi);
        dest.writeInt(this.type);
        dest.writeLong(this.lastFoundTime);
        dest.writeParcelable(this.iBeacon, flags);
    }

    protected BLEDevice(Parcel in) {
        this.sn = in.readString();
        this.hardwareVersion = in.readString();
        this.firmwareVersion = in.readString();
        this.macAddress = in.readString();
        this.batteryLevel = in.readInt();
        this.rssi = in.readInt();
        this.type = in.readInt();
        this.lastFoundTime = in.readLong();
        this.iBeacon = in.readParcelable(IBeacon.class.getClassLoader());
    }

}
