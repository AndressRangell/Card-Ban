package com.cobranzas.actividades;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.CARDS;
import com.cobranzas.inicializacion.configuracioncomercio.COMERCIOS;
import com.cobranzas.inicializacion.configuracioncomercio.Device;
import com.cobranzas.inicializacion.configuracioncomercio.HOST;
import com.cobranzas.inicializacion.configuracioncomercio.IPS;
import com.cobranzas.inicializacion.configuracioncomercio.PLANES;
import com.cobranzas.inicializacion.configuracioncomercio.TRANSACCIONES;
import com.cobranzas.inicializacion.configuracioncomercio.TransActive;
import com.cobranzas.inicializacion.tools.PolarisUtil;
import com.cobranzas.inicializacion.trans_init.Init;
import com.cobranzas.polaris_validation.ReadWriteFileMDM;
import com.cobranzas.tools.BatteryStatus;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.utils.PAYUtils;

import java.io.File;
import java.util.ArrayList;

import cn.desert.newpos.payui.base.PayApplication;


public class StartAppBANCARD extends AppCompatActivity {

    public static final String CERT = "CERTIFICACION   ";
    public static String VERSION = "6.6";
    public static COMERCIOS tablaComercios = null;
    public static Device tablaDevice = null;
    public static HOST tablaHost = null;
    public static IPS tablaIp = null;
    public static CARDS tablaCards = null;
    public static TRANSACCIONES tablaTransacciones = null;
    public static boolean isInit = false;
    public static boolean MODE_KIOSK = false;
    public static ArrayList<TRANSACCIONES> listadoTransacciones = null;
    public static ArrayList<IPS> listadoIps = null;
    public static ArrayList<PLANES> listadoPlanes = null;
    public static PolarisUtil polarisUtil = null;
    public static TransActive transActive;
    public static boolean intentoInicializacion = false;
    public static boolean initSeconIp = true;
    public static ReadWriteFileMDM readWriteFileMDM = null;
    //CREATEDATABASE = true -> Se usa la base de datos que se crea localmente
    //CREATEDATABASE = false -> Se usa la base de datos que se importo
    String clase = "StartAppBANCARD.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {//check system version ,if it is Android 5, load the so file
                String path = getFilesDir().getPath() + "/libPlatform.so";
                File file = new File(path);
                boolean fileExist = file.exists();
                Log.e("TAGS ", "File " + path + "\n" + fileExist);
                if (!fileExist) {
                    Log.e("StarpApp", "copy assets file to data");
                    PAYUtils.copyAssetsToData(getApplicationContext(), "libPlatform.so");  // copy the so file from folder assets to data folder
                }
                System.load(path);
            }
        } catch (SecurityException | UnsatisfiedLinkError | NullPointerException e) {
            Logger.logLine(LogType.EXCEPTION, "Error al cargar la libreria" + e.getMessage());
            Log.e(clase, "Error al cargar la libreria" + e.getMessage());
            e.printStackTrace();
        }

        try {
            readWriteFileMDM = ReadWriteFileMDM.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        initSDK();

        this.registerReceiver(BatteryStatus.getInstance(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        initApp();
    }

    public void kiosk() {
        //kioske mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && MODE_KIOSK) {
            startLockTask();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            this.unregisterReceiver(BatteryStatus.getInstance());
        } catch (final Exception exception) {
            exception.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, exception.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, exception.getStackTrace());
            // The receiver was not registered.
            // There is nothing to do in that case.
            // Everything is fine.
        }

    }

    /**
     * inicializa el sdk
     */
    private void initSDK() {
        PaySdk.getInstance().setActivity(this);
        PayApplication.getInstance().addActivity(this);
        PayApplication.getInstance().setRunned();
    }


    /**
     * Inicio de la app
     */
    private void initApp() {

        polarisUtil = new PolarisUtil();
        polarisUtil.initObjetPSTIS(StartAppBANCARD.this);
        polarisUtil.leerBaseDatos(StartAppBANCARD.this);

        Logger.debug("********|Verificar Trans Activas|********");
        Logger.debug("venta: " + transActive.isVenta());
        Logger.debug("venta zimple: " + transActive.isVentaZimple());
        Logger.debug("venta cashback: " + transActive.isVentaCashBack());
        Logger.debug("venta minutos: " + transActive.isVentaMinutos());
        Logger.debug("*********+*********************");

        if (!isInit) {
            if (!intentoInicializacion) {
                Logger.debug("intentoInicializacion ++++ " + intentoInicializacion + " isInit ++++  " + isInit);
                Intent intent = new Intent();
                intent.setClass(StartAppBANCARD.this, Init.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setClass(StartAppBANCARD.this, FalloConexionActivity.class);
                startActivity(intent);
            }
            return;

        }

        if (intentoInicializacion) {
            Logger.debug("INFORMACION ------------  ");
            Intent intent = new Intent();
            intent.setClass(StartAppBANCARD.this, MainActivity.class);
            intent.putExtra(DefinesBANCARD.habilitacionCasteoAplicacion, true);
            intent.putExtra(DefinesBANCARD.fechaInicializacion, true);
            if (initSeconIp) {
                intent.putExtra("typesInitSeconIP", true);
                intent.setClass(StartAppBANCARD.this, Init.class);
                initSeconIp = false;
            }
            startActivity(intent);
        } else {
            Logger.debug("intentoInicializacion2 ++++ " + intentoInicializacion + " isInit2 ++++  " + isInit);
            Intent intent = new Intent();
            intent.setClass(StartAppBANCARD.this, Init.class);
            startActivity(intent);
        }
    }


    @Override
    public void onBackPressed() {
        //
    }
}
