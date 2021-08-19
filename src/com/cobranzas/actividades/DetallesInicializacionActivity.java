package com.cobranzas.actividades;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cobranzas.adaptadores.AdaptadorDetalles;
import com.cobranzas.model.ModelSetting;
import com.cobranzas.setting.ListSetting;
import com.wposs.cobranzas.R;

import java.util.List;

import cn.desert.newpos.payui.master.FormularioActivity;

public class DetallesInicializacionActivity extends FormularioActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        initToolbar();

        cargarInformacioRecyclerView();
        mostrarSerialvsVersion();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void cargarInformacioRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rcy);
        inicializarRecyclerView(this, recyclerView);
        List<ModelSetting> listadoInicializacion = ListSetting.getInstanceListDetalles();

        AdaptadorDetalles adaptadorSetting = new AdaptadorDetalles(this, listadoInicializacion);
        recyclerView.setAdapter(adaptadorSetting);
    }

    private void inicializarRecyclerView(Context context, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }


    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DetallesInicializacionActivity.this, ConfiguracionTecnicoActivity.class));
            }
        });
    }
}