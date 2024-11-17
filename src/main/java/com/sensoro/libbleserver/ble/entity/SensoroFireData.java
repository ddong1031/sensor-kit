package com.sensoro.libbleserver.ble.entity;

import java.io.Serializable;

public final class SensoroFireData implements Serializable {

    public boolean hasSensorPwd;
    public boolean hasLeakageTh;
    public boolean hasTempTh;
    public boolean hasCurrentTh;
    public boolean hasLoadTh;
    public boolean hasVolHighTh;
    public boolean hasVolLowTh;
    public boolean hasCmd;


    //
    public int sensorPwd;
    public int leakageTh;
    public int tempTh;
    public int currentTh;
    public int loadTh;
    public int volHighTh;
    public int volLowTh;
    public int cmd;
}
