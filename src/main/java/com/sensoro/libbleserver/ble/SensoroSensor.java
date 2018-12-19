package com.sensoro.libbleserver.ble;

import android.os.Parcel;
import android.os.Parcelable;

import com.sensoro.libbleserver.ble.bean.SensoroAcrelFires;
import com.sensoro.libbleserver.ble.bean.SensoroCayManData;

import java.io.Serializable;

/**
 * Created by sensoro on 17/1/19.
 */

public class SensoroSensor extends BLEDevice implements Parcelable, Cloneable {


    public SensoroData temperature;// 温度
    public SensoroData light; // 光线照度
    public SensoroData humidity;//湿度
    public SensoroData co;
    public SensoroData co2;
    public SensoroData no2;
    public SensoroData methane;
    public SensoroData level;
    public SensoroData accelerometerCount; // 加速度计数器

    public byte[] customize;

    public SensoroData ch20;
    public SensoroData ch4;
    public SensoroData coverStatus;
    public SensoroData so2;
    public SensoroData gps;
    public SensoroData leak;
    public SensoroData lpg;
    public SensoroData magnetism;
    public SensoroData o3;
    public SensoroData pm1;
    public SensoroData pm25;
    public SensoroData pm10;
    public SensoroData smoke;
    public SensoroData pitch;
    public SensoroData roll;
    public SensoroData yaw;
    public SensoroData gas;
    public SensoroData flame;
    public SensoroData waterPressure;
    public SensoroData multiTemperature;
    public SensoroFireData elecFireData;
    public SensoroMantunData mantunData;
    public SensoroAcrelFires acrelFires;// 安科瑞三相电
    public SensoroCayManData cayManData;//嘉德 自研烟感
    //
    public boolean hasAccelerometerCount;
    public boolean hasAngle;
    public boolean hasBattery;
    public boolean hasCh2O;
    public boolean hasCh4;
    public boolean hasCover;
    public boolean hasGps;
    public boolean hasCo;
    public boolean hasCo2;
    public boolean hasNo2;
    public boolean hasSo2;
    public boolean hasGyroscope;
    public boolean hasLeak;
    public boolean hasHumidity;
    public boolean hasTemperature;
    public boolean hasLevel;
    public boolean hasLight;
    public boolean hasLpg;
    public boolean hasMagnetism;
    public boolean hasO3;
    public boolean hasPm1;
    public boolean hasPm25;
    public boolean hasPm10;
    public boolean hasSmoke;
    public boolean hasPitch;
    public boolean hasRoll;
    public boolean hasYaw;
    public boolean hasFlame;
    public boolean hasGas;
    public boolean hasWaterPressure;
    public boolean hasMultiTemp;
    public boolean hasMethane;
    public boolean hasFireData;
    public boolean hasMantunData;
    public boolean hasAcrelFires;
    public boolean hasCayMan;

    public SensoroSensor() {
    }

    protected SensoroSensor(Parcel in) {
        super(in);
        temperature = (SensoroData) in.readSerializable();
        o3 = (SensoroData) in.readSerializable();
        smoke = (SensoroData) in.readSerializable();
        magnetism = (SensoroData) in.readSerializable();
        gps = (SensoroData) in.readSerializable();
        so2 = (SensoroData) in.readSerializable();
        ch4 = (SensoroData) in.readSerializable();
        ch20 = (SensoroData) in.readSerializable();
        light = (SensoroData) in.readSerializable();
        humidity = (SensoroData) in.readSerializable();
        accelerometerCount = (SensoroData) in.readSerializable();
        leak = (SensoroData) in.readSerializable();
        co = (SensoroData) in.readSerializable();
        co2 = (SensoroData) in.readSerializable();
        no2 = (SensoroData) in.readSerializable();
        methane = (SensoroData) in.readSerializable();
        lpg = (SensoroData) in.readSerializable();
        pm1 = (SensoroData) in.readSerializable();
        pm25 = (SensoroData) in.readSerializable();
        pm10 = (SensoroData) in.readSerializable();
        coverStatus = (SensoroData) in.readSerializable();
        level = (SensoroData) in.readSerializable();
        flame = (SensoroData) in.readSerializable();
        pitch = (SensoroData) in.readSerializable();
        roll = (SensoroData) in.readSerializable();
        yaw = (SensoroData) in.readSerializable();
        gas = (SensoroData) in.readSerializable();
        multiTemperature = (SensoroData) in.readSerializable();
        waterPressure = (SensoroData) in.readSerializable();
        Serializable serializable = in.readSerializable();
        if (serializable instanceof SensoroFireData ){
            elecFireData = (SensoroFireData) serializable;
        }
        Serializable mantun = in.readSerializable();
        if(mantun instanceof SensoroMantunData){
            mantunData = (SensoroMantunData) mantun;
        }

        Serializable  acrelFir = in.readSerializable();
        if(acrelFir instanceof SensoroAcrelFires){
            acrelFires = (SensoroAcrelFires) acrelFir;
        }

        Serializable cayMan = in.readSerializable();
        if (cayMan instanceof SensoroCayManData) {
            cayManData = (SensoroCayManData)cayMan;
        }
        customize = in.createByteArray();
        hasAccelerometerCount = in.readByte() != 0;
        hasAngle = in.readByte() != 0;
        hasBattery = in.readByte() != 0;
        hasCh2O = in.readByte() != 0;
        hasCh4 = in.readByte() != 0;
        hasCo = in.readByte() != 0;
        hasCo2 = in.readByte() != 0;
        hasCover = in.readByte() != 0;
        hasGps = in.readByte() != 0;
        hasGyroscope = in.readByte() != 0;
        hasHumidity = in.readByte() != 0;
        hasLeak = in.readByte() != 0;
        hasLevel = in.readByte() != 0;
        hasLight = in.readByte() != 0;
        hasLpg = in.readByte() != 0;
        hasMagnetism = in.readByte() != 0;
        hasNo2 = in.readByte() != 0;
        hasO3 = in.readByte() != 0;
        hasPm1 = in.readByte() != 0;
        hasPm25 = in.readByte() != 0;
        hasPm10 = in.readByte() != 0;
        hasSmoke = in.readByte() != 0;
        hasSo2 = in.readByte() != 0;
        hasTemperature = in.readByte() != 0;
        hasPitch = in.readByte() != 0;
        hasRoll = in.readByte() != 0;
        hasYaw = in.readByte() != 0;
        hasFlame = in.readByte() != 0;
        hasGas = in.readByte() != 0;
        hasWaterPressure = in.readByte() != 0;
        hasMultiTemp = in.readByte() != 0;
        hasMethane = in.readByte() != 0;
        hasFireData = in.readByte() != 0;
        hasMantunData = in.readByte() != 0;
        hasAcrelFires = in.readByte() != 0;
        hasCayMan = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeSerializable(temperature);
        parcel.writeSerializable(smoke);
        parcel.writeSerializable(o3);
        parcel.writeSerializable(magnetism);
        parcel.writeSerializable(gps);
        parcel.writeSerializable(so2);
        parcel.writeSerializable(ch4);
        parcel.writeSerializable(ch20);
        parcel.writeSerializable(light);
        parcel.writeSerializable(humidity);
        parcel.writeSerializable(accelerometerCount);
        parcel.writeSerializable(leak);
        parcel.writeSerializable(co);
        parcel.writeSerializable(co2);
        parcel.writeSerializable(no2);
        parcel.writeSerializable(methane);
        parcel.writeSerializable(lpg);
        parcel.writeSerializable(pm1);
        parcel.writeSerializable(pm25);
        parcel.writeSerializable(pm10);
        parcel.writeSerializable(coverStatus);
        parcel.writeSerializable(level);
        parcel.writeSerializable(flame);
        parcel.writeSerializable(pitch);
        parcel.writeSerializable(roll);
        parcel.writeSerializable(yaw);
        parcel.writeSerializable(gas);
        parcel.writeSerializable(waterPressure);
        parcel.writeSerializable(elecFireData);
        parcel.writeSerializable(mantunData);
        parcel.writeSerializable(acrelFires);
        parcel.writeSerializable(cayManData);
        parcel.writeSerializable(multiTemperature);
        parcel.writeByteArray(customize);
        parcel.writeByte((byte) (hasAccelerometerCount ? 1 : 0));
        parcel.writeByte((byte) (hasAngle ? 1 : 0));
        parcel.writeByte((byte) (hasBattery ? 1 : 0));
        parcel.writeByte((byte) (hasCh2O ? 1 : 0));
        parcel.writeByte((byte) (hasCh4 ? 1 : 0));
        parcel.writeByte((byte) (hasCo ? 1 : 0));
        parcel.writeByte((byte) (hasCo2 ? 1 : 0));
        parcel.writeByte((byte) (hasCover ? 1 : 0));
        parcel.writeByte((byte) (hasGps ? 1 : 0));
        parcel.writeByte((byte) (hasGyroscope ? 1 : 0));
        parcel.writeByte((byte) (hasHumidity ? 1 : 0));
        parcel.writeByte((byte) (hasLeak ? 1 : 0));
        parcel.writeByte((byte) (hasLevel ? 1 : 0));
        parcel.writeByte((byte) (hasLight ? 1 : 0));
        parcel.writeByte((byte) (hasLpg ? 1 : 0));
        parcel.writeByte((byte) (hasMagnetism ? 1 : 0));
        parcel.writeByte((byte) (hasNo2 ? 1 : 0));
        parcel.writeByte((byte) (hasO3 ? 1 : 0));
        parcel.writeByte((byte) (hasPm1 ? 1 : 0));
        parcel.writeByte((byte) (hasPm25 ? 1 : 0));
        parcel.writeByte((byte) (hasPm10 ? 1 : 0));
        parcel.writeByte((byte) (hasSmoke ? 1 : 0));
        parcel.writeByte((byte) (hasSo2 ? 1 : 0));
        parcel.writeByte((byte) (hasTemperature ? 1 : 0));
        parcel.writeByte((byte) (hasPitch ? 1 : 0));
        parcel.writeByte((byte) (hasRoll ? 1 : 0));
        parcel.writeByte((byte) (hasYaw ? 1 : 0));
        parcel.writeByte((byte) (hasFlame ? 1 : 0));
        parcel.writeByte((byte) (hasGas ? 1 : 0));
        parcel.writeByte((byte) (hasWaterPressure ? 1 : 0));
        parcel.writeByte((byte) (hasMultiTemp ? 1 : 0));
        parcel.writeByte((byte) (hasMethane ? 1 : 0));
        parcel.writeByte((byte) (hasFireData ? 1 : 0));
        parcel.writeByte((byte)(hasMantunData?1:0));
        parcel.writeByte((byte)(hasAcrelFires?1:0));
        parcel.writeByte((byte)(hasCayMan?1:0));
    }

    public static final Creator<SensoroSensor> CREATOR = new Creator<SensoroSensor>() {

        @Override
        public SensoroSensor createFromParcel(Parcel parcel) {
            return new SensoroSensor(parcel);
        }

        @Override
        public SensoroSensor[] newArray(int size) {
            return new SensoroSensor[size];
        }
    };

    @Override
    public SensoroSensor clone() throws CloneNotSupportedException {
        SensoroSensor newSensor = null;
        try {
            newSensor = (SensoroSensor) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return newSensor;
    }

}
