package com.cobranzas.cajas.model;

import android.support.annotation.Keep;

@Keep
public class SuccesBines {

    private String bin;
    private String nsu;

    public SuccesBines() {
        // Do nothing because of X and Y.
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getNsu() {
        return nsu;
    }

    public void setNsu(String nsu) {
        this.nsu = nsu;
    }
}
