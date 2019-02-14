package com.sensoro.libbleserver.ble.chipeupgrade;

public interface ChipEUpgradeErrorCode {
    int FILE_NO_EXIST = 0;//文件为空
    int FILE_SIZE_ZERO = 1;//文件大小为空
    int HEAD_PACKET_ERROR = 2;//packet 包错误
    int SEND_HEAD_PACKET_ERROR = 3;// 发送头部包失败
    int SEND_PACKET_ERROR = 4;//发送数据错误
    int SEND_VERIFY_ERROR = 5;//发送确认指令失败
    int UPGRADE_ERROR = 6;//升级异常
    int UPGRADE_CMD_ERROR = 7;//升级过程错误
}
