package com.cobranzas.model;

import java.util.ArrayList;
import java.util.List;

public class InfoModelo {
    private String titulo;
    private String subtitulo;
    private List<InfoModelo> list = null;
    private boolean isContenedor = false;

    public InfoModelo(String titulo, String subtitulo) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
    }

    public InfoModelo(String titulo) {
        this.titulo = titulo;
    }

    public InfoModelo(String titulo, boolean isContenedor) {
        this.titulo = titulo;
        this.isContenedor = isContenedor;
        if (isContenedor) {
            list = new ArrayList<>();
        }
    }

    public InfoModelo() {
    }

    public void crearLista() {
        list = new ArrayList<>();
    }

    public void addItem(InfoModelo item) {
        list.add(item);
    }

    public InfoModelo getItem(int i) {
        return list.get(i);
    }

    public int getSizeLis() {
        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }

    public boolean isContenedor() {
        boolean result = false;
        if (list == null) {
            return false;
        } else {
            for (InfoModelo item : list) {
                if (item.getSizeLis() == 0) {
                    return false;
                } else {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isContenedorNoVerificado() {
        return isContenedor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public List<InfoModelo> getList() {
        if (list == null) {
            return new ArrayList<>();
        } else {
            return list;
        }
    }

    public void setList(List<InfoModelo> list) {
        this.list = list;
    }
}
