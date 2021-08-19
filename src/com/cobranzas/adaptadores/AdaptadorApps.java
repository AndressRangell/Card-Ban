package com.cobranzas.adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wposs.cobranzas.R;

import java.util.List;

public class AdaptadorApps extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModeloApps> modeloAppsList;
    Context ctx;
    private AdaptadorApps.OnItemClickListener mOnItemClickListener;
    private int layout;

    public AdaptadorApps(List<ModeloApps> modeloAppsList, Context ctx) {
        this.modeloAppsList = modeloAppsList;
        this.ctx = ctx;
        layout = R.layout.item_more_apps;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, ModeloApps obj, int position);
    }

    public void setOnItemClickListener(final AdaptadorApps.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (holder instanceof AdaptadorApps.OriginalViewHolder) {
            final AdaptadorApps.OriginalViewHolder view = (AdaptadorApps.OriginalViewHolder) holder;

            ModeloApps p = modeloAppsList.get(position);
            view.textViewApps.setText(p.getNombreApp());

            view.cardViewApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, modeloAppsList.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return modeloAppsList.size();
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public final TextView textViewApps;
        public final LinearLayout cardViewApps;

        public OriginalViewHolder(View v) {
            super(v);
            textViewApps = v.findViewById(R.id.textViewApps);
            cardViewApps = v.findViewById(R.id.card_apps);
        }
    }
}
