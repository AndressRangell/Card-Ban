package com.cobranzas.transactions.echotest;

import android.content.Context;
import android.media.ToneGenerator;

import com.cobranzas.transactions.DataAdicional.DataAdicional;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.TransInputPara;
import com.newpos.libpay.utils.PAYUtils;

import cn.desert.newpos.payui.UIUtils;

public class EchoTest extends Trans implements TransPresenter {

    String clase = "EchoTest.java";
    private int timeOutScreensInit = 5 * 1000;
    private boolean isErase22DataAdicional;

    /**
     * 金融交易类构造
     *
     * @param ctx
     * @param transEname
     */
    public EchoTest(Context ctx, String transEname, TransInputPara p, boolean isErase22DataAdicional) {
        super(ctx, transEname);
        TransEName = transEname;
        this.isErase22DataAdicional = isErase22DataAdicional;
        para = p;
        if (para != null) {
            transUI = para.getTransUI();
        }
    }

    @Override
    public void start() {

        if (isErase22DataAdicional) {
            DataAdicional.addOrUpdate(22, null);
        }

        transUI.handling(timeout, Tcode.Status.echo_test);
        retVal = sendEchoTest();
        if (retVal != 0) {
            transUI.showError(timeOutScreensInit, retVal, true);
            UIUtils.beep(ToneGenerator.TONE_PROP_BEEP2);
        } else {
            transUI.trannSuccess(timeout, Tcode.Status.echo_test_success);
            UIUtils.beep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
        }
        Logger.debug("InitTrans>>finish");
        return;
    }

    @Override
    public ISO8583 getISO8583() {
        return null;
    }

    /**
     * @return - 0 : Echo Test Exitoso
     * - Tcode.T_socket_err
     * - Tcode.T_send_err
     * - Tcode.T_receive_err
     */
    public int sendEchoTest() {
        setFixedDatas();
        setFieldEchoTest();

        return retVal = sendRcvdInit();
    }

    private void setFieldEchoTest() {
        iso8583.setHasMac(false);
        iso8583.clearData();

        LocalTime = PAYUtils.getLocalTime();
        LocalDate = PAYUtils.getLocalDate();

        if (MsgID != null) {
            iso8583.setField(0, MsgID);
        }
        if (TraceNo != null) {
            iso8583.setField(11, TraceNo);
        }
        if (LocalTime != null) {
            iso8583.setField(12, LocalTime);
        }
        if (LocalDate != null) {
            iso8583.setField(13, LocalDate);
        }
        DataAdicional dataAdicional = new DataAdicional(MsgID);
        dataAdicional.commonConfig(context);
        Field63 = dataAdicional.getSubFields("");
        iso8583.setField(63, Field63);
    }

    /**
     * @return - 0 : Echo Test Exitoso
     * - Tcode.T_socket_err
     * - Tcode.T_send_err
     * - Tcode.T_receive_err
     */
    private int sendRcvdInit() {
        byte[] respData;
        int rta;

        Logger.logLine(LogType.FLUJO, clase, "sendRcvdInit : Pendiente");

        rta = retriesConnect(0, true, false);

        if (rta == Tcode.T_socket_err) {
            Logger.logLine(LogType.FLUJO, clase, "return T_socket_err");
            return rta;
        }
        transUI.handling(timeout, Tcode.Status.send_data_2_server);
        if (send() == -1) {
            Logger.logLine(LogType.FLUJO, clase, "Error enviando EchoTestr");
            return Tcode.T_send_err;
        }
        transUI.handling(timeout, Tcode.Status.send_over_2_recv);
        respData = recive();

        netWork.close();

        if (respData == null || respData.length <= 0) {
            Logger.logLine(LogType.FLUJO, clase, "T_receive_err despues de closer");
            return Tcode.T_receive_err;
        } else {
            return 0;
        }
    }
}
