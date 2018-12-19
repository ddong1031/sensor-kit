package com.sensoro.libbleserver.ble;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fangping on 2016/7/25.
 */

public class SensoroStation extends BLEDevice implements Parcelable, Cloneable {

    String sn;
    String hardwareModelName;// hardware version.
    String firmwareVersion;// firmware version.
    int workStatus;
    int netStatus;
    int wifiStatus;
    int ethStatus;
    int cellularStatus;
    String ip;
    String gateway;
    String mask;
    String pdns;
    String adns;
    String sid;
    String pwd;
    String encrpt;

    public int getSgl_dr() {
        return sgl_dr;
    }

    public void setSgl_dr(int sgl_dr) {
        this.sgl_dr = sgl_dr;
    }

    //单通道基站DR
    int sgl_dr;

    public int getSgl_freq() {
        return sgl_freq;
    }

    public void setSgl_freq(int sgl_freq) {
        this.sgl_freq = sgl_freq;
    }

    int sgl_freq;

    int accessMode;
    int allocationMode;
    int rssi;
    String netid;
    String cloudaddress;
    String cloudport;
    String key;
    public long lastFoundTime;

    public SensoroStation() {
        lastFoundTime = System.currentTimeMillis();
    }

    public SensoroStation(Parcel in) {
        super(in);
        sn = in.readString();
        hardwareModelName = in.readString();
        firmwareVersion = in.readString();
        workStatus = in.readInt();
        netStatus = in.readInt();
        wifiStatus = in.readInt();
        ethStatus = in.readInt();
        cellularStatus = in.readInt();
        ip = in.readString();
        gateway = in.readString();
        mask = in.readString();
        pdns = in.readString();
        adns = in.readString();
        sid = in.readString();
        pwd = in.readString();
        encrpt = in.readString();
        accessMode = in.readInt();
        allocationMode = in.readInt();
        netid = in.readString();
        cloudaddress = in.readString();
        cloudport = in.readString();
        key = in.readString();
        lastFoundTime = in.readLong();
        rssi = in.readInt();
        sgl_dr = in.readInt();
        sgl_freq = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(sn);
        parcel.writeString(hardwareModelName);
        parcel.writeString(firmwareVersion);
        parcel.writeInt(workStatus);
        parcel.writeInt(netStatus);
        parcel.writeInt(wifiStatus);
        parcel.writeInt(ethStatus);
        parcel.writeInt(cellularStatus);
        parcel.writeString(ip);
        parcel.writeString(gateway);
        parcel.writeString(mask);
        parcel.writeString(pdns);
        parcel.writeString(adns);
        parcel.writeString(sid);
        parcel.writeString(pwd);
        parcel.writeString(encrpt);
        parcel.writeInt(accessMode);
        parcel.writeInt(allocationMode);
        parcel.writeString(netid);
        parcel.writeString(cloudaddress);
        parcel.writeString(cloudport);
        parcel.writeString(key);
        parcel.writeLong(lastFoundTime);
        parcel.writeInt(rssi);
        parcel.writeInt(sgl_dr);
        parcel.writeInt(sgl_freq);
    }

    public static final Creator<SensoroStation> CREATOR = new Creator<SensoroStation>() {

        @Override
        public SensoroStation createFromParcel(Parcel parcel) {
            return new SensoroStation(parcel);
        }

        @Override
        public SensoroStation[] newArray(int size) {
            return new SensoroStation[size];
        }
    };

    @Override
    public SensoroStation clone() throws CloneNotSupportedException {
        SensoroStation newStation = null;
        try {
            newStation = (SensoroStation) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return newStation;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getHardwareVersion() {
        return hardwareModelName;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareModelName = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public int getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(int workStatus) {
        this.workStatus = workStatus;
    }

    public int getNetStatus() {
        return netStatus;
    }

    public void setNetStatus(int netStatus) {
        this.netStatus = netStatus;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getPdns() {
        return pdns;
    }

    public void setPdns(String pdns) {
        this.pdns = pdns;
    }

    public String getAdns() {
        return adns;
    }

    public void setAdns(String adns) {
        this.adns = adns;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEncrpt() {
        return encrpt;
    }

    public void setEncrpt(String encrpt) {
        this.encrpt = encrpt;
    }

    public int getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(int accessMode) {
        this.accessMode = accessMode;
    }

    public int getAllocationMode() {
        return allocationMode;
    }

    public void setAllocationMode(int allocationMode) {
        this.allocationMode = allocationMode;
    }

    public String getNetid() {
        return netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getCloudaddress() {
        return cloudaddress;
    }

    public void setCloudaddress(String cloudaddress) {
        this.cloudaddress = cloudaddress;
    }

    public String getCloudport() {
        return cloudport;
    }

    public void setCloudport(String cloudport) {
        this.cloudport = cloudport;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getWifiStatus() {
        return wifiStatus;
    }

    public void setWifiStatus(int wifiStatus) {
        this.wifiStatus = wifiStatus;
    }

    public int getEthStatus() {
        return ethStatus;
    }

    public void setEthStatus(int ethStatus) {
        this.ethStatus = ethStatus;
    }

    public int getCellularStatus() {
        return cellularStatus;
    }

    public void setCellularStatus(int cellularStatus) {
        this.cellularStatus = cellularStatus;
    }
}
