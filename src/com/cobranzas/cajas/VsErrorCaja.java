package com.cobranzas.cajas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cobranzas.timertransacciones.TimerTrans;
import com.cobranzas.actividades.MainActivity;
import com.wposs.cobranzas.R;

import cn.desert.newpos.payui.master.FormularioActivity;

public class VsErrorCaja extends FormularioActivity {

    String tagMensaje = "VsErrorCaja ";
    String metodoCounterDownTimer = "counterDownTimer: ";
    Context context = this;
    TimerTrans timerTrans;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_result_trans);

        mostrarSerialvsVersion();
        Bundle extras = getIntent().getExtras();
        String mensaje = extras.getString("mensaje");
        boolean codeStatus = extras.getBoolean("codeStatus");
        tipoMensaje(mensaje, codeStatus);

    }

    private void tipoMensaje(String mensaje, boolean codeStatus) {
        counterDownTimer(60000, "Tiempo de espera de ingreso de datos agotado", "Clase VsErrorCaja");
        encenderPantalla(VsErrorCaja.this);

        ImageView imgView = findViewById(R.id.imgView);
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        TextView tvMensajeHost = findViewById(R.id.tvMensajeHost);
        LinearLayout linearOpciones = findViewById(R.id.linearOpciones);

        linearOpciones.setVisibility(View.GONE);


        if (codeStatus) {
            tvTitulo.setText("SERVICIO ACTIVO");
            imgView.setImageDrawable(getDrawable(R.drawable.transaccion_aceptada));
        } else {
            tvTitulo.setText("TRANSACCIÓN RECHAZADA");
            imgView.setImageDrawable(getDrawable(R.drawable.transaccion_fallida));
        }
        tvMensajeHost.setText(mensaje);


        ImageView ivClose = (ImageView) findViewById(R.id.iv_close);
        ivClose.setVisibility(View.VISIBLE);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerTrans.deleteTimer();
                Intent intent = new Intent(VsErrorCaja.this, MainActivity.class);
                context.startActivity(intent);
                finish();
            }
        });

    }

    private void encenderPantalla(final Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void counterDownTimer(final int timeout, final String mensaje, final String metodo) {
        Log.e(tagMensaje, metodoCounterDownTimer + "Ingreso ");
        timerTrans.getInstanceTimerTrans(timeout, mensaje, metodo, new TimerTrans.OnResultTimer() {
            @Override
            public void rsp2Timer() {
                Log.e(tagMensaje, metodoCounterDownTimer + " Finalizado ");
                Intent intent = new Intent(VsErrorCaja.this, MainActivity.class);
                context.startActivity(intent);
                finish();
            }
        });

    }


    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

}
