package com.cobranzas.menus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wposs.cobranzas.R;
import com.cobranzas.defines_bancard.DefinesBANCARD;

import java.util.ArrayList;
import java.util.List;

public class NewAdapterMenus extends RecyclerView.Adapter<NewAdapterMenus.ViewHolder2> {

    public List<menuItemsModelo> menuItemsModeloList;
    Context context;
    String tipoLayout;
    private String tipoMenu;

    public NewAdapterMenus(List<menuItemsModelo> menuItemsModeloList, Context context, String tipoLayout) {
        this.menuItemsModeloList = menuItemsModeloList;
        this.context = context;
        this.tipoLayout = tipoLayout;

    }

    public void setTipoMenu(String s) {
        this.tipoMenu = s;
    }

    private OnItemClickListenerMesas listener;

    /**
     * Interfaz de comunicación
     */
    public interface OnItemClickListenerMesas {
        void onItemClick(RecyclerView.ViewHolder item, int position, int id);
    }

    public void setOnItemClickListener(OnItemClickListenerMesas listener) {
        this.listener = listener;
    }

    public OnItemClickListenerMesas getOnItemClickListener() {
        return listener;
    }

    @Override
    public NewAdapterMenus.ViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
        int idItemMenu;

        if (tipoMenu.equals(DefinesBANCARD.MENU_PRINCIPAL)) {
            idItemMenu = R.layout.itemmenu3;
        } else {
            idItemMenu = R.layout.itemmenu;
        }

        View view= LayoutInflater.from(parent.getContext()).inflate(idItemMenu, parent, false);

        return new ViewHolder2(view, context, menuItemsModeloList, tipoLayout, this);
    }

    @Override
    public void onBindViewHolder(NewAdapterMenus.ViewHolder2 holder, int position) {
        holder.textoItemMenu.setText(menuItemsModeloList.get(position).getTextoItem());
        if (tipoMenu.equals(DefinesBANCARD.MENU_PRINCIPAL)) {
            holder.logoItem.setImageDrawable(context.getResources().getDrawable(menuItemsModeloList.get(position).getImgItemMenu()));
        }
    }

    @Override
    public int getItemCount() {
        return menuItemsModeloList.size();
    }

    public static class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textoItemMenu;
        ImageView logoItem;
        List <menuItemsModelo> menuItems = new ArrayList<>();
        Context ctx;
        String tipoLayout;
        String tipoMenu;
        ArrayList<Drawable> drawables;
        NewAdapterMenus adapter;

        public ViewHolder2(View itemView, Context ctx, List<menuItemsModelo> menuItems, String tipoLayout, NewAdapterMenus adapter) {
            super(itemView);
            this.ctx = ctx;
            this.menuItems = menuItems;
            this.tipoLayout = tipoLayout;
            this.adapter = adapter;
            itemView.setOnClickListener(this);
            textoItemMenu =(TextView)itemView.findViewById(R.id.tvItemMenuTrans);
            logoItem= (ImageView)itemView.findViewById(R.id.imgItemTrans);
            drawables = new ArrayList<>();
        }

        @Override
        public void onClick(View view) {
            final OnItemClickListenerMesas listener = adapter.getOnItemClickListener();
            int id = view.getId();
            if (listener != null) {
                listener.onItemClick(this, getAdapterPosition(), id);
            }
        }
    }
}
