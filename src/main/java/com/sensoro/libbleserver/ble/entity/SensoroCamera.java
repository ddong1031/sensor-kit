package com.sensoro.libbleserver.ble.entity;/** * @author : bin.tian * date   : 2020/5/9 */import android.os.Parcel;import android.os.Parcelable;/** * 设备SN （定长8字节） * modelname        = LINS （固定是LINS） * modelid               = S2121-SAP-M6 （定长12字节）---重定向为2个字节，小端 * hardwareversion= S2121-SAP-M6-V200（只需要广播后边的200就可以，定长2字节，整形数，小端） * Wi-Fi状态 （实时更新） * 未连接             ----0 * 连接上AP        ----1 * 连接上外网     ----2 * 绑定状态 （实时更新） * 未绑定    ---0 * 已绑定    ---1 */public class SensoroCamera extends BLEDevice implements Parcelable, Cloneable {    public String modelName;    public int modelId;    public String hardwareVersion;    /**     * 未连接 0     * 连接上AP  1     * 连接上外网  2     */    public int wifiStatus;    /**     * 未绑定    0     * 已绑定    1     */    public int bindStatus;    public String getModelName() {        return modelName;    }    public void setModelName(String modelName) {        this.modelName = modelName;    }    public int getModelId() {        return modelId;    }    public void setModelId(int modelId) {        this.modelId = modelId;    }    @Override    public String getHardwareVersion() {        return hardwareVersion;    }    @Override    public void setHardwareVersion(String hardwareVersion) {        this.hardwareVersion = hardwareVersion;    }    public int getWifiStatus() {        return wifiStatus;    }    public void setWifiStatus(int wifiStatus) {        this.wifiStatus = wifiStatus;    }    public int getBindStatus() {        return bindStatus;    }    public void setBindStatus(int bindStatus) {        this.bindStatus = bindStatus;    }    @Override    public SensoroCamera clone() {        SensoroCamera camera = null;        try {            camera = (SensoroCamera) super.clone();        } catch (CloneNotSupportedException e) {            e.printStackTrace();        }        return camera;    }    @Override    public int describeContents() {        return 0;    }    @Override    public void writeToParcel(Parcel dest, int flags) {        super.writeToParcel(dest, flags);        dest.writeString(this.modelName);        dest.writeInt(this.modelId);        dest.writeString(this.hardwareVersion);        dest.writeInt(this.wifiStatus);        dest.writeInt(this.bindStatus);    }    public SensoroCamera() {    }    protected SensoroCamera(Parcel in) {        super(in);        this.modelName = in.readString();        this.modelId = in.readInt();        this.hardwareVersion = in.readString();        this.wifiStatus = in.readInt();        this.bindStatus = in.readInt();    }    public static final Creator<SensoroCamera> CREATOR = new Creator<SensoroCamera>() {        @Override        public SensoroCamera createFromParcel(Parcel source) {            return new SensoroCamera(source);        }        @Override        public SensoroCamera[] newArray(int size) {            return new SensoroCamera[size];        }    };}