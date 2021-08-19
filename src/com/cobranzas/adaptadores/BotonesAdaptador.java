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

public class BotonesAdaptador extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    private List<ModeloBotones> botones;
    private OnItemClickListener mOnItemClickListener;


    public BotonesAdaptador(Context context, List<ModeloBotones> botones) {
        this.context = context;
        this.botones = botones;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_botones, parent, false);
        vh = new ViewHolderBotones(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolderBotones) {
            final ViewHolderBotones view = (ViewHolderBotones) holder;

            ModeloBotones p = botones.get(position);

            view.title.setText(p.getNombreBoton());

            if (p.imageDrw != null) {
                view.imageView.setImageDrawable(p.imageDrw);
            }

            view.cvMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(v, botones.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return botones.size();
    }

    public class ViewHolderBotones extends RecyclerView.ViewHolder {

        public final TextView title;
        public final ImageView imageView;
        public final LinearLayout cvMenu;

        public ViewHolderBotones(View v) {
            super(v);
            cvMenu = v.findViewById(R.id.cv_menu);
            imageView = v.findViewById(R.id.imageView);
            title = v.findViewById(R.id.titulo);
        }
    }
}
