package com.cobranzas.model;

import java.util.ArrayList;
import java.util.List;

public class ContenedorRcyModel implements Contenedor, Item {
    protected String tituloContenedor;
    protected List<Item> list;

    public ContenedorRcyModel(String tituloContenedor, List<Item> list) {
        this.tituloContenedor = tituloContenedor;
        this.list = list;
    }

    public ContenedorRcyModel(String tituloContenedor) {
        this.tituloContenedor = tituloContenedor;
        this.list = new ArrayList<>();
    }

    public void addItemWidget(Item item) {
        list.add(item);
    }

    @Override
    public String getTitulo() {
        return tituloContenedor;
    }

    @Override
    public int getSizeList() {
        return list.size();
    }

    @Override
    public List<Item> getList() {
        return list;
    }
}
