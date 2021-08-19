package com.cobranzas.inicializacion.configuracioncomercio;

public class Defered {

    String activarPlan;
    private String descripcionPlan;
    private String etiquetaPlan;
    private String idPlan;
    private String rfu;

    public static final String NAME_TABLE = "PLANES";

    protected static String[] fields = new String[]{
            "ACTIVAR_PLAN",
            "DESCRIPCION_PLAN",
            "ETIQUETA_PLAN",
            "ID_PLAN",
            "RFU"
    };

    private void setPlanes(String column, String value) {
        switch (column) {
            case "ACTIVAR_PLAN":
                setActivarPlan(value);
                break;
            case "DESCRIPCION_PLAN":
                setDescripcionPlan(value);
                break;
            case "ETIQUETA_PLAN":
                setEtiquetaPlan(value);
                break;
            case "ID_PLAN":
                setIdPlan(value);
                break;
            case "RFU":
                setRfu(value);
                break;
            default:
                break;
        }
    }

    public void setActivarPlan(String activarPlan) {
        this.activarPlan = activarPlan;
    }

    public String getDescripcionPlan() {
        return descripcionPlan;
    }

    public void setDescripcionPlan(String descripcionPlan) {
        this.descripcionPlan = descripcionPlan;
    }

    public String getEtiquetaPlan() {
        return etiquetaPlan;
    }

    public void setEtiquetaPlan(String etiquetaPlan) {
        this.etiquetaPlan = etiquetaPlan;
    }

    public String getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(String idPlan) {
        this.idPlan = idPlan;
    }

    public String getRfu() {
        return rfu;
    }

    public void setRfu(String rfu) {
        this.rfu = rfu;
    }
}
