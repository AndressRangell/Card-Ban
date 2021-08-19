package com.cobranzas.menus;

import java.util.ArrayList;
import java.util.List;

public class MenuModelo {

    private int img;
    private List<menuItemsModelo> listMenuItemsModelos = new ArrayList<>();

    public MenuModelo(int imgHeader) {
        this.img = imgHeader;
    }
    public MenuModelo() {

    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public List<menuItemsModelo> getListMenuItemsModelos() {
        return listMenuItemsModelos;
    }


    public void setListMenuItemsModelos(List<menuItemsModelo> listMenuItemsModelos) {
        this.listMenuItemsModelos = listMenuItemsModelos;
    }

    public void addMenuItemModel(menuItemsModelo itemsModelo) {
        listMenuItemsModelos.add(itemsModelo);
    }
}
