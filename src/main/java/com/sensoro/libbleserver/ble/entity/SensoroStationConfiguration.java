package com.sensoro.libbleserver.ble.entity;

/**
 * Created by sensoro on 16/8/16.
 */

public class SensoroStationConfiguration {

    public String sn;
    public String hardwareModelName;// hardware version.
    public String firmwareVersion;// firmware version.
    public int workStatus;
    public int netStatus;
    public String ip;
    public String gateway;
    public String mask;
    public String pdns;
    public String adns;
    public String sid;
    public String pwd;
    public String encrpt;
    public int accessMode;
    public int allocationMode;
    public String netid;
    public String cloudaddress;
    public String cloudport;
    public String key;

    public int getSgl_dr() {
        return sgl_dr;
    }

    public void setSgl_dr(int sgl_dr) {
        this.sgl_dr = sgl_dr;
    }

    public int getSgl_freq() {
        return sgl_freq;
    }

    public void setSgl_freq(int sgl_freq) {
        this.sgl_freq = sgl_freq;
    }

    public int sgl_dr;
    public int sgl_freq;

    private SensoroStationConfiguration(Builder builder) {
        this.sn = builder.sn;
        this.accessMode = builder.accessMode;
        this.allocationMode = builder.allocationMode;
        this.ip = builder.ip;
        this.adns = builder.adns;
        this.mask = builder.mask;
        this.firmwareVersion = builder.firmwareVersion;
        this.encrpt = builder.encrpt;
        this.hardwareModelName = builder.hardwareModelName;
        this.workStatus = builder.workStatus;
        this.sid = builder.sid;
        this.pwd = builder.pwd;
        this.pdns = builder.pdns;
        this.gateway = builder.gateway;
        this.netid = builder.netid;
        this.cloudaddress = builder.cloudaddress;
        this.cloudport = builder.cloudport;
        this.key = builder.key;
        this.sgl_freq = builder.sgl_freq;
        this.sgl_dr = builder.sgl_dr;
    }

    public static class Builder {
        private String sn;
        private String hardwareModelName;// hardware version.
        private String firmwareVersion;// firmware version.
        private int workStatus;
        private int netStatus;
        private String ip;
        private String gateway;
        private String mask;
        private String pdns;
        private String adns;
        private String sid;
        private String pwd;
        private String encrpt;
        private int accessMode;
        private int allocationMode;
        private String netid;
        private String cloudaddress;
        private String cloudport;
        private String key;

        public int getSgl_dr() {
            return sgl_dr;
        }

        public void setSgl_dr(int sgl_dr) {
            this.sgl_dr = sgl_dr;
        }

        public int getSgl_freq() {
            return sgl_freq;
        }

        public void setSgl_freq(int sgl_freq) {
            this.sgl_freq = sgl_freq;
        }

        private int sgl_dr;
        private int sgl_freq;

        public Builder setSn(String sn) {
            this.sn = sn;
            return this;
        }


        public Builder setHardwareModelName(String name) {
            this.hardwareModelName = name;
            return this;
        }

        public Builder setFirmwareVersion(String version) {
            this.firmwareVersion = version;
            return this;
        }

        public Builder setAccessMode(int accessMode) {
            this.accessMode = accessMode;
            return this;
        }

        public Builder setAllocationMode(int assignment) {
            this.allocationMode = assignment;
            return this;
        }

        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setMask(String mask) {
            this.mask = mask;
            return this;
        }

        public Builder setPdns(String pdns) {
            this.pdns = pdns;
            return this;
        }

        public Builder setAdns(String adns) {
            this.adns = adns;
            return this;
        }

        public Builder setSid(String sid) {
            this.sid = sid;
            return this;
        }

        public Builder setNetId(String netid) {
            this.netid = netid;
            return this;
        }

        public Builder setCloudAddress(String address) {
            this.cloudaddress = address;
            return this;
        }

        public Builder setCloudPort(String port) {
            this.cloudport = port;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setPassword(String pwd) {
            this.pwd = pwd;
            return this;
        }

        public Builder setEncrpt(String encrpt) {
            this.encrpt = encrpt;
            return this;
        }

        public Builder setRouter(String router) {
            this.gateway = router;
            return this;
        }

        public SensoroStationConfiguration build() {
            return new SensoroStationConfiguration(this);
        }
    }
}
