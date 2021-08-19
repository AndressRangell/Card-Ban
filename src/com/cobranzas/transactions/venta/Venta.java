package com.cobranzas.transactions.venta;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.transactions.DataAdicional.DataAdicional;
import com.cobranzas.transactions.common.CommonFunctionalities;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.TransInputPara;
import com.newpos.libpay.trans.finace.FinanceTrans;

public class Venta extends FinanceTrans implements TransPresenter {

    String claseVenta = this.getClass().getName();
    String mensajeLog = "prepareOnline: retVal == ";

    /**
     * Estructura de transacciones financieras
     *
     * @param ctx        Context
     * @param transEname Nombre Transaccion
     * @param p          Parametros
     */
    public Venta(Context ctx, String transEname, String codigo, TransInputPara p, Activity activity, boolean isCajas) {
        super(ctx, transEname);
        init(transEname, p);
        this.activity = activity;
        this.isCajas = isCajas;

        Code = codigo;
        try {
            String field24 = "0";
            if (isCajas) {
                field24 = "1";
            }
            DataAdicional.addOrUpdate(24, field24);
            DataAdicional.addOrUpdate(82, codigo);
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, claseVenta, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, claseVenta, e.getStackTrace());
            transUI.showFinish();
        }

    }

    private void init(String transEname, TransInputPara p) {
        para = p;
        transUI = para.getTransUI();
        isReversal = true;
        isProcPreTrans = true;
        isSaveLog = true;
        isDebit = true;
        typeCoin = CommonFunctionalities.tipoMoneda()[1];
        TransEName = transEname;
        host_id = idLote;
    }

    @Override
    public ISO8583 getISO8583() {
        return null;
    }

    @Override
    public void start() {

        Logger.logLine(LogType.FLUJO, claseVenta, "Inicio transaccion Venta");

        if (!checkBatchAndSettle(true, false))
            return;

        consultaDeudas();

        Logger.debug("SaleTrans>>finish");
    }

    public void consultaDeudas() {
        inputMode = ENTRY_MODE_HAND;
        isReversal = false;
        if (!prepareOnline(true)) return;
    }

    private boolean prepareOnline(boolean is2View) {
        Logger.logLine(LogType.FLUJO, claseVenta, "Ingreso a metodo prepareOnline");
        if (Code == null || Code.isEmpty()) {
            transUI.showError(timeout, Tcode.T_user_cancel_input, false);
            return false;
        }

        Logger.logLine(LogType.FLUJO, claseVenta, "Monto = " + Amount);

        /*Por defecto al iniciar la transaccion OtherAmount = -1*/

        if (is2View) {
            if (!verificaPedirVuelto()) {
                transUI.showError(timeout, Tcode.T_user_cancel_operation, false);
                return false;
            }

            transUI.handling(timeout, Tcode.Status.connecting_center, TransEName);

            if (!requestPin1()) {
                return false;
            }
        }

        if (retVal == 0) {

            if (is2View) {
                transUI.handling(timeout, Tcode.Status.connecting_center, TransEName);
            }
            setDatas(inputMode);
            switch (inputMode) {
                case ENTRY_MODE_ICC:
                case ENTRY_MODE_NFC:
                    retVal = OnlineTrans(emv);
                    Log.d(claseVenta, mensajeLog + retVal);
                    break;
                default:
                    retVal = OnlineTrans(null);
                    Log.d(claseVenta, mensajeLog + retVal);
                    break;
            }
            savePreferences();
            Logger.debug("SaleTrans>>OnlineTrans=" + retVal);
            clearPan();
            if (retVal == 0) {
                return true;
            } else {
                switch (retVal) {

                    case Tcode.T_envio_fallido_reverso_fail:
                        transUI.showError(timeout, "REVERSA PENDIENTE", Tcode.T_envio_fallido_reverso_fail, false, false);
                        break;
                    case Tcode.T_user_cancel_operation:
                        transUI.showError(timeout, "OPERACIÓN DECLINADA", Tcode.T_user_cancel_operation, false, false);
                        break;
                    case Tcode.T_go_menu:
                        transUI.showFinish();
                        break;
                    default:
                        transUI.showError(timeout, retVal, false);
                        break;
                }
                return false;
            }

        } else {
            transUI.showError(timeout, retVal, true);
            return false;
        }
    }
}
