package com.cobranzas.actividades;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.adaptadores.recy.AdaptadorRcyConfiWifi;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.FormularioActivity;

public class ConfiWifiActivity extends FormularioActivity implements AdaptadorRcyConfiWifi.OnItemClickListener {

    ProgressBar progressBar;
    Context context = this;
    CountDownTimer timer;
    Toolbar toolbar;
    int signal = 0;
    String tipoRed;
    String clase = "ConfiWifiActivity.java";
    Handler handler = new Handler();
    AdaptadorRcyConfiWifi rcyConfiWifi = null;
    boolean isMostrarPass = false;
    CountDownTimer timer2;
    String ssdConected;
    private WifiManager wifiManager;
    private RelativeLayout rwebView;
    private ArrayList<ModeloBotones> listaRedes;
    private RecyclerView recy;
    private TextView tvTitulo;
    private LinearLayout lyL;


    private Switch aSwitch;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            tvTitulo.setText("Redes Wifi Disponibles");
            rwebView.setVisibility(View.GONE);
            timer.start();
            mostrarLista(true);
        }
    };
    private Dialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        recy = findViewById(com.wposs.cobranzas.R.id.rvMenus);
        recy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        progressBar = findViewById(R.id.handling_loading);
        rwebView = findViewById(R.id.rl02);
        tvTitulo = findViewById(R.id.tvTitulo);
        lyL = findViewById(R.id.ly01);
        LinearLayout lyR = findViewById(R.id.ly02);


        mostrarSerialvsVersion();
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(getDrawable(R.drawable.ic_autorenew_black_24dp));

        aSwitch = new Switch(this);
        aSwitch.setThumbDrawable(getDrawable(R.drawable.fondo_switch));
        aSwitch.setSwitchTextAppearance(this, R.style.SwitchTextAppearance);
        aSwitch.setTextOn("ON");
        aSwitch.setTextOff("OFF");
        aSwitch.setShowText(true);
        aSwitch.setChecked(true);
        aSwitch.setEnabled(false);


        lyR.addView(aSwitch);
        lyR.setVisibility(View.VISIBLE);
        lyL.addView(imageView);


        tvTitulo.setText("Wifi");
        initToolbar();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        obtenerLista();


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.reassociate();
                lyL.setVisibility(View.GONE);
                obtenerLista();
                actualizarRecy(true);
            }
        });

        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estadoWifi(!aSwitch.isChecked());
                if (!aSwitch.isChecked()) {
                    tvTitulo.setText("Wifi Apagado");
                    lyL.setVisibility(View.GONE);
                    actualizarRecy(true);
                }
            }
        });
        inicializarTimer();

    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void inicializarTimer() {
        timer = new CountDownTimer(50000, 5000) {
            public void onTick(long millisUntilFinished) {
                // Do nothing because of X and Y.
            }

            public void onFinish() {
                wifiManager.reassociate();
                mostrarLista(true);
                timer.cancel();
                timer.start();
            }

        };
    }

    private void estadoWifi(boolean estado) {
        if (!estado) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            obtenerLista();

        } else {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                timer.cancel();
            }
            rwebView.setVisibility(View.GONE);

            handler.removeCallbacks(r);
        }
    }

    private void obtenerLista() {
        rwebView.setVisibility(View.VISIBLE);
        recy.setVisibility(View.GONE);
        tvTitulo.setText("Buscando redes Wifi");

        handler.postDelayed(r, 5000);
        recy.setVisibility(View.VISIBLE);

    }

    private void mostrarLista(boolean isNew) {
        wifiManager.reassociate();

        listaRedes = getListRedes(wifiManager.getScanResults(), wifiManager.getConnectionInfo());

        if (isNew) {
            rcyConfiWifi = new AdaptadorRcyConfiWifi(context, listaRedes, this, R.layout.item_img_string);
            recy.setAdapter(rcyConfiWifi);
        } else {
            rcyConfiWifi.notifyDataSetChanged();

        }
        if (!listaRedes.isEmpty()) {
            lyL.setVisibility(View.VISIBLE);
            aSwitch.setEnabled(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private ArrayList<ModeloBotones> getListRedes(List<ScanResult> scanResults, WifiInfo conectado) {
        ArrayList<ModeloBotones> resultados = new ArrayList<>();
        ModeloBotones item;


        if (scanResults != null) {
            for (int i = 0; i < scanResults.size(); i++) {
                if (!scanResults.get(i).SSID.isEmpty()) {
                    item = new ModeloBotones(scanResults.get(i).SSID);
                    signal = WifiManager.calculateSignalLevel(scanResults.get(i).level, 3);
                    item.imageDrw = (getIconSignal(signal, false));
                    item.setIdBoton(signal);
                    resultados.add(item);

                }
            }

        }

        String s = conectado.getSSID().replace("\"", "");

        if (s != null && !s.isEmpty() && (!s.trim().equals("0x") && !s.trim().equals("<unknown ssid>"))) {
            item = new ModeloBotones(s);
            signal = WifiManager.calculateSignalLevel(conectado.getRssi(), 3);
            item.imageDrw = (getIconSignal(signal, true));
            item.setIdBoton(signal);
            resultados.add(item);
        }
        if (!resultados.isEmpty()) {
            resultados = organizarLista(resultados);
        }
        return resultados;
    }

    public void solicitarContrasena(final String titulo, final ModeloBotones obj) {
        final int typeNet = typeNetwork(titulo);
        if (typeNet > 0) {
            mDialog = UIUtils.centerDialog(ConfiWifiActivity.this, R.layout.setting_home_wifi, R.id.setting_pass_layout);
            final EditText editText;
            ImageView close;
            ImageButton aceptar;

            editText = mDialog.findViewById(R.id.editText);
            close = mDialog.findViewById(R.id.setting_pass_close);
            aceptar = mDialog.findViewById(R.id.btnAceptar);
            editText.setHint("Contraseña de " + titulo);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.requestFocus();

            final CheckBox checkBox = mDialog.findViewById(R.id.checkbox);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });


            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();


                }
            });

            aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    validarRed(titulo, editText.getText().toString(), typeNet, obj);
                }
            });

            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == keyEvent.KEYCODE_ENDCALL) {
                        validarRed(titulo, editText.getText().toString(), typeNet, obj);
                        mDialog.dismiss();
                    }
                    return true;
                }
            });
        } else {
            validarRed(titulo, "", typeNet, obj);
        }
    }

    private int typeNetwork(String titulo) {
        List<ScanResult> lista = wifiManager.getScanResults();

        Set<ScanResult> hashSet = new HashSet<>(lista);
        lista.clear();
        lista.addAll(hashSet);

        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).SSID.equals(titulo)) {
                String capabilities = lista.get(i).capabilities;
                if (!capabilities.contains("-")) {
                    return WifiConfiguration.KeyMgmt.NONE;
                } else {
                    String type = capabilities.substring(capabilities.indexOf("-") + 1, capabilities.indexOf("-") + 4);

                    if (type.contains("IEEE802.1X")) {
                        return WifiConfiguration.KeyMgmt.IEEE8021X;
                    } else if (type.contains("WPA")) {
                        tipoRed = "WPA";
                        return WifiConfiguration.KeyMgmt.WPA_EAP;
                    } else if (type.contains("PSK")) {
                        tipoRed = "WPA/WPA2 PSK";
                        return WifiConfiguration.KeyMgmt.WPA_PSK;

                    }
                }
            }
        }
        return -1;
    }

    private void validarRed(String red, String contrasena, int typeKey, ModeloBotones obj) {
        try {
            boolean conexionExitosa;
            // setup a ic_wifi configuration
            WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = "\"" + red + "\"";
            if (typeKey != WifiConfiguration.KeyMgmt.NONE) {
                wc.preSharedKey = "\"" + contrasena + "\"";
            }
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedKeyManagement.set(typeKey);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            // connect to and enable the connection
            int netId = wifiManager.addNetwork(wc);
            conexionExitosa = wifiManager.enableNetwork(netId, false);

            if (conexionExitosa) {
                conectar(red, obj);
            } else {
                UIUtils.toast((Activity) context, R.drawable.logoinfonet02, "Longitud inválida", Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    private void conectar(final String ssid, final ModeloBotones obj) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        timer.cancel();
        progressDialog.setMessage("Conectando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        timer2 = new CountDownTimer(20800, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                wifiManager.reassociate();
                ssdConected = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                if (wifi.isConnected()) {
                    if (ssdConected.equals(ssid)) {
                        UIUtils.toast((Activity) context, R.drawable.logoinfonet, "Conexión establecida", Toast.LENGTH_SHORT);
                        progressDialog.cancel();
                        mDialog.dismiss();
                        mostrarLista(true);

                        timer.start();
                        timer2.cancel();
                    } else {
                        modificarRed(ssdConected);
                    }
                } else {
                    wifiManager.reconnect();
                    wifiManager.reassociate();

                }
            }

            @Override
            public void onFinish() {
                UIUtils.toast((Activity) context, R.drawable.logoinfonet, "Conexión no establecida", Toast.LENGTH_SHORT);
                progressDialog.cancel();
                wifiManager.reassociate();
                mostrarLista(true);
                obj.imageDrw = getIconSignal(obj.getIdBoton(), false);
                timer.start();
                timer2.cancel();
            }
        };
        timer2.start();
    }

    private boolean modificarRed(String ssd) {
        final int typeKey = typeNetwork(ssd);

        // setup a ic_wifi configuration
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + ssd + "\"";
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(typeKey);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        // connect to and enable the connection

        wifiManager.reassociate();

        mostrarLista(false);
        timer.start();
        return true;
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onItemClick(View view, ModeloBotones obj, int position) {
        String red = obj.nombreBoton;
        String redConectada = wifiManager.getConnectionInfo().getSSID();
        if (redConectada != null) {
            if (red.equals(redConectada.replace("\"", ""))) {
                modificarRed(obj.getNombreBoton());
                obj.imageDrw = getIconSignal(obj.getIdBoton(), false);
                infoRed(red, signal);

            } else {
                solicitarContrasena(red, obj);
            }
        }
    }

    private void infoRed(final String red, int potencia) {
        final int typeNet = typeNetwork(red);
        if (typeNet > 0) {
            mDialog = UIUtils.centerDialog(ConfiWifiActivity.this, R.layout.info_home_wifi, R.id.setting_pass_layout);
            TextView editTextNombreRed;
            TextView textViewValPotencia;
            TextView textViewValSeguridad;
            TextView textViewIP;
            ImageView close;
            Button btnOlvidar;
            Button btnListo;

            editTextNombreRed = mDialog.findViewById(R.id.editTextNombreRed);
            textViewValPotencia = mDialog.findViewById(R.id.textViewValPotencia);
            textViewValSeguridad = mDialog.findViewById(R.id.textViewValSeguridad);
            textViewIP = mDialog.findViewById(R.id.textViewIPWifi);

            try {
                WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                String ipAddress = Formatter.formatIpAddress(ip);

                textViewIP.setText(ipAddress);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }


            close = mDialog.findViewById(R.id.setting_pass_close);
            btnOlvidar = mDialog.findViewById(R.id.btnOlvidar);
            btnListo = mDialog.findViewById(R.id.btnListo);
            editTextNombreRed.setText(red);
            textViewValSeguridad.setText(tipoRed);

            switch (potencia) {
                case 0:
                    textViewValPotencia.setText("Baja");
                    break;
                case 1:
                    textViewValPotencia.setText("Media");
                    break;
                case 2:
                    textViewValPotencia.setText("Alta");
                    break;
                default:
                    break;
            }

            /*Obtener informacion necesaria de la red*/
            final WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = "\"" + red + "\"";
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedKeyManagement.set(typeNet);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

            final int redId = wifiManager.addNetwork(wc);

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();

                }
            });
            btnListo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();

                }
            });

            btnOlvidar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean desconexionExitosa = wifiManager.disableNetwork(redId);        // desconectar
                    String temporal;
                    if (desconexionExitosa) {
                        mostrarLista(true);
                        temporal = "Red desconectada";
                    } else {
                        temporal = "No fué posible desconectar";
                    }

                    Toast.makeText(context, temporal, Toast.LENGTH_SHORT).show();

                    wifiManager.reassociate();
                }
            });
        }
    }


    private ArrayList<ModeloBotones> organizarLista(ArrayList<ModeloBotones> resultados) {
        TreeMap<String, ModeloBotones> map = new TreeMap<>();
        for (ModeloBotones item : resultados) {
            map.put(item.nombreBoton, item);
        }

        resultados.clear();
        resultados.addAll(map.values());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resultados.sort(new Comparator<ModeloBotones>() {
                @Override
                public int compare(ModeloBotones o1, ModeloBotones o2) {
                    return new Integer(o2.getIdBoton()).compareTo(new Integer(o1.getIdBoton()));
                }
            });
        }

        return resultados;
    }

    private void actualizarRecy(boolean limpiar) {
        if (limpiar) {
            recy.setAdapter(new AdaptadorRcyConfiWifi(this, new ArrayList<ModeloBotones>(), this, R.layout.item_img_string));
        } else {
            recy.setAdapter(new AdaptadorRcyConfiWifi(this, listaRedes, this, R.layout.item_img_string));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Drawable getIconSignal(int signal, boolean isOn) {
        Drawable result;
        switch (signal) {
            case 0:
                result = getDrawable(R.drawable.signal_low);
                break;
            case 1:
                result = getDrawable(R.drawable.signal_med);
                break;
            case 2:
                result = getDrawable(R.drawable.signal_full);
                break;
            default:
                return getDrawable(R.drawable.signal_null);
        }

        if (isOn) {
            result.setTint(getColor(R.color.colorbuttonnumber));
        } else {
            result.setTint(getColor(R.color.gray_enable));
        }


        return result;
    }
}
