package com.cobranzas.basedatos.migracion;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;
import com.wposs.cobranzas.R;

import java.io.File;

import cn.desert.newpos.payui.UIUtils;

public class MigraccionBaseDatos extends AsyncTask<Void, Void, Void> {


    String clase = "MigraccionBaseDatos.java";
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    TextView textProgressBar;
    int timerOut = 32;
    Context context;

    ProcederMigracion procederMigracion;

    public MigraccionBaseDatos(Context context, ProcederMigracion procederMigracion) {
        this.context = context;
        this.procederMigracion = procederMigracion;
    }

    public interface ProcederMigracion {
        void rspProcesoTerminadoMigracion(boolean isContinuarIncializacion);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            progressDialog = new ProgressDialog(context);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
            this.progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            this.progressDialog.setContentView(R.layout.proceso_migracion_inicializacion);
            progressBar = progressDialog.findViewById(R.id.progressBar);
            textProgressBar = progressDialog.findViewById(R.id.Texto);
            mostrarSerialvsVersion(progressDialog);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Log.d("TAG", "onPreExecute: " + e.getMessage());
        }
    }


    private void mostrarSerialvsVersion(ProgressDialog pd) {
        TextView tvVersion = pd.findViewById(R.id.tvVersion);
        TextView tvSerial = pd.findViewById(R.id.tvSerial);
        UIUtils.mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        procesoMigracion((Activity) context);
        return null;
    }

    int i = 0;

    private void procesoMigracion(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    progressDialog.dismiss();
                    procederMigracion.rspProcesoTerminadoMigracion(true);
                }
        });
    }

    private void visualizarProceso(final TextView textProgressBar) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (i <= 100) {
                    progressBar.setProgress(i);
                    textProgressBar.setText("Actualizando Base de Datos... \n(" + i + " % )");
                    i++;
                    handler.postDelayed(this, timerOut);
                } else {
                    progressDialog.dismiss();
                    handler.removeCallbacks(this);
                    ISOUtil.showMensaje("Base de Datos Actualizada", context);
                    procederMigracion.rspProcesoTerminadoMigracion(true);
                }
            }
        }, timerOut);

    }


    private String checkNullZero(String data) {
        if (data == null || data.trim().equals("")) {
            return "0";
        } else {
            return data;
        }
    }

    private boolean checkDataBase(String myPath) {
        try {
            File file = new File(myPath);
            if (file.exists() && !file.isDirectory()) {
                SQLiteDatabase checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
                if (checkDB != null) {
                    checkDB.close();
                    return true;
                }
            }

        } catch (SQLiteException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        return false;
    }
}
