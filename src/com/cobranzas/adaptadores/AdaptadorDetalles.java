package com.cobranzas.adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cobranzas.model.ModelSetting;
import com.wposs.cobranzas.R;

import java.util.List;

public class AdaptadorDetalles extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModelSetting> items;
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public AdaptadorDetalles() {
    }

    public interface OnItemClickListener {
        void onItemClick(View view, ModeloBotones obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdaptadorDetalles(Context ctx, List<ModelSetting> items) {
        this.ctx = ctx;
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_info, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            final ModelSetting p = items.get(position);
            view.tvTitle.setText(p.getNombre());
            inicializarRecyclerView(view.rvAdapter, ctx);
            if (!p.getModeloBotones().isEmpty()) {
                Adaptador adaptador = new Adaptador(ctx, p.getModeloBotones());
                adaptador.setLayout(R.layout.itemdetalle);
                view.rvAdapter.setAdapter(adaptador);
                adaptador.setOnItemClickListener(new Adaptador.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, ModeloBotones obj, int position) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(view, p.getModeloBotones().get(position), position);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvTitle;
        public final RecyclerView rvAdapter;

        public OriginalViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitulo);
            rvAdapter = v.findViewById(R.id.rcyItem);
        }
    }

    private void inicializarRecyclerView(RecyclerView recyclerView, Context context) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
}

