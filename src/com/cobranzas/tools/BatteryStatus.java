package com.cobranzas.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryStatus extends BroadcastReceiver {

    static BatteryStatus batteryStatus;

    int levelBattery;
    boolean isCharging = false;

    public int getLevelBattery() {
        return levelBattery;
    }

    public boolean isCharging() {
        return isCharging;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        this.levelBattery = level;
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isChargingLocal = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        this.isCharging = isChargingLocal;
    }

    public static BatteryStatus getInstance(){
        if (batteryStatus == null){
            batteryStatus = new BatteryStatus();
        }
        return batteryStatus;
    }
}
