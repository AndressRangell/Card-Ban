package com.cobranzas.adaptadores;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wposs.cobranzas.R;

import java.util.List;

public class Adaptador extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModeloBotones> modeloBotonesList;
    Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private int layout;

    public Adaptador(Context ctx, List<ModeloBotones> modeloBotonesList) {
        this.modeloBotonesList = modeloBotonesList;
        this.ctx = ctx;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, ModeloBotones obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            ModeloBotones p = modeloBotonesList.get(position);
            view.txTitleMenu.setText(p.getNombreBoton());

            if (p.imageDrw != null) {
                view.imageView.setImageDrawable(p.imageDrw);
            } else {
                view.txCodButton.setText(p.getCodBoton().trim());
            }

            view.cvMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, modeloBotonesList.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return modeloBotonesList.size();
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public final TextView txTitleMenu;
        public final TextView txCodButton;
        public final ImageView imageView;
        public final LinearLayout cvMenu;

        public OriginalViewHolder(View v) {
            super(v);
            txTitleMenu = v.findViewById(R.id.tx_titulomenu);
            cvMenu = v.findViewById(R.id.cv_menu);
            imageView = v.findViewById(R.id.imageView);
            txCodButton = v.findViewById(R.id.tx_codBoton);
        }
    }
}
