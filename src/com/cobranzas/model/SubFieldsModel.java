package com.cobranzas.model;

public class SubFieldsModel {
    String mti;
    String proCode;
    int[] fields;

    public SubFieldsModel(String mti, String proCode, int[] fields) {
        this.mti = mti;
        this.proCode = proCode;
        this.fields = fields;
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getProCode() {
        return proCode;
    }

    public void setProCode(String proCode) {
        this.proCode = proCode;
    }

    public int[] getFields() {
        return fields;
    }

    public void setFields(int[] fields) {
        this.fields = fields;
    }

}