package com.cobranzas.adaptadores.recy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cobranzas.adaptadores.ModeloBotones;
import com.wposs.cobranzas.R;

import java.util.ArrayList;

public class AdaptadorRcyConfiWifi extends RecyclerView.Adapter<AdaptadorRcyConfiWifi.ViewHolder> {

    Context context;
    private ArrayList<ModeloBotones> list;
    private OnItemClickListener mOnItemClickListener;

    private int layout;

    public AdaptadorRcyConfiWifi(Context context, ArrayList<ModeloBotones> list, OnItemClickListener mOnItemClickListener, int layout) {
        this.context = context;
        this.list = list;
        this.mOnItemClickListener = mOnItemClickListener;
        this.layout = layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        ModeloBotones item = list.get(position);
        holder.textView.setText(item.getNombreBoton());
        if (item.imageDrw != null) {
            holder.img.setImageDrawable(item.imageDrw);
        }
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, list.get(position), position);
            }
        });

    }


    public interface OnItemClickListener {
        void onItemClick(View view, ModeloBotones obj, int position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView img;
        RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv01);
            img = itemView.findViewById(R.id.img);
            relativeLayout = itemView.findViewById(R.id.rl01);
        }
    }
}
