package com.cobranzas.inicializacion.configuracioncomercio;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import static com.cobranzas.actividades.StartAppBANCARD.tablaHost;
import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;

public class HOST {

    private static final String NAME_HOST_ID = "HOST_ID";
    private static final String NAME_REINTENTOS = "REINTENTOS";
    private static final String NAME_TIEMPO_ESPERA_CONEXION = "TIEMPO_ESPERA_CONEXION";
    private static final String NAME_TIEMPO_ESPERA_RESPUESTA = "TIEMPO_ESPERA_RESPUESTA";
    private static final String NAME_IP_ID_1 = "IP_ID_1";
    private static final String NAME_IP_ID_2 = "IP_ID_2";
    private static final String NAME_IP_ID_3 = "IP_ID_3";
    private static final String NAME_IP_ID_4 = "IP_ID_4";
    private static final String NAME_IP_ID_5 = "IP_ID_5";
    private static final String NAME_IP_ID_6 = "IP_ID_6";
    private static final String NAME_IP_ID_7 = "IP_ID_7";
    private static final String NAME_IP_ID_8 = "IP_ID_8";
    private static final String NAME_IP_ID_9 = "IP_ID_9";
    private static final String NAME_IP_ID_10 = "IP_ID_10";
    private static final String NAME_TIEMPO_CONEXION_3G = "TIEMPO_CONEXION_3G";
    private static final String NAME_TABLE = "HOST";

    protected static String[] fields = new String[]{
            NAME_HOST_ID,
            NAME_REINTENTOS,
            NAME_TIEMPO_ESPERA_CONEXION,
            NAME_TIEMPO_ESPERA_RESPUESTA,
            NAME_IP_ID_1,
            NAME_IP_ID_2,
            NAME_IP_ID_3,
            NAME_IP_ID_4,
            NAME_IP_ID_5,
            NAME_IP_ID_6,
            NAME_IP_ID_7,
            NAME_IP_ID_8,
            NAME_IP_ID_9,
            NAME_IP_ID_10,
            NAME_TIEMPO_CONEXION_3G,
    };
    String clase = "HOST.java";
    String ipId1;
    private String hostId;
    private String reintentos;
    private String tiempoEsperaConexion;
    private String tiempoEsperaRespuesta;
    private String ipId2;
    private String ipId3;
    private String ipId4;
    private String ipId5;
    private String ipId6;
    private String ipId7;
    private String ipId8;
    private String ipId9;
    private String ipId10;
    private String tiempoConexion3G;

    public static HOST getSingletonInstance() {
        if (tablaHost == null) {
            tablaHost = new HOST();
        } else {
            Log.d("Host_Confi", "No se puede crear otro objeto, ya existe");
        }
        return tablaHost;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getReintentos() {
        return reintentos;
    }

    public void setReintentos(String reintentos) {
        this.reintentos = reintentos;
    }

    public String getTiempoEsperaConexion() {
        return tiempoEsperaConexion;
    }

    public void setTiempoEsperaConexion(String tiempoEsperaConexion) {
        this.tiempoEsperaConexion = tiempoEsperaConexion;
    }

    public String getTiempoEsperaRespuesta() {
        return tiempoEsperaRespuesta;
    }

    public void setTiempoEsperaRespuesta(String tiempoEsperaRespuesta) {
        this.tiempoEsperaRespuesta = tiempoEsperaRespuesta;
    }

    public void setIpId1(String ipId1) {
        this.ipId1 = ipId1;
    }

    public String getIpId2() {
        return ipId2;
    }

    public void setIpId2(String ipId2) {
        this.ipId2 = ipId2;
    }

    public String getIpId3() {
        return ipId3;
    }

    public void setIpId3(String ipId3) {
        this.ipId3 = ipId3;
    }

    public String getIpId4() {
        return ipId4;
    }

    public void setIpId4(String ipId4) {
        this.ipId4 = ipId4;
    }

    public String getIpId5() {
        return ipId5;
    }

    public void setIpId5(String ipId5) {
        this.ipId5 = ipId5;
    }

    public String getIpId6() {
        return ipId6;
    }

    public void setIpId6(String ipId6) {
        this.ipId6 = ipId6;
    }

    public String getIpId7() {
        return ipId7;
    }

    public void setIpId7(String ipId7) {
        this.ipId7 = ipId7;
    }

    public String getIpId8() {
        return ipId8;
    }

    public void setIpId8(String ipId8) {
        this.ipId8 = ipId8;
    }

    public String getIpId9() {
        return ipId9;
    }

    public void setIpId9(String ipId9) {
        this.ipId9 = ipId9;
    }

    public String getIpId10() {
        return ipId10;
    }

    public void setIpId10(String ipId10) {
        this.ipId10 = ipId10;
    }

    public String getTiempoConexion3G() {
        return tiempoConexion3G;
    }

    public void setTiempoConexion3G(String tiempoConexion3G) {
        this.tiempoConexion3G = tiempoConexion3G;
    }

    private void setHostConfi(String column, String value) {
        switch (column) {
            case NAME_HOST_ID:
                setHostId(value);
                break;
            case NAME_REINTENTOS:
                setReintentos(value);
                break;
            case NAME_TIEMPO_ESPERA_CONEXION:
                setTiempoEsperaConexion(value);
                break;
            case NAME_TIEMPO_ESPERA_RESPUESTA:
                setTiempoEsperaRespuesta(value);
                break;
            case NAME_IP_ID_1:
                setIpId1(value);
                break;
            case NAME_IP_ID_2:
                setIpId2(value);
                break;
            case NAME_IP_ID_3:
                setIpId3(value);
                break;
            case NAME_IP_ID_4:
                setIpId4(value);
                break;
            case NAME_IP_ID_5:
                setIpId5(value);
                break;
            case NAME_IP_ID_6:
                setIpId6(value);
                break;
            case NAME_IP_ID_7:
                setIpId7(value);
                break;
            case NAME_IP_ID_8:
                setIpId8(value);
                break;
            case NAME_IP_ID_9:
                setIpId9(value);
                break;
            case NAME_IP_ID_10:
                setIpId10(value);
                break;
            case NAME_TIEMPO_CONEXION_3G:
                setTiempoConexion3G(value);
                break;
            default:
                break;
        }
    }

    public void clearHostConfig() {
        for (String s : fields) {
            setHostConfi(s, "");
        }
    }

    public boolean selectHostConfig(Context context) {
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        String sql = consultaSQL();

        try {
            Cursor cursor = databaseAccess.rawQuery(sql, null);
            cursor.moveToFirst();
            int indexColumn;
            while (!cursor.isAfterLast()) {
                clearHostConfig();
                indexColumn = 0;
                for (String s : fields) {
                    setHostConfi(s, cursor.getString(indexColumn++).trim());
                }
                cursor.moveToNext();
            }
            cursor.close();

        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.debug(e.getMessage());
            return false;
        }
        databaseAccess.closeDb();
        return true;
    }

    private String consultaSQL() {
        StringBuilder sql = new StringBuilder();

        sql.append("select ");
        int counter = 1;
        for (String s : fields) {
            sql.append(s);
            if (counter++ < fields.length) {
                sql.append(",");
            }
        }
        sql.append(" from " + NAME_TABLE);
        return sql.toString();
    }

    private void setContentValue(String column) {
        switch (column) {
            case NAME_HOST_ID:
            case NAME_REINTENTOS:
            case NAME_TIEMPO_ESPERA_CONEXION:
            case NAME_TIEMPO_ESPERA_RESPUESTA:
            case NAME_IP_ID_1:
            case NAME_IP_ID_2:
            case NAME_IP_ID_3:
            case NAME_IP_ID_4:
            case NAME_IP_ID_5:
            case NAME_IP_ID_6:
            case NAME_IP_ID_7:
            case NAME_IP_ID_8:
            case NAME_IP_ID_9:
            case NAME_IP_ID_10:
            case NAME_TIEMPO_CONEXION_3G:
            default:
                break;
        }
    }
}
