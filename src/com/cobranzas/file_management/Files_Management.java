package com.cobranzas.file_management;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.cobranzas.defines_bancard.DefinesBANCARD.NAME_FOLDER_CTL_FILES;

public class Files_Management {

    private static final String CLASE = "Files_Management.java";
    Context mContext;
    private String mfileData;

    public Files_Management(Context mContext) {
        this.mContext = mContext;
        this.mfileData = "";
    }

    /**
     * Realiza la lectura de los archivo de configuracion del CTL
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static byte[] readFileBin(String fileName, Context context) {

        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(NAME_FOLDER_CTL_FILES, Context.MODE_PRIVATE);
        File f = new File(directory, fileName);

        try {
            InputStream ios;
            ios = new FileInputStream(f);
            byte[] data = new byte[1 << 20];
            int length = 0;

            length = ios.read(data);
            ios.close();
            if (length != -1) {
                byte[] out = new byte[length];
                System.arraycopy(data, 0, out, 0, length);
                return out;
            } else {
                return new byte[0];
            }
        } catch (IOException e) {
            Logger.logLine(LogType.EXCEPTION, CLASE, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, CLASE, e.getStackTrace());
            e.printStackTrace();
            return new byte[0];
        }
    }

    public String getMfileData() {
        return mfileData;
    }

    public boolean readFile(String fileName) {
        boolean ret = false;
        String line = null;
        StringBuilder stringBuffer = new StringBuilder("");
        File rutaSd = Environment.getExternalStorageDirectory();
        File f = new File(rutaSd.getAbsolutePath(), fileName);
        try (BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(f)));) {
            while ((line = fin.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            mfileData = stringBuffer.toString();
            ret = true;
        } catch (Exception ex) {
            Logger.logLine(LogType.EXCEPTION, CLASE, ex.getMessage());
            Logger.logLine(LogType.EXCEPTION, CLASE, ex.getStackTrace());
            Log.e("Ficheros", "Error al leer archivo");
        }

        return ret;
    }
}
