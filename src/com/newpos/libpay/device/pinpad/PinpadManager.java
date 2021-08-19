package com.newpos.libpay.device.pinpad;

import android.util.Log;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKException;
import com.pos.device.icc.SlotType;
import com.pos.device.ped.IccOfflinePinApdu;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.MACMode;
import com.pos.device.ped.Ped;
import com.pos.device.ped.PedRetCode;
import com.pos.device.ped.PinBlockCallback;
import com.pos.device.ped.PinBlockFormat;
import com.pos.device.ped.RsaPinKey;
import com.secure.api.PadView;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cobranzas.keys.InjectMasterKey.MASTERKEYIDX;
import static com.cobranzas.keys.InjectMasterKey.TRACK2KEYIDX;

/**
 * Created by zhouqiang on 2017/3/14.
 *
 * @author zhouqiang
 * 密码键盘管理者
 */

public class PinpadManager {

    String clase = "PinpadManager.java";
    private static PinpadManager instance;

    private PinpadManager() {
    }

    public static PinpadManager getInstance() {
        if (instance == null) {
            instance = new PinpadManager();
        }
        return instance;
    }

    /**
     * 注入主密钥
     *
     * @param info
     * @return
     */
    public static int loadMKey(MasterKeyinfo info) {
        return Ped.getInstance().injectKey(
                PinpadKeytem.getKS(info.getKeySystem()),
                PinpadKeytype.getKT(info.getKeyType()),
                info.getMasterIndex(),
                info.getPlainKeyData());
    }

    /**
     * 注入工作密钥
     *
     * @param info
     * @return
     */
    public static int loadWKey(WorkKeyinfo info) {
        return Ped.getInstance().writeKey(
                PinpadKeytem.getKS(info.getKeySystem()),
                PinpadKeytype.getKT(info.getKeyType()),
                info.getMasterKeyIndex(),
                info.getWorkKeyIndex(),
                info.getMode(),
                info.getPrivacyKeyData());
    }

    private PinpadListener listener;
    private String pinCardNo;
    private int timeout;

    /**
     * 获取联机PIN
     *
     * @param t
     * @param c
     * @param l
     */
    public void getPin(int t, String amount, String c, PinpadListener l) {
        this.listener = l;
        this.timeout = t;
        this.pinCardNo = c;
        //TODO
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
                //padView.setTitleMsg("Newpos Secure Keyboard");
                //padView.setAmountTitle("Amount:");
                //padView.setAmount(PAYUtils.TwoWei(amount));
                padView.setPinTips("Ingrese PIN");
            }
            ped.setPinPadView(padView);
            new Thread() {
                @Override
                public void run() {
                    ped.getPinBlock(KeySystem.MS_DES,
                            MASTERKEYIDX,
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

    public void getOfflinePin(int i, OfflineRSA key, int counts, PinpadListener l) {
        this.listener = l;
        Ped ped = Ped.getInstance();
        PadView padView = new PadView();
        final String pinTips;
        if (Locale.getDefault().getLanguage().equals("zh")) {
            padView.setTitleMsg("华智融安全键盘");
            pinTips = "请输入脱机PIN\n" + "剩余 " + counts + " 次";
        } else {
            padView.setTitleMsg("Newpos Secure Keyboard");
            pinTips = "Please enter offline PIN\n" + "Left " + counts + " times";
        }
        final PinInfo info = new PinInfo();
        padView.setPinTips(pinTips);
        ped.setPinPadView(padView);
        IccOfflinePinApdu apdu = new IccOfflinePinApdu();
        if (i == 1) {
            RsaPinKey rsaPinKey = new RsaPinKey();
            rsaPinKey.setIccrandom(key.getIccRandom());
            rsaPinKey.setModlen(key.getMod().length);
            rsaPinKey.setMod(key.getMod());
            rsaPinKey.setExplen(key.getExp().length);
            rsaPinKey.setExp(key.getExp());
            apdu.setRsakey(rsaPinKey);
        }
        apdu.setCla(0x00);
        apdu.setIns(0x20);
        apdu.setLe(0x00);
        apdu.setLeflg(0x00);
        apdu.setP1(0x00);
        apdu.setP2(i == 1 ? 0x88 : 0x80);
        ped.getOfflinePin(i == 1 ? KeySystem.ICC_CIPHER : KeySystem.ICC_PLAIN,
                ped.getIccSlot(SlotType.USER_CARD),
                "0,4,5,6,7,8,9,10,11,12",
                apdu,
                new PinBlockCallback() {
                    @Override
                    public void onPinBlock(int i, byte[] bytes) {
                        if (bytes != null) {
                            Logger.debug("getOfflinePin->bytes:" + ISOUtil.byte2hex(bytes));
                        }
                        info.setPinblock(bytes);
                        if (i == PedRetCode.NO_PIN) {
                            info.setResultFlag(true);
                            info.setNoPin(true);
                        } else if (i == 0) {
                            info.setResultFlag(true);
                        } else {
                            info.setResultFlag(false);
                        }
                        listener.callback(info);
                    }
                });
    }

    /**
     * 获取加密后的MAC信息
     *
     * @param data   加密源数据
     * @param offset
     * @param len
     * @return 加密后的MAC信息
     */
    public byte[] getMac(byte[] data, int offset, int len) {
        byte[] macIn;
        macIn = new byte[((len + 7) >> 3) << 3];
        System.arraycopy(data, offset, macIn, 0, len);
        byte[] macBlock = Ped.getInstance().getMac(KeySystem.MS_DES, TMConfig.getInstance().getMasterKeyIndex(), MACMode.MAC_MODE_CUP_8, macIn);
        return macBlock;
    }

    /**
     * 中信银行算MAC采用CBC方式
     *
     * @param data
     * @param offset
     * @param len
     * @return
     */
    public byte[] getCITICMac(byte[] data, int offset, int len) {
        byte[] macIn;
        macIn = new byte[((len + 7) >> 3) << 3];
        System.arraycopy(data, offset, macIn, 0, len);
        byte[] macBlock = Ped.getInstance().getMac(KeySystem.MS_DES, TMConfig.getInstance().getMasterKeyIndex(), MACMode.MAC_MODE_CUP, macIn);
        return macBlock;
    }

    /**
     * 获取加密后的磁道信息
     * 磁道加密
     *
     * @param track
     * @return
     */
    public String getEac(int index, String track) {
        int ofs, org_len;
        StringBuffer trackEnc = new StringBuffer(120);
        byte[] bufSrc;
        byte[] bufDest;
        if (track == null || track.equals("")) {
            return null;
        }
        org_len = track.length();//37
        if (((org_len % 2) != 0)) {
            if (track.length() < 17) {
                return null;
            }
            ofs = org_len - 17;
        } else {
            if (track.length() < 18) {
                return null;
            }
            ofs = org_len - 18;
        }
        trackEnc.append(track.substring(0, ofs));
        bufSrc = ISOUtil.str2bcd(track.substring(ofs, ofs + 16), false);
        bufDest = Ped.getInstance().encryptAccount(KeySystem.MS_DES, index, Ped.TDEA_MODE_ECB, bufSrc);
        if (bufDest == null) {
            return null;
        }
        trackEnc.append(ISOUtil.byte2hex(bufDest));
        trackEnc.append(track.substring(ofs + 16, org_len));
        return trackEnc.toString();
    }

    public String encryptTrack2(String track) {

        Log.d("Track 2 ", track);

        int ofs, org_len, cont, fill_len = 0;
        int cantFs = 0;
        byte[] bufSrc;
        byte[] bufDest;

        StringBuffer trackEnc = new StringBuffer(120);

        if (track == null || track.equals("")) {
            return null;
        }
        org_len = fill_len = track.length();
        if (((org_len % 2) != 0)) {
            if (track.length() < 17) {
                return null;
            }
            ofs = org_len - 17;
        } else {
            if (track.length() < 18) {
                return null;
            }
            ofs = org_len - 18;
        }
/*
        // Cantidad de Bytes
        if((org_len%2) != 0){
            fill_len++; // Adiciona 1 F para completar numero pares de digitos 2 digitos : 1 byte
        }
        cont = fill_len / 2; // Cantidad bytes

        // Adicion de F's hasta completar multiplos de 8
        if((cont%8) != 0){
            fill_len = ((cont/8)+1) * 8;
        }else{
            fill_len = cont;
        }
        fill_len = fill_len * 2;// Cantidad de Bytes finales
*/
        String trackFormat = ISOUtil.padright(track, 48, 'F');
        bufSrc = ISOUtil.str2bcd(trackFormat, false);

        bufDest = Ped.getInstance().encryptAccount(KeySystem.MS_DES, TRACK2KEYIDX, Ped.TDEA_MODE_ECB, bufSrc);

        if (bufDest == null) {
            return null;
        }
        trackEnc.append(ISOUtil.byte2hex(bufDest));
        Log.d("Track 2 encriptado ", trackEnc.toString());
        return trackEnc.toString();
    }


    private Object offlinePinLock = new Object();
    private int mErrorCode;

    /**
     * get offline pin.
     *
     * @param tryTime
     * @param keyType
     * @param rsaPinkey
     * @param len
     * @param offlinePinBlcok
     * @return
     */
    public int getOfflinePin(int tryTime, int keyType, long amount, RsaPinKey rsaPinkey, final byte[] len, final byte[] offlinePinBlcok) {
        Ped ped = Ped.getInstance();
        PadView padView = new PadView();
        //long amount = 10000;
        /*if (amount >= 0) {
            double amt = (double)amount/(double)100;
            DecimalFormat df = new DecimalFormat("0.00");
            padView.setAmount(df.format(amt));
            padView.setAmountTitle("TO DO");
        }*/
        try {
            ped.setPinEntryTimeout(60);
        } catch (SDKException e) {
            e.printStackTrace();
            Logger.logLine(LogType.FLUJO, clase, e.getStackTrace());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }

        /*if(tryTime == Integer.MAX_VALUE)
            padView.setTitleMsg(" ");
        else {
            String temp = "Ingrese PIN";
            String strTip = String.format(temp, tryTime);
            padView.setTitleMsg(strTip);
        }*/

        padView.setPinTips("INGRESE PIN OFFLINE ");
        ped.setPinPadView(padView);

        KeySystem ks;
        if (keyType == 1)
            ks = KeySystem.ICC_CIPHER;
        else
            ks = KeySystem.ICC_PLAIN;
        int fd = ped.getIccSlot(SlotType.USER_CARD);
        Log.d("slot", "fd=" + fd);
        String pinLenLimit = "0,4,5,6,7,8,9,10,11,12";

        IccOfflinePinApdu apdu = new IccOfflinePinApdu();
        if (keyType == 1) {
            //Log.d("EMVTransDemo",EMVtest.bcd2str(rsaPinkey.getIccrandom(),0,8,false));
            apdu.setRsakey(rsaPinkey);
        }
        apdu.setCla(0x00);
        apdu.setIns(0x20);
        apdu.setLe(0x00);
        apdu.setLeflg(0x00);
        apdu.setP1(0x00);
        apdu.setP2((keyType == 1) ? 0x88 : 0x80);

        status.set(false);
        mErrorCode = -1;
        ped.getOfflinePin(ks, fd, pinLenLimit, apdu, new PinBlockCallback() {
            @Override
            public void onPinBlock(int result, byte[] pinBlock) {

                if (result != 0) {
                    mErrorCode = result;
                } else { //成功
                    if (pinBlock != null && len != null && offlinePinBlcok != null) {
                        len[0] = (byte) pinBlock.length;
                        System.arraycopy(pinBlock, 0, offlinePinBlcok, 0, len[0]);
                        mErrorCode = 0;
                    }
                }
                status.set(true);
                releaseLock(offlinePinLock);
            }
        });
        tryLock(offlinePinLock, TIME_OUT);

        return mErrorCode;
    }


    /**
     * lock the thread.
     *
     * @param timeOut
     */
    private void tryLock(Object lock, int timeOut) {
        if (lock != null) {
            try {
                synchronized (lock) {
                    lock.wait(timeOut * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.logLine(LogType.FLUJO, clase, e.getStackTrace());
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
    }

    /**
     * release lock
     */
    private void releaseLock(Object lock) {
        if (lock != null) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    /**
     * get pin status.
     */
    private AtomicBoolean status = new AtomicBoolean(false);

    /**
     * wait the time get offline pin.
     */
    private final static int TIME_OUT = 70;//Second
}
