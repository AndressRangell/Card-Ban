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

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class COMERCIOS {

    String clase = "COMERCIOS.java";

    private String categoria;
    private String merchantDescription;
    private boolean habilitaFirma;
    private String claveComercio;
    private String redId;
    private String perfil;
    private String tipo;
    private String fechaHoraAlta;
    private String fechaHoraUltimaActualizacion;
    private String usuarioUltimaActualizacion;
    private String usuarioAlta;
    private String perfilDescarga;

    private static final String NAME_CATEGORIA = "CATEGORIA";
    private static final String NAME_MERCHANT_DESCRIPTION = "merchant_description";
    private static final String NAME_HABILITA_FIRMA = "HABILITA_FIRMA";
    private static final String NAME_CLAVE_COMERCIO = "CLAVE_COMERCIO";
    private static final String NAME_RED_ID = "red_id";
    private static final String NAME_PERFIL = "PERFIL";
    private static final String NAME_TIPO = "TIPO";
    private static final String NAME_FECHA_HORA_ALTA = "FECHA_HORA_ALTA";
    private static final String NAME_FECHA_HORA_ULTIMA_ACTUALIZACION = "FECHA_HORA_ULTIMA_ACTUALIZACION";
    private static final String NAME_USUARIO_ULTIMA_ACTUALIZACION = "USUARIO_ULTIMA_ACTUALIZACION";
    private static final String NAME_USUARIO_ALTA = "USUARIO_ALTA";
    private static final String NAME_PERFIL_DESCARGA = "PERFIL_DESCARGA";

    private static final String NAME_TABLE = "COMERCIOS";
    public SUCURSAL sucursal = null;

    private Context context;

    public COMERCIOS(Context context) {
        this.context = context;
    }

    protected static String[] fields = new String[]{
            NAME_CATEGORIA,
            NAME_MERCHANT_DESCRIPTION,
            NAME_HABILITA_FIRMA,
            NAME_CLAVE_COMERCIO,
            NAME_RED_ID,
            NAME_PERFIL,
            NAME_TIPO,
            NAME_FECHA_HORA_ALTA,
            NAME_FECHA_HORA_ULTIMA_ACTUALIZACION,
            NAME_USUARIO_ULTIMA_ACTUALIZACION
    };

    private void setTComercio(String column, String value) {
        switch (column) {

            case NAME_CATEGORIA:
                setCategoria(value);
                break;
            case NAME_MERCHANT_DESCRIPTION:
                setMerchantDescription(value);
                break;
            case NAME_HABILITA_FIRMA:
                setHabilitaFirma(value);
                break;
            case NAME_CLAVE_COMERCIO:
                setClaveComercio(value);
                break;
            case NAME_RED_ID:
                setRedId(value);
                break;
            case NAME_PERFIL:
                setPerfil(value);
                break;
            case NAME_TIPO:
                setTipo(value);
                break;
            case NAME_FECHA_HORA_ALTA:
                setFechaHoraAlta(value);
                break;
            case NAME_FECHA_HORA_ULTIMA_ACTUALIZACION:
                setFechaHoraUltimaActualizacion(value);
                break;
            case NAME_USUARIO_ULTIMA_ACTUALIZACION:
                setUsuarioUltimaActualizacion(value);
                break;
            case NAME_USUARIO_ALTA:
                setUsuarioAlta(value);
                break;
            case NAME_PERFIL_DESCARGA:
                setPerfilDescarga(value);
                break;
            default:
                break;
        }
    }

    public void clearCOMERCIOS() {
        for (String s : COMERCIOS.fields) {
            setTComercio(s, "");
        }
    }

    public static COMERCIOS getSingletonInstance(Context context) {
        if (tablaComercios == null) {
            tablaComercios = new COMERCIOS(context);
        } else {
            Log.d(NAME_TABLE, "No se puede crear otro objeto, ya existe");
        }
        return tablaComercios;
    }

    public boolean selectComercios(Context context) {
        //Read packages
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        String sql = consultaSQL();

        try {
            Cursor cursor = databaseAccess.rawQuery(sql, null);
            cursor.moveToFirst();
            int indexColumn;
            while (!cursor.isAfterLast()) {
                clearCOMERCIOS();
                indexColumn = 0;
                for (String s : COMERCIOS.fields) {
                    setTComercio(s, cursor.getString(indexColumn++).trim());
                }
                cursor.moveToNext();
            }
            cursor.close();
            // Cargar Informacion de sucursal
            if (sucursal == null) {
                sucursal = new SUCURSAL();
            } else {
                Log.d("SUCURSAL", "No se puede crear otro objeto, ya existe");
            }
            sucursal.selectSUCURSAL(context);

        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.debug(e.getMessage());
            return false;
        }
        databaseAccess.closeDb();
        return true;
    }

    private String consultaSQL() {
        StringBuilder sql = new StringBuilder();

        /*
         * Observacion : El 18-05-2020, se definió con el ingeniero Francisco
         * que la tabla Comercio, solo tendrá 1 registro por el momento. No aplica multicomercio aun.
         */

        sql.append("SELECT ");
        int counter = 1;
        for (String s : COMERCIOS.fields) {
            sql.append(s);
            if (counter++ < COMERCIOS.fields.length) {
                sql.append(",");
            }
        }

        sql.append(" FROM ");
        sql.append(NAME_TABLE);

        return sql.toString();
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getMerchantDescription() {
        return merchantDescription;
    }

    public void setMerchantDescription(String merchantDescription) {
        this.merchantDescription = merchantDescription;
    }

    public boolean isHabilitaFirma() {
        return habilitaFirma;
    }

    public void setHabilitaFirma(String habilitaFirma) {
        if (!habilitaFirma.isEmpty()) {
            if (habilitaFirma.equals("true") || habilitaFirma.equals("false")) {
                this.habilitaFirma = Boolean.parseBoolean(habilitaFirma);
            } else {
                showMensaje(" Error en la obtención de los Comercios");
                this.habilitaFirma = false;
            }
        }
    }

    public String getClaveComercio() {
        return claveComercio;
    }

    public void setClaveComercio(String claveComercio) {
        this.claveComercio = claveComercio;
    }

    public String getRedId() {
        return redId;
    }

    public void setRedId(String redId) {
        this.redId = redId;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFechaHoraAlta() {
        return fechaHoraAlta;
    }

    public void setFechaHoraAlta(String fechaHoraAlta) {
        this.fechaHoraAlta = fechaHoraAlta;
    }

    public String getFechaHoraUltimaActualizacion() {
        return fechaHoraUltimaActualizacion;
    }

    public void setFechaHoraUltimaActualizacion(String fechaHoraUltimaActualizacion) {
        this.fechaHoraUltimaActualizacion = fechaHoraUltimaActualizacion;
    }

    public String getUsuarioUltimaActualizacion() {
        return usuarioUltimaActualizacion;
    }

    public void setUsuarioUltimaActualizacion(String usuarioUltimaActualizacion) {
        this.usuarioUltimaActualizacion = usuarioUltimaActualizacion;
    }

    public String getUsuarioAlta() {
        return usuarioAlta;
    }

    public void setUsuarioAlta(String usuarioAlta) {
        this.usuarioAlta = usuarioAlta;
    }

    public String getPerfilDescarga() {
        return perfilDescarga;
    }

    public void setPerfilDescarga(String perfilDescarga) {
        this.perfilDescarga = perfilDescarga;
    }

    private void showMensaje(String mensaje) {
        if (context != null)
            UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, mensaje, Toast.LENGTH_LONG);
        else
            Log.d("Info", "showMensaje: " + "context == null "+mensaje);
    }
}
