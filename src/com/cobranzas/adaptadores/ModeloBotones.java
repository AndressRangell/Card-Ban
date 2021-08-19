package com.cobranzas.adaptadores;

import android.graphics.drawable.Drawable;

public class ModeloBotones {

    public int idBoton;
    public String codBoton;
    public String nombreBoton;
    public Drawable imageDrw;

    public ModeloBotones() {
    }

    public ModeloBotones(String nombreBoton) {
        this.nombreBoton = nombreBoton;
    }

    public ModeloBotones(String codBoton, String nombreBoton) {
        this.codBoton = codBoton;
        this.nombreBoton = nombreBoton;
    }

    public ModeloBotones(String codBoton, String nombreBoton, Drawable imageDrw) {
        this.codBoton = codBoton;
        this.nombreBoton = nombreBoton;
        this.imageDrw = imageDrw;
    }

    public ModeloBotones(int idBoton, Drawable imageDrw) {
        this.idBoton = idBoton;
        this.imageDrw = imageDrw;
    }

    public int getIdBoton() {
        return idBoton;
    }

    public void setIdBoton(int idBoton) {
        this.idBoton = idBoton;
    }

    public String getCodBoton() {
        return codBoton;
    }

    public void setCodBoton(String codBoton) {
        this.codBoton = codBoton;
    }

    public String getNombreBoton() {
        return nombreBoton;
    }

    public void setNombreBoton(String nombreBoton) {
        this.nombreBoton = nombreBoton;
    }

    public Drawable getImageDrw() {
        return imageDrw;
    }

    public void setImageDrw(Drawable imageDrw) {
        this.imageDrw = imageDrw;
    }
}
