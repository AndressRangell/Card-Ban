package com.cobranzas.inicializacion.configuracioncomercio;

import android.content.Context;
import android.database.Cursor;

import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class Red {

    static Red tablaRed;
    String claveTecnico;
    String clase = "Red.java";
    String tablaBaseDatosRed = "RED";
    String columnaClaveTecnico = "clave_tecnico";

    String[] listadoColumnasSQL = new String[]{
            columnaClaveTecnico
    };

    public static Red getInstance(boolean isBorrarInfo) {
        if (isBorrarInfo) {
            tablaRed = null;
        }
        if (tablaRed == null) {
            tablaRed = new Red();
        }
        return tablaRed;
    }

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
        sql.append(tablaBaseDatosRed);
        return sql.toString();
    }

    public boolean inicializandoComponentes(Context context) {

        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        String sql = consultaSQL(listadoColumnasSQL);

        try {
            Cursor cursor = databaseAccess.rawQuery(sql, null);
            cursor.moveToFirst();
            int indexColumn;

            while (!cursor.isAfterLast()) {
                tablaRed = new Red();
                tablaRed.clearAPP();
                indexColumn = 0;
                for (String s : listadoColumnasSQL) {
                    tablaRed.setAPP(s, cursor.getString(indexColumn++).trim());
                }
                cursor.moveToNext();
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

    public void clearAPP() {
        for (String s : APLICACIONES.fields) {
            setAPP(s, "");
        }
    }

    private void setAPP(String column, String value) {
        if (columnaClaveTecnico.equals(column)) {
            setClaveTecnico(value);
        }
    }

    public String getClaveTecnico() {
        return claveTecnico;
    }

    public void setClaveTecnico(String claveTecnico) {
        this.claveTecnico = claveTecnico;
    }
}
