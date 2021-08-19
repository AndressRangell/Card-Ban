package com.cobranzas.actividades;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.adaptadores.AdaptadorApps;
import com.cobranzas.adaptadores.ModeloApps;
import com.cobranzas.cajas.JSONServer;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.Device;
import com.cobranzas.inicializacion.configuracioncomercio.Tareas;
import com.cobranzas.keys.DUKPT;
import com.cobranzas.menus.MenusActivity;
import com.cobranzas.polaris_validation.ReadWriteFileMDM;
import com.cobranzas.timertransacciones.TimerTrans;
import com.cobranzas.tools.ConectarseRedWifi;
import com.cobranzas.tools.RebootServiceClass;
import com.cobranzas.tools_bacth.ToolsBatch;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.trans.manager.RevesalTrans;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.config.DevConfig;
import com.wposs.cobranzas.BuildConfig;
import com.wposs.cobranzas.R;

import java.io.File;
import java.util.List;
import java.util.Objects;

import Interactor.Utilidades.MenusAplicaciones;
import Interactor.Utilidades.MoreApps;
import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.FormularioActivity;
import cn.desert.newpos.payui.master.MasterControl;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static Interactor.Utilidades.SeguridadImpl.isNamedProcessRunning;
import static Interactor.Utilidades.SeguridadImpl.verificarModoSeguro;
import static com.cobranzas.actividades.StartAppBANCARD.isInit;
import static com.cobranzas.actividades.StartAppBANCARD.readWriteFileMDM;
import static com.cobranzas.defines_bancard.DefinesBANCARD.POLARIS_APP_NAME;
import static com.cobranzas.defines_bancard.DefinesBANCARD.columnaEventosTareaRealizada;
import static com.cobranzas.defines_bancard.DefinesBANCARD.namePreferenciaEventosTareas;
import static com.cobranzas.inicializacion.trans_init.Init.DEFAULT_DOWNLOAD_PATH;
import static com.cobranzas.keys.InjectMasterKey.MASTERKEYIDX;
import static com.cobranzas.keys.InjectMasterKey.TRACK2KEYIDX;
import static com.cobranzas.keys.InjectMasterKey.threreIsKey;
import static com.cobranzas.keys.InjectMasterKey.threreIsKeyWK;
import static com.newpos.libpay.trans.Trans.idLote;

public class MainActivity extends FormularioActivity implements View.OnClickListener {

    private static final String PACKAGEMANAGER = "com.downloadmanager";
    public static boolean modoCaja = false;
    MoreApps moreApps = null;
    Toolbar toolbar;
    StringBuilder builder;
    EditText editText;
    RecyclerView recyclerViewMoreApps;
    ImageView imageViewMoreApps;
    CardView cardviewRecycler;
    String clase = "MainActivity.java";
    int longitudMaxima = 16;
    int longitudMinima = 2;
    AdaptadorApps.OnItemClickListener onItemClickListener = new AdaptadorApps.OnItemClickListener() {
        @Override
        public void onItemClick(View view, ModeloApps obj, int position) {
            String nombrePackage = null;
            switch (obj.getNombreApp()) {
                case "LEALTAD":
                    nombrePackage = "com.wposs.cobranzas_lealtad";
                    break;
                case "POLARIS CLOUD":
                    nombrePackage = PACKAGEMANAGER;
                    break;
                default:
            }

            if (nombrePackage != null && moreApps.isAppInstalada(nombrePackage)) {
                Intent intentapk = getApplicationContext().getPackageManager().getLaunchIntentForPackage(nombrePackage);
                startActivity(intentapk);
            } else {
                UIUtils.toast(MainActivity.this, R.drawable.ic_cobranzas_blanca, "Aplicación no instalada", Toast.LENGTH_SHORT);
            }
        }
    };
    private String requiereInit = "Requiere inicialización";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eliminarDatos(new File(DEFAULT_DOWNLOAD_PATH), clase);

        Bundle bundle = getIntent().getExtras();
        verificacionCasteoAplicacion(bundle);

        verificadorActualizacionAgente();


        if (validadServicioActivoPolaris()) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!Device.getConexion()) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                }
            } else {
                forzarConexionWifi();
            }
            incializacionComponentes();
        } else {
            vistaBloqueoPolaris();
        }

        // Habilita instalacion

        if (!RevesalTrans.isReversalPending()) {
            readWriteFileMDM.writeFileMDM(readWriteFileMDM.getReverse(), readWriteFileMDM.getSettle());
            readWriteFileMDM.writeFileMDM(ReadWriteFileMDM.IS_TRANSACCION_DEACTIVE);
        }
    }

    private void forzarConexionWifi() {
        String temp;
        String nombreRed = null;
        String claveRed = null;

        for (int i = 0; i < ChequeoIPs.getLengIps(); i++) {
            temp = ChequeoIPs.seleccioneIP(i).getIdIp();
            if (temp != null && temp.contains("com_wifi")) {
                nombreRed = temp;
                claveRed = ChequeoIPs.seleccioneIP(i).getClaveWifi();

            }
        }

        if (!RebootServiceClass.alarma) {
            if (nombreRed != null && claveRed != null && !claveRed.equals("")) {
                ConectarseRedWifi.conectarRed(nombreRed, claveRed, MainActivity.this);
            }

        }
    }

    private void vistaBloqueoPolaris() {
        setContentView(R.layout.activity_agente);
        TextView tvVersion = findViewById(R.id.tvVersion);
        tvVersion.setText(BuildConfig.VERSION_NAME);
        TextView tvSerial = findViewById(R.id.tvSerial);
        tvSerial.setText(formatSerial(DevConfig.getSN()));

    }

    public boolean validadServicioActivoPolaris() {

        if (!verificarModoSeguro(MainActivity.this) && isNamedProcessRunning(MainActivity.this, PACKAGEMANAGER)) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            List<ComponentName> activeAdmins = devicePolicyManager.getActiveAdmins();
            if (activeAdmins != null) {
                for (ComponentName admin : activeAdmins) {
                    if (admin.getPackageName().contains(PACKAGEMANAGER)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void verificacionCasteoAplicacion(Bundle bundle) {
        if (bundle != null) {
            boolean casteoAplicacion = bundle.getBoolean(DefinesBANCARD.habilitacionCasteoAplicacion, false);
            if (casteoAplicacion && !getConfirmacionTareaRealizadas()) {
                List<Tareas> tareasList = Tareas.getInstance(false).getListadoTarea2Aplicacion(POLARIS_APP_NAME);
                if (tareasList != null && !tareasList.isEmpty()) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(MainActivity.this, CasteoAplicacionAtivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        }
    }

    private void verificadorActualizacionAgente() {
        try {
            if (readWriteFileMDM != null) {
                readWriteFileMDM.readFileMDM();
                String validActualizacion = readWriteFileMDM.getInitAuto();
                System.out.println("PROCESO ACTUALIZACION " + validActualizacion);
                if (validActualizacion != null && !validActualizacion.equals("") && validActualizacion.equals("1") && ToolsBatch.statusTrans(idLote)) {
                    final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#0E3E8A"));
                    pDialog.setCancelable(false);
                    pDialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.dismiss();

                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setClass(MainActivity.this, MasterControl.class);
                            intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[25]);
                            intent.putExtra(MasterControl.CAJAS, true);
                            startActivity(intent);
                        }
                    }, 2500);

                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    private Boolean getConfirmacionTareaRealizadas() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(namePreferenciaEventosTareas, MODE_PRIVATE);
        return prefs.getBoolean(columnaEventosTareaRealizada, false);
    }

    private void incializacionComponentes() {
        modoCaja = Device.getCajaPOS();

        if (modoCaja) {
            JSONServer.getInstanceServer(MainActivity.this);
        }
        builder = new StringBuilder();
        editText = (EditText) findViewById(R.id.editText);
        TextView tvNombreComercio = findViewById(R.id.tvNombreComercio);
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);

        try {
            if (StartAppBANCARD.tablaComercios.sucursal.getDescripcion() != null) {
                tvNombreComercio.setText(StartAppBANCARD.tablaComercios.sucursal.getDescripcion());
            }
            TimerTrans.deleteTimer();
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            ISOUtil.AlertExcepciones("Error informacion Tabla comerio : " + e.getMessage(), MainActivity.this);
            Log.d("ERROR  ---- ", " onCreate: " + e.getMessage());
        }

        imageViewMoreApps = findViewById(R.id.iv_Info);
        imageViewMoreApps.setImageDrawable(getDrawable(R.drawable.ic_more_apps));
        cardviewRecycler = findViewById(R.id.cardviewRecycler);

        imageViewMoreApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardviewRecycler.getVisibility() == View.GONE) {
                    cardviewRecycler.setVisibility(View.VISIBLE);
                } else {
                    cardviewRecycler.setVisibility(View.GONE);
                }
            }
        });

        MasterControl.setMcontext(MainActivity.this);
        initToolbar();
        armarMenuMoreApps();

        new Handler().postAtTime(new Runnable() {
            @Override
            public void run() {
                transaccionInyeccionLLaves();
            }
        }, 6000);

    }

    private void armarMenuMoreApps() {

        recyclerViewMoreApps = findViewById(R.id.recyclerViewMoreApps);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewMoreApps.setLayoutManager(layoutManager);

        moreApps = new MenusAplicaciones(MainActivity.this);
        List<ModeloApps> appList = moreApps.listadoAplicaciones(APLICACIONES.getSingletonInstance());

        if (!appList.isEmpty()) {
            AdaptadorApps adaptador = new AdaptadorApps(appList, MainActivity.this);
            adaptador.setLayout(R.layout.item_more_apps);
            recyclerViewMoreApps.setAdapter(adaptador);
            adaptador.setOnItemClickListener(onItemClickListener);
        } else {
            imageViewMoreApps.setVisibility(View.INVISIBLE);
        }


    }

    private void transaccionInyeccionLLaves() {
        if (!verificarLlaves()) {
            intentSetting("Debe inyectar las llaves");
        }
    }

    public void intentSetting(String mensaje) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(MainActivity.this, MenusActivity.class);
        intent.putExtra(DefinesBANCARD.MENSAJE_ERROR_INYECCION_LLAVES, mensaje);
        startActivity(intent);
    }

    private boolean verificarLlaves() {
        return DUKPT.checkIPEK() == 0 && threreIsKey(MASTERKEYIDX, "Debe cargar Master Key", MainActivity.this) &&
                threreIsKeyWK(TRACK2KEYIDX, "Debe cargar Work key", MainActivity.this);
    }

    private void initToolbar() {
/*        toolbar = findViewById(R.id.toolbarBancard);
        toolbar.setNavigationIcon(R.drawable.btn_menu);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);*/

        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setVisibility(View.VISIBLE);
        ivClose.setImageDrawable(getDrawable(R.drawable.btn_menu));
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MenusActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MenusActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void modifyFlag() {
        Logger.debug("Modificando Flag");
        readWriteFileMDM.writeFileMDM(ReadWriteFileMDM.IS_TRANSACCION_DEACTIVE);
    }

    private void eliminarUltimoCaracter() {
        int len = builder.length();
        if (len != 0) {
            builder.deleteCharAt(len - 1);
        }
        mostrarEnTextView();
    }

    private void mostrarEnTextView() {
        int len = builder.length();
        if (len == 0) {
            editText.setText("");
            editText.setHint("Codigo");
            modifyFlag();
        } else if (len <= longitudMaxima) {
            editText.setText(builder.toString());
        } else {
            eliminarUltimoCaracter();
        }
    }

    @Override
    public void onBackPressed() {
        //
    }

    private String formatSerial(String serial) {
        int espacio = 5;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < serial.length(); i += espacio) {
            if (i + espacio >= serial.length()) {
                result.append(serial.substring(i));
            } else {
                result.append(serial.substring(i, i + espacio)).append("-");
            }
        }
        return result.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        builder = new StringBuilder();
        builder.setLength(0);
        mostrarEnTextView();
        if (!validadServicioActivoPolaris()) {
            vistaBloqueoPolaris();
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        if (isInit) {

            // DESACTIVA INSTALACION de APP por Polaris Agente, se juega con variable reverso ACTIVATE

            ReadWriteFileMDM.getInstance().writeFileMDM(ReadWriteFileMDM.IS_TRANSACCION_ACTIVE);

            switch (view.getId()) {
                //validar inicializacion del dispositivo.
                case R.id.number0:
                    builder.append("0");
                    break;
                case R.id.number1:
                    builder.append("1");
                    break;
                case R.id.number2:
                    builder.append("2");
                    break;
                case R.id.number3:
                    builder.append("3");
                    break;
                case R.id.number4:
                    builder.append("4");
                    break;
                case R.id.number5:
                    builder.append("5");
                    break;
                case R.id.number6:
                    builder.append("6");
                    break;
                case R.id.number7:
                    builder.append("7");
                    break;
                case R.id.number8:
                    builder.append("8");
                    break;
                case R.id.number9:
                    builder.append("9");
                    break;
                case R.id.number000:
                    builder.append("000");
                    break;
                case R.id.btnClear:
                    builder.setLength(0);
                    break;
                case R.id.numberDelete:
                    eliminarUltimoCaracter();
                    break;
                case R.id.btnOk:
                    validacionTrans();
                    break;
                default:
                    Toast.makeText(this, "" + view.getId(), Toast.LENGTH_SHORT).show();
                    break;
            }
            switch (view.getId()) {
                case R.id.numberDelete:
                case R.id.btnOk:
                    break;
                default:
                    mostrarEnTextView();
                    break;
            }
        } else {
            Toast.makeText(this, requiereInit, Toast.LENGTH_SHORT).show();
        }
    }

    private void validacionTrans() {
        if (builder.length() < longitudMinima)
            UIUtils.toast(this, R.drawable.ic_cobranzas_blanca, "El código debe ser de mínimo 2 dígitos", Toast.LENGTH_SHORT);
        else intentTrans();
    }

    void intentTrans() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(MainActivity.this, MasterControl.class);
        intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[21]);
        intent.putExtra(MasterControl.CODIGO, builder.toString());
        intent.putExtra(MasterControl.CAJAS, false);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
