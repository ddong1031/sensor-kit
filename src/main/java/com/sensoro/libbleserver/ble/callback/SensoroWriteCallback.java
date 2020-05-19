package com.sensoro.libbleserver.ble.callback;

import androidx.annotation.Nullable;

/**
 * Created by sensoro on 17/5/4.
 */

public interface SensoroWriteCallback {
    void onWriteSuccess(@Nullable Object o, int cmd);

    void onWriteFailure(int errorCode, int cmd);

}
