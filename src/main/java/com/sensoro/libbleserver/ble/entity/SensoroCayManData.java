package com.sensoro.libbleserver.ble.entity;

import java.io.Serializable;

/**
 * 嘉德自研烟感
 * 1、浮点型数据一律扩大相应倍数后，以整形数据上行或下行，以正常数据显示给用户
 * 类型	扩大倍数
 * valueOfTem	10
 * valueOfHum	10
 * alarmOfHighTem	10
 * alarmOfLowTem	10
 * alarmOfHighHum	10
 * alarmOfLowHum	10
 * valueOfBatb	1000
 * 2、bleAdvType（蓝牙广播类型）值为0时，app不显示bleAdvStartTime 和 bleAdvEndTime 参数；bleAdvType = 1时，显示bleAdvStartTime 和 bleAdvEndTime 参数
 */
public class SensoroCayManData implements Serializable {
    public boolean hasIsSmoke;
    public boolean hasIsMoved;
    public boolean hasValueOfTem;
    public boolean hasValueOfHum;
    public boolean hasAlarmOfHighTem;
    public boolean hasAlarmOfLowTem;
    public boolean hasAlarmOfHighHum;
    public boolean hasAlarmOfLowHum;
    public boolean hasCmd;
    public boolean hasValueOfphotor;
    public boolean hasBleAdvType;
    public boolean hasBleAdvStartTime;
    public boolean hasBleAdvEndTime;
    public boolean hasValueOfBatb;
    public boolean hasHumanDetectionTime;
    public boolean hasDefenseMode;
    public boolean hasDefenseTimerMode;
    public boolean hasDefenseModeStartTime;
    public boolean hasDefenseModeStopTime;
    public boolean hasInvadeAlarm;
    //
    public boolean hasCdsSwitch;
    public boolean hasNightLightSwitch;
    public boolean hasHumanDetectionSwitch;
    public boolean hasHumanDetectionSync;
    public boolean hasVoicePlayIndex;


    public int isSmoke;// 烟雾状态 0 无烟 1 有烟
    public int isMoved;// 安装状态 0 未安装 1 安装
    public int valueOfTem; // 温度值 范围: [-40.0-140.0] 单位: 摄氏度
    public int valueOfHum; // 湿度值: [0%-100%]
    public int alarmOfHighTem; //温度预警值（高）范围: [54℃~65℃] 默认值: 54℃
    public int alarmOfLowTem;// 温度预警值（低）范围: [-40℃~-20℃] 默认值: -20℃
    public int alarmOfHighHum;//湿度预警值（高）范围: [90%~100%] 默认值: 90%
    public int alarmOfLowHum;//湿度预警值（低）范围: [0%~10%] 默认值: 0%
    public int cmd;//Bit0:自检 Bit1:恢复出厂 Bit2:消音
    public int valueOfphotor;//光敏值
    public int bleAdvType;//蓝牙广播类型 蓝牙广播类型	0-持续广播 1-间断广播
    public int bleAdvStartTime;//蓝牙广播开始时间 蓝牙广播开始时间	[0x0000-0x2359]
    public int bleAdvEndTime;//蓝牙广播开始时间 蓝牙广播结束时间	[0x0000-0x2359]
    public int valueOfBatb;//从机电池电量 [0-3.5V]
    public int humanDetectionTime;//人体检测时间参数, 单位 s, 范围 [1 - 30], (上|下)
    public int defenseMode;//设防模式开关, 范围 [0 - 1], (上|下)
    public int defenseTimerMode;//定时设防模式开关, 范围 [0 - 1], (上|下)
    public int defenseModeStartTime;//定时设防开始时间, 单位 min, 范围 [0 - 1439], (上|下)
    public int defenseModeStopTime;//定时设防结束时间, 单位 min, 范围 [0 - 1439], (上|下)
    public int invadeAlarm;//入侵报警开关, 范围 [0 - 1], (上|下)
    //
    public int cdsSwitch;           // 凝露检测开关, 范围 [0 - 1], (上|下)
    public int nightLightSwitch;    // 夜灯开关, 范围 [0 - 1], (上|下)
    public int humanDetectionSwitch;// 人体检测开关, 范围 [0 - 1], (上|下)
    public int humanDetectionSync;  // 人体检测同步开关, 范围 [0 - 1], (上|下)
    public int voicePlayIndex;      // 语音播放 index, (下) (α Tool 无需关心)

}
