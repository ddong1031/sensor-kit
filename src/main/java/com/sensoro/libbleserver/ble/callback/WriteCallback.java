package com.sensoro.libbleserver.ble.callback;

public interface WriteCallback {
    void onWriteSuccess();

    void onWriteFailure(int errorCode);
}
