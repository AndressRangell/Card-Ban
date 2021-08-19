package com.cobranzas.tools;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.util.List;

import cn.desert.newpos.payui.UIUtils;

public class ConectarseRedWifi extends AppCompatActivity {

    public static void conectarRed(final String nombreRed, String claveRed, final Context context) {
        try {
            final WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + nombreRed + "\"";
            conf.preSharedKey = "\"" + claveRed + "\"";
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);
            assert wifiManager != null;
            if ((!wifiManager.isWifiEnabled())) {
                UIUtils.toast((Activity) context, R.drawable.logoinfonet02, "Activando Wifi..", Toast.LENGTH_SHORT);
                wifiManager.setWifiEnabled(true);
            }
            wifiManager.addNetwork(conf);
            UIUtils.toast((Activity) context, R.drawable.logoinfonet02, "Conectando a la red", Toast.LENGTH_LONG);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + nombreRed + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    UIUtils.toast((Activity) context, R.drawable.logoinfonet02, "Se ha conectando a la Red", Toast.LENGTH_SHORT);
                    break;
                }
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, "ConectarseRedWifi.java", e.getMessage());
            Logger.logLine(LogType.EXCEPTION, "ConectarseRedWifi.java", e.getStackTrace());
            e.getMessage();
        }
    }
}
