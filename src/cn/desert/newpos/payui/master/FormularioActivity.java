package cn.desert.newpos.payui.master;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.pos.device.config.DevConfig;
import com.wposs.cobranzas.BuildConfig;

import java.io.File;

public class FormularioActivity extends AppCompatActivity {


    protected void mostrarSerialvsVersion(TextView tvVersion, TextView tvSerial) {
        tvVersion.setText(BuildConfig.VERSION_NAME);
        tvSerial.setText(formatSerial(DevConfig.getSN()));
    }

    private String formatSerial(String serial) {
        int espacio = 5;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < serial.length(); i += espacio) {
            if (i + espacio >= serial.length()) {
                result.append(serial.substring(i));
            } else {
                result.append(serial.substring(i, i + espacio)).append("-");
            }
        }
        return result.toString();
    }


    protected void inicializarRecyclerViewLinear(Context context, RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    protected void inicializarRecyclerViewGrid(Context context, RecyclerView recyclerView, int spanCount) {
        recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    protected void eliminarCache(Context context, String clase) {
        try {
            File dir = context.getCacheDir();
            if (dir.exists()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
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

    protected void eliminarDatos(File fileOrDirectory, String clase) {
        try {
            if (fileOrDirectory.exists()) {
                if (fileOrDirectory.isDirectory())
                    for (File child : fileOrDirectory.listFiles())
                        eliminarDatos(child, clase);
                fileOrDirectory.delete();
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
    }
}
