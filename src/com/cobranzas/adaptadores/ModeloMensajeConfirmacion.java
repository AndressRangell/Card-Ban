package com.cobranzas.adaptadores;

import android.graphics.drawable.Drawable;

public class ModeloMensajeConfirmacion {

    private String banner;
    private String titulo;
    private String mensaje;
    private String subMensaje;
    private String msgBtnAceptar;
    private String msgBtnCancelar;
    private Drawable drawable;

    public ModeloMensajeConfirmacion() {
    }

    public ModeloMensajeConfirmacion(String banner, String titulo, String mensaje, String subMensaje, String msgBtnAceptar, String msgBtnCancelar, Drawable drawable) {
        this.banner = banner;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.subMensaje = subMensaje;
        this.msgBtnAceptar = msgBtnAceptar;
        this.msgBtnCancelar = msgBtnCancelar;
        this.drawable = drawable;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getSubMensaje() {
        return subMensaje;
    }

    public void setSubMensaje(String subMensaje) {
        this.subMensaje = subMensaje;
    }

    public String getMsgBtnAceptar() {
        return msgBtnAceptar;
    }

    public void setMsgBtnAceptar(String msgBtnAceptar) {
        this.msgBtnAceptar = msgBtnAceptar;
    }

    public String getMsgBtnCancelar() {
        return msgBtnCancelar;
    }

    public void setMsgBtnCancelar(String msgBtnCancelar) {
        this.msgBtnCancelar = msgBtnCancelar;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
