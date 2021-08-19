package com.cobranzas.inicializacion.configuracioncomercio;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.actividades.StartAppBANCARD.tablaIp;
import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class IPS {

    private String idIp; // El nombre será tratado en ID_IP
    private String ip;
    private String puerto;
    private boolean tls;
    private boolean autenticarCliente;
    private String claveWifi;
    private static final String CLASE = "IPS.java";

    private static final String NAME_ID_IP = "IP_ID";
    private static final String NAME_IP = "IP";
    private static final String NAME_PUERTO = "PUERTO";
    private static final String NAME_TLS = "TLS";
    private static final String NAME_AUTENTICAR_CLIENTE = "AUTENTICAR_CLIENTE";
    private static final String NAME_CLAVE_WIFI = "ip_clave_wifi";


    public static final String NAME_TABLE = "IPS";

    private Context context;

    public IPS(Context context) {
        this.context = context;
    }

    protected static String[] fields = new String[]{
            NAME_ID_IP,
            NAME_IP,
            NAME_PUERTO,
            NAME_TLS,
            NAME_AUTENTICAR_CLIENTE,
            NAME_CLAVE_WIFI
    };

    public static String[] fieldsEdit = new String[]{
            NAME_IP,
            NAME_PUERTO,
            NAME_TLS
    };

    public static IPS getSingletonInstance(Context context) {
        if (tablaIp == null) {
            tablaIp = new IPS(context);
        } else {
            Log.d("IPS", "No se puede crear otro objeto, ya existe");
        }
        return tablaIp;
    }

    public void setIP(String column, String value) {
        StringBuilder stringBuilder = new StringBuilder();

        switch (column) {
            case NAME_ID_IP:
                stringBuilder.append(" ID IP: "+value+"\n");
                setIdIp(value);
                break;
            case NAME_IP:
                stringBuilder.append(" NOMBRE IP: "+value+"\n");
                setIP(value);
                break;
            case NAME_PUERTO:
                stringBuilder.append(" PUERTO : "+value+"\n");
                setPuerto(value);
                break;
            case NAME_TLS:
                stringBuilder.append(" TLS : "+value+"\n");
                setTls(value);
                break;
            case NAME_AUTENTICAR_CLIENTE:
                stringBuilder.append(" AUTENTICACION CLIENTE : "+value+"\n");
                setAutenticarCliente(value);
                break;
            case NAME_CLAVE_WIFI:
                stringBuilder.append(" CLAVE WIFI : "+value+"\n");
                setClaveWifi(value);
                break;
            default:
                break;
        }
        Log.d(CLASE, "setIP: "+stringBuilder.toString());
    }

    public void clearIP() {
        for (String s : IPS.fields) {
            setIP(s, "");
        }
    }

    public boolean updateSelectIps(String id, String[] rowToModificate, String[] args, Context context) {
        boolean ok = false;

        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        StringBuilder sql = new StringBuilder();

        sql.append(" UPDATE ");
        sql.append(NAME_TABLE);
        sql.append(" set ");
        int idx;
        idx = 0;
        for (String s : IPS.fields) {
            if (s.equals(rowToModificate[idx])) {
                sql.append(rowToModificate[idx]);
                sql.append(" = ");
                sql.append("'");
                sql.append(args[idx]);
                sql.append("'");
                if (idx < (rowToModificate.length - 1)) {
                    idx++;
                    sql.append(",");
                }
            }
        }
        if (!id.isEmpty()) {

            sql.append(" where trim(IP_ID)= ");
            sql.append("'");
            sql.append(id);
            sql.append("'");
        }

        sql.append(";");

        try {
            databaseAccess.execSql(sql.toString());
            ok = true;
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, CLASE, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, CLASE, e.getStackTrace());
            Logger.debug(e.getMessage());
        }

        databaseAccess.close();
        return ok;
    }

    public List<IPS> getListIPs(Context context) {
        List<IPS> result = new ArrayList<>();
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        StringBuilder sql = new StringBuilder();

        sql.append("select ");
        int counter = 1;
        for (String s : IPS.fields) {
            sql.append(s);
            if (counter++ < IPS.fields.length) {
                sql.append(",");
            }
        }
        sql.append(" from IP;");
        Logger.debug(sql.toString());

        try {

            Cursor cursor = databaseAccess.rawQuery(sql.toString());
            cursor.moveToFirst();
            int indexColumn;
            IPS ips;
            while (!cursor.isAfterLast()) {
                ips = new IPS(context);
                indexColumn = 0;
                for (String s : IPS.fields) {
                    ips.setIP(s, cursor.getString(indexColumn++).trim());
                }
                cursor.moveToNext();
                result.add(ips);
            }
            cursor.close();

        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, CLASE, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, CLASE, e.getStackTrace());
            Logger.debug(e.getMessage());
        }
        databaseAccess.closeDb();
        return result;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            Logger.logLine(LogType.EXCEPTION, CLASE, ignored.getMessage());
            Logger.logLine(LogType.EXCEPTION, CLASE, ignored.getStackTrace());
        }
        return "";
    }

    public String getIdIp() {
        return idIp;
    }

    public void setIdIp(String idIp) {
        this.idIp = idIp;
    }

    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(String tls) {
        if (!tls.isEmpty()) {
            if (tls.equals("true") || tls.equals("false")) {
                this.tls = Boolean.parseBoolean(tls);
            } else {
                showMensaje("Error en la obtención de las TLS");
                this.tls = false;
            }
        }
    }

    public boolean isAutenticarCliente() {
        return autenticarCliente;
    }

    public void setAutenticarCliente(String autenticarCliente) {
        if (!autenticarCliente.isEmpty()) {
            if ((autenticarCliente.equals("true") || autenticarCliente.equals("false"))) {
                this.autenticarCliente = Boolean.parseBoolean(autenticarCliente);
            } else {
                showMensaje("Error en la autenticación cliente");
                this.autenticarCliente = false;
            }
        } else {
            this.autenticarCliente = false;
        }
    }

    public String getClaveWifi() {
        return claveWifi;
    }

    public void setClaveWifi(String claveWifi) {
        this.claveWifi = claveWifi;
    }

    private void showMensaje(String mensaje) {
        if (context != null)
            UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, mensaje, Toast.LENGTH_LONG);
        else
            Log.d("Info", "showMensaje: " + "context == null "+mensaje);
    }
}
