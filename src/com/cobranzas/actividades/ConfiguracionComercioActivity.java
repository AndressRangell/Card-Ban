package com.cobranzas.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.wposs.cobranzas.R;
import com.cobranzas.adaptadores.Adaptador;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.menus.MenusActivity;

import java.util.ArrayList;
import java.util.List;

import cn.desert.newpos.payui.master.FormularioActivity;

public class ConfiguracionComercioActivity extends FormularioActivity {

    TextView tvTitulo;
    RecyclerView rvMenus;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initToolbar();

        tvTitulo = findViewById(R.id.tvTitulo);
        rvMenus = findViewById(R.id.rvMenus);

        tvTitulo.setText("Configuración comercio");
        cargarMenus();
        mostrarSerialvsVersion();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion,tvSerial);
    }
    private void cargarMenus() {
        rvMenus.setLayoutManager(new GridLayoutManager(ConfiguracionComercioActivity.this, 3));
        rvMenus.setHasFixedSize(true);
        rvMenus.setNestedScrollingEnabled(false);

        List<ModeloBotones> modePlansList = new ArrayList<>();
        modePlansList.add(new ModeloBotones("1", DefinesBANCARD.ITEM_CONFIG_WIFI, getDrawable(R.drawable.ic__cierre)));
        Adaptador adaptador = new Adaptador(ConfiguracionComercioActivity.this, modePlansList);
        adaptador.setLayout(R.layout.item_menu_);
        rvMenus.setAdapter(adaptador);
        adaptador.setOnItemClickListener(onItemClickListener);
    }

    Adaptador.OnItemClickListener onItemClickListener = new Adaptador.OnItemClickListener() {
        @Override
        public void onItemClick(View view, ModeloBotones obj, int position) {
            if (DefinesBANCARD.ITEM_CONFIG_WIFI.equals(obj.getNombreBoton())) {
                startActivity(new Intent(ConfiguracionComercioActivity.this, ConfiWifiDirectoAcitvity.class));
            }
        }
    };

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ConfiguracionComercioActivity.this, MenusActivity.class));
            }
        });
    }
}
