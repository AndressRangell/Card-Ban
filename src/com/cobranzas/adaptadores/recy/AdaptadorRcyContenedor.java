package com.cobranzas.adaptadores.recy;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wposs.cobranzas.R;
import com.cobranzas.model.Contenedor;
import com.cobranzas.model.Item;
import com.cobranzas.model.widgets.LayoutManagerRcy;

import java.util.List;

public class AdaptadorRcyContenedor extends RecyclerView.Adapter<AdaptadorRcyContenedor.ViewHolder> implements AdapterRecyItem {

    private List<Item> list;
    private Context ctx;
    private int layout;
    private AdapterRecyItem adapterRecyItem;
    private AdapterRecyItem adapterRecyItem2;
    private LayoutManagerRcy layoutManager;


    public AdaptadorRcyContenedor(int layout, AdapterRecyItem adapterRecyItem2) {
        this.layout = layout;
        this.adapterRecyItem2 = adapterRecyItem2;
    }

    /**
     * @param layout debe tener TextView id = tvTitulo  y RecyclerView id = rcyItem )
     */
    public AdaptadorRcyContenedor(List<Item> list, Context ctx, int layout, AdapterRecyItem adapterRecyItem) {
        this.list = list;
        this.ctx = ctx;
        this.layout = layout;
        this.adapterRecyItem = adapterRecyItem;
        this.layoutManager = new LayoutManagerRcy(ctx);
    }

    public AdaptadorRcyContenedor(List<Item> list, Context ctx, int layout, AdapterRecyItem adapterRecyItem, LayoutManagerRcy layoutManager) {
        this.list = list;
        this.ctx = ctx;
        this.layout = layout;
        this.adapterRecyItem = adapterRecyItem;
        this.layoutManager = layoutManager;
    }

    @Override
    public AdaptadorRcyContenedor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AdaptadorRcyContenedor.ViewHolder holder, int i) {
        Contenedor item = (Contenedor) list.get(i);
        if (item.getTitulo().isEmpty()) {
            holder.tvTitulo.setVisibility(View.GONE);
        }
        holder.tvTitulo.setText(item.getTitulo());
        holder.rcy.setLayoutManager(layoutManager.getLayoutManager());
        holder.rcy.setAdapter(adapterRecyItem.newAdapter(item.getList(), ctx.getApplicationContext()));
    }

    public void setLayoutManager(LayoutManagerRcy layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public RecyclerView.Adapter newAdapter(List<Item> list, Context ctx) {
        return new AdaptadorRcyContenedor(list, ctx, layout, adapterRecyItem2, layoutManager);
    }

    public void setAdapterRecyItem2(AdapterRecyItem adapterRecyItem2) {
        this.adapterRecyItem2 = adapterRecyItem2;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitulo;
        private RecyclerView rcy;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            rcy = itemView.findViewById(R.id.rcyItem);
            rcy.setLayoutManager(new LinearLayoutManager(ctx));
        }
    }
}
