package com.sensoro.libbleserver.ble.entity;

import com.google.protobuf.ByteString;

import java.io.Serializable;

public class SensoroIbeacon implements Serializable {
    public boolean hasUuid;
    public boolean hasMajor;
    public boolean hasMinor;
    public boolean hasMrssi;

    public ByteString uuid;
    public int major; //主设备号
    public int minor;//从设备号
    public int mrssi;//广播功率
}
