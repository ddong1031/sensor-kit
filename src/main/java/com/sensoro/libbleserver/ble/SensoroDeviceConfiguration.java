package com.sensoro.libbleserver.ble;


import com.sensoro.libbleserver.ble.bean.SensoroChannel;

import java.util.List;

/**
 * Created by Sensoro on 15/7/27.
 */
public class SensoroDeviceConfiguration {

    String password = null;
    Integer major = null;
    Integer minor = null;
    Boolean isIBeaconEnabled = null;
    String proximityUUID = null;
    Integer bleTurnOnTime = null;
    Integer bleTurnOffTime = null;
    Integer bleTxp = null;
    Float bleInt = null;
    String devEui = null;
    String appEui = null;
    String appKey = null;
    String appSkey = null;
    String nwkSkey = null;
    Integer devAdr = null;
    Integer loadAdr = null;
    Integer loraDr = null;
    Integer loraTxp = null;
    Integer loraEirp = null;
    Integer sglStatus = null;
    Integer sglDatarate = null;
    Integer sglFrequency = null;
    Integer flame = null;
    Float loraInt = null;
    Boolean isEddystoneUIDEnabled = null;
    Boolean isEddystoneURLEnabled = null;
    Boolean isEddystoneTLMEnabled = null;
    String eddystoneUID = null;
    String eddystoneURL = null;
    Boolean isAliBeaconEnabled = null;
    Boolean isEddystoneOnly = null;
    String customPackage1 = null;
    String customPackage2 = null;
    String customPackage3 = null;
    Boolean isCustomPackage1Enabled = null;
    Boolean isCustomPackage2Enabled = null;
    Boolean isCustomPackage3Enabled = null;
    SensoroSlot[] sensoroSlots = null;
    Integer classBEnabled = null;
    Integer classBDateRate = null;
    Integer classBPeriodicity = null;
    Integer sensorBroadcastEnabled = null;
    Integer activation = null;
    Integer uploadIntervalData = null;
    Integer confirmData = null;
    Integer delay = null;
    List<Integer> channelList = null;
    List<SensoroChannel> channels = null;
    SensoroSensorConfiguration sensoroSensorConfiguration;
    boolean hasUploadInterval;
    boolean hasConfirm;
    boolean hasActivation;
    boolean hasAppParam;
    boolean hasBleParam;
    boolean hasLoraParam;
    boolean hasDevEui;
    boolean hasAppEui;
    boolean hasAppKey;
    boolean hasAppSkey;
    boolean hasNwkSkey;
    boolean hasDevAddr;
    boolean hasLoraInterval;
    boolean hasIBeacon;
    boolean hasEddystone;
    boolean hasSensorBroadcast;
    boolean hasCustom;
    boolean hasFlame;
    boolean hasDelay;
    boolean hasSglStauts;
    boolean hasSglDataRate;

    public boolean isHasSglStauts() {
        return hasSglStauts;
    }

    public void setHasSglStauts(boolean hasSglStauts) {
        this.hasSglStauts = hasSglStauts;
    }

    public boolean isHasSglDataRate() {
        return hasSglDataRate;
    }

    public void setHasSglDataRate(boolean hasSglDataRate) {
        this.hasSglDataRate = hasSglDataRate;
    }

    public boolean isHasSglFrequency() {
        return hasSglFrequency;
    }

    public void setHasSglFrequency(boolean hasSglFrequency) {
        this.hasSglFrequency = hasSglFrequency;
    }

    boolean hasSglFrequency;
    boolean hasAlarmHigh;
    boolean hasAlarmLow;
    boolean hasAlarmStepHigh;
    boolean hasAlarmStepLow;
    boolean hasMultiTemperature;
    /**
     * 报警设定的上下限的步长支持
     */
    Integer alarmStepHigh;
    Integer alarmStepLow;
    Integer alarmHigh;
    Integer alarmLow;

    protected SensoroDeviceConfiguration() {

    }

    protected SensoroDeviceConfiguration(Builder builder) {
        password = builder.password;
        isIBeaconEnabled = builder.isIBeaconEnabled;
        proximityUUID = builder.proximityUUID;
        major = builder.major;
        minor = builder.minor;
        bleTurnOnTime = builder.bleTurnOnTime;
        bleTurnOffTime = builder.bleTurnOffTime;
        bleTxp = builder.bleTxp;
        bleInt = builder.bleInt;
        loraInt = builder.loraInt;
        loraTxp = builder.loraTxp;
        devEui = builder.devEui;
        appEui = builder.appEui;
        appKey = builder.appKey;
        appSkey = builder.appSkey;
        nwkSkey = builder.nwkSkey;
        devAdr = builder.devAdr;
        loadAdr = builder.loraAdr;
        loraDr = builder.loraDr;
        loraEirp = builder.loraEirp;
        sglStatus = builder.sglStatus;
        sglFrequency = builder.sglFrequency;
        sglDatarate = builder.sglDatarate;
        flame = builder.flame;
        isEddystoneOnly = builder.isEddystoneOnly;
        isEddystoneTLMEnabled = builder.isEddystoneTLMEnabled;
        isEddystoneUIDEnabled = builder.isEddystoneUIDEnabled;
        isEddystoneURLEnabled = builder.isEddystoneURLEnabled;
        eddystoneUID = builder.eddystoneUID;
        eddystoneURL = builder.eddystoneURL;
        isAliBeaconEnabled = builder.isAliBeaconEnabled;
        customPackage1 = builder.customPackage1;
        customPackage2 = builder.customPackage2;
        customPackage3 = builder.customPackage3;
        isCustomPackage1Enabled = builder.isCustomPackage1Enabled;
        isCustomPackage2Enabled = builder.isCustomPackage2Enabled;
        isCustomPackage3Enabled = builder.isCustomPackage3Enabled;
        sensoroSlots = builder.sensoroSlots;
        classBEnabled = builder.classBEnabled;
        classBDateRate = builder.classBDataRate;
        classBPeriodicity = builder.classBPeriodicity;
        sensorBroadcastEnabled = builder.sensorBroadcastEnabled;
        sensoroSensorConfiguration = builder.sensorConfiguration;
        activation = builder.activation;
        uploadIntervalData = builder.uploadIntervalData;
        confirmData = builder.confirmData;
        delay = builder.delay;
        channelList = builder.channelList;
        channels = builder.channels;
        hasUploadInterval = builder.hasUploadInterval;
        hasConfirm = builder.hasConfirm;
        hasActivation = builder.hasActivation;
        hasAppParam = builder.hasAppParam;
        hasBleParam = builder.hasBleParam;
        hasLoraParam = builder.hasLoraParam;
        hasDevEui = builder.hasDevEui;
        hasAppEui = builder.hasAppEui;
        hasAppKey = builder.hasAppKey;
        hasAppSkey = builder.hasAppSkey;
        hasNwkSkey = builder.hasNwkSkey;
        hasDevAddr = builder.hasDevAddr;
        hasLoraInterval = builder.hasLoraInterval;
        hasIBeacon = builder.hasIBeacon;
        hasEddystone = builder.hasEddystone;
        hasSensorBroadcast = builder.hasSensorBroadcast;
        hasCustom = builder.hasCustom;
        hasFlame = builder.hasFlame;
        hasDelay = builder.hasDelay;
        hasSglStauts = builder.hasSglStatus;
        hasSglDataRate = builder.hasSglDataRate;
        hasSglFrequency = builder.hasSglFrequency;
        //
        alarmStepHigh = builder.alarmStepHigh;
        alarmStepLow = builder.alarmStepLow;
        alarmHigh = builder.alarmHigh;
        alarmLow = builder.alarmLow;
        hasAlarmHigh = builder.hasAlarmHigh;
        hasAlarmLow = builder.hasAlarmLow;
        hasAlarmStepHigh = builder.hasAlarmStepHigh;
        hasAlarmStepLow = builder.hasAlarmStepLow;
        hasMultiTemperature = builder.hasMultiTemperature;
    }

    public String getPassword() {
        return password;
    }

    public Integer getMajor() {
        return major;
    }

    public Integer getMinor() {
        return minor;
    }


    public Boolean getIBeaconEnabled() {
        return isIBeaconEnabled;
    }

    public String getProximityUUID() {
        return proximityUUID;
    }


    public Integer getBleTurnOnTime() {
        return bleTurnOnTime;
    }

    public Integer getBleTurnOffTime() {
        return bleTurnOffTime;
    }

    public Integer getBleTxp() {
        return bleTxp;
    }

    public Float getBleInt() {
        return bleInt;
    }

    public String getDevEui() {
        return devEui;
    }

    public String getAppEui() {
        return appEui;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSkey() {
        return appSkey;
    }

    public String getNwkSkey() {
        return nwkSkey;
    }

    public Integer getDevAdr() {
        return devAdr;
    }

    public Integer getLoraAdr() {
        return loadAdr;
    }

    public Integer getLoraTxp() {
        return loraTxp;
    }

    public Float getLoraInt() {
        return loraInt;
    }

    public Boolean getEddystoneUIDEnabled() {
        return isEddystoneUIDEnabled;
    }

    public Boolean getEddystoneURLEnabled() {
        return isEddystoneURLEnabled;
    }

    public Boolean getEddystoneTLMEnabled() {
        return isEddystoneTLMEnabled;
    }

    public String getEddystoneUID() {
        return eddystoneUID;
    }

    public String getEddystoneURL() {
        return eddystoneURL;
    }

    public Boolean getAliBeaconEnabled() {
        return isAliBeaconEnabled;
    }

    public Boolean getEddystoneOnly() {
        return isEddystoneOnly;
    }

    public String getCustomPackage1() {
        return customPackage1;
    }

    public String getCustomPackage2() {
        return customPackage2;
    }

    public String getCustomPackage3() {
        return customPackage3;
    }

    public Boolean getCustomPackage1Enabled() {
        return isCustomPackage1Enabled;
    }

    public Boolean getCustomPackage2Enabled() {
        return isCustomPackage2Enabled;
    }

    public Boolean getCustomPackage3Enabled() {
        return isCustomPackage3Enabled;
    }

    public SensoroSlot[] getSensoroSlots() {
        return sensoroSlots;
    }

    public int getClassBEnabled() {
        return classBEnabled;
    }

    public int getClassDateRate() {
        return classBDateRate;
    }

    public int getClassPeriodicity() {
        return classBPeriodicity;
    }

    public int getActivation() {
        return activation;
    }

    public Integer getClassBDateRate() {
        return classBDateRate;
    }

    public Integer getClassBPeriodicity() {
        return classBPeriodicity;
    }

    public Integer getConfirmData() {
        return confirmData;
    }

    public List<Integer> getChannelList() {
        return channelList;
    }
    public List<SensoroChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<SensoroChannel> channels) {
        this.channels = channels;
    }

    public boolean hasAppParam() {
        return hasAppParam;
    }

    public boolean hasBleParam() {
        return hasBleParam;
    }

    public boolean hasConfirm() {
        return hasConfirm;
    }

    public boolean hasLoraParam() {
        return hasLoraParam;
    }

    public boolean hasDevEui() {
        return hasDevEui;
    }

    public boolean hasAppEui() {
        return hasAppEui;
    }

    public boolean hasAppKey() {
        return hasAppKey;
    }

    public boolean hasAppSkey() {
        return hasAppSkey;
    }

    public boolean hasNwkSkey() {
        return hasNwkSkey;
    }

    public boolean hasDevAddr() {
        return hasDevAddr;
    }

    public boolean hasSglStatus() {
        return hasSglStauts;
    }
    public boolean hasSglDataRate() {
        return hasSglDataRate;
    }
    public boolean hasSglFrequency() {
        return hasSglFrequency;
    }

    public boolean hasIBeacon() {
        return hasIBeacon;
    }

    public boolean hasSensorBroadcast() {
        return hasSensorBroadcast;
    }

    public boolean hasCustom() {
        return hasCustom;
    }

    public boolean hasEddystone() {
        return hasEddystone;
    }

    public boolean hasLoraInterval() {
        return hasLoraInterval;
    }

    public boolean hasUploadInterval() {
        return hasUploadInterval;
    }

    public Integer getLoadAdr() {
        return loadAdr;
    }

    public Integer getSensorBroadcastEnabled() {
        return sensorBroadcastEnabled;
    }


    public SensoroSensorConfiguration getSensoroSensorConfiguration() {
        return sensoroSensorConfiguration;
    }

    public Integer getUploadIntervalData() {
        return uploadIntervalData;
    }

    public boolean hasActivation() {
        return hasActivation;
    }


    public boolean hasDelay() {
        return hasDelay;
    }

    public SensoroSensorConfiguration getSensorConfiguration() {
        return sensoroSensorConfiguration;
    }

    public Integer getLoraDr() {
        return loraDr;
    }

    public Integer getLoraEirp() {
        return loraEirp;
    }

    public Integer getSglStatus() {
        return sglStatus;
    }

    public Integer getSglDatarate() {
        return sglDatarate;
    }

    public Integer getSglFrequency() {
        return sglFrequency;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getFlame() {
        return flame;
    }

    public boolean hasFlame() {
        return hasFlame;
    }

    public static class Builder {
        private String password = "";
        private Boolean isIBeaconEnabled = null;
        private String proximityUUID = null;
        private Integer major = null;
        private Integer minor = null;

        private Integer bleTurnOnTime = null;
        private Integer bleTurnOffTime = null;
        private Integer bleTxp = null;
        private Float bleInt = null;
        private Integer loraTxp = null;
        private Float loraInt = null;
        private String devEui = null;
        private String appEui = null;
        private String appKey = null;
        private String appSkey = null;
        private String nwkSkey = null;
        private Integer devAdr = null;
        private Integer loraAdr = null;
        private Integer loraDr = null;
        private Integer loraEirp = null;
        private Integer sglStatus = null;
        private Integer sglFrequency = null;
        private Integer sglDatarate = null;
        private Integer flame;
        private Boolean isEddystoneUIDEnabled = null;
        private Boolean isEddystoneURLEnabled = null;
        private Boolean isEddystoneTLMEnabled = null;
        private String eddystoneUID = null;
        private String eddystoneURL = null;
        private Boolean isAliBeaconEnabled = null;
        private Boolean isEddystoneOnly = null;
        private String customPackage1 = null;
        private String customPackage2 = null;
        private String customPackage3 = null;
        private Boolean isCustomPackage1Enabled = null;
        private Boolean isCustomPackage2Enabled = null;
        private Boolean isCustomPackage3Enabled = null;
        private SensoroSlot[] sensoroSlots;
        private Integer classBEnabled;
        private Integer classBDataRate;
        private Integer classBPeriodicity;
        private Integer sensorBroadcastEnabled;
        private Integer activation;
        private Integer uploadIntervalData;
        private Integer confirmData;
        private Integer delay;

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

        public boolean hasMultiTemperature() {
            return hasMultiTemperature;
        }

        public void setHasMultiTemperature(boolean hasMultiTemperature) {
            this.hasMultiTemperature = hasMultiTemperature;
        }

        private List<Integer> channelList;
        private List<SensoroChannel> channels;
        private boolean hasDelay;
        private boolean hasUploadInterval;
        private boolean hasConfirm;
        private boolean hasActivation;
        private boolean hasAppParam;
        private boolean hasBleParam;
        private boolean hasLoraParam;
        private boolean hasDevEui;
        private boolean hasAppEui;
        private boolean hasAppKey;
        private boolean hasAppSkey;
        private boolean hasNwkSkey;
        private boolean hasDevAddr;
        private boolean hasLoraInterval;
        private boolean hasIBeacon;
        private boolean hasEddystone;
        private boolean hasSensorBroadcast;
        private boolean hasCustom;
        private boolean hasFlame;
        private boolean hasSglStatus;
        private boolean hasSglDataRate;
        private boolean hasSglFrequency;
        private SensoroSensorConfiguration sensorConfiguration;

        public Builder() {
            this.alarmHigh = null;
            this.alarmLow = null;
            this.alarmStepHigh = null;
            this.alarmStepLow = null;
            this.password = "";
            this.isIBeaconEnabled = null;
            this.proximityUUID = null;
            this.major = null;
            this.minor = null;
            this.bleInt = null;
            this.bleTxp = null;
            this.bleTurnOffTime = null;
            this.bleTurnOnTime = null;
            this.loraInt = null;
            this.loraTxp = null;
            this.devEui = null;
            this.appEui = null;
            this.appKey = null;
            this.appSkey = null;
            this.nwkSkey = null;
            this.devAdr = null;
            this.flame = null;
            this.isEddystoneUIDEnabled = null;
            this.isEddystoneURLEnabled = null;
            this.isEddystoneTLMEnabled = null;
            this.eddystoneUID = null;
            this.eddystoneURL = null;
            this.isAliBeaconEnabled = null;
            this.isEddystoneOnly = null;
            this.customPackage1 = null;
            this.customPackage2 = null;
            this.customPackage3 = null;
            this.isCustomPackage1Enabled = null;
            this.isCustomPackage2Enabled = null;
            this.isCustomPackage3Enabled = null;
            this.sensoroSlots = null;
            this.classBEnabled = null;
            this.classBDataRate = null;
            this.classBPeriodicity = null;
            this.sensorBroadcastEnabled = null;
            this.sensorConfiguration = null;
            this.activation = null;
            this.uploadIntervalData = null;
            this.confirmData = null;
            this.delay = null;
            this.channelList = null;
            this.channels = null;
            this.hasDelay = false;
            this.hasSglStatus = false;
            this.hasSglDataRate = false;
            this.hasSglFrequency = false;
            this.hasUploadInterval = false;
            this.hasConfirm = false;
            this.hasActivation = false;
            this.hasAppParam = false;
            this.hasBleParam = false;
            this.hasLoraParam = false;
            this.hasDevEui = false;
            this.hasAppEui = false;
            this.hasAppKey = false;
            this.hasAppSkey = false;
            this.hasNwkSkey = false;
            this.hasDevAddr = false;
            this.hasLoraInterval = false;
            this.hasIBeacon = false;
            this.hasEddystone = false;
            this.hasSensorBroadcast = false;
            this.hasCustom = false;
            this.hasFlame = false;

        }

        /**
         * Set the password of beacon. If password is null, it will disable the password of beacon.
         *
         * @param password
         * @return
         */
        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setIBeaconEnabled(boolean isIBeaconEnabled) {
            this.isIBeaconEnabled = isIBeaconEnabled;
            return this;
        }

        public Builder setProximityUUID(String proximityUUID) {
            this.proximityUUID = proximityUUID;
            return this;
        }

        public Builder setMajor(int major) {
            this.major = major;
            return this;
        }

        public Builder setMinor(int minor) {
            this.minor = minor;
            return this;
        }


        public Builder setEddystoneUIDEnabled(boolean isEddystoneUIDEnabled) {
            this.isEddystoneUIDEnabled = isEddystoneUIDEnabled;
            return this;
        }

        public Builder setEddystoneURLEnabled(boolean isEddystoneURLEnabled) {
            this.isEddystoneURLEnabled = isEddystoneURLEnabled;
            return this;
        }

        public Builder setEddystoneTLMEnabled(boolean isEddystoneTLMEnabled) {
            this.isEddystoneTLMEnabled = isEddystoneTLMEnabled;
            return this;
        }

        public Builder setEddystoneUID(String eddystoneUID) {
            this.eddystoneUID = eddystoneUID;
            return this;
        }

        public Builder setEddystoneURL(String eddystoneURL) {
            this.eddystoneURL = eddystoneURL;
            return this;
        }

        public Builder setAliBeaconEnabled(boolean isAliBeaconEnabled) {
            this.isAliBeaconEnabled = isAliBeaconEnabled;
            return this;
        }

        public Builder setEddystoneOnly(Boolean isEddystoneOnly) {
            this.isEddystoneOnly = isEddystoneOnly;
            return this;
        }

        public Builder setBleTurnOnTime(Integer bleTurnOnTime) {
            this.bleTurnOnTime = bleTurnOnTime;
            return this;
        }

        public Builder setHasLoraInterval(boolean hasLoraInterval) {
            this.hasLoraInterval = hasLoraInterval;
            return this;
        }

        public Builder setBleTurnOffTime(Integer bleTurnOffTime) {
            this.bleTurnOffTime = bleTurnOffTime;
            return this;
        }

        public Builder setBleTxp(Integer bleTxp) {
            this.bleTxp = bleTxp;
            return this;
        }

        public Builder setBleInt(Float bleInt) {
            this.bleInt = bleInt;
            return this;
        }

        public Builder setCustomPackage1(String customPackage1) {
            this.customPackage1 = customPackage1;
            return this;
        }

        public Builder setCustomPackage2(String customPackage2) {
            this.customPackage2 = customPackage2;
            return this;
        }

        public Builder setCustomPackage3(String customPackage3) {
            this.customPackage3 = customPackage3;
            return this;
        }

        public Builder setCustomPackage1Enabled(boolean isEnabled) {
            this.isCustomPackage1Enabled = isEnabled;
            return this;
        }

        public Builder setCustomPackage2Enabled(boolean isEnabled) {
            this.isCustomPackage2Enabled = isEnabled;
            return this;
        }

        public Builder setCustomPackage3Enabled(boolean isEnabled) {
            this.isCustomPackage3Enabled = isEnabled;
            return this;
        }

        public Builder setDevEui(String devEui) {
            this.devEui = devEui;
            return this;
        }

        public Builder setAppEui(String appEui) {
            this.appEui = appEui;
            return this;
        }

        public Builder setAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder setAppSkey(String appSkey) {
            this.appSkey = appSkey;
            return this;
        }

        public Builder setNwkSkey(String nwkSkey) {
            this.nwkSkey = nwkSkey;
            return this;
        }

        public Builder setDevAdr(int devAdr) {
            this.devAdr = devAdr;
            return this;
        }

        public Builder setLoraAdr(int loraAdr) {
            this.loraAdr = loraAdr;
            return this;
        }

        public Builder setLoraDr(int loraDr) {
            this.loraDr = loraDr;
            return this;
        }

        public Builder setLoraInt(Float loraInt) {
            this.loraInt = loraInt;
            return this;
        }

        public Builder setLoraTxp(int loraTxp) {
            this.loraTxp = loraTxp;
            return this;
        }

        public Builder setSensoroSlotArray(SensoroSlot[] sensoroSlots) {
            this.sensoroSlots = sensoroSlots;
            return this;
        }

        public Builder setSensorBroadcastEnabled(int enabled) {
            this.sensorBroadcastEnabled = enabled;
            return this;
        }

        public Builder setClassBEnabled(int enabled) {
            this.classBEnabled = enabled;
            return this;
        }

        public Builder setClassBDataRate(int dataRate) {
            this.classBDataRate = dataRate;
            return this;
        }

        public Builder setClassBPeriodicity(int periodicity) {
            this.classBPeriodicity = periodicity;
            return this;
        }

        public Builder setActivation(int activation) {
            this.activation = activation;
            return this;
        }

        public Builder setSensorConfiguration(SensoroSensorConfiguration sensorConfiguration) {
            this.sensorConfiguration = sensorConfiguration;
            return this;
        }

        public Builder setUploadIntervalData(Integer uploadInterval) {
            this.uploadIntervalData = uploadInterval;
            return this;
        }

        public Builder setHasUploadInterval(boolean hasUploadInterval) {
            this.hasUploadInterval = hasUploadInterval;
            return this;
        }

        public Builder setHasConfirm(boolean hasConfirm) {
            this.hasConfirm = hasConfirm;
            return this;
        }

        public Builder setHasConfirmData(Integer confirmData) {
            this.confirmData = confirmData;
            return this;
        }

        public Builder setHasIBeacon(boolean hasIBeacon) {
            this.hasIBeacon = hasIBeacon;
            return this;
        }

        public Builder setHasLoraParam(boolean hasLoraParam) {
            this.hasLoraParam = hasLoraParam;
            return this;
        }

        public Builder setHasDevEui(boolean hasDevEui) {
            this.hasDevEui = hasDevEui;
            return this;
        }

        public Builder setHasAppEui(boolean hasAppEui) {
            this.hasAppEui = hasAppEui;
            return this;
        }

        public Builder setHasAppKey(boolean hasAppKey) {
            this.hasAppKey = hasAppKey;
            return this;
        }

        public Builder setHasAppSkey(boolean hasAppSkey) {
            this.hasAppSkey = hasAppSkey;
            return this;
        }

        public Builder setHasNwkSkey(boolean hasNwkSkey) {
            this.hasNwkSkey = hasNwkSkey;
            return this;
        }

        public Builder setHasDevAddr(boolean hasDevAddr) {
            this.hasDevAddr = hasDevAddr;
            return this;
        }

        public Builder setHasSglStatus(boolean hasSglStatus) {
            this.hasSglStatus = hasSglStatus;
            return this;
        }

        public Builder setHasSglDataRate(boolean hasSglDataRate) {
            this.hasSglDataRate = hasSglDataRate;
            return this;
        }

        public Builder setHasSglFrequency(boolean hasSglFrequency) {
            this.hasSglFrequency = hasSglFrequency;
            return this;
        }

        public Builder setHasSensorBroadcast(boolean hasSensorBroadcast) {
            this.hasSensorBroadcast = hasSensorBroadcast;
            return this;
        }

        public Builder setHasCustom(boolean hasCustom) {
            this.hasCustom = hasCustom;
            return this;
        }

        public Builder setHasEddystone(boolean hasEddystone) {
            this.hasEddystone = hasEddystone;
            return this;
        }

        public Builder setHasAppParam(boolean hasAppParam) {
            this.hasAppParam = hasAppParam;
            return this;
        }

        public Builder setHasBleParam(boolean hasBleParam) {
            this.hasBleParam = hasBleParam;
            return this;
        }

        public Builder setHasDelay(boolean hasDelay) {
            this.hasDelay = hasDelay;
            return this;
        }

        public Builder setHasActivation(boolean hasActivation) {
            this.hasActivation = hasActivation;
            return this;
        }

        public Builder setConfirmData(Integer confirmData) {
            this.confirmData = confirmData;
            return this;
        }

        public Builder setFlame(Integer flame) {
            this.flame = flame;
            return this;
        }

        public Builder setHasFlame(boolean hasFlame) {
            this.hasFlame = hasFlame;
            return this;
        }

        public Builder setDelay(Integer delay) {
            this.delay = delay;
            return this;
        }

        public Builder setChannelList(List<Integer> channelList) {
            this.channelList = channelList;
            return this;
        }

        public Builder setChannels(List<SensoroChannel> channels) {
            this.channels = channels;
            return this;
        }

        public Builder setLoraEirp(Integer loraEirp) {
            this.loraEirp = loraEirp;
            return this;
        }

        public Builder setSglStatus(Integer sglStatus) {
            this.sglStatus = sglStatus;
            return this;
        }

        public Builder setSglFrequency(Integer sglFrequency) {
            this.sglFrequency = sglFrequency;
            return this;
        }

        public Builder setSglDatarate(Integer sglDatarate) {
            this.sglDatarate = sglDatarate;
            return this;
        }

        public SensoroDeviceConfiguration build() {
            return new SensoroDeviceConfiguration(this);
        }
    }
}
