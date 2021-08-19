package com.newpos.libpay.process;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.android.desert.keyboard.InputInfo;
import com.cobranzas.cajas.ProcesamientoCajas;
import com.cobranzas.cajas.waitConfirmacionCaja;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.transactions.common.CommonFunctionalities;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.presenter.TransUI;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.TransInputPara;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKException;
import com.pos.device.config.DevConfig;
import com.pos.device.emv.CandidateListApp;
import com.pos.device.emv.CoreParam;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVCallback;
import com.pos.device.emv.IEMVHandler;
import com.pos.device.emv.TerminalMckConfigure;
import com.pos.device.icc.ContactCard;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.OperatorMode;
import com.pos.device.icc.SlotType;
import com.pos.device.icc.VCC;
import com.pos.device.ped.RsaPinKey;
import com.pos.device.picc.EmvContactlessCard;
import com.pos.device.picc.PiccReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import static cn.desert.newpos.payui.master.MasterControl.incardTable;
import static cn.desert.newpos.payui.master.MasterControl.mcontext;
import static com.cobranzas.actividades.MainActivity.modoCaja;
import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.cobranzas.actividades.StartAppBANCARD.tablaDevice;
import static com.cobranzas.defines_bancard.DefinesBANCARD.INGRESO_MONTO;
import static com.newpos.libpay.trans.finace.FinanceTrans.LOCAL;
import static com.newpos.libpay.trans.finace.FinanceTrans.validarMontos;


/**
 * EMV交易流程
 *
 * @author zhouqiang
 */
public class EmvTransaction {

    static String clase = "EmvTransaction.java";
    final int wOnlineTags[] = {0x9F26, // AC (Application Cryptogram)
            0x9F27, // CID
            0x9F10, // IAD (Issuer Application Data)
            0x9F37, // Unpredicatable Number
            0x9F36, // ATC (Application Transaction Counter)
            0x95, // TVR
            0x9A, // Transaction Date
            0x9C, // Transaction Type
            0x9F02, // Amount Authorised
            0x5F2A, // Transaction Currency Code
            0x82, // AIP
            0x9F1A, // Terminal Country Code
            0x9F03, // Amount Other
            0x9F33, // Terminal Capabilities
            // opt
            0x9F34, // CVM Result
            0x9F35, // Terminal Type
            0x9F1E, // IFD Serial Number
            0x84, // Dedicated File Name
            0x9F09, // Application Version #
            0x9F41, // Transaction Sequence Counter
            0x5F20, //CardName Holder
            // 0x5F34, // PAN Sequence Number
            0};
    final int wISR_tags[] = {0x9F33, // Terminal Capabilities
            0x95, // TVR
            0x9F37, // Unpredicatable Number
            0x9F1E, // IFD Serial Number
            0x9F10, // Issuer Application Data
            0x9F26, // Application Cryptogram
            0x9F36, // Application Tranaction Counter
            0x82, // AIP
            0xDF31, // 发卡行脚本结果
            0x9F1A, // Terminal Country Code
            0x9A, // Transaction Date
            0};
    final int reversal_tag[] = {0x95, // TVR
            0x9F1E, // IFD Serial Number
            0x9F10, // Issuer Application Data
            0x9F36, // Application Transaction Counter
            0xDF31, // 发卡行脚本结果
            0};
    protected String CVV;
    String TipoVenta;
    private IccReader icCard = null;
    private ContactCard contactCard = null;
    private IEMVHandler emvHandler = null;
    private PiccReader nfcCard = null;
    private EmvContactlessCard emvContactlessCard = null;
    private int timeout;
    private int offlinePinTryCounts;
    private boolean isCajas;
    private long Amount;
    private int inputMode;
    private long otherAmount;
    private long AmountBase0;
    private long AmountXX;
    private long ServiceAmount;
    private long TipAmount;
    private long IvaAmount;
    private long tips;
    private int numCuotas;
    private String typeCoin;
    private String typeTrans;
    private String traceNo;
    private String rspCode;// 39
    private String authCode;// 38
    private byte[] rspICCData;// 55
    private int onlineResult;// 成功和失败
    private String pinBlock = "";
    private String pinBlokOffline = "";
    private String ksnEmv = "";
    private String ECAmount = null;
    private int retExpApp;
    private int ret;
    private TransUI transUI;
    // 0X8E, //CVM
    private TransInputPara para;
    private IEMVCallback.EMVInitListener emvInitListener = new IEMVCallback.EMVInitListener() {
        @Override
        public int candidateAppsSelection() {
            Logger.debug("======candidateAppsSelection=====");
            int[] numData = new int[1];
            CandidateListApp[] listapp = new CandidateListApp[32];
            try {
                listapp = emvHandler.getCandidateList();
                if (listapp != null) {
                    numData[0] = listapp.length;
                } else {
                    return -1;
                }
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
            int ret = 0;
            if (listapp.length > 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < numData[0]; i++) {
                    Logger.debug("应用名称：" + listapp[i].toString());
                    if (i == 0) {
                        sb.append(new String(listapp[i].gettCandAppName()));
                    } else {
                        sb.append("," + new String(listapp[i].gettCandAppName()));
                    }
                }
                if (numData[0] > 1) {
                    //TODO 周强  提示用户选择应用
                    Logger.debug("卡片多应用选择");
                    int select = transUI.showCardApplist(timeout, sb.toString().split(","));
                    if (select >= 0) {
                        ret = select;
                    } else {
                        retExpApp = Tcode.T_user_cancel_operation;
                        return -1;
                    }
                }
                Logger.debug("EMV>>多应用选择>>用户选择的应用ret=" + ret);
                return ret;
            } else {
                return -1;
            }
        }

        @Override
        public void multiLanguageSelection() {
            byte[] tag = new byte[]{0x5F, 0x2D};
            int ret = emvHandler.checkDataElement(tag);
            // 从内核读aid 0x9f06 设置是否支持联机PIN
            Logger.debug("===multiLanguageSelection==ret:" + ret);
        }

        @Override
        public int getAmount(int[] transAmount, int[] cashBackAmount) {
            if (!modoCaja) {
                Logger.debug("====getAmount======" + Amount);
                if (para.isNeedAmount()) {
                    if (Amount <= 0) {
                        // 调用输入金额
                        Logger.debug("EMV>>需要输入金额且金额参数未0>>EMV流程执行获取金额的回调");
                        InputInfo info = transUI.showIngresoDataNumerico(timeout, INGRESO_MONTO, "Monto:", 9, "Venta", 0);
                        if (info.isResultFlag()) {
                            Amount = Long.parseLong(info.getResult());
                            para.setAmount(Amount);
                            para.setOtherAmount(-1);
                            Logger.debug("EMV>>用户输入的金额=" + Amount);
                            transUI.handling(timeout, Tcode.Status.connecting_center, "Procesando");
                        } else {
                            //TODO
                        }
                        otherAmount = 0;
                    } else {
//                    transAmount[0] = Integer.valueOf(Amount + "");
//                    cashBackAmount[0] = Integer.valueOf(otherAmount + "");
                    }
                }
            }
            return 0;
        }

        @Override
        public int getPin(int[] pinLen, byte[] cardPin) {
            Logger.debug("=====getOfflinePin======");
            // 读PED倒计时并为零 才继续执行
            // 请输入OFFLINE PIN
            return 0;
        }

        @Override
        public int pinVerifyResult(int tryCount) {
            Logger.debug("======pinVerifyResult=======" + tryCount);
            // 处理脱机pin 是否成功 失败提示重试次数
            //TODO
            if (tryCount == 0) {
                Logger.debug("EMV>>pinVerifyResult>>脱机PIN校验成功");
            } else if (tryCount == 1) {
                offlinePinTryCounts = 1;
                Logger.debug("EMV>>pinVerifyResult>>脱机PIN输入只剩下最后一次机会");
            } else {
                offlinePinTryCounts = tryCount;
                Logger.debug("EMV>>pinVerifyResult>>脱机PIN输入还剩下" + tryCount + "次机会");
            }
            return 0;
        }

        @Override
        public int checkOnlinePIN() {
            Logger.debug("checkOnlinePIN pass=" + para.isNeedPass());
            if (para.isNeedPass()) {
                Logger.debug("=====checkOnlinePIN=======");
                byte[] val = new byte[16];
                String cardNum = "";
                try {
                    val = emvHandler.getDataElement(new byte[]{0x5A});
                } catch (SDKException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                if (val != null) {
                    cardNum = ISOUtil.trimf(ISOUtil.byte2hex(val, 0, val.length));
                    Logger.debug("EMV>>获取联机PIN>>卡号=" + cardNum);
                }
                long newAmount = 0;
                if (otherAmount > 0) {
                    newAmount = Amount + otherAmount;
                } else {
                    newAmount = Amount;
                }
                PinInfo info = transUI.getPinpadOnlinePinDUKPT(timeout, String.valueOf(newAmount), cardNum);

                if (info.isResultFlag()) {
                    if (info.isNoPin()) {
                        if (getCardAID().equals("A000000333010102")) {
                            return 0;
                        } else {
                            pinBlock = "NULL";
                            return -1;
                        }
                    } else {
                        pinBlock = ISOUtil.hexString(info.getPinblock());
                        ksnEmv = info.getKsnString();

                        Logger.debug("EMV>>获取联机PIN>>pinBlock=" + pinBlock);
                    }
                } else {
                    //pinBlock = null;
                    pinBlock = "CANCEL";
                    return -1;
                }
                return 0;
            } else {
                Logger.debug("EMV>>checkOnlinePIN>>ret=0");
                return 0;
            }
        }

        /** 核对身份证证件 **/
        @Override
        public int checkCertificate() {
            Logger.debug("=====checkCertificate====");
            return 0;
        }

        @Override
        public int onlineTransactionProcess(byte[] brspCode, byte[] bauthCode,
                                            int[] authCodeLen, byte[] bauthData, int[] authDataLen,
                                            byte[] script, int[] scriptLen, byte[] bonlineResult) {
            Logger.debug("==onlineTransactionProcess========");
            brspCode[0] = 0;
            brspCode[1] = 0;
            authCodeLen[0] = 0;
            scriptLen[0] = 0;
            authDataLen[0] = 0;
            bonlineResult[0] = (byte) onlineResult;
            if (rspCode == null || rspCode.equals("") || onlineResult != 0) {
                return 0;
            } else {
                System.arraycopy(rspCode.getBytes(), 0, brspCode, 0, 2);
            }
            if (authCode == null || authCode.equals("")) {
                authCodeLen[0] = 0;
            } else {
                authCodeLen[0] = authCode.length();
                System.arraycopy(authCode.getBytes(), 0, bauthCode, 0, authCodeLen[0]);
            }
            if (rspICCData != null && rspICCData.length > 0) {
                authDataLen[0] = PAYUtils.get_tlv_data(rspICCData, rspICCData.length, 0x91, bauthData, false);
                byte[] scriptTemp = new byte[256];
                int scriptLen1 = PAYUtils.get_tlv_data(rspICCData, rspICCData.length, 0x71, scriptTemp, true);
                System.arraycopy(scriptTemp, 0, script, 0, scriptLen1);
                int scriptLen2 = PAYUtils.get_tlv_data(rspICCData, rspICCData.length, 0x72, scriptTemp, true);
                System.arraycopy(scriptTemp, 0, script, scriptLen1, scriptLen2);
                scriptLen[0] = scriptLen1 + scriptLen2;
            }
            bonlineResult[0] = (byte) onlineResult;
            Logger.debug("onlineTransactionProcess return_exit 0.");
            return 0;
        }

        @Override
        public int issuerReferralProcess() {
            Logger.debug("=====issuerReferralProcess======");
            return 0;
        }

        @Override
        public int adviceProcess(int firstFlg) {
            Logger.debug("=====adviceProcess======");
            return 0;
        }

        @Override
        public int checkRevocationCertificate(int caPublicKeyID, byte[] RID,
                                              byte[] destBuf) {
            Logger.debug("===checkRevocationCertificate==");
            return -1;
        }

        /**
         * 黑名单
         */
        @Override
        public int checkExceptionFile(int panLen, byte[] pan, int panSN) {
            Logger.debug("==checkExceptionFile=");
            return -1;
        }

        /**
         * 判断IC卡脱机的累计金额 超过就强制联机
         */
        @Override
        public int getTransactionLogAmount(int panLen, byte[] pan, int panSN) {
            Logger.debug("======getTransactionLogAmount===");
            return 0;
        }

        //增加脱机PIN回调接口
        @Override
        public int getOfflinePin(int i, RsaPinKey rsaPinKey, byte[] bytes, byte[] bytes1) {
            int ret = PinpadManager.getInstance().getOfflinePin(offlinePinTryCounts, i, Amount, rsaPinKey, bytes, bytes1);

            Log.d("EmvTransaction", "getOfflinePin: " + ret);
            if (ret == 0) {
                setPinBlokOffline("PIN OFFLINE");
            }

            offlinePinTryCounts = Integer.MAX_VALUE;
            return ret;

        }
        /*@Override
        public int getOfflinePin(int i, RsaPinKey rsaPinKey, byte[] recvLen, byte[] recvData) {
            Logger.debug("======getOfflinePin===");
            try {
                //获取脱机PIN次数
                byte[] counts = emvHandler.getDataElement(new byte[]{(byte) 0x9F, (byte) 0x17});
                if (counts != null) {
                    Logger.debug("getDataElement(9F17):" + ISOUtil.hexString(counts));
                    offlinePinTryCounts = counts[0];
                }
            } catch (SDKException e) {
                e.printStackTrace();
            }
            OfflineRSA offlineRSA = null;
            if (i == 1) {
                offlineRSA.setIccRandom(rsaPinKey.getIccrandom());
                offlineRSA.setModLen(rsaPinKey.getMod().length);
                offlineRSA.setMod(rsaPinKey.getMod());
                offlineRSA.setExpLen(rsaPinKey.getExp().length);
                offlineRSA.setExp(rsaPinKey.getExp());
            }
            PinInfo info = transUI.getPinpadOfflinePin(timeout, i, offlineRSA, offlinePinTryCounts);
            if (info.isResultFlag()) {
                byte[] rsp = info.getPinblock();
                if (rsp == null) {
                    return -1;
                }
                Logger.debug("getOfflinePin->rsp:" + ISOUtil.byte2hex(rsp));
                recvLen[0] = (byte) rsp.length;
                System.arraycopy(rsp, 0, recvData, 0, recvLen[0]);
                Logger.debug("getOfflinePin return 0 to kernel");
                return 0;
            }
            return info.getErrno();
        }*/
    };
    private IEMVCallback.ApduExchangeListener apduExchangeListener = new IEMVCallback.ApduExchangeListener() {
        @Override
        public int apduExchange(byte[] sendData, int[] recvLen, byte[] recvData) {
            Logger.debug("==apduExchangeListener===");
            Logger.debug("sendData:" + ISOUtil.byte2hex(sendData));
            if (inputMode == Trans.ENTRY_MODE_NFC) {
                int[] status = new int[1];
                long start = SystemClock.uptimeMillis();
                while (true) {
                    if (SystemClock.uptimeMillis() - start > 3 * 1000) {
                        break;
                    }
                    try {
                        status[0] = emvContactlessCard.getStatus();
                    } catch (SDKException e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    }
                    if (status[0] == EmvContactlessCard.STATUS_EXCHANGE_APDU) {
                        break;
                    }
                    try {
                        Thread.sleep(6);
                    } catch (Exception e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    }
                }
                int len = 0;
                try {
                    byte[] rawData = emvContactlessCard.transmit(sendData);
                    if (rawData != null) {
                        Logger.debug("rawData = " + ISOUtil.hexString(rawData));
                        len = rawData.length;
                    }
                    if (len <= 0) {
                        return -1;
                    }
                    System.arraycopy(rawData, 0, recvData, 0, rawData.length);
                } catch (SDKException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                if (len >= 0) {
                    recvLen[0] = len;
                    Logger.debug("Data received from card:" + ISOUtil.byte2hex(recvData, 0, recvLen[0]));
                    return 0;
                }
                return -1;
            }
            if (Trans.ENTRY_MODE_ICC == inputMode) {
                int len = 0;
                try {
                    if (contactCard == null || icCard == null) {
                        return -1;
                    } else {
                        byte[] rawData = icCard.transmit(contactCard, sendData);
                        if (rawData != null) {
                            Logger.debug("rawData = " + ISOUtil.hexString(rawData));
                            len = rawData.length;
                        }
                        if (len <= 0) {
                            return -1;
                        }
                        System.arraycopy(rawData, 0, recvData, 0, rawData.length);
                    }
                } catch (SDKException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                if (len >= 0) {
                    recvLen[0] = len;
                    Logger.debug("Data received from card:" + ISOUtil.byte2hex(recvData, 0, recvLen[0]));

                    //if (!PAYUtils.stringToBoolean(rango.getPERMITIR_TARJ_EXP())) {//Validacion personalizada
                    if (isAppExpDate(recvData)) {
                        retExpApp = Tcode.T_err_exp_date_app;
                        //return -1;
                    }
                    //}
                    return 0;
                }
                return -1;
            }
            return -1;
        }
    };
    /**
     * 初始化内核专用构造器
     */
    public EmvTransaction() {
        emvHandler = EMVHandler.getInstance();
    }

    /**
     * EMV流程专用构造器
     */
    public EmvTransaction(TransInputPara p, String typeTrans) {
        this.emvHandler = EMVHandler.getInstance();
        this.para = p;
        this.transUI = para.getTransUI();
        this.typeTrans = typeTrans;
        this.retExpApp = -1;
        Logger.debug("amount = " + para.isNeedAmount());
        Logger.debug("online = " + para.isNeedOnline());
        Logger.debug("pass = " + para.isNeedPass());
        Logger.debug("eccash = " + para.isECTrans());
        Logger.debug("print = " + para.isNeedPrint());
        if (para.isNeedAmount()) {
            this.Amount = para.getAmount();
            this.otherAmount = para.getOtherAmount();
        }
        this.inputMode = para.getInputMode();
        if (inputMode == Trans.ENTRY_MODE_NFC) {
            try {
                nfcCard = PiccReader.getInstance();
                emvContactlessCard = EmvContactlessCard.connect();
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
        if (inputMode == Trans.ENTRY_MODE_ICC) {
            try {
                icCard = IccReader.getInstance(SlotType.USER_CARD);
                contactCard = icCard.connectCard(VCC.VOLT_5, OperatorMode.EMV_MODE);
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
    }


    public EmvTransaction(TransInputPara p, String typeTrans, String tipoVenta, int timeout) {
        this.emvHandler = EMVHandler.getInstance();
        this.para = p;
        this.transUI = para.getTransUI();
        this.typeTrans = typeTrans;
        this.TipoVenta = tipoVenta;
        this.retExpApp = -1;
        this.timeout = timeout;
        Logger.debug("amount = " + para.isNeedAmount());
        Logger.debug("online = " + para.isNeedOnline());
        Logger.debug("pass = " + para.isNeedPass());
        Logger.debug("eccash = " + para.isECTrans());
        Logger.debug("print = " + para.isNeedPrint());
        if (para.isNeedAmount()) {
            this.Amount = para.getAmount();
            this.otherAmount = para.getOtherAmount();
        }
        this.inputMode = para.getInputMode();
        if (inputMode == Trans.ENTRY_MODE_NFC) {
            try {
                nfcCard = PiccReader.getInstance();
                emvContactlessCard = EmvContactlessCard.connect();
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
        if (inputMode == Trans.ENTRY_MODE_ICC) {
            try {
                icCard = IccReader.getInstance(SlotType.USER_CARD);
                contactCard = icCard.connectCard(VCC.VOLT_5, OperatorMode.EMV_MODE);
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
    }

    public String getPinBlokOffline() {
        return pinBlokOffline;
    }

    public void setPinBlokOffline(String pinBlokOffline) {
        this.pinBlokOffline = pinBlokOffline;
    }

    public String getTraceNo() {
        return this.traceNo;
    }

    public void setTraceNo(String traceNo) {
        this.traceNo = traceNo;
    }

    public String getAuthCode() {
        return authCode;
    }

    /**
     * Inyectar monto y tipo de moneda al kernel emv
     */
    private void setAmountAndCurrencyCodeEMV() {
        String amountTmp1;

        //Amount

        amountTmp1 = ISOUtil.padleft(para.getAmount() / 100 + "", 12, '0');

        byte[] amountTmp2 = ISOUtil.str2bcd(amountTmp1, false);
        emvHandler.setDataElement(new byte[]{(byte) 0x9f, 0x02}, amountTmp2);

        if (otherAmount > 0) {
            amountTmp1 = ISOUtil.padleft(otherAmount / 100 + "", 12, '0');

            amountTmp2 = ISOUtil.str2bcd(amountTmp1, false);
            emvHandler.setDataElement(new byte[]{(byte) 0x9f, 0x03}, amountTmp2);
        }

        //Currency code
        if (typeCoin == LOCAL) {
            emvHandler.setDataElement(new byte[]{(byte) 0x5f, 0x2a}, new byte[]{0x06, 0x00});
        } else {
            emvHandler.setDataElement(new byte[]{(byte) 0x5f, 0x2a}, new byte[]{0x06, 0x00});
        }
    }

    /**
     * Inyectar el tag 9F1E IFDSerialNo
     */
    private void getSerialNoEMV() {
        String SN = DevConfig.getSN();
        SN = SN.substring(2, SN.length());
        byte[] serialNo = SN.getBytes();
        emvHandler.setDataElement(new byte[]{(byte) 0x9f, 0x1e}, serialNo);
    }

    /**
     * EMV交易流程开始
     *
     * @return
     */
    public int start() {
        Logger.debug("EmvTransaction>>EMVTramsProcess");
        initEmvKernel();
        ret = emvReadData(true);
        if (ret != 0) {
            return ret;
        }

        if (!modoCajaPOS(getCardNo())) {
            return Tcode.T_monto_invalido;
        }

        return procesoValidadorTarjeta();
    }

    private boolean modoCajaPOS(String pan) {

        {
            final boolean[] respuesta = {false};
            if (modoCaja && isCajas) {
//                Activity activity = (Activity) mcontext;
                final CountDownLatch latch = new CountDownLatch(1);
                Logger.logLine(LogType.ERROR, clase + " Metodo : modoCajaPOS", "Ingreso al Metodo  de CajaPOS");

                final ProcesamientoCajas procesamientoCajas = new ProcesamientoCajas(mcontext, transUI);
                procesamientoCajas.lecturaTarjeta2Caja(pan, traceNo);

                Handler handler = new Handler(Looper.getMainLooper());

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        transUI.handling(timeout, Tcode.Status.handling);
                        Logger.logLine(LogType.FLUJO, clase + " Metodo : modoCajaPOS", "Esperando Respuesta de Caja");
                        procesamientoCajas.counterDownTimer(timeout, "Tiempo de espera de ingreso de datos agotado", "waitConfirmacionCaja");
                        procesamientoCajas.esperaConfirmacionCaja(new waitConfirmacionCaja() {
                            @Override
                            public void waitConfirmacionCaja(JSONObject jsonObject) {
                                try {
                                    Log.d("ESPERAR CONFIRMACION --", "waitConfirmacionCaja: " + jsonObject.getString("monto"));
                                    Logger.logLine(LogType.FLUJO, clase + " Metodo : waitConfirmacionCaja", "Monto Recibido -> " + jsonObject.getString("monto"));
                                    if (!jsonObject.isNull("monto")) {
                                        if (!jsonObject.getString("monto").equals("0")) {
                                            Logger.logLine(LogType.FLUJO, clase + " Metodo : waitConfirmacionCaja", "Ingresando al Metodo de Validacion de Montos ");
                                            String monto = jsonObject.getString("monto") + "00";
                                            boolean respuestaVal = validarMontos(monto);
                                            Logger.logLine(LogType.FLUJO, clase + " Metodo : waitConfirmacionCaja", "Respuesta Validacion -> " + respuestaVal);
                                            if (respuestaVal) {
                                                Logger.logLine(LogType.FLUJO, clase + " Metodo : waitConfirmacionCaja", "Se llena la Variable Amount ");
                                                Amount = Long.parseLong(monto);
                                                para.setAmount(Amount);
                                                respuesta[0] = true;
                                                procesamientoCajas.deleteTimer();
                                                latch.countDown();
                                            } else {
                                                procesamientoCajas.deleteTimer();
                                                transUI.showError(timeout, Tcode.T_monto_invalido, false);
                                            }
                                        } else {
                                            Logger.logLine(LogType.FLUJO, clase + " Metodo : waitConfirmacionCaja", "El Valor de Monto -> 0 ");
                                            procesamientoCajas.deleteTimer();
                                            latch.countDown();
                                        }
                                    } else {
                                        Logger.logLine(LogType.FLUJO, clase + " Metodo : waitConfirmacionCaja", "No Ingreso a la Validaciones Monto Nulo");
                                    }
                                } catch (JSONException e) {
                                    procesamientoCajas.mensajeErrorCajas("Error del SDK Venta " + e.getMessage(), 500);
                                    Logger.logLine(LogType.EXCEPTION, clase + " Metodo : modoCajaPOS", e.getMessage());
                                    Logger.logLine(LogType.EXCEPTION, clase + "Metodo : modoCajaPOS", e.getStackTrace());
                                    e.printStackTrace();

                                    Log.d("JSONException", "waitConfirmacionCaja: " + e.getMessage());
                                }
                            }
                        });
                    }
                };

                handler.post(runnable);

                // activity.runOnUiThread();

                try {
                    latch.await();
                    handler.removeCallbacks(runnable);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, clase + " Metodo : modoCajaPOS", e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase + "Metodo : modoCajaPOS", e.getStackTrace());
                }

            } else {
                respuesta[0] = true;
            }
            return respuesta[0];
        }
    }

    private int procesoValidadorTarjeta() {
        if (para.isNeedConfirmCard()) {
            Logger.debug("EmvTransaction>>EMVTramsProcess>>提示确认卡号");
            String cn = getCardNo();
            Logger.debug("EmvTransaction>>EMVTramsProcess>>卡号=" + cn);
            ret = transUI.showCardConfirm(timeout, cn);
            if (ret != 0) {
                return Tcode.T_user_cancel_operation;
            }
        }

        if (!incardTable(getCardNo())) {
            ret = Tcode.T_unsupport_card;
            transUI.showError(timeout, Tcode.T_unsupport_card, false);
            return ret;
        }


        if (!verificaPedirVuelto()) {
            return Tcode.T_user_cancel_operation;
        }

        if (!typeTrans.equals(Trans.Type.ANULACION)) {
            CommonFunctionalities.showCardImage(transUI);
        }

        //Inyectar monto y tipo de moneda al kernel emv
        setAmountAndCurrencyCodeEMV();

        //Tag 9f1e IFDSerialNo
        getSerialNoEMV();

        if (!para.isEmvAll()) {
            return 0;
        }
        Logger.debug("EmvTransaction>>EMVTramsProcess>>processRestriction");
        emvHandler.processRestriction();
        Logger.debug("EmvTransaction>>EMVTramsProcess>>持卡人认证");
        try {
            ret = emvHandler.cardholderVerify();
        } catch (SDKException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        Logger.debug("EmvTransaction>>EMVTramsProcess>>cardholderVerify=" + ret);
        if (ret != 0) {
            Logger.debug("EmvTransaction>>EMVTramsProcess>>cardholderVerify fail");
            return Tcode.T_card_holder_auth_err;
        }

        Logger.debug("EmvTransaction>>EMVTramsProcess>>终端风险分析");
        try {
            ret = emvHandler.terminalRiskManage();
        } catch (SDKException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        Logger.debug("EmvTransaction>>EMVTramsProcess>>terminalRiskManage=" + ret);
        if (ret != 0) {
            Logger.debug("EmvTransaction>>EMVTramsProcess>>terminalRiskManage fail");
            return Tcode.T_terminal_action_ana_err;
        }

        Logger.debug("EmvTransaction>>EMVTramsProcess>>是否联机");
        boolean isNeedOnline = false;
        try {
            isNeedOnline = emvHandler.terminalActionAnalysis();
        } catch (SDKException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            if (e.toString().indexOf("2069") >= 0) {
                if (pinBlock != null) {
                    if (pinBlock.equals("CANCEL")) {
                        return Tcode.T_user_cancel;
                    } else {

                    }
                } else {
                    return Tcode.T_decline_offline;
                }


            }
        }
        Logger.debug("EmvTransaction>>EMVTramsProcess>>terminalActionAnalysis=" + isNeedOnline);
        if (isNeedOnline) {
            Logger.debug("EmvTransaction>>EMVTramsProcess>>联机交易");
            Logger.debug("EMV完整流程结束");
            return 1;
        }
        Logger.debug("EmvTransaction>>EMVTramsProcess>>脱机批准");
        Logger.debug("EMV完整流程结束");
        return 0;
    }

    private boolean verificaPedirVuelto() {

        long rta;
        if (TipoVenta.equals(DefinesBANCARD.CONSULTA_DEUDAS)) {
            otherAmount = para.getOtherAmount();
            if (para.isNeedOtherAmount() == DefinesBANCARD.DATO_HABILITADO) {
                rta = CommonFunctionalities.validaPedirVuelto(mcontext, timeout, transUI, getCardNo(), Amount);
                if (rta > 0) {// Monto ingresado valido
                    otherAmount = rta;
                    para.setAmount(Amount);
                    para.setOtherAmount(otherAmount);
                    para.setNeedOtherAmount(DefinesBANCARD.DATO_NO_REQUERIDO);// YA FUE SOLICITADO
                } else if (rta == -1) {
                    return false;
                } else {// Usuario no desea vuelto
                    para.setNeedOtherAmount(DefinesBANCARD.DATO_NO_HABILITADO); // DES HABILITAR VUELTO
                }
            }
        } else {// Otras transacciones NO TIENEN VUELTO
            para.setNeedOtherAmount(DefinesBANCARD.DATO_NO_HABILITADO); // DES HABILITAR VUELTO
        }
        return true; // No aplica solicitar vuelto, vuelto ingresado exitosamente
    }

    /**
     * EMV读数据
     *
     * @param ifOfflineDataAuth
     * @return
     */
    private int emvReadData(boolean ifOfflineDataAuth) {
        Logger.debug("EmvTransaction>>emvReadData>>start");
        if (para.isECTrans()) {
            emvHandler.pbocECenable(true);
            Logger.debug("set pboc EC enable true");
        } else {
            emvHandler.pbocECenable(false);
            Logger.debug("set pboc EC enable false");
        }
        emvHandler.setEMVInitCallback(emvInitListener);
        emvHandler.setApduExchangeCallback(apduExchangeListener);
        emvHandler.setDataElement(new byte[]{(byte) 0x9c}, new byte[]{0x00});

        Logger.debug("EmvTransaction>>emvReadData>>应用选择");
        int ret = emvHandler.selectApp(Integer.parseInt(traceNo));//JM
        //int ret = emvHandler.selectApp(Integer.parseInt(ISOUtil.padleft(2 + "", 6, '0')));
        Logger.debug("EmvTransaction>>emvReadData>>selectApp = " + ret);
        if (ret != 0) {

            if (ret == 2063)
                return Tcode.T_blocked_aplication;

            else if (ret == 2064) {
                return Tcode.T_err_fallback;
            } else if (ret == 2062) {
                return Tcode.T_CardBlock;
            } else {
                Logger.debug("EmvTransaction>>emvReadData>>selectApp fail");
                return Tcode.T_select_app_err;
            }
        }

        if (para.isECTrans()) {
            byte[] firstBal = emvHandler.pbocReadECBalance();
            //9F79 , DF71 , DF79 , DF71
            if (firstBal != null) {
                ECAmount = ISOUtil.byte2hex(firstBal);
            }
        }

        Logger.debug("EmvTransaction>>emvReadData>>读应用数据");
        try {
            ret = emvHandler.readAppData();
        } catch (SDKException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        Logger.debug("EmvTransaction>>emvReadData>>readAppData = " + ret);
        if (ret != 0) {
            Logger.debug("EmvTransaction>>emvReadData>>readAppData fail");
            switch (retExpApp) {
                case Tcode.T_err_exp_date_app:
                    ret = Tcode.T_err_exp_date_app;
                    break;
                case Tcode.T_user_cancel_operation:
                    ret = Tcode.T_user_cancel_operation;
                    break;
                default:
                    ret = Tcode.T_read_app_data_err;
                    break;
            }
            return ret;
        }

        Logger.debug("EmvTransaction>>emvReadData>>脱机数据认证");
        if (ifOfflineDataAuth) {
            try {
                ret = emvHandler.offlineDataAuthentication();
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
            Logger.debug("EmvTransaction>>emvReadData>>offlineDataAuthentication=" + ret);
            if (ret != 0) {
                Logger.debug("EmvTransaction>>emvReadData>>offlineDataAuthentication fail");
                return Tcode.T_offline_dataauth_err;
            }
        }
        Logger.debug("EmvTransaction>>emvReadData>>finish");
        return 0;
    }

    /**
     * EMV联机后处理，二次授权
     *
     * @param rspCode
     * @param authCode
     * @param rspICCData
     * @param onlineResult
     * @return
     */
    public int afterOnline(String rspCode, String authCode, byte[] rspICCData, int onlineResult) {
        Logger.debug("enter afterOnline");
        Logger.debug("rspCode = " + rspCode);
        Logger.debug("authCode = " + authCode);
        if (rspICCData != null) {
            Logger.debug("rspICCData = " + ISOUtil.byte2hex(rspICCData));
        }
        this.rspCode = rspCode;
        this.authCode = authCode;
        this.rspICCData = rspICCData;
        this.onlineResult = onlineResult;

        boolean onlineTransaction = false;
        try {
            onlineTransaction = emvHandler.onlineTransaction();
        } catch (SDKException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        Logger.debug("onlineTransaction =" + onlineTransaction);
        if (onlineTransaction) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * 获取当前交易卡号
     *
     * @return
     */
    public String getCardNo() {
        byte[] temp = new byte[256];
        int len = PAYUtils.get_tlv_data_kernal(0x5A, temp);
        return ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len));
    }

    /**
     * 获取当前交易卡号
     *
     * @return
     */
    public String getTrack2() {
        byte[] temp = new byte[256];
        int len = PAYUtils.get_tlv_data_kernal(0x57, temp);
        return ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len));
    }

    /**
     * 获取当前交易密码加密
     *
     * @return
     */
    public String getPinBlock() {
        return pinBlock;
    }

    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }

    public String getKsnEmv() {
        return ksnEmv;
    }

    /**
     * 获取电子现金余额
     *
     * @return
     */
    public String getECAmount() {
        return ECAmount;
    }

    public long getTips() {
        return tips;
    }

    public String getTypeCoin() {
        return typeCoin;
    }

    public int getNumCuotas() {
        return numCuotas;
    }

    /**
     * 初始化Kernel
     *
     * @return
     */
    public boolean initEmvKernel() {
        emvHandler.initDataElement();
        emvHandler.setKernelType(EMVHandler.KERNEL_TYPE_PBOC);

        // 配置MCK,支持项默认为支持，不支持的请设置为-1
        TerminalMckConfigure configure = new TerminalMckConfigure();
        configure.setTerminalType(0x22);
        configure.setTerminalCapabilities(new byte[]{(byte) 0xE0, (byte) 0xF8, (byte) 0xC8});
        configure.setAdditionalTerminalCapabilities(new byte[]{0x60, 0x00, (byte) 0xF0, (byte) 0xA0, 0x01});
        configure.setSupportCardInitiatedVoiceReferrals(false);
        configure.setSupportForcedAcceptanceCapability(false);
        if (para.isNeedOnline()) {
            configure.setSupportForcedOnlineCapability(false); // mtip 51 test 01 scenario 01
            Logger.debug("setSupportForcedOnlineCapability true");
        } else {
            configure.setSupportForcedOnlineCapability(false);
            Logger.debug("setSupportForcedOnlineCapability false");
        }
        configure.setPosEntryMode(0x05);

        int ret = emvHandler.setMckConfigure(configure);
        if (ret != 0) {
            Logger.debug("setMckConfigure failed");
            return false;
        }
        CoreParam coreParam = new CoreParam();
        //coreParam.setTerminalId("POS00001".getBytes());
        if (tablaDevice.getNumeroCajas() != null)
            coreParam.setTerminalId(ISOUtil.padright("" + tablaDevice.getNumeroCajas(), 8, ' ').getBytes());
        else
            coreParam.setTerminalId(TMConfig.getInstance().getTermID().getBytes());

        if (tablaComercios.sucursal.getCardAccpMerch() != null)
            coreParam.setMerchantId(ISOUtil.padright("" + tablaComercios.sucursal.getCardAccpMerch(), 15, ' ').getBytes());
        else
            coreParam.setMerchantId(TMConfig.getInstance().getMerchID().getBytes());
        coreParam.setMerchantCateCode(new byte[]{0x00, 0x01});
        //coreParam.setMerchantNameLocLen(35);
        coreParam.setMerchantNameLocLen(17);
        //coreParam.setMerchantNameLoc("Band Card Test Center,Beijing,China".getBytes());
        coreParam.setMerchantNameLoc("Bancard, Paraguay".getBytes());
        coreParam.setTerminalCountryCode(new byte[]{0x06, 0x00});

        //coreParam.setTransactionCurrencyCode(new byte[]{0x01, 0x56});
        //coreParam.setReferCurrencyCode(new byte[]{0x01, 0x56});

        coreParam.setTransactionCurrencyCode(new byte[]{0x06, 0x00});
        coreParam.setReferCurrencyCode(new byte[]{0x06, 0x00});

        coreParam.setTransactionCurrencyExponent(0x02);
        coreParam.setReferCurrencyExponent(0x02);
        coreParam.setReferCurrencyCoefficient(1000);
        coreParam.setTransactionType(EMVHandler.EMVTransType.EMV_GOODS);

        ret = emvHandler.setCoreInitParameter(coreParam);
        if (ret != 0) {
            Logger.debug("setCoreInitParameter error");
            return false;
        }
        return true;
    }

    /**
     * 与卡片进行APDU交互
     *
     * @param apdu
     * @return
     */
    private byte[] exeAPDU(byte[] apdu) {
        byte[] rawData = null;
        int recvlen = 0;
        try {
            rawData = icCard.transmit(contactCard, apdu);
            if (rawData != null) {
                Logger.debug("rawData = " + ISOUtil.hexString(rawData));
                recvlen = rawData.length;
            }
        } catch (SDKException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        byte[] recv = new byte[recvlen];
        if (rawData != null) {
            System.arraycopy(rawData, 0, recv, 0, recvlen);
        } else {
            recv = null;
        }
        return recv;
    }

    /**
     * 从apdu中格式化金额
     *
     * @param hex
     * @return
     */
    private String fromApdu2Amount(String hex) {
        int len = hex.length();
        if (len > 2 && ((hex.contains("9F79") && hex.contains("9000")) ||
                (hex.contains("9F78") && hex.contains("9000"))) ||
                (hex.contains("9F5D") && hex.contains("9000"))) {
            int offset = 4;
            int l = Integer.parseInt(hex.substring(offset, offset + 2));
            return hex.substring(offset + 2, offset + 2 + l * 2);
        }
        return null;
    }

    /**
     * Application Expiration Date
     * Tag emv 5F24
     * JM
     *
     * @return
     */
    private boolean isAppExpDate(byte[] data) {
        String hex = "";
        int len = data.length;

        try {
            hex = ISOUtil.byte2hex(data, 0, len);
        } catch (NumberFormatException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return false;
        }
        try {
            if (len > 2 && (hex.contains("5F24"))) {

                int offset, yearCard, monCard, yearLocal, monLocal;
                String dateCard, dateLocal;

                offset = hex.indexOf("5F24");
                offset += 4;

                int l = Integer.parseInt(hex.substring(offset, offset + 2));
                dateCard = hex.substring(offset + 2, offset + 2 + l * 2);

                dateLocal = PAYUtils.getExpDate();
                monLocal = Integer.parseInt(dateLocal.substring(2));
                yearLocal = Integer.parseInt(dateLocal.substring(0, 2));
                yearCard = Integer.parseInt(dateCard.substring(0, 2));
                monCard = Integer.parseInt(dateCard.substring(2, 4));

                if (yearCard > yearLocal) {
                    return false;
                } else if (yearCard == yearLocal) {
                    if (monCard > monLocal) {
                        return false;
                    } else if (monCard == monLocal) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        } catch (NumberFormatException ex) {
            Logger.logLine(LogType.EXCEPTION, clase, ex.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, ex.getStackTrace());
            return false;
        }
        return false;
    }

    /**
     * 获取当前交易卡号
     *
     * @return
     */
    public String getCardAID() {
        byte[] temp = new byte[256];
        int len = PAYUtils.get_tlv_data_kernal(0x9F06, temp);
        return ISOUtil.bcd2str(temp, 0, len);
    }

    public long getAmountBase0() {
        return AmountBase0;
    }

    public long getAmountXX() {
        return AmountXX;
    }

    public long getServiceAmount() {
        return ServiceAmount;
    }

    public long getTipAmount() {
        return TipAmount;
    }

    public long getIvaAmount() {
        return IvaAmount;
    }


    public boolean isCajas() {
        return isCajas;
    }

    public void setCajas(boolean cajas) {
        isCajas = cajas;
    }


}
