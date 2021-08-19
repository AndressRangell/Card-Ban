package com.cobranzas.inicializacion.tools;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.cobranzas.actividades.StartAppBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;
import com.cobranzas.inicializacion.configuracioncomercio.CARDS;
import com.cobranzas.inicializacion.configuracioncomercio.COMERCIOS;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.Device;
import com.cobranzas.inicializacion.configuracioncomercio.HOST;
import com.cobranzas.inicializacion.configuracioncomercio.IPS;
import com.cobranzas.inicializacion.configuracioncomercio.Red;
import com.cobranzas.inicializacion.configuracioncomercio.Tareas;
import com.cobranzas.inicializacion.configuracioncomercio.TransActive;
import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.cobranzas.setting.ListSetting;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.util.ArrayList;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.actividades.StartAppBANCARD.listadoIps;
import static com.cobranzas.actividades.StartAppBANCARD.tablaCards;
import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.cobranzas.actividades.StartAppBANCARD.tablaDevice;
import static com.cobranzas.actividades.StartAppBANCARD.tablaHost;
import static com.cobranzas.actividades.StartAppBANCARD.tablaIp;
import static com.cobranzas.actividades.StartAppBANCARD.transActive;
import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class PolarisUtil {

    static String clase = "PolarisUtil.java";

    public PolarisUtil() {
    }

    /**
     * isInitPolaris check Stis Table
     *
     * @param context - Activity's context
     * @return true or false
     * @author Francisco Mahecha
     * @version 1.0
     */
    public static boolean isInitPolaris(Context context) {
        int countRow;
        int counterTables = 1;

        boolean aplicaciones = false;
        boolean capks = false;
        boolean cards = false;
        boolean comercios = false;
        boolean device = false;
        boolean host = false;
        boolean ips = false;
        boolean red = false;
        boolean sucursal = false;
        boolean emvApps = false;
        boolean emvappsdebug = false;
        boolean tareas = false;

        //Read packages
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);


        String sql = consultaSQL();

        try {

            System.out.println("SQL ---- " + sql);
            Cursor cursor = databaseAccess.rawQuery(sql);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                countRow = cursor.getInt(0);

                switch (counterTables) {
                    case 1:
                        aplicaciones = verificacionTabla(context, countRow, "aplicaciones");
                        break;
                    case 2:
                        capks = verificacionTabla(context, countRow, "capks");
                        break;
                    case 3:
                        cards = verificacionTabla(context, countRow, "cards");
                        break;
                    case 4:
                        comercios = verificacionTabla(context, countRow, "comercios");
                        break;
                    case 5:
                        device = verificacionTabla(context, countRow, "device");
                        break;
                    case 6:
                        host = verificacionTabla(context, countRow, "host");
                        break;
                    case 7:
                        ips = verificacionTabla(context, countRow, "ips");
                        break;
                    case 8:
                        red = verificacionTabla(context, countRow, "red");
                        break;
                    case 9:
                        sucursal = verificacionTabla(context, countRow, "sucursal");
                        break;
                    case 10:
                        emvApps = verificacionTabla(context, countRow, "emvApps");
                        break;
                    case 11:
                        if (countRow == 0) {
                            tareas = false;
                        } else {
                            tareas = true;
                        }
                        break;
                    case 12:
                        emvappsdebug = true;
                        break;
                    default:
                        break;
                }
                counterTables = counterTables + 1;
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        databaseAccess.closeDb();

        System.out.println("counterTables " + counterTables);
        System.out.println("aplicaciones " + aplicaciones);
        System.out.println("capks " + capks);
        System.out.println("cards " + cards);
        System.out.println("comercios " + comercios);
        System.out.println("device " + device);
        System.out.println("host " + host);
        System.out.println("ips " + ips);
        System.out.println("red " + red);
        System.out.println("sucursal " + sucursal);
        System.out.println("emvApps " + emvApps);
        System.out.println("emvappsdebug " + emvappsdebug);
        System.out.println("tareas " + tareas);

        return counterTables == 12 &&
                aplicaciones &&
                capks &&
                cards &&
                comercios &&
                device &&
                host &&
                ips &&
                red &&
                sucursal &&
                emvApps;
    }

    public static boolean verificacionTabla(Context context, int countRow, String tabla) {
        if (countRow == 0) {
            showMensaje(context, "Tabla " + tabla + " vacía ");
            return false;
        } else {
            return true;
        }
    }

    private static String consultaSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("select count (*) from APLICACIONES ");
        sql.append("union all ");
        sql.append("select count (*) from CAPKS ");
        sql.append("union all ");
        sql.append("select  count (*) from CARDS ");
        sql.append("union all ");
        sql.append("select  count (*) from COMERCIOS ");
        sql.append("union all ");
        sql.append("select  count (*) from DEVICE ");
        sql.append("union all ");
        sql.append("select  count (*) from HOST ");
        sql.append("union all ");
        sql.append("select  count (*) from IPS ");
        sql.append("union all ");
        sql.append("select count (*) from RED ");
        sql.append("union all ");
        sql.append("select  count (*) from SUCURSAL ");
        sql.append("union all ");
        sql.append("select  count (*) from EMVAPPS ");
        sql.append("union all ");
        sql.append("select  count (*) from tareas");

        return sql.toString();
    }

    private static void showMensaje(Context context, String mensaje) {
        if (context != null)
            UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, mensaje, Toast.LENGTH_LONG);
        else
            Log.d("Info", "showMensaje: " + "context == null "+mensaje);
    }

    /**
     * Mostrar en pantalla si falla si falla query de lectura de alguna
     * tabla de la BD
     *
     * @param nameTable
     */
    private void showErrMsg(String nameTable, Context context) {
        StartAppBANCARD.isInit = false;
        UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, "Error al leer tabla ," + nameTable + "\n Por favor Inicialice nuevamente", Toast.LENGTH_LONG);
    }

    /**
     * Permite tener toda la informacion pertinente al comercio antes de
     * mostrar el menu principal
     *
     * @param context
     */
    public void leerBaseDatos(Context context) {
        if ((StartAppBANCARD.isInit = PolarisUtil.isInitPolaris(context))) {
            if (Device.selectT_conf(context)) {
                if (tablaComercios.selectComercios(context)) {
                    if (tablaHost.selectHostConfig(context)) {
                        if (!Tareas.getInstance(false).inicializandoComponentes(context)) {
                        }
                        if (!Red.getInstance(false).inicializandoComponentes(context)) {
                            showErrMsg("RED", context);
                        }
                        if (!APLICACIONES.checksAppsActive(context)) {
                            showErrMsg(APLICACIONES.NAME_TABLE, context);
                        }
                        if ((listadoIps = ChequeoIPs.selectIP(context)) == null) {
                            showErrMsg(IPS.NAME_TABLE, context);
                        }
                    }
                }
            }
        }
    }

    /**
     * Instancia todos los objetos necesarios para el manejo de la
     * inicializacion del PSTIS
     */
    public void initObjetPSTIS(Context context) {


        APLICACIONES.setAplicacionesNull();
        ListSetting.setModelSettingsNull();

        //----------- Init Bancard-----------
        if (tablaHost == null) {
            tablaHost = HOST.getSingletonInstance();
        }

        if (listadoIps == null) {
            listadoIps = new ArrayList<>();
        }

        if (tablaIp == null) {
            tablaIp = new IPS(context);
        }

        if (tablaCards == null) {
            tablaCards = CARDS.getSingletonInstance(context);
        }

        if (tablaDevice == null) {
            tablaDevice = Device.getSingletonInstance(context);
        }

        if (tablaComercios == null) {
            tablaComercios = COMERCIOS.getSingletonInstance(context);
        }

        APLICACIONES.getSingletonInstance();

        Red.getInstance(true);

        Tareas.getInstance(true);

        if (transActive == null) {
            transActive = new TransActive();
        }

        //--------- limpiar datos----------
        if (tablaDevice != null) {
            Device.clearTConf();
        }

        if (tablaComercios != null) {
            tablaComercios.clearCOMERCIOS();
        }


        if (tablaHost != null) {
            tablaHost.clearHostConfig();
        }

        if (listadoIps != null) {
            listadoIps.clear();
        }
    }
}
