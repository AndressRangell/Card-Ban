package com.newpos.libpay.trans;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.newpos.libemv.PBOCManager;
import com.android.newpos.libemv.PBOCTerConf;
import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.Device;
import com.cobranzas.transactions.common.CommonFunctionalities;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.contactless.EmvL2Process;
import com.newpos.libpay.device.contactless.EmvPBOCUpi;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.helper.iso8583.ISO8583;
import com.newpos.libpay.helper.ssl.NetworkHelper;
import com.newpos.libpay.paras.EmvAidInfo;
import com.newpos.libpay.paras.EmvCapkInfo;
import com.newpos.libpay.presenter.TransUI;
import com.newpos.libpay.process.EmvTransaction;
import com.newpos.libpay.process.QpbocTransaction;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.emv.CoreParam;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVHandler;
import com.wposs.cobranzas.BuildConfig;
import com.wposs.cobranzas.R;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import static android.content.Context.MODE_MULTI_PROCESS;
import static android.content.Context.MODE_PRIVATE;
import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.cobranzas.actividades.StartAppBANCARD.tablaDevice;
import static com.cobranzas.actividades.StartAppBANCARD.tablaHost;
import static com.cobranzas.actividades.StartAppBANCARD.tablaIp;
import static com.cobranzas.defines_bancard.DefinesBANCARD.POLARIS_APP_NAME;
import static java.util.Calendar.getInstance;


/**
 * 交易抽象类，定义所有交易类的父类
 *
 * @author zhouqiang
 */
public abstract class Trans {

    public static final int BASE12AMOUNT = 0;
    public static final int TIPAMOUNT = 3;
    public static final int IVAAMOUNT = 4;
    public static final int BASE0AMOUNT = 5;
    public static final int SERVICEAMOUNT = 6;
    public static final int AMOUNT = 7;
    public static final int CASHOVERAMOUNT = 8;
    public static final String AGREGADO = "Agregado";
    public static final String DESAGREGADO = "Desagregado";
    public static final String MANUAL = "Manual";
    public static final String PREGUNTA = "Pregunta";
    public static final String idLote = "01";
    /**
     * 22域服务点输入方式
     */
    public static final int ENTRY_MODE_HAND = 1;
    public static final int ENTRY_MODE_MAG = 2;
    public static final int ENTRY_MODE_ICC = 5;
    public static final int ENTRY_MODE_NFC = 7;
    public static final int ENTRY_MODE_FALLBACK = 3;
    //public static final String MODE_CTL_NO_PIN = "07";
    public static final String MODE_HANDLE = "001";
    public static final String MODE_ICC = "005";
    public static final String MODE_CTL = "007";
    public static final String MODE1_FALLBACK = "080";
    public static final String MODE_MAG = "002";
    public static final String MODE2_FALLBACK = "090";
    public static final String MODE_CTL_USING_MG_STRIPE = "91";
    public static String deferredType[][] = {
            {"0", "SIN INTERES"},
            {"1", "CON INTERES"}
    };
    public static String deferreds[][] = {
            {"002", "NORMAL"},
            {"003", "MESES DE GRACIA"},
            {"004", "PAGO MES A MES"}
    };
    public static String moneda[] = new String[]{
            "Local",//0
            "Dolar",//1
            "Euro"//2
    };
    private static String defaultNii = "0000";
    public final int RFDAMOUNT = 1;
    public final int ORGAMOUNT = 2;
    protected Activity activity;
    /**
     * 上下文对象
     */
    protected Context context;
    /**
     * 8583组包解包
     */
    protected ISO8583 iso8583;
    /**
     * 网络操作对象
     */
    protected NetworkHelper netWork;
    /**
     * 交易记录的集合
     */
    protected TransLog transLog;
    /**
     * 配置文件操作实例
     */
    protected TMConfig cfg;
    /**
     * MODEL与VIEW层接口实例
     */
    protected TransUI transUI;
    /**
     * 等待页面超时时间
     */
    protected int timeout;
    /**
     * 返回值全局定义
     */
    protected int retVal;
    /**
     * EMV流程控制实例
     */
    protected EmvTransaction emv;
    /**
     * QPBOC流程控制实例
     */
    protected QpbocTransaction qpboc;
    /**
     * 交易相关参数集合
     */
    protected TransInputPara para;
    /**
     * 0 消息类型
     */
    protected String MsgID;
    protected String Field02;
    /**
     * 2* 卡号
     */
    protected String Pan;
    protected String PanPE;
    /**
     * 3  预处理码
     */
    protected String ProcCode;
    /**
     * 4* 金额
     */
    protected long Amount;
    protected String Code;
    protected long OtherAmount;
    protected long AmountAndOtherAmount;
    protected long ServiceAmount;
    protected long TipAmount;
    protected long IvaAmount;
    protected long CashOverAmount;
    protected long montoFijo;
    protected String tipoMontoFijo;
    protected String CVV;
    protected String pagoVarioSeleccionado;
    protected String pagoVarioSeleccionadoNombre;
    protected String numCuotasDeferred;
    protected String CodOTT;
    protected boolean multicomercio;
    protected String nameMultAcq;
    protected String idComercio;
    protected String issuerName;
    protected String labelName;
    protected boolean isField55;
    /**
     * 11域交易流水号
     */
    protected String TraceNo;
    protected String NroCargo;
    protected String NroBoleta;
    protected String Status;
    /**
     * 12 hhmmss*
     */
    protected String LocalTime;
    /**
     * 13 MMDD*
     */
    protected String LocalDate;
    /**
     * 14 YYMM*
     */
    protected String ExpDate;
    /**
     * 19 AcqConCod
     */
    protected String Field19;
    /**
     * 15 MMDD*
     */
    protected String SettleDate;
    /**
     * 18
     */
    protected String MerchantType;
    /**
     * 22*
     */
    protected String EntryMode;
    /**
     * 23*
     */
    protected String PanSeqNo;
    /**
     * 24*
     */
    protected String Nii;
    /**
     * 25
     */
    protected String SvrCode;
    /**
     * 26
     */
    protected String CaptureCode;
    /**
     * 32*
     */
    protected String AcquirerID;
    /**
     * 1磁道数据
     */
    protected String Track1;
    /**
     * 35
     */
    protected String Track2;
    /**
     * 36
     */
    protected String Track3;
    /**
     * 37*
     */
    protected String RRN;
    /**
     * 38*
     */
    protected String AuthCode;
    /**
     * 39
     */
    protected String RspCode;
    /**
     * 41
     */
    protected String TermID;
    /**
     * 42
     */
    protected String MerchID;
    /**
     * 44 *
     */
    protected String Field44;
    /**
     * 48 *
     */
    protected String Field48;
    /**
     * 49*
     */
    protected String CurrencyCode;
    /**
     * 52
     */
    protected String PIN;
    /**
     * 53
     */
    protected String SecurityInfo;
    /**
     * 54
     */
    protected String ExtAmount;
    protected String Field54;
    /**
     * 55*
     */
    protected byte[] ICCData;
    /**
     * 57
     */
    protected String Field57;
    /**
     * 58
     */
    protected String Field58;
    /**
     * 59
     */
    protected String Field59;
    /**
     * 60
     */
    protected String Field60;
    /**
     * 61
     */
    protected String Field61;
    /**
     * 62
     */
    protected String Field62;
    /**
     * 63
     */
    protected String Field63;
    /**
     * 64
     */
    protected String Field64;
    protected String Ksn;
    protected String TypeTransVoid;
    /**
     * 交易英文名 主键 交易初始化设置
     */
    protected String TransEName;
    protected String TipoVenta = "";
    /**
     * 批次号 60_2
     */
    protected String BatchNo;
    /**
     * 标记此次交易流水号是否自增
     */
    protected boolean isTraceNoInc = false;
    /**
     * 是否允许IC卡降级为磁卡
     */
    protected boolean isFallBack;
    /**
     * 使用原交易的第3域和 60.1域
     */
    protected boolean isUseOrgVal = false;
    protected EmvL2Process emvl2;   //非接交易
    protected EmvPBOCUpi emvPBOCUpi;
    /**
     * PBOC library manager
     */
    protected PBOCManager pbocManager;
    protected org.com.iso.Result result = null;
    protected byte[] pack = null;
    /**
     * Defered
     */
    protected String TypeDeferred;
    protected String idDeferredSel;
    String clase = "Trans.java";
    private WifiManager wifiManager;
    private String F60_1;
    private String F60_3;
    private byte[] recive = null;

    protected Trans() {
    }

    /***
     * Trans 构造
     * @param ctx
     * @param transEname
     */
    public Trans(Context ctx, String transEname) {
        try {
            this.context = ctx;
            this.TransEName = transEname;

            this.timeout = Integer.parseInt(ctx.getResources().getString(R.string.timerDataConfig));
            loadConfig();
            transLog = TransLog.getInstance(idLote);
            this.pbocManager = PBOCManager.getInstance();
            this.pbocManager.setDEBUG(true);
            loadAIDCAPK2EMVUPI();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    public Trans(Context ctx, String transEname, String fileNameLog) {
        try {
            this.context = ctx;
            this.TransEName = transEname;
            this.timeout = Integer.parseInt(ctx.getResources().getString(R.string.timerDataConfig));
            loadConfig();
            transLog = TransLog.getInstance(idLote + fileNameLog);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    private static String getTpdu(String nii) {
        if ((nii == null) || (nii.isEmpty())) {
            nii = defaultNii;
        }
        String tpdu = ISOUtil.padright("60" + nii, 10, '0');
        Log.e("Trans.java", "getTpdu: tpdu" + tpdu);
        return tpdu;
    }

    public String getNii() {
        Nii = defaultNii;
        if (APLICACIONES.getSingletonInstanceAppActual(POLARIS_APP_NAME) != null) {
            String nitPolaris = APLICACIONES.getSingletonInstanceAppActual(POLARIS_APP_NAME).getNiiTransacciones();
            if (!nitPolaris.isEmpty()) {
                Nii = nitPolaris;
            } else {
                Logger.debug("No se logra procesar aplicaciones");
                Logger.logLine(LogType.EXCEPTION, clase, "No se logra procesar aplicaciones - Nii vacio");
            }

        }

        return Nii;
    }

    /**
     * 加载初始设置
     */
    private void loadConfig() {
        cfg = TMConfig.getInstance();

        AmountAndOtherAmount = 0;


        if (tablaDevice.getNumeroCajas().length() < 8)
            TermID = ISOUtil.padleft(tablaDevice.getNumeroCajas() + "", 8, '0');
        else
            TermID = tablaDevice.getNumeroCajas().substring(0, 8);

        if (tablaComercios.sucursal.getCardAccpMerch().length() < 15)
            MerchID = ISOUtil.padleft("" + tablaComercios.sucursal.getCardAccpMerch(), 15, '0');//cfg.getMerchID();
        else
            MerchID = tablaComercios.sucursal.getCardAccpMerch();


        loadPreferences();
        CurrencyCode = cfg.getCurrencyCode();
        BatchNo = cfg.getBatchNo();
        TraceNo = ISOUtil.padleft("" + cfg.getTraceNo(), 6, '0');
        NroCargo = ISOUtil.padleft("" + cfg.getNroCargo(), 6, '0');
        cfg.setPubCommun(true);  //Se pone en true para que siempre se intente primero por la IP 1


        Nii = getNii();

        String tpdu = getTpdu(Nii);//cfg.getTpdu();
        String header = cfg.getHeader();
        setFixedDatas();

        iso8583 = new ISO8583(this.context, tpdu, header);
    }

    private void loadPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("InformacionTransaccional", MODE_MULTI_PROCESS);
        String versionName = prefs.getString("Version", null);
        if (versionName != null && !versionName.equals(BuildConfig.VERSION_NAME)) {
            cfg.setBatchNo(getValue(prefs.getString("BatchNo", null)));
            cfg.setTraceNo(getValue(prefs.getString("TraceNo", null)));
            cfg.setNroCargo("" + getValue(prefs.getString("NroCargo", cfg.getNroCargo())));
        }
    }

    protected void savePreferences() {
        SharedPreferences.Editor editor = context.getApplicationContext()
                .getSharedPreferences("InformacionTransaccional", MODE_PRIVATE).edit();
        editor.putString("BatchNo", BatchNo);
        editor.putString("TraceNo", cfg.getTraceNo());
        editor.putString("NroCargo", cfg.getNroCargo());
        editor.putString("Version", BuildConfig.VERSION_NAME);
        editor.apply();
    }

    private int getValue(String data) {
        int value = 0;
        try {
            if (data != null && !data.isEmpty()) {
                value = Integer.parseInt(data);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return value;
    }

    private void loadConfigIP(int idx) {
        int timeoutCon;
        int timeoutRes;
        boolean isTls = true;

        tablaIp = ChequeoIPs.seleccioneIP(idx);

        String ip = null;
        int port = 0;
        //JM
        try {
            if (tablaIp.getIP() != null) {
                ip = tablaIp.getIP();
            } else {
                ip = context.getResources().getString(R.string.ipConfigWifi);
            }
            if (tablaIp.getPuerto() != null) {
                port = Integer.parseInt(tablaIp.getPuerto());
            } else {
                port = Integer.parseInt(context.getResources().getString(R.string.portConfig));
            }

            if (tablaIp != null && !tablaIp.isTls()) {
                isTls = tablaIp.isTls();
            } else {
                isTls = context.getResources().getBoolean(R.bool.tls);
            }

            timeoutRes = getTiempoEsperaRespuesta(ProcCode);


            if (tablaHost.getTiempoEsperaConexion() != null) {
                timeoutCon = Integer.parseInt(tablaHost.getTiempoEsperaConexion()) * 1000;
                Log.d(clase, "loadConfigIP: Timer Conexion --> " + timeoutCon);
            } else {
                timeoutCon = Integer.parseInt(context.getResources().getString(R.string.timerConfig));
            }


        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());

            timeoutRes = Integer.parseInt(context.getResources().getString(R.string.timerConfig));
            timeoutCon = Integer.parseInt(context.getResources().getString(R.string.timerConfig));
        }

        netWork = new NetworkHelper(ip, port, context, timeoutCon, timeoutRes, isTls);


    }

    private int getTiempoEsperaRespuesta(String procCode) {
        int timeoutRespuesta = Integer.parseInt(context.getResources().getString(R.string.timerConfig));
        try {
            if (tablaHost.getTiempoEsperaRespuesta() != null) {
                timeoutRespuesta = Integer.parseInt(tablaHost.getTiempoEsperaRespuesta()) * 1000;
                Log.d(clase, "getTiempoEsperaRespuesta Timer Espera: " + timeoutRespuesta);
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            timeoutRespuesta = Integer.parseInt(context.getResources().getString(R.string.timerConfig));
        }

        return timeoutRespuesta;
    }

    /**
     * 设置消息类型及60域3个子域数据
     */
    protected void setFixedDatas() {
        Logger.debug("==Trans->setFixedDatas==");
        if (null == TransEName) {
            return;
        }
        Properties pro = PAYUtils.lodeConfig(context, TMConstants.TRANS);
        if (pro == null) {
            return;
        }
        String prop = pro.getProperty(TransEName);
        String[] propGroup = prop.split(",");
        if (!PAYUtils.isNullWithTrim(propGroup[0])) {
            MsgID = propGroup[0];
        } else {
            MsgID = null;
        }
        if (!isUseOrgVal) {
            if (!PAYUtils.isNullWithTrim(propGroup[1])) {
                ProcCode = propGroup[1];
            } else {
                ProcCode = null;
            }
        }
        if (!PAYUtils.isNullWithTrim(propGroup[2])) {
            SvrCode = propGroup[2];
        } else {
            SvrCode = null;
        }
        if (!isUseOrgVal) {
            if (!PAYUtils.isNullWithTrim(propGroup[3])) {
                F60_1 = propGroup[3];
            } else {
                F60_1 = null;
            }
        }
        if (!PAYUtils.isNullWithTrim(propGroup[4])) {
            F60_3 = propGroup[4];
        } else {
            F60_3 = null;
        }
        if (F60_1 != null && F60_3 != null) {
            Field60 = F60_1 + cfg.getBatchNo() + F60_3;
        }

    }

    /**
     * 获取流水号是否自增
     *
     * @return
     */
    public boolean isTraceNoInc() {
        return isTraceNoInc;
    }

    /**
     * 设置流水号是否自增
     *
     * @param isTraceNoInc
     */
    public void setTraceNoInc(boolean isTraceNoInc) {
        this.isTraceNoInc = isTraceNoInc;
    }

    /**
     * 追加60域内容
     *
     * @param f60
     */
    protected void appendField60(String f60) {
        Field60 = Field60 + f60;
    }

    /**
     * 连接
     *
     * @return -1 : Exepcion
     * 0 : Exitoso
     */
    private int connect() {
        return netWork.Connect();
    }

    /**
     * 发送
     *
     * @return - T_package_mac_err
     * - -1, exception
     * - 0 Succes
     */
    protected int send() {
        Logger.logLine(LogType.FLUJO, clase, "Inicio metodo send");
        byte[] pack = iso8583.packetISO8583();
        if (pack == null) {
            Logger.logLine(LogType.FLUJO, clase, "Error en armar el paquete del iso8583");
            return -1;
        }
        Logger.debug("TRANS:" + TransEName + "\nISO_TX:" + ISOUtil.hexString(pack));
        Logger.logLine(LogType.FLUJO, clase, "ISO TX = " + ISOUtil.hexString(pack));
        return netWork.Send(pack);
    }

    /**
     * 接收
     *
     * @return
     */
    protected byte[] recive() {
        recive = null;
        try {
            Logger.logLine(LogType.FLUJO, clase, "recive - Tiempo Espera (*1.000) - " + tablaHost.getTiempoEsperaRespuesta());
            recive = netWork.Recive(2048, Integer.parseInt(tablaHost.getTiempoEsperaRespuesta()) * 1000);
        } catch (InterruptedIOException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            // 读取超时处理
            Logger.logLine(LogType.COMUNICACION, clase, " recive - ERROR : " + e.getMessage());
            return null;
        } catch (IOException e) {
            // 读取超时处理
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return null;
        }

        if (recive != null) {
            Logger.debug("TRANS:" + TransEName + "\nISO_RX:" + ISOUtil.hexString(recive));
            Logger.logLine(LogType.FLUJO, clase, "recive != null");
        }
        return recive;
    }

    /**
     * 联机处理
     *
     * @return
     */
    protected int OnLineTrans() {
        int rta;

        rta = retriesConnect(0, false, false);

        if (rta == Tcode.T_socket_err) {
            return rta;
        }

        if (send() == -1) {
            return Tcode.T_send_err;
        }

        byte[] respData = recive();
        netWork.close();

        if (respData == null || respData.length <= 0) {
            return Tcode.T_receive_err;
        }

        int ret;

        ret = iso8583.unPacketISO8583(respData);

        if (ret == 0) {
            if (isTraceNoInc) {
                cfg.incTraceNo().save();
            }
        }

        return ret;
    }

    /**
     * 清除关键信息
     */
    protected void clearPan() {
        Pan = null;
        Track2 = null;
        Track3 = null;
        System.gc();//显示调用清除内存
    }

    /**
     * @param aCtn
     * @return
     */


    protected int retriesConnect(int aCtn, final boolean showMsg, boolean ultimaConexion) {
        List<Integer> listadoIP = new ArrayList<>();
        listadoIP = cargarListadoIpsWifiManager(context);
        if (listadoIP != null && listadoIP.size() > 0) {

            if (!procesamientoIntentoConexion(listadoIP, showMsg)) {
                if (Device.getConexion() && !Device.getCajaPOS()) {
                    if (!ultimaConexion) {
                        if (verificarConexionWifi(context)) {
                            listadoIP.clear();
                            return retriesConnect(0, true, true);
                        }
                    }
                }
            } else {
                return Tcode.T_success;
            }

        } else {
            listadoIP = cargarListadoIpsTelephonyManager(context);

            if (listadoIP != null && listadoIP.size() > 0) {
                if (procesamientoIntentoConexion(listadoIP, showMsg)) {
                    listadoIP.clear();
                    return Tcode.T_success;
                }
            }

        }
        return Tcode.T_socket_err;
    }

    private List<Integer> cargarListadoIpsTelephonyManager(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null && !manager.getSimOperatorName().isEmpty()) {
            return ChequeoIPs.getPosicionesIpsConexion3G(manager.getSimOperatorName());
        }

        return null;
    }

    private List<Integer> cargarListadoIpsWifiManager(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null) {
                if (info.getSSID() != null) {
                    if (info.getBSSID() != null && !info.getSSID().isEmpty() && !info.getSSID().equals("0x") && !info.getSSID().equals("<unknown ssid>")) {
                        Log.d("Data : ", "cargarListadoIpsWifiManager: " + info.getBSSID());
                        return ChequeoIPs.getPosicionesWifi("COM_WIFI", "wifi", wifiManager);
                    }
                }
            }
        }
        return null;
    }

    private boolean procesamientoIntentoConexion(List<Integer> posicionesIP, boolean showMsg) {
        int retries = 2;  //Intentos
        int timeoutConexion = 0;
        int rta = -1;
        if (tablaHost.getReintentos() != null && tablaHost.getTiempoEsperaConexion() != null) {
            retries = Integer.parseInt(tablaHost.getReintentos());
            Log.d(clase, "procesamientoIntentoConexion: Reintentos ---> " + retries);
            timeoutConexion = Integer.parseInt(tablaHost.getTiempoEsperaConexion()) * 1000;
            Log.d(clase, "procesamientoIntentoConexion: Tiempós Conexion ---> " + timeoutConexion);
        }
        try {
            System.out.println("INFORMACION RED SIZE------ " + posicionesIP.size());
            if (posicionesIP.size() > 0) {
                for (int i : posicionesIP) {
                    loadConfigIP(i);

                    for (int j = 0; j < retries; j++) {
                        if (showMsg) {

                            Thread.sleep(500);

                            tablaIp = ChequeoIPs.seleccioneIP(i);
                            transUI.handling(timeoutConexion, Tcode.Status.connecting_center, "CONECTANDO  [" + tablaIp.getIdIp() + "]" + " Intento " + "[" + (j + 1) + "]");
                        }
                        rta = connect();
                        if (rta == 0) {
                            return true;
                        }

                    }

                }
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }

        return false;
    }

    private boolean verificarConexionWifi(Context context) {
        if (context != null) {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (networkInfo != null && networkInfo.isConnected()) {
                // Si hay conexión a Internet en este momento
                wifiManager.setWifiEnabled(false);
                activarAlarma();
                if (!manager.getSimOperatorName().isEmpty()) {
                    int contador = 6;
                    while (!conectarDatos()) {
                        if (contador == 0) {
                            break;
                        }
                        conectarDatos();
                        contador--;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        e.printStackTrace();
                    }
                    if (conectarDatos()) {
                        transUI.toasTrans("Conexion datos exitosa", false, false);
                        return true;
                    } else {
                        transUI.toasTrans("Conexion datos fallida", false, true);
                    }

                } else {
                    transUI.toasTrans("Conexion datos fallida", false, false);

                }

            }
        }
        return false;

    }

    private boolean conectarDatos() {
        ConnectivityManager connManager2 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobile = connManager2.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mMobile.isConnected()) {
            return true;
        }
        return false;
    }

    private void activarAlarma() {
        SharedPreferences settings;
        settings = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        Calendar mcurrentTime = getInstance();
        long horaMilisecSistema = mcurrentTime.getTimeInMillis();
        int minutosPolaris = Integer.parseInt(tablaHost.getTiempoConexion3G());
        Long tiempoConexion = Long.valueOf(minutosPolaris * 60000);
        horaMilisecSistema = horaMilisecSistema + tiempoConexion;

        //SAVE ALARM TIME TO USE IT IN CASE OF REBOOT
        int alarmID = 1;
        SharedPreferences.Editor edit = settings.edit();
        edit.putInt("alarmID", alarmID);
        edit.putLong("alarmTime", horaMilisecSistema);
        edit.commit();
        ISOUtil.setAlarm(alarmID, horaMilisecSistema, context);
    }

    /**
     * sendRcvTrans: Abre socket, envia, recibe, cierra socket. Teniendo encuenta si debe
     * almacenar el reverso de la transaccion.
     * Se desempaqueta la trama recibida en objeto "iso8583"
     *
     * @param isReversal
     * @param reversalData
     * @return :
     * Exitoso
     * - Tcode.T_success
     * Errores
     * - Tcode.T_socket_err
     * - Tcode.T_send_err
     * - Errores relacionados a "unPacketISO8583"
     * Tcode.T_unknow_err, Tcode.T_package_mac_err, Tcode.T_package_illegal
     */
    protected int sendRcvTrans(boolean isReversal, TransLogData reversalData) {
        int rta;
        int rtn = Tcode.T_send_err;
        transUI.handling(timeout, Tcode.Status.connecting_center, TransEName);

        rta = retriesConnect(0, true, false);
        if (rta == Tcode.T_success) {
            if (isReversal) {
                Log.d(clase, "sendRcvTrans: Trans->sendRcvTrans->save Reversal ");
                Logger.debug("Trans->sendRcvTrans->save Reversal");
                Logger.logLine(LogType.FLUJO, clase, "sendRcvTrans - Trans->sendRcvTrans->save Reversal");
                TransLog.saveReversal(reversalData);
            }
            transUI.handling(timeout, Tcode.Status.send_data_2_server, TransEName);
            rta = send();
            if (rta == Tcode.T_success) {
                if (isTraceNoInc) {
                    cfg.incTraceNo();
                }
                transUI.handling(timeout, Tcode.Status.send_over_2_recv, TransEName);
                Logger.logLine(LogType.FLUJO, clase, "sendRcvTrans - Antes de recive- ");
                byte[] respData = recive();
                netWork.close();

                Logger.logLine(LogType.FLUJO, clase, "sendRcvTrans - Fuerda de netWork.close();");

                if (respData == null || respData.length <= 0) {
                    return Tcode.T_receive_err; // Analizar por fuera si se debe enviar reverso
                }
                rtn = iso8583.unPacketISO8583(respData);
                if (rtn != 0) {

                    Logger.logLine(LogType.FLUJO, clase, "sendRcvTrans ->unPacketISO8583 ERROR :" + rtn);

                    Logger.debug("Trans->sendRcvTrans->unPacketISO8583 ERROR :" + rtn);

                    if (rtn == Tcode.T_package_mac_err) {
                        if (isReversal) {
                            //Devuelva el mensaje de verificación de error de MAC, actualice la causa correcta A0
                            TransLogData newR = TransLog.getReversal();
                            newR.setRspCode("A0");
                            TransLog.clearReveral();
                            TransLog.saveReversal(newR);
                        }
                    }
                }
            } else {
                return Tcode.T_send_err;
            }
        } else {
            rtn = Tcode.T_socket_err;
        }
        return rtn;
    }

    /**
     * 将下载的CAPK和AID写入到内核中
     */
    protected void loadAIDCAPK2EMVUPI() {
        String aidFilePath = TMConfig.getRootFilePath() + EmvAidInfo.FILENAME;
        Logger.debug("load aid from path = " + aidFilePath);
        File aidFile = new File(aidFilePath);
        if (!aidFile.exists()) {
            Logger.debug("emv load aid file not found");
        }

        try {
            EmvAidInfo aidInfo = (EmvAidInfo) PAYUtils.file2Object(aidFilePath);
            if (aidInfo != null && aidInfo.getAidInfoList() != null) {
                for (byte[] item : aidInfo.getAidInfoList()) {
                    String temp = ISOUtil.byte2hex(item);
                    if (temp.contains("A0000003330")) {

                        item = ISOUtil.hex2byte(temp.substring(0, temp.indexOf("DF21") + 6) + "000010000000");

                        String aux = CommonFunctionalities.setTag(item, 0xDF20, 6, "999999999999");

                        item = aux != null && !aux.isEmpty() ? ISOUtil.hex2byte(aux) : item;
                    }

                    Logger.debug("load aid:" + ISOUtil.byte2hex(item));
                    byte[] aid = new byte[item.length - 1];
                    System.arraycopy(item, 1, aid, 0, aid.length);

                    int ret = pbocManager.setEmvParas(aid);
                    Log.d("TAG", "loadAIDCAPK2EMVUPI:  RET --: " + ret);
                }
            }
        } catch (Exception e) {
            Logger.error("Exception" + e.toString());
        }

        String capkFilePath = TMConfig.getRootFilePath() + EmvCapkInfo.FILENAME;
        Logger.debug("load capk from path = " + capkFilePath);
        File capkFile = new File(capkFilePath);
        if (!capkFile.exists()) {
            Logger.debug("emv load capk file not found");
        }

        try {
            EmvCapkInfo capk = (EmvCapkInfo) PAYUtils.file2Object(capkFilePath);
            if (capk != null && capk.getCapkList() != null) {
                for (byte[] item : capk.getCapkList()) {
                    Logger.debug("load capk:" + ISOUtil.byte2hex(item));
                    pbocManager.setEmvCapks(item);
                }
            }
        } catch (Exception e) {
            Logger.error("Exception" + e.toString());
        }
        try {
            PBOCTerConf pbocTerConf = new PBOCTerConf();
            // pbocTerConf.setTransCurrCode("0156"); // certificacion UPAY
            // pbocTerConf.setTransCurrCode("0840"); // produccion dolares
            pbocManager.setTerConf(pbocTerConf);

            IEMVHandler emvHandler = EMVHandler.getInstance();

            CoreParam coreParam = emvHandler.getCoreInitParameter();

            byte[] monedaPais = new byte[]{0x06, 0x00};

            coreParam.setTerminalCountryCode(monedaPais);
            coreParam.setTransactionCurrencyCode(monedaPais);

            int ret = emvHandler.setCoreInitParameter(coreParam);
            if (ret != 0) {
                Logger.debug("setCoreInitParameter error");
            }

        } catch (Exception e) {
            Logger.error("Exception" + e.toString());
        }

    }


    /**
     * 交易类型定义
     */
    public interface Type {
        String LOGOUT = "LOGOUT";
        String EC_ENQUIRY = "EC_ENQUIRY";
        String QUICKPASS = "QUICKPASS";
        String SETTLE = "SETTLE";
        String REVERSAL = "REVERSAL";
        String ECHO_TEST = "ECHO_TEST";
        String ANULACION = "ANULACION";
        String FALLBACK = "FALLBACK";
        String AUTO_SETTLE = "AUTO_SETTLE";
        String SALE_CTL = "SALE_CTL";
        String DEFERRED = "DIFERIDO";
        String ALL = "ALL";
        String ECHO = "ECHO";
        String CONFIRMACION = "CONFIRMACION";
        String REIMPRESION = "REIMPRESION";
        String CASH_OVER = "CASH_OVER";
        String INYECCION = "INYECCION";
        String RECARGARP = "RECARGARP";
    }

}
