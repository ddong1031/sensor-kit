package com.sensoro.libbleserver.ble.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.sensoro.libbleserver.ble.entity.BLEDevice;
import com.sensoro.libbleserver.ble.entity.ScanBLEResult;
import com.sensoro.libbleserver.ble.factory.BLEDeviceFactory;
import com.sensoro.libbleserver.ble.scanner.BLEDeviceManager;
import com.sensoro.libbleserver.ble.scanner.BLEScanCallback;
import com.sensoro.libbleserver.ble.scanner.BLEScanner;
import com.sensoro.libbleserver.ble.scanner.ScanBLEFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BleScanTask implements BLEScanCallback {

    private final ConcurrentHashMap<String, BLEDevice> scanDeviceHashMap = new ConcurrentHashMap<>();
    private final ArrayList<BLEDevice> updateDevices = new ArrayList<BLEDevice>();
    private BLEScanner bleScanner;
    private ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static Context mContext;

    private BleScanTask() {
        bleScanner = BLEScanner.createScanner(mContext, this);
        executorService = Executors.newCachedThreadPool();
        List<ScanBLEFilter> scanBLEResults = new ArrayList<>();
        ScanBLEFilter scanBLEFilter = new ScanBLEFilter.Builder()
                .build();
        scanBLEResults.add(scanBLEFilter);
        bleScanner.setScanBLEFilters(scanBLEResults);
//        bleScanner.setScanPeriod(BLEDeviceManager.FOREGROUND_SCAN_PERIOD);
        bleScanner.setScanPeriod(2000);
        bleScanner.setBetweenScanPeriod(BLEDeviceManager.FOREGROUND_BETWEEN_SCAN_PERIOD);
    }

    public static BleScanTask getInstance(Context context) {
        if (context == null) {
            throw new NullPointerException("不能为空");
        }
        mContext = context.getApplicationContext();
        return BleScanTaskHolder.instance;
    }

    private static final class BleScanTaskHolder {
        private static final BleScanTask instance = new BleScanTask();
    }

    public void startScan() {
        if (bleScanner != null) {
            bleScanner.start();
        }

    }

    public void stopScan() {
        if (bleScanner != null) {
            bleScanner.stop();
        }

    }

    public void onDestroy() {
        bleScanner.stop();
        executorService.shutdown();
        mainHandler.removeCallbacksAndMessages(null);
    }

    public void setBackgroundMode(boolean isBackgroundMode) {
        if (bleScanner != null) {
            if (isBackgroundMode) {
                bleScanner.setScanPeriod(BLEDeviceManager.BACKGROUND_SCAN_PERIOD);
                bleScanner.setBetweenScanPeriod(BLEDeviceManager.BACKGROUND_BETWEEN_SCAN_PERIOD);
            } else {
                bleScanner.setScanPeriod(BLEDeviceManager.FOREGROUND_SCAN_PERIOD);
                bleScanner.setBetweenScanPeriod(BLEDeviceManager.FOREGROUND_BETWEEN_SCAN_PERIOD);
                bleScanner.stop();
                bleScanner.start();
            }
        }
    }

    private void processScanDevice(BLEDevice device) {
        BLEDevice containedDevice = scanDeviceHashMap.get(device.getMacAddress());
        if (containedDevice == null) {
            scanDeviceHashMap.put(device.getMacAddress(), device);
            enterDevice(device);
        } else {
            updateDeviceInfo(device, containedDevice);
        }
    }

    private void enterDevice(BLEDevice device) {
        try {
            final BLEDevice newDevice = device.clone();
            if (Looper.myLooper() == Looper.getMainLooper()) {
                BLEDeviceManager.getInstance(mContext).getBLEDeviceListener().onNewDevice(newDevice);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BLEDeviceManager.getInstance(mContext).getBLEDeviceListener().onNewDevice(newDevice);
                    }
                });
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

    private void updateDeviceInfo(BLEDevice newDevice, BLEDevice containedDevice) {
        try {
            containedDevice.setSn(newDevice.getSn());
            String hardwareVersion = newDevice.getHardwareVersion();
            if (!TextUtils.isEmpty(hardwareVersion)) {
                containedDevice.setHardwareVersion(hardwareVersion);
            }
            String firmwareVersion = newDevice.getFirmwareVersion();
            if (!TextUtils.isEmpty(firmwareVersion)) {
                containedDevice.setFirmwareVersion(firmwareVersion);
            }
            containedDevice.lastFoundTime = newDevice.lastFoundTime;
            containedDevice.setBatteryLevel(newDevice.getBatteryLevel());
            containedDevice.setRssi(newDevice.getRssi());
            containedDevice.setType(newDevice.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateDevices() {
        try {
            final ArrayList<BLEDevice> updateDevicesClone = (ArrayList<BLEDevice>) updateDevices.clone();
            if (Looper.myLooper() == Looper.getMainLooper()) {
                BLEDeviceManager.getInstance(mContext).getBLEDeviceListener().onUpdateDevices(updateDevicesClone);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BLEDeviceManager.getInstance(mContext).getBLEDeviceListener().onUpdateDevices(updateDevicesClone);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void exitDevice() {
        // 清空updateDevices
        try {
            updateDevices.clear();
            for (Map.Entry entry : scanDeviceHashMap.entrySet()) {
                BLEDevice monitoredDevice = (BLEDevice) entry.getValue();
                if (System.currentTimeMillis() - monitoredDevice.lastFoundTime > BLEDeviceManager.OUT_OF_RANGE_DELAY) {
                    final BLEDevice goneDevice = monitoredDevice.clone();
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        BLEDeviceManager.getInstance(mContext).getBLEDeviceListener().onGoneDevice(goneDevice);
                    } else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                BLEDeviceManager.getInstance(mContext).getBLEDeviceListener().onGoneDevice(goneDevice);
                            }
                        });
                    }
                    scanDeviceHashMap.remove(monitoredDevice.getMacAddress());
                } else {
                    updateDevices.add(monitoredDevice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLeScan(final ScanBLEResult scanBLEResult) {
        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (scanDeviceHashMap) {
                        BLEDeviceFactory deviceFactory = new BLEDeviceFactory(scanBLEResult);
                        BLEDevice bleDevice = deviceFactory.create();
                        if (bleDevice != null) {//&& bleDevice.getSn().equals("10310117C5A3FD2D")
                            String sn = bleDevice.getSn();
                            processScanDevice(bleDevice);
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onScanCycleFinish() {
        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (scanDeviceHashMap) {
                        exitDevice();
                        updateDevices();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
