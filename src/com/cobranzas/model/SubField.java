package com.cobranzas.model;

public class SubField {
    String fieldId;
    String fieldData;


    public SubField(String idCampo, String dataCampo) {
        this.fieldId = idCampo;
        this.fieldData = dataCampo;
    }


    public String getFieldId() {
        return fieldId;
    }


    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldData() {
        return fieldData;
    }


    public void setFieldData(String fieldData) {
        this.fieldData = fieldData;
    }


}
