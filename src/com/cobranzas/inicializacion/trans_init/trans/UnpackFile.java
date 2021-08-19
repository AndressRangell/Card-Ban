package com.cobranzas.inicializacion.trans_init.trans;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cn.desert.newpos.payui.UIUtils;

import static java.lang.Thread.sleep;

/**
 * Created by Technology&Solutions on 31/05/2017.
 */

public class UnpackFile extends AsyncTask<String, Integer, Boolean> {

    String clase = "UnpackFile.java";
    private final ProgressDialog mProgresoDescompresion;
    private final String ubicacionZip;
    private final String destinoDescompresion;
    private final boolean mantenerZip;
    private final FileCallback callback;
    private final boolean ponerLaT;

    /**
     * Descomprime un archivo .ZIP
     *
     * @param ctx       Contexto de la Aplicación Android
     * @param ubicacion Ruta ABSOLUTA de un archivo .zip
     * @param destino   Ruta ABSOLUTA del destino de la descompresión. Finalizar con /
     * @param mantener  Indica si se debe mantener el archivo ZIP despues de descomprimir
     */

    public UnpackFile(Context ctx, String ubicacion, String destino, boolean ponerLaT, boolean mantener, final FileCallback callback) {
        this.ubicacionZip = ubicacion;
        this.destinoDescompresion = destino;
        this.mantenerZip = mantener;
        this.callback = callback;
        this.ponerLaT = ponerLaT;
        this.mProgresoDescompresion = new ProgressDialog(ctx, R.style.Mypd);
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        this.mProgresoDescompresion.setCancelable(false);
        this.mProgresoDescompresion.show();
        this.mProgresoDescompresion.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.mProgresoDescompresion.setContentView(R.layout.progress_inicializacion);
        TextView textView = mProgresoDescompresion.findViewById(R.id.Texto);
        textView.setText("Inicializando POS");
        mostrarSerialvsVersion(mProgresoDescompresion);
    }

    private void mostrarSerialvsVersion(ProgressDialog pd) {
        TextView tvVersion = pd.findViewById(R.id.tvVersion);
        TextView tvSerial = pd.findViewById(R.id.tvSerial);
        UIUtils.mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    @Override
    protected Boolean doInBackground(String... params) {

        int size;
        byte[] buffer = new byte[2048];

        new File(destinoDescompresion).mkdirs(); //Crea la ruta de descompresion si no existe

        try {
            try {
                FileInputStream lectorArchivo = new FileInputStream(destinoDescompresion + ubicacionZip);
                ZipInputStream lectorZip = new ZipInputStream(lectorArchivo);
                ZipEntry itemZip = null;

                while ((itemZip = lectorZip.getNextEntry()) != null) {
                    Log.v("UnpackFile", "Descomprimiendo " + itemZip.getName());

                    try {
                        if (itemZip.isDirectory()) { //Si el elemento es un directorio, crearlo
                            creaCarpetas(itemZip.getName(), destinoDescompresion);
                        } else {
                            FileOutputStream outStream;
                            if (ponerLaT) {
                                if (itemZip.getName().endsWith(".bin") || itemZip.getName().endsWith(".BIN")) {
                                    outStream = new FileOutputStream(destinoDescompresion + itemZip.getName());
                                } else {
                                    outStream = new FileOutputStream(destinoDescompresion + itemZip.getName() + "T");

                                }
                            } else {
                                outStream = new FileOutputStream(destinoDescompresion + itemZip.getName());
                            }
                            BufferedOutputStream bufferOut = new BufferedOutputStream(outStream, buffer.length);

                            while ((size = lectorZip.read(buffer, 0, buffer.length)) != -1) {
                                bufferOut.write(buffer, 0, size);
                            }

                            bufferOut.flush();
                            bufferOut.close();
                        }
                    } catch (FileNotFoundException e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        Log.d("ERROR", "doInBackground: " + e.getMessage());
                    }
                }
                lectorZip.close();
                lectorArchivo.close();

                //Conservar archvi .zip
                if (!mantenerZip)
                    new File(ubicacionZip).delete();

                //Espera para poder realizar las validaciones por cada ciclo
                sleep(5000);
                return true;
            } catch (Exception e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Log.e("UnpackFile", "Descomprimir", e);
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Log.e("Error", e.getMessage());
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (mProgresoDescompresion != null)
            this.mProgresoDescompresion.dismiss();
        //callback
        callback.rspUnpack(aBoolean);
    }

    /**
     * Crea la carpeta donde seran almacenados los archivos del .zip
     *
     * @param dir
     * @param location
     */

    private void creaCarpetas(String dir, String location) {
        File f = new File(location + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    public interface FileCallback {
        boolean rspUnpack(boolean okUnpack);
    }
}
