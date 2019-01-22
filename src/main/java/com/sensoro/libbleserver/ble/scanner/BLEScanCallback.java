package com.sensoro.libbleserver.ble.scanner;

import com.sensoro.libbleserver.ble.entity.ScanBLEResult;

/**
 * Created by Sensoro on 15/6/2.
 */
public interface BLEScanCallback {
    void onLeScan(ScanBLEResult scanBLEResult);

    void onScanCycleFinish();
}
