package com.sensoro.libbleserver.ble.callback;

import androidx.annotation.Nullable;

public interface OnDeviceUpdateObserver {
    //正在切换至DFU
    void onEnteringDFU(String deviceMacAddress, String filePath, @Nullable String msg);

    //升级完成
    void onUpdateCompleted(String filePath, String deviceMacAddress, @Nullable String msg);

    //正在传输数据
    void onDFUTransfer(String deviceAddress, int percent, float speed, float avgSpeed, int
            currentPart, int partsTotal, @Nullable String msg);

    //正在校验
    void onUpdateValidating(String deviceMacAddress,@Nullable String msg);

    //业务超时
    void onUpdateTimeout(int code, @Nullable Object data, @Nullable String msg);

    //断开连接
    void onDisconnecting();

    //操作失败
    void onFailed(String deviceMacAddress, @Nullable String errorMsg, @Nullable Throwable e);

}
