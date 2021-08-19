package com.newpos.libpay.trans.finace;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.desert.keyboard.InputInfo;
import com.android.newpos.libemv.EMVISRCode;
import com.android.newpos.libemv.PBOCCardInfo;
import com.android.newpos.libemv.PBOCException;
import com.android.newpos.libemv.PBOCListener;
import com.android.newpos.libemv.PBOCOnlineResult;
import com.android.newpos.libemv.PBOCPin;
import com.android.newpos.libemv.PBOCPinRet;
import com.android.newpos.libemv.PBOCPinType;
import com.android.newpos.libemv.PBOCTag9c;
import com.android.newpos.libemv.PBOCTransFlow;
import com.android.newpos.libemv.PBOCTransProperty;
import com.android.newpos.libemv.PBOCUtil;
import com.android.newpos.libemv.PBOCode;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.adaptadores.ModeloMensajeConfirmacion;
import com.cobranzas.cajas.ProcesamientoCajas;
import com.cobranzas.cajas.waitConfirmacionCaja;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;
import com.cobranzas.transactions.DataAdicional.Campo87;
import com.cobranzas.transactions.DataAdicional.DataAdicional;
import com.cobranzas.transactions.common.CommonFunctionalities;
import com.newpos.bypay.EmvL2CVM;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.card.CardManager;
import com.newpos.libpay.device.contactless.EmvL2Process;
import com.newpos.libpay.device.contactless.EmvPBOCUpi;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.process.EmvTransaction;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.manager.RevesalTrans;
import com.newpos.libpay.trans.manager.ScriptTrans;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKException;
import com.pos.device.beeper.Beeper;
import com.pos.device.emv.EMVHandler;
import com.pos.device.printer.Printer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.MasterControl;

import static cn.desert.newpos.payui.master.MasterControl.incardTable;
import static cn.desert.newpos.payui.master.MasterControl.mcontext;
import static com.cobranzas.actividades.MainActivity.modoCaja;
import static com.cobranzas.actividades.StartAppBANCARD.readWriteFileMDM;
import static com.cobranzas.actividades.StartAppBANCARD.tablaCards;
import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.cobranzas.defines_bancard.DefinesBANCARD.ITEM_SIN_TARJETA_CUENTA_ST;
import static com.cobranzas.defines_bancard.DefinesBANCARD.POLARIS_APP_NAME;
import static com.cobranzas.menus.menus.FALLBACK;
import static com.cobranzas.menus.menus.TOTAL_BATCH;
import static com.cobranzas.menus.menus.contFallback;
import static com.cobranzas.transactions.common.CommonFunctionalities.confirmTransaction;
import static com.cobranzas.transactions.common.CommonFunctionalities.ctlPIN;
import static com.cobranzas.transactions.common.CommonFunctionalities.getKsn;
import static com.cobranzas.transactions.common.CommonFunctionalities.getPin;
import static com.newpos.libpay.trans.Trans.Type.SETTLE;


/**
 * 金融交易类
 *
 * @author zhouqiang
 */
public class FinanceTrans extends Trans {

    /**
     * 外界输入类型
     */
    public static final int INMODE_HAND = 0x01;
    public static final int INMODE_MAG = 0x02;
    public static final int INMODE_IC = 0x08;
    public static final int INMODE_NFC = 0x10;
    /**
     * 联机交易还是脱机交易
     */
    public static final int AAC_ARQC = 1;
    public static final int AAC_TC = 0;
    public static final String LOCAL = "1";
    public static final String DOLAR = "2";
    public static final String EURO = "3";
    public static boolean isNoValidarCajas = false;
    public static waitConfirmacionCaja confirmacionCaja;
    static String montoInicial;
    /**
     * var multi-acq
     */
    protected final int NOMBRE_COMERCIO = 0;
    protected final int MID = 1;
    public String transEname;
    /**
     * 卡片模式
     */
    protected int inputMode = 0x02;// 刷卡模式 1 手输卡号；2刷卡；5 3插IC；7 4非接触卡
    /**
     * 是否有密码
     */
    protected boolean isPinExist = false;
    protected boolean isCajas;
    /**
     * 是否式IC卡
     */
    protected boolean isICC = false;
    /**
     * 标记此次交易是否需要冲正
     */
    protected boolean isReversal;
    /**
     * 标记此次交易是否需要存记录
     */
    protected boolean isSaveLog;
    /**
     * 是否借记卡交易
     */
    protected boolean isDebit;
    /**
     * 标记此交易联机前是否进行冲正上送
     */
    protected boolean isProcPreTrans;
    /**
     * 后置交易
     */
    protected boolean isProcSuffix;
    /**
     * whether need GAC2
     */
    protected boolean isNeedGAC2;
    protected String typeCoin;
    protected String host_id;
    protected int numCuotas;
    protected DataAdicional dataAdicional;
    protected boolean Unionpay = false;
    String clase = "FinanceTrans.java";
    /**
     * PBOC listener
     */
    private final PBOCListener listener = new PBOCListener() {
        @Override
        public int dispMsg(int i, String s, int i1) {
            return 0;
        }

        @Override
        public int callbackSelApp(String[] strings) {
            return transUI.showCardApplist(timeout, strings);
        }

        @Override
        public int callbackCardNo(String s) {
            Pan = s;
            return transUI.showCardConfirm(timeout, s);
        }

        @Override
        public PBOCPin callbackEnterPIN(PBOCPinType pbocPinType) {
            PBOCPin pin = new PBOCPin();
            if (handleNFCPin()) {
                pin.setPinBlock(ISOUtil.str2bcd(PIN, false));
                pin.setErrno(0);
            } else {
                pin.setPbocPinRet(PBOCPinRet.FAIL);
            }

            return pin;
        }

        @Override
        public int callbackVerifyCert(int i, String s) {
            return 0;
        }

        @Override
        public void pbocBeforeGPO() {
            return;
            //transInterface.beforeGPO();
        }
    };
    int timerCancelacion = 10;
    Date fecIni;

    /**
     * 金融交易类构造
     *
     * @param ctx
     * @param transEname
     */
    public FinanceTrans(Context ctx, String transEname) {
        super(ctx, transEname);
        dataAdicional = new DataAdicional(MsgID);
        dataAdicional.commonConfig(ctx);
        this.transEname = transEname;
        iso8583.setHasMac(false);
        setTraceNoInc(true);
    }

    public FinanceTrans(Context ctx, String transEname, String fileNameLog) {
        super(ctx, transEname, fileNameLog);
        dataAdicional = new DataAdicional(MsgID);
        dataAdicional.commonConfig(ctx);
        this.transEname = transEname;
        iso8583.setHasMac(false);
        setTraceNoInc(true);
    }

    /**
     * 格式化处理响应码
     *
     * @param rsp
     * @return
     */
    public static int formatRsp(String rsp) {

        String[] stand_rsp = {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60",
                "61", "62", "63", "64", "65", "66", "67", "68", "69", "70",
                "71", "72", "73", "74", "75", "76", "77", "78", "79", "80",
                "81", "82", "83", "84", "85", "86", "87", "88", "89", "90",
                "91", "92", "93", "94", "95", "96", "97", "98", "99", "A1",
                "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AN", "B0",
                "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "C0",
                "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "D0",
                "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DP",
                "E1", "K0", "K1", "K2", "K3", "N0", "N3", "N4", "N7", "P0",
                "P1", "P2", "Q1", "U4", "UB", "XA", "XD", "Z3", "YY", "ZZ",
        };

        int START = 3000;
        boolean finded = false;
        for (int i = 0; i < stand_rsp.length; i++) {
            if (stand_rsp[i].equals(rsp)) {
                START += i;
                finded = true;
                break;
            }
        }
        if (finded) {
            return START;
        } else {
            //return Integer.parseInt(rsp);
            return 4000;
        }
    }

    public static boolean validarMontos(String monto) {
        Logger.logLine(LogType.FLUJO, "FinanceTrans.java " + " Metodo : validarMontos", "Ingreso al Megtodo Validar Montos ");
        long monInicial = Integer.parseInt(montoInicial);
        int monFinal = Integer.parseInt(monto);
        if (isNoValidarCajas) {
            isNoValidarCajas = false;
            return true;
        }
        if (monInicial >= monFinal) {
            return true;
        }
        return false;
    }

    public static void setConfirmacionCaja(waitConfirmacionCaja confirmacionCaja) {
        FinanceTrans.confirmacionCaja = confirmacionCaja;
    }

    /**
     * 联机前某些特殊值的处理
     *
     * @param inputMode
     */
    protected void setDatas(int inputMode) {

        Logger.debug("==FinanceTrans->setDatas==");
        this.inputMode = inputMode;

        if (isPinExist) {
            CaptureCode = "12";
        }

        if (inputMode == ENTRY_MODE_MAG) {
            if (isFallBack) {
                EntryMode = MODE1_FALLBACK + CapPinPOS();
            } else {
                EntryMode = MODE_MAG + CapPinPOS();
            }
        } else if (inputMode == ENTRY_MODE_ICC) {
            EntryMode = MODE_ICC + CapPinPOS();
        } else if (inputMode == ENTRY_MODE_NFC) {
            EntryMode = MODE_CTL + CapPinPOS();
        } else if (inputMode == ENTRY_MODE_HAND) {
            EntryMode = MODE_HANDLE + CapPinPOS();
        } else {
            EntryMode = "000";
        }

        if (isPinExist || Track2 != null || Track3 != null) {
            if (isPinExist) {
                SecurityInfo = "2";
            } else {
                SecurityInfo = "0";
            }
            if (cfg.isSingleKey()) {
                SecurityInfo += "0";
            } else {
                SecurityInfo += "6";
            }
            if (cfg.isTrackEncrypt()) {
                SecurityInfo += "10000000000000";
            } else {
                SecurityInfo += "00000000000000";
            }
        }
        //appendField60("048");
    }

    public String CapPinPOS() {
        String capPINPos = "2";

        if (PIN != null) {
            capPINPos = "1";
        }

        /**
         * COMENTADO POR FABIAN ARDILA
         * No carga el getNameIssuer en proceso manual (Hand)
         * Se debe rivasar esta certificacion con Morantes.
         */
        //Configuramos el ultimo digito del entry point para Union Pay
        /*if (card.getNameIssuer().equals("UNIONPAY") && PIN == null)
        {
            capPINPos = "2";
            //EntryMode = EntryMode.substring(0, 2) + ((PIN != null) ? "1" : "2");
        }*/
        return capPINPos;
    }

    /**
     * 从内核获取
     * 卡号，
     * 有效期，
     * 2磁道，
     * 1磁道，
     * 卡序号
     * 55域数据
     */
    protected void setICCData() {
        Logger.debug("==FinanceTrans->setICCData==");
        byte[] temp = new byte[128];
        // 卡号
        int len = PAYUtils.get_tlv_data_kernal(0x5A, temp);
        Pan = ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len));
        // 有效期
        len = PAYUtils.get_tlv_data_kernal(0x5F24, temp);
        if (len == 3) {
            ExpDate = ISOUtil.byte2hex(temp, 0, len - 1);
        }
        // 2磁道
        len = PAYUtils.get_tlv_data_kernal(0x57, temp);
        Track2 = ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len));
        // 1磁道
        len = PAYUtils.get_tlv_data_kernal(0x9F1F, temp);
        Track1 = new String(temp, 0, len);
        // 卡序号
        len = PAYUtils.get_tlv_data_kernal(0x5F34, temp);
        if (len != 0)
            PanSeqNo = ISOUtil.padleft(ISOUtil.byte2hex(temp, 0, len) + "", 3, '0');
        //55域数据
        temp = new byte[512];
        if (OtherAmount == 0) {
            len = PAYUtils.pack_tags(PAYUtils.wOnlinesiAmount, temp);
        } else {
            len = PAYUtils.pack_tags(PAYUtils.wOnlineTags, temp);
        }

        if (len > 0) {
            ICCData = new byte[len];
            System.arraycopy(temp, 0, ICCData, 0, len);
        } else {
            ICCData = null;
        }
    }

    /**
     * set some IC card data
     */
    protected void setICCDataCTL() {
        Logger.debug("==FinanceTrans->setICCData==");
        PBOCCardInfo info = PBOCUtil.getPBOCCardInfo();
        Pan = info.getCardNO();
        ExpDate = info.getExpDate();
        Track2 = info.getCardTrack2();
        Track1 = info.getCardTrack1();
        Track3 = info.getCardTrack3();
        PanSeqNo = info.getCardSeqNo();
        int[] wOnlineTags = new int[]{40742, 40743, 40720, 40759, 40758, 149, 154, 156, 40706, 24362, 130, 40730, 40707, 40755, 40756, 132, 0}; //79 //80
        ICCData = PBOCUtil.getF55Data(wOnlineTags);
    }

    public void setFieldTrans() {
        LocalTime = PAYUtils.getLocalTime();
        LocalDate = PAYUtils.getLocalDate();
        switch (transEname) {
            default:
                break;
        }
    }


    /**
     * sendRcvFinance :logica evacuacion de reverso + llamado a "sendRcvTrans"
     * Acciones secundarias realizadas :
     * - Actualiza hora POS desde servidor
     *
     * @return - Tcode.T_success
     * - Otros codigos : Error
     */
    protected int sendRcvFinance() {

        int rta = Tcode.T_success;
        RevesalTrans reversalTrans = new RevesalTrans(context, "REVERSAL", timeout);
        if (isProcPreTrans) {
            rta = reversalTrans.analizarReversoAntesDe(transUI);
            if (rta != Tcode.T_success) {
                return rta;
            }
        }

        if (rta == Tcode.T_success) {
            TransLogData reveralData = setReveralData();

            int rtnTrans = sendRcvTrans(isReversal, reveralData);

            Log.d(clase, "sendRcvFinance: >> rtnTrans " + rtnTrans);

            if (rtnTrans == Tcode.T_success) {

                RspCode = iso8583.getfield(39);
                // Actualizacion Hora desde servidor
                if (RspCode != null && RspCode.equals("00")) {
                    actualizarFechaDesdeHost();
                }
                return Tcode.T_success;
            } else if (rtnTrans == Tcode.T_receive_err) {

                if (transUI != null) {
                    transUI.toasTrans(rtnTrans, true, true);
                }


                int rspDespuesDe = reversalTrans.analizarReversoDespuesDe(transUI);
                if (rspDespuesDe != Tcode.T_success) { // Transaccion REVERSADA
                    return rspDespuesDe;
                }

            } else {
                if (transUI != null) {
                    switch (rtnTrans) {
                        case Tcode.T_package_illegal:
                        case Tcode.T_socket_err:
                            break;
                        default:
                            Log.d(clase, "sendRcvFinance: >> toasTrans >>" + rtnTrans);
                            transUI.toasTrans(rtnTrans, true, true);
                            break;

                    }

                }
            }
        }

        return Tcode.T_socket_err;
    }

    /**
     * actualizarFechaDesdeHost  : Actualiza Fecha y hora de la informacion obtenida en "iso8583" campos 12 y 13
     * Se asigna informacion a variables LocalDate y LocalTime de la clase Trans
     * campo 12  -> LocalTime
     * campo 13  -> LocalDate
     */
    private void actualizarFechaDesdeHost() {
        if (iso8583 != null) {
            String date = iso8583.getfield(13);
            String time = iso8583.getfield(12);
            if ((date != null) && (time != null)) {
                UIUtils.dateTime(date, time);
                LocalDate = date;
                LocalTime = time;
            }
        }
    }

    private byte[] get_ICCDataFromIso8583() {
        String strICC = null;
        if (iso8583.getfield(55) != null) {
            strICC = iso8583.getfield(55);
            strICC = ISOUtil.hex2AsciiStr(strICC);
            if (strICC != null && (!strICC.trim().equals(""))) {
                return ISOUtil.str2bcd(strICC, false);
            }
        }
        return null;
    }

    private int need2AC_ICC(EmvTransaction emvTrans, int retVal) {
        int rtn = Tcode.T_success;

        boolean need2AC = /*TransEName.equals(Type.CONSULTA_DEUDAS) ||*/ TransEName.equals(Type.QUICKPASS); //revisar con jhon

        if (emvTrans != null && retVal == 0 && need2AC) {
            byte[] tag9f27 = new byte[1];
            byte[] tag9b = new byte[2];
            rtn = emvTrans.afterOnline(RspCode, AuthCode, ICCData, retVal);
            int lenOf9f27 = PAYUtils.get_tlv_data_kernal(0x9F27, tag9f27);
            if (lenOf9f27 != 1) {
                // Procesamiento de falla de IC Si el campo 39 es 00, el archivo de actualización es correcto. 39 Campo 06
                TransLogData revesalData = TransLog.getReversal();
                if (revesalData != null) {
                    revesalData.setRspCode("06");
                    TransLog.saveReversal(revesalData);
                }
            }
            if (tag9f27[0] != 0x40) {
                // Aprobado en segundo plano, rechazado por la tarjeta, para mantener el golpe
                return Tcode.T_gen_2_ac_fail;
            }
            //Resultado del script del emisor
            int len9b = PAYUtils.get_tlv_data_kernal(0x9b, tag9b);
            if (len9b == 2 && (tag9b[0] & 0x04) != 0) {
                // Guarde los resultados del script de línea de tarjeta
                byte[] temp = new byte[256];
                int len = PAYUtils.pack_tags(PAYUtils.wISR_tags, temp);
                if (len > 0) {
                    ICCData = new byte[len];
                    System.arraycopy(temp, 0, ICCData, 0, len);
                } else {
                    ICCData = null;
                }
                TransLogData scriptResult = setScriptData();
                TransLog.saveScriptResult(scriptResult);
            }
        }
        return rtn;
    }

    private int need2AC(int inputMode, EmvTransaction emvTrans, int retVal) {
        int rtn = Tcode.T_success;
        switch (inputMode) {
            case ENTRY_MODE_ICC:
                rtn = need2AC_ICC(emvTrans, retVal);
                break;
            case ENTRY_MODE_NFC:
                if (isNeedGAC2) {
                    rtn = genAC2Trans();
                    if (rtn != PBOCode.PBOC_TRANS_SUCCESS) {
                        return rtn;
                    }
                }
                break;
        }
        return rtn;
    }


    protected int OnlineTrans(EmvTransaction emvTrans) {
        Logger.logLine(LogType.FLUJO, clase, "Ingreso a metodo OnlineTrans");

        setFieldTrans();
        retVal = sendRcvFinance();
        if (retVal != Tcode.T_success) {
            return retVal;
        }

        RspCode = iso8583.getfield(39);

        if (RspCode.trim().isEmpty()) {
            return Tcode.T_rspCode_no_llego;
        }

        ICCData = get_ICCDataFromIso8583();

        if (RspCode.equals("00")) {
            retVal = need2AC(inputMode, emvTrans, retVal);
            if (retVal != 0) {
                return retVal;
            }
        }

        String campo63;
        campo63 = iso8583.getfield(63);
        if (campo63 != null) {
            System.out.println("CAMPO 63 ---- " + campo63);
            dataAdicional.setSubCampos(campo63);
            Logger.logLine(LogType.FLUJO, clase, "Campo 63 = " + campo63);
        }

        if (!RspCode.equals("00")) {
            TransLog.clearReveral();
            int ret = formatRsp(RspCode);
            int rta;
            switch (RspCode) {
                case "60":
                    //Pedir PIN
                    RspCode = null;
                    PinInfo info = transUI.getPinpadOnlinePinDUKPT(timeout, String.valueOf(Amount), Pan);
                    if (info.isResultFlag()) {
                        if (info.isNoPin()) {
                            return -1;
                        } else {
                            PIN = ISOUtil.hexString(info.getPinblock());
                            Ksn = info.getKsnString();
                            isPinExist = true;
                            setDatas(inputMode);
                            return OnlineTrans(emvTrans);
                        }
                    } else {
                        return -1;
                    }
                case "56":
                    RspCode = null;

                    rta = procesarMultipleCuenta();
                    if (rta == Tcode.T_success) {
                        return OnlineTrans(emvTrans);
                    } else if (rta == -1) {
                        return Tcode.T_success;// Timeout / Cancelado por usuario / Error mostrado internamente
                    }

                case "57":
                    RspCode = null;
                    InputInfo inputInfo = transUI.showSeleccionTipoDeCuenta(timeout);
                    if (inputInfo.isResultFlag()) {
                        String result = inputInfo.getResult();
                        if (result.equals(DefinesBANCARD.TIPO_AHORROS) || result.equals(DefinesBANCARD.TIPO_CORRIENTE) || result.equals(DefinesBANCARD.TIPO_CREDITO)) {
                            return OnlineTrans(emvTrans);
                        }
                    } else {
                        return -1;
                    }
                    break;
                default:
                    MasterControl.field22 = DataAdicional.getField(22);
                    return ret;
            }
        }


        //脚本上送
        TransLogData data = TransLog.getScriptResult();
        if (data != null) {
            ScriptTrans script = new ScriptTrans(context, "SENDSCRIPT");
            int ret = script.sendScriptResult(data);
            if ((ret != Tcode.T_socket_err) && (ret != Tcode.T_send_err) && (ret != Tcode.T_receive_err)) {
                TransLog.clearScriptResult();
                Logger.error("Se elimina el log del clearScriptResult");
            }
        }


        TransLogData logData;

        if (readWriteFileMDM.getSettle().equals(readWriteFileMDM.SETTLE_DEACTIVE)) {
            try {
                readWriteFileMDM.writeFileMDM(readWriteFileMDM.getReverse(), readWriteFileMDM.SETTLE_ACTIVE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isSaveLog) {
            logData = setLogData(isSaveLog);
            transLog.saveLog(logData, host_id);
        } else {
            logData = setLogData(isSaveLog);
        }

        Logger.logLine(LogType.FLUJO, clase, "Se elimina el log del reverso");
        TransLog.clearReveral();

        transUI.showResult(timeout, true, false, true, DataAdicional.getField(22));
        transUI.showFinish();

        return retVal;
    }

    /**
     * Pago de servicio
     *
     * @return Tcode
     */

    /*
   cambiarProcCode : Retorna codigo de proceso asignando la cadena "cod" desde la posicion "ini"
   Parametros :
       - cod : Cadena a ser asignada
       - ini : posicion de inicio para ser asignado a String de retorno
    */
    private String cambiarProcCode(String cod, int ini) {
        StringBuilder builder = new StringBuilder(ProcCode);
        int fin;

        if (cod != null) {
            fin = ini + cod.length();
            builder.replace(ini, fin, cod);
        }

        return builder.toString();
    }

    private ArrayList<ModeloBotones> obtenerCuentasCampo30() {
        ArrayList<ModeloBotones> cuentas = new ArrayList<>();
        String data = DataAdicional.getField(30);
        if (data != null) {
            int lenCampo = data.length();
            int id = 1;
            try {
                for (int i = 0; i < lenCampo; i++) {
                    ModeloBotones modelo = new ModeloBotones();
                    String cuenta = data.substring(i, i + 20);
                    System.out.println(cuenta);
                    modelo.setNombreBoton(data.substring(i, i + 20));
                    modelo.setCodBoton(String.valueOf(id));
                    cuentas.add(modelo);
                    id++;
                    i = i + 19;
                }
            } catch (Exception e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Log.d("ERROR MULTICUENTA -", e.getMessage());
            }

            return cuentas;
        } else {
            return null;
        }
    }

    private String procesarCampo87(ArrayList<Campo87> campo87ArrayList) {
        StringBuilder builder = new StringBuilder();
        for (Campo87 campo87 : campo87ArrayList) {
            builder.append(campo87.getId());
            builder.append(campo87.getLen());
            builder.append(campo87.getMensaje());
        }
        if (builder.toString().equals("")) {
            return null;
        } else {
            Logger.logLine(LogType.FLUJO, clase, "Campo 87 -  = " + builder.toString());
            return builder.toString();
        }
    }

    private String getNameCardSwhipe(String trak1) {
        String nameCard = null;
        try {
            String[] parts = trak1.split("\\^");
            nameCard = parts[1];
            return nameCard.trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        return null;
    }

    private String getNameCard() {
        byte[] temp = new byte[128];
        int len = PAYUtils.get_tlv_data_kernal(0x5F20, temp);
        String nameCard = new String(temp, 0, len);
        return nameCard.trim();
    }

    @NonNull
    private String getLabelCard() {
        byte[] temp = new byte[128];
        int len = PAYUtils.get_tlv_data_kernal(0x50, temp);
        String aux = null;
        try {
            aux = new String(temp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        return aux.trim().substring(0, len);
    }

    private String getARQC() {
        byte[] temp = new byte[128];
        int len = PAYUtils.get_tlv_data_kernal(0x9F26, temp);
        String aux = ISOUtil.bcd2str(temp, 0, len);
        return aux.trim();
    }

    private String getAID() {
        byte[] temp = new byte[128];
        int len = PAYUtils.get_tlv_data_kernal(0x9F06, temp);
        String aux = ISOUtil.bcd2str(temp, 0, len);
        return aux.trim();
    }

    private String getTC() {
        byte[] temp = new byte[128];
        PAYUtils.get_tlv_data_kernal(0x9F26, temp);
        String aux = ISOUtil.bcd2str(temp, 0, 2);
        return aux.trim();
    }

    private String getTVR() {
        byte[] temp = new byte[128];
        int len = PAYUtils.get_tlv_data_kernal(0x95, temp);
        String aux = ISOUtil.bcd2str(temp, 0, len);
        return aux.trim();
    }

    private String getTSI() {
        byte[] temp = new byte[128];
        int len = PAYUtils.get_tlv_data_kernal(0x9B, temp);
        String aux = ISOUtil.bcd2str(temp, 0, len);
        return aux.trim();
    }

    private String getPreferenceLabelCard() {
        byte[] temp = new byte[128];
        int len = PAYUtils.get_tlv_data_kernal(0x9F11, temp);
        String aux = null;

        try {
            aux = new String(temp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.error("Exception" + e.toString());
            Thread.currentThread().interrupt();
        }

        if (temp[0] == 1) {
            len = PAYUtils.get_tlv_data_kernal(0x9F12, temp);
            aux = null;

            try {
                aux = new String(temp, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Logger.error("Exception" + e.toString());
                Thread.currentThread().interrupt();
            }
        } else {
            len = PAYUtils.get_tlv_data_kernal(0x50, temp);
            aux = null;

            try {
                aux = new String(temp, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Logger.error("Exception" + e.toString());
                Thread.currentThread().interrupt();
            }
        }
        if (aux != null) {
            return aux.trim().substring(0, len);
        }
        return null;
    }

    public TransLogData setLogData(boolean isSaveLog) {

        TransLogData LogData = new TransLogData();

        if (MsgID != null) {
            LogData.setMsgID(MsgID);
        }

        if (Field02 != null) {
            LogData.setField02(Field02);
        }

        if (ProcCode != null) {
            LogData.setProcCode(ProcCode);
        }

        if (typeCoin != null) {
            LogData.setTypeCoin(typeCoin);
        }

        if (ExpDate != null) {
            LogData.setExpDate(ExpDate);
        }

        LogData.setOprNo(cfg.getOprNo());

        if (BatchNo != null) {
            LogData.setBatchNo(BatchNo);
        }
        if (TransEName != null) {
            LogData.setEName(TransEName);
            if (TypeTransVoid != null)
                LogData.setTypeTransVoid(TypeTransVoid);

            if (TipoVenta != null)
                LogData.setTipoVenta(TipoVenta);
        }

        LogData.setAac(FinanceTrans.AAC_ARQC);

        if (CashOverAmount != 0) {
            LogData.setAmmountCashOver(CashOverAmount);
        }

        if (OtherAmount != 0) {
            LogData.setOtherAmount(OtherAmount);
        }

        if (Amount != 0) {
            LogData.setAmount(Amount);
        }

        if (montoFijo != 0) {
            LogData.setMontoFijo(montoFijo);
        }

        if (tipoMontoFijo != null) {
            LogData.setTipoMontoFijo(tipoMontoFijo);
        }

        if (iso8583.getfield(11) != null) {
            LogData.setTraceNo(iso8583.getfield(11));
        } else if (TraceNo != null) {
            LogData.setTraceNo(TraceNo);
        }

        if (LocalTime != null) {
            LogData.setLocalTime(LocalTime);
        }

        LogData.setLocalDate(PAYUtils.getYear() + LocalDate);

        LogData.setDatePrint(PAYUtils.getMonth() + " " + PAYUtils.getDay() + "," + PAYUtils.getYear());

        if (numCuotas > 0) {
            LogData.setNumCuotas(numCuotas);
        }
        if (iso8583.getfield(14) != null) {
            LogData.setExpDate(iso8583.getfield(14));
        }
        if (iso8583.getfield(15) != null) {
            LogData.setSettleDate(iso8583.getfield(15));
        }
        if (EntryMode != null) {
            LogData.setEntryMode(EntryMode);
        }
        if (iso8583.getfield(23) != null) {
            LogData.setPanSeqNo(iso8583.getfield(23));
        }
        if (Nii != null) {
            LogData.setNii(Nii);
        }
        if (SvrCode != null) {
            LogData.setSvrCode(SvrCode);
        }
        if (iso8583.getfield(32) != null) {
            LogData.setAcquirerID(iso8583.getfield(32));
        }
        if (Track2 != null) {
            LogData.setTrack2(Track2);
        }

        if (NroBoleta != null) {
            LogData.setRrn(NroBoleta);
        }

        if (iso8583.getfield(37) != null) {
            LogData.setRrn(iso8583.getfield(37));
        }

        switch (transEname) {
            case Type.ANULACION:

                if (AuthCode != null) {
                    LogData.setAuthCode(AuthCode);
                }
                break;
            default:
                if (iso8583.getfield(38) != null) {
                    LogData.setAuthCode(iso8583.getfield(38));
                }
                break;
        }

        if (iso8583.getfield(39) != null) {
            LogData.setRspCode(iso8583.getfield(39));
        }
        if (TermID != null) {
            LogData.setTermID(TermID);
        }
        if (MerchID != null) {
            LogData.setMerchID(MerchID);
        }

        //Codigo del negocio
        if (iso8583.getfield(42) != null) {
            LogData.setCodigoDelNegocio(iso8583.getfield(42));
        }

        if (Field48 != null) {
            LogData.setField48(Field48);
        }

        if (iso8583.getfield(49) != null) {
            LogData.setCurrencyCode(iso8583.getfield(49));
        }

        if (CurrencyCode != null) {
            LogData.setCurrencyCode(CurrencyCode);
        }
        if (PIN != null) {
            LogData.setPin(PIN);
        }
        if (Field62 != null) {
            LogData.setField62(Field62);
        }
        if (Field63 != null) {
            LogData.setField63(Field63);
        }
        if (Field61 != null) {
            LogData.setField61(Field61);
        }

        String campo61 = iso8583.getfield(61);
        if (campo61 != null) {
            LogData.setField61(campo61);
        } else {
            LogData.setField61(null);
        }

        if (ICCData != null) {
            LogData.setIccdata(ICCData);
            LogData.setField55(ISOUtil.byte2hex(ICCData));
        }

        if (inputMode == ENTRY_MODE_NFC) {
            LogData.setNFC(true);
        }
        if (inputMode == ENTRY_MODE_ICC) {
            LogData.setICC(true);
        }

        if (isFallBack)
            LogData.setFallback(isFallBack);

        if (numCuotasDeferred != null) {
            LogData.setNumCuotasDeferred(numCuotasDeferred);
        }

        if (TransEName.equals(Type.ANULACION)) {
            if (issuerName != null) {
                LogData.setIssuerName(issuerName);
            }

            if (labelName != null) {
                LogData.setLabelCard(labelName);
            }

        }

        if (EntryMode != null) {

            if (EntryMode.equals(MODE_ICC + CapPinPOS())) {
                LogData.setNameCard(getNameCard());
                LogData.setAid(getAID());
                LogData.setArqc(getARQC());
                LogData.setTc(getTC());
                LogData.setTvr(getTVR());
                LogData.setTsi(getTSI());
                LogData.setTypeAccount(getLabelCard());
            } else if (EntryMode.equals(MODE_MAG + CapPinPOS()) || EntryMode.equals(MODE1_FALLBACK + CapPinPOS()) || EntryMode.equals(MODE2_FALLBACK + CapPinPOS())) {
                if (Track1 != null)
                    LogData.setNameCard(getNameCardSwhipe(Track1));
            } else if (EntryMode.equals(MODE_CTL + CapPinPOS())) {
                if (emvPBOCUpi instanceof EmvPBOCUpi) {

                    if (!MasterControl.HOLDER_NAME.equals("---"))
                        LogData.setNameCard(MasterControl.HOLDER_NAME);

                    if (emvPBOCUpi.GetLable() != null) {
                        LogData.setAidname(emvPBOCUpi.GetLable());
                    }
                    if (emvPBOCUpi.GetAid() != null) {
                        LogData.setAid(emvPBOCUpi.GetAid());
                    }
                    if (emvPBOCUpi.GetTVR() != null) {
                        LogData.setTvr(emvPBOCUpi.GetTVR());
                    }
                    if (emvPBOCUpi.GetTSI() != null) {
                        LogData.setTsi(emvPBOCUpi.GetTSI());
                    }

                    if (emvPBOCUpi.GetARQC() != null) {
                        LogData.setArqc(emvPBOCUpi.GetARQC());
                    }

                } else {
                    if (!MasterControl.HOLDER_NAME.equals("---"))
                        LogData.setNameCard(MasterControl.HOLDER_NAME);
                    if (emvl2.GetLable() != null) {
                        LogData.setAidname(emvl2.GetLable());
                    }
                    if (emvl2.GetAid() != null) {
                        LogData.setAid(emvl2.GetAid());
                    }
                    if (emvl2.GetTVR() != null) {
                        LogData.setTvr(emvl2.GetTVR());
                    }
                    if (emvl2.GetTSI() != null) {
                        LogData.setTsi(emvl2.GetTSI());
                    }

                    if (emvl2.GetARQC() != null) {
                        LogData.setArqc(emvl2.GetARQC());
                    }
                }


            }
        }

        if (tablaComercios.sucursal.getDescripcion() != null) {
            LogData.setNombreComercio(tablaComercios.sucursal.getDescripcion());
        }

        if (tablaComercios.sucursal.getDireccionPrincipal() != null) {
            LogData.setAddressTrade(tablaComercios.sucursal.getDireccionPrincipal());
            LogData.setPhoneTrade(tablaComercios.sucursal.getTelefono());
        }

        if (MerchantType != null) {
            LogData.setMerchantType(MerchantType);
        }

        LogData.setDebit(isDebit);
        LogData.setPinExist(isPinExist);


        LogData.setNroCargo(NroCargo);

        if (Pan != null) {
            LogData.setPan(Pan);
        }

        if (iso8583.getfield(54) != null) {
            LogData.setAdditionalAmount(ISOUtil.hex2AsciiStr(iso8583.getfield(54)));
        }

        if (DataAdicional.getField(89) != null) {
            switch (TipoVenta) {
                case ITEM_SIN_TARJETA_CUENTA_ST:
                    LogData.setTipoTarjeta("T");
                    break;
                default:
                    if (TransEName.equals("CONSULTA")) {
                        LogData.setTipoTarjeta("CS");
                    } else {
                        String obtenerTipoTarjeta = obtenerTipoTarjeta();
                        LogData.setTipoTarjeta(obtenerTipoTarjeta);
                    }
                    break;

            }
            if (LogData.getTipoTarjeta() != null) {
                LogData.setTipoTarjeta(LogData.getTipoTarjeta());
            }


        } else {
            switch (TipoVenta) {
                case ITEM_SIN_TARJETA_CUENTA_ST:
                    LogData.setTipoTarjeta("T");
                    break;
                default:
                    LogData.setTipoTarjeta("X");
                    break;
            }

            if (LogData.getTipoTarjeta() != null) {
                LogData.setTipoTarjeta(LogData.getTipoTarjeta());
            }
        }
        return LogData;
    }

    private String obtenerTipoTarjeta() {
        switch (DataAdicional.getField(89)) {
            case "AC":
            case "BC":
            case "CB":
            case "CC":
            case "CP":
            case "MC":
            case "PC":
            case "VC":
            case "VS":
                return "C";
            case "ID":
            case "md":
            case "MD":
            case "UD":
            case "VD":
                return "D";
            default:
                return "C";
        }
    }

    /**
     * 保存扫码交易数据
     *
     * @param code 付款码
     * @return
     */
    protected TransLogData setScanData(String code) {
        TransLogData LogData = new TransLogData();
        LogData.setAmount(Amount);
        LogData.setOtherAmount(OtherAmount);
        LogData.setPan(code);
        LogData.setOprNo(cfg.getOprNo());
        LogData.setBatchNo(BatchNo);
        LogData.setEName(TransEName);
        LogData.setIccdata(ICCData);
        if (inputMode == ENTRY_MODE_NFC) {
            LogData.setNFC(true);
        }
        if (inputMode == ENTRY_MODE_ICC) {
            LogData.setICC(true);
        }
        LogData.setLocalDate(PAYUtils.getYMD());
        LogData.setTraceNo(TraceNo);
        LogData.setLocalTime(PAYUtils.getHMS());
        LogData.setSettleDate(PAYUtils.getYMD());
        LogData.setAcquirerID("12345678");
        LogData.setRrn("170907084952");
        LogData.setAuthCode("084952");
        LogData.setRspCode("00");
        LogData.setCurrencyCode("156");
        return LogData;
    }

    /**
     * 脱机打单
     *
     * @param ec_amount
     * @return
     */
    protected int offlineTrans(String ec_amount) {
        if (isSaveLog) {
            TransLogData LogData = new TransLogData();
            if (para.getTransType().equals(Type.EC_ENQUIRY)) {
                LogData.setAmount(Long.parseLong(ec_amount));
            } else {
                LogData.setAmount(Amount);
            }
            LogData.setOtherAmount(OtherAmount);
            LogData.setPan(PAYUtils.getSecurityNum(Pan, 6, 3));
            LogData.setOprNo(cfg.getOprNo());
            LogData.setEName(TransEName);
            LogData.setEntryMode(ISOUtil.padleft(inputMode + "", 2, '0') + "10");
            LogData.setTraceNo(cfg.getTraceNo());
            LogData.setBatchNo(cfg.getBatchNo());
            LogData.setLocalDate(PAYUtils.getYear() + PAYUtils.getLocalDate());
            LogData.setLocalTime(PAYUtils.getLocalTime());
            LogData.setAac(FinanceTrans.AAC_TC);
            LogData.setIccdata(ICCData);
            if (inputMode == ENTRY_MODE_NFC) {
                LogData.setNFC(true);
            }
            if (inputMode == ENTRY_MODE_ICC) {
                LogData.setICC(true);
            }
            transLog.saveLog(LogData);
            if (isTraceNoInc) {
                cfg.incTraceNo();
            }
        }
        if (para.isNeedPrint()) {
            transUI.handling(timeout, Tcode.Status.printing_recept);
            PrintManager print = PrintManager.getmInstance(context, transUI);
            do {
                retVal = print.print(transLog.getLastTransLog(), false, false);
            } while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
            if (retVal == Printer.PRINTER_OK) {
                return 0;
            } else {
                return Tcode.T_printer_exception;
            }
        } else {
            return 0;
        }
    }

    /**
     * 设置发卡行脚本数据
     *
     * @return
     */
    private TransLogData setScriptData() {
        TransLogData LogData = new TransLogData();

        LogData.setMsgID("0220");
        LogData.setProcCode("000000");


        LogData.setPan(PAYUtils.getSecurityNum(Pan, 6, 3));
        LogData.setIccdata(ICCData);
        LogData.setBatchNo(BatchNo);
        LogData.setOtherAmount(OtherAmount);

        if (iso8583.getfield(4) != null) {
            LogData.setAmount(Long.parseLong(iso8583.getfield(4)));
        } else {
            LogData.setAmount(Amount);
        }

        if (iso8583.getfield(11) != null) {
            LogData.setTraceNo(iso8583.getfield(11));
        }

        if (iso8583.getfield(12) != null) {
            LogData.setLocalTime(iso8583.getfield(12));
        }

        if (iso8583.getfield(13) != null) {
            LogData.setLocalDate(iso8583.getfield(13));
        }

        if (EntryMode != null) {
            LogData.setEntryMode(EntryMode);
        }

        if (iso8583.getfield(23) != null) {
            LogData.setPanSeqNo(iso8583.getfield(23));
        }

        if (Nii != null) {
            LogData.setNii(Nii);
        }


        if (SvrCode != null) {
            LogData.setSvrCode(SvrCode);
        }

        if (Nii != null) {
            LogData.setNii(Nii);
        }

        if (iso8583.getfield(32) != null) {
            LogData.setAcquirerID(iso8583.getfield(32));
        }

        if (iso8583.getfield(37) != null) {
            LogData.setRrn(iso8583.getfield(37));
        }

        if (iso8583.getfield(38) != null) {
            LogData.setAuthCode(iso8583.getfield(38));
        }

        if (Field63 != null) {
            LogData.setField63(Field63);
        }


        return LogData;
    }

    /**
     * 设置冲正数据
     *
     * @return
     */
    protected TransLogData setReveralData() {

        TransLogData LogData = new TransLogData();

        if (transEname != null) {
            LogData.setEName(transEname);
        }

        if (Field02 != null) {
            LogData.setField02(Field02);
        }

        if (Pan != null) {
            LogData.setPan(Pan);
        }

        if (ProcCode != null) {
            LogData.setProcCode(ProcCode);
        }

        LogData.setAmount(Amount);

        if (TraceNo != null) {
            LogData.setTraceNo(TraceNo);
        }

        if (Field19 != null) {
            LogData.setField19(Field19);
        }

        if (LocalTime != null) {
            LogData.setLocalTime(LocalTime);
        }

        if (LocalDate != null) {
            LogData.setLocalDate(LocalDate);
        }

        if (EntryMode != null) {
            LogData.setEntryMode(EntryMode);
        }

        if (PanSeqNo != null) {
            LogData.setPanSeqNo(PanSeqNo);
        }

        if (Nii != null) {
            LogData.setNii(Nii);
        }

        if (SvrCode != null) {
            LogData.setSvrCode(SvrCode);
        }

        if (AcquirerID != null) {
            LogData.setAcquirerID(AcquirerID);
        }

        if (Track2 != null) {
            LogData.setTrack2(Track2);
        }

        if (RRN != null) {
            LogData.setRrn(RRN);
        }

        if (AuthCode != null) {
            LogData.setAuthCode(AuthCode);
        }

        if (TermID != null) {
            LogData.setTermID(TermID);
        }

        if (MerchID != null) {
            LogData.setMerchID(MerchID);
        }

        if (Field48 != null) {
            LogData.setField48(Field48);
        }

        if (CurrencyCode != null) {
            LogData.setCurrencyCode(CurrencyCode);
        }

        if (PIN != null) {
            LogData.setPin(PIN);
        }

        if (ICCData != null) {
            LogData.setIccdata(ICCData);
            LogData.setField55(ISOUtil.convertStringToHex(ISOUtil.byte2hex(ICCData)));
        }

        if (Field61 != null) {
            LogData.setField61(Field61);
        }

        if (NroCargo != null) {
            LogData.setField62(NroCargo);
        }

        if (Field63 != null) {
            LogData.setField63(Field63);
        }


        return LogData;
    }

    protected int printData(TransLogData logData) {
        try {

            transUI.showImprimiendo(timeout);
            PrintManager printManager = PrintManager.getmInstance(context, transUI);
            if (modoCaja && isCajas) {
                printManager.setTraceNo(TraceNo);
                printManager.setCajas(isCajas);
            }
            printManager.setHost_id(host_id);
            if (emv != null && emv.getPinBlokOffline().equals("PIN OFFLINE")) {
                printManager.setPinOffline(emv.getPinBlokOffline());
            }


            String tipoVoucher = "AMBOS";
            if (APLICACIONES.getSingletonInstanceAppActual(POLARIS_APP_NAME) != null) {
                tipoVoucher = APLICACIONES.getSingletonInstanceAppActual(POLARIS_APP_NAME).getImprimirCopiaComercio();
            } else {
                ISOUtil.showMensaje("No se logra procesar aplicacione 1s", context);
                Logger.debug("No se logra procesar aplicacione 1");
            }

            do {
                switch (tipoVoucher) {
                    case "AMBOS":
                    case "NINGUNO":
                        retVal = impresionAmbos(printManager, logData);
                        break;
                    case "COPIA COMERCIO":
                        retVal = impresionComercio(printManager, logData);
                        break;
                    case "COPIA CLIENTE":
                        retVal = impresionCliente(printManager, logData);
                        break;
                    default:
                        retVal = impresionAmbos(printManager, logData);
                        break;
                }
            } while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }

        if (retVal != Printer.PRINTER_OK) {
            retVal = Tcode.T_printer_exception;
        }
        return retVal;
    }

    private int impresionComercio(PrintManager printManager, TransLogData logData) {
        retVal = printManager.print(logData, false, false);
        if (!logData.getEName().equals(SETTLE) && !logData.getEName().equals(Trans.Type.AUTO_SETTLE)) {
            transUI.showFinish();
        }
        return retVal;
    }

    private int impresionCliente(PrintManager printManager, TransLogData logData) {
        if (!logData.getEName().equals(SETTLE) && !logData.getEName().equals(Trans.Type.AUTO_SETTLE)) {
            InputInfo inputInfo = transUI.showResult(timeout, true, false, true, DataAdicional.getField(22));
            if (inputInfo.isResultFlag()) {
                transUI.showImprimiendo(timeout);
                retVal = printManager.print(logData, true, false);
                if (retVal == Printer.PRINTER_OK) {
                    transUI.showFinish();
                }
            }
        } else {
            retVal = printManager.print(logData, false, false);
        }
        return retVal;
    }

    private int impresionAmbos(PrintManager printManager, TransLogData logData) {

        retVal = printManager.print(logData, false, false);
        if (!logData.getEName().equals(SETTLE) && !logData.getEName().equals(Trans.Type.AUTO_SETTLE)) {
            InputInfo inputInfo = transUI.showResult(timeout, true, false, true, DataAdicional.getField(22));
            Log.e("inp>>", "inputInfo" + inputInfo);
            if (inputInfo.isResultFlag()) {
                transUI.showImprimiendo(timeout);
                retVal = printManager.print(logData, true, false);
                if (retVal == Printer.PRINTER_OK) {
                    transUI.showFinish();
                }
            }
        }

        return retVal;
    }

    private int printDataReject(String value1, String value2, int ret) {
        transUI.handling(timeout, Tcode.Status.printing_recept);
        PrintManager printManager = PrintManager.getmInstance(context, transUI);
        do {
            retVal = printManager.printTransreject(value1, value2, ret);
        } while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
        if (retVal == Printer.PRINTER_OK) {
            retVal = 0;
        } else {
            retVal = Tcode.T_printer_exception;
        }
        return retVal;
    }

    /**
     * deal with GAC2
     *
     * @return
     */
    private int genAC2Trans() {
        PBOCOnlineResult result = new PBOCOnlineResult();
        result.setField39(RspCode.getBytes());
        result.setFiled38(AuthCode.getBytes());
        result.setField55(ICCData);
        result.setResultCode(PBOCOnlineResult.ONLINECODE.SUCCESS);
        int retVal = pbocManager.afterOnlineProc(result);
        Logger.debug("genAC2Trans->afterOnlineProc:" + retVal);

        //Issue script deal result
        int isResult = pbocManager.getISResult();
        if (isResult != EMVISRCode.NO_ISR) {
            // save issue script result
            byte[] temp = new byte[256];
            int len = PAYUtils.pack_tags(PAYUtils.wISR_tags, temp);
            if (len > 0) {
                ICCData = new byte[len];
                System.arraycopy(temp, 0, ICCData, 0, len);
            } else {
                ICCData = null;
            }
            TransLogData scriptResult = setScriptData();
            TransLog.saveScriptResult(scriptResult);
        }

        if (retVal != PBOCode.PBOC_TRANS_SUCCESS) {
            //IC card transaction failed, if return "00" in field 39,
            //update the field 39 as "06" in reversal data
            TransLogData revesalData = TransLog.getReversal();
            if (revesalData != null) {
                revesalData.setRspCode("06");
                TransLog.saveReversal(revesalData);
            }
        }

        return retVal;
    }

    //============================= Card Functionalities ============================

    private int verificarCodDiners() {

        //Codigo Diners
        if ("88".equals(RspCode)) {
            for (int i = 0; i < 3; i++) {

                transUI.handling(timeout, Tcode.Status.msg_cod_diners);
                while (true) {
                    try {
                        Thread.sleep(20 * 1000);
                    } catch (InterruptedException e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        Thread.currentThread().interrupt();
                        Logger.error("Exception" + e.toString());
                    }
                    break;
                }

                transUI.handling(timeout, Tcode.Status.send_data_2_server);
                retVal = OnLineTrans();
                transUI.handling(timeout, Tcode.Status.send_over_2_recv);
                if (retVal == 0) {

                    RspCode = iso8583.getfield(39);

                    if (RspCode.equals("00")) {
                        return retVal;
                    }
                    if (RspCode.equals("88")) {
                        continue;
                    } else {
                        break;
                    }
                }
            }
        }

        return retVal;
    }

    private String[] UnpackFld57MultiAcq(String fld57) {
        String id = "";
        String[] rspField57 = new String[2];
        String[] identificadoresActivos = new String[25];

        final int ID_007 = 5;
        final int ID_017 = 14;
        final int ID_018 = 15;

        for (int i = 0; i < rspField57.length; i++) {
            rspField57[i] = "-";
        }

        for (int i = 0; i < identificadoresActivos.length; i++) {
            identificadoresActivos[i] = "-";
        }

        if (fld57 != null) {
            id = fld57.substring(0, 3);
            String msg = fld57.substring(3);

            try {
                switch (id) {

                    //Nombre y MID del POS
                    case "007":
                        identificadoresActivos[ID_007] = id;
                        rspField57[NOMBRE_COMERCIO] = msg.substring(0, 16);
                        rspField57[MID] = msg.substring(16, 26);
                        break;
                    //Nombre + MID + Valor financiacion
                    case "017":
                        identificadoresActivos[ID_017] = id;
                        rspField57[NOMBRE_COMERCIO] = msg.substring(0, 16);
                        rspField57[MID] = msg.substring(16, 26);
                        break;
                    case "018":
                        identificadoresActivos[ID_018] = id;
                        rspField57[NOMBRE_COMERCIO] = msg.substring(0, 16);
                        rspField57[MID] = msg.substring(16, 26);
                        break;
                }
            } catch (IndexOutOfBoundsException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }

        return rspField57;
    }

    protected boolean checkBatchAndSettle(boolean checkBatch, boolean checkSettle) {

        //Chequear el numero de trans en el lote
        if (transLog.getSize() >= TOTAL_BATCH) {
            transUI.showError(timeout, retVal, true);
            return false;
        }
        return true;
    }

    protected int cardProcess(int mode, boolean opciones, boolean isUltimoIntento) {

        CardInfo cardInfo = transUI.getCardUse("Pasar la tarjeta", timeout, mode, transEname, Amount, opciones);

        if (cardInfo.isResultFalg()) {
            transUI.handling(timeout, Tcode.Status.handling);
            int type = cardInfo.getCardType();
            switch (type) {
                case CardManager.TYPE_MAG:
                    inputMode = ENTRY_MODE_MAG;
                    break;
                case CardManager.TYPE_ICC:
                    inputMode = ENTRY_MODE_ICC;
                    break;
                case CardManager.TYPE_NFC:
                    inputMode = ENTRY_MODE_NFC;
                    break;
                case CardManager.TYPE_HAND:
                    inputMode = ENTRY_MODE_HAND;
                    break;
                default:
                    transUI.showError(timeout, Tcode.T_not_allow, true);
                    return 0;
            }
            para.setInputMode(inputMode);
            if (inputMode == ENTRY_MODE_ICC) {
                Logger.logLine(LogType.FLUJO, clase, " ENTRY_MODE_ICC " + inputMode);
                if (isICC1()) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (inputMode == ENTRY_MODE_MAG) {
                isDebit = false;
                Logger.logLine(LogType.FLUJO, clase, " ENTRY_MODE_MAG " + inputMode);
                if (isMag1(cardInfo.getTrackNo()))
                    return 1;

            }
            if (inputMode == ENTRY_MODE_NFC) {
                transUI.showContacLessInfo(false);
                if (cfg.isForcePboc()) {
                    if (isICC1()) {
                        return 1;
                    }
                } else {
                    if (PBOCTrans1()) {
                        if (Unionpay) {
                            cardInfo = transUI.getCardUse("PROCESANDO ...", timeout, mode);
                            if (!cardInfo.isResultFalg()) {
                                transUI.showError(timeout, Tcode.T_err_detect_card_failed, false);
                                return 0;
                            }

                            if (PBOCTransUPI()) {
                                return 1;
                            } else {
                                return 0;
                            }
                        } else {
                            return 1;
                        }
                    } else {
                        return 0;
                    }

                }


            }
            if (inputMode == ENTRY_MODE_HAND) {
                isDebit = false;
                if (isHandle1())
                    return 1;
            }
        } else {
            retVal = cardInfo.getErrno();
            if (retVal == 0) {
                transUI.showFinish();
                return 0;
            }

            if (retVal == 107) {
                if (!CommonFunctionalities.validateCard(timeout, transUI)) {
                    //retVal = Tcode.T_err_timeout;
                    transUI.showError(timeout, Tcode.T_err_timeout, false);
                    return 0;
                }
                contFallback++;
            }
            if (retVal == 131) {
                transUI.showError(timeout, retVal, false);
                return 0;
            }
        }

        if (contFallback == FALLBACK) {
            isFallBack = true;
            DataAdicional.addOrUpdate(13, "1");
            if (cardICCFallback(true)) {
                return 1;
            }
        }

        if (retVal == 107) {
            return cardProcess(mode, opciones, false);
        }

        if (!isUltimoIntento && retVal == 103) {
            return cardProcess(mode, opciones, true);
        }
        System.out.println("RESPUESTA ---- " + retVal);
        transUI.showError(timeout, Tcode.T_err_detect_card_failed, false);
        return 1010;
    }

    public boolean isICC1() {
        String creditCard = "SI";
        para.setAmount(Amount);
        para.setOtherAmount(OtherAmount);
        transUI.handling(timeout, Tcode.Status.handling);
        emv = new EmvTransaction(para, "aqui va la transaccion", TipoVenta, timeout);
        montoInicial = String.valueOf(Amount);
        System.out.println("Monto inicial " + Amount);
        emv.setCajas(isCajas);
        emv.setTraceNo(TraceNo);
        retVal = emv.start();
        Pan = emv.getCardNo();

        Logger.logLine(LogType.FLUJO, clase, " isICC1  retVal " + retVal);


        OtherAmount = para.getOtherAmount();

        Logger.logLine(LogType.FLUJO, clase, " isICC1  OtherAmount " + OtherAmount);

        if (retVal == 1 || retVal == 0) {
            //Credito
            if (PAYUtils.isNullWithTrim(emv.getPinBlock())) {
                isPinExist = true;
                isDebit = false;
            }//Cancelo usuario
            else if (emv.getPinBlock().equals("CANCEL")) {
                isPinExist = false;
                transUI.showError(timeout, Tcode.T_user_cancel_pin_err, false);
                return false;
            } else if (emv.getPinBlock().equals("NULL")) {
                if (emv.getCardAID().equals("A000000333010102")) {
                    isDebit = isPinExist = true;
                } else {
                    isPinExist = false;
                    transUI.showError(timeout, Tcode.T_err_pin_null, false);
                    return false;
                }


            }
            //debito
            else {
                creditCard = "NO";
                isPinExist = true;
                isDebit = true;
            }
            if (isPinExist) {

                if (creditCard.equals("NO")) {
                    PIN = emv.getPinBlock();
                    Ksn = emv.getKsnEmv();
                }
                setICCData();
                retVal = 0;
                //prepareOnline();
                return true;
            } else {
                transUI.showError(timeout, retVal, false);
                return false;
            }
        } else {
            transUI.showError(timeout, retVal, false);
            return false;
        }
    }

    protected boolean isMag1(String[] tracks) {
        String data1 = null;
        String data2 = null;
        String data3 = null;
        int msgLen = 0;
        if (tracks[0].length() > 0 && tracks[0].length() <= 80) {
            data1 = tracks[0];
        }
        if (tracks[1].length() >= 13 && tracks[1].length() <= 37) {
            data2 = tracks[1];
            if (!data2.contains("=")) {
                retVal = Tcode.T_search_card_err;
            } else {
                String judge = data2.substring(0, data2.indexOf('='));
                if (judge.length() < 13 || judge.length() > 19) {
                    retVal = Tcode.T_search_card_err;
                } else {
                    if (data2.indexOf('=') != -1) {
                        msgLen++;
                    }
                }
            }
        }
        if (tracks[2].length() >= 15 && tracks[2].length() <= 107) {
            data3 = tracks[2];
        }
        if (retVal != 0) {
            transUI.showError(timeout, retVal, false);
            return false;
        } else {
            if (msgLen == 0) {
                //retVal = Tcode.T_search_card_err;
                transUI.showError(timeout, Tcode.T_search_card_err, false);

                return false;
            } else {
                int splitIndex = data2.indexOf("=");
                char isDebitChar = data2.charAt(splitIndex + 7);
                try {
                    if (incardTable(data2.substring(0, data2.indexOf('=')))) {
                        isDebit = tablaCards.isRequierePin();
                    } else {
                        if (isDebitChar == '0' || isDebitChar == '5' || isDebitChar == '6' || isDebitChar == '7') {
                            isDebit = true;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    //retVal = Tcode.T_read_app_data_err;
                    transUI.showError(timeout, Tcode.T_read_app_data_err, false);
                    return false;
                }

                if (data2.length() - splitIndex >= 5) {
                    char iccChar = data2.charAt(splitIndex + 5);

                    if ((iccChar == '2' || iccChar == '6') && (!isFallBack)) {
                        return cardICCFallback(false);
                    } else {
                        return afterMAGJudge1(data1, data2, data3);
                    }
                } else {
                    transUI.showError(timeout, Tcode.T_search_card_err, false);
                    return false;
                }
            }
        }
    }

    protected boolean cardICCFallback(boolean isFallback) {
        CardInfo cardInfo = null;
        isFallBack = isFallback;
        if (!isFallback) {
            ModeloMensajeConfirmacion confirmacion = new ModeloMensajeConfirmacion();
            String monto = String.valueOf(Amount);
            monto = PAYUtils.FormatPyg(monto);
            confirmacion.setTitulo("Monto compra: Gs. " + monto);
            confirmacion.setMensaje("Inserte la tarjeta chip en lector de chip.");
            InputInfo inputInfo = transUI.showMensajeConfirmacion(3000, confirmacion);
            if (inputInfo.isResultFlag()) {
                retVal = 0;
            }

        } else {
            retVal = 0;
            contFallback = 0;
        }
        if (0 == retVal) {
            if (!isFallback) {
                cardInfo = transUI.getCardFallback("Inserte la tarjeta por chip", timeout, INMODE_IC, transEname, Amount);
            } else {
                cardInfo = transUI.getCardUse("Pasar la tarjeta", timeout, INMODE_MAG, transEname, Amount, false);
            }
            if (cardInfo.isResultFalg()) {
                int type = cardInfo.getCardType();
                switch (type) {
                    case CardManager.TYPE_MAG:
                        inputMode = ENTRY_MODE_MAG;
                        break;
                    case CardManager.TYPE_ICC:
                        inputMode = ENTRY_MODE_ICC;
                        break;
                    default:
                        transUI.showError(timeout, Tcode.T_not_allow, false);
                        return false;
                }
                para.setInputMode(inputMode);
                if (inputMode == ENTRY_MODE_ICC) {
                    return isICC1();

                }
                if (inputMode == ENTRY_MODE_MAG) {
                    isDebit = false;
                    return isMag1(cardInfo.getTrackNo());
                }
            } else {
                retVal = cardInfo.getErrno();
                if (retVal == 0) {
                    transUI.showError(timeout, Tcode.T_user_cancel_input, false);
                    return false;
                } else {
                    transUI.showError(timeout, Tcode.T_err_detect_card_failed, false);
                    return false;
                }
            }
        } else {
            transUI.showError(timeout, Tcode.T_user_cancel_operation, false);
            return false;
        }
        return true;
    }

    private boolean afterMAGJudge1(String data1, String data2, String data3) {
        String cardNo = data2.substring(0, data2.indexOf('='));

        montoInicial = String.valueOf(Amount);

        if (!modoCajaPOS(cardNo)) {
            transUI.showError(timeout, Tcode.T_monto_invalido, false);
            return false;
        }

        Pan = cardNo;
        Track1 = data1;
        Track2 = data2;
        Track3 = data3;

        if ((retVal = CommonFunctionalities.last4card(timeout, TransEName, Pan, transUI, false)) != 0) {
            return false;
        }

        CVV = CommonFunctionalities.getCvv2();

        return true;
        //prepareOnline();
    }

    protected boolean isHandle1() {
        if ((retVal = CommonFunctionalities.setPanManual(timeout, TransEName, transUI)) != 0) {
            return false;
        }

        Pan = CommonFunctionalities.getPan();

        /*if (!CommonFunctionalities.permitirTransGasolinera(Pan)){
            //retVal = Tcode.T_msg_err_gas;
            transUI.showError(timeout, Tcode.T_msg_err_gas);
            return false;
        }*/

        if (!incardTable(Pan)) {
            //retVal = Tcode.T_unsupport_card;
            transUI.showError(timeout, Tcode.T_unsupport_card, false);
            return false;
        }

        ExpDate = CommonFunctionalities.getExpDate();


        CVV = CommonFunctionalities.getCvv2();

        if (!verificaPedirVuelto()) {
            transUI.showError(timeout, Tcode.T_user_cancel_operation, false);
            return false;
        }

        return true;
        //prepareOnline();
    }

    protected boolean PBOCTrans1() {

        int code = 0;

        PBOCTransProperty property = new PBOCTransProperty();
        property.setTag9c(PBOCTag9c.sale);
        property.setTraceNO(Integer.parseInt(TraceNo));
        property.setFirstEC(false);
        property.setForceOnline(true);
        property.setAmounts(Amount);
        property.setOtherAmounts(OtherAmount);
        property.setIcCard(false);

        //transUI.handling(timeout, Tcode.Status.process_trans);

        emvl2 = new EmvL2Process(this.context, transUI);
        emvl2.setTraceNo(TraceNo);//JM
        emvl2.setTypeTrans(TransEName);

        if ((retVal = emvl2.emvl2ParamInit()) != 0) {
            switch (retVal) {
                case 1:
                    retVal = Tcode.T_err_not_file_terminal;
                    break;
                case 2:
                    retVal = Tcode.T_err_not_file_processing;
                    break;
                case 3:
                    retVal = Tcode.T_err_not_file_entry_point;
                    break;
            }
            transUI.showError(timeout, retVal, false);
            return false;
        }

        montoInicial = String.valueOf(Amount);
        emvl2.SetAmount(Amount, OtherAmount);
        emvl2.setTypeCoin(typeCoin);//JM
        code = emvl2.start();

        if (emvl2.getAid() != null) {
            if (emvl2.getAid().contains("A000000333")) {
                Unionpay = true;
                return true;
            }
        }


        Logger.debug("EmvL2Process return = " + code);
        if (code != 0) {
            switch (code) {
                case 1:
                    retVal = Tcode.T_usar_chip;
                    break;
                case 7:
                    retVal = Tcode.T_insert_card;
                    break;
                case 15:
                    retVal = Tcode.T_aplicacion_no_encontrada;
                    break;
                default:
                    retVal = Tcode.T_err_detect_card_failed;
                    break;
            }

            if (retVal != Tcode.T_usar_chip && retVal != Tcode.T_insert_card) {
                transUI.showError(timeout, retVal, false);
                return false;
            } else {
                return cardICCFallback(false);
            }

        }


        if (!modoCajaPOS(emvl2.GetCardNo())) {
            transUI.showError(timeout, Tcode.T_monto_invalido, false);
            return false;
        }

        Pan = emvl2.GetCardNo();
        PanSeqNo = emvl2.GetPanSeqNo();
        Track2 = emvl2.GetTrack2data();
        ICCData = emvl2.GetEmvOnlineData();
        MasterControl.HOLDER_NAME = emvl2.getHolderName();
        Logger.error("PAN =" + Pan);

        /*if (!CommonFunctionalities.permitirTransGasolinera(Pan)){
            //retVal = Tcode.T_msg_err_gas;
            transUI.showError(timeout, Tcode.T_msg_err_gas);
            return false;
        }*/

        if (!incardTable(Pan)) {
            //retVal = Tcode.T_unsupport_card;
            transUI.showError(timeout, Tcode.T_unsupport_card, false);
            return false;
        }

        if (!verificaPedirVuelto()) {
            transUI.showError(timeout, Tcode.T_user_cancel_operation, false);
            return false;
        }

        if (emvl2.GetCVMType() == EmvL2CVM.L2_CVONLINE_PIN || VerificarPin()) {
            long newAmount = 0;
            if (OtherAmount > 0) {
                newAmount = Amount + OtherAmount;
            } else {
                newAmount = Amount;
            }
            if (ctlPIN(Pan, timeout, newAmount, transUI) != 0) {
                //retVal = Tcode.T_user_cancel_input;
                transUI.showError(timeout, Tcode.T_user_cancel_input, false);
                return false;
            }
            PIN = getPin();
            Ksn = getKsn();
            isPinExist = true;
        }

        if (emvl2.GetCVMType() == EmvL2CVM.L2_CVOBTAIN_SIGNATURE) {
            MasterControl.CTL_SIGN = true;
        }


        if (!handlePBOCode1(PBOCode.PBOC_REQUEST_ONLINE))
            return false;


        return true;
    }

    private boolean modoCajaPOS(String pan) {
        final boolean[] respuesta = {false};
        if (modoCaja && isCajas) {
//            Activity activity = (Activity) mcontext;
            final CountDownLatch latch = new CountDownLatch(1);
            Logger.logLine(LogType.ERROR, clase + " Metodo : modoCajaPOS", "Ingreso al Metodo  de CajaPOS");

            final ProcesamientoCajas procesamientoCajas = new ProcesamientoCajas(mcontext, transUI);
            procesamientoCajas.lecturaTarjeta2Caja(pan, TraceNo);

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

    private boolean VerificarPin() {
        if (tablaCards.isRequierePin()) {
            //todo monto pedir pin configurable
            if (Amount > 10000000) {
                return true;
            }
        }
        return false;

    }

    protected boolean verificaPedirVuelto() {
        long rta;
        if (TipoVenta.equals(DefinesBANCARD.CONSULTA_DEUDAS)) {
            OtherAmount = para.getOtherAmount();
            if (para.isNeedOtherAmount() == DefinesBANCARD.DATO_HABILITADO) {
                rta = CommonFunctionalities.validaPedirVuelto(context, timeout, transUI, Pan, Amount);
                if (rta > 0) {// Monto ingresado valido
                    OtherAmount = rta;
                    para.setAmount(Amount);
                    para.setOtherAmount(OtherAmount);
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
     * handle PBOC transaction
     *
     * @param code
     */
    private boolean handlePBOCode1(int code) {
        if (code != PBOCode.PBOC_REQUEST_ONLINE) {
            transUI.showError(timeout, code, false);
            return false;
        }
        if (inputMode != ENTRY_MODE_NFC)
            setICCDataCTL();

        return true;
    }

    protected boolean requestPin1() {

        if (inputMode == ENTRY_MODE_MAG) {
            if (isDebit && !isPinExist) {
                long newAmount = 0;
                if (OtherAmount > 0) {
                    newAmount = Amount + OtherAmount;
                } else {
                    newAmount = Amount;
                }
                PinInfo info = transUI.getPinpadOnlinePinDUKPT(timeout, String.valueOf(newAmount), Pan);
                if (info.isResultFlag()) {
                    if (info.isNoPin()) {
                        isPinExist = false;
                    } else {
                        if (null == info.getPinblock()) {
                            isPinExist = false;
                        } else {
                            isPinExist = true;
                            Ksn = info.getKsnString();
                        }
                        PIN = ISOUtil.hexString(Objects.requireNonNull(info.getPinblock()));
                    }
                    if (isPinExist) {
                        return true;
                    } else {
                        transUI.showError(timeout, info.getErrno(), false);
                        return false;
                    }
                } else {
                    transUI.showError(timeout, Tcode.T_user_cancel_pin_err, false);
                    return false;
                }
            }
        }
        return true;
    }

    private void setKsn() {
        if (Ksn != null)
            DataAdicional.addOrUpdate(33, Ksn);
    }

    /**
     * @return Tcode.T_success : Proceso Exitoso
     * Errores :
     * -1 :
     * Timeout
     * Cancelado por usuario
     * No existe informacion de cuentas en subCampo 30. Muestra mensaje internamente
     */
    private int procesarMultipleCuenta() {
        ArrayList<ModeloBotones> cuentas = obtenerCuentasCampo30();

        if (cuentas != null) {
            InputInfo inputInfo = transUI.showBotones(timeout, "", cuentas);
            if (inputInfo.isResultFlag()) {
                if (inputInfo.getResult().equals("CANCEL")) {
                    transUI.showFinish();
                    // Cancelado por el usuario, retorna -1
                } else {
                    String result = inputInfo.getResult();
                    ProcCode = cambiarProcCode(result, 2);
                    TraceNo = ISOUtil.padleft("" + cfg.getTraceNo(), 6, '0');
                    return Tcode.T_success;
                }
            } // Timeout, retorna -1
        } else {
            transUI.showError(timeout, Tcode.T_no_datos_cuentas, false); // Retorna -1
        }
        return -1; // Timeout
    }

    protected boolean PBOCTransUPI() {
        int code = 0;

        PBOCTransProperty property = new PBOCTransProperty();
        property.setTag9c(PBOCTag9c.sale);
        property.setTraceNO(Integer.parseInt(TraceNo));
        property.setFirstEC(false);
        property.setForceOnline(true);
        property.setAmounts(Amount);
        property.setOtherAmounts(0);
        property.setIcCard(false);

        property.setTransFlow(PBOCTransFlow.QPASS);

        code = startPBOCUPI(property);
        Logger.logLine(LogType.FLUJO, clase, "startPBOCUPI : " + code);
        return handlePBOCodeUPI(code);

    }

    /**
     * handle PBOC transaction
     *
     * @param code
     */
    protected boolean handlePBOCodeUPI(int code) {
        byte[] temp = new byte[256];
        emvPBOCUpi = new EmvPBOCUpi(temp);
        byte[] dest;
        String AID;

        int len = PBOCUtil.get_tlv_data_kernel(0x9F06, temp);
        if (len > 0) {
            dest = new byte[len];
            System.arraycopy(temp, 0, dest, 0, len);
        } else {
            return false;
        }

        AID = ISOUtil.hexString(dest);

        emvPBOCUpi.setAID(AID);
        //AID = emvl2.tkn;

        if (AID.equals("A0000003330101021111111111111111") && code == 6011) {

            try {
                Beeper.getInstance().beep(1000, 500);
            } catch (Exception e) {
                // Log.println(Log.ERROR, onlineTrans, e.getMessage());
            }

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                //Log.println(Log.ERROR, onlineTrans, e.getMessage());
            }

            if (cardProcess(INMODE_NFC, false, true) != 1)
                return false;
        } else if (code != PBOCode.PBOC_REQUEST_ONLINE && (AID.equals("A0000003330101021111111111111111") || AID.equals("A000000333010102")
                || AID.equals("A000000333010103") || AID.equals("A000000333010101"))) {
            transUI.showError(timeout, code, false);
            return false;
        }

        Pan = PBOCUtil.getPBOCCardInfo().getCardNO();

        if (!modoCajaPOS(Pan)) {
            transUI.showError(timeout, Tcode.T_monto_invalido, false);
            return false;
        }

        boolean incardTable = incardTable(Pan);
        Logger.logLine(LogType.FLUJO, clase, "handlePBOCodeUPI incardTable : " + incardTable);
        if (!incardTable) {
            transUI.showError(timeout, Tcode.T_unsupport_card, false);
            return false;
        }

        len = PBOCUtil.get_tlv_data_kernel(0x5F20, temp);
        if (len > 0) {
            dest = new byte[len];
            System.arraycopy(temp, 0, dest, 0, len);
            String nameCard = new String(dest);
            //MasterControl.HOLDER_NAME = ISOUtil.hexString(dest);
            MasterControl.HOLDER_NAME = nameCard.trim();
        }

        try {
            Beeper.getInstance().beep(1000, 500);
        } catch (SDKException e) {

        }

        if (handleNFCPin()) {
            setICCDataCTL();
            return true;
        }

        return false;
    }

    /**
     * start PBOC transaction
     *
     * @param property transaction property
     * @return
     */
    protected int startPBOCUPI(PBOCTransProperty property) {
        /* transUI.handling(timeout, Tcode.Status.process_trans, TransEName);*/
        int pboccode;
        try {
            pboccode = pbocManager.startPBOC(property, listener);
        } catch (PBOCException e) {
            Logger.logLine(LogType.EXCEPTION, clase, " startPBOCUPI " + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
            pboccode = PBOCode.PBOC_UNKNOWN_ERROR;
        }
        return pboccode;
    }

    /**
     * handle enter PIN of contactless card
     *
     * @return
     */
    protected boolean handleNFCPin() {

        byte[] temp = new byte[256];
        byte[] dest;
        String AID;
        long CVM = 0;
        int len;

        len = PBOCUtil.get_tlv_data_kernel(0x9F06, temp);
        if (len > 0) {
            dest = new byte[len];
            System.arraycopy(temp, 0, dest, 0, len);
        } else {
            //  errorProcess(DefinesCajas.PP, DefinesCajas.ERROR_PROCESO, Tcode.T_not_allow);
            transUI.showError(timeout, Tcode.T_not_allow, false);
            return false;
        }

        AID = ISOUtil.hexString(dest);
        Logger.logLine(LogType.FLUJO, clase, "handleNFCPin AID : " + AID);

        if (AID.equals("A000000333010103") || AID.equals("A000000333010102")) {

            try {
                CVM = EMVHandler.getInstance().getAidInfo(11).getClCVMAmount();
            } catch (SDKException e) {
            }

            if (Amount <= CVM)
                return true;
        }

        if (!pbocManager.isCardSupportedOnlinePIN()) {
            return true;
        }
        PinInfo info = transUI.getPinpadOnlinePin(timeout, String.valueOf(Amount), PBOCUtil.getPBOCCardInfo().getCardNO());
        if (info.isResultFlag()) {
            if (info.isNoPin()) {
                isPinExist = false;
            } else {
                if (null == info.getPinblock()) {
                    isPinExist = false;
                } else {
                    isPinExist = true;
                }
                PIN = ISOUtil.hexString(info.getPinblock());
                return true;
            }
        } else {
            transUI.showError(timeout, info.getErrno(), false);
            return false;
        }

        return false;
    }


}