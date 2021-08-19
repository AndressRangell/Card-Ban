package com.cobranzas.transactions.Reversal;

import android.content.Context;

import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.presenter.TransUI;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.TransInputPara;
import com.newpos.libpay.trans.manager.RevesalTrans;
import com.newpos.libpay.trans.translog.TransLog;

public class ReversalTransAuto extends RevesalTrans implements TransPresenter {

    TransUI transUI;
    public ReversalTransAuto(Context ctx, String transEName, int timeOut, TransInputPara p) {
        super(ctx, transEName, timeOut);
        para = p;
        if (para != null) {
            transUI = para.getTransUI();
        }
    }

    @Override
    public void start() {
        int respuestaReverso  = analizarReversoAntesDe(transUI);
        if (respuestaReverso != Tcode.T_success){
            transUI.showError(timeout, "REVERSA PENDIENTE", Tcode.T_envio_fallido_reverso_fail, false, false);
        }else {
            TransLog.clearReveral();
            transUI.showError(timeout, "REVERSA APROBADA", Tcode.T_envio_fallido_reverso_ok, false, true);
        }
    }

    @Override
    public ISO8583 getISO8583() {
        return null;
    }
}
