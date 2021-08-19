package com.cobranzas.transactions.DataAdicional;

import android.content.Context;
import android.util.Log;

import com.cobranzas.model.SubField;
import com.cobranzas.model.SubFieldsModel;
import com.cobranzas.tools.UtilNetwork;
import com.google.common.primitives.Ints;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.config.DevConfig;
import com.wposs.cobranzas.BuildConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.newpos.libpay.utils.ISOUtil.convertStringToHex;

public class DataAdicional {

    public static ArrayList<SubFieldsModel> subFieldsModel = new ArrayList<>();
    static String clase = "DataAdicional.java";
    private static ArrayList<SubField> decoFields = new ArrayList<>();
    private static int indice = 0;
    String msgId;

    /**
     * @param msgId -> Identificador del tipo de mensaje
     */
    public DataAdicional(String msgId) {
        this.msgId = msgId;
        loadSubFieldsModel();
    }

    /**
     * Devuelve un array list de SubFieldsModel
     *
     * @return subFieldsModel -> es el modelo de la transaccion con el (mti, el identificaor de la transaccion(procode) y la fila de campos que va contener la transacción)
     */
    public static ArrayList<SubFieldsModel> getSubTrans() {
        return subFieldsModel;
    }

    /**
     * Se hace el modelo de respuesta del campo63, se mapea de acuerdo al msgId y el proCode
     */
    private static void loadSubFieldsModel() {
        if (subFieldsModel != null) {
            subFieldsModel.clear();
        } else {
            subFieldsModel = new ArrayList<>();
        }
        subFieldsModel.add(new SubFieldsModel("0400", "", new int[]{81, 89, 92, 22}));
        subFieldsModel.add(new SubFieldsModel("0800", "", new int[]{81}));
        subFieldsModel.add(new SubFieldsModel("0200", "720000", new int[]{70, 81, 82, 90, 94, 95}));
        subFieldsModel.add(new SubFieldsModel("0200", "820000", new int[]{33, 70, 71, 81, 82, 90, 94, 95}));
    }

    /**
     * Se obtiene el valor de un campo en especifico
     *
     * @param idCampo -> el identificador del campo
     * @return res -> Dato del campo
     */
    public static String getField(int idCampo) {
        String res = null;
        String id = String.valueOf(idCampo);
        for (SubField sub : getSubFieldsList()) {
            if (sub.getFieldId().equals(id)) {
                res = sub.getFieldData();
            }
        }
        return res;
    }

    /**
     * Es un  array de SubField
     *
     * @return subFields -> son los subcampos para el campo 63
     */
    private static ArrayList<SubField> getSubFieldsList() {
        return decoFields;
    }

    /**
     * Se actualiza un campo o lo añade en caso de que no esté
     *
     * @param id   -> el código del campo
     * @param data -> dato del campo
     */
    public static void addOrUpdate(int id, String data) {
        String idCampo = String.valueOf(id);
        int position = getPosition(idCampo);
        if (position == -1) {
            decoFields.add(new SubField(idCampo, data));
        } else {
            decoFields.get(position).setFieldData(data);
        }
    }

    /**
     * Obtiene la posición en la que van los campos correspondiente al ID del campo
     *
     * @param fieldId
     * @return posicion
     */

    private static int getPosition(String fieldId) {
        ArrayList<SubField> fields = getSubFieldsList();
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getFieldId().equals(fieldId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Se setean los subcampos para el campo 63, a partir de una trama (en hexadecimal)
     *
     * @param field63
     */
    public void setSubCampos(String field63) {
        indice = 0;
        decoFields = new ArrayList<>();
        for (int i = 0; i < field63.length(); i = indice) {
            String fieldLen = field63.substring(indice, indice += 4);
            String fieldId = ISOUtil.hex2AsciiStr(field63.substring(indice, indice + 4));
            String fieldData = obtenerCampo(field63);
            decoFields.add(new SubField(fieldId, fieldData));
            indice += Integer.parseInt(fieldLen) * 2;
        }
    }

    /**
     * Se obtiene la longitud del campo(id + dato), luego de esto se calcula la longitud real del campo 63(longitud +id +dato)
     * este dato se usa para obtener el campo en especifico tomando del campo 63 los caracteres desde el indice+4 hasta la longitud Real
     * ese campo se convierte a Ascii y se almanena en Data
     *
     * @param campo63
     * @return
     */
    private String obtenerCampo(String campo63) {
        if (!campo63.equals("")) {
            try {
                int lenCampo = Integer.parseInt(campo63.substring(indice - 4, indice)) * 2;
                int lenReal = (indice) + lenCampo;
                if (campo63.length() < ((indice) + lenCampo)) {
                    lenReal = campo63.length();
                }
                String data = campo63.substring(indice + 4, lenReal);
                return ISOUtil.hex2AsciiStr(data);
            } catch (NumberFormatException ex) {
                Logger.logLine(LogType.FLUJO, clase, "Error NumberFormatException");
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Se setean los campos dependiendo del tipo de trnasaccion (msgID) y el codigo de proceso (proCodde)
     *
     * @return Hex.toString()
     */
    public String getSubFields(String proCode) {
        StringBuilder Hex = new StringBuilder();
        for (SubFieldsModel subField : getSubTrans()) {
            if (subField.getMti().equals(msgId)) {
                switch (subField.getMti()) {
                    case "0400":
                    case "0800":
                        Hex.append(getField63(subField.getFields()));
                        break;
                    case "0200":
                        if (subField.getProCode().equals(proCode)) {
                            Hex.append(getField63(subField.getFields()));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return Hex.toString();
    }

    /**
     * Se valida la longitud para la respuesta del campo 63 dependiendo de su ID
     *
     * @param subPrint
     * @return res.toString();
     */
    private String getField63(int[] subPrint) {
        StringBuilder res = new StringBuilder();
        if (subPrint.length > 0) {
            ArrayList<SubField> sb = getSubFieldsList();
            Collections.sort(sb, new CustomComparator());
            for (SubField field : getSubFieldsList()) {
                int id = Integer.parseInt(field.getFieldId());
                boolean exist = Ints.contains(subPrint, id);
                Log.e(clase, "getField63: exists " + exist);
                if (exist) {
                    String data = getFieldData(field.getFieldId(), field.getFieldData());
                    res.append(data);
                }
            }
        } else {
            Log.e(clase, "getField63: Array Vacio");
            res = new StringBuilder();
        }
        return res.toString();
    }

    /**
     * Se crea el campo 63 (se pasa en formato Hexa)
     *
     * @param fieldId
     * @param fieldData
     * @return res
     */

    private String getFieldData(String fieldId, String fieldData) {
        String res;
        int len = ISOUtil.convertStringToHex(fieldData).length() / 2;
        len = len + 2;
        String leng = ISOUtil.padleft(String.valueOf(len), 4, '0');
        res = leng + convertStringToHex(fieldId) + convertStringToHex(fieldData);
        return res;
    }

    /**
     * Se setean los campos para toda las transacciones
     *
     * @param contextv
     */
    public void commonConfig(Context contextv) {
        String serialTerminal = DevConfig.getSN();
        addOrUpdate(81, serialTerminal);
        String firmware = formatField70(DevConfig.getFirmwareVersion());
        if (firmware != null) {
            addOrUpdate(70, firmware);
        }
        setCampo90();
        addOrUpdate(94, UtilNetwork.getIpFull(contextv));
        addOrUpdate(95, UtilNetwork.getImei(contextv));
    }

    private String formatField70(String firmware) {
        String res = null;
        try {
            if (firmware.length() > 0) {
                String word = "firmware";
                int length = (11 - firmware.length());
                res = ISOUtil.padright(word.substring(0, length - 1) + "-" + firmware, 11, '0');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Se setea el nombre de la versión en el campo 90
     */
    private void setCampo90() {
        int lenIni = 0;
        try {
            String verAxu = BuildConfig.VERSION_NAME;
            if (BuildConfig.VERSION_NAME.length() > 7) { // Tamaño maximo de longitud = 10
                lenIni = BuildConfig.VERSION_NAME.length() - 7;
                verAxu = BuildConfig.VERSION_NAME.substring(lenIni, BuildConfig.VERSION_NAME.length());
            }
            verAxu = ISOUtil.padright(verAxu, 7, ' ');
            Log.e(clase, "setCampo90: verAux " + verAxu);
            addOrUpdate(90, verAxu);
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }
}

class CustomComparator implements Comparator<SubField> {
    @Override
    public int compare(SubField sb1, SubField sb2) {
        return sb1.getFieldId().compareTo(sb2.getFieldId());
    }
}