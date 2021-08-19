package com.cobranzas.actividades;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.tools.ConectarseRedWifi;
import com.wposs.cobranzas.R;


import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.FormularioActivity;

public class ConfiWifiDirectoAcitvity extends FormularioActivity {
    EditText editTextNombreRed;
    Button btnGuardar;
    TextInputEditText editTextClave;
    Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configwifi_directo);
        TextView tvTitulo;
        tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText("Redes Wifi");

        editTextNombreRed = findViewById(R.id.editTextNombreRed);
        editTextClave = findViewById(R.id.editTextClave);
        btnGuardar = findViewById(R.id.btnGuardar);
        initToolbar();

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.toast(ConfiWifiDirectoAcitvity.this, R.drawable.logoinfonet02, "Conectando a la red", Toast.LENGTH_LONG);
                String nombreRed = editTextNombreRed.getText().toString();
                String claveRed = editTextClave.getText().toString();
                ConectarseRedWifi.conectarRed(nombreRed, claveRed, ConfiWifiDirectoAcitvity.this);
            }
        });
        mostrarSerialvsVersion();

    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ConfiWifiDirectoAcitvity.this, ConfiguracionComercioActivity.class));
            }
        });
    }
}
