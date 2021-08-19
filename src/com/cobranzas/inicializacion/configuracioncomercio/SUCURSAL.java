package com.cobranzas.inicializacion.configuracioncomercio;

import android.content.Context;
import android.database.Cursor;

import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class SUCURSAL {

    private static final String NAME_SUCURSAL_ID = "sucursal_id";
    private static final String NAME_MERCHANT_ID = "merchant_id";
    private static final String NAME_DESCRIPCION = "descripcion";
    private static final String NAME_PERFIL = "PERFIL";
    private static final String NAME_CARD_ACCP_MERCH = "CARD_ACCP_MERCH";
    private static final String NAME_DIRECCION_PRINCIPAL = "DIRECCION_PRINCIPAL";
    private static final String NAME_ESTADO = "ESTADO";
    private static final String NAME_DEPARTAMENTO = "DEPARTAMENTO";
    private static final String NAME_CIUDAD = "CIUDAD";
    private static final String NAME_ZONA = "ZONA";
    private static final String NAME_RUC = "RUC";
    private static final String NAME_RUBRO = "RUBRO";
    private static final String NAME_TELEFONO = "TELEFONO";
    private static final String NAME_FECHA_HORA_ALTA = "FECHA_HORA_ALTA";
    private static final String NAME_FECHA_HORA_ULTIMA_ACTUALIZACION = "FECHA_HORA_ULTIMA_ACTUALIZACION";
    private static final String NAME_USUARIO_ULTIMA_ACTUALIZACION = "USUARIO_ULTIMA_ACTUALIZACION";
    private static final String NAME_USUARIO_ALTA = "USUARIO_ALTA";
    private static final String NAME_PERFIL_DESCARGA = "PERFIL_DESCARGA";
    protected static String[] fields = new String[]{
            NAME_SUCURSAL_ID,
            NAME_MERCHANT_ID,
            NAME_DESCRIPCION,
            NAME_PERFIL,
            NAME_CARD_ACCP_MERCH,
            NAME_DIRECCION_PRINCIPAL,
            NAME_ESTADO,
            NAME_DEPARTAMENTO,
            NAME_CIUDAD,
            NAME_ZONA,
            NAME_RUC,
            NAME_RUBRO,
            NAME_TELEFONO,
            NAME_FECHA_HORA_ALTA,
            NAME_FECHA_HORA_ULTIMA_ACTUALIZACION,
            NAME_USUARIO_ULTIMA_ACTUALIZACION,
            NAME_USUARIO_ALTA,
            NAME_PERFIL_DESCARGA
    };
    private final String nameTable = "SUCURSAL";
    String clase = "SUCURSAL.java";
    private String sucursalId;
    private String merchantId;
    private String descripcion;
    private String perfil;
    private String cardAccpMerch;
    private String direccionPrincipal;
    private String estado;
    private String departamento;
    private String ciudad;
    private String zona;
    private String ruc;
    private String rubro;
    private String telefono;
    private String fechaHoraAlta;
    private String fechaHoraUltimaActualizacion;
    private String usuarioUltimaActualizacion;
    private String usuarioAlta;
    private String perfilDescarga;

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getCardAccpMerch() {
        return cardAccpMerch;
    }

    public void setCardAccpMerch(String cardAccpMerch) {
        this.cardAccpMerch = cardAccpMerch;
    }

    public String getDireccionPrincipal() {
        return direccionPrincipal;
    }

    public void setDireccionPrincipal(String direccionPrincipal) {
        this.direccionPrincipal = direccionPrincipal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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

    private void setSUCURSAL(String column, String value) {
        switch (column) {

            case NAME_SUCURSAL_ID:
                setSucursalId(value);
                break;
            case NAME_MERCHANT_ID:
                setMerchantId(value);
                break;
            case NAME_DESCRIPCION:
                setDescripcion(value);
                break;
            case NAME_PERFIL:
                setPerfil(value);
                break;
            case NAME_CARD_ACCP_MERCH:
                setCardAccpMerch(value);
                break;
            case NAME_DIRECCION_PRINCIPAL:
                setDireccionPrincipal(value);
                break;
            case NAME_ESTADO:
                setEstado(value);
                break;
            case NAME_DEPARTAMENTO:
                setDepartamento(value);
                break;
            case NAME_CIUDAD:
                setCiudad(value);
                break;
            case NAME_ZONA:
                setZona(value);
                break;
            case NAME_RUC:
                setRuc(value);
                break;
            case NAME_RUBRO:
                setRubro(value);
                break;
            case NAME_TELEFONO:
                setTelefono(value);
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

    /**
     * Observacion : El 18-05-2020, se definió con el ingeniero Francisco
     * que la tabla sucursal, solo tendrá 1 registro por el momento. No aplica multicomercio aun.
     *
     * @param context
     * @return
     */

    public boolean selectSUCURSAL(Context context) {
        //Read packages
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        int counter = 1;
        for (String s : SUCURSAL.fields) {
            sql.append(s);
            if (counter++ < SUCURSAL.fields.length) {
                sql.append(",");
            }
        }

        sql.append(" FROM ");
        sql.append(nameTable);

        try {
            Cursor cursor = databaseAccess.rawQuery(sql.toString(), null);
            cursor.moveToFirst();
            int indexColumn;
            while (!cursor.isAfterLast()) {
                clearSUCURSAL();
                indexColumn = 0;
                for (String s : SUCURSAL.fields) {
                    setSUCURSAL(s, cursor.getString(indexColumn++).trim());
                }
                cursor.moveToNext();
            }
            cursor.close();

        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.debug(e.getMessage());
            return false;
        }
        databaseAccess.closeDb();
        return true;
    }

    public void clearSUCURSAL() {
        for (String s : SUCURSAL.fields) {
            setSUCURSAL(s, "");
        }
    }
}
