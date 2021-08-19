package com.cobranzas.inicializacion.init_emv;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;
import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.pos.device.emv.CAPublicKey;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVHandler;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;
import static org.jpos.stis.Util.hex2byte;
import static org.jpos.stis.Util.hexString;


/**
 * @author francisco
 */
public class CAPK_ROW {

    private String plantillaId;
    private String keyId;
    private String keyRid;
    private String keyExponent;
    private String keySize;
    private String keyModule;
    private String keyExpirationDate;
    private String keySha1;
    private static final String EMVINIT = "emvinit";

    String clase = "CAPK_ROW.java";

    private boolean showDebug = false;

    public static String[] fields = new String[]{
            "PLANTILLA_ID",
            "KEY_ID",
            "KEY_RID",
            "KEY_EXPONENT",
            "KEY_SIZE",
            "KEY_MODULE",
            "KEY_EXPIRATION_DATE",
            "KEY_SHA1"
    };

    public static String[] fieldsCAKEY = new String[]{
            "RID",
            "KeyIdx",
            "Exponent",
            "subType",
            "len",
            "KeySize",
            "Key"};

    private static CAPK_ROW capkRow;

    public void setCAPK_ROW(String column, String value) {
        switch (column) {
            case "PLANTILLA_ID":
                setPlantillaId(value);
                break;
            case "KEY_ID":
                setKeyId(value);
                break;
            case "KEY_RID":
                setKeyRid(value);
                break;
            case "KEY_EXPONENT":
                setKeyExponent(value);
                break;
            case "KEY_SIZE":
                setKeySize(value);
                break;
            case "KEY_MODULE":
                setKeyModule(value);
                break;
            case "KEY_EXPIRATION_DATE":
                setKeyExpirationDate(value);
                break;
            case "KEY_SHA1":
                setKeySha1(value);
                break;

            default:
                break;
        }
    }

    public boolean checkSigned(Context context) {
        StringBuilder textToVerify = new StringBuilder();
        textToVerify.append(this.plantillaId);
        textToVerify.append(this.keyId);
        textToVerify.append(this.keyRid);
        textToVerify.append(this.keyExponent);
        textToVerify.append(this.keySize);
        textToVerify.append(this.keyModule);
        textToVerify.append(this.keyExpirationDate);
        textToVerify.append(this.keySha1);
        return true;
    }

    public void clearCAPK_ROW() {
        for (String s : CAPK_ROW.fields) {
            setCAPK_ROW(s, "");
        }
    }

    public boolean selectCAPK_ROW(Context context) {
        boolean ok = false;
        dbHelper databaseAccess = new dbHelper(context, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);

        String sql = consultaSQL();

        try {
            Cursor cursor = databaseAccess.rawQuery(sql);
            cursor.moveToFirst();
            int indexColumn;
            IEMVHandler emvHandler = EMVHandler.getInstance();
            while (!cursor.isAfterLast()) {
                clearCAPK_ROW();
                indexColumn = 0;
                for (String s : CAPK_ROW.fields) {
                    setCAPK_ROW(s, cursor.getString(indexColumn++));
                }
                if (showDebug) {
                    Log.d(EMVINIT, "\n" + this.ToString());
                    Log.d(EMVINIT, "CAPK_ROW checkSigned: " + (this.checkSigned(context) ? "true" : "false"));
                    Logger.debug("");
                    Logger.debug("");
                    Logger.debug("");
                }
                try {
                    CAPublicKey caPublicKey = new CAPublicKey();
                    caPublicKey.setRID(hex2byte(this.getKeyRid()));
                    caPublicKey.setIndex(Integer.parseInt(this.getKeyId(), 16));

                    int moduleLength = Integer.parseInt(this.getKeySize(), 16);
                    caPublicKey.setLenOfModulus(moduleLength);
                    byte[] key = hex2byte(this.getKeyModule());
                    byte[] module = new byte[moduleLength];
                    System.arraycopy(key, 0, module, 0, moduleLength);
                    caPublicKey.setModulus(module);


                    byte[] exponent = getExp(hex2byte(this.getKeyExponent()));
                    if (caPublicKey != null && exponent != null) {
                        caPublicKey.setLenOfExponent(exponent.length);
                        caPublicKey.setExponent(exponent);
                    }
                    byte[] expDate = new byte[3];
                    byte[] date = hex2byte(this.getKeyExpirationDate());
                    byte[] lastDayMonth = lastDayOfMonth(date);
                    System.arraycopy(date, 1, expDate, 0, 2);
                    System.arraycopy(lastDayMonth, 0, expDate, 2, 1);
                    caPublicKey.setExpirationDate(expDate);

                    caPublicKey.setChecksum(hex2byte(this.getKeySha1()));

                    int rta = emvHandler.addCAPublicKey(caPublicKey);

                    if (showDebug)
                        Log.d(EMVINIT, "load capk index:  " + this.getKeyId() + " - Result: " + rta);

                    ok = true;
                    cursor.moveToNext();
                } catch (Exception e) {
                    Logger.debug(e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, "Error al cargar CAPK", Toast.LENGTH_SHORT);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.debug(e.getMessage());
        }
        databaseAccess.closeDb();
        return ok;
    }

    private String consultaSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        int counter = 1;
        for (String s : CAPK_ROW.fields) {
            sql.append(s);
            if (counter++ < CAPK_ROW.fields.length) {
                sql.append(",");
            }
        }
        sql.append(" from capks");
        sql.append(";");

        return sql.toString();
    }

    /**
     * create file CAKEY used in the callback CTL
     *
     * @return
     */

    private byte[] lastDayOfMonth(byte[] date) {
        byte[] ndays = new byte[]{(byte) 0x00, (byte) 0x31, (byte) 0x28, (byte) 0x31, (byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x31,
                (byte) 0x31, (byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x31};

        byte[] year = new byte[2];
        byte[] month = new byte[1];
        byte[] ret = new byte[1];
        System.arraycopy(date, 0, year, 0, 2);
        System.arraycopy(date, 2, month, 0, 1);

        int yearI = Integer.parseInt(hexString(year));
        int monthI = Integer.parseInt(hexString(month));

        ret[0] = ndays[monthI];
        if (monthI == 0x02) {
            if ((yearI % 4 == 0) && !(yearI % 100 == 0)) {
                ret[0]++; //leap year
            } else if (yearI % 400 == 0) {
                ret[0]++; //leap year
            }
        }
        return ret;
    }

    //as I know, the exponent only have two types
    //One is len 1, exponent =0x03
    //second id len 3, exponent = 0x01,0x00,0x01

    private byte[] getExp(byte[] source) {
        int lenModule = 4;
        int index = 0;
        if (source[0] != 0x00) {
            return null;
        }

        while (lenModule > 0) {
            if (source[index++] == 0x00) {
                lenModule--;
            } else {
                break;
            }
        }
        byte[] exponent = new byte[lenModule];
        if (lenModule > 0) {
            System.arraycopy(source, 4 - lenModule, exponent, 0, lenModule);
        }
        return exponent;
    }

    //as I know, the exponent only have two types
    //One is len 1, exponent =0x03
    //second id len 3, exponent = 0x01,0x00,0x01

    private CAPK_ROW() {
    }

    public static CAPK_ROW getSingletonInstance() {
        if (capkRow == null) {
            capkRow = new CAPK_ROW();
        } else {
            Logger.debug("No se puede crear otro objeto, ya existe");
        }
        return capkRow;
    }

    public String ToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CAPK_ROW: \n");

        sb.append("\t");
        sb.append("PLANTILLA_ID: ");
        sb.append(this.plantillaId);
        sb.append("\n");

        sb.append("\t");
        sb.append("KEY_ID: ");
        sb.append(this.keyId);
        sb.append("\n");

        sb.append("\t");
        sb.append("KEY_RID: ");
        sb.append(this.keyRid);
        sb.append("\n");

        sb.append("\t");
        sb.append("KEY_EXPONENT: ");
        sb.append(this.keyExponent);
        sb.append("\n");

        sb.append("\t");
        sb.append("KEY_SIZE: ");
        sb.append(this.keySize);
        sb.append("\n");

        sb.append("\t");
        sb.append("KEY_MODULE: ");
        sb.append(this.keyModule);
        sb.append("\n");

        sb.append("\t");
        sb.append("KEY_EXPIRATION_DATE: ");
        sb.append(this.keyExpirationDate);
        sb.append("\n");

        sb.append("\t");
        sb.append("KEY_SHA1: ");
        sb.append(this.keySha1);
        sb.append("\n");

        return sb.toString();
    }

    public String getPlantillaId() {
        return plantillaId;
    }

    public void setPlantillaId(String plantillaId) {
        this.plantillaId = plantillaId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeyRid() {
        return keyRid;
    }

    public void setKeyRid(String keyRid) {
        this.keyRid = keyRid;
    }

    public String getKeyExponent() {
        return keyExponent;
    }

    public void setKeyExponent(String keyExponent) {
        this.keyExponent = keyExponent;
    }

    public String getKeySize() {
        return keySize;
    }

    public void setKeySize(String keySize) {
        this.keySize = keySize;
    }

    public String getKeyModule() {
        return keyModule;
    }

    public void setKeyModule(String keyModule) {
        this.keyModule = keyModule;
    }

    public String getKeyExpirationDate() {
        return keyExpirationDate;
    }

    public void setKeyExpirationDate(String keyExpirationDate) {
        this.keyExpirationDate = keyExpirationDate;
    }

    public String getKeySha1() {
        return keySha1;
    }

    public void setKeySha1(String keySha1) {
        this.keySha1 = keySha1;
    }
}
