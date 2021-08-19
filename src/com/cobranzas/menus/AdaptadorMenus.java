package com.cobranzas.menus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.cobranzas.adaptadores.ModeloMenusOpciones;
import com.wposs.cobranzas.R;

import java.util.ArrayList;

public class AdaptadorMenus extends RecyclerView.Adapter<AdaptadorMenus.ViewHolder> {

    ArrayList<ModeloMenusOpciones> menuList;
    Context context;
    MenusCallback callback;

    public void setCallback(MenusCallback callback) {
        this.callback = callback;
    }

    public AdaptadorMenus(Context context, ArrayList<ModeloMenusOpciones> menuList) {
        this.context = context;
        this.menuList = menuList;
    }

    public interface MenusCallback {
        void onMenuClick(ModeloMenusOpciones opcion);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_imgbutton, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (holder instanceof ViewHolder) {
            if (menuList.get(position).isHabilitarTransaccion()) {
                holder.menuOpcion.setImageDrawable(menuList.get(position).getIcono());
                holder.menuOpcion.setVisibility(View.VISIBLE);
                holder.menuOpcion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onMenuClick(menuList.get(position));
                    }
                });
            } else {
                holder.menuOpcion.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton menuOpcion;

        public ViewHolder(View itemView) {
            super(itemView);
            menuOpcion = itemView.findViewById(R.id.menuOpcion);
        }
    }
}

