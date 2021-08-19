package com.cobranzas.transactions.inyeccion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.encrypt_data.TripleDES;
import com.cobranzas.keys.DUKPT;
import com.cobranzas.keys.InjectMasterKey;
import com.cobranzas.menus.MenusActivity;
import com.cobranzas.transactions.RSA.RSATransport;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.TransInputPara;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.cobranzas.keys.InjectMasterKey.MASTERKEYIDX;
import static com.cobranzas.keys.InjectMasterKey.TRACK2KEYIDX;
import static com.cobranzas.keys.InjectMasterKey.threreIsKey;
import static com.cobranzas.keys.InjectMasterKey.threreIsKeyWK;

public class InyeccionLlave extends Trans implements TransPresenter {

    int contador;
    boolean activarSetting = false;
    Context ctx;
    int maximo = 3;
    Activity activity;
    RSATransport rsaTransport = new RSATransport();
    private String tagClaseInyeccion = "InyeccionLlave.java";


    public InyeccionLlave(Context ctx, String transEname, TransInputPara p, Activity activity) {
        super(ctx, transEname);
        TransEName = transEname;
        this.ctx = ctx;
        this.activity = activity;
        para = p;
        if (para != null) {
            transUI = para.getTransUI();
        }
    }

    @Override
    public void start() {
        procesoTransaccionLLaves();
    }

    private void procesoTransaccionLLaves() {
        do {
            transUI.handling(timeout, Tcode.Status.inyeccion_llaves);
            if (armarTrama2Llaves() && implementacionLlaves()) {
                activarSetting = false;
                transUI.trannSuccess(timeout, Tcode.Status.inyeccion_llaves_exitoso);
                break;
            } else {
                activarSetting = true;
                contador++;
            }
        } while (contador == maximo);

        if (activarSetting) {
            intentSetting("No se pudo inyectar la llaves");
        }
    }

    private boolean implementacionLlaves() {
        return inyectarLLaves() && verificarLlaves(activity);
    }

    private boolean inyectarLLaves() {
        String campo61 = iso8583.getfield(61);

        Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "inyectarLLaves:" + campo61);
        try {
            campo61 = rsaTransport.Decrypt(campo61);
            Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "KeyDecryp: " + campo61);
        } catch (InvalidKeyException e) {
            Log.d(tagClaseInyeccion, "InvalidKeyException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        } catch (IllegalBlockSizeException e) {
            Log.d(tagClaseInyeccion, "IllegalBlockSizeException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        } catch (BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            Log.d(tagClaseInyeccion, "BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        }

        if (campo61 != null && !campo61.isEmpty()) {
            String[] llaves = campo61.split("\\|");

            if (llaves.length > 1) {

                String llaveIPEK = llaves[0];
                Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "IPEK:" + llaveIPEK);
                String llaveTMK = llaves[1];
                Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "TMK:" + llaveTMK);
                if (!llaveIPEK.isEmpty() && !llaveTMK.isEmpty()) {
                    try {
                        Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "----------IPEK:" + llaveIPEK);
                        DUKPT.injectIPEK(llaveIPEK);

                        Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "----------MASTER KEY: " + llaveTMK);

                        byte[] encrypted2 = TripleDES.xor(ISOUtil.hex2byte(llaveTMK), ISOUtil.hex2byte(llaveTMK));
                        byte[] dataEncrypted = TripleDES.cryptBytes(encrypted2, 0, encrypted2);

                        if (InjectMasterKey.injectMk(ISOUtil.byte2hex(encrypted2)) == 0) {
                            InjectMasterKey.injectWorkingKey(ISOUtil.byte2hex(dataEncrypted));
                            Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "----------SALE DE INYECCION MASTER KEY: " + llaveTMK);
                            return true;
                        }

                    } catch (SDKException e) {
                        e.printStackTrace();
                        Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
                    }
                }

            }

        }
        return false;
    }


    private boolean verificarLlaves(Activity activity) {
        Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "------------verificarLlaves");

        Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "activity ++++++ " + activity);
        return DUKPT.checkIPEK() == 0 && (threreIsKey(MASTERKEYIDX, "Debe cargar Master Key", activity) &&
                threreIsKeyWK(TRACK2KEYIDX, "Debe cargar Work key", activity));
    }

    private boolean armarTrama2Llaves() {
        try {
            retVal = sendInyeccionLlaves();
            if (retVal == 0) {
                return true;
            }
        } catch (InvalidKeyException e) {
            Log.d(tagClaseInyeccion, "InvalidKeyException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        } catch (BadPaddingException e) {
            Log.d(tagClaseInyeccion, "BadPaddingException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        } catch (NoSuchAlgorithmException e) {
            Log.d(tagClaseInyeccion, "NoSuchAlgorithmException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        } catch (IllegalBlockSizeException e) {
            Log.d(tagClaseInyeccion, "IllegalBlockSizeException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        } catch (NoSuchPaddingException e) {
            Log.d(tagClaseInyeccion, "NoSuchPaddingException: " + e.getMessage());
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, tagClaseInyeccion, e.getStackTrace());
        }
        return false;
    }

    private int sendInyeccionLlaves() throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        setFixedDatas();
        setFieldInyeccionLlaves();

        return sendRcvdInit();
    }

    private void setFieldInyeccionLlaves() throws
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        ProcCode = "920000";

        iso8583.setTpdu("6000030000");

        iso8583.setHasMac(false);
        iso8583.clearData();

        LocalTime = PAYUtils.getLocalTime();
        LocalDate = PAYUtils.getLocalDate();

        if (MsgID != null) {
            iso8583.setField(0, MsgID);
        }
        if (ProcCode != null) {
            iso8583.setField(3, ProcCode);
        }
        if (TraceNo != null) {
            iso8583.setField(11, TraceNo);
        }
        if (TermID != null) {
            iso8583.setField(41, TermID);
        }

        Field60 = TripleDES.getKsnInicial2Hsm();
        if (Field60 != null) {
            iso8583.setField(60, Field60);
        }

        rsaTransport.genKeyPair(4096);
        Field61 = rsaTransport.getPublicKeyString();
        if (Field61 != null) {
            iso8583.setField(61, Field61);
        }
    }

    @Override
    public ISO8583 getISO8583() {
        return null;
    }

    private int sendRcvdInit() {
        int rta;
        Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "sendRcvdInit : Pendiente");
        rta = retriesConnect(0, true, false);
        if (rta == Tcode.T_socket_err) {
            Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "return T_socket_err");
            return rta;
        }
        transUI.handling(timeout, Tcode.Status.send_data_2_server);
        if (send() == -1) {
            Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "Error enviando InyeccionLlave");
            return Tcode.T_send_err;
        }
        transUI.handling(timeout, Tcode.Status.send_over_2_recv);
        byte[] respData = recive();
        netWork.close();
        if (respData == null || respData.length <= 0) {
            Logger.logLine(LogType.FLUJO, tagClaseInyeccion, "T_receive_err despues de closer");
            return Tcode.T_receive_err;
        } else {
            int rtn = iso8583.unPacketISO8583(respData);
            if (rtn != 0) {
                return Tcode.T_receive_err;
            } else {
                return Tcode.T_success;
            }
        }
    }

    public void intentSetting(String mensaje) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, MenusActivity.class);
        intent.putExtra(DefinesBANCARD.MENSAJE_ERROR_INYECCION_LLAVES, mensaje);
        context.startActivity(intent);
    }
}
