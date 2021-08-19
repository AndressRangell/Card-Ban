package com.cobranzas.cajas;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import cn.desert.newpos.payui.UIUtils;

public class JSONServer extends Thread {

    static String clase = "JSONServer.java";

    private static int puertoEstablecido;
    private static WebServer androidWebServer;

    private static boolean startAndroidWebServer(int port, Context context1) {
        Logger.debug("Inicio server");
        try {
            if (port != puertoEstablecido) {
                puertoEstablecido = port;
                androidWebServer = null;
            }
            if (androidWebServer == null) {
                androidWebServer = new WebServer(port, context1);
                androidWebServer.start();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean stopAndroidWebServer() {
        if (androidWebServer != null) {
            androidWebServer.stop();
            return true;
        }
        return false;
    }

    public static void getInstanceServer(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Activity activity = (Activity) context;
                int puerto = ChequeoIPs.getPuertoCajas("CAJA");
                if (puerto <= 0) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UIUtils.toast(activity, R.drawable.ic_cobranzas_blanca, "No se encuentra puerto caja", Toast.LENGTH_LONG);
                        }
                    });
                    stopAndroidWebServer();
                    return;
                }
                if (!startAndroidWebServer(puerto, context)) {
                    Log.e("server", "server iniciando...");
                }
            }
        }).start();
    }
}
