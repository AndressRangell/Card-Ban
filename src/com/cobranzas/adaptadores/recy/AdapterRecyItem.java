package com.cobranzas.adaptadores.recy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.cobranzas.model.Item;

import java.util.List;

public interface AdapterRecyItem {

    public RecyclerView.Adapter newAdapter(List<Item> list, Context ctx);
}
