package com.cobranzas.model;

import com.cobranzas.adaptadores.ModeloBotones;

import java.util.List;

public class ModelSetting {

    public String idBoton;
    public String nombre;
    public List<ModeloBotones> modeloBotones;

    public ModelSetting() {
    }

    public ModelSetting(String idBoton, String nombre, List<ModeloBotones> modeloBotones) {
        this.idBoton = idBoton;
        this.nombre = nombre;
        this.modeloBotones = modeloBotones;
    }

    public String getIdBoton() {
        return idBoton;
    }

    public void setIdBoton(String idBoton) {
        this.idBoton = idBoton;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<ModeloBotones> getModeloBotones() {
        return modeloBotones;
    }

    public void setModeloBotones(List<ModeloBotones> modeloBotones) {
        this.modeloBotones = modeloBotones;
    }
}
