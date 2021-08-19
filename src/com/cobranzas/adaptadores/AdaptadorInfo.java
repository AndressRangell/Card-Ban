package com.cobranzas.adaptadores;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wposs.cobranzas.R;
import com.cobranzas.model.InfoModelo;

import java.util.List;

public class AdaptadorInfo extends RecyclerView.Adapter<AdaptadorInfo.ViewHolder> {

    private boolean isItem;
    private List<InfoModelo> list;
    private int layout;
    private Context ctx;

    public AdaptadorInfo(List<InfoModelo> list, boolean isItem, Context ctx) {
        this.isItem = isItem;
        this.list = list;
        this.ctx = ctx;
        layout = R.layout.item_info;
    }

    @Override
    public AdaptadorInfo.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AdaptadorInfo.ViewHolder holder, int i) {
        InfoModelo item = list.get(i);
        holder.tvTitle.setText(item.getTitulo());
        AdaptadorInfo adapterInfo;
        if (isItem) {
            if (item.isContenedor()) {
                adapterInfo = new AdaptadorInfo(item.getList(), true, ctx);
                adapterInfo.setLayout(layout);

            } else {
                adapterInfo = new AdaptadorInfo(item.getList(), false, ctx);
                adapterInfo.setLayout(R.layout.item_item_info_);
            }
            holder.rcy.setAdapter(adapterInfo);
        } else {
            holder.tvSub.setText(item.getSubtitulo());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvSub;
        ImageView img;
        private RecyclerView rcy;
        LinearLayout ly;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitulo);
            tvSub = itemView.findViewById(R.id.tvSubTitulo);
            if (isItem) {
                rcy = itemView.findViewById(R.id.rcyItem);
                rcy.setLayoutManager(new LinearLayoutManager(ctx));
            }
        }
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }
}
