package com.cobranzas.model.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class LayoutManagerRcy {

    private final Context ctx;

    int tipo;

    public LayoutManagerRcy(Context ctx, int tipo) {
        this.tipo = tipo;
        this.ctx = ctx;
    }

    public LayoutManagerRcy(Context ctx) {
        this.ctx = ctx;
        this.tipo = 0;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        if (tipo == 1) {
            return new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false);
        }
        return new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false);
    }
}
