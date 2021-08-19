package com.cobranzas.inicializacion.configuracioncomercio;

import android.content.Context;
import android.database.Cursor;

import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class Tareas {

    List<Tareas> listadoTareas;
    String tarea;
    String aplicacionId;
    String aplicacion;
    String codigoTarea;
    String descripcion;
    String clase ="Tareas.java";

    static Tareas tablaTareas;

    String tablaBaseDatosTarea = "tareas";
    private static final String COLUMNATAREA = "tarea";
    private static final String COLUMNA_APLICACIONID = "aplicacionid";
    private static final String COLUMNA_APLICACION = "aplicacion";
    private static final String COLUMNA_CODIGOTAREA = "codigotarea";
    private static final String COLUMNA_DESCRIPCION = "descripcion";

    String[] listadoColumnasSQL = new String[]{
            COLUMNATAREA,
            COLUMNA_APLICACIONID,
            COLUMNA_APLICACION,
            COLUMNA_CODIGOTAREA,
            COLUMNA_DESCRIPCION
    };

    private String consultaSQL(String[] listadoColumnasSQL) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        for (int i = 0; i < listadoColumnasSQL.length; i++) {
            sql.append(listadoColumnasSQL[i]);
            if (i < (listadoColumnasSQL.length - 1)) {
                sql.append(", ");
            }
        }
        sql.append(" FROM ");
        sql.append(tablaBaseDatosTarea);

        return sql.toString();
    }

    public boolean inicializandoComponentes(Context context) {

        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        String sql = consultaSQL(listadoColumnasSQL);

        try {
            Cursor cursor = databaseAccess.rawQuery(sql, null);
            listadoTareas = new ArrayList<>();
            cursor.moveToFirst();
            int indexColumn;

            while (!cursor.isAfterLast()) {
               Tareas tareas = new Tareas();
                tareas.clearAPP();
                indexColumn = 0;
                for (String s : listadoColumnasSQL) {
                    tareas.setAPP(s, cursor.getString(indexColumn++).trim());
                }
                cursor.moveToNext();
                listadoTareas.add(tareas);
            }
            cursor.close();
            return true;
        } catch (Exception ex) {
            Logger.logLine(LogType.EXCEPTION, clase, ex.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, ex.getStackTrace());
            ex.printStackTrace();
            Logger.debug(ex.getMessage());
        }
        databaseAccess.closeDb();
        return false;
    }

    public static Tareas getInstance(boolean isBorrarInfo) {
        if (isBorrarInfo) {
            tablaTareas = null;
        }
        if (tablaTareas == null) {
            tablaTareas = new Tareas();
        }
        return tablaTareas;
    }

    public void clearAPP() {
        for (String s : APLICACIONES.fields) {
            setAPP(s, "");
        }
    }

    private void setAPP(String column, String value) {
        switch (column) {
            case COLUMNATAREA:
                setTarea(value);
                break;
            case COLUMNA_APLICACIONID:
                setAplicacionId(value);
                break;
            case COLUMNA_APLICACION:
                setAplicacion(value);
                break;
            case COLUMNA_CODIGOTAREA:
                setCodigoTarea(value);
                break;
            case COLUMNA_DESCRIPCION:
                setDescripcion(value);
                break;
            default:
                Logger.debug("Case invalid");
                break;
        }
    }

    public List<Tareas> getListadoTarea2Aplicacion(String nombreAplicacion) {
        List<Tareas> tareasList = new ArrayList<>();
        if (listadoTareas != null){
            for (Tareas tareas : listadoTareas) {
                if (tareas != null && !tareas.getAplicacion().isEmpty() && tareas.getAplicacion().contains(nombreAplicacion)){
                    tareasList.add(tareas);
                }
            }
        }
        return tareasList;
    }

    public String getTarea() {
        return tarea;
    }

    private void setTarea(String tarea) {
        this.tarea = tarea;
    }

    public String getAplicacionId() {
        return aplicacionId;
    }

    private void setAplicacionId(String aplicacionId) {
        this.aplicacionId = aplicacionId;
    }

    public String getAplicacion() {
        return aplicacion;
    }

    private void setAplicacion(String aplicacion) {
        this.aplicacion = aplicacion;
    }

    public String getCodigoTarea() {
        return codigoTarea;
    }

    private void setCodigoTarea(String codigoTarea) {
        this.codigoTarea = codigoTarea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    private void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}