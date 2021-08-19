package com.cobranzas.inicializacion.configuracioncomercio;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import java.util.ArrayList;

public class TransActive {

    private boolean venta = false;
    private boolean ventaZimple = false;
    private boolean ventaCashBack = false;
    private boolean ventaMinutos = false;

    String clase = "TransActive.java";

    public static String[] listTransAllow = new String[]{
            "venta",
            "venta zimple",
            "venta casback",
            "venta minutos",
    };

    private void setTransAllow(int pos, boolean value) {
        switch (pos) {
            case 0:
                setVenta(value);
                break;
            case 1:
                setVentaZimple(value);
                break;
            case 2:
                setVentaCashBack(value);
                break;
            case 3:
                setVentaMinutos(value);
                break;
            default:
                break;
        }
    }

    public void verificateTransActive(ArrayList<TRANSACCIONES> transaccionesList) {

        TRANSACCIONES currentTrans = null;
        try {
            for (int i = 0; i < listTransAllow.length; i++) {
                currentTrans = transaccionesList.get(i);
                if (listTransAllow[i].equals(currentTrans.getNombre())) {
                    System.out.println();
                    setTransAllow(i, currentTrans.getHabilitar());
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
            Logger.logLine(LogType.EXCEPTION, clase, ignored.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, ignored.getStackTrace());
        }
    }

    public boolean isVenta() {
        return venta;
    }

    public void setVenta(boolean venta) {
        this.venta = venta;
    }

    public boolean isVentaZimple() {
        return ventaZimple;
    }

    public void setVentaZimple(boolean ventaZimple) {
        this.ventaZimple = ventaZimple;
    }

    public boolean isVentaCashBack() {
        return ventaCashBack;
    }

    public void setVentaCashBack(boolean ventaCashBack) {
        this.ventaCashBack = ventaCashBack;
    }

    public boolean isVentaMinutos() {
        return ventaMinutos;
    }

    public void setVentaMinutos(boolean ventaMinutos) {
        this.ventaMinutos = ventaMinutos;
    }

}
