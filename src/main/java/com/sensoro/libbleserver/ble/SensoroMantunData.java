package com.sensoro.libbleserver.ble;

import java.io.Serializable;

public class SensoroMantunData implements Serializable{
    public boolean hasVolVal;
    public boolean hasCurrVal;
    public boolean hasLeakageVal;
    public boolean hasPowerVal;
    public boolean hasKwhVal;
    public boolean hasTempVal;
    public boolean hasStatus;
    public boolean hasSwOnOff;
    public boolean hasId;

    public boolean hasVolHighTh;
    public boolean hasVolLowTh;
    public boolean hasLeakageTh;
    public boolean hasTempTh;
    public boolean hasCurrentTh;
    public boolean hasPowerTh;
    public boolean hasAttribute;
    public boolean hasCmd;


    public int volVal;
    public int currVal;
    public int leakageVal;
    public int powerVal;
    public int kwhVal;
    public int tempVal;
    public int status;
    public int swOnOff;
    public int id;

    public int volHighTh;
    public int volLowTh;
    public int leakageTh;
    public int tempTh;
    public int currentTh;
    public int powerTh;
    public int attribute;
    public int cmd;
}
