package com.sensoro.libbleserver.ble;

import java.io.Serializable;

public final class SensoroData implements Serializable {
    /**
     * 基本属性date
     */
    public Integer data_int;
    public Float data_float;
    /**
     * 预警上限值
     */
    public Integer alarmHigh_int;
    public Float alarmHigh_float;
    /**
     * 预警下限值
     */
    public Integer alarmLow_int;
    public Float alarmLow_float;
    /**
     * 状态属性
     */
    public Integer status;

    /**
     * 步长上限值
     */
    public Integer alarmStepHigh_int;
    public Float alarmStepHigh_float;
    /**
     * 步长下限值
     */
    public Integer alarmStepLow_int;
    public Float alarmStepLow_float;

    //
    public boolean has_data;

    public boolean has_alarmHigh;

    public boolean has_alarmLow;

    public boolean has_status;

    public boolean has_alarmStepHigh;

    public boolean has_alarmStepLow;

}
