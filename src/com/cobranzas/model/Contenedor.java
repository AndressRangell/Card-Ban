package com.cobranzas.model;

import java.util.List;

public interface Contenedor extends Item {

    String getTitulo();
    int getSizeList();
    List<Item> getList();
}
