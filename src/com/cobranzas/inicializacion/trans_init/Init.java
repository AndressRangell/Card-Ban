package com.cobranzas.inicializacion.trans_init;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.PermissionActivity;
import com.cobranzas.actividades.FalloConexionActivity;
import com.cobranzas.actividades.MainActivity;
import com.cobranzas.actividades.StartAppBANCARD;
import com.cobranzas.basedatos.migracion.MigraccionBaseDatos;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.Device;
import com.cobranzas.inicializacion.tools.PolarisUtil;
import com.cobranzas.inicializacion.trans_init.trans.ISO;
import com.cobranzas.inicializacion.trans_init.trans.SendRcvd;
import com.cobranzas.inicializacion.trans_init.trans.UnpackFile;
import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.cobranzas.tools.PermissionStatus;
import com.cobranzas.tools.VerificadorConexion;
import com.cobranzas.transactions.callbacks.waitInitCallback;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.SDKException;
import com.pos.device.beeper.Beeper;
import com.pos.device.config.DevConfig;
import com.wposs.cobranzas.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.FormularioActivity;

import static com.cobranzas.actividades.StartAppBANCARD.isInit;
import static com.cobranzas.actividades.StartAppBANCARD.listadoIps;
import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.cobranzas.defines_bancard.DefinesBANCARD.CAKEY;
import static com.cobranzas.defines_bancard.DefinesBANCARD.ENTRY_POINT;
import static com.cobranzas.defines_bancard.DefinesBANCARD.NAME_FOLDER_CTL_FILES;
import static com.cobranzas.defines_bancard.DefinesBANCARD.PROCESSING;
import static com.cobranzas.defines_bancard.DefinesBANCARD.REVOK;
import static com.cobranzas.defines_bancard.DefinesBANCARD.TERMINAL;
import static com.cobranzas.menus.MenuAction.callBackSeatle;

public class Init extends FormularioActivity {

    public static final int InitTotal = 1;
    public static final int InitParcial = 2;
    public static final String DEFAULT_DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + File.separator + "bancard" + File.separator + "Cobranzas";
    public static String gHashTotal;
    public static waitInitCallback callBackInit;
    public static String gFileName;
    public static String gTID;
    public static String gOffset;
    public static File gFile;
    public static String NAME_DB = "init";
    public static String APLICACIONES = "APLICACIONES";
    public static String CAPKS = "capks";
    public static String CARDS = "CARDS";
    public static String COMERCIOS = "COMERCIOS";
    public static String DEVICE = "DEVICE";
    public static String HOST = "HOST";
    public static String IPS = "IPS";
    public static String PLANES = "PLANES";
    public static String RED = "RED";
    public static String SUCURSAL = "SUCURSAL";
    public static String EMVAPPS = "emvapps";
    public static String emvappsdebug = "emvappsdebug";
    public static String tareas = "tareas";
    private static String nii;
    public boolean isParcial = false;
    public int tipoInit;
    String clase = "Init.java";
    TextView txt;
    TextView tv_title;
    PermissionStatus permissionStatus;
    boolean type = false;
    private String IP;
    private String puerto;
    private int espera;
    private String TID;

    private static String getNameFileCTL(int id) {
        String ret = "";
        switch (id) {
            case 1:
                ret = ENTRY_POINT;
                break;
            case 2:
                ret = PROCESSING;
                break;
            case 3:
                ret = REVOK;
                break;
            case 4:
                ret = TERMINAL;
                break;
            case 5:
                ret = CAKEY;
                break;
        }
        return ret;
    }

    private static String getNameTableById(int id) {
        String ret = "";
        switch (id) {
            case 1:
                ret = APLICACIONES;
                break;
            case 2:
                ret = CAPKS;
                break;
            case 3:
                ret = CARDS;
                break;
            case 4:
                ret = COMERCIOS;
                break;
            case 5:
                ret = DEVICE;
                break;
            case 6:
                ret = HOST;
                break;
            case 7:
                ret = IPS;
                break;
            case 8:
                ret = PLANES;
                break;
            case 9:
                ret = RED;
                break;
            case 10:
                ret = SUCURSAL;
                break;
            case 11:
                ret = EMVAPPS;
                break;
            case 12:
                ret = emvappsdebug;
                break;
            case 13:
                ret = tareas;
                break;

            default:
                break;
        }
        return ret;
    }

    public String getTID() {
        return TID;
    }

    public void setTID() {
        String marca = "NEWPOS";
        String modelo = "";
        String serial = "";
        try {
            String modelo_serial = DevConfig.getSN(); // 4 digitos iniciales del serial corresponden al modelo
            modelo = modelo_serial.substring(0, 4);
            serial = modelo_serial;
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Log.d("Error serial -", e.getMessage());
        }
//        this.TID = marca + "_"  + modelo + "_"  + serial;
        this.TID = serial;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        eliminarCache(getApplicationContext(), clase);
        Logger.logLine(LogType.COMUNICACION, clase, "Eliminando Directorio: " + new File(DEFAULT_DOWNLOAD_PATH).getAbsolutePath());
        eliminarDatos(new File(DEFAULT_DOWNLOAD_PATH), clase);
        Logger.logLine(LogType.COMUNICACION, clase, "Directorio Eliminado >>" + !new File(DEFAULT_DOWNLOAD_PATH).exists());

        permissionStatus = new PermissionStatus(Init.this, this);
        if (!permissionStatus.validatePermissions()) {
            Intent intent = new Intent();
            intent.setClass(Init.this, PermissionActivity.class);
            startActivity(intent);
        } else {
            if (getIntent().hasExtra("typesInitSeconIP")) {
                type = getIntent().getBooleanExtra("typesInitSeconIP", false);
            }
            txt = findViewById(R.id.output);

            isParcial = getIntent().getBooleanExtra("PARCIAL", false);
            tipoInit = isParcial ? InitParcial : InitTotal;

            tv_title = findViewById(R.id.textView_titleToolbar);
            tv_title.setText(Html.fromHtml("<h4> INICIALIZACIÓN POLARIS </h4>"));
            callBackSeatle = null;

            download();
            mostrarSerialvsVersion();
        }
    }

    private void mostrarSerialvsVersion() {
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvSerial = findViewById(R.id.tvSerial);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


    private void init(String tipoIP, String port) {
        setTID();
        gFileName = TID + ".zip";
        gTID = TID;
        gOffset = "0";
        this.IP = tipoIP;
        this.puerto = port;
        this.nii = getResources().getString(R.string.niiConfig);
        this.espera = Integer.parseInt(getResources().getString(R.string.timerConfig));
    }

    private void download() {
        new MigraccionBaseDatos(this, new MigraccionBaseDatos.ProcederMigracion() {
            @Override
            public void rspProcesoTerminadoMigracion(boolean isContinuarIncializacion) {
                new VerificadorConexion(Init.this, new VerificadorConexion.ProcederConexion() {
                    @Override
                    public void rspProcesoExitosoWifi(String mensajeExitoso) {
                        IP = getResources().getString(R.string.ipConfigWifi);
                        puerto = getResources().getString(R.string.portConfig);

                        ISOUtil.showMensaje(mensajeExitoso, Init.this);
                        if (type) {
                            int posicionRed = obtenerPolaris("polarisDNS");
                            if (ChequeoIPs.seleccioneIP(posicionRed) != null &&
                                    ChequeoIPs.seleccioneIP(posicionRed) != null) {

                                IP = ChequeoIPs.seleccioneIP(posicionRed).getIP();
                                puerto = ChequeoIPs.seleccioneIP(posicionRed).getPuerto();
                            }
                        }

                        init(IP, puerto);
                        onlineTrans();
                    }

                    @Override
                    public void rspProcesoExitoso3G(String mensajeExitoso) {
                        ISOUtil.showMensaje(mensajeExitoso, Init.this);
                        IP = getResources().getString(R.string.ipConfig3G);
                        puerto = getResources().getString(R.string.portConfig);

                        if (type) {
                            int posicionRed = obtenerPolaris("polaris3G");
                            if (ChequeoIPs.seleccioneIP(posicionRed) != null &&
                                    ChequeoIPs.seleccioneIP(posicionRed) != null) {

                                IP = ChequeoIPs.seleccioneIP(posicionRed).getIP();
                                puerto = ChequeoIPs.seleccioneIP(posicionRed).getPuerto();
                            }

                        }

                        init(IP, puerto);
                        onlineTrans();
                    }

                    @Override
                    public void rspProcesoFallido(String mensajeError) {
                        //Validar que la bd tenga info -true >main -false > FalloConexionActivity
                        Intent intent = new Intent();
                        String msg;
                        if (PolarisUtil.isInitPolaris(Init.this)) {
                            intent.setClass(Init.this, MainActivity.class);
                            msg = "Pasale!";
                        } else {
                            msg = "No puede pasar!";
                            intent.setClass(Init.this, FalloConexionActivity.class);
                        }
                        System.out.println(msg);
                        startActivity(intent);
                    }
                }).execute();
            }
        }).execute();

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


    private int obtenerPolaris(String tipoConexion) {
        int posicion = -1;
        posicion = ChequeoIPs.getPosicionIps(tipoConexion);
        if (posicion != -1) {
            com.cobranzas.inicializacion.configuracioncomercio.IPS ip = ChequeoIPs.seleccioneIP(posicion);
            if (!ip.getIP().equals(TMConfig.getInstance().getIp()) || !ip.getPuerto().equals(TMConfig.getInstance().getPort())) {
                return posicion;
            }
        }
        return posicion;
    }

    private int onlineTrans() {

        final byte[] dataVacia = new byte[]{};

        SendRcvd sendTrans = new SendRcvd(IP, Integer.parseInt(puerto), espera, Init.this);

        sendTrans.setFileName(gFileName);
        sendTrans.setNii(nii);
        sendTrans.setTid(gTID);
        sendTrans.setOffset(gOffset);
        sendTrans.setPathDefault(DEFAULT_DOWNLOAD_PATH);
        sendTrans.setTramaQueEnvia(tipoInit);

        if (!type) {
            sendTrans.setWithMensaje(false);
        }

        sendTrans.callbackResponse(new SendRcvd.TcpCallback() {

            @Override
            public void rspHost(byte[] rxBuf, String resultOk) {
                if (rxBuf == null || Arrays.equals(rxBuf, dataVacia)) {
                    showMensaje("ERROR, INICIALIZACIÓN FALLIDA", false);
                    finalizarActividad();
                    return;
                }

                if (!resultOk.equals("OK")) {
                    if (!resultOk.contains("ERROR DESCONOCIDO")) {
                        UIUtils.toast(Init.this, R.drawable.ic_cobranzas_blanca, resultOk, Toast.LENGTH_SHORT);
                    }
                    //checkAutoInit();
                    finalizarActividad();
                    return;
                }

                ISO rspIso = new ISO(rxBuf, ISO.lenghtNotInclude, ISO.TpduInclude);

                if (rspIso.GetField(ISO.field_03_PROCESSING_CODE).equals("310100")) {
                    showMensaje(rspIso.GetField(ISO.field_60_RESERVED_PRIVATE), false);

                    callBackInit = null;

                    txt.setText(R.string.label_init_process);

                    if (processFile(gFileName)) {

                        callBackInit = new waitInitCallback() {
                            @Override
                            public void getRspInitCallback(int status) {
                                try {
                                    //Inyectar WorkingKey
                                    /*if(!inyectarWorkingKey()){
                                        isInit = false;
                                        UIUtils.toast(Init.this, R.drawable.ic_cobranzas_blanca, "INICIALIZACION FALLIDA", Toast.LENGTH_SHORT);
                                        finish();
                                    }else {*/

                                    Logger.logLine(LogType.FLUJO, clase, "********|Verificar Inicializacion en Init |********");
                                    if ((isInit = PolarisUtil.isInitPolaris(Init.this))) {
                                        if (Device.selectT_conf(Init.this)) {
                                            if (tablaComercios.selectComercios(Init.this)) {
                                                if (StartAppBANCARD.tablaHost.selectHostConfig(Init.this)) {
                                                    if (cargarListadoIps()) {
                                                        Beeper.getInstance().beep();
                                                        showMensaje("INICIALIZACIÓN EXITOSA", true);

                                                        TMConfig.getInstance().setIp(IP);
                                                        TMConfig.getInstance().setPort(puerto);
                                                        finalizarActividad();
                                                    }

                                                }
                                            }
                                        }
                                    } else {
                                        showMensaje("INICIALIZACIÓN FALLIDA", false);
                                        finalizarActividad();
                                        //UIUtils.backToMainMenu(Init.this);
                                    }
                                    //}

                                } catch (SDKException e) {
                                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                                }
                            }
                        };
                    } else {
                        finalizarActividad();
                    }
                } else if (rspIso.GetField(ISO.field_03_PROCESSING_CODE).equals("960080")) {
                    if (processFile(gFileName)) {
                        callBackInit = null;
                        callBackInit = new waitInitCallback() {
                            @Override
                            public void getRspInitCallback(int status) {
                                finalizarActividad();
                            }
                        };
                    }
                }
            }
        });

        sendTrans.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return 0;
    }

    private boolean cargarListadoIps() {
        listadoIps = ChequeoIPs.selectIP(Init.this);
        if (listadoIps == null) {
            listadoIps = new ArrayList<>();
            listadoIps.clear();
            isInit = false;
            showMensaje("Error al leer tabla, Por favor Inicialice nuevamente", false);
            finalizarActividad();
            return false;
        } else if (listadoIps.isEmpty()) {
            isInit = false;
            showMensaje("Error al leer tabla, Por favor Inicialice nuevamente", false);
            finalizarActividad();
            return false;
        }
        return true;
    }


    private void showMensaje(String mensaje, boolean isok) {
        if (isok) {
            StartAppBANCARD.initSeconIp = false;
        }
        if (!type) {
            UIUtils.toast(Init.this, R.drawable.ic_cobranzas_blanca, mensaje, Toast.LENGTH_SHORT);
        }
    }

    private void finalizarActividad() {
        StartAppBANCARD.intentoInicializacion = true;
        startActivity(new Intent(this, StartAppBANCARD.class));
        finish();
    }

    public boolean processFile(String aFileName) {
        int READ_BLOCK_SIZE = 65000 * 2;
        File file = new File(DEFAULT_DOWNLOAD_PATH + File.separator + gFileName + "T");
        if (!file.exists()) {
            file = new File(DEFAULT_DOWNLOAD_PATH + File.separator + gFileName);
        }
        if (file.exists()) {
            file.renameTo(new File(DEFAULT_DOWNLOAD_PATH + File.separator + gFileName));
            if (gFileName.endsWith(".txt")) {
                try {
                    FileInputStream fileIn = new FileInputStream(new File(DEFAULT_DOWNLOAD_PATH + File.separator + gFileName));
//                            this.ctx.openFileInput(DEFAULT_DOWNLOAD_PATH +  File.separator+ gFileName);
                    InputStreamReader InputRead = new InputStreamReader(fileIn, StandardCharsets.ISO_8859_1);

                    char[] inputBuffer = new char[READ_BLOCK_SIZE];
                    String s = "";
                    int charRead;

                    while ((charRead = InputRead.read(inputBuffer)) > 0) {
                        // char to string conversion
                        String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                        s += readstring;
                    }
                    InputRead.close();
                    String[] fieldSplit = s.split(";");
                    dbHelper db = new dbHelper(getApplicationContext(), "init", null, 1);
                    db.openDb("init");
                    for (String str : fieldSplit) {
                        if (!str.equals("\n")) {
                            if (str.contains("DROP TABLE")) {
                                try {
                                    Log.v("Init", "DROP query " + str);
                                    db.execSql(str);
                                } catch (Exception e) {
                                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                                }
                            } else {
                                Log.v("Init", "Query " + str);
                                db.execSql(str);
                            }
                        }
                    }

                    db.closeDb();
                    String rename = DEFAULT_DOWNLOAD_PATH + File.separator + gFileName + "T";
                    new File(DEFAULT_DOWNLOAD_PATH + File.separator + gFileName).renameTo(new File(rename));

                    //file.delete();//luego de creada la tabla en la base de datos se eliminan los archivos descargados

                } catch (Exception e) {
                    UIUtils.toast(Init.this, R.drawable.ic_cobranzas_blanca, "INICIALIZACIÓN FALLIDA",
                            Toast.LENGTH_SHORT);
                    //Tools.toast("Inicializacion Fallo");
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    new File(DEFAULT_DOWNLOAD_PATH + File.separator + gFileName).delete();
                    return false;
                }

                //Tools.saveHash(gHashTotal, getApplicationContext()); //guarda hash
            }
            if (gFileName.endsWith(".zip")) {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            unzip(gFileName, DEFAULT_DOWNLOAD_PATH + File.separator, Init.this);
                        }
                    });
                } catch (Exception e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    Logger.logLine(LogType.FLUJO, clase, e.getStackTrace());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Lee los archivos de configuracion de CTL y los copia en una ruta interna dentro del
     * package de la aplicacion (acceso solo desde la app), posterior a esto se eliminan del
     * SD
     *
     * @param aFileName
     * @param aFileWithOutExt
     * @return
     */
    public boolean processFilesCTL(String aFileName, String aFileWithOutExt) {
        File fileLocation = new File(DEFAULT_DOWNLOAD_PATH + File.separator + aFileName);

        ContextWrapper cw = new ContextWrapper(Init.this);
        File directory = cw.getDir(NAME_FOLDER_CTL_FILES, Context.MODE_PRIVATE);
        File file = new File(directory + File.separator + aFileWithOutExt);

        if (fileLocation.exists()) {

            if (gFileName.endsWith(".bin") || gFileName.endsWith(".BIN")) {
                try {
                    FileInputStream InputRead = new FileInputStream(fileLocation);
                    FileOutputStream outWrite = new FileOutputStream(file);

                    byte[] inputBuffer = new byte[1024];
                    int charRead;

                    while ((charRead = InputRead.read(inputBuffer)) > 0) {
                        outWrite.write(inputBuffer, 0, charRead);
                        outWrite.flush();
                    }
                    InputRead.close();
                    outWrite.close();

                    //fileLocation.delete();

                } catch (NullPointerException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                } catch (Exception e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
            }
        }
        return true;
    }

    public void unzip(final String zipFile, String location, Context context) {

        UnpackFile unpackFile;
        boolean ponerLaT = true;

        unpackFile = new UnpackFile(context, zipFile, location, ponerLaT, false, new UnpackFile.FileCallback() {
            @Override
            public boolean rspUnpack(boolean okUnpack) {

                if (okUnpack) {
                    String nameAux;
                    String nameTbl;

                    int i;
                    new File(DEFAULT_DOWNLOAD_PATH + File.separator + gFileName).delete();

                    nameAux = zipFile.replace(".zip", "");
                    i = 1;
                    nameTbl = getNameTableById(i);
                    while (!nameTbl.equals("")) {
                        gFileName = nameAux + "_" + getNameTableById(i) + ".txt";
                        processFile(gFileName);
                        i++;
                        nameTbl = getNameTableById(i);
                    }

                    i = 1;
                    nameTbl = getNameFileCTL(i);
                    while (!nameTbl.equals("")) {
                        gFileName = nameAux + "_" + getNameFileCTL(i) + ".bin";
                        processFilesCTL(gFileName, getNameFileCTL(i));
                        i++;
                        nameTbl = getNameFileCTL(i);
                    }

                    //Tools.toast("Inicializacion finalizada!!!");
                    if (callBackInit != null)
                        callBackInit.getRspInitCallback(0);
                    return true;
                } else {
                    return false;
                }
            }
        });
        unpackFile.execute();
    }
}
