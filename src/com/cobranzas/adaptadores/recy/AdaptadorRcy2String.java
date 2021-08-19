package com.cobranzas.adaptadores.recy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wposs.cobranzas.R;
import com.cobranzas.model.DosStringModel;
import com.cobranzas.model.Item;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorRcy2String extends RecyclerView.Adapter<AdaptadorRcy2String.ViewHolder> implements AdapterRecyItem {

    private List<Item> list;
    Context ctx;
    private int layout;

    public AdaptadorRcy2String(int layout, Context ctx) {
        this.list = new ArrayList<>();
        this.ctx = ctx;
        this.layout = layout;
    }

    public AdaptadorRcy2String(List<Item> list, Context ctx, int layout) {
        this.list = list;
        this.ctx = ctx;
        this.layout = layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DosStringModel item = (DosStringModel) list.get(position);
        holder.textView.setText(item.getTitulo());
        holder.textView2.setText(item.getSubTitulo());
        if (item.getSubTitulo().length() > 20) {
            holder.textView2.setGravity(Gravity.RIGHT);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public RecyclerView.Adapter newAdapter(List<Item> list, Context ctx) {
        return new AdaptadorRcy2String(list, ctx, layout);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView textView2;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvTitulo);
            textView2 = itemView.findViewById(R.id.tvSubTitulo);
        }
    }
}
