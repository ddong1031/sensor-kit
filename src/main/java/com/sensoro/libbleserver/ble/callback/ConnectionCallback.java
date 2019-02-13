package com.sensoro.libbleserver.ble.callback;

public interface ConnectionCallback {
    void onConnectFailed(int resultCode);

    void onConnectSuccess();

    void onNotify(byte[] data);
}
