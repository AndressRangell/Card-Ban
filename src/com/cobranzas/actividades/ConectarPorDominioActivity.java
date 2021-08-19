package com.cobranzas.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.IPS;
import com.newpos.libpay.device.printer.PrintRes;
import com.wposs.cobranzas.R;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.FormularioActivity;
import cn.desert.newpos.payui.master.MasterControl;

import static com.cobranzas.actividades.StartAppBANCARD.listadoIps;


public class ConectarPorDominioActivity extends FormularioActivity {

    TextView tvTitulo;
    EditText etIp;
    EditText etPort;
    Button btnResolverUrl;
    Button btnGuardar;
    Switch aSwitch;

    IPS ip;
    IPS nIP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conexion_url);

        int codIP = 0;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            codIP = bundle.getInt("CodigoIP");
        }

        nIP = ChequeoIPs.seleccioneIP(codIP);

        initToolbar();

        ip = IPS.getSingletonInstance(getApplicationContext());

        configuracionRed();

        mostrarSerialvsVersion();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersionLocal = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersionLocal, tvSerial);
    }


    private void configuracionRed() {
        tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText("Conexión " + nIP.getIdIp());
        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        aSwitch = findViewById(R.id.aSwitch);

        etIp.setText(nIP.getIP());
        etPort.setText(nIP.getPuerto());
        setChecked(nIP.isTls());


        btnGuardar = findViewById(R.id.btnGuardar);


        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarIP(etIp.getText().toString(), etPort.getText().toString());
            }
        });
    }


    private void actualizarIP(String editIp, String editPort) {
        if (!editIp.isEmpty() && !editPort.isEmpty()) {
            String[] data = new String[]{editIp, editPort, String.valueOf(aSwitch.isChecked())};
            if (ip.updateSelectIps(nIP.getIdIp(), IPS.fieldsEdit, data, this)) {
                UIUtils.toast(this, R.drawable.logoinfonet, "Conexión actualizada con exito", Toast.LENGTH_SHORT);
                listadoIps = ChequeoIPs.selectIP(ConectarPorDominioActivity.this);
                realizarEchoTest();
                finish();
            }
        } else {
            UIUtils.toast(this, R.drawable.logoinfonet, "Datos incompletos", Toast.LENGTH_SHORT);
        }
    }

    private void realizarEchoTest() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(ConectarPorDominioActivity.this, MasterControl.class);
        intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[20]);
        startActivity(intent);
    }

    private void initToolbar() {
        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void setChecked(Boolean oneOrZero) {
        aSwitch.setChecked(oneOrZero);
    }
}
