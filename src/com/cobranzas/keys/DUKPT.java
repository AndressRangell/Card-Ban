

package com.cobranzas.keys;

import com.cobranzas.encrypt_data.TripleDES;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinpadListener;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKException;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;
import com.pos.device.ped.Ped;
import com.pos.device.ped.PedRetCode;
import com.pos.device.ped.PinBlockCallback;
import com.pos.device.ped.PinBlockFormat;
import com.secure.api.PadView;

import java.util.Locale;

public class DUKPT {

    static String clase = "DUKPT.java";
    private static DUKPT instance = null;
    public static final int DUKPT_KEY_INDEX = 1;
    public static final int DES_KEY_INDEX = 2;

    private PinpadListener listener;
    private String pinCardNo;
    String pan;
    private String ksnStr;
    int timeout;

    //dukpt key
    private static final byte[] dukptIpek = {0x77, 0x0A, 0x1A, (byte) 0xA7, 0x62, (byte) 0xC0, 0x23, (byte) 0xC3, 0x5F, (byte) 0xF8, 0x6E, 0x54, 0x4A, (byte) 0xAC, 0x37, 0x69};
    private static final byte[] ksn = {0x00, 0x00, 0x12, 0x20, 0x01, 0x66, 0x25, 0x00, 0x00, 0x00};

    public static DUKPT getInstance() {
        return instance == null ? instance = new DUKPT() : instance;
    }

    public static String getCCKSN() {
        byte[] ccksn = Ped.getInstance().getDukptKsn(DUKPT_KEY_INDEX);
        Logger.debug("current ksn: " + ISOUtil.byte2hex(ccksn));

        if (ccksn != null) {
            return ISOUtil.byte2hex(ccksn);
        }
        return "";
    }

    public static byte[] encryptData(byte[] data) {
        byte[] output = Ped.getInstance().dukptEncryptRequest(DUKPT_KEY_INDEX, data);
        Logger.debug("Encrypt Data: " + ISOUtil.byte2hex(output));
        return output;
    }

    /**
     * @param ipek
     * @return
     */
    public static int injectIPEK(String ipek) throws SDKException {
        if (Ped.getInstance().checkKey(KeySystem.DUKPT_DES, KeyType.KEY_TYPE_DUKPTK, DUKPT_KEY_INDEX, 0) == 0) {
            try {
                Ped.getInstance().deleteKey(KeySystem.DUKPT_DES, KeyType.KEY_TYPE_DUKPTK, DUKPT_KEY_INDEX);
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }

        int ret = Ped.getInstance().createDukptKey(DUKPT_KEY_INDEX, ISOUtil.str2bcd(ipek, false), ISOUtil.str2bcd(TripleDES.getKsnInicial(), false));
        if (ret != 0) {
            Logger.debug("ped create dukpk key failed:" + ret);
            Logger.logLine(LogType.FLUJO, clase, "ped create dukpk key failed: " + ret);
        } else {
            Logger.debug("ped create dukpt key success\n" + "ipek:" + ISOUtil.byte2hex(dukptIpek)
                    + "\nksn:" + ISOUtil.byte2hex(ksn));
            Logger.logLine(LogType.FLUJO, clase, "ped create dukpt key success ");
        }
        return ret;
    }

    public static int checkIPEK() {
        int ret;

        Logger.logLine(LogType.FLUJO, clase, "checkIPEK:");

        ret = Ped.getInstance().checkKey(KeySystem.DUKPT_DES, KeyType.KEY_TYPE_DUKPTK, DUKPT_KEY_INDEX, 0);

        Logger.logLine(LogType.FLUJO, clase, "RET : " + ret);
        if (ret == 0) {
            Logger.debug("ped dukpk key already exist" + ret);
        } else {
            Logger.debug("ped dukpk key not exist" + ret);
        }
        return ret;
    }

    public static void deleteDUKPT() {
        try {
            Ped.getInstance().deleteKey(KeySystem.DUKPT_DES, KeyType.KEY_TYPE_DUKPTK, DUKPT_KEY_INDEX);
        } catch (SDKException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
    }


    /**
     * 获取联机PIN
     *
     * @param t
     * @param c
     * @param l
     */
    public void getPinDUKPT(int t, String amount, String c, PinpadListener l) {
        this.listener = l;
        this.timeout = t;
        this.pinCardNo = c;
        this.pan = c;
        //处理PED次数调用
        final PinInfo info = new PinInfo();
        if (null == l) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_invoke_para_err);
            listener.callback(info);
        } else if (pinCardNo == null || pinCardNo.equals("")) {
            info.setResultFlag(false);
            info.setErrno(Tcode.T_invoke_para_err);
            listener.callback(info);
        } else {
            Logger.debug("PinpadManager>>getPin>>");

            final Ped ped = Ped.getInstance();
            pinCardNo = pinCardNo.substring(pinCardNo.length() - 13, pinCardNo.length() - 1);
            pinCardNo = ISOUtil.padleft(pinCardNo, pinCardNo.length() + 4, '0');
            PadView padView = new PadView();
            if (Locale.getDefault().getLanguage().equals("zh")) {
                padView.setTitleMsg("华智融安全键盘");
                padView.setAmountTitle("金额:");
                padView.setAmount(PAYUtils.TwoWei(amount));
                padView.setPinTips("请输入密码:");
            } else {
                padView.setTitleMsg("Red Infonet - Ingreso de PIN");
                padView.setAmountTitle("Monto: Gs.");
                padView.setAmount(PAYUtils.TwoWei(amount));
                padView.setPinTips("Ingrese PIN");
            }

            ksnStr = getCCKSN();

            ped.setPinPadView(padView);
            new Thread() {
                @Override
                public void run() {
                    ped.getPinBlock(KeySystem.DUKPT_DES,
                            DUKPT_KEY_INDEX,
                            PinBlockFormat.PIN_BLOCK_FORMAT_0,
                            "0,4,5,6,7,8,9,10,11,12",
                            pinCardNo,
                            new PinBlockCallback() {
                                @Override
                                public void onPinBlock(int i, byte[] bytes) {
                                    switch (i) {
                                        case PedRetCode.NO_PIN:
                                            info.setResultFlag(true);
                                            info.setNoPin(true);
                                            break;
                                        case PedRetCode.TIMEOUT:
                                            info.setResultFlag(false);
                                            info.setErrno(Tcode.T_wait_timeout);
                                            break;
                                        case PedRetCode.ENTER_CANCEL:
                                            info.setResultFlag(false);
                                            info.setErrno(Tcode.T_user_cancel_pin_err);
                                            break;
                                        case 0:
                                            info.setResultFlag(true);
                                            info.setPinblock(bytes);
                                            info.setKsnString(ksnStr); //incrementa KSN
                                            break;
                                        default:
                                            info.setResultFlag(false);
                                            info.setErrno(i);
                                            break;
                                    }
                                    listener.callback(info);
                                }
                            });
                }
            }.start();
        }
    }
}
