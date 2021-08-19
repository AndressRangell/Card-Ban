package com.cobranzas.inicializacion.configuracioncomercio;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.wposs.cobranzas.R;

import java.util.ArrayList;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class PLANES {

    private String plantillaId;
    private String planId;
    private boolean activar;
    private String descripcion;
    private String etiqueta;
    private Context context;

    public PLANES(Context context) {
        this.context = context;
    }

    public PLANES(String plantillaId, String planId, boolean activar, String descripcion, String etiqueta) {
        this.plantillaId = plantillaId;
        this.planId = planId;
        this.activar = activar;
        this.descripcion = descripcion;
        this.etiqueta = etiqueta;
    }

    public static ArrayList<PLANES> getAllPlanes(Context context) {
        ArrayList<PLANES> list = new ArrayList<>();
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);
        String query = "SELECT * FROM PLANES";
        Cursor cursor = databaseAccess.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                PLANES planes = new PLANES(context);
                planes.setPlantillaId(cursor.getString(0).trim());
                planes.setPlanId(cursor.getString(1).trim());
                planes.setActivar(cursor.getString(2).trim());
                planes.setDescripcion(cursor.getString(3).trim());
                planes.setEtiqueta(cursor.getString(4).replaceFirst("^0*", "").trim());

                list.add(planes);
            } while (cursor.moveToNext());
        }
        cursor.close();
        databaseAccess.close();
        return list;
    }

    public String getPlantillaId() {
        return plantillaId;
    }

    public void setPlantillaId(String plantillaId) {
        this.plantillaId = plantillaId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public boolean isActivar() {
        return activar;
    }

    public void setActivar(String activar) {
        if (!activar.isEmpty()) {
            if (activar.equals("true") || activar.equals("false")) {
                this.activar = Boolean.parseBoolean(activar);
            } else {
                showMensaje(" Error en la obtención de los Planes");
                this.activar = false;
            }
        }
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    private void showMensaje(String mensaje) {
        if (context != null)
            UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, mensaje, Toast.LENGTH_LONG);
        else
            Log.d("Info", "showMensaje: " + "context == null "+mensaje);
    }
}
