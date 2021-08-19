package com.cobranzas.cajas;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cobranzas.cajas.model.Eco;
import com.cobranzas.cajas.model.ErrorJSON;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.trans.finace.FinanceTrans;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.desert.newpos.payui.master.MasterControl;

public class ApiJson implements Runnable {

    private static final String TAGERROR = "ERROR CAJA POS";
    private static final String CUOTAS = "cuotas";
    private static final String NROFACTURAS = "facturaNro";
    private static final String INVALIDA = "Información inválida";
    private static final String MONTO = "monto";
    private static final String RECEIVE = "receiveJSON: ";
    public static Reponde2CajaJSON listener;
    String clase = "ApiJson.java";
    ArrayList<Thread> hilos = new ArrayList<>();
    private Context context;
    private String tipoTrans;
    private String body;
    private wsCallback callback;


    ApiJson(Context context, String uri, String body, wsCallback callback) {
        this.context = context;
        this.callback = callback;
        this.tipoTrans = uri;
        this.body = body;
        Logger.debug("llegada a Apijson");
    }


    @Override
    public void run() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = null;
                try {
                    if (body != null) {
                        json = reciveJSON(body);

                        if (json != null) {
                            transaccionCaja(json, tipoTrans, new Reponde2CajaJSON() {
                                @Override
                                public void rsp2Cajas(Object json, String http) {
                                    if (json != null && !http.isEmpty()) {
                                        callback.rspCallback(json, http);
                                    }

                                }
                            });
                        } else {
                            errorVerificacioJSON("ERROR AL RECIBIR DATOS DE CAJA", 400, "400");
                        }
                    } else {
                        errorVerificacioJSON("MENSAJE INCOMPLETO", 400, "400");
                    }
                } catch (Exception e) {
                    errorVerificacioJSON("Error de SDK", 400, "500");
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    e.printStackTrace();
                    Log.d(TAGERROR, " \"JSONObject\" run: " + e.getMessage());
                }
            }
        });

        thread.start();
        hilos.add(thread);

    }

    private void transaccionCaja(JSONObject json, String tipoTrans, Reponde2CajaJSON listener) {
        this.setListener(listener);
        switch (tipoTrans) {
            case "/pos/eco":
                ecoCajas(json);
                break;
            case "/pos/venta-ux":
                if (!json.isNull(CUOTAS) && !json.isNull("plan")) {
                    venta(context, json);
                } else {
                    ventasUX(context, json);
                }
                break;
            case "/pos/descuento":
                if (FinanceTrans.confirmacionCaja != null) {
                    FinanceTrans.confirmacionCaja.waitConfirmacionCaja(json);
                } else {
                    Log.d("ERROR CAJAPOS", "transaccionCaja: " + "FinanceTrans.confirmacionCaja == null");
                }
                break;
            case "/pos/venta/credito":
                ventaCreditoCaja(context, json);
                break;
            case "/pos/venta/debito":
                ventaDebitoCaja(context, json);
                break;

            default:
                errorVerificacioJSON("Venta no Especificada", 400, "400");
                break;
        }
    }

    private void ventaCreditoCaja(Context context, JSONObject json) {
        try {
            if (!json.isNull(NROFACTURAS) && !json.isNull(CUOTAS) && !json.isNull("plan")) {
                if (!json.getString(NROFACTURAS).equals("0") && !json.getString(CUOTAS).equals("0") || !json.getString("plan").equals("0")) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(context, MasterControl.class);
                    intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[34]);
                    intent.putExtra(MasterControl.CODIGO, json.getString(NROFACTURAS) + "@" + json.getString(CUOTAS) + "@" + json.getString("plan"));
                    context.startActivity(intent);
                } else {
                    errorVerificacioJSON(INVALIDA, 400, "400");
                }
            } else {
                errorVerificacioJSON("Error con los datos del JSON - Venta Credito", 400, "400");
            }
        } catch (JSONException e) {
            Log.d(TAGERROR, "ventaCreditoCaja: " + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());

        }
    }

    private void ventaDebitoCaja(Context context, JSONObject json) {
        try {
            if (!json.isNull(NROFACTURAS)) {
                if (!json.getString(NROFACTURAS).equals("0")) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(context, MasterControl.class);
                    intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[35]);
                    intent.putExtra(MasterControl.CODIGO, json.getString(NROFACTURAS));
                    context.startActivity(intent);
                } else {
                    errorVerificacioJSON(INVALIDA, 400, "400");
                }
            } else {
                errorVerificacioJSON("Error con los datos del JSON - Venta Debito", 400, "400");
            }
        } catch (JSONException e) {
            Log.d(TAGERROR, "ventaDebitoCaja: " + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    private void errorVerificacioJSON(String mensaje, int codeStatus, String restHttp) {
        ErrorJSON error = new ErrorJSON();
        error.setStatusCode(codeStatus);
        error.setError("Bad Request");
        error.setMessage(mensaje);
        callback.rspCallback(error, restHttp);
        intentCajas(mensaje, false);
    }

    private void venta(Context context, JSONObject json) {
        try {

            if (!json.isNull(MONTO) &&
                    (!json.getString(CUOTAS).equals("0") && !json.getString("plan").equals("0") && !json.getString(MONTO).equals("0"))) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[33]);
                intent.putExtra(MasterControl.CODIGO, json.getString(CUOTAS) + "@" + json.getString("plan") + "@" + json.getString(MONTO) + "00");
                intent.putExtra(MasterControl.CAJAS, true);
                context.startActivity(intent);
            } else {
                errorVerificacioJSON(INVALIDA, 400, "400");
            }

        } catch (JSONException e) {
            Log.d(TAGERROR, "venta: " + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }

    }

    private void ecoCajas(JSONObject json) {
        try {
            Eco eco = new Eco();
            if (!json.isNull("eco")) {
                eco.setEco(json.getInt("eco"));
                if (ApiJson.listener != null) {
                    intentCajas("Servicio Encendido Correctamente", true);
                    ApiJson.listener.rsp2Cajas(eco, "200");
                } else {
                    Log.d("ERROR", "ecoCajas: " + "ApiJson.listener == null");
                }
            } else {
                errorVerificacioJSON("Error con los datos del JSON", 400, "400");
            }
        } catch (JSONException e) {
            Log.d("ERROR CAJAPOS ", "JSONException ecoCajas: " + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    private void ventasUX(Context context, JSONObject json) {
        try {
            if (!json.isNull(MONTO)) {
                if (!json.getString(MONTO).equals("0")) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(context, MasterControl.class);
                    intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[21]);
                    intent.putExtra(MasterControl.CODIGO, json.getString(MONTO) + "00");
                    intent.putExtra(MasterControl.CAJAS, true);
                    context.startActivity(intent);
                } else {
                    errorVerificacioJSON("Monto Invalido", 400, "400");
                }

            } else {
                errorVerificacioJSON("Error con los datos del JSON", 400, "400");
            }

        } catch (JSONException e) {
            Log.d("ERROR CAJAPOS ", "JSONException ventasUX: " + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }

    }

    /***
     * @funcion  : receiveJSON
     *
     * @Descripcion : Procesa la trama que llega de Caja y donde comienza hacer las particiones de la data donde se
     * define el tipo transaccion y hace la conversion a JSONObject para el manejo de su informacion
     *
     * @param dataString = : Se Recibe un String que es el encargado de traer la informacion
     * @return : JSONObject
     */

    public JSONObject reciveJSON(String dataString) {
        String json;
        try {
            if (!dataString.isEmpty()) {

                Log.d("DATA JSON --- ", RECEIVE + dataString);
                Logger.logLine(LogType.COMUNICACION, clase, "DATA JSON --- " + RECEIVE + dataString);

                if ((dataString.indexOf("{") != -1) && (dataString.lastIndexOf("}") != -1)) {
                    json = dataString.substring(dataString.indexOf("{"), dataString.lastIndexOf("}") + 1);
                    Log.d("JSON Obtendio ---- ", RECEIVE + json);

                    if (tipoTrans.equals("/pos/venta-ux")) {
                        return new JSONObject(json);
                    } else {
                        if (json.contains("null")) {
                            return null;
                        }
                        return new JSONObject(json);
                    }

                } else {
                    return null;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAGERROR, RECEIVE + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase + " receiveJSON", e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase + " receiveJSON", e.getStackTrace());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public void setListener(Reponde2CajaJSON repondeJSON) {
        ApiJson.listener = repondeJSON;
    }

    public void intentCajas(String mensaje, boolean codeStatus) {
        Intent intent = new Intent(context, VsErrorCaja.class);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("codeStatus", codeStatus);
        context.startActivity(intent);
    }
}
