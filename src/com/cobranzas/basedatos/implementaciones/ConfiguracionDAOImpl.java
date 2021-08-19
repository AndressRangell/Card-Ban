package com.cobranzas.basedatos.implementaciones;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;

import com.cobranzas.basedatos.ConexionSQLite;
import com.cobranzas.basedatos.ModelConfiguracion;
import com.cobranzas.basedatos.interfaces.ConfiguracionDAO;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

public class ConfiguracionDAOImpl extends ConexionSQLite implements ConfiguracionDAO {


    public ConfiguracionDAOImpl(Context context) {
        super(context);
    }

    String clase = "ConfiguracionDAOIlmp.java";

    @Override
    public boolean ingresarRegistro(ModelConfiguracion modelConfiguracion) {

        boolean ret = false;
        conexionSQlite = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (modelConfiguracion.getIpPrincipalConfig() != null) {
            values.put(columnIpPrimaria, modelConfiguracion.getIpPrincipalConfig());
        }

        if (modelConfiguracion.getPortPrincipalConfig() != 0) {
            values.put(columnPuertoPrimaria, modelConfiguracion.getPortPrincipalConfig());
        }

        if (modelConfiguracion.getIpSecundariaConfig() != null) {
            values.put(columnIpSecundaria, modelConfiguracion.getIpSecundariaConfig());
        }

        if (modelConfiguracion.getPortSecundarioConfig() != 0) {
            values.put(columnPuertoSecundaria, modelConfiguracion.getPortSecundarioConfig());
        }

        if (modelConfiguracion.getTimerConfig() != 0) {
            values.put(columnTimeout, modelConfiguracion.getTimerConfig());
        }

        if (modelConfiguracion.getTimerDataConfig() != 0) {
            values.put(columnTimeoutData, modelConfiguracion.getTimerDataConfig());
        }

        if (modelConfiguracion.getNiiConfig() != null) {
            values.put(columnNii, modelConfiguracion.getNiiConfig());
        }

        try {
            conexionSQlite.insert(tableTmconfig, null, values);
            conexionSQlite.close();
            ret = true;
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.getCause();
        }

        return ret;
    }
}
