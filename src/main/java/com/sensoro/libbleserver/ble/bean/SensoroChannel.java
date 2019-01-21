package com.sensoro.libbleserver.ble.bean;

import com.google.protobuf.GeneratedMessage;
import com.sensoro.libbleserver.ble.proto.MsgNode1V1M5;

import java.io.Serializable;

/**
 * 主要用作信道频段的修改
 */
public class SensoroChannel implements Serializable {
    public boolean hasFrequency;
    public boolean hasRx1Frequency;
    //上行频点
    public int frequency;
    //下行频点
    public int rx1Frequency;
}