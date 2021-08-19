package com.cobranzas.model;

import com.cobranzas.model.widgets.ItemWidget;

import java.util.ArrayList;
import java.util.List;

public class ContenedorRcyWidgetM extends ContenedorRcyModel implements Contenedor, Item {

    private String[] args;
    private String[] rowToModificate;

    public ContenedorRcyWidgetM(String tituloContenedor, List<Item> list) {
        super(tituloContenedor, list);
    }


    public String[] getRowToModificate() {
        toModificate();
        return rowToModificate;
    }

    public String[] getArgsToModificate() {
        return args;
    }

    private void toModificate() {
        List<String> titulos = new ArrayList<>();
        List<String> valoresNew = new ArrayList<>();

        ItemWidget itemWidget;
        for (Item item_item : list) {
            itemWidget = (ItemWidget) item_item;
            if (itemWidget.isNewValor()) {
                titulos.add(itemWidget.getTituloItem());
                valoresNew.add(itemWidget.getResultado());
            }
        }
        rowToModificate = new String[titulos.size()];
        args = new String[valoresNew.size()];

        args = valoresNew.toArray(args);
        rowToModificate = titulos.toArray(rowToModificate);
    }
}
