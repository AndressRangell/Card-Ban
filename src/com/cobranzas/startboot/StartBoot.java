package com.cobranzas.startboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cobranzas.actividades.StartAppBANCARD;

public class StartBoot extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent Intent = new Intent(context, StartAppBANCARD.class);
        Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent);
    }
}
