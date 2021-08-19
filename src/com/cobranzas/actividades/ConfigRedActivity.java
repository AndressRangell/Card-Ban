package com.cobranzas.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cobranzas.adaptadores.Adaptador;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.IPS;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import cn.desert.newpos.payui.master.FormularioActivity;

public class ConfigRedActivity extends FormularioActivity {

    RecyclerView recyclerView;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_red);

        cargarListadoConexiones();
        initToolbar();
        mostrarSerialvsVersion();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void cargarListadoConexiones() {
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(ConfigRedActivity.this, 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        List<ModeloBotones> listIp = new ArrayList<>();
        IPS nIP;
        for (int i = 0; i < ChequeoIPs.getLengIps(); i++) {
            nIP = ChequeoIPs.seleccioneIP(i);
            String cod = String.valueOf(i);
            ModeloBotones model = new ModeloBotones();
            model.codBoton = cod;
            model.nombreBoton = nIP.getIdIp();
            model.imageDrw = getDrawable(R.drawable.ic_inicializacion);
            listIp.add(model);
        }


        Adaptador adaptador = new Adaptador(ConfigRedActivity.this, eliminarRepetidos(listIp));
        adaptador.setLayout(R.layout.item_menu_);
        recyclerView.setAdapter(adaptador);
        adaptador.setOnItemClickListener(onItemClickListener);

    }

    private ArrayList<ModeloBotones> eliminarRepetidos(List<ModeloBotones> resultados) {
        TreeMap<String, ModeloBotones> map = new TreeMap<>();
        for (ModeloBotones item : resultados) {
            map.put(item.getNombreBoton(), item);
        }
        resultados.clear();
        resultados.addAll(map.values());
        return new ArrayList<>(resultados);
    }

    Adaptador.OnItemClickListener onItemClickListener = new Adaptador.OnItemClickListener() {
        @Override
        public void onItemClick(View view, ModeloBotones obj, int position) {
            int cod = Integer.parseInt(obj.getCodBoton());
            Logger.debug("cod = " + cod);
            Intent intent = new Intent();
            intent.setClass(ConfigRedActivity.this, ConectarPorDominioActivity.class);
            intent.putExtra("CodigoIP", cod);
            startActivity(intent);
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
                startActivity(new Intent(ConfigRedActivity.this, ConfiguracionTecnicoActivity.class));
            }
        });
    }
}
