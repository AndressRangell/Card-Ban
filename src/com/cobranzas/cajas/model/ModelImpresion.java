package com.cobranzas.cajas.model;

import android.support.annotation.Keep;

@Keep
public class ModelImpresion {

    private String codigoAutorizacion;
    private String nroBoleta;
    private String codigoComercio;
    private String nombreTarjeta;
    private String pan;
    private String mensajeDisplay;
    private int saldo;
    private String nombreCliente;
    private String issuerId;
    private int montoVuelto;

    public ModelImpresion() {
        // Do nothing because of X and Y.
    }

    public String getCodigoAutorizacion() {
        return codigoAutorizacion;
    }

    public void setCodigoAutorizacion(String codigoAutorizacion) {
        this.codigoAutorizacion = codigoAutorizacion;
    }

    public String getNroBoleta() {
        return nroBoleta;
    }

    public void setNroBoleta(String nroBoleta) {
        this.nroBoleta = nroBoleta;
    }

    public String getCodigoComercio() {
        return codigoComercio;
    }

    public void setCodigoComercio(String codigoComercio) {
        this.codigoComercio = codigoComercio;
    }

    public String getNombreTarjeta() {
        return nombreTarjeta;
    }

    public void setNombreTarjeta(String nombreTarjeta) {
        this.nombreTarjeta = nombreTarjeta;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getMensajeDisplay() {
        return mensajeDisplay;
    }

    public void setMensajeDisplay(String mensajeDisplay) {
        this.mensajeDisplay = mensajeDisplay;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public int getMontoVuelto() {
        return montoVuelto;
    }

    public void setMontoVuelto(int montoVuelto) {
        this.montoVuelto = montoVuelto;
    }
}
