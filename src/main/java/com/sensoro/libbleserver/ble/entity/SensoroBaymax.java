package com.sensoro.libbleserver.ble.entity;

import java.io.Serializable;

public class SensoroBaymax implements Serializable {
    public boolean hasGasDevClass;
    public boolean hasGasDensity;
    public boolean hasGasDensityL1;
    public boolean hasGasDensityL2;
    public boolean hasGasDensityL3;
    public boolean hasGasDisassembly;
    public boolean hasGasLosePwr;
    public boolean hasGasEMValve;
    public boolean hasGasDeviceStatus;
    public boolean hasGasDeviceOpState;
    public boolean hasGasDeviceComsDown;
    public boolean hasGasDeviceCMD;
    public boolean hasGasDeviceSilentMode;



    public int gasDevClass;
    public int gasDensity;
    public int gasDensityL1;
    public int gasDensityL2;
    public int gasDensityL3;
    public int gasDisassembly;
    public int gasLosePwr;
    public int gasEMValve;
    public int gasDeviceStatus;
    public int gasDeviceOpState;
    public int gasDeviceComsDown;
    public int gasDeviceCMD;
    public int gasDeviceSilentMode;
}
