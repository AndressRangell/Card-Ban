package com.cobranzas.model.widgets;


import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.cobranzas.model.Item;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public abstract class WidgetModel implements Item {

    protected String titulo;
    protected   int tipo;
    Context context;
    LinearLayout.LayoutParams lp;
    protected String oldValue;


    public WidgetModel(String titulo, int tipo, Context context) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.context = context;
        this.oldValue="";
        lp = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    public int getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public abstract View getWidget();

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
}
