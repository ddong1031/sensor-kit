package com.sensoro.libbleserver.ble;

import android.os.ParcelUuid;

import com.sensoro.libbleserver.ble.scanner.BLEFilter;
import com.sensoro.libbleserver.ble.scanner.ScanBLEResult;
import com.sensoro.libbleserver.ble.scanner.SensoroUUID;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fangping on 2016/7/13.
 */

public class BLEDeviceFactory {
    private static final String TAG = "BLEDeviceFactory";
    private ScanBLEResult scanBLEResult;
    private static HashMap<String, String> snMap = new HashMap<>();
    protected static HashMap<String, Integer> accelerometerCountMap = new HashMap<>();
    protected static HashMap<String, byte[]> customizeMap = new HashMap<>();
    private static HashMap<String, Float> temperatureMap = new HashMap<>();
    private static HashMap<String, Float> humidityMap = new HashMap<>();
    private static HashMap<String, Float> lightMap = new HashMap<>();
    protected static HashMap<String, Integer> leakMap = new HashMap<>();
    protected static HashMap<String, Float> coMap = new HashMap<>();
    protected static HashMap<String, Float> co2Map = new HashMap<>();
    protected static HashMap<String, Float> no2Map = new HashMap<>();
    protected static HashMap<String, Float> methaneMap = new HashMap<>();
    protected static HashMap<String, Float> lpgMap = new HashMap<>();
    protected static HashMap<String, Float> pm1Map = new HashMap<>();
    protected static HashMap<String, Float> pm25Map = new HashMap<>();
    protected static HashMap<String, Float> pm10Map = new HashMap<>();
    protected static HashMap<String, Float> coverstatusMap = new HashMap<>();
    protected static HashMap<String, Float> levelMap = new HashMap<>();
    protected static HashMap<String, Float> pitchAngleMap = new HashMap<>();
    protected static HashMap<String, Float> rollAngleMap = new HashMap<>();
    protected static HashMap<String, Float> yawAngleMap = new HashMap<>();
    protected static HashMap<String, Integer> flameMap = new HashMap<>();
    protected static HashMap<String, Float> gasMap = new HashMap<>();
    protected static HashMap<String, Integer> smokeMap = new HashMap<>();
    protected static HashMap<String, Float> pressureMap = new HashMap<>();


    public BLEDeviceFactory(ScanBLEResult scanBLEResult) {
        this.scanBLEResult = scanBLEResult;
    }

    public SensoroDevice createDevice() {
        if (scanBLEResult == null) {
            return null;
        }

        Map<ParcelUuid, byte[]> serviceData = scanBLEResult.getScanRecord().getServiceData();
        if (serviceData == null) {
            return null;
        }
        ParcelUuid parcelUuid = BLEFilter.createServiceDataUUID(BLEFilter.DEVICE_SERVICE_DATA_UUID);
        byte device_data[] = scanBLEResult.getScanRecord().getServiceData(parcelUuid);

        if (device_data != null) {
            SensoroDevice bleDevice = new SensoroDevice();
            byte[] sn = new byte[8];
            System.arraycopy(device_data, 0, sn, 0, sn.length);
            bleDevice.setSn(SensoroUUID.parseSN(sn));

            byte[] hardware = new byte[2];
            System.arraycopy(device_data, 8, hardware, 0, hardware.length);
            int hardwareCode = (int) hardware[0] & 0xff;
            String hardwareVersion = Integer.toHexString(hardwareCode).toUpperCase();
            bleDevice.setHardwareVersion(hardwareVersion);

            int firmwareCode = (int) hardware[1] & 0xff;
            String firmwareVersion = Integer.toHexString(firmwareCode / 16).toUpperCase() + "." + Integer.toHexString
                    (firmwareCode % 16).toUpperCase();
            bleDevice.setFirmwareVersion(firmwareVersion);

            int batteryLevel = ((int) device_data[10] & 0xff);
            bleDevice.setBatteryLevel(batteryLevel);

            int power = ((int) device_data[11] & 0xff);
            bleDevice.setPower(power);

            byte[] sf = new byte[4];
            System.arraycopy(device_data, 12, sf, 0, sf.length);
            bleDevice.setSf(SensoroUUID.byteArrayToFloat(sf, 0));

            bleDevice.setMacAddress(scanBLEResult.getDevice().getAddress());
            int last_index = device_data.length - 1;
            if (device_data[last_index] == 0x01) { // dfu
                bleDevice.setDfu(true);
            } else {
                bleDevice.setDfu(false);
            }
            bleDevice.setRssi(scanBLEResult.getRssi());
            bleDevice.setType(BLEDevice.TYPE_DEVICE);
            return bleDevice;
        }
        return null;

    }

    @Deprecated
    public SensoroSensor createSensor() {
        if (scanBLEResult == null) {
            return null;
        }
        try {
            E3214 e3214 = E3214.createE3214(scanBLEResult);
            if (e3214 != null) {
                SensoroSensor sensoroSensor = new SensoroSensor();
                sensoroSensor.setHardwareVersion(e3214.hardwareVersion);
                sensoroSensor.setFirmwareVersion(e3214.firmwareVersion);
                sensoroSensor.setBatteryLevel(e3214.batteryLevel == null ? 0 : e3214.batteryLevel);
                String address = scanBLEResult.getDevice().getAddress();
                sensoroSensor.setMacAddress(address);
                sensoroSensor.setRssi(scanBLEResult.getRssi());
                if (e3214.sn != null) {
                    snMap.put(address, e3214.sn);
                }
                sensoroSensor.setSn(snMap.get(address));

                sensoroSensor.humidity = new SensoroData();
                if (e3214.humidity != null) {
                    humidityMap.put(address, (float) e3214.humidity);
                    sensoroSensor.hasHumidity = true;
                    sensoroSensor.humidity.has_data = true;
                }
                sensoroSensor.humidity.data_float = humidityMap.get(address);
//                sensoroSensor.setHumidity(humidityMap.get(address));

                sensoroSensor.temperature = new SensoroData();
                if (e3214.temperature != null) {
                    temperatureMap.put(address, e3214.temperature);
                    sensoroSensor.hasTemperature = true;
                    sensoroSensor.temperature.has_data = true;
                }
                sensoroSensor.temperature.data_float = temperatureMap.get(address);

                sensoroSensor.light = new SensoroData();
                if (e3214.light != null) {
                    lightMap.put(address, e3214.light);
                    sensoroSensor.hasLight = true;
                    sensoroSensor.light.has_data = true;
                }
                sensoroSensor.light.data_float = lightMap.get(address);

                sensoroSensor.accelerometerCount = new SensoroData();
                if (e3214.accelerometerCount != null) {
                    accelerometerCountMap.put(address, e3214.accelerometerCount);
                    sensoroSensor.hasAccelerometerCount = true;
                    sensoroSensor.accelerometerCount.has_data = true;
                }
                sensoroSensor.accelerometerCount.data_int = accelerometerCountMap.get(address);

                if (e3214.customize != null) {
                    customizeMap.put(address, e3214.customize);
                }
                sensoroSensor.customize = customizeMap.get(sensoroSensor.macAddress);


                sensoroSensor.leak = new SensoroData();
                if (e3214.leak != null) {
                    leakMap.put(sensoroSensor.macAddress, e3214.leak);
                    sensoroSensor.hasLeak = true;
                    sensoroSensor.leak.has_data = true;
                }
                sensoroSensor.leak.data_int = leakMap.get(sensoroSensor.macAddress);
//TODO
                sensoroSensor.co = new SensoroData();
                if (e3214.co != null) {
                    coMap.put(sensoroSensor.macAddress, e3214.co);
                    sensoroSensor.hasCo = true;
                    sensoroSensor.co.has_data = true;
                }
                sensoroSensor.co.data_float = coMap.get(sensoroSensor.macAddress);

                sensoroSensor.co2 = new SensoroData();
                if (e3214.co2 != null) {
                    co2Map.put(sensoroSensor.macAddress, e3214.co2);
                    sensoroSensor.hasCo2 = true;
                    sensoroSensor.co2.has_data = true;
                }
                sensoroSensor.co2.data_float = co2Map.get(sensoroSensor.macAddress);

                sensoroSensor.no2 = new SensoroData();
                if (e3214.no2 != null) {
                    no2Map.put(sensoroSensor.macAddress, e3214.no2);
                    sensoroSensor.hasNo2 = true;
                    sensoroSensor.no2.has_data = true;
                }
                sensoroSensor.no2.data_float = no2Map.get(sensoroSensor.macAddress);

                sensoroSensor.methane = new SensoroData();
                if (e3214.methane != null) {
                    methaneMap.put(sensoroSensor.macAddress, e3214.methane);
                    sensoroSensor.hasMethane = true;
                    sensoroSensor.methane.has_data = true;
                }
                sensoroSensor.methane.data_float = methaneMap.get(sensoroSensor.macAddress);

                sensoroSensor.lpg = new SensoroData();
                if (e3214.lpg != null) {
                    lpgMap.put(sensoroSensor.macAddress, e3214.lpg);
                    sensoroSensor.hasLpg = true;
                    sensoroSensor.lpg.has_data = true;
                }
                sensoroSensor.lpg.data_float = lpgMap.get(sensoroSensor.macAddress);

                sensoroSensor.pm1 = new SensoroData();
                if (e3214.pm1 != null) {
                    pm1Map.put(sensoroSensor.macAddress, e3214.pm1);
                    sensoroSensor.hasPm1 = true;
                    sensoroSensor.pm1.has_data = true;
                }
                sensoroSensor.pm1.data_float = pm1Map.get(sensoroSensor.macAddress);

                sensoroSensor.pm25 = new SensoroData();
                if (e3214.pm25 != null) {
                    pm25Map.put(sensoroSensor.macAddress, e3214.pm25);
                    sensoroSensor.hasPm25 = true;
                    sensoroSensor.pm25.has_data = true;
                }
                sensoroSensor.pm25.data_float = pm25Map.get(sensoroSensor.macAddress);

                sensoroSensor.pm10 = new SensoroData();
                if (e3214.pm10 != null) {
                    pm10Map.put(sensoroSensor.macAddress, e3214.pm10);
                    sensoroSensor.hasPm10 = true;
                    sensoroSensor.pm10.has_data = true;
                }
                sensoroSensor.pm10.data_float = pm10Map.get(sensoroSensor.macAddress);

                sensoroSensor.coverStatus = new SensoroData();
                if (e3214.coverstatus != null) {
                    coverstatusMap.put(sensoroSensor.macAddress, (float) e3214.coverstatus);
                    sensoroSensor.hasCover = true;
                    sensoroSensor.coverStatus.has_data = true;
                }
                sensoroSensor.coverStatus.data_float = coverstatusMap.get(sensoroSensor.macAddress);

                sensoroSensor.level = new SensoroData();
                if (e3214.level != null) {
                    levelMap.put(sensoroSensor.macAddress, e3214.level);
                    sensoroSensor.hasLevel = true;
                    sensoroSensor.level.has_data = true;
                }
                sensoroSensor.level.data_float = levelMap.get(sensoroSensor.macAddress);

                sensoroSensor.pitch = new SensoroData();
                if (e3214.pitchAngle != null) {
                    pitchAngleMap.put(sensoroSensor.macAddress, e3214.pitchAngle);
                    sensoroSensor.hasPitch = true;
                    sensoroSensor.pitch.has_data = true;
                }
                sensoroSensor.pitch.data_float = pitchAngleMap.get(sensoroSensor.macAddress);

                sensoroSensor.roll = new SensoroData();
                if (e3214.rollAngle != null) {
                    rollAngleMap.put(sensoroSensor.macAddress, e3214.rollAngle);
                    sensoroSensor.hasRoll = true;
                    sensoroSensor.roll.has_data = true;
                }
                sensoroSensor.roll.data_float = rollAngleMap.get(sensoroSensor.macAddress);

                sensoroSensor.yaw = new SensoroData();
                if (e3214.yawAngle != null) {
                    yawAngleMap.put(sensoroSensor.macAddress, e3214.yawAngle);
                    sensoroSensor.hasYaw = true;
                    sensoroSensor.yaw.has_data = true;
                }
                sensoroSensor.yaw.data_float = yawAngleMap.get(sensoroSensor.macAddress);

                sensoroSensor.flame = new SensoroData();
                if (e3214.flame != null) {
                    flameMap.put(sensoroSensor.macAddress, e3214.flame);
                    sensoroSensor.hasFlame = true;
                    sensoroSensor.flame.has_data = true;
                }
                sensoroSensor.flame.data_int = flameMap.get(sensoroSensor.macAddress);

                sensoroSensor.gas = new SensoroData();
                if (e3214.artificial_gas != null) {
                    gasMap.put(sensoroSensor.macAddress, e3214.artificial_gas);
                    sensoroSensor.hasGas = true;
                    sensoroSensor.gas.has_data = true;
                }
                sensoroSensor.gas.data_float = gasMap.get(sensoroSensor.macAddress);

                sensoroSensor.smoke = new SensoroData();
                if (e3214.smoke != null) {
                    smokeMap.put(sensoroSensor.macAddress, e3214.smoke);
                    sensoroSensor.hasSmoke = true;
                    sensoroSensor.smoke.has_data = true;
                }
                sensoroSensor.smoke.data_int = smokeMap.get(sensoroSensor.macAddress);

                sensoroSensor.waterPressure = new SensoroData();
                if (e3214.pressure != null) {
                    pressureMap.put(sensoroSensor.macAddress, e3214.pressure);
                    sensoroSensor.hasWaterPressure = true;
                    sensoroSensor.waterPressure.has_data = true;
                }
                sensoroSensor.waterPressure.data_float = pressureMap.get(sensoroSensor.macAddress);

                sensoroSensor.setType(BLEDevice.TYPE_SENSOR);
                if (sensoroSensor.getSn() == null) {
                    return null;
                }

                return sensoroSensor;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Deprecated
    public SensoroStation createStation() {
        if (scanBLEResult == null) {
            return null;
        }
        Map<ParcelUuid, byte[]> serviceData = scanBLEResult.getScanRecord().getServiceData();
        if (serviceData == null) {
            return null;
        }
        ParcelUuid parcelUuid = BLEFilter.createServiceDataUUID(BLEFilter.STATION_SERVICE_DATA_UUID);
        byte station_data[] = scanBLEResult.getScanRecord().getServiceData(parcelUuid);
        if (station_data != null) {
            SensoroStation bleDevice = new SensoroStation();
            byte[] sn = new byte[8];
            System.arraycopy(station_data, 0, sn, 0, sn.length);
            bleDevice.setSn(SensoroUUID.parseSN(sn));

            byte[] hardware = new byte[2];
            System.arraycopy(station_data, 8, hardware, 0, hardware.length);
            int hardwareCode = (int) hardware[0] & 0xff;
            String hardwareVersion = Integer.toHexString(hardwareCode).toUpperCase();
            bleDevice.setHardwareVersion(hardwareVersion);

            int firmwareCode = (int) hardware[1] & 0xff;
            String firmwareVersion = Integer.toHexString(firmwareCode / 16).toUpperCase() + "." + Integer.toHexString
                    (firmwareCode % 16).toUpperCase();
            bleDevice.setFirmwareVersion(firmwareVersion);

            int workStatus = ((int) station_data[10] & 0xff);
            bleDevice.setWorkStatus(workStatus);
            //03, 0c, 30
//            int netStatus = ((int) station_data[11] & 0xff);
            int wifiStatus = (int) station_data[11] & 0x03;
            int ethStatus = ((int) station_data[11] & 0x0c) >> 2;
            int celluarStatus = ((int) station_data[11] & 0x30) >> 4;
//            bleDevice.setNetStatus(netStatus);
            bleDevice.setWifiStatus(wifiStatus);
            bleDevice.setEthStatus(ethStatus);
            bleDevice.setCellularStatus(celluarStatus);
            bleDevice.setRssi(scanBLEResult.getRssi());
            bleDevice.setMacAddress(scanBLEResult.getDevice().getAddress());
            bleDevice.setType(BLEDevice.TYPE_STATION);
            return bleDevice;
        }
        return null;
    }

    public BLEDevice create() {
        if (scanBLEResult == null) {
            return null;
        }
        Map<ParcelUuid, byte[]> serviceData = scanBLEResult.getScanRecord().getServiceData();
        if (serviceData == null) {
            return null;
        }
        ParcelUuid stationParcelUuid = BLEFilter.createServiceDataUUID(BLEFilter.STATION_SERVICE_DATA_UUID);
        ParcelUuid deviceParcelUuid = BLEFilter.createServiceDataUUID(BLEFilter.DEVICE_SERVICE_DATA_UUID);
        ParcelUuid sensorParcelUuid = BLEFilter.createServiceDataUUID(BLEFilter.SENSOR_SERVICE_UUID_E3412);
        byte sensor_data[] = scanBLEResult.getScanRecord().getServiceData(sensorParcelUuid);
        byte device_data[] = scanBLEResult.getScanRecord().getServiceData(deviceParcelUuid);
        byte station_data[] = scanBLEResult.getScanRecord().getServiceData(stationParcelUuid);
        SensoroSensor sensoroSensor = null;
        if (sensor_data != null) {
            E3214 e3214 = E3214.parseE3214(sensor_data);
            if (e3214 != null) {
                sensoroSensor = new SensoroSensor();
                sensoroSensor.setHardwareVersion(e3214.hardwareVersion);
                sensoroSensor.setFirmwareVersion(e3214.firmwareVersion);
                sensoroSensor.setBatteryLevel(e3214.batteryLevel == null ? 0 : e3214.batteryLevel);
                String address = scanBLEResult.getDevice().getAddress();
                sensoroSensor.setMacAddress(address);
                sensoroSensor.setRssi(scanBLEResult.getRssi());
                if (e3214.sn != null) {
                    snMap.put(address, e3214.sn);

                }
                sensoroSensor.setSn(snMap.get(address));
                sensoroSensor.humidity = new SensoroData();
                if (e3214.humidity != null) {
                    humidityMap.put(address, (float) e3214.humidity);
                    sensoroSensor.hasHumidity = true;
                    sensoroSensor.humidity.has_data = true;
                }
                sensoroSensor.humidity.data_float = humidityMap.get(address);
                sensoroSensor.temperature = new SensoroData();
                if (e3214.temperature != null) {
                    temperatureMap.put(address, e3214.temperature);
                    sensoroSensor.hasTemperature = true;
                    sensoroSensor.temperature.has_data = true;
                }
                sensoroSensor.temperature.data_float = temperatureMap.get(address);

                sensoroSensor.light = new SensoroData();
                if (e3214.light != null) {
                    lightMap.put(address, e3214.light);
                    sensoroSensor.hasLight = true;
                    sensoroSensor.light.has_data = true;
                }
                sensoroSensor.light.data_float = lightMap.get(address);

                sensoroSensor.accelerometerCount = new SensoroData();
                if (e3214.accelerometerCount != null) {
                    accelerometerCountMap.put(address, e3214.accelerometerCount);
                    sensoroSensor.hasAccelerometerCount = true;
                    sensoroSensor.accelerometerCount.has_data = true;
                }
                sensoroSensor.accelerometerCount.data_int = accelerometerCountMap.get(address);

                if (e3214.customize != null) {
                    customizeMap.put(address, e3214.customize);
                }
                sensoroSensor.customize = customizeMap.get(sensoroSensor.macAddress);


                sensoroSensor.leak = new SensoroData();
                if (e3214.leak != null) {
                    leakMap.put(sensoroSensor.macAddress, e3214.leak);
                    sensoroSensor.hasLeak = true;
                    sensoroSensor.leak.has_data = true;
                }
                sensoroSensor.leak.data_int = leakMap.get(sensoroSensor.macAddress);

                sensoroSensor.co = new SensoroData();
                if (e3214.co != null) {
                    coMap.put(sensoroSensor.macAddress, e3214.co);
                    sensoroSensor.hasCo = true;
                    sensoroSensor.co.has_data = true;
                }
                sensoroSensor.co.data_float = coMap.get(sensoroSensor.macAddress);

                sensoroSensor.co2 = new SensoroData();
                if (e3214.co2 != null) {
                    co2Map.put(sensoroSensor.macAddress, e3214.co2);
                    sensoroSensor.hasCo2 = true;
                    sensoroSensor.co2.has_data = true;
                }
                sensoroSensor.co2.data_float = co2Map.get(sensoroSensor.macAddress);

                sensoroSensor.no2 = new SensoroData();
                if (e3214.no2 != null) {
                    no2Map.put(sensoroSensor.macAddress, e3214.no2);
                    sensoroSensor.hasNo2 = true;
                    sensoroSensor.no2.has_data = true;
                }
                sensoroSensor.no2.data_float = no2Map.get(sensoroSensor.macAddress);

                sensoroSensor.methane = new SensoroData();
                if (e3214.methane != null) {
                    methaneMap.put(sensoroSensor.macAddress, e3214.methane);
                    sensoroSensor.hasMethane = true;
                    sensoroSensor.methane.has_data = true;
                }
                sensoroSensor.methane.data_float = methaneMap.get(sensoroSensor.macAddress);

                sensoroSensor.lpg = new SensoroData();
                if (e3214.lpg != null) {
                    lpgMap.put(sensoroSensor.macAddress, e3214.lpg);
                    sensoroSensor.hasLpg = true;
                    sensoroSensor.lpg.has_data = true;
                }
                sensoroSensor.lpg.data_float = lpgMap.get(sensoroSensor.macAddress);

                sensoroSensor.pm1 = new SensoroData();
                if (e3214.pm1 != null) {
                    pm1Map.put(sensoroSensor.macAddress, e3214.pm1);
                    sensoroSensor.hasPm1 = true;
                    sensoroSensor.pm1.has_data = true;
                }
                sensoroSensor.pm1.data_float = pm1Map.get(sensoroSensor.macAddress);

                sensoroSensor.pm25 = new SensoroData();
                if (e3214.pm25 != null) {
                    pm25Map.put(sensoroSensor.macAddress, e3214.pm25);
                    sensoroSensor.hasPm25 = true;
                    sensoroSensor.pm25.has_data = true;
                }
                sensoroSensor.pm25.data_float = pm25Map.get(sensoroSensor.macAddress);

                sensoroSensor.pm10 = new SensoroData();
                if (e3214.pm10 != null) {
                    pm10Map.put(sensoroSensor.macAddress, e3214.pm10);
                    sensoroSensor.hasPm10 = true;
                    sensoroSensor.pm10.has_data = true;
                }
                sensoroSensor.pm10.data_float = pm10Map.get(sensoroSensor.macAddress);

                sensoroSensor.coverStatus = new SensoroData();
                if (e3214.coverstatus != null) {
                    coverstatusMap.put(sensoroSensor.macAddress, (float) e3214.coverstatus);
                    sensoroSensor.hasCover = true;
                    sensoroSensor.coverStatus.has_data = true;
                }
                sensoroSensor.coverStatus.data_float = coverstatusMap.get(sensoroSensor.macAddress);

                sensoroSensor.level = new SensoroData();
                if (e3214.level != null) {
                    levelMap.put(sensoroSensor.macAddress, e3214.level);
                    sensoroSensor.hasLevel = true;
                    sensoroSensor.level.has_data = true;
                }
                sensoroSensor.level.data_float = levelMap.get(sensoroSensor.macAddress);

                sensoroSensor.pitch = new SensoroData();
                if (e3214.pitchAngle != null) {
                    pitchAngleMap.put(sensoroSensor.macAddress, e3214.pitchAngle);
                    sensoroSensor.hasPitch = true;
                    sensoroSensor.pitch.has_data = true;
                }
                sensoroSensor.pitch.data_float = pitchAngleMap.get(sensoroSensor.macAddress);

                sensoroSensor.roll = new SensoroData();
                if (e3214.rollAngle != null) {
                    rollAngleMap.put(sensoroSensor.macAddress, e3214.rollAngle);
                    sensoroSensor.hasRoll = true;
                    sensoroSensor.roll.has_data = true;
                }
                sensoroSensor.roll.data_float = rollAngleMap.get(sensoroSensor.macAddress);

                sensoroSensor.yaw = new SensoroData();
                if (e3214.yawAngle != null) {
                    yawAngleMap.put(sensoroSensor.macAddress, e3214.yawAngle);
                    sensoroSensor.hasYaw = true;
                    sensoroSensor.yaw.has_data = true;
                }
                sensoroSensor.yaw.data_float = yawAngleMap.get(sensoroSensor.macAddress);

                sensoroSensor.flame = new SensoroData();
                if (e3214.flame != null) {
                    flameMap.put(sensoroSensor.macAddress, e3214.flame);
                    sensoroSensor.hasFlame = true;
                    sensoroSensor.flame.has_data = true;
                }
                sensoroSensor.flame.data_int = flameMap.get(sensoroSensor.macAddress);

                sensoroSensor.gas = new SensoroData();
                if (e3214.artificial_gas != null) {
                    gasMap.put(sensoroSensor.macAddress, e3214.artificial_gas);
                    sensoroSensor.hasGas = true;
                    sensoroSensor.gas.has_data = true;
                }
                sensoroSensor.gas.data_float = gasMap.get(sensoroSensor.macAddress);

                sensoroSensor.smoke = new SensoroData();
                if (e3214.smoke != null) {
                    smokeMap.put(sensoroSensor.macAddress, e3214.smoke);
                    sensoroSensor.hasSmoke = true;
                    sensoroSensor.smoke.has_data = true;
                }
                sensoroSensor.smoke.data_int = smokeMap.get(sensoroSensor.macAddress);

                sensoroSensor.waterPressure = new SensoroData();
                if (e3214.pressure != null) {
                    pressureMap.put(sensoroSensor.macAddress, e3214.pressure);
                    sensoroSensor.hasWaterPressure = true;
                    sensoroSensor.waterPressure.has_data = true;
                }
                sensoroSensor.waterPressure.data_float = pressureMap.get(sensoroSensor.macAddress);
                //
                sensoroSensor.setType(BLEDevice.TYPE_SENSOR);
                if (sensoroSensor.getSn() == null) {
                    sensoroSensor = null;
                }
            }
        }
        if (device_data != null) {
            SensoroDevice bleDevice = new SensoroDevice();
            byte[] sn = new byte[8];
            try {
                System.arraycopy(device_data, 0, sn, 0, sn.length);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            bleDevice.setSn(SensoroUUID.parseSN(sn));

            byte[] hardware = new byte[2];
            System.arraycopy(device_data, 8, hardware, 0, hardware.length);
            int hardwareCode = (int) hardware[0] & 0xff;
            String hardwareVersion = Integer.toHexString(hardwareCode).toUpperCase();
            bleDevice.setHardwareVersion(hardwareVersion);

            int firmwareCode = (int) hardware[1] & 0xff;
            String firmwareVersion = Integer.toHexString(firmwareCode / 16).toUpperCase() + "." + Integer.toHexString
                    (firmwareCode % 16).toUpperCase();
            bleDevice.setFirmwareVersion(firmwareVersion);

            int batteryLevel = ((int) device_data[10] & 0xff);
            bleDevice.setBatteryLevel(batteryLevel);

            int power = ((int) device_data[11] & 0xff);
            bleDevice.setPower(power);

            byte[] sf = new byte[4];
            System.arraycopy(device_data, 12, sf, 0, sf.length);
            bleDevice.setSf(SensoroUUID.byteArrayToFloat(sf, 0));

            bleDevice.setMacAddress(scanBLEResult.getDevice().getAddress());
            int last_index = device_data.length - 1;
            if (device_data[last_index] == 0x01) { // dfu
                bleDevice.setDfu(true);
            } else {
                bleDevice.setDfu(false);
            }
            if (sensoroSensor != null) {
                bleDevice.setSensoroSensorTest(sensoroSensor);
            }
            bleDevice.setRssi(scanBLEResult.getRssi());
            bleDevice.setType(BLEDevice.TYPE_DEVICE);
            return bleDevice;
        } else if (station_data != null) {
            SensoroStation bleDevice = new SensoroStation();
            byte[] sn = new byte[8];
            System.arraycopy(station_data, 0, sn, 0, sn.length);
            bleDevice.setSn(SensoroUUID.parseSN(sn));

            byte[] hardware = new byte[2];
            System.arraycopy(station_data, 8, hardware, 0, hardware.length);
            int hardwareCode = (int) hardware[0] & 0xff;
            String hardwareVersion = Integer.toHexString(hardwareCode).toUpperCase();
            bleDevice.setHardwareVersion(hardwareVersion);

            int firmwareCode = (int) hardware[1] & 0xff;
            String firmwareVersion = Integer.toHexString(firmwareCode / 16).toUpperCase() + "." + Integer.toHexString
                    (firmwareCode % 16).toUpperCase();
            bleDevice.setFirmwareVersion(firmwareVersion);

            int workStatus = ((int) station_data[10] & 0xff);
            bleDevice.setWorkStatus(workStatus);
            //03, 0c, 30
//            int netStatus = ((int) station_data[11] & 0xff);
            int wifiStatus = (int) station_data[11] & 0x03;
            int ethStatus = ((int) station_data[11] & 0x0c) >> 2;
            int celluarStatus = ((int) station_data[11] & 0x30) >> 4;
//            bleDevice.setNetStatus(netStatus);
            bleDevice.setWifiStatus(wifiStatus);
            bleDevice.setEthStatus(ethStatus);
            bleDevice.setCellularStatus(celluarStatus);
            bleDevice.setRssi(scanBLEResult.getRssi());
            bleDevice.setMacAddress(scanBLEResult.getDevice().getAddress());
            bleDevice.setType(BLEDevice.TYPE_STATION);
            return bleDevice;
        } else {
            if (sensor_data != null) {
                return sensoroSensor;
            } else {
                return null;
            }

        }
    }

    protected static void clear() {
        snMap.clear();
        lightMap.clear();
        temperatureMap.clear();
        humidityMap.clear();
        leakMap.clear();
        coMap.clear();
        co2Map.clear();
        no2Map.clear();
        methaneMap.clear();
        lpgMap.clear();
        pm1Map.clear();
        pm25Map.clear();
        pm10Map.clear();
        coverstatusMap.clear();
        levelMap.clear();
        pitchAngleMap.clear();
        rollAngleMap.clear();
        yawAngleMap.clear();
        flameMap.clear();
        gasMap.clear();
        smokeMap.clear();
        pressureMap.clear();
    }
}


