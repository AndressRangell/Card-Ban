package com.cobranzas.tools;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.cobranzas.actividades.MainActivity;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.Device;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.util.Arrays;
import java.util.List;

public class NotificationService extends IntentService {

    private static final int NOTIFICATION_ID = 1;
    Notification notification;

    public NotificationService(String name) {
        super(name);
    }

    public NotificationService() {
        super("SERVICE");
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent2) {
        String notificationChannelId = getApplicationContext().getString(R.string.app_name);
        Context context = this.getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(this, MainActivity.class);
        Resources res = this.getResources();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        String message = getString(R.string.app_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final int NOTIFY_ID = 0; // ID of notification
            String id = notificationChannelId; // default_channel_id
            String title = notificationChannelId; // Default Channel
            PendingIntent pendingIntent;
            NotificationCompat.Builder builder;
            NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifManager == null) {
                notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle("Wifi Activado").setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_senal_wifi)  // required
                    .setContentText(message)
                    .setColorized(true)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_icono_bancard))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            Notification build = builder.build();
            notifManager.notify(NOTIFY_ID, build);

            startForeground(1, build);

        } else {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_senal_wifi)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_wifi))
                    .setColorized(true)
                    .setAutoCancel(true)
                    .setContentTitle("Wifi Activado").setCategory(Notification.CATEGORY_SERVICE)
                    .setContentText(message).build();
            notificationManager.notify(NOTIFICATION_ID, notification);

            if (Device.getConexion()) {
                if (!verificacionConexionWifi(context)) {
                    activarWifi();
                }
            } else {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                }
            }
        }
    }

    private boolean verificacionConexionWifi(Context context) {
        ConnectivityManager connManager1 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager1.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    private void activarWifi() {
        WifiManager wifiManager;
        if (getApplicationContext() != null) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            String tempo;
            String ssid = null;
            String clave = null;
            for (int i = 0; i < ChequeoIPs.getLengIps(); i++) {
                tempo = ChequeoIPs.seleccioneIP(i).getIdIp();
                if (tempo != null) {
                    tempo = tempo.toLowerCase();
                    Logger.error("El IP que llego " + tempo);
                    if (tempo.contains("com_wifi")) {
                        ssid = tempo;
                        clave = ChequeoIPs.seleccioneIP(i).getClaveWifi();
                    }
                }
            }
            if (ssid != null) {
                Logger.error("El SSID a probar es " + ssid);
                Logger.error(connectToNetworkWPA(ssid, clave) ? "Funciono" : "No funciono");
            }
        } else {
            Logger.error("Error en el contexto");
        }
    }

    public boolean connectToNetworkWPA(String networkSSID, String clave) {
        boolean conecto = false;
        try {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\""; // Please note the quotes. String should contain SSID in quotes
            conf.preSharedKey = "\"" + clave + "\"";
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                Logger.error("SSD : " + i.SSID);
                if (i.SSID != null && i.SSID.equalsIgnoreCase("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    Logger.error("Se conecto");
                    conecto = true;
                    break;
                }
            }
            //WiFi Connection success, return true

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.error(Arrays.toString(ex.getStackTrace()));
            return false;
        }

        return conecto;
    }

}