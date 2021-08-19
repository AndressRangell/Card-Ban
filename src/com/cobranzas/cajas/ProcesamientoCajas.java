package com.cobranzas.cajas;

import android.content.Context;
import android.util.Log;

import com.cobranzas.cajas.model.ErrorJSON;
import com.cobranzas.cajas.model.SuccesBines;
import com.cobranzas.timertransacciones.TimerTrans;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.presenter.TransUI;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.finace.FinanceTrans;

public class ProcesamientoCajas {

    TimerTrans timerTrans;
    String clase = "ProcesamientoCajas.java ";
    Context context;
    TransUI transUI;
    waitConfirmacionCaja confirmacionCaja;


    public ProcesamientoCajas(Context context, TransUI transUI) {
        this.context = context;
        this.transUI = transUI;
    }

    public void lecturaTarjeta2Caja(String pan, String traceNo) {
        if (pan != null && !pan.isEmpty()) {
            Logger.logLine(LogType.FLUJO, clase + " Metodo : modoCajaPOS", "Ingreso al Metodo  de lecturaTarjeta2Caja");
            SuccesBines succesBines = new SuccesBines();
            Log.d("PAN ", "isICC1: " + pan);

            succesBines.setBin(pan.substring(0, 10));
            succesBines.setNsu(traceNo);
            if (ApiJson.listener != null) {
                Logger.logLine(LogType.FLUJO, clase + " Metodo : modoCajaPOS", "Envio Respuesta a la Caja");
                ApiJson.listener.rsp2Cajas(succesBines, "200");
            } else {
                Log.d("ERROR", "lecturaTarjeta2Caja: " + "ApiJson.listener == null");
            }

        }
    }

    public void esperaConfirmacionCaja(waitConfirmacionCaja confirmacionCaja) {
        FinanceTrans.confirmacionCaja = null;
        FinanceTrans.setConfirmacionCaja(confirmacionCaja);
    }

    public void counterDownTimer(final int timeout, final String mensaje, final String metodo) {
        timerTrans.getInstanceTimerTrans(timeout, mensaje, metodo, new TimerTrans.OnResultTimer() {
            @Override
            public void rsp2Timer() {
                transUI.showError(timeout, Tcode.T_err_timeout, true);//182 Tiempo Agotado
            }
        });

    }

    public void mensajeErrorCajas(String mensaje, int codeStatus) {
        ErrorJSON error = new ErrorJSON();
        error.setStatusCode(codeStatus);
        error.setError("Bad Request");
        error.setMessage(mensaje);
        if (ApiJson.listener != null) {
            ApiJson.listener.rsp2Cajas(error, "400");
        } else {
            Log.d("ERROR", "mensajeErrorCajas: " + " ApiJson.listener == null");
        }
    }

    public void deleteTimer() {
        Logger.logLine(LogType.FLUJO, clase + " Metodo : deleteTimer", "Se elimina el Timer Creado ");
        timerTrans.deleteTimer();
    }

}
