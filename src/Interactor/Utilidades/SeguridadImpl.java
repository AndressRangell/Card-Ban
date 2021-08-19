package Interactor.Utilidades;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;

import com.newpos.libpay.Logger;

public class SeguridadImpl  {

    private SeguridadImpl(){}

    public static boolean isNamedProcessRunning(Context ctx, String processName) {

        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            String serv = service.service.getClassName();
            if (serv.contains(processName)){
                return true;
            }
            Logger.debug("Procesos " + serv);

        }
        return false;
    }

    public static boolean verificarModoSeguro(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.isSafeMode();
    }
}
