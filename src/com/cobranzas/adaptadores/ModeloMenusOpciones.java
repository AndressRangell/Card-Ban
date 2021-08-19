package com.cobranzas.adaptadores;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.cobranzas.inicializacion.configuracioncomercio.TRANSACCIONES;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import static com.cobranzas.actividades.StartAppBANCARD.listadoTransacciones;

public class ModeloMenusOpciones {

    private String id;
    private String nombreTransaccion;
    private boolean habilitarTransaccion;
    private Drawable icono;
    private String inputContent;

    public ModeloMenusOpciones() {
    }

    public ModeloMenusOpciones(String id, String nombreTransaccion, boolean habilitarTransaccion) {
        this.id = id;
        this.nombreTransaccion = nombreTransaccion;
        this.habilitarTransaccion = habilitarTransaccion;
    }

    public ModeloMenusOpciones(String id, Drawable icono) {
        this.id = id;
        this.icono = icono;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreTransaccion() {
        return nombreTransaccion;
    }

    public void setNombreTransaccion(String nombreTransaccion) {
        this.nombreTransaccion = nombreTransaccion;
    }

    public boolean isHabilitarTransaccion() {
        return habilitarTransaccion;
    }

    public void setHabilitarTransaccion(boolean habilitarTransaccion) {
        this.habilitarTransaccion = habilitarTransaccion;
    }

    public Drawable getIcono() {
        return icono;
    }

    public void setIcono(Drawable icono) {
        this.icono = icono;
    }

    public static ModeloMenusOpciones getTransaccion(String nameTrans) {
        ModeloMenusOpciones list = new ModeloMenusOpciones();
        try {
            for (TRANSACCIONES transaccion : listadoTransacciones) {
                if ((transaccion.getNombre().equals(nameTrans)) && (transaccion.getHabilitar())) {
                    list.setId(transaccion.getTransaccionId());
                    list.setNombreTransaccion(transaccion.getNombre());
                    list.setHabilitarTransaccion(transaccion.getHabilitar());
                    break;
                }
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION,"ModeloMenusOpciones.java", e.getMessage());
            Logger.logLine(LogType.EXCEPTION,"ModeloMenusOpciones.java", e.getStackTrace());
            e.printStackTrace();
            Log.d("ModeloMenusOpciones", "getTransaccion: " + e.getMessage());
        }

        return list;
    }

    public String getInputContent() {
        return inputContent;
    }

    public void setInputContent(String inputContent) {
        this.inputContent = inputContent;
    }
}
