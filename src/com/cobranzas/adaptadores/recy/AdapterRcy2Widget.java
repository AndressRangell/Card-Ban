package com.cobranzas.adaptadores.recy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cobranzas.model.Item;
import com.cobranzas.model.widgets.EdiTextWMbtn;
import com.cobranzas.model.widgets.EdiTextWidgetModel;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterRcy2Widget extends  RecyclerView.Adapter<AdapterRcy2Widget.ViewHolder> implements AdapterRecyItem  {
    private List<Item> list;
    Context ctx;
    private int layout;

    public AdapterRcy2Widget(int layout) {
        this.list = new ArrayList<>();
        this.ctx = null;
        this.layout = layout;
    }

    public AdapterRcy2Widget(List<Item> list, Context ctx, int layout) {
        this.list = list;
        this.ctx = ctx;
        this.layout = layout;
    }

    @Override
    public AdapterRcy2Widget.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, null, false);
        return new AdapterRcy2Widget.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AdapterRcy2Widget.ViewHolder holder, int position) {
        try {
            EdiTextWMbtn item =(EdiTextWMbtn) list.get(position);
            if (item.getButton() != null) {
                holder.linearLayout2.addView(item.getButton());
            }
            holder.textView.setText(item.getTituloItem());
            holder.linearWidget.addView(item.getWidgetItem());
        } catch (Exception e) {
            EdiTextWidgetModel etItem =(EdiTextWidgetModel) list.get(position);
            holder.textView.setText(etItem.getTituloItem());
            holder.linearWidget.addView(etItem.getWidgetItem());
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public RecyclerView.Adapter newAdapter(List<Item> list, Context ctx) {
        return new AdapterRcy2Widget(list, ctx, layout);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        LinearLayout linearWidget;
        LinearLayout linearLayout2;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            linearWidget = itemView.findViewById(R.id.linearWidget);
            linearLayout2 = itemView.findViewById(R.id.linearWidget2);
        }
    }
}
