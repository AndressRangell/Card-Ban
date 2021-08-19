package com.newpos.libpay.presenter;

import android.app.Activity;
import android.media.ToneGenerator;
import android.util.Log;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.adaptadores.ModeloMensajeConfirmacion;
import com.cobranzas.cajas.ApiJson;
import com.cobranzas.cajas.model.ErrorJSON;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.keys.DUKPT;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.PaySdkException;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.card.CardListener;
import com.newpos.libpay.device.card.CardManager;
import com.newpos.libpay.device.pinpad.OfflineRSA;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinpadListener;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.device.user.OnUserResultListener;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.actividades.MainActivity.modoCaja;

/**
 * Created by zhouqiang on 2017/4/25.
 *
 * @author zhouqiang
 * 交易UI接口实现类
 * MVP架构中的P层 ，处理复杂的逻辑及数据
 */

public class TransUIImpl implements TransUI {

    String clase = "TransUllmpl.java";
    private TransView transView;
    private Activity mActivity;
    private CardInfo cInfo;
    private CardManager cardManager = null;
    private CountDownLatch mLatch;
    private int mRet = 0;
    private InputManager.Style payStyle;
    private final OnUserResultListener listener = new OnUserResultListener() {
        @Override
        public void confirm(InputManager.Style style) {
            try {
                mRet = 0;
                payStyle = style;
                mLatch.countDown();
            } catch (NullPointerException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Log.e("Exception ", e.toString());
            }
        }

        @Override
        public void cancel() {
            try {
                mRet = 1;
                mLatch.countDown();
            } catch (NullPointerException e) {
                Log.e("Exception ", e.toString());
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }

        @Override
        public void confirm(int applistselect) {
            try {
                mRet = applistselect;
                mLatch.countDown();
            } catch (NullPointerException e) {
                Log.e("Exception ", e.toString());
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
    };

    public TransUIImpl(Activity activity, TransView tv) {
        this.transView = tv;
        this.mActivity = activity;
    }

    public static String getErrInfo(String status) {
        try {
            String[] errs = Locale.getDefault().getLanguage().equals("zh") ?
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.ERRNO, status) :
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.ERRNO_EN, status);
            if (errs != null) {
                return errs[0];
            }
        } catch (PaySdkException pse) {
            Logger.logLine(LogType.EXCEPTION, "TransUllmpl.java", pse.getMessage());
            Logger.logLine(LogType.EXCEPTION, "TransUllmpl.java", pse.getStackTrace());
            Logger.error("Exception" + pse.toString());
            Thread.currentThread().interrupt();
        }
        if (Locale.getDefault().getLanguage().equals("zh")) {
            return "未知错误";
        } else {
            return "Código de Error Desconocido";
        }
    }

    @Override
    public PinInfo getPinpadOfflinePin(int timeout, int i, OfflineRSA key, int counts) {
        this.mLatch = new CountDownLatch(1);
        final PinInfo pinInfo = new PinInfo();
        PinpadManager pinpadManager = PinpadManager.getInstance();
        pinpadManager.getOfflinePin(i, key, counts, new PinpadListener() {
            @Override
            public void callback(PinInfo info) {
                pinInfo.setResultFlag(info.isResultFlag());
                pinInfo.setErrno(info.getErrno());
                pinInfo.setNoPin(info.isNoPin());
                pinInfo.setPinblock(info.getPinblock());
                mLatch.countDown();
            }
        });
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        transView.showMsgInfo(timeout, getStatusInfo(String.valueOf(Tcode.Status.handling)), false, false);
        return pinInfo;
    }

    @Override
    public InputInfo getOutsideInput(int timeout, InputManager.Mode type, String title, String trx, long amount) {
        String tipoIngreso;
        int longitudMaxima = 0;
        if (type.equals(InputManager.Mode.AMOUNT)) {
            tipoIngreso = DefinesBANCARD.INGRESO_MONTO;
            longitudMaxima = 10;
        } else {
            tipoIngreso = DefinesBANCARD.INGRESO_TELEFONO;
        }
        transView.showIngresoDataNumericoView(timeout, tipoIngreso, "", title, longitudMaxima, trx, amount, listener);
        //transView.showInputView(timeout, type, listener, title);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(type));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public CardInfo getCardUse(String msg, int timeout, int mode, String title, long amount, boolean opciones) {
        transView.showCardView(msg, timeout, mode, title, amount, opciones, listener);
        this.mLatch = new CountDownLatch(1);
        final CardInfo cInfo = new CardInfo();
        CardManager cardManager = CardManager.getInstance(mode);
        //cInfo = new CardInfo();
        //cardManager = CardManager.getInstance(mode);
        cardManager.getCard(timeout, new CardListener() {
            @Override
            public void callback(CardInfo cardInfo) {
                cInfo.setResultFalg(cardInfo.isResultFalg());
                cInfo.setNfcType(cardInfo.getNfcType());
                cInfo.setCardType(cardInfo.getCardType());
                cInfo.setTrackNo(cardInfo.getTrackNo());
                cInfo.setCardAtr(cardInfo.getCardAtr());
                cInfo.setErrno(cardInfo.getErrno());
                mLatch.countDown();
            }
        });
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }

        return cInfo;
    }

    @Override
    public CardInfo getCardUse(String msg, int timeout, int mode) {
        transView.showMsgInfo(timeout, msg, false, false);
        this.mLatch = new CountDownLatch(1);
        final CardInfo cInfo = new CardInfo();
        CardManager cardManager = CardManager.getInstance(mode);
        //cInfo = new CardInfo();
        //cardManager = CardManager.getInstance(mode);
        cardManager.getCard(timeout, new CardListener() {
            @Override
            public void callback(CardInfo cardInfo) {
                cInfo.setResultFalg(cardInfo.isResultFalg());
                cInfo.setNfcType(cardInfo.getNfcType());
                cInfo.setCardType(cardInfo.getCardType());
                cInfo.setTrackNo(cardInfo.getTrackNo());
                cInfo.setCardAtr(cardInfo.getCardAtr());
                cInfo.setErrno(cardInfo.getErrno());
                mLatch.countDown();
            }
        });
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        return cInfo;
    }

    @Override
    public CardInfo getCardFallback(String msg, int timeout, int mode, String title, long amount) {
        transView.showCardView(msg, timeout, mode, title, amount, false, listener);
        this.mLatch = new CountDownLatch(1);
        //final CardInfo cInfo = new CardInfo() ;
        //CardManager cardManager = CardManager.getInstance(mode);
        cInfo = new CardInfo();
        cardManager = CardManager.getInstance(mode);
        cardManager.getCard(timeout, new CardListener() {
            @Override
            public void callback(CardInfo cardInfo) {
                cInfo.setResultFalg(cardInfo.isResultFalg());
                cInfo.setNfcType(cardInfo.getNfcType());
                cInfo.setCardType(cardInfo.getCardType());
                cInfo.setTrackNo(cardInfo.getTrackNo());
                cInfo.setCardAtr(cardInfo.getCardAtr());
                cInfo.setErrno(cardInfo.getErrno());
                mLatch.countDown();
            }
        });
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }

        return cInfo;
    }

    @Override
    public PinInfo getPinpadOnlinePin(int timeout, String amount, String cardNo) {
        this.mLatch = new CountDownLatch(1);
        final PinInfo pinInfo = new PinInfo();
        PinpadManager pinpadManager = PinpadManager.getInstance();
        pinpadManager.getPin(timeout, amount, cardNo, new PinpadListener() {
            @Override
            public void callback(PinInfo info) {
                pinInfo.setResultFlag(info.isResultFlag());
                pinInfo.setErrno(info.getErrno());
                pinInfo.setNoPin(info.isNoPin());
                pinInfo.setPinblock(info.getPinblock());
                mLatch.countDown();
            }
        });
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        transView.showMsgInfo(timeout, getStatusInfo(String.valueOf(Tcode.Status.handling)), false, false);
        return pinInfo;
    }

    @Override
    public PinInfo getPinpadOnlinePinDUKPT(int timeout, String amount, String cardNo) {
        this.mLatch = new CountDownLatch(1);
        final PinInfo pinInfo = new PinInfo();
        DUKPT dukpt = DUKPT.getInstance();
        String ksnString;
        dukpt.getPinDUKPT(timeout, amount, cardNo, new PinpadListener() {
            @Override
            public void callback(PinInfo info) {
                pinInfo.setResultFlag(info.isResultFlag());
                pinInfo.setErrno(info.getErrno());
                pinInfo.setNoPin(info.isNoPin());
                pinInfo.setPinblock(info.getPinblock());
                pinInfo.setKsnString(info.getKsnString());
                mLatch.countDown();
            }
        });
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        transView.showMsgInfo(timeout, getStatusInfo(String.valueOf(Tcode.Status.handling)), false, false);
        return pinInfo;
    }

    @Override
    public int showCardConfirm(int timeout, String cn) {
        this.mLatch = new CountDownLatch(1);
        transView.showCardNo(timeout, cn, listener);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        return mRet;
    }

    @Override
    public InputInfo showMessageInfo(String title, String msg, String btnCancel, String btnConfirm, int timeout) {
        this.mLatch = new CountDownLatch(1);
        transView.showMessageInfo(title, msg, btnCancel, btnConfirm, timeout, listener);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.REFERENCE));
        }
        return info;
    }

    @Override
    public InputInfo showMessageImpresion(String title, String msg, String btnCancel, String btnConfirm, int timeout) {
        this.mLatch = new CountDownLatch(1);
        transView.showMessageImpresion(title, msg, btnCancel, btnConfirm, timeout, listener);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.REFERENCE));
        }
        return info;
    }

    @Override
    public int showCardApplist(int timeout, String[] list) {
        this.mLatch = new CountDownLatch(1);
        transView.showCardAppListView(timeout, list, listener);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        return mRet;
    }

    @Override
    public int showMultiLangs(int timeout, String[] langs) {
        this.mLatch = new CountDownLatch(1);
        transView.showMultiLangView(timeout, langs, listener);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        return mRet;
    }

    @Override
    public void handling(int timeout, int status) {
        if (status == Tcode.Status.process_trans) {
            transView.showMsgInfo(timeout, getStatusInfo(String.valueOf(status)), false, true);

        } else {
            transView.showMsgInfo(timeout, getStatusInfo(String.valueOf(status)), false, false);

        }
    }

    @Override
    public void handling(int timeout, int status, String title) {
        transView.showMsgInfo(timeout, getStatusInfo(String.valueOf(status)), title, false);
    }

    @Override
    public void handling(int timeout, String mensaje, String title) {
        transView.showMsgInfo(timeout, mensaje, title, false);
    }

    @Override
    public int showTransInfo(int timeout, TransLogData logData) {
        this.mLatch = new CountDownLatch(1);
        transView.showTransInfoView(timeout, logData, listener);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        return mRet;
    }

    @Override
    public void trannSuccess(int timeout, int code, String... args) {
        String info = getStatusInfo(String.valueOf(code));
        if (args.length != 0) {
            info += "\n" + args[0];
        }
        transView.showResultView(timeout, true, false, false, info, listener);
        UIUtils.beep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
    }

    @Override
    public void showError(int timeout, int errcode, boolean isIconoWfi) {
        if (modoCaja) {
            mensajeErrorCajas(errcode);
        }
        String descripcion = getErrInfo(String.valueOf(errcode));
        Logger.logLine(LogType.FLUJO, clase, " Codigo : " + errcode + " Descripcion : " + descripcion);
        Log.d(clase, "showError: " + " Codigo : " + errcode + " Descripcion : " + descripcion);
        transView.showResultView(timeout, false, isIconoWfi, false, getErrInfo(String.valueOf(errcode)), listener);
        UIUtils.beep(ToneGenerator.TONE_PROP_BEEP2);
    }

    @Override
    public void showError(int timeout, String encabezado, int errcode, boolean isIconoWfi, boolean aprobado) {
        if (modoCaja) {
            mensajeErrorCajas(errcode);
        }
        String descripcion = getErrInfo(String.valueOf(errcode));
        Logger.logLine(LogType.FLUJO, clase, "Header : " + encabezado + " Codigo : " + errcode + " Descripcion : " + descripcion);
        Log.d(clase, "showError: " + "Header : " + encabezado + " Codigo : " + errcode + " Descripcion : " + descripcion);
        transView.showResultView(timeout, encabezado, aprobado, isIconoWfi, false, descripcion, listener);
        UIUtils.beep(ToneGenerator.TONE_PROP_BEEP2);
    }

    @Override
    public void showError(int timeout, String encabezado, String errcode, boolean isIconoWfi, boolean aprobado) {
        Logger.logLine(LogType.FLUJO, clase, "Header : " + encabezado + " Codigo : " + errcode + " Descripcion : " + errcode);
        Log.d(clase, "showError: " + "Header : " + encabezado + " Codigo : " + errcode + " Descripcion : " + errcode);
        transView.showResultView(timeout, encabezado, aprobado, isIconoWfi, false, errcode, listener);
        UIUtils.beep(ToneGenerator.TONE_PROP_BEEP2);
    }

    private void mensajeErrorCajas(int errcode) {
        ErrorJSON error = new ErrorJSON();
        error.setStatusCode(400);
        error.setError("Bad Request");
        error.setMessage(getErrInfo(String.valueOf(errcode)));
        if (ApiJson.listener != null) {
            ApiJson.listener.rsp2Cajas(error, "400");
        } else {
            Log.d("ERROR", "mensajeErrorCajas: " + " ApiJson.listener == null");
        }

    }

    @Override
    public InputInfo showTypeCoin(int timeout, final String title) {
        transView.showTypeCoinView(timeout, title, listener);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public InputInfo showInputUser(int timeout, String title, String label, int min, int max) {
        transView.showInputUser(timeout, title, label, min, max, listener);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Thread.currentThread().interrupt();
            Logger.error("Exception" + e.toString());
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.REFERENCE));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public void toasTrans(int errcode, boolean sound, boolean isErr) {
        if (isErr)
            transView.toasTransView(getErrInfo(String.valueOf(errcode)), sound);
        else
            transView.toasTransView(getStatusInfo(String.valueOf(errcode)), sound);
    }

    public void toasTrans(String errcode, boolean sound, boolean isErr) {
        if (isErr)
            transView.toasTransView(errcode, sound);
        else
            transView.toasTransView(errcode, sound);
    }



    @Override
    public void showMessage(String message, boolean transaccion) {
        switch (message) {
            case "Retire la tarjeta":
                transView.showRetireTarjeta();
                break;
            default:
                transView.showMsgInfo(60 * 1000, message, transaccion, false);
                break;
        }

    }

    /**
     * =============================================
     */

    private String getStatusInfo(String status) {
        try {
            String[] infos = Locale.getDefault().getLanguage().equals("zh") ?
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.STATUS, status) :
                    PAYUtils.getProps(PaySdk.getInstance().getContext(), TMConstants.STATUS_EN, status);
            if (infos != null) {
                return infos[0];
            }
        } catch (PaySdkException pse) {
            Logger.logLine(LogType.EXCEPTION, clase, pse.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, pse.getStackTrace());
            Logger.error("Exception" + pse.toString());
            Thread.currentThread().interrupt();
        }
        if (Locale.getDefault().getLanguage().equals("zh")) {
            return "未知信息";
        } else {
            return "Error Desconocido";
        }
    }

    @Override
    public void showCardImg(String img) {
        this.mLatch = new CountDownLatch(1);
        transView.showCardViewImg(img, listener);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
            info.setNextStyle(payStyle);
        }
    }

    @Override
    public InputInfo showSignature(int timeout, String title, String transType) {
        transView.showSignatureView(timeout, listener, title, transType);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public InputInfo showIngresoDataNumerico(int timeout, String tipoIngreso, String title,
                                             int longitudMaxima, String trx, long amount) {
        return showIngresoDataNumerico(timeout, tipoIngreso, "", title, longitudMaxima, trx, amount);
    }

    @Override
    public InputInfo showIngresoDataNumerico(int timeout, String tipoIngreso, String mensaje, String title, int longitudMaxima, String trx, long amount) {
        transView.showIngresoDataNumericoView(timeout, tipoIngreso, mensaje, title, longitudMaxima, trx, amount, listener);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public InputInfo showSeleccionTipoDeCuenta(int timeout) {
        transView.showSeleccionTipoDeCuentaView(timeout, listener);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public void showImprimiendo(int timeout) {
        transView.showImprimiendoView(timeout);
    }

    @Override
    public InputInfo showResult(int timeout, boolean aprobada, boolean isIconoWifi, boolean opciones, String mensajeHost) {
        transView.showResultView(timeout, aprobada, isIconoWifi, opciones, mensajeHost, listener);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
            info.setNextStyle(payStyle);
        }
        return info;
    }


    @Override
    public void showFinish() {
        if (modoCaja) {
            mensajeErrorCajasFinish("Transaccion Cancelada");
        }
        transView.showFinishView();
    }

    private void mensajeErrorCajasFinish(String mensaje) {
        ErrorJSON error = new ErrorJSON();
        error.setStatusCode(400);
        error.setError("Bad Request");
        error.setMessage(mensaje);
        if (ApiJson.listener != null) {
            ApiJson.listener.rsp2Cajas(error, "400");
        } else {
            Log.d("ERROR", "mensajeErrorCajas: " + " ApiJson.listener == null");
        }

    }

    @Override
    public InputInfo showBotones(int timeout, String titulo, ArrayList<ModeloBotones> botones) {
        transView.showBotonesView(timeout, titulo, botones, listener);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            info.setResultFlag(true);
            info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
            info.setNextStyle(payStyle);
        }
        return info;
    }

    @Override
    public InputInfo showMensajeConfirmacion(int timeout, ModeloMensajeConfirmacion modelo) {
        transView.showMensajeConfirmacionView(timeout, modelo, listener);
        this.mLatch = new CountDownLatch(1);
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        InputInfo info = new InputInfo();
        if (mRet == 1) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_user_cancel);
        } else {
            if (transView.getInput(InputManager.Mode.AMOUNT).equals("no")) {
                info.setResultFlag(false);
                info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
                info.setErrno(Tcode.T_user_cancel);
            } else {
                info.setResultFlag(true);
                info.setResult(transView.getInput(InputManager.Mode.AMOUNT));
                info.setNextStyle(payStyle);
            }
        }
        return info;
    }

    @Override
    public void showContacLessInfo(boolean finish) {
        transView.showContacLessInfoView(finish);
    }


}
