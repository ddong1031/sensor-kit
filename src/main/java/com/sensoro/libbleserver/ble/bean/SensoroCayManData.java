package com.sensoro.libbleserver.ble.bean;

import java.io.Serializable;

/**
 * 安科瑞三相电
 */
public class SensoroCayManData implements Serializable {
    public boolean hasIsSmoke;
    public boolean hasIsMoved;
    public boolean hasValueOfTem;
    public boolean hasValueOfHum;
    public boolean hasAlarmOfHighTem;
    public boolean hasAlarmOfLowTem;
    public boolean hasAlarmOfHighHum;
    public boolean hasAlarmOfLowHum;
    public boolean hasCmd;


    public int isSmoke;
    public int isMoved;
    public int valueOfTem;
    public int valueOfHum;
    public int alarmOfHighTem;
    public int alarmOfLowTem;
    public int alarmOfHighHum;
    public int alarmOfLowHum;
    public int cmd;
}
