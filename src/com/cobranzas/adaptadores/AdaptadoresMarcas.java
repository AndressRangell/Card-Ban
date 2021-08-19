package com.cobranzas.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wposs.cobranzas.R;

import java.util.List;

public class AdaptadoresMarcas extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModeloBotones> itemsMarcas;
    Context ctx;

    public AdaptadoresMarcas() {
    }

    public AdaptadoresMarcas(Context ctx, List<ModeloBotones> itemsMarcas) {
        this.ctx = ctx;
        this.itemsMarcas = itemsMarcas;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemarcas, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            ModeloBotones p = itemsMarcas.get(position);
            view.imgMenu.setImageDrawable(p.imageDrw);
        }
    }

    @Override
    public int getItemCount() {
        return itemsMarcas.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imgMenu;
        public final CardView cvMenu;

        public OriginalViewHolder(View v) {
            super(v);
            imgMenu = v.findViewById(R.id.img_menu1);
            cvMenu = v.findViewById(R.id.cv_menu);
        }
    }
}
