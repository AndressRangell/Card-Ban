package com.cobranzas.actividades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.inicializacion.configuracioncomercio.Tareas;
import com.cobranzas.inicializacion.trans_init.Init;
import com.cobranzas.inicializacion.trans_init.trans.ISO;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.trans.translog.TransLog;
import com.wposs.cobranzas.R;

import java.io.File;
import java.util.List;

import Interactor.CasteoAplicacion.CasteoTransPackImpl;
import Interactor.CasteoAplicacion.SendRcdConfirmacion;
import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.FormularioActivity;

import static com.cobranzas.defines_bancard.DefinesBANCARD.POLARIS_APP_NAME;
import static com.cobranzas.defines_bancard.DefinesBANCARD.columnaEventosTareaRealizada;
import static com.cobranzas.defines_bancard.DefinesBANCARD.namePreferenciaEventosTareas;

public class CasteoAplicacionAtivity extends FormularioActivity {

    TextView textView;
    List<Tareas> tareasList;
    String clase = "CasteoAplicacionAtivity.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trans_handling);
        textView = findViewById(R.id.handing_msginfo);

        mostrarSerialvsVersion();
        try {
            verificandoTipoEvento();
        } catch (Exception exception) {
            Logger.logLine(LogType.EXCEPTION, clase, exception.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, exception.getStackTrace());
            exception.printStackTrace();
        }

    }

    private void verificandoTipoEvento() {

        tareasList = Tareas.getInstance(false).getListadoTarea2Aplicacion(POLARIS_APP_NAME);

        if (tareasList != null && !tareasList.isEmpty()) {
            textView.setText(tareasList.get(0).getDescripcion());

            if ("BORRAR CACHE APLICACION".equals(tareasList.get(0).getDescripcion())) {
                TransLog.clearReveral();
                TransLog.clearScriptResult();
                deleteCache(CasteoAplicacionAtivity.this);
            }

        }
    }

    public void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (deleteDir(dir)) {
                Toast.makeText(context, "Cache Borrado", Toast.LENGTH_SHORT).show();
                enviarTransaccionConfirmacion();
            } else {
                Toast.makeText(context, "Cache No Borrado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }


    void enviarTransaccionConfirmacion() {
        SendRcdConfirmacion sendRcdConfirmacion = new SendRcdConfirmacion(
                CasteoAplicacionAtivity.this,
                new CasteoTransPackImpl(this, tareasList), getResources().getString(R.string.ipConfigWifi),
                Integer.parseInt(getResources().getString(R.string.portConfig)),
                Integer.parseInt(getResources().getString(R.string.timerConfig)), new SendRcdConfirmacion.setEventoListener() {
            @Override
            public void onShowError(String mensaje) {
                UIUtils.toast(CasteoAplicacionAtivity.this, R.drawable.ic_cobranzas_blanca, mensaje, Toast.LENGTH_LONG);
            }

            @Override
            public void onShowSuccess(byte[] rspServidor) {
                if (rspServidor != null) {
                    unpackTrama(new ISO(rspServidor, ISO.lenghtNotInclude, ISO.TpduInclude));
                } else {
                    UIUtils.toast(CasteoAplicacionAtivity.this, R.drawable.ic_cobranzas_blanca, "ERROR EN TRANSACCION NO SE RECIBIO RESPUESTA DEL SERVIDOR", Toast.LENGTH_LONG);
                    intentActivity(MainActivity.class);
                }

            }
        });

        sendRcdConfirmacion.execute();
    }


    private void unpackTrama(ISO rspTx) {
        String rspCode = rspTx.GetField(ISO.field_39_RESPONSE_CODE);
        String mensaje = rspTx.GetField(ISO.field_60_RESERVED_PRIVATE);
        Log.d("CasteoAplicacionAtivity", "unpackTrama: rspCode " + rspCode);
        Log.d("CasteoAplicacionAtivity", "unpackTrama: mensaje " + mensaje);

        UIUtils.toast(CasteoAplicacionAtivity.this, R.drawable.ic_cobranzas_blanca, mensaje, Toast.LENGTH_LONG);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());

        }
        if (rspCode.equals("00")) {
            guardarConfirmacionTarea(true);
            intentActivity(Init.class);
        } else {
            intentActivity(MainActivity.class);
        }


    }

    void intentActivity(Class mainActivityClass) {
        tareasList.clear();
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(CasteoAplicacionAtivity.this, mainActivityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }


    private void guardarConfirmacionTarea(Boolean respuesta) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(namePreferenciaEventosTareas, MODE_PRIVATE).edit();
        editor.putBoolean(columnaEventosTareaRealizada, respuesta);
        editor.apply();
    }
}