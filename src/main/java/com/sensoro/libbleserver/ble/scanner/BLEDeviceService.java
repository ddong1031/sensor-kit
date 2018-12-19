package com.sensoro.libbleserver.ble.scanner;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.BLEDeviceFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fangping on 2016/7/13.
 */

public class BLEDeviceService extends Service implements BLEScanCallback {

    private final ConcurrentHashMap<String, BLEDevice> scanDeviceHashMap = new ConcurrentHashMap<>();
    private final ArrayList<BLEDevice> updateDevices = new ArrayList<BLEDevice>();
    private BLEScanner bleScanner;
    private ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        bleScanner = BLEScanner.createScanner(this, this);
        executorService = Executors.newCachedThreadPool();
        List<ScanBLEFilter> scanBLEResults = new ArrayList<>();
        ScanBLEFilter scanBLEFilter = new ScanBLEFilter.Builder()
                .build();
        scanBLEResults.add(scanBLEFilter);
        bleScanner.setScanBLEFilters(scanBLEResults);
//        bleScanner.setScanPeriod(BLEDeviceManager.FOREGROUND_SCAN_PERIOD);
        bleScanner.setScanPeriod(2000);
        bleScanner.setBetweenScanPeriod(BLEDeviceManager.FOREGROUND_BETWEEN_SCAN_PERIOD);
        bleScanner.start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        bleScanner.stop();
        executorService.shutdown();
        mainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new BLEDeviceServiceV4Binder();
    }

    public class BLEDeviceServiceV4Binder extends Binder {
        public BLEDeviceService getService() {
            return BLEDeviceService.this;
        }
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
                BLEDeviceManager.getInstance(getApplication()).getBLEDeviceListener().onNewDevice(newDevice);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BLEDeviceManager.getInstance(getApplication()).getBLEDeviceListener().onNewDevice(newDevice);
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
                BLEDeviceManager.getInstance(getApplication()).getBLEDeviceListener().onUpdateDevices(updateDevicesClone);
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BLEDeviceManager.getInstance(getApplication()).getBLEDeviceListener().onUpdateDevices(updateDevicesClone);
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
                        BLEDeviceManager.getInstance(getApplication()).getBLEDeviceListener().onGoneDevice(goneDevice);
                    } else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                BLEDeviceManager.getInstance(getApplication()).getBLEDeviceListener().onGoneDevice(goneDevice);
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
