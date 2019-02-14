package com.sensoro.libbleserver.ble;

public class SensoroBleManger {
    private SensoroBleManger() {
    }

    public static SensoroBleManger getInstance() {
        return SensoroBleMangerHolder.instance;
    }

    private static final class SensoroBleMangerHolder {
        private static final SensoroBleManger instance = new SensoroBleManger();
    }
}
