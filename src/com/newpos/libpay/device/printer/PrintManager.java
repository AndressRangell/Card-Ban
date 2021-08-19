package com.newpos.libpay.device.printer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.cobranzas.cajas.ApiJson;
import com.cobranzas.cajas.model.ModelImpresion;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.init_emv.CAPK_ROW;
import com.cobranzas.inicializacion.init_emv.EMVAPP_ROW;
import com.cobranzas.inicializacion.trans_init.trans.Tools;
import com.cobranzas.inicializacion.trans_init.trans.dbHelper;
import com.cobranzas.tools.UtilNetwork;
import com.cobranzas.transactions.DataAdicional.DataAdicional;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.presenter.TransUI;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.config.DevConfig;
import com.pos.device.printer.PrintCanvas;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;
import com.pos.device.printer.PrinterCallback;
import com.wposs.cobranzas.BuildConfig;
import com.wposs.cobranzas.R;

import org.jpos.stis.TLV_parsing;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import cn.desert.newpos.payui.master.MasterControl;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static cn.desert.newpos.payui.transrecord.HistoryTrans.ALL_F_REDEN;
import static com.cobranzas.actividades.MainActivity.modoCaja;
import static com.cobranzas.actividades.StartAppBANCARD.VERSION;
import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.cobranzas.actividades.StartAppBANCARD.tablaDevice;
import static com.cobranzas.inicializacion.trans_init.Init.NAME_DB;
import static com.newpos.libpay.presenter.TransUIImpl.getErrInfo;
import static com.newpos.libpay.trans.Trans.IVAAMOUNT;
import static com.newpos.libpay.trans.Trans.MODE_CTL;
import static com.newpos.libpay.trans.Trans.MODE_HANDLE;
import static com.newpos.libpay.trans.Trans.MODE_ICC;
import static com.newpos.libpay.trans.Trans.MODE_MAG;
import static com.newpos.libpay.trans.Trans.SERVICEAMOUNT;
import static com.newpos.libpay.trans.Trans.TIPAMOUNT;
import static com.newpos.libpay.trans.Trans.idLote;
import static com.newpos.libpay.trans.finace.FinanceTrans.LOCAL;
import static org.jpos.stis.Util.hex2byte;


/**
 * Created by zhouqiang on 2017/3/14.
 *
 * @author zhouqiang
 * 打印管理类
 */
public class PrintManager {

    static String clase = "PrintManager.java";
    private static PrintManager mInstance;
    private static TMConfig cfg;
    private static Context mContext;
    private static TransUI transUI;
    private final int S_SMALL = 15;
    private final int S_MEDIUM = 23;
    private final int S_BIG = 29;
    private final int MAX_CHAR_SMALL = 42;
    private final int MAX_CHAR_MEDIUM = 28;
    private final int MAX_CHAR_BIG = 22;
    int num = 0;
    boolean isPrinting = false;
    boolean isICC;
    boolean isNFC;
    boolean isFallback;
    private TransLogData dataTrans;
    private boolean BOLD_ON = true;
    private boolean BOLD_OFF = false;
    private Printer printer = null;
    private PrintTask printTask = null;
    private PackageInfo packageInfo;
    private String host_id;
    private String[] rspField57 = new String[17];
    private String[] identificadoresActivos = new String[25];
    private String TraceNo;


    //Reportes
    private long subTotalSubTotal = 0;
    private long ivaAmountSubTotal = 0;
    private long serviceAmountSubTotal = 0;
    private long tipAmountSubTotal = 0;
    private long montoFijoSubTotal = 0;

    private long totalTempAmount = 0;
    private long totalTempIva = 0;
    private long totalTempServiceAmount = 0;
    private long totalTempTipAmount = 0;
    private long totalTempMontoFijo = 0;

    private long granTotal = 0;
    private long granTotalIva = 0;
    private long granTotalService = 0;
    private long granTotalTip = 0;
    private long granTotalMontoFijo = 0;

    private long amount;
    private long subTotal;
    private long ivaAmount;
    private long serviceAmount;
    private long tipAmount;
    private long montoFijo = 0;

    private int contTransAcq = 0;
    private int contTotalTransAcq = 0;
    private int contTransEmisor = 0;

    private String nombreActualEmisor = "";
    private String fechaTransActual = "";
    private String nombreAdquirenteActual = "";
    private String MID_InterOper = "";
    private boolean soloUnCiclo = false;
    private String[] comercioImpreso;
    private int idxImpresionComercio = 0;
    private boolean omitir = false;

    private boolean printNameIssuer = false;
    private boolean printDateTransxIssuer = false;
    private boolean isCajas;
    private String pinOffline;


    private PrintManager() {
    }

    public static PrintManager getmInstance(Context c, TransUI tui) {
        mContext = c;
        transUI = tui;
        if (null == mInstance) {
            mInstance = new PrintManager();
        }
        cfg = TMConfig.getInstance();

        //tconf = TCONF.getSingletonInstance();
        return mInstance;
    }

    public void setPinOffline(String pinOffline) {
        this.pinOffline = pinOffline;
    }

    public void setHost_id(String host_id) {
        this.host_id = host_id;
    }

    private void totalVueltos(int contVueltos, long montoVueltos, Paint paint, PrintCanvas canvas) {
        if (contVueltos > 0) {
            setTextPrint(setTextColumn("TOTAL VUELTO        " + ISOUtil.padleft(String.valueOf(contVueltos), 4, '0') + " Gs.", PAYUtils.FormatPyg(String.valueOf(montoVueltos)), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            return;
        }
        setTextPrint(setTextColumn("TOTAL VUELTO        0000 Gs.", "0", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
    }

    /**
     * print
     *
     * @param data   dataTrans
     * @param isCopy isCopy
     * @return return
     */
    public int print(final TransLogData data, boolean isCopy, boolean duplicate) {
        int ret = -1;
        String typeTransVoid = null;
        this.printTask = new PrintTask();
        this.printTask.setGray(150);
        dataTrans = data;
        isICC = data.isICC();
        isNFC = data.isNFC();
        isFallback = data.isFallback();
        int sizeTransLog = -1;

        if (dataTrans.getTypeTransVoid() != null)
            typeTransVoid = dataTrans.getTypeTransVoid();

        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.error("Exception" + e.toString());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }

        sizeTransLog = TransLog.getInstance(idLote).getSize();

        if (sizeTransLog == 0) {
            ret = Tcode.T_print_no_log_err;
        } else {
            printer = Printer.getInstance();
            if (printer == null) {
                ret = Tcode.T_sdk_err;
            } else {
                    if (modoCaja && isCajas()) {
                        impresionCaja();
                    }
                    try {
                        ret = printSaleBancard(false, isCopy, duplicate);
                    } catch (Exception e) {
                        if (transUI != null)
                            transUI.toasTrans("Error print : " + e.getMessage(), false, true);
                        e.printStackTrace();
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    }
            }
        }
        return ret;
    }


    private void printHeaderVenta(PrintCanvas canvas, Paint paint) {
        String nombreComercio = checkNull(tablaComercios.sucursal.getDescripcion());
        String ciudadComerio = checkNull(tablaComercios.sucursal.getDireccionPrincipal());
        String codigoNegocio = checkNull(dataTrans.getCodigoDelNegocio());

        String date = checkNull(dataTrans.getLocalDate());
        String hora = checkNull(dataTrans.getLocalTime());

        String formatDate = PAYUtils.StrToDate(date, "yyyyMMdd", "dd/MM/yyyy");
        String formatHour = PAYUtils.StrToDate(hora, "HHmmss", "HH:mm:ss");

        String fecha;
        fecha = "F:" + formatDate + " H:" + formatHour;
        String aux = "C.N.: " + codigoNegocio;

        printLogoRedInfonet(paint, canvas);
        if (dataTrans.getTipoVenta() != null) {
        }
        /*setTextPrint(setCenterText(aux.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);*/
        setTextPrint(setTextColumn(aux.trim(), fecha, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
    }

    private void printSubHeader(PrintCanvas canvas, Paint paint, boolean duplicate) {
        String cardMode;
        String lecturaTarjeta = formatDetailsType(dataTrans);
        if (dataTrans.getRrn() != null) {
            setTextPrint(setCenterText("BOLETA: " + dataTrans.getRrn(), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        }
        if (duplicate) {
            setTextPrint(setCenterText("***** DUPLICADO *****", S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        }
        setTextPrint(setTextColumn(dataTrans.getTipoVenta() + " " + lecturaTarjeta, formatSerial(DevConfig.getSN()) + " " + Tools.getVersion(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        printDataCARDCHIP(paint, canvas);

        String aux1 = "";
        String aux2 = "";
        String auxPan = dataTrans.getPan();
        if (auxPan != null) {
            if (auxPan.length() >= 4) {
                aux1 = "T : *" + auxPan.substring((dataTrans.getPan().length() - 4));
            }
        } else if (dataTrans.getField02() != null) {
            aux1 = "T : *" + dataTrans.getField02().substring((dataTrans.getField02().length() - 4));
        }

        if (dataTrans.getAuthCode() != null) {
            aux2 += "C.AUT: " + dataTrans.getAuthCode();
            setTextPrint(setTextColumn(aux1, aux2, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        }
    }

    private String formatSerial(String serial) {
        int espacio = 5;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < serial.length(); i += espacio) {
            if (i + espacio >= serial.length()) {
                result.append(serial.substring(i));
            } else {
                result.append(serial.substring(i, i + espacio)).append("-");
            }
        }
        return result.toString();
    }

    public int printSaleBancard(boolean isRePrint, boolean isCopy, boolean duplicate) throws Exception {

        Logger.debug("PrintManager>>start>>printSaleBancard>>");

        this.printTask = new PrintTask();
        this.printTask.setGray(150);

        PrintCanvas canvas = new PrintCanvas();
        Paint paint = new Paint();

        PrintCanvas canvasDuplicado = new PrintCanvas();
        Paint paintDuplicado = new Paint();

        printHeaderVenta(canvas, paint);
        printSubHeader(canvas, paint, duplicate);

        printHeaderVenta(canvasDuplicado, paintDuplicado);
        printSubHeader(canvasDuplicado, paintDuplicado, true);


        if (dataTrans.getTipoVenta() != null) {
                validaImprimirVuelto(canvas, paint);
                validaImprimirVuelto(canvasDuplicado, paintDuplicado);
            } else {
                if (dataTrans.getAmount() != null) {
                    setTextPrint(setTextColumn("MONTO:", "G. " + formatAmoun(dataTrans.getAmount()), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
                    setTextPrint(setTextColumn("MONTO:", "G. " + formatAmoun(dataTrans.getAmount()), S_MEDIUM), paintDuplicado, BOLD_ON, canvasDuplicado, S_MEDIUM);
                }
        }

        String cajaNro = ISOUtil.padleft(checkNull(tablaDevice.getNumeroCajas()), 4, '0');
        String batch = ISOUtil.padleft(checkNull(dataTrans.getBatchNo()), 4, '0');
        String cargo = ISOUtil.padleft(checkNull(dataTrans.getNroCargo()), 6, '0');
        String aux = setTextColumn("Caja Nro: " + cajaNro + "  Lote: " + batch, "Cargo: " + cargo, S_SMALL);
        setTextPrint(aux, paint, BOLD_ON, canvas, S_SMALL);
        setTextPrint(aux, paintDuplicado, BOLD_ON, canvasDuplicado, S_SMALL);
        printStringLargue("ACEPTO PAGAR EL MONTO DE ESTA OPERACION", S_SMALL, BOLD_ON, true, paint, canvas);
        printStringLargue("ACEPTO PAGAR EL MONTO DE ESTA OPERACION", S_SMALL, BOLD_ON, true, paintDuplicado, canvasDuplicado);

        /* Informacion local de resumen de cada transaccion*/
        if (dataTrans.getTipoVenta() != null) {
            String infLocal;
            String field14 = DataAdicional.getField(14);
            String field45 = DataAdicional.getField(45);
            if (field14 != null && field45 != null) {
                //stringToAscii
                infLocal = "PLAN " + ISOUtil.hex2AsciiStr(field14) + " P/PAGAR EN " + ISOUtil.hex2AsciiStr(field45).replaceFirst("^0", "") + " CUOTAS";
                setTextPrint(infLocal, paint, BOLD_ON, canvas, S_SMALL);
                setTextPrint(infLocal, paintDuplicado, BOLD_ON, canvasDuplicado, S_SMALL);
            }
        }


        if (isCopy) {
            if (dataTrans.getAdditionalAmount() != null) {
                setTextPrint("Disponible Gs:" + PAYUtils.FormatPyg(dataTrans.getAdditionalAmount()), paint, BOLD_ON, canvas, S_SMALL);
                setTextPrint("Disponible Gs:" + PAYUtils.FormatPyg(dataTrans.getAdditionalAmount()), paintDuplicado, BOLD_ON, canvasDuplicado, S_SMALL);
            }
            printPuntosBancard(paint, canvas);
            printPuntosBancard(paintDuplicado, canvasDuplicado);
            if (dataTrans.getTipoVenta() != null) {
                if (dataTrans.isPinExist() ||
                        (pinOffline != null && pinOffline.equals("PIN OFFLINE"))) {
                    setTextPrint(setCenterText("TRANSACCION AUTENTICADA POR PIN", S_SMALL), paint, BOLD_OFF, canvas, S_SMALL);
                    setTextPrint(setCenterText("TRANSACCION AUTENTICADA POR PIN", S_SMALL), paintDuplicado, BOLD_OFF, canvasDuplicado, S_SMALL);

                    setPinOffline(null);
                } else {
                    if (dataTrans.isNFC()) {
                        setTextPrint(setCenterText("NO REQUIERE PIN NI FIRMA", S_SMALL), paint, BOLD_OFF, canvas, S_SMALL);
                        setTextPrint(setCenterText("NO REQUIERE PIN NI FIRMA", S_SMALL), paintDuplicado, BOLD_OFF, canvasDuplicado, S_SMALL);
                    }
                }
            }
            setTextPrint(setCenterText("<< Copia Cliente >>", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            setTextPrint(setCenterText("<< Copia Cliente >>", S_SMALL), paintDuplicado, BOLD_ON, canvasDuplicado, S_SMALL);
        } else {
            if (dataTrans.getTipoVenta() != null) {
                if (!(dataTrans.getTipoVenta().equals(DefinesBANCARD.ITEM_SIN_TARJETA_CUENTA_ST))) {
                    if (!PAYUtils.isNullWithTrim(dataTrans.getPin()) ||
                            (pinOffline != null && pinOffline.equals("PIN OFFLINE"))) {
                        setTextPrint(setCenterText("TRANSACCION AUTENTICADA POR PIN", S_SMALL), paint, BOLD_OFF, canvas, S_SMALL);
                        setTextPrint(setCenterText("TRANSACCION AUTENTICADA POR PIN", S_SMALL), paintDuplicado, BOLD_OFF, canvasDuplicado, S_SMALL);
                        setPinOffline(null);

                    } else {
                        if (dataTrans.isNFC()) {
                            setTextPrint(setCenterText("NO REQUIERE PIN NI FIRMA", S_SMALL), paint, BOLD_OFF, canvas, S_SMALL);
                            setTextPrint(setCenterText("NO REQUIERE PIN NI FIRMA", S_SMALL), paintDuplicado, BOLD_OFF, canvasDuplicado, S_SMALL);
                        } else {
                            println(paint, canvas); //SE IMPRIME LINEA EN BLANCO
                            //NOMBRE DEL TITULAR DE LA TARJETA Y CAMPO DE FIRMA
                            printSignatureBancard(checkNull(dataTrans.getNameCard()), paint, canvas, false);
                            printSignatureBancard(checkNull(dataTrans.getNameCard()), paintDuplicado, canvasDuplicado, false);
                        }
                    }
                }
            }

            setTextPrint(setCenterText("<< Copia Comercio >>", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            setTextPrint(setCenterText("<< Copia Comercio >>", S_SMALL), paintDuplicado, BOLD_ON, canvasDuplicado, S_SMALL);
        }
        validaMensajeHost_Sub29(canvas, paint);
        validaMensajeHost_Sub29(canvasDuplicado, paintDuplicado);
        int ret = printData(canvas, dataTrans.getTransEName());

        if (printer != null) {
            printer = null;
        }


        return ret;
    }


    private void validaMensajeHost_Sub29(PrintCanvas canvas, Paint paint) {
        String field29 = DataAdicional.getField(29);
        if (field29 != null) {
            if (!field29.isEmpty()) {
                if (field29.length() >= 40) {
                    String data1 = field29.substring(0, 40);
                    String data2 = field29.substring(40);
                    setTextPrint(setCenterText(data1, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
                    setTextPrint(setCenterText(data2, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
                } else {
                    setTextPrint(setCenterText(field29, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
                }

            }
        }
    }

    private void validaImprimirVuelto(PrintCanvas canvas, Paint paint) {
        String field41 = DataAdicional.getField(41);
        if (field41 != null) {
            setTextPrint(setTextColumn("MONTO:", "G. " + formatAmoun(dataTrans.getAmount() - Integer.parseInt(ISOUtil.hex2AsciiStr(field41))), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
            String vuelto = "00";
            vuelto = formatAmoun(Integer.parseInt(ISOUtil.hex2AsciiStr(field41)));
            setTextPrint(setTextColumn("VUELTO:", "G. " + vuelto, S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
            setTextPrint(setTextColumn("TOTAL:", "G. " + formatAmoun(dataTrans.getAmount()), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        } else {
            if (dataTrans.getAmount() != null) {
                setTextPrint(setTextColumn("MONTO:", "G. " + formatAmoun(dataTrans.getAmount()), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
            }
        }
    }

    private String InputType(String entryMode) {
        String ret;
        switch (entryMode) {
            case "051":
            case "071":
                ret = "(C)";
                break;
            case "901":
                ret = "(B)";
                break;
            default:
                ret = "";
                break;
        }

        return ret;
    }


    private void printSignatureBancard(String nameCard, Paint paint, PrintCanvas canvas, boolean isCopy) {
        boolean isHexa;
        if (!isCopy) {
            setTextPrint("FIRMA X........................", paint, BOLD_OFF, canvas, S_MEDIUM);
            if (!nameCard.equals("----")) {//Con esto evitamos imprimir la cadena "---" que se agrega cuando el labelCard es null
                if (nameCard.length() > 0) {
                    isHexa = nameCard.matches("^[0-9a-fA-F]+$");                   //validacion de variable labelCard para evitar conversion
                    if (!isHexa) {
                        nameCard = ISOUtil.convertStringToHex(nameCard);
                    }
                    setTextPrint(setCenterText(checkNumCharacters(ISOUtil.hex2AsciiStr(nameCard.trim()), S_SMALL), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
                }
            }
        }
    }

    public void imprimirReporteDetallado(List<TransLogData> list) {
        int ret = -1;
        this.printTask = new PrintTask();
        this.printTask.setGray(150);

        PrintCanvas canvas = new PrintCanvas();
        Paint paint = new Paint();

        printLogoRedInfonet(paint, canvas);

        String nombreComercio = checkNull(tablaComercios.sucursal.getDescripcion());
        String ciudadComerio = checkNull(tablaComercios.sucursal.getDireccionPrincipal());
        setTextPrint(setCenterText(nombreComercio.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        setTextPrint(setCenterText(ciudadComerio.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        setTextPrint(setCenterText("REPORTE DETALLADO", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        setTextPrint("FECHA             HORA            TERMINAL", paint, BOLD_ON, canvas, S_SMALL);
        setTextPrint(setTextColumn(PAYUtils.getLocalDate2() + "        " + PAYUtils.getLocalTime2(), ISOUtil.padleft(tablaDevice.getNumeroCajas(), 4, '0'), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        setTextPrint("Nro de lote: " + TMConfig.getInstance().getBatchNo() + " Comercio: " + tablaComercios.sucursal.getMerchantId(), paint, BOLD_ON, canvas, S_SMALL);

        String fechaActual = PAYUtils.getLocalDateFormat("dd/MM/yyyy HH:mm");

        setTextPrint("Hasta: " + fechaActual, paint, BOLD_ON, canvas, S_SMALL);

        println(paint, canvas);
        setTextPrint("REF        NRO TARJETA          MONTO TIPO", paint, BOLD_ON, canvas, S_SMALL);

        int contador = 0;

        for (TransLogData data : list) {
            String cargo = data.getNroCargo().substring(2);
            String card = "****";
            if (data.getPan() != null) {
                card += data.getPan().substring(data.getPan().length() - 4);
            }
            String monto = String.valueOf(data.getAmount());
            monto = monto.substring(0, monto.length() - 2);
            monto = PAYUtils.formatMontoGs(monto);
            if (monto.equals("G. nu")) {
                monto = "G. 0";
            }

            setTextPrint(setTextColumn(cargo + "       " + card, monto + "   " + data.getTipoTarjeta(), S_SMALL), paint, BOLD_OFF, canvas, S_SMALL);

            contador++;
        }

        setTextPrint("------------------------------------------", paint, BOLD_ON, canvas, S_SMALL);

        setTextPrint("CANTIDAD DE TRANSACCIONES:   " + contador, paint, BOLD_ON, canvas, S_SMALL);


        println(paint, canvas);
        println(paint, canvas);

        ret = printData(canvas, "");

        if (printer != null) {
            printer = null;
        }
    }

    public void selectPrintReport(String typeReport) {
        switch (typeReport) {
            case ALL_F_REDEN:
                printReport(false);
                break;
            default:
                printReport(true);
                break;
        }
    }

    public int printReport(boolean title) {

        Logger.debug("PrintManager>>start>>printReport>>");
        String address;
        String phone;
        String tmp;
        this.printTask = new PrintTask();
        this.printTask.setGray(150);
        int ret = -1;

        if (TransLog.getInstance(idLote).getSize() == 0) {
            ret = Tcode.T_print_no_log_err;
        } else {

            printer = Printer.getInstance();
            if (printer == null) {
                ret = Tcode.T_sdk_err;
            } else {

                PrintCanvas canvas = new PrintCanvas();
                Paint paint = new Paint();

                printDateAndTime(getFormatDateAndTime(checkNull(PAYUtils.getMonth() + " " + PAYUtils.getDay() + "," + PAYUtils.getYear()), checkNull(PAYUtils.getHMS())), S_MEDIUM, BOLD_OFF, paint, canvas);

                println(paint, canvas);
                println(paint, canvas);

                if (title)
                    setTextPrintREV(setCenterText("REPORTE DEPOSITO", S_BIG), paint, BOLD_OFF, canvas, S_BIG);

                println(paint, canvas);

                setTextPrint(setCenterText("LOTE: " + checkNull(dataTrans.getBatchNo()), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);

                println(paint, canvas);
                println(paint, canvas);

                printLine(paint, canvas);


                println(paint, canvas);

                setTextPrint(setCenterText("REF.      TARJETA       MONTO  ", S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
                println(paint, canvas);

                printAllData(paint, canvas, idLote);

                printLine(paint, canvas);

                setTextPrint(setCenterText("FIN  DE  REPORTE", S_BIG), paint, BOLD_OFF, canvas, S_BIG);

                println(paint, canvas);
                println(paint, canvas);
                println(paint, canvas);

                ret = printData(canvas, "");

                if (printer != null) {
                    printer = null;
                }
            }
        }

        return ret;
    }

    public int printTransreject(String value1, String value2, int rerval) {

        Logger.debug("PrintManager>>start>>printTransreject>>");
        String lote;
        String term;
        String idCom;
        this.printTask = new PrintTask();
        this.printTask.setGray(150);
        int ret = -1;

        printer = Printer.getInstance();
        if (printer == null) {
            ret = Tcode.T_sdk_err;
        } else {

            PrintCanvas canvas = new PrintCanvas();
            Paint paint = new Paint();

            lote = TMConfig.getInstance().getBatchNo();
            term = tablaDevice.getNumeroCajas();
            idCom = tablaComercios.sucursal.getCardAccpMerch();

            printSecondHeader(checkNull(lote), checkNull(term), checkNull(idCom), paint, canvas);

            setTextPrint(setCenterText(checkNull(PAYUtils.getSecurityNum(value1, 6, 3)).trim(), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
            println(paint, canvas);

            setTextPrint(setCenterText("REF : " + checkNull(value2).trim(), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
            println(paint, canvas);

            printDateAndTime(getFormatDateAndTime(checkNull(PAYUtils.getMonth() + " " + PAYUtils.getDay() + "," + PAYUtils.getYear()), checkNull(PAYUtils.getHMS())), S_MEDIUM, BOLD_OFF, paint, canvas);
            println(paint, canvas);

            String msg = getErrInfo(String.valueOf(rerval));
            setTextPrint(setCenterText(checkNull(msg).trim(), S_BIG), paint, BOLD_ON, canvas, S_MEDIUM);

            println(paint, canvas);
            println(paint, canvas);
            println(paint, canvas);

            ret = printData(canvas, "");

            if (printer != null) {
                printer = null;
            }
        }

        return ret;
    }

    public int printEMVAppCfg() {

        Logger.debug("PrintManager>>start>>printReportEmv>>");

        EMVAPP_ROW emvappRow = EMVAPP_ROW.getSingletonInstance();
        CAPK_ROW capkRow = CAPK_ROW.getSingletonInstance();

        this.printTask = new PrintTask();
        this.printTask.setGray(150);
        int ret = -1;

        printer = Printer.getInstance();
        if (printer == null) {
            ret = Tcode.T_sdk_err;
        } else {

            PrintCanvas canvas = new PrintCanvas();
            Paint paint = new Paint();

            /*setTextPrint(setCenterText(ISOUtil.hex2AsciiStr(termCfg.getSb_dflt_name()), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
            setTextPrint(setCenterText(ISOUtil.hex2AsciiStr(termCfg.getSb_name_loc().substring(0, 46)), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
            setTextPrint(setCenterText(ISOUtil.hex2AsciiStr(termCfg.getSb_name_loc().substring(46)), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);*/

            println(paint, canvas);
            println(paint, canvas);
            println(paint, canvas);
            println(paint, canvas);

            getEMVAPP_ROW(emvappRow, paint, canvas);

            println(paint, canvas);
            println(paint, canvas);

            setTextPrint("EMV KEY INFO", paint, BOLD_ON, canvas, S_MEDIUM);
            println(paint, canvas);

            setTextPrint("Public Key ID      Eff     Exp", paint, BOLD_ON, canvas, S_MEDIUM);
            getCAPK_ROW(capkRow, paint, canvas);

            ret = printData(canvas, "");

            if (printer != null) {
                printer = null;
            }
        }

        return ret;
    }

    public int printConfigTerminal() {

        Logger.debug("PrintManager>>start>>printConfigTerminal>>");

        String term;
        this.printTask = new PrintTask();
        this.printTask.setGray(150);
        int ret = -1;

        printer = Printer.getInstance();
        if (printer == null) {
            ret = Tcode.T_sdk_err;
        } else {

            PrintCanvas canvas = new PrintCanvas();
            Paint paint = new Paint();

            setTextPrint(setCenterText("CONFIGURACION TERMINAL", S_BIG), paint, BOLD_ON, canvas, S_BIG);
            println(paint, canvas);

            term = tablaDevice.getNumeroCajas();
            setTextPrint(checkNumCharacters("TERMINAL: " + checkNull(term), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
            printDateAndTime(getFormatDateAndTime(checkNull(PAYUtils.getMonth() + " " + PAYUtils.getDay() + "," + PAYUtils.getYear()), checkNull(PAYUtils.getHMS())), S_MEDIUM, BOLD_OFF, paint, canvas);
            printLine("=", paint, canvas);

            setTextPrint(setCenterText("LISTA DE APLICACIONES", S_BIG), paint, BOLD_ON, canvas, S_BIG);
            printLine("=", paint, canvas);
            println(paint, canvas);

            setTextPrint(setTextColumn("APPLICATION_ID", "VERSION_NAME", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            setTextPrint(setTextColumn(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            //Obtenido del documento EMVCo Letter of Approval - Contact Terminal Level 2 - October 27, 2017
            setTextPrint(setTextColumn("libemv.so Version 1.0.9", "AFD80709", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            println(paint, canvas);

            printLine("=", paint, canvas);

            setTextPrint(setCenterText("IP NETWORK ", S_BIG), paint, BOLD_ON, canvas, S_BIG);
            printLine("=", paint, canvas);
            println(paint, canvas);

            // test functions
            setTextPrint(checkNumCharacters("MAC Address: " + checkNull(UtilNetwork.getMACAddress("wlan0")), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
            setTextPrint(checkNumCharacters("IP Address: " + checkNull(UtilNetwork.getIPAddress(true)), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
            println(paint, canvas);

            setTextPrint(setCenterText("FIN  DE  REPORTE", S_BIG), paint, BOLD_ON, canvas, S_BIG);

            println(paint, canvas);
            println(paint, canvas);
            println(paint, canvas);
            println(paint, canvas);


            ret = printData(canvas, "");

            if (printer != null) {
                printer = null;
            }
        }

        return ret;
    }

    /*******
     Tools print
     *******/
    private boolean getCAPK_ROW(CAPK_ROW capkRow, Paint paint, PrintCanvas canvas) {
        boolean ok = false;
        String eff;
        String exp;
        String tmp;
        dbHelper databaseAccess = new dbHelper(mContext, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        int counter = 1;
        for (String s : CAPK_ROW.fields) {
            sql.append(s);
            if (counter++ < CAPK_ROW.fields.length) {
                sql.append(",");
            }
        }
        sql.append(" from capks");
        sql.append(";");

        try {

            Cursor cursor = databaseAccess.rawQuery(sql.toString());
            cursor.moveToFirst();
            int indexColumn;
            while (!cursor.isAfterLast()) {
                capkRow.clearCAPK_ROW();
                indexColumn = 0;
                for (String s : CAPK_ROW.fields) {
                    capkRow.setCAPK_ROW(s, cursor.getString(indexColumn++));
                }

                //Effect date // PREGUNTARLE A FER
              /*  tmp = capkRow.getEffectDate();
                eff = tmp.substring(4, 6);
                eff += "/";
                eff += tmp.substring(2, 4);*/

                //Exp date
                tmp = capkRow.getKeyExpirationDate();
                exp = tmp.substring(4, 6);
                exp += "/";
                exp += tmp.substring(2, 4);

                setTextPrint(setTextColumn(capkRow.getKeyRid() + "-" + capkRow.getKeyId(), "eff" + "  " + exp, S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                /*Log.d("Capks", "***********");
                Log.d("RID", capkRow.getRID());
                Log.d("ID", String.valueOf(Integer.parseInt(capkRow.getKeyIdx(), 16)));
                Log.d("Eff", capkRow.getEffectDate());
                Log.d("Exp", capkRow.getExpiryDate());
                Log.d("Capks", "***********");*/

                ok = true;
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            System.out.println(e.getMessage());
        }
        databaseAccess.closeDb();
        return ok;
    }

    private boolean getEMVAPP_ROW(EMVAPP_ROW emvappRow, Paint paint, PrintCanvas canvas) {
        boolean ok = false;
        int aux;
        String tmp;
        long flr;
        dbHelper databaseAccess = new dbHelper(mContext, NAME_DB, null, 1);
        databaseAccess.openDb(NAME_DB);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        int counter = 1;
        for (String s : EMVAPP_ROW.fields) {
            sql.append(s);
            if (counter++ < EMVAPP_ROW.fields.length) {
                sql.append(",");
            }
        }
        sql.append(" from emvapps");
        sql.append(";");

        try {

            Cursor cursor = databaseAccess.rawQuery(sql.toString());
            cursor.moveToFirst();
            int indexColumn;

            while (!cursor.isAfterLast()) {
                emvappRow.clearEMVAPP_ROW();
                indexColumn = 0;
                for (String s : EMVAPP_ROW.fields) {
                    emvappRow.setEMVAPP_ROW(s, cursor.getString(indexColumn++));
                }

                setTextPrint("EMV APP CFG", paint, BOLD_ON, canvas, S_MEDIUM);
                println(paint, canvas);
                TLV_parsing tlvParsing = new TLV_parsing(emvappRow.geteACFG());
                setTextPrint(setTextColumn("AID: ", ISOUtil.bcd2str(tlvParsing.getValueB(0x9f06), 0, tlvParsing.getValueB(0x9f06).length), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                byte[] output = hex2byte(emvappRow.geteBitField());

                //AQC profile
                aux = (output[0] & 0x80);
                tmp = (aux == 0) ? "Y" : "N";
                setTextPrint(setTextColumn("Def ACQ Profile: ", tmp, S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);


                setTextPrint(setTextColumn("Type: ", (emvappRow.geteType()), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                setTextPrint(setTextColumn("Ver: ", ISOUtil.bcd2str(tlvParsing.getValueB(0x9f09), 0, tlvParsing.getValueB(0x9f09).length), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                if (tlvParsing.getValueB(0x9f1b) != null) {
                    flr = Long.parseLong(ISOUtil.bcd2str(tlvParsing.getValueB(0x9f1b), 0, tlvParsing.getValueB(0x9f1b).length));
                    setTextPrint(setTextColumn("FLR LIMIT: ", PAYUtils.getStrAmount(flr), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                }

                if (tlvParsing.getValueB(0xdf7f) != null) {
                    flr = Long.parseLong(ISOUtil.bcd2str(tlvParsing.getValueB(0xdf7f), 0, tlvParsing.getValueB(0xdf7f).length));
                    setTextPrint(setTextColumn("FLR LIMIT(0): ", PAYUtils.getStrAmount(flr), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                }

                setTextPrint(setTextColumn("Basic Random: ", emvappRow.geteRSBThresh(), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                setTextPrint(setTextColumn("Target %: ", emvappRow.geteRSTarget() + "-" + emvappRow.geteRSBMax(), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                setTextPrint(setTextColumn("Country: ", ISOUtil.bcd2str(tlvParsing.getValueB(0x9f1a), 0, tlvParsing.getValueB(0x9f1a).length), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                setTextPrint(setTextColumn("Currency: ", ISOUtil.bcd2str(tlvParsing.getValueB(0x5f2a), 0, tlvParsing.getValueB(0x5f2a).length), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                aux = (output[0] & 0x01);
                tmp = (aux == 0) ? "Y" : "N";
                setTextPrint(setTextColumn("Allow Partial AID: ", tmp, S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                aux = (output[0] & 0x02);
                tmp = (aux == 0) ? "Y" : "N";
                setTextPrint(setTextColumn("Referral Enable: ", tmp, S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                aux = (output[0] & 0x04);
                tmp = (aux == 0) ? "Y" : "N";
                setTextPrint(setTextColumn("PIN Bypass Enable: ", tmp, S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                aux = (output[0] & 0x08);
                tmp = (aux == 0) ? "Y" : "N";
                setTextPrint(setTextColumn("Force TRM: ", tmp, S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                setTextPrint(setTextColumn("TAC Denial: ", (emvappRow.geteTACDenial()), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                setTextPrint(setTextColumn("TAC Online: ", (emvappRow.geteTACOnline()), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                setTextPrint(setTextColumn("TAC Default: ", (emvappRow.geteTACDefault()), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                setTextPrint(setTextColumn("Term Cap: ", ISOUtil.bcd2str(tlvParsing.getValueB(0x9f33), 0, tlvParsing.getValueB(0x9f33).length), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                setTextPrint(setTextColumn("Add Cap: ", ISOUtil.bcd2str(tlvParsing.getValueB(0x9f40), 0, tlvParsing.getValueB(0x9f40).length), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

                println(paint, canvas);
                println(paint, canvas);

                ok = true;
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            System.out.println(e.getMessage());
        }
        databaseAccess.closeDb();
        return ok;
    }

    private void printAllData(Paint paint, PrintCanvas canvas, String batch) {
        List<TransLogData> list = new ArrayList<>(TransLog.getInstance(batch).getData());

        if (list != null) {
            List<TransLogData> listFinal = new ArrayList<>(orderList(list));
            List<TransLogData> listDetalle = new ArrayList<>(listFinal);

            Collections.sort(listDetalle, new Comparator<TransLogData>() {
                @Override
                public int compare(TransLogData transLogData, TransLogData t1) {
                    return transLogData.getTraceNo().compareTo(t1.getTraceNo());
                }
            });
            //Collections.reverse(listDetalle);

            try {

                limpiarVar();

                limpiarComerciosImpresos(listFinal);
                //List de comercios
                for (TransLogData translogAllAdquirer : listFinal) {

                    if (soloUnCiclo)
                        break;

                }

                imprimirGranTotal(paint, canvas);

            } catch (ArrayIndexOutOfBoundsException exception) {
                Logger.logLine(LogType.EXCEPTION, clase, exception.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, exception.getStackTrace());
            }
        }
    }

    private void limpiarComerciosImpresos(List<TransLogData> listFinal) {
        comercioImpreso = new String[listFinal.size()];
        for (int i = 0; i < comercioImpreso.length; i++) {
            comercioImpreso[i] = "-";
        }
    }

    private void limpiarVar() {
        contTransAcq = 0;
        contTotalTransAcq = 0;
        contTransEmisor = 0;

        subTotalSubTotal = 0;
        ivaAmountSubTotal = 0;
        serviceAmountSubTotal = 0;
        tipAmountSubTotal = 0;
        montoFijoSubTotal = 0;

        totalTempAmount = 0;
        totalTempIva = 0;
        totalTempServiceAmount = 0;
        totalTempTipAmount = 0;
        totalTempMontoFijo = 0;

        granTotal = 0;
        granTotalIva = 0;
        granTotalService = 0;
        granTotalTip = 0;
        granTotalMontoFijo = 0;

        montoFijo = 0;

        nombreActualEmisor = "";
        fechaTransActual = "";
        nombreAdquirenteActual = "";
        soloUnCiclo = false;
        idxImpresionComercio = 0;
        omitir = false;


    }

    private void imprimirGranTotal(Paint paint, PrintCanvas canvas) {
        println(paint, canvas);
        println(paint, canvas);
        setTextPrintREV("Total Generalizado", paint, BOLD_ON, canvas, S_BIG);
        println(paint, canvas);
        setTextPrint("------------------------------------------", paint, BOLD_OFF, canvas, S_SMALL);
        setTextPrint(setTextColumn("GRAN TOTAL:          " + contTotalTransAcq, "$ " + PAYUtils.getStrAmount(granTotal) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        /*if (GetAmount.checkIVA())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_IMPUESTO()) + ":", "$ " + PAYUtils.getStrAmount(granTotalIva) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        if (GetAmount.checkService())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_SERVICIO()) + ":", "$ " + PAYUtils.getStrAmount(granTotalService) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        if (GetAmount.checkTip())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_PROPINA()) + ":", "$ " + PAYUtils.getStrAmount(granTotalTip) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        if (tconf.getHABILITA_MONTO_FIJO() != null) {
            //if (granTotalMontoFijo != 0) {
            if (ISOUtil.stringToBoolean(tconf.getHABILITA_MONTO_FIJO())) {
                setTextPrint(setTextColumn("TARIFA: ", "$ " + PAYUtils.getStrAmount(granTotalMontoFijo) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            }
        }*/

        setTextPrint(setTextColumn("T.Neto:" + "", "$ " + PAYUtils.getStrAmount(granTotal + granTotalIva + granTotalService + granTotalTip) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        println(paint, canvas);
        setTextPrint("==========================================", paint, BOLD_OFF, canvas, S_SMALL);
    }

    private void imprimirTotalComercio(Paint paint, PrintCanvas canvas, long totalComercio, long totalIvaComercio, long totalServiceComercio, long totalTipComercio) {
        println(paint, canvas);
        println(paint, canvas);
        setTextPrint(setTextColumn("VENTA:          " + contTotalTransAcq, "$ " + PAYUtils.getStrAmount(totalComercio) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        /*if (GetAmount.checkIVA())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_IMPUESTO()) + "", "$ " + PAYUtils.getStrAmount(totalIvaComercio) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        if (GetAmount.checkService())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_SERVICIO()) + "", "$ " + PAYUtils.getStrAmount(totalServiceComercio) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        if (GetAmount.checkTip())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_PROPINA()) + "", "$ " + PAYUtils.getStrAmount(totalTipComercio) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        setTextPrint(setTextColumn("TOTAL:" + "", "$ " + PAYUtils.getStrAmount(totalComercio+totalIvaComercio+totalServiceComercio+totalTipComercio) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        if (tconf.getHABILITA_MONTO_FIJO() != null) {
            //if (granTotalMontoFijo != 0) {
            if (ISOUtil.stringToBoolean(tconf.getHABILITA_MONTO_FIJO())) {
                setTextPrint(setTextColumn("TARIFA: ", "$ " + PAYUtils.getStrAmount(granTotalMontoFijo) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            }
        }*/
        println(paint, canvas);
        setTextPrint("==========================================", paint, BOLD_OFF, canvas, S_SMALL);
    }

    private void imprimirSubTotal(TransLogData translogByAdquirer, Paint paint, PrintCanvas canvas) {
        println(paint, canvas);
        if (contTransEmisor != 0) {
            setTextPrint(setTextColumn("", "---------------", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            setTextPrint(setTextColumn("SUB TOTAL:          " + contTransEmisor, "$ " + PAYUtils.getStrAmount(subTotal) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
           /* if (GetAmount.checkIVA())
                setTextPrint(setTextColumn(checkNull(tconf.getLABEL_IMPUESTO()) + ":", "$ " +
                        PAYUtils.getStrAmount(ivaAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

            if (GetAmount.checkService())
                setTextPrint(setTextColumn( checkNull(tconf.getLABEL_SERVICIO()) + ":", "$ " +
                        PAYUtils.getStrAmount(serviceAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

            if (GetAmount.checkTip())
                setTextPrint(setTextColumn(checkNull(tconf.getLABEL_PROPINA()) + ":", "$ " +
                        PAYUtils.getStrAmount(tipAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);*/

            if (translogByAdquirer.getTipoMontoFijo() != null) {
                //if (translogByAdquirer.getTipoMontoFijo().equals(AUTOMATICO)) {
                //if (translogByAdquirer.getMontoFijo() != 0) {
                setTextPrint(setTextColumn("TARIFA: ", "$ " + PAYUtils.getStrAmount(montoFijo) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
                //}
                //}
            }
        }
        println(paint, canvas);
    }

    /*private void imprimirTotal(TransLogData translogByAdquirer, Paint paint, PrintCanvas canvas) {

        setTextPrint(setTextColumn("", "===============", S_SMALL), paint, BOLD_OFF, canvas, S_SMALL);

        setTextPrint(setTextColumn("VALOR TOTAL:        " + contTransAcq, "$ " + PAYUtils.getStrAmount(totalTempAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        if (GetAmount.checkIVA())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_IMPUESTO()) + ":", "$ " + PAYUtils.getStrAmount(totalTempIva) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        if (GetAmount.checkService())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_SERVICIO()) + ":", "$ " + PAYUtils.getStrAmount(totalTempServiceAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        if (GetAmount.checkTip())
            setTextPrint(setTextColumn(checkNull(tconf.getLABEL_PROPINA()) + ":", "$ " + PAYUtils.getStrAmount(totalTempTipAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        if (translogByAdquirer.getTipoMontoFijo() != null) {
            //if (translogByAdquirer.getTipoMontoFijo().equals(AUTOMATICO)) {
            //if (translogByAdquirer.getMontoFijo() != 0) {
            setTextPrint(setTextColumn("TARIFA: ", "$ " + PAYUtils.getStrAmount(totalTempMontoFijo) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            //}
            //}
        }
    }*/

    /*private void imprimirTotalxComercio(TransLogData translogByAdquirer, Paint paint, PrintCanvas canvas) {

        if (translogByAdquirer.isMulticomercio()) {
            println(paint, canvas);
            setTextPrint(setTextColumn("", "===============", S_SMALL), paint, BOLD_OFF, canvas, S_SMALL);
            setTextPrint(setTextColumn("TOTAL COMERCIO:     " + contTransAcq, "$ " + PAYUtils.getStrAmount(totalTempAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

            if (GetAmount.checkIVA())
                setTextPrint(setTextColumn(checkNull(tconf.getLABEL_IMPUESTO()) + ":", "$ " + PAYUtils.getStrAmount(totalTempIva) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            if (GetAmount.checkService())
                setTextPrint(setTextColumn(checkNull(tconf.getLABEL_SERVICIO()) + ":", "$ " + PAYUtils.getStrAmount(totalTempServiceAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            if (GetAmount.checkTip())
                setTextPrint(setTextColumn(checkNull(tconf.getLABEL_PROPINA()) + ":", "$ " + PAYUtils.getStrAmount(totalTempTipAmount) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

            if (translogByAdquirer.getTipoMontoFijo() != null) {
                //if (translogByAdquirer.getTipoMontoFijo().equals(AUTOMATICO)) {
                //if (translogByAdquirer.getMontoFijo() != 0) {
                setTextPrint(setTextColumn("TARIFA: ", "$ " + PAYUtils.getStrAmount(totalTempMontoFijo) + "    ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
                //}
                //}
            }
        }
    }*/

    private void printVersionVoid(boolean isRePrint, Paint paint, PrintCanvas canvas) {
        if (isRePrint) {
            setTextPrint(setCenterText("XXX   COPIA   XXX", S_BIG), paint, BOLD_ON, canvas, S_BIG);
            println(paint, canvas);
        }

        setTextPrint(setTextColumn(" ", "Version " + checkNull(VERSION).trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        println(paint, canvas);
        println(paint, canvas);
        println(paint, canvas);
    }

    private void limpiarMontoSubtotales() {
        subTotal = 0;
        ivaAmount = 0;
        serviceAmount = 0;
        tipAmount = 0;
        montoFijo = 0;
    }

    private void limpiarMontoSubTotales2() {
        subTotalSubTotal = 0;
        ivaAmountSubTotal = 0;
        serviceAmountSubTotal = 0;
        tipAmountSubTotal = 0;
        montoFijoSubTotal = 0;
    }

    private void limpiarMontoTotales() {
        totalTempAmount = 0;
        totalTempIva = 0;
        totalTempServiceAmount = 0;
        totalTempTipAmount = 0;
        totalTempMontoFijo = 0;
    }


    private String checkNull(String strText) {
        if (strText == null) {
            strText = "   ";
        }
        return strText;
    }

    private void printDateAndDues(Paint paint, PrintCanvas canvas) {

        String numCoutas = "0";

        if (dataTrans.getNumCuotas() > 1) {
            numCoutas = dataTrans.getNumCuotas() + "";
        }
        if (numCoutas.equals("0")) {
            setTextPrint(setTextColumn(getFormatDateAndTime(checkNull(dataTrans.getDatePrint()), checkNull(dataTrans.getLocalTime())),
                    " ", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        } else {
            setTextPrint(setTextColumn(getFormatDateAndTime(checkNull(dataTrans.getDatePrint()), checkNull(dataTrans.getLocalTime())),
                    "CUOTAS: " + numCoutas, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        }
    }

    private void printSecondHeader(String lote, String term, String id_com, Paint paint, PrintCanvas canvas) {
        setTextPrint(checkNumCharacters("LOTE:" + lote.trim() + " TERM:" + term.trim() + " ID COM:" + id_com.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        println(paint, canvas);
        printLine(paint, canvas);
        println(paint, canvas);
    }

    private void print_AP_REF(String AP, String REF, Paint paint, PrintCanvas canvas) {
        setTextPrint(setTextColumn("AP: " + AP.trim(), "REF: " + REF.trim(), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        println(paint, canvas);
    }

    private void print_expires_RRN_tipo(String expires, String RRN, String type, Paint paint, PrintCanvas canvas) {
        setTextPrint(checkNumCharacters("VENCE:" + expires.substring(2) + "/" + expires.substring(0, 2) + " RRN:" + RRN.trim() + " TIPO:" + type.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
    }

    private void print_Rnn(String RRN, Paint paint, PrintCanvas canvas) {
        setTextPrint(checkNumCharacters(" RRN: " + RRN.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
    }

    private void printAID(String AID, Paint paint, PrintCanvas canvas) {
        if (isICC) {
            setTextPrint("AID: " + checkNumCharacters(AID.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        } else if (isNFC) {
            if (dataTrans.getAidname().equals("") && AID.trim().substring(0, 14).equals("A0000000031010")) {
                setTextPrint("AID NAME: VISA CREDITO", paint, BOLD_ON, canvas, S_SMALL);
            } else {
                setTextPrint("AID NAME: " + dataTrans.getAidname(), paint, BOLD_ON, canvas, S_SMALL);
            }
            setTextPrint("AID: " + checkNumCharacters(AID.trim(), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        }

    }

    private void printDateAndTime(String data, int typeFont, boolean isBold, Paint paint, PrintCanvas canvas) {
        setTextPrint(checkNumCharacters(data, typeFont), paint, isBold, canvas, typeFont);
    }

    private String getFormatDateAndTime(String date, String time) {
        String newtime = PAYUtils.StringPattern(time.trim(), "HHmmss", "HH:mm");
        return "FECHA: " + date.trim() + "  HORA: " + newtime;
    }

    private void printAmountVoid(long total, String typeCoin, Paint paint, PrintCanvas canvas) {
        if (typeCoin.equals(LOCAL)) {
            typeCoin = "LOCAL.";
        } else {
            typeCoin = "DOLAR";
        }
        setTextPrint(setTextColumn("TOTAL      : $", formatAmountLess(total), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        println(paint, canvas);
        println(paint, canvas);
    }

    private void printSignature(String labelCard, boolean isCopy, Paint paint, PrintCanvas canvas) {
        if (!isCopy) {
            if (TMConfig.getInstance().isBanderaMessageFirma()) {
                if (dataTrans.getEntryMode().equals(MODE_CTL + CapPinPOS())) {
                    if (MasterControl.CTL_SIGN)
                        setTextPrint("FIRMA X........................", paint, BOLD_OFF, canvas, S_MEDIUM);
                } else {
                    setTextPrint("FIRMA X........................", paint, BOLD_OFF, canvas, S_MEDIUM);
                }
            }

            if (!labelCard.equals("---"))//Con esto evitamos imprimir la cadena "---" que se agrega cuando el labelCard es null
                if (labelCard.length() > 0) {
                    setTextPrint(setCenterText(checkNumCharacters(labelCard.trim(), S_MEDIUM), S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
                    println(paint, canvas);
                }

            if (TMConfig.getInstance().isBanderaMessageDoc())
                setTextPrint(checkNumCharacters("DOC: ", S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);

            if (TMConfig.getInstance().isBanderaMessageTel())
                setTextPrint(checkNumCharacters("TEL:", S_MEDIUM), paint, BOLD_OFF, canvas, S_MEDIUM);
            println(paint, canvas);
        }
    }

    private void printLine(String character, Paint paint, PrintCanvas canvas) {
        StringBuilder dat = new StringBuilder();
        for (int i = 0; i < 45; i++) {
            dat.append(character);
        }
        setTextPrint(dat.toString(), paint, BOLD_ON, canvas, S_SMALL);
    }

    private void printLine(Paint paint, PrintCanvas canvas) {
        setTextPrint("---------------------------------------------", paint, BOLD_ON, canvas, S_SMALL);
    }

    private void printLineResult(Paint paint, PrintCanvas canvas) {
        println(paint, canvas);
        setTextPrint("                  =======", paint, BOLD_OFF, canvas, S_BIG);
        println(paint, canvas);
    }

    private String setTextColumn(String columna1, String columna2, int size) {
        String aux = "";
        String auxText = columna2;
        auxText = setRightText(auxText, size);
        String auxText2 = columna1;

        if (auxText2.length() < auxText.length())
            aux = auxText.substring(auxText2.length());

        auxText2 += aux;

        return auxText2;
    }

    private String checkNumCharacters(String data, int size) {
        String dataPrint = "";
        int lenData = 0;

        lenData = data.length();

        switch (size) {
            case S_SMALL:
                if (lenData > MAX_CHAR_SMALL) {
                    dataPrint = data.substring(0, MAX_CHAR_SMALL);
                } else {
                    dataPrint = data;
                }
                break;

            case S_MEDIUM:
                if (lenData > MAX_CHAR_MEDIUM) {
                    dataPrint = data.substring(0, MAX_CHAR_MEDIUM);
                } else {
                    dataPrint = data;
                }
                break;

            case S_BIG:
                if (lenData > MAX_CHAR_BIG) {
                    dataPrint = data.substring(0, MAX_CHAR_BIG);
                } else {
                    dataPrint = data;
                }
                break;

        }
        return dataPrint;
    }

    private void println(Paint paint, PrintCanvas canvas) {
        setTextPrint("                                             ", paint, BOLD_ON, canvas, S_SMALL);
    }

    private void setTextPrint(String data, Paint paint, boolean bold, PrintCanvas canvas, int size) {
        Typeface typeface = (Typeface.MONOSPACE);
        data = checkNumCharacters(data, size);
        canvas.drawBitmap(drawText(data, (float) size, bold, typeface), paint);
    }

    private void setTextPrintREV(String data, Paint paint, boolean bold, PrintCanvas canvas, int size) {
        Typeface typeface = (Typeface.MONOSPACE);
        canvas.drawBitmap(drawTextREV(data, (float) size, bold, typeface), paint);
    }

    private String setCenterText(String data, int size) {
        data = padLeft(checkNumCharacters(data.trim(), size), size);
        return data;
    }

    private String setRightText(String data, int size) {
        String dataFinal = "";
        int len1 = 0;
        switch (size) {
            case S_SMALL:
                len1 = MAX_CHAR_SMALL - data.length();
                break;
            case S_MEDIUM:
                len1 = MAX_CHAR_MEDIUM - data.length();
                break;
            case S_BIG:
                len1 = MAX_CHAR_BIG - data.length();
                break;
        }

        for (int i = 0; i < len1; i++) {
            dataFinal += " ";
        }

        dataFinal += data;
        return dataFinal;
    }

    private String formatAmoun(long valor) {
        // Si pongo 1 en el monto se estalla
        String result = valor + "";
        result = result.substring(0, result.length() - 2);
        Long.parseLong(result);
        return String.format("%,d", Long.parseLong(result)).replace(",", ".");
    }

    private String formatAmounCaja(long valor) {
        String result = valor + "";
        result = result.substring(0, result.length() - 2);
        return result;
    }

    private String formatAmountLess(long valor) {

        String auxText;

        if (String.valueOf(valor).length() == 1)
            auxText = ISOUtil.decimalFormat("0" + String.valueOf(valor));
        else
            auxText = ISOUtil.decimalFormat(String.valueOf(valor));

        return auxText;
    }

    private String padLeft(String data, int size) {

        String dataFinal = "";
        int len1 = 0;

        switch (size) {
            case S_SMALL:
                len1 = MAX_CHAR_SMALL - data.length();
                break;
            case S_MEDIUM:
                len1 = MAX_CHAR_MEDIUM - data.length();
                break;
            case S_BIG:
                len1 = MAX_CHAR_BIG - data.length();
                break;
        }

        for (int i = 0; i < len1 / 2; i++) {
            dataFinal += " ";
        }
        dataFinal += data;

        return dataFinal;
    }

    private Bitmap drawText(String text, float textSize, boolean bold, Typeface typeface) {

        // Get text dimensions
        TextPaint textPaint = new TextPaint(ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeface);
        textPaint.setFakeBoldText(bold);

        StaticLayout mTextLayout = new StaticLayout(text, textPaint, 400, Layout.Alignment.ALIGN_NORMAL, 40.0f, 20.0f, false);

        // Create bitmap and canvas to draw to
        Bitmap b = Bitmap.createBitmap(400, mTextLayout.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);

        // Draw background
        Paint paint = new Paint(ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        c.drawPaint(paint);

        // Draw text
        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();

        return b;
    }

    private Bitmap drawTextREV(String text, float textSize, boolean bold, Typeface typeface) {

        // Get text dimensions
        TextPaint textPaint = new TextPaint(ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeface);
        textPaint.setFakeBoldText(bold);

        StaticLayout mTextLayout = new StaticLayout(text, textPaint, 400, Layout.Alignment.ALIGN_NORMAL, 40.0f, 20.0f, false);

        // Create bitmap and canvas to draw to
        Bitmap b = Bitmap.createBitmap(400, mTextLayout.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);

        // Draw background
        Paint paint = new Paint(ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        c.drawPaint(paint);

        // Draw text
        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();

        return b;
    }


    private int printData(PrintCanvas pCanvas, String text) {
        final CountDownLatch latch = new CountDownLatch(1);
        printer = Printer.getInstance();
        int ret = printer.getStatus();
        Logger.debug("打印机状态：" + ret);
        if (Printer.PRINTER_STATUS_PAPER_LACK == ret) {
            Logger.debug("打印机缺纸，提示用户装纸");
            //transUI.handling(60 * 1000, Tcode.Status.printer_lack_paper, text);
            long start = SystemClock.uptimeMillis();
            while (true) {
                if (SystemClock.uptimeMillis() - start > 60 * 1000) {
                    ret = Printer.PRINTER_STATUS_PAPER_LACK;
                    break;
                }
                if (printer.getStatus() == Printer.PRINTER_OK) {
                    ret = Printer.PRINTER_OK;
                    break;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        Thread.currentThread().interrupt();
                        Logger.debug("printer task interrupted");
                    }
                }
            }
        }
        Logger.debug("开始打印");
        if (ret == Printer.PRINTER_OK) {
            //transUI.handling(60 * 1000, Tcode.Status.printing_recept, text);
            printTask.setPrintCanvas(pCanvas);
            printer.startPrint(printTask, new PrinterCallback() {
                @Override
                public void onResult(int i, PrintTask printTask) {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Logger.error("Exception" + e.toString());
                Thread.currentThread().interrupt();
            }
        }
        return ret;
    }

    public byte[] crearImagen(Bitmap bitmap) {
        // tamaño del baos depende del tamaño de tus imagenes en promedio
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);

        // aqui tenemos el byte[] con el imagen comprimido, ahora lo guardemos en SQLite

        return baos.toByteArray();
    }


    private int checkPrinterStatus() {
        long t0 = System.currentTimeMillis();
        int ret;
        while (true) {
            if (System.currentTimeMillis() - t0 > 30000) {
                ret = -1;
                break;
            }
            ret = printer.getStatus();
            Logger.debug("printer.getStatus() ret = " + ret);
            if (ret == Printer.PRINTER_OK) {
                Logger.debug("printer.getStatus()=Printer.PRINTER_OK");
                Logger.debug("打印机状态正常");
                break;
            } else if (ret == -3) {
                Logger.debug("printer.getStatus()=Printer.PRINTER_STATUS_PAPER_LACK");
                Logger.debug("提示用户装纸...");
                break;
            } else if (ret == Printer.PRINTER_STATUS_BUSY) {
                Logger.debug("printer.getStatus()=Printer.PRINTER_STATUS_BUSY");
                Logger.debug("打印机忙");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    Logger.error("Exception" + e.toString());
                    Thread.currentThread().interrupt();
                }
            } else {
                break;
            }
        }
        return ret;
    }

    private String formatTranstype(String type) {
        int index = 0;
        for (int i = 0; i < PrintRes.TRANSEN.length; i++) {
            if (PrintRes.TRANSEN[i].equals(type)) {
                index = i;
            }
        }
        if (Locale.getDefault().getLanguage().equals("zh")) {
            return PrintRes.TRANSCH[index] + "(" + type + ")";
        } else {
            return type;
        }
    }

    private String formatDetailsType(TransLogData data) {
        if (data.isICC()) {
            return "(C)";
        } else if (data.isNFC()) {
            return "(CTLS)";
        } else {
            return "(B)";
        }
    }

    private String formatDetailsAuth(TransLogData data) {
        if (data.getAuthCode() == null) {
            return "000000";
        } else {
            return data.getAuthCode();
        }
    }

    private void setFontStyle(Paint paint, int size, boolean isBold) {
        if (isBold) {
            paint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            paint.setTypeface(Typeface.SERIF);
        }
        switch (size) {
            case 0:
                break;
            case 1:
                paint.setTextSize(15F);
                break;
            case 2:
                paint.setTextSize(22F);
                break;
            case 3:
                paint.setTextSize(30F);
                break;
            default:
                break;
        }
    }

    /**
     * Bancard
     */
    private void printLogoRedInfonet(Paint paint, PrintCanvas canvas) {
        try {
            Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logoinfonet02);
            canvas.setX(115);
            canvas.drawBitmap(image, paint);
            canvas.setX(0);
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Log.e("ERROR", e.toString());
        }

    }

    /**
     * Medianet
     */
    private void printHeaderBancard(String text1, String text2, String text3, String text4, Paint paint, PrintCanvas canvas) {
        if (!text1.isEmpty() && !text1.substring(0, 1).equals("0")) {
            setTextPrint(setCenterText(text1.trim(), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        }
        if (!text2.isEmpty() && !text2.substring(0, 1).equals("0")) {
            setTextPrint(setCenterText(text2.trim(), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        }
        if (!text3.isEmpty() && !text3.substring(0, 1).equals("0")) {
            setTextPrint(setCenterText(text3.trim(), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        }
        if (!text4.isEmpty() && !text4.substring(0, 1).equals("0")) {
            setTextPrint(setCenterText(text4.trim(), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
        }
    }

    /**
     * Bancard
     */
    private void printComercioTID(String comercio, String tid, Paint paint, PrintCanvas canvas) {
        setTextPrint(setTextColumn("COMERCIO: " + comercio, "TID: " + tid, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
    }


    /**
     * Bancard
     */
    private void printPuntosBancard(Paint paint, PrintCanvas canvas) {
        String field28 = DataAdicional.getField(28);
        if (field28 != null) {
            if (!field28.isEmpty()) {
                setTextPrint(setCenterText(field28, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            }
        }
    }

    private void printStringLargue(String mensaje, int size, boolean isCenter, boolean bold, Paint paint, PrintCanvas canvas) {
        int tamañoMaximmoLinea;
        String[] palabras = mensaje.trim().split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> lienas = new ArrayList<String>();
        switch (size) {
            case S_SMALL:
                tamañoMaximmoLinea = MAX_CHAR_SMALL;
                break;
            case S_MEDIUM:
                tamañoMaximmoLinea = MAX_CHAR_MEDIUM;
                break;
            case S_BIG:
                tamañoMaximmoLinea = MAX_CHAR_BIG;
                break;

            default:
                return;
        }
        for (String s : palabras) {
            if (stringBuilder.length() + s.length() < tamañoMaximmoLinea) {
                stringBuilder.append(s + " ");
            } else {
                lienas.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append(s + " ");
            }
        }
        lienas.add(stringBuilder.toString());

        if (isCenter) {
            for (String linea : lienas) {
                setTextPrint(setCenterText(linea, size), paint, bold, canvas, size);
            }
        } else {
            for (String linea : lienas) {
                setTextPrint(linea, paint, bold, canvas, size);
            }
        }

    }

    private void print_DateAndTime(TransLogData datatrans, Paint paint, PrintCanvas canvas, boolean isTotalReport) {

        try {
            if (isTotalReport) {
                setTextPrint("FECHA   : " + PAYUtils.getDay() + "/" + PAYUtils.getMonth() + "/" + String.valueOf(PAYUtils.getYear()).substring(2), paint, BOLD_ON, canvas, S_MEDIUM);
                setTextPrint("HORA    : " + PAYUtils.StringPattern(PAYUtils.getLocalTime().trim(), "HHmmss", "HH:mm"), paint, BOLD_ON, canvas, S_MEDIUM);
            } else {
                if (datatrans.getLocalDate() != null && datatrans.getLocalTime() != null) {
                    setTextPrint(setTextColumn(formato2Fecha(datatrans.getLocalDate()),
                            formato2Hora(datatrans.getLocalTime()), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
                } else {
                    setTextPrint(setTextColumn(PAYUtils.getDay() + "/" + PAYUtils.getMonth() + "/" + String.valueOf(PAYUtils.getYear()).substring(2),
                            "H: " + formato2Hora(datatrans.getLocalTime()), S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);
                }
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    private void printLoteDataCARD(String Acquirer, String Pan, String expDate, String Lote, Paint paint, PrintCanvas canvas) {
        setTextPrint(setTextColumn(Acquirer, Pan, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);

        /*switch (dataTrans.getEName()) {
            case Trans.Type.VENTA:
            case Trans.Type.ANULACION:
                expDate = "XX/XXXX";
                break;
            default:
                break;
        }*/
        expDate = "XX/XXXX";
        setTextPrint(setTextColumn("VENCE: " + expDate + "     " + checkNull(typeEntryPoint()), "Lote: " + Lote, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
    }

    private void printTraceNoAuthNo(String TraceNo, String AuthNo, Paint paint, PrintCanvas canvas) {
        setTextPrint(setTextColumn("TRANSACCION # " + TraceNo, "AUTORIZACION # " + AuthNo, S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
    }

    private void printHeadDefered(String typeTrans, Paint paint, PrintCanvas canvas) {

        if (typeTrans.equals(Trans.Type.DEFERRED)) {

            if (dataTrans.getNumCuotasDeferred() != null) {

                String fee = dataTrans.getNumCuotasDeferred().substring(0, 2);
                String feeMonthGrace = dataTrans.getNumCuotasDeferred().substring(2, 4);

                setTextPrint(setCenterText("PLAZO MESES: " + fee, S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);

            }
            println(paint, canvas);
        }
    }

    private void printTipoTrans(String typeTrans, Paint paint, PrintCanvas canvas) {
        println(paint, canvas);
        setTextPrint(setCenterText("- " + typeTrans + " -", S_MEDIUM), paint, BOLD_ON, canvas, S_MEDIUM);

        printHeadDefered(typeTrans, paint, canvas);
    }

    private void printDataCARDCHIP(Paint paint, PrintCanvas canvas) {
        if (isICC) {
            setTextPrint(setCenterText(checkNull(dataTrans.getTypeAccount()), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            setTextPrint(setTextColumn(checkNull(dataTrans.getAid()), checkNull(dataTrans.getTc()) + checkNull(dataTrans.getTvr()) + checkNull(dataTrans.getTsi()), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        } else if (isNFC) {
            //TODO Validacion no concuerda. FA
            if (dataTrans.getAidname().equals("") && dataTrans.getAid().trim().substring(0, 14).equals("A0000000031010")) {
                setTextPrint(setTextColumn("VISA CREDIT", "Ctls", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            } else {
                setTextPrint(setTextColumn(dataTrans.getAidname(), "Ctls", S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
            }
            setTextPrint(setTextColumn(checkNull(dataTrans.getAid().trim()), checkNull(dataTrans.getArqc()), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        }
        if (dataTrans.getField61() != null) {
            setTextPrint(setCenterText(checkNull(dataTrans.getField61()), S_SMALL), paint, BOLD_ON, canvas, S_SMALL);
        }
    }


    private String checkIfModoItem(int ucTipoMonto) {

        String tipoEntrada = null;

        switch (ucTipoMonto) {
            case SERVICEAMOUNT:
                tipoEntrada = "A";
                break;

            case TIPAMOUNT:
                tipoEntrada = "P";
                break;

            case IVAAMOUNT:
                tipoEntrada = "D";
                break;
        }

        return tipoEntrada;
    }

    private List<TransLogData> orderList(List<TransLogData> list1) {

        String getNameAcq = "";
        int cont = 0;
        List<TransLogData> auxList = new ArrayList<>();
        List<TransLogData> list = new ArrayList<>(list1);

        try {
            while (list.size() > 0) {

                int lenAuxlist = auxList.size();
                String acqActual = "";

                if (cont >= list.size()) {
                    auxList.add(list.get(0));

                    getNameAcq = list.get(0).getIssuerName().trim();

                    list.remove(0);
                    cont = 0;

                } else {

                    getNameAcq = list.get(cont).getIssuerName().trim();

                }
                if (list.size() > 0) {
                    if (lenAuxlist > 0) {

                        acqActual = auxList.get(lenAuxlist - 1).getIssuerName().trim();
                    } else {
                        auxList.add(list.get(0));
                        list.remove(0);
                        continue;
                    }
                    if (getNameAcq.equals(acqActual)) {
                        auxList.add(list.get(cont));
                        list.remove(cont);
                    } else {
                        cont++;
                    }
                }
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
        return auxList;
    }


    private String formatoFecha(String fecha) {
        StringBuilder date = new StringBuilder();

        date.append(fecha.substring(4));
        date.append("/");
        date.append(PAYUtils.getMonth(fecha.substring(2, 4)));
        date.append("/");
        date.append(fecha, 0, 2);

        return date.toString();
    }

    private String formato2Fecha(String fecha) {//20190310
        StringBuilder date = new StringBuilder();

        date.append(fecha.substring(6));
        date.append("/");
        date.append(PAYUtils.getMonth(fecha.substring(4, 6)));
        date.append("/");
        date.append(fecha, 2, 4);

        return date.toString();
    }

    private String formatoHora(String hora) {
        StringBuilder date = new StringBuilder();

        date.append(hora, 0, 2);
        date.append(":");
        date.append(hora, 2, 4);
        date.append(":");
        date.append(hora.substring(4));

        return date.toString();
    }

    private String formatoHoraMin(String hora) {
        StringBuilder date = new StringBuilder();

        date.append(hora, 0, 2);
        date.append(":");
        date.append(hora, 2, 4);
        return date.toString();
    }


    private String formato2Hora(String hora) {
        StringBuilder date = new StringBuilder();

        date.append(hora, 0, 2);
        date.append(":");
        date.append(hora, 2, 4);
        date.append(":");
        date.append(hora, 4, 6);
        return date.toString();
    }


    private String typeEntryPoint() {
        String typeEntry = "";

        try {
            if (isFallback) {
                typeEntry = "FALLBACK";
            } else if (dataTrans.getEntryMode().equals(MODE_MAG + CapPinPOS())) {
                typeEntry = "BANDA";
            } else if (dataTrans.getEntryMode().equals(MODE_ICC + CapPinPOS())) {
                typeEntry = "CHIP";
            } else if (dataTrans.getEntryMode().equals(MODE_CTL + CapPinPOS())) {
                typeEntry = "CTL C";
            } else if (dataTrans.getEntryMode().equals(MODE_HANDLE + CapPinPOS())) {
                typeEntry = "MANUAL";
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }

        return typeEntry;
    }

    public String CapPinPOS() {
        String capPINPos = "1";
        //Configuramos el ultimo digito del entry point para Union Pay

        return capPINPos;
    }


    private void impresionCaja() {
        FinanceTrans.confirmacionCaja = null;
        ModelImpresion impresion = new ModelImpresion();


        impresion.setCodigoAutorizacion(dataTrans.getAuthCode());

        String codigoBoleta = dataTrans.getRrn();
        impresion.setNroBoleta(codigoBoleta);

        impresion.setCodigoComercio(checkNull(dataTrans.getCodigoDelNegocio()));

        impresion.setNombreTarjeta(checkNull(dataTrans.getField61()));

        validarCardPan(impresion);
        impresion.setMensajeDisplay("APROBADA");

        validarVuelto(impresion);

        //NOMBRE DEL TITULAR DE LA TARJETA Y CAMPO DE FIRMA
        impresion.setNombreCliente(obtenerNombreCiente(dataTrans.getNameCard()));
        String field89 = DataAdicional.getField(89);
        if (field89 != null && !field89.isEmpty()) {
            impresion.setIssuerId(field89);
        }

        if (ApiJson.listener != null) {
            ApiJson.listener.rsp2Cajas(impresion, "200");
        } else {
            Log.d("ERROR", "impresionCaja: " + " ApiJson.listener == null");
        }

    }

    private void validarCardPan(ModelImpresion impresion) {
        String auxPan = dataTrans.getPan();
        if (auxPan != null && !auxPan.isEmpty()) {
            if (auxPan.length() >= 4) {
                impresion.setPan(auxPan.substring((dataTrans.getPan().length() - 4)));
            }
        } else if (dataTrans.getField02() != null) {
            impresion.setPan(dataTrans.getField02().substring((dataTrans.getField02().length() - 4)));
        }
    }

    private String obtenerNombreCiente(String nameCard) {
        boolean isHexa;
        if (nameCard != null && !nameCard.isEmpty() && !nameCard.equals("----")) {//Con esto evitamos imprimir la cadena "---" que se agrega cuando el labelCard es null
            if (nameCard.length() > 0) {
                isHexa = nameCard.matches("^[0-9a-fA-F]+$");                   //validacion de variable labelCard para evitar conversion
                if (!isHexa) {
                    nameCard = ISOUtil.convertStringToHex(nameCard);
                }
                return checkNumCharacters(ISOUtil.hex2AsciiStr(nameCard.trim()), S_SMALL);
            }
        } else {
            if (dataTrans.getField61() != null) {
                return dataTrans.getField61();
            }
        }

        return "";
    }

    private void validarVuelto(ModelImpresion impresion) {
        String field41 = DataAdicional.getField(41);
        if (field41 != null) {
            impresion.setSaldo(Integer.parseInt(formatAmounCaja(dataTrans.getAmount() - Integer.parseInt(ISOUtil.hex2AsciiStr(field41)))));
            String vuelto = "00";
            vuelto = formatAmounCaja(Integer.parseInt(ISOUtil.hex2AsciiStr(field41)));
            System.out.println("VUELTO *************** " + vuelto);
            impresion.setMontoVuelto(Integer.parseInt(vuelto));

        } else {
            if (dataTrans.getAmount() != null) {
                impresion.setSaldo(Integer.parseInt(formatAmounCaja(dataTrans.getAmount())));
            }
        }
    }


    public String getTraceNo() {
        return TraceNo;
    }

    public void setTraceNo(String traceNo) {
        if (traceNo != null) {
            TraceNo = traceNo;
        } else {
            TraceNo = " ";
        }

    }

    public boolean isCajas() {
        return isCajas;
    }

    public void setCajas(boolean cajas) {
        isCajas = cajas;
    }

}
