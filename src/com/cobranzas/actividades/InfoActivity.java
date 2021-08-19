package com.cobranzas.actividades;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

import com.cobranzas.adaptadores.recy.AdaptadorRcy2String;
import com.cobranzas.adaptadores.recy.AdaptadorRcyContenedor;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.IPS;
import com.cobranzas.model.ContenedorRcyModel;
import com.cobranzas.model.DosStringModel;
import com.cobranzas.model.Item;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.pos.device.config.DevConfig;
import com.wposs.cobranzas.BuildConfig;
import com.wposs.cobranzas.R;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.desert.newpos.payui.master.FormularioActivity;

import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;

public class InfoActivity extends FormularioActivity {

    String clase = "InfoActivity.java";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        initToolbar();
        String menu;
        if (getIntent().hasExtra("menu")) {
            menu = getIntent().getStringExtra("menu");
        } else {
            menu = " ";
        }
        int layout;
        if (DefinesBANCARD.CONFI_INFO.equals(menu)) {
            layout = R.layout.item_item_info_confi_;
            findViewById(R.id.imgView).setVisibility(View.GONE);
            TextView textView = findViewById(R.id.tvTitulo);
            textView.setVisibility(View.VISIBLE);

            textView.setText(menu);
        } else {
            layout = R.layout.item_item_info_;
        }
        RecyclerView recyclerView = findViewById(R.id.rcy);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AdaptadorRcy2String adaptadorRcyDosString = new AdaptadorRcy2String(layout, this);
        recyclerView.setAdapter(new AdaptadorRcyContenedor(getItems(menu), InfoActivity.this, R.layout.item_info, adaptadorRcyDosString));

        mostrarSerialvsVersion();

    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }


    private List<Item> getItems(String menu) {
        List<Item> result = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        StringBuilder string = new StringBuilder(" ");
        if (DefinesBANCARD.CONFI_INFO.equals(menu)) {
            ContenedorRcyModel contenedorRcyModel;
            IPS ip;
            for (int i = 0; i < ChequeoIPs.getLengIps(); i++) {
                ip = ChequeoIPs.seleccioneIP(i);
                items.add(new DosStringModel("ID", ip.getIdIp()));
                items.add(new DosStringModel("Nombre", ip.getIdIp()));
                items.add(new DosStringModel("Ip Host", ip.getIP()));
                items.add(new DosStringModel("Puerto", ip.getPuerto()));
                items.add(new DosStringModel("TLS", String.valueOf(ip.isTls())));
                items.add(new DosStringModel("Cliente", String.valueOf(ip.isAutenticarCliente())));
                contenedorRcyModel = new ContenedorRcyModel("IP " + i, items);
                result.add(contenedorRcyModel);
                items = new ArrayList<>();
            }
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            String nombreComercio = tablaComercios.sucursal.getDescripcion();
            if (nombreComercio != null) {
                items.add(new DosStringModel(nombreComercio));
            }

            ContenedorRcyModel contenedorRcyModel = new ContenedorRcyModel("Información del comercio", items);
            result.add(contenedorRcyModel);


            items = new ArrayList<>();
            items.add(new DosStringModel("IP", IPS.getIPAddress(true)));
            try {
                String address = getmacaddr();
                Logger.debug("MAC ADD: " + address);
                items.add(new DosStringModel("MAC", address));
                items.add(new DosStringModel("Operador", telephonyManager.getNetworkOperatorName()));
                items.add(new DosStringModel("IMEI", telephonyManager.getDeviceId()));
            } catch (Exception e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                e.printStackTrace();
            }
            items.add(new DosStringModel("Modo avión", isAirplaneModeOn()));
            items.add(new DosStringModel("Android", Build.VERSION.RELEASE));
            items.add(new DosStringModel("Serial", DevConfig.getSN()));
            contenedorRcyModel = new ContenedorRcyModel("Dispositivo", items);
            result.add(contenedorRcyModel);

            items = new ArrayList<>();
            items.add(new DosStringModel("Nombre", getString(R.string.app_name)));
            items.add(new DosStringModel("Versión", BuildConfig.VERSION_NAME));
            items.add(new DosStringModel("Tipo de app", BuildConfig.BUILD_TYPE));
            for (String db : getApplicationContext().databaseList()) {
                string.append(" ").append(db);
            }
            items.add(new DosStringModel("Nombre db", string.toString()));
            contenedorRcyModel = new ContenedorRcyModel("Información de app", items);
            result.add(contenedorRcyModel);
        }


        return result;
    }

    public static String getmacaddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macbytes = nif.getHardwareAddress();
                if (macbytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macbytes) {
                    res1.append(String.format("%02x:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return "02:00:00:00:00:00";
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(0, 0);
            }
        });

    }

    private String isAirplaneModeOn() {

        if (Settings.System.getInt(this.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
            return "Si";

        } else {
            return "No";

        }
    }
}
