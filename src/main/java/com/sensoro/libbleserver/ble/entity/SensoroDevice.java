package com.sensoro.libbleserver.ble.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fangping on 2016/7/25.
 */

public class SensoroDevice extends BLEDevice implements Parcelable, Cloneable {

    // hardware version in String
    public static final String HW_A0 = "A0";
    public static final String HW_B0 = "B0";
    public static final String HW_C0 = "C0";
    public static final String HW_C1 = "C1";
    // Firmware version
//    public static final float FV_1_2 = 1.2f;
    public static final String FV_1_2 = "1.2";

    int major; // major
    int minor; // minor
    String proximityUUID; // proximityUuid

    int accelerometerCount; // accelerometer reConnectCount.
    int power;//功率
    float sf;//BL间隔
    public String devEui;
    public String appEui;
    public String appKey;
    public String appSkey;
    public String nwkSkey;
    String password;
    String dfuInfo;
    String band;
    public int devAdr;
    int loraDr;
    int loraAdr;
    int loraTxp;
    int dfuProgress;
    float loraInt;
    int bleTxp;
    float bleInt;
    int bleOnTime;
    int bleOffTime;
    int tempInterval;
    int lightInterval;
    int humidityInterval;
    public int classBEnabled;
    int classBDataRate;
    int classBPeriodicity;
    Integer uploadInterval;
    Integer confirm;
    Integer demoMode;
    Integer batteryBeep;
    Integer beepMuteTime;
    Integer ledStatus;


    Integer alertModeStatus;
    Integer activation;
    public Integer delay;
    transient List<Integer> channelMaskList;
    ArrayList<SensoroChannel> channelList;
    ArrayList<Integer> cmdArrayList;
    transient int maxEirp;
    transient int sglStatus;
    transient int sglFrequency;
    transient int sglDatarate;
    transient int lbtStatus;
    transient int lbtThreshold;


    transient int rx2Frequency;
    transient int rx2Datarate;

    byte dataVersion;
    boolean isIBeaconEnabled; // is beacon function enable.
    public boolean isDfu;
    boolean hasBleInterval;
    boolean hasBleOffTime;
    boolean hasBleOnTime;
    boolean hasBleOnOff;
    boolean hasBleTxp;
    boolean hasAdr;
    boolean hasAppEui;
    boolean hasAppKey;
    boolean hasAppSkey;
    boolean hasDevAddr;
    boolean hasDevEui;
    boolean hasNwkSkey;
    boolean hasNwkAddress;
    boolean hasLoraSf;
    boolean hasDataRate;
    boolean hasActivation;
    boolean hasLoraTxp;
    boolean hasLoraInterval;
    boolean hasLoraParam;
    boolean hasBleParam;
    boolean hasAppParam;
    boolean hasConfirm;
    boolean hasDemoMode;
    boolean hasBatteryBeep;
    boolean hasBeepMuteTime;
    boolean hasLedStatus;


    boolean hasAlertModeStatus;
    boolean hasUploadInterval;
    boolean hasEddyStone;
    boolean hasIbeacon;
    boolean hasSensorBroadcast;
    boolean hasSensorParam;
    boolean hasCustomPackage;
    boolean hasDelay;


    boolean hasLbtStatus;
    boolean hasLbtThreshold;

    public boolean hasLbtStatus() {
        return hasLbtStatus;
    }

    public void setHasLbtStatus(boolean hasLbtStatus) {
        this.hasLbtStatus = hasLbtStatus;
    }

    public boolean hasLbtThreshold() {
        return hasLbtThreshold;
    }

    public void setHasLbtThreshold(boolean hasLbtThreshold) {
        this.hasLbtThreshold = hasLbtThreshold;
    }

    public boolean hasSglFrequency() {
        return hasSglFrequency;
    }

    public void setHasSglFrequency(boolean hasSglFrequency) {
        this.hasSglFrequency = hasSglFrequency;
    }

    boolean hasSglFrequency;


    public boolean hasRx2Frequency() {
        return hasRx2Frequency;
    }

    public void setHasRx2Frequency(boolean hasRx2Frequency) {
        this.hasRx2Frequency = hasRx2Frequency;
    }

    boolean hasRx2Frequency;

    public boolean hasRx2Datarate() {
        return hasRx2Datarate;
    }

    public void setHasRx2Datarate(boolean hasRx2Datarate) {
        this.hasRx2Datarate = hasRx2Datarate;
    }

    boolean hasRx2Datarate;

    public boolean hasSglDatarate() {
        return hasSglDatarate;
    }

    public void setHasSglDatarate(boolean hasSglDatarate) {
        this.hasSglDatarate = hasSglDatarate;
    }

    boolean hasSglDatarate;

    public boolean hasSglStatus() {
        return hasSglStatus;
    }

    public void setHasSglStatus(boolean hasSglStatus) {
        this.hasSglStatus = hasSglStatus;
    }

    transient boolean hasSglStatus;
    transient boolean hasMaxEirp;
    SensoroSlot slotArray[];
    //    SensoroSensor sensoroSensor;
    private SensoroSensor sensoroSensorTest;
    public long lastFoundTime;

    public Integer getAlarmStepHigh() {
        return alarmStepHigh;
    }

    public void setAlarmStepHigh(Integer alarmStepHigh) {
        this.alarmStepHigh = alarmStepHigh;
    }

    public Integer getAlarmStepLow() {
        return alarmStepLow;
    }

    public void setAlarmStepLow(Integer alarmStepLow) {
        this.alarmStepLow = alarmStepLow;
    }

    public boolean hasAlarmStepHigh() {
        return hasAlarmStepHigh;
    }

    public void setHasAlarmStepHigh(boolean hasAlarmStepHigh) {
        this.hasAlarmStepHigh = hasAlarmStepHigh;
    }

    public boolean hasAlarmStepLow() {
        return hasAlarmStepLow;
    }

    public void setHasAlarmStepLow(boolean hasAlarmStepLow) {
        this.hasAlarmStepLow = hasAlarmStepLow;
    }

    /**
     * 报警设定的上下限的步长支持
     */
    Integer alarmStepHigh;
    Integer alarmStepLow;
    Integer alarmHigh;
    Integer alarmLow;
    boolean hasAlarmHigh;
    boolean hasAlarmLow;
    boolean hasAlarmStepHigh;
    boolean hasAlarmStepLow;
    boolean hasMultiTemperature;

    public boolean hasMultiTemperature() {
        return hasMultiTemperature;
    }

    public void setHasMultiTemperature(boolean hasMultiTemperature) {
        this.hasMultiTemperature = hasMultiTemperature;
    }


    public Integer getAlarmHigh() {
        return alarmHigh;
    }

    public void setAlarmHigh(Integer alarmHigh) {
        this.alarmHigh = alarmHigh;
    }

    public Integer getAlarmLow() {
        return alarmLow;
    }

    public void setAlarmLow(Integer alarmLow) {
        this.alarmLow = alarmLow;
    }

    public boolean hasAlarmHigh() {
        return hasAlarmHigh;
    }

    public void setHasAlarmHigh(boolean hasAlarmHigh) {
        this.hasAlarmHigh = hasAlarmHigh;
    }

    public boolean hasAlarmLow() {
        return hasAlarmLow;
    }

    public void setHasAlarmLow(boolean hasAlarmLow) {
        this.hasAlarmLow = hasAlarmLow;
    }


    public SensoroDevice() {
        lastFoundTime = System.currentTimeMillis();
        sn = null;
        major = 0;
        minor = 0;
        proximityUUID = null;
        macAddress = null;
        batteryLevel = 0;
        hardwareVersion = null;
        firmwareVersion = null;
        dfuInfo = null;
        accelerometerCount = 0;
        dfuProgress = 0;
        delay = 0;
        isIBeaconEnabled = true;
        isDfu = false;
        hasBleInterval = false;
        hasBleOffTime = false;
        hasBleOnTime = false;
        hasBleOnOff = false;
        hasBleTxp = false;
        hasAdr = false;
        hasAppEui = false;
        hasAppKey = false;
        hasAppSkey = false;
        hasDevAddr = false;
        hasDevEui = false;
        hasNwkSkey = false;
        hasNwkAddress = false;
        hasLoraSf = false;
        hasDataRate = false;
        hasActivation = false;
        hasLoraTxp = false;
        hasLoraInterval = false;
        hasLoraParam = false;
        hasBleParam = false;
        hasAppParam = false;
        hasConfirm = false;
        hasDemoMode = false;
        hasBatteryBeep = false;
        hasBeepMuteTime = false;
        hasLedStatus = false;
        hasAlertModeStatus = false;

        hasUploadInterval = false;
        hasIbeacon = false;
        hasEddyStone = false;
        hasCustomPackage = false;
        hasSensorBroadcast = false;
        hasSensorParam = false;
        hasDelay = false;
        hasMaxEirp = false;
        hasSglFrequency = false;
        hasSglDatarate = false;
        hasLbtStatus = false;
        hasLbtThreshold = false;
        hasRx2Datarate = false;
        hasRx2Frequency = false;
    }

    @Override
    public SensoroDevice clone() throws CloneNotSupportedException {
        SensoroDevice newDevice = null;
        try {
            newDevice = (SensoroDevice) super.clone();
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

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getProximityUUID() {
        return proximityUUID;
    }

    public void setProximityUUID(String proximityUUID) {
        this.proximityUUID = proximityUUID;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }


    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }


    public int getAccelerometerCount() {
        return accelerometerCount;
    }

    public void setAccelerometerCount(int accelerometerCount) {
        this.accelerometerCount = accelerometerCount;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public float getSf() {
        return sf;
    }

    public void setSf(float sf) {
        this.sf = sf;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getDevEui() {
        return devEui;
    }

    public void setDevEui(String devEui) {
        this.devEui = devEui;
    }

    public String getAppEui() {
        return appEui;
    }

    public void setAppEui(String appEui) {
        this.appEui = appEui;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSkey() {
        return appSkey;
    }

    public void setAppSkey(String appSkey) {
        this.appSkey = appSkey;
    }

    public String getNwkSkey() {
        return nwkSkey;
    }

    public void setNwkSkey(String nwkSkey) {
        this.nwkSkey = nwkSkey;
    }

    public int getDevAdr() {
        return devAdr;
    }

    public void setDevAdr(int devAdr) {
        this.devAdr = devAdr;
    }

    public int getLoraDr() {
        return loraDr;
    }

    public void setLoraDr(int loraDr) {
        this.loraDr = loraDr;
    }

    public int getLoraAdr() {
        return loraAdr;
    }

    public void setLoraAdr(int loraAdr) {
        this.loraAdr = loraAdr;
    }

    public int getLoraTxp() {
        return loraTxp;
    }

    public void setLoraTxp(int loraTxp) {
        this.loraTxp = loraTxp;
    }

    public float getLoraInt() {
        return loraInt;
    }

    public void setLoraInt(int loraInt) {
        this.loraInt = loraInt;
    }

    public int getBleTxp() {
        return bleTxp;
    }

    public void setBleTxp(int bleTxp) {
        this.bleTxp = bleTxp;
    }

    public float getBleInt() {
        return bleInt;
    }

    public void setBleInt(float bleInt) {
        this.bleInt = bleInt;
    }

    public int getBleOnTime() {
        return bleOnTime;
    }

    public void setBleOnTime(int bleOnTime) {
        this.bleOnTime = bleOnTime;
    }

    public int getBleOffTime() {
        return bleOffTime;
    }

    public void setBleOffTime(int bleOffTime) {
        this.bleOffTime = bleOffTime;
    }

    public void setLoraInt(float loraInt) {
        this.loraInt = loraInt;
    }

    public boolean isDfu() {
        return isDfu;
    }

    public void setDfu(boolean isDfu) {
        this.isDfu = isDfu;
    }


    public String getDfuInfo() {
        return dfuInfo;
    }

    public void setDfuInfo(String dfuInfo) {
        this.dfuInfo = dfuInfo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTempInterval() {
        return tempInterval;
    }

    public void setTempInterval(int tempInterval) {
        this.tempInterval = tempInterval;
    }

    public int getLightInterval() {
        return lightInterval;
    }

    public void setLightInterval(int lightInterval) {
        this.lightInterval = lightInterval;
    }

    public int getHumidityInterval() {
        return humidityInterval;
    }

    public void setIBeaconEnabled(boolean IBeaconEnabled) {
        isIBeaconEnabled = IBeaconEnabled;
    }

    public void setHumidityInterval(int humidityInterval) {
        this.humidityInterval = humidityInterval;
    }


    public SensoroSlot[] getSlotArray() {
        return slotArray;
    }

    public void setSlotArray(SensoroSlot[] slotArray) {
        this.slotArray = slotArray;
    }

    public int getClassBEnabled() {
        return classBEnabled;
    }

    public void setClassBEnabled(int classBEnabled) {
        this.classBEnabled = classBEnabled;
    }

    public int getClassBDataRate() {
        return classBDataRate;
    }

    public void setClassBDataRate(int classBDataRate) {
        this.classBDataRate = classBDataRate;
    }

    public int getClassBPeriodicity() {
        return classBPeriodicity;
    }

    public void setClassBPeriodicity(int classBPeriodicity) {
        this.classBPeriodicity = classBPeriodicity;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getDfuProgress() {
        return dfuProgress;
    }

    public void setDfuProgress(int dfuProgress) {
        this.dfuProgress = dfuProgress;
    }

    public byte getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(byte dataVersion) {
        this.dataVersion = dataVersion;
    }

    public boolean isIBeaconEnabled() {
        return isIBeaconEnabled;
    }

    public List<Integer> getChannelMaskList() {
        return channelMaskList;
    }

    public void setChannelMaskList(List<Integer> channelMaskList) {
        this.channelMaskList = channelMaskList;
    }

    public ArrayList<SensoroChannel> getChannelList() {
        return channelList;
    }

    public void setChannelList(ArrayList<SensoroChannel> channelList) {
        this.channelList = channelList;
    }


    @Override
    public boolean equals(Object that) {
        if (!(that instanceof SensoroDevice)) {
            return false;
        }
        SensoroDevice thatBeacon = (SensoroDevice) that;

        return (thatBeacon.macAddress.equals(this.macAddress));
    }

    @Override
    public int hashCode() {
        return macAddress.hashCode();
    }

    public boolean hasAdr() {
        return hasAdr;
    }

    public void setHasAdr(boolean hasAdr) {
        this.hasAdr = hasAdr;
    }

    public boolean hasAppEui() {
        return hasAppEui;
    }

    public void setHasAppEui(boolean hasAppEui) {
        this.hasAppEui = hasAppEui;
    }

    public boolean hasAppKey() {
        return hasAppKey;
    }

    public void setHasAppKey(boolean hasAppKey) {
        this.hasAppKey = hasAppKey;
    }

    public boolean hasAppSkey() {
        return hasAppSkey;
    }

    public void setHasAppSkey(boolean hasAppSkey) {
        this.hasAppSkey = hasAppSkey;
    }

    public boolean hasBleInterval() {
        return hasBleInterval;
    }

    public void setHasBleInterval(boolean hasBleInterval) {
        this.hasBleInterval = hasBleInterval;
    }

    public boolean hasBleOffTime() {
        return hasBleOffTime;
    }

    public void setHasBleOffTime(boolean hasBleOffTime) {
        this.hasBleOffTime = hasBleOffTime;
    }

    public boolean hasBleOnOff() {
        return hasBleOnOff;
    }

    public void setHasBleOnOff(boolean hasBleOnOff) {
        this.hasBleOnOff = hasBleOnOff;
    }

    public boolean hasBleOnTime() {
        return hasBleOnTime;
    }

    public void setHasBleOnTime(boolean hasBleOnTime) {
        this.hasBleOnTime = hasBleOnTime;
    }


    public int getActivation() {
        return activation;
    }

    public void setActivation(int activation) {
        this.activation = activation;
    }

    public boolean hasActivation() {
        return hasActivation;
    }

    public void setHasActivation(boolean hasActivation) {
        this.hasActivation = hasActivation;
    }

    public boolean hasBleTxp() {
        return hasBleTxp;
    }

    public void setHasBleTxp(boolean hasBleTxp) {
        this.hasBleTxp = hasBleTxp;
    }

    public boolean hasDataRate() {
        return hasDataRate;
    }

    public void setHasDataRate(boolean hasDataRate) {
        this.hasDataRate = hasDataRate;
    }

    public boolean hasDevAddr() {
        return hasDevAddr;
    }

    public void setHasDevAddr(boolean hasDevAddr) {
        this.hasDevAddr = hasDevAddr;
    }

    public boolean hasDevEui() {
        return hasDevEui;
    }

    public void setHasDevEui(boolean hasDevEui) {
        this.hasDevEui = hasDevEui;
    }

    public boolean hasLoraSf() {
        return hasLoraSf;
    }

    public void setHasLoraSf(boolean hasLoraSf) {
        this.hasLoraSf = hasLoraSf;
    }

    public boolean hasNwkAddress() {
        return hasNwkAddress;
    }

    public void setHasNwkAddress(boolean hasNwkAddress) {
        this.hasNwkAddress = hasNwkAddress;
    }

    public boolean hasNwkSkey() {
        return hasNwkSkey;
    }

    public void setHasNwkSkey(boolean hasNwkSkey) {
        this.hasNwkSkey = hasNwkSkey;
    }

    public boolean hasLoraTxp() {
        return hasLoraTxp;
    }

    public void setHasLoraTxp(boolean hasTxPower) {
        this.hasLoraTxp = hasTxPower;
    }


    public boolean hasAppParam() {
        return hasAppParam;
    }

    public void setHasAppParam(boolean hasAppParam) {
        this.hasAppParam = hasAppParam;
    }

    public boolean hasBleParam() {
        return hasBleParam;
    }

    public void setHasBleParam(boolean hasBleParam) {
        this.hasBleParam = hasBleParam;
    }

    public boolean hasLoraParam() {
        return hasLoraParam;
    }

    public void setHasLoraParam(boolean hasLoraParam) {
        this.hasLoraParam = hasLoraParam;
    }


    public Integer getUploadInterval() {
        return uploadInterval;
    }

    public void setUploadInterval(Integer uploadInterval) {
        this.uploadInterval = uploadInterval;
    }

    public boolean hasUploadInterval() {
        return hasUploadInterval;
    }

    public void setHasUploadInterval(boolean hasUploadInterval) {
        this.hasUploadInterval = hasUploadInterval;
    }

    public Integer getConfirm() {
        return confirm;
    }

    public void setConfirm(Integer confirm) {
        this.confirm = confirm;
    }

    public Integer getDemoMode() {
        return demoMode;
    }

    public Integer getBatteryBeep() {
        return batteryBeep;
    }

    public Integer getBeepMuteTime() {
        return beepMuteTime;
    }

    public Integer getLedStatus() {
        return ledStatus;
    }

    public void setDemoMode(Integer demoMode) {
        this.demoMode = demoMode;
    }

    public void setBatteryBeep(Integer batteryBeep) {
        this.batteryBeep = batteryBeep;
    }

    public void setBeepMuteTime(Integer beepMuteTime) {
        this.beepMuteTime = beepMuteTime;
    }

    public void setLedStatus(Integer ledStatus) {
        this.ledStatus = ledStatus;
    }

    public boolean hasConfirm() {
        return hasConfirm;
    }

    public void setHasConfirm(boolean hasConfirm) {
        this.hasConfirm = hasConfirm;
    }

    public boolean hasDemoMode() {
        return hasDemoMode;
    }

    public boolean hasBatteryBeep() {
        return hasBatteryBeep;
    }

    public boolean hasBeepMuteTime() {
        return hasBeepMuteTime;
    }

    public boolean hasLedStatus() {
        return hasLedStatus;
    }

    public void setHasDemoMode(boolean hasDemoMode) {
        this.hasDemoMode = hasDemoMode;
    }

    public void setHasBatteryBeep(boolean hasBatteryBeep) {
        this.hasBatteryBeep = hasBatteryBeep;
    }

    public void setHasBeepMuteTime(boolean hasBeepMuteTime) {
        this.hasBeepMuteTime = hasBeepMuteTime;
    }

    public void setHasLedStatus(boolean hasLedStatus) {
        this.hasLedStatus = hasLedStatus;
    }

//    public SensoroSensor getSensoroSensor() {
//        return sensoroSensor;
//    }
//
//    public void setSensoroSensor(SensoroSensor sensoroSensor) {
//        this.sensoroSensor = sensoroSensor;
//    }

    public SensoroSensor getSensoroSensorTest() {
        return sensoroSensorTest;
    }

    public void setSensoroSensorTest(SensoroSensor sensoroSensorTest) {
        this.sensoroSensorTest = sensoroSensorTest;
    }

    public boolean hasLoraInterval() {
        return hasLoraInterval;
    }

    public void setHasLoraInterval(boolean hasLoraInterval) {
        this.hasLoraInterval = hasLoraInterval;
    }

    public boolean hasEddyStone() {
        return hasEddyStone;
    }

    public void setHasEddyStone(boolean hasEddyStone) {
        this.hasEddyStone = hasEddyStone;
    }

    public boolean hasIbeacon() {
        return hasIbeacon;
    }

    public void setHasIbeacon(boolean hasIbeacon) {
        this.hasIbeacon = hasIbeacon;
    }

    public void setActivation(Integer activation) {
        this.activation = activation;
    }

    public boolean hasCustomPackage() {
        return hasCustomPackage;
    }

    public void setHasCustomPackage(boolean hasCustomPackage) {
        this.hasCustomPackage = hasCustomPackage;
    }

    public boolean hasSensorBroadcast() {
        return hasSensorBroadcast;
    }

    public void setHasSensorBroadcast(boolean hasSensorBroadcast) {
        this.hasSensorBroadcast = hasSensorBroadcast;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public int getMaxEirp() {
        return maxEirp;
    }

    public SensoroDevice setMaxEirp(int maxEirp) {
        this.maxEirp = maxEirp;
        return this;
    }

    public int getSglStatus() {
        return sglStatus;
    }

    public SensoroDevice setSglStatus(int sglStatus) {
        this.sglStatus = sglStatus;
        return this;
    }

    public int getSglFrequency() {
        return sglFrequency;
    }

    public SensoroDevice setSglFrequency(int sglFrequency) {
        this.sglFrequency = sglFrequency;
        return this;
    }


    public int getRx2Frequency() {
        return rx2Frequency;
    }

    public void setRx2Frequency(int rx2Frequency) {
        this.rx2Frequency = rx2Frequency;
    }

    public int getRx2Datarate() {
        return rx2Datarate;
    }

    public void setRx2Datarate(int rx2Datarate) {
        this.rx2Datarate = rx2Datarate;
    }

    public int getLbtStatus() {
        return lbtStatus;
    }

    public void setLbtStatus(int lbtStatus) {
        this.lbtStatus = lbtStatus;
    }

    public int getLbtThreshold() {
        return lbtThreshold;
    }

    public void setLbtThreshold(int lbtThreshold) {
        this.lbtThreshold = lbtThreshold;
    }

    public int getSglDatarate() {
        return sglDatarate;
    }

    public SensoroDevice setSglDatarate(int sglDatarate) {
        this.sglDatarate = sglDatarate;
        return this;
    }

    public boolean hasMaxEirp() {
        return hasMaxEirp;
    }

    public void setHasMaxEirp(boolean hasMaxEirp) {
        this.hasMaxEirp = hasMaxEirp;
    }

    public boolean hasDelay() {
        return hasDelay;
    }

    public void setHasDelay(boolean hasDelay) {
        this.hasDelay = hasDelay;
    }

    public boolean hasSensorParam() {
        return hasSensorParam;
    }

    public void setHasSensorParam(boolean hasSensorParam) {
        this.hasSensorParam = hasSensorParam;
    }

    public boolean hasAlertModeStatus() {
        return hasAlertModeStatus;
    }

    public void setHasAlertModeStatus(boolean hasAlertModeStatus) {
        this.hasAlertModeStatus = hasAlertModeStatus;
    }

    public Integer getAlertModeStatus() {
        return alertModeStatus;
    }

    public void setAlertModeStatus(Integer alertModeStatus) {
        this.alertModeStatus = alertModeStatus;
    }

    public ArrayList<Integer> getCmdArrayList() {
        return cmdArrayList;
    }

    public void setCmdArrayList(ArrayList<Integer> cmdArrayList) {
        this.cmdArrayList = cmdArrayList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeString(this.proximityUUID);
        dest.writeInt(this.accelerometerCount);
        dest.writeInt(this.power);
        dest.writeFloat(this.sf);
        dest.writeString(this.devEui);
        dest.writeString(this.appEui);
        dest.writeString(this.appKey);
        dest.writeString(this.appSkey);
        dest.writeString(this.nwkSkey);
        dest.writeString(this.password);
        dest.writeString(this.dfuInfo);
        dest.writeString(this.band);
        dest.writeInt(this.devAdr);
        dest.writeInt(this.loraDr);
        dest.writeInt(this.loraAdr);
        dest.writeInt(this.loraTxp);
        dest.writeInt(this.dfuProgress);
        dest.writeFloat(this.loraInt);
        dest.writeInt(this.bleTxp);
        dest.writeFloat(this.bleInt);
        dest.writeInt(this.bleOnTime);
        dest.writeInt(this.bleOffTime);
        dest.writeInt(this.tempInterval);
        dest.writeInt(this.lightInterval);
        dest.writeInt(this.humidityInterval);
        dest.writeInt(this.classBEnabled);
        dest.writeInt(this.classBDataRate);
        dest.writeInt(this.classBPeriodicity);
        dest.writeValue(this.uploadInterval);
        dest.writeValue(this.confirm);
        dest.writeValue(this.demoMode);
        dest.writeValue(this.batteryBeep);
        dest.writeValue(this.beepMuteTime);
        dest.writeValue(this.ledStatus);
        dest.writeValue(this.alertModeStatus);
        dest.writeValue(this.activation);
        dest.writeValue(this.delay);
        dest.writeList(this.channelList);
        dest.writeList(this.cmdArrayList);
        dest.writeByte(this.dataVersion);
        dest.writeByte(this.isIBeaconEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isDfu ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBleInterval ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBleOffTime ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBleOnTime ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBleOnOff ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBleTxp ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAdr ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAppEui ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAppKey ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAppSkey ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasDevAddr ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasDevEui ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasNwkSkey ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasNwkAddress ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasLoraSf ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasDataRate ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasActivation ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasLoraTxp ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasLoraInterval ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasLoraParam ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBleParam ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAppParam ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasConfirm ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasDemoMode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBatteryBeep ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasBeepMuteTime ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasLedStatus ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAlertModeStatus ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasUploadInterval ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasEddyStone ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasIbeacon ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasSensorBroadcast ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasSensorParam ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasCustomPackage ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasDelay ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasLbtStatus ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasLbtThreshold ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasSglFrequency ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasRx2Frequency ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasRx2Datarate ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasSglDatarate ? (byte) 1 : (byte) 0);
        dest.writeTypedArray(this.slotArray, flags);
        dest.writeParcelable(this.sensoroSensorTest, flags);
        dest.writeLong(this.lastFoundTime);
        dest.writeValue(this.alarmStepHigh);
        dest.writeValue(this.alarmStepLow);
        dest.writeValue(this.alarmHigh);
        dest.writeValue(this.alarmLow);
        dest.writeByte(this.hasAlarmHigh ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAlarmLow ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAlarmStepHigh ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasAlarmStepLow ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hasMultiTemperature ? (byte) 1 : (byte) 0);
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

    protected SensoroDevice(Parcel in) {
        super(in);
        this.major = in.readInt();
        this.minor = in.readInt();
        this.proximityUUID = in.readString();
        this.accelerometerCount = in.readInt();
        this.power = in.readInt();
        this.sf = in.readFloat();
        this.devEui = in.readString();
        this.appEui = in.readString();
        this.appKey = in.readString();
        this.appSkey = in.readString();
        this.nwkSkey = in.readString();
        this.password = in.readString();
        this.dfuInfo = in.readString();
        this.band = in.readString();
        this.devAdr = in.readInt();
        this.loraDr = in.readInt();
        this.loraAdr = in.readInt();
        this.loraTxp = in.readInt();
        this.dfuProgress = in.readInt();
        this.loraInt = in.readFloat();
        this.bleTxp = in.readInt();
        this.bleInt = in.readFloat();
        this.bleOnTime = in.readInt();
        this.bleOffTime = in.readInt();
        this.tempInterval = in.readInt();
        this.lightInterval = in.readInt();
        this.humidityInterval = in.readInt();
        this.classBEnabled = in.readInt();
        this.classBDataRate = in.readInt();
        this.classBPeriodicity = in.readInt();
        this.uploadInterval = (Integer) in.readValue(Integer.class.getClassLoader());
        this.confirm = (Integer) in.readValue(Integer.class.getClassLoader());
        this.demoMode = (Integer) in.readValue(Integer.class.getClassLoader());
        this.batteryBeep = (Integer) in.readValue(Integer.class.getClassLoader());
        this.beepMuteTime = (Integer) in.readValue(Integer.class.getClassLoader());
        this.ledStatus = (Integer) in.readValue(Integer.class.getClassLoader());
        this.alertModeStatus = (Integer) in.readValue(Integer.class.getClassLoader());
        this.activation = (Integer) in.readValue(Integer.class.getClassLoader());
        this.delay = (Integer) in.readValue(Integer.class.getClassLoader());
        this.channelList = new ArrayList<SensoroChannel>();
        in.readList(this.channelList, SensoroChannel.class.getClassLoader());
        this.cmdArrayList = new ArrayList<Integer>();
        in.readList(this.cmdArrayList, Integer.class.getClassLoader());
        this.dataVersion = in.readByte();
        this.isIBeaconEnabled = in.readByte() != 0;
        this.isDfu = in.readByte() != 0;
        this.hasBleInterval = in.readByte() != 0;
        this.hasBleOffTime = in.readByte() != 0;
        this.hasBleOnTime = in.readByte() != 0;
        this.hasBleOnOff = in.readByte() != 0;
        this.hasBleTxp = in.readByte() != 0;
        this.hasAdr = in.readByte() != 0;
        this.hasAppEui = in.readByte() != 0;
        this.hasAppKey = in.readByte() != 0;
        this.hasAppSkey = in.readByte() != 0;
        this.hasDevAddr = in.readByte() != 0;
        this.hasDevEui = in.readByte() != 0;
        this.hasNwkSkey = in.readByte() != 0;
        this.hasNwkAddress = in.readByte() != 0;
        this.hasLoraSf = in.readByte() != 0;
        this.hasDataRate = in.readByte() != 0;
        this.hasActivation = in.readByte() != 0;
        this.hasLoraTxp = in.readByte() != 0;
        this.hasLoraInterval = in.readByte() != 0;
        this.hasLoraParam = in.readByte() != 0;
        this.hasBleParam = in.readByte() != 0;
        this.hasAppParam = in.readByte() != 0;
        this.hasConfirm = in.readByte() != 0;
        this.hasDemoMode = in.readByte() != 0;
        this.hasBatteryBeep = in.readByte() != 0;
        this.hasBeepMuteTime = in.readByte() != 0;
        this.hasLedStatus = in.readByte() != 0;
        this.hasAlertModeStatus = in.readByte() != 0;
        this.hasUploadInterval = in.readByte() != 0;
        this.hasEddyStone = in.readByte() != 0;
        this.hasIbeacon = in.readByte() != 0;
        this.hasSensorBroadcast = in.readByte() != 0;
        this.hasSensorParam = in.readByte() != 0;
        this.hasCustomPackage = in.readByte() != 0;
        this.hasDelay = in.readByte() != 0;
        this.hasLbtStatus = in.readByte() != 0;
        this.hasLbtThreshold = in.readByte() != 0;
        this.hasSglFrequency = in.readByte() != 0;
        this.hasRx2Frequency = in.readByte() != 0;
        this.hasRx2Datarate = in.readByte() != 0;
        this.hasSglDatarate = in.readByte() != 0;
        this.slotArray = in.createTypedArray(SensoroSlot.CREATOR);
        this.sensoroSensorTest = in.readParcelable(SensoroSensor.class.getClassLoader());
        this.lastFoundTime = in.readLong();
        this.alarmStepHigh = (Integer) in.readValue(Integer.class.getClassLoader());
        this.alarmStepLow = (Integer) in.readValue(Integer.class.getClassLoader());
        this.alarmHigh = (Integer) in.readValue(Integer.class.getClassLoader());
        this.alarmLow = (Integer) in.readValue(Integer.class.getClassLoader());
        this.hasAlarmHigh = in.readByte() != 0;
        this.hasAlarmLow = in.readByte() != 0;
        this.hasAlarmStepHigh = in.readByte() != 0;
        this.hasAlarmStepLow = in.readByte() != 0;
        this.hasMultiTemperature = in.readByte() != 0;
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

    public static final Creator<SensoroDevice> CREATOR = new Creator<SensoroDevice>() {
        @Override
        public SensoroDevice createFromParcel(Parcel source) {
            return new SensoroDevice(source);
        }

        @Override
        public SensoroDevice[] newArray(int size) {
            return new SensoroDevice[size];
        }
    };
}

