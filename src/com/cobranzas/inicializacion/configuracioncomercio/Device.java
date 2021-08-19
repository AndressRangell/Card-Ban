package com.cobranzas.inicializacion.configuracioncomercio;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.actividades.StartAppBANCARD.tablaDevice;
import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class Device {

    private static final String NAME_TABLE = "DEVICE";
    private static Context context;
    private static String deviceIdentifier;
    private static String merchantID;
    private static String deviceDescription;
    private static boolean cajaPOS;
    private static String numeroCajas;
    private static String numSerial;
    private static String surcursalID;
    private static String prioridad;
    private static String prefil;
    private static String modelo;
    private static String estado;
    private static String versionSoftware;
    private static String fechaUltimoEcho;
    private static String fechaUltimaTransaccion;
    private static String fechaUltimoCierre;
    private static String grupo;
    private static String fechaAlta;
    private static String fechaUltimaActualizacion;
    private static String usuarioUltimoActualizacion;
    private static String inicializacionMsg;
    private static String cargaLlavesMsg;
    private static boolean conexion;
    private static String[] fields = new String[]{
            "device_identifier",
            "merchant_id",
            "device_description",
            "CAJA_POS",
            "NUMERO_CAJA",
            "NUM_SERIAL",
            "sucursal_id",
            "PRIORIDAD",
            "PERFIL",
            "MODELO",
            "ESTADO",
            "VERSION_SOFTWARE",
            "FECHA_ULTIMO_ECHO",
            "FECHA_ULTIMA_TRANSACCION",
            "FECHA_ULTIMO_CIERRE",
            "GRUPO",
            "FECHA_ALTA",
            "FECHA_ULTIMA_ACTUALIZACION",
            "USUARIO_ULTIMA_ACTUALIZACION",
            "inicializacionMsg",
            "cargaLlavesMsg",
            "CONEXION"
    };

    public Device(Context context) {
        this.context = context;
    }


    private static void setTConf(String column, String value) {
        switch (column) {
            case "device_identifier":
                setDeviceIdentifier(value);
                break;
            case "merchant_id":
                setMerchantID(value);
                break;
            case "device_description":
                setDeviceDescription(value);
                break;
            case "CAJA_POS":
                setCajaPOS(value);
                break;
            case "NUMERO_CAJA":
                setNumeroCajas(value);
                break;
            case "NUM_SERIAL":
                setNumSerial(value);
                break;
            case "sucursal_id":
                setSurcursalID(value);
                break;
            case "PRIORIDAD":
                setPrioridad(value);
                break;
            case "PERFIL":
                setPrefil(value);
                break;
            case "MODELO":
                setModelo(value);
                break;
            case "ESTADO":
                setEstado(value);
                break;
            case "VERSION_SOFTWARE":
                setVersionSoftware(value);
                break;
            case "FECHA_ULTIMO_ECHO":
                setFechaUltimoEcho(value);
                break;
            case "FECHA_ULTIMA_TRANSACCION":
                setFechaUltimaTransaccion(value);
                break;
            case "FECHA_ULTIMO_CIERRE":
                setFechaUltimoCierre(value);
                break;
            case "GRUPO":
                setGrupo(value);
                break;
            case "FECHA_ALTA":
                setFechaAlta(value);
                break;
            case "FECHA_ULTIMA_ACTUALIZACION":
                setFechaUltimaActualizacion(value);
                break;
            case "USUARIO_ULTIMA_ACTUALIZACION":
                setUsuarioUltimoActualizacion(value);
                break;
            case "inicializacionMsg":
                setInicializacionMsg(value);
                break;
            case "cargaLlavesMsg":
                setCargaLlavesMsg(value);
                break;
            case "CONEXION":
                setConexion(value);
                break;
            default:
                break;
        }
    }

    public static void clearTConf() {
        for (String s : fields) {
            setTConf(s, "");
        }
    }

    public static Device getSingletonInstance(Context context) {
        if (tablaDevice == null) {
            tablaDevice = new Device(context);
        } else {
            Log.d("Device", "No se puede crear otro objeto, ya existe");
        }
        return tablaDevice;
    }

    public static boolean selectT_conf(Context context) {
        //Read packages
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        String sql = consultaSQL();

        try {
            Cursor cursor = databaseAccess.rawQuery(sql);
            cursor.moveToFirst();
            int indexColumn;
            while (!cursor.isAfterLast()) {
                clearTConf();
                indexColumn = 0;
                for (String s : fields) {
                    setTConf(s, cursor.getString(indexColumn++).trim());
                }
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, "Device.java", e.getMessage());
            Logger.logLine(LogType.EXCEPTION, "Device.java", e.getStackTrace());
            Logger.debug(e.getMessage());
            return false;
        }
        databaseAccess.closeDb();
        return true;
    }

    private static String consultaSQL() {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        int counter = 1;
        for (String s : fields) {
            sql.append(s);
            if (counter++ < fields.length) {
                sql.append(",");
            }
        }
        sql.append(" FROM ");
        sql.append(NAME_TABLE).append(";");
        return sql.toString();
    }

    public static String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public static void setDeviceIdentifier(String deviceIdentifier) {
        Device.deviceIdentifier = deviceIdentifier;
    }

    public static String getMerchantID() {
        return merchantID;
    }

    public static void setMerchantID(String merchantID) {
        Device.merchantID = merchantID;
    }

    public static String getDeviceDescription() {
        return deviceDescription;
    }

    public static void setDeviceDescription(String deviceDescription) {
        Device.deviceDescription = deviceDescription;
    }

    public static boolean getCajaPOS() {
        return cajaPOS;
    }

    public static void setCajaPOS(String cajaPOS) {

        if (!cajaPOS.isEmpty()) {
            Device.cajaPOS = ISOUtil.validarBooleanPolaris(cajaPOS, "Caja POS", context);
        }
    }

    public static String getNumeroCajas() {
        return numeroCajas;
    }

    public static void setNumeroCajas(String numeroCajas) {
        Device.numeroCajas = numeroCajas;
    }

    public static String getNumSerial() {
        return numSerial;
    }

    public static void setNumSerial(String numSerial) {
        Device.numSerial = numSerial;
    }

    public static String getSurcursalID() {
        return surcursalID;
    }

    public static void setSurcursalID(String surcursalID) {
        Device.surcursalID = surcursalID;
    }

    public static String getPrioridad() {
        return prioridad;
    }

    public static void setPrioridad(String prioridad) {
        Device.prioridad = prioridad;
    }

    public static String getPrefil() {
        return prefil;
    }

    public static void setPrefil(String prefil) {
        Device.prefil = prefil;
    }

    public static String getModelo() {
        return modelo;
    }

    public static void setModelo(String modelo) {
        Device.modelo = modelo;
    }

    public static String getEstado() {
        return estado;
    }

    public static void setEstado(String estado) {
        Device.estado = estado;
    }

    public static String getVersionSoftware() {
        return versionSoftware;
    }

    public static void setVersionSoftware(String versionSoftware) {
        Device.versionSoftware = versionSoftware;
    }

    public static String getFechaUltimoEcho() {
        return fechaUltimoEcho;
    }

    public static void setFechaUltimoEcho(String fechaUltimoEcho) {
        Device.fechaUltimoEcho = fechaUltimoEcho;
    }

    public static String getFechaUltimaTransaccion() {
        return fechaUltimaTransaccion;
    }

    public static void setFechaUltimaTransaccion(String fechaUltimaTransaccion) {
        Device.fechaUltimaTransaccion = fechaUltimaTransaccion;
    }

    public static String getFechaUltimoCierre() {
        return fechaUltimoCierre;
    }

    public static void setFechaUltimoCierre(String fechaUltimoCierre) {
        Device.fechaUltimoCierre = fechaUltimoCierre;
    }

    public static String getGrupo() {
        return grupo;
    }

    public static void setGrupo(String grupo) {
        Device.grupo = grupo;
    }

    public static String getFechaAlta() {
        return fechaAlta;
    }

    public static void setFechaAlta(String fechaAlta) {
        Device.fechaAlta = fechaAlta;
    }

    public static String getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public static void setFechaUltimaActualizacion(String fechaUltimaActualizacion) {
        Device.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public static String getUsuarioUltimoActualizacion() {
        return usuarioUltimoActualizacion;
    }

    public static void setUsuarioUltimoActualizacion(String usuarioUltimoActualizacion) {
        Device.usuarioUltimoActualizacion = usuarioUltimoActualizacion;
    }

    public static String getInicializacionMsg() {
        return inicializacionMsg;
    }

    public static void setInicializacionMsg(String inicializacionMsg) {
        Device.inicializacionMsg = inicializacionMsg;
    }

    public static String getCargaLlavesMsg() {
        return cargaLlavesMsg;
    }

    public static void setCargaLlavesMsg(String cargaLlavesMsg) {
        Device.cargaLlavesMsg = cargaLlavesMsg;
    }

    public static boolean getConexion() {
        return conexion;
    }

    public static void setConexion(String conexion) {
        Device.conexion = UIUtils.verificacionBoolean(context, conexion, "Tabla Device");

    }
}
