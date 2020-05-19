package com.sensoro.libbleserver.ble.callback;

import androidx.annotation.Nullable;

import com.sensoro.libbleserver.ble.entity.BLEDevice;

/**
 * Created by sensoro on 17/5/4.
 */

public interface SensoroConnectionCallback {

    void onConnectedSuccess(@Nullable BLEDevice bleDevice, int cmd);

    void onConnectedFailure(int errorCode);

    void onDisconnected();
}
