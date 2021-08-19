package com.cobranzas.menus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdaptadorMenu extends RecyclerView.Adapter<RecyclerViewAdaptadorMenu.ViewHolder> {

    public List<menuItemsModelo> menuItemsModeloList;
    Context context;
    String tipoLayout;
    private String tipoMenu;
    private Drawable drawable;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textoItemMenu;
        ImageView logoItem;
        List<menuItemsModelo> menuItems;
        Context ctx;
        String tipoLayout;
        ArrayList<Drawable> drawables;

        public ViewHolder(View itemView, Context ctx, List<menuItemsModelo> menuItems, String tipoLayout) {
            super(itemView);
            this.ctx = ctx;
            this.menuItems = menuItems;
            this.tipoLayout = tipoLayout;
            itemView.setOnClickListener(this);
            if (tipoLayout.equals("LINEAR")) {

                textoItemMenu = (TextView) itemView.findViewById(R.id.tvItemMenuTrans);
                logoItem = (ImageView) itemView.findViewById(R.id.imgItemTrans);
            } else {
                textoItemMenu = (TextView) itemView.findViewById(R.id.tvItemGrid);
                logoItem = (ImageView) itemView.findViewById(R.id.imgItemGrid);
            }
            drawables = new ArrayList<>();
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            menuItemsModelo menuItemModelo = this.menuItems.get(position);
            MenuAction menuAction = new MenuAction(ctx, menuItemModelo.getTextoItem());
            menuAction.selectAction();
        }
    }

    public RecyclerViewAdaptadorMenu(List<menuItemsModelo> menuItemsModeloList, Context context, String tipoLayout) {
        this.menuItemsModeloList = menuItemsModeloList;
        this.context = context;
        this.tipoLayout = tipoLayout;
    }

    public void setTipoMenu(String s) {
        this.tipoMenu = s;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int idItemMenu;

        if (tipoLayout.equals(DefinesBANCARD.TIPO_LAYOUT_LINEAR)) {
            if (tipoMenu.equals(DefinesBANCARD.MENU_PRINCIPAL)) {
                idItemMenu = R.layout.itemmenu3;
            } else {
                idItemMenu = R.layout.itemmenu;
            }
        } else {
            idItemMenu = R.layout.itemmenugrid;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(idItemMenu, parent, false);
        ViewHolder viewHolder;
        viewHolder = new ViewHolder(view, context, menuItemsModeloList, tipoLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textoItemMenu.setText(menuItemsModeloList.get(position).getTextoItem());
        if (tipoMenu.equals(DefinesBANCARD.MENU_PRINCIPAL)) {
            holder.logoItem.setImageDrawable(context.getResources().getDrawable(menuItemsModeloList.get(position).getImgItemMenu()));
        }
    }

    @Override
    public int getItemCount() {
        return menuItemsModeloList.size();
    }
}

