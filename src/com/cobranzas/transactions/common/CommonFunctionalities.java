package com.cobranzas.transactions.common;

import static android.content.Context.MODE_PRIVATE;
import static com.cobranzas.actividades.StartAppBANCARD.listadoTransacciones;
import static com.cobranzas.actividades.StartAppBANCARD.tablaCards;
import static com.cobranzas.defines_bancard.DefinesBANCARD.INGRESO_VUELTO;
import static com.cobranzas.menus.menus.NO_FALLBACK;
import static com.newpos.libpay.trans.Trans.ENTRY_MODE_FALLBACK;
import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.SystemClock;

import com.android.desert.keyboard.InputInfo;
import com.android.newpos.libemv.PBOCUtil;
import com.cobranzas.adaptadores.ModeloMensajeConfirmacion;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.TRANSACCIONES;
import com.cobranzas.menus.menus;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.presenter.TransUI;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.finace.FinanceTrans;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.SlotType;

import java.util.Objects;

public class CommonFunctionalities {

    private static final String FIRTS_TRANS = "firtsTrans";
    private static final String FECHA_CIERRE = "fecha-cierre";
    public static StringBuilder Fld58Prompts;
    public static StringBuilder Fld58PromptsPrinter;
    public static StringBuilder Fld58PromptsAmountPrinter;
    public static boolean multicomercio = false;
    public static String idComercio;
    public static long sumarTotales;
    public static boolean isSumarTotales = false;
    static String clase = "CommonFunctionalities.java";
    private static String pan;
    private static String codOTT;
    private static String expDate;
    private static String cvv2;
    private static boolean isPinExist;
    private static String pin;
    private static String ksn;
    private static String numReferencia;
    private static String proCode;

    private CommonFunctionalities() {
    }

    public static String getKsn() {
        return ksn;
    }

    public static String getPan() {
        return pan;
    }

    public static String getCodOTT() {
        return codOTT;
    }

    public static String getExpDate() {
        return expDate;
    }

    public static String getCvv2() {
        return cvv2;
    }

    public static boolean isIsPinExist() {
        return isPinExist;
    }

    public static String getPin() {
        return pin;
    }

    public static String getNumReferencia() {
        return numReferencia;
    }

    public static String getProCode() {
        return proCode;
    }

    public static String getFld58Prompts() {
        if (Fld58Prompts == null || Fld58Prompts.toString().equals("")) {
            return null;
        }
        return Fld58Prompts.toString();
    }

    public static String getFld58PromptsPrinter() {
        return Fld58PromptsPrinter.toString();
    }

    public static String getFld58PromptsAmountPrinter() {
        return Fld58PromptsAmountPrinter.toString();
    }

    public static boolean isMulticomercio() {
        return multicomercio;
    }

    public static String getIdComercio() {
        return idComercio;
    }

    public static long getSumarTotales() {
        return sumarTotales;
    }

    public static boolean isSumarTotales() {
        return isSumarTotales;
    }

    private static boolean getImg(String img) {
        boolean rta = false;
        switch (img.trim()) {
            case "0"://visa
            case "1"://Master
            case "2"://Amex
            case "3"://Diners
            case "4"://Visa Electron
            case "5"://Maestro
            case "6"://
                rta = true;
                break;

            default:
                break;
        }
        return rta;
    }

    public static void showCardImage(TransUI transUI) {

        String idLabel = "";

        if (getImg(idLabel)) {
            transUI.showCardImg(idLabel);
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                break;
            }
        }
    }

    public static String[] tipoMoneda() {
        return new String[]{"$", FinanceTrans.LOCAL};
    }

    public static int setPanManual(int timeout, String transEName, TransUI transUI) {

        int ret = 1;

        while (true) {

            InputInfo inputInfo = transUI.showInputUser(timeout, transEName, "DIGITE TARJETA", 0, 19);

            if (inputInfo.isResultFlag()) {
                pan = inputInfo.getResult();
                //Falta agregar funcionalidad para verificar el digito de chequeo de la tarjeta
                ret = 0;
                break;
            } else {
                ret = Tcode.T_user_cancel_input;
                transUI.showError(timeout, ret, false);
                break;
            }
        }

        return ret;
    }

    public static int setOTT_Token(int timeout, String transEName, String title, String tipoPE, int min, int max, TransUI transUI) {

        int ret = 1;

        while (true) {

            InputInfo inputInfo = null;

            if (inputInfo.isResultFlag()) {
                codOTT = inputInfo.getResult();
                ret = 0;
                break;
            } else {
                ret = Tcode.T_user_cancel_input;
                transUI.showError(timeout, ret, false);
                break;
            }
        }

        return ret;
    }

    public static int setFechaExp(int timeout, String transEName, TransUI transUI, boolean mostrarPantalla) {

        int ret = 1;
        String tmp;

        if (!mostrarPantalla) {
            return 0;
        }

        while (true) {
            InputInfo inputInfo = transUI.showInputUser(timeout, transEName, "FECHA EXPIRACION MM/YY", 0, 4);

            if (inputInfo.isResultFlag()) {
                tmp = inputInfo.getResult();
                expDate = "";
                try {
                    expDate += tmp.substring(2, 4);
                    expDate += tmp.substring(0, 2);
                    ret = 0;
                } catch (IndexOutOfBoundsException e) {
                    ret = Tcode.T_err_invalid_len;
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    transUI.toasTrans(Tcode.T_err_invalid_len, true, true);
                    continue;
                }
                break;
            } else {
                ret = Tcode.T_user_cancel_input;
                transUI.showError(timeout, ret, false);
                break;
            }
        }

        return ret;
    }

    public static int setCVV2(int timeout, String transEName, TransUI transUI, boolean mostrarPantalla) {

        int ret = 1;

        if (!mostrarPantalla) {
            return 0;
        }

        while (true) {
            InputInfo inputInfo = transUI.showInputUser(timeout, transEName, "CODIGO SEGURIDAD CVV2", 0, 3);

            if (inputInfo.isResultFlag()) {
                if (inputInfo.getResult().length() == 3) {
                    cvv2 = inputInfo.getResult();
                    ret = 0;
                } else {
                    ret = Tcode.T_err_invalid_len;
                    transUI.toasTrans(Tcode.T_err_invalid_len, true, true);
                    continue;
                }
                break;
            } else {
                ret = Tcode.T_user_cancel_input;
                transUI.showError(timeout, ret, false);
                break;
            }
        }

        return ret;
    }

    public static int ctlPIN(String pan, int timeout, long amount, TransUI transUI) {
        int ret = 1;
        PinInfo info = transUI.getPinpadOnlinePinDUKPT(timeout, String.valueOf(amount), pan);
        if (info.isResultFlag()) {
            if (info.isNoPin()) {
                isPinExist = false;
            } else {
                if (null == info.getPinblock()) {
                    isPinExist = false;
                } else {
                    isPinExist = true;
                }
                pin = ISOUtil.hexString(Objects.requireNonNull(info.getPinblock()));
                ksn = info.getKsnString();
                ret = 0;
            }
            if (!isPinExist) {
                ret = Tcode.T_user_cancel_pin_err;
                return ret;
            }
        } else {
            ret = Tcode.T_user_cancel_pin_err;
            return ret;
        }
        return ret;
    }

    public static int last4card(int timeout, String transEName, String pan, TransUI transUI, boolean mostrarPantalla) {

        int ret = 1;
        int intRest = 1;
        String intento = "";

        if (!mostrarPantalla)
            return 0;

        while (true) {
            InputInfo inputInfo = transUI.showInputUser(timeout, transEName + intento, "ULTIMOS 4 DIGITOS", 0, 4);

            if (inputInfo.isResultFlag()) {
                String last4Pan = pan.substring((pan.length() - 4), pan.length());
                if (last4Pan.equals(inputInfo.getResult())) {
                    ret = 0;
                    break;
                } else {
                    ret = Tcode.T_err_last_4;
                    transUI.toasTrans(Tcode.T_err_last_4, true, true);
                }
                intRest--;
                intento = "\nIntento Restante " + intRest;
                if (intRest == 0) {
                    ret = Tcode.T_err_last_4;
                    break;
                }
            } else {
                ret = Tcode.T_user_cancel_input;
                transUI.showError(timeout, ret, false);
                break;
            }
        }

        return ret;
    }

    public static int setNumReferencia(int timeout, String transEName, TransUI transUI) {

        int ret = 1;

        while (true) {
            InputInfo inputInfo = transUI.showInputUser(timeout, transEName, "NO. REFERENCIA", 0, 6);

            if (inputInfo.isResultFlag()) {
                numReferencia = inputInfo.getResult();
                ret = 0;
                break;
            } else {
                ret = Tcode.T_user_cancel_input;
                transUI.showError(timeout, ret, false);
                break;
            }
        }

        return ret;
    }

    public static int setTipoCuenta(int timeout, String fld3, TransUI transUI, boolean mostrarPantalla) {

        int ret = 1;

        if (!mostrarPantalla) {
            proCode = fld3;
            return 0;
        }

        InputInfo inputInfo = transUI.showTypeCoin(timeout, "TIPO DE CUENTA");
        if (inputInfo.isResultFlag()) {
            if (inputInfo.getResult().equals("1")) {
                proCode = fld3.replaceFirst("30", "10");
            } else if (inputInfo.getResult().equals("2")) {
                proCode = fld3.replace("30", "20");
            } else {
                proCode = fld3;
            }
            ret = 0;
        } else {
            ret = Tcode.T_user_cancel_input;
            transUI.showError(timeout, ret, false);
        }
        return ret;
    }

    public static boolean checkExpDate(String track2, boolean checkExpDate) {
        String track;
        String dateCard;
        String dateLocal;
        int yearCard;
        int monCard;
        int yearLocal;
        int monLocal;

        track = track2.replace('D', '=');
        String tmp2 = track.substring(track.indexOf('=') + 1, track.length());
        dateCard = tmp2.substring(0, 4);
        expDate = dateCard;

        if (!checkExpDate)
            return false;


        dateLocal = PAYUtils.getExpDate();
        monLocal = Integer.parseInt(dateLocal.substring(2));
        yearLocal = Integer.parseInt(dateLocal.substring(0, 2));
        yearCard = Integer.parseInt(dateCard.substring(0, 2));
        monCard = Integer.parseInt(dateCard.substring(2));

        if (yearCard > yearLocal) {
            return false;
        } else if (yearCard == yearLocal) {
            if (monCard > monLocal) {
                return false;
            } else return monCard != monLocal;
        } else {
            return true;
        }
    }

    public static void saveFirtsTrans(Context context, boolean flag) {
        SharedPreferences.Editor editor = context.getSharedPreferences(FIRTS_TRANS, MODE_PRIVATE).edit();
        editor.putBoolean(FIRTS_TRANS, flag);
        editor.apply();
    }

    public static String getFechaUltimoCierre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(FECHA_CIERRE, MODE_PRIVATE);
        return prefs.getString("fechaUltimoCierre", null);
    }

    public static String getIdentificadorUltimoCierre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(FECHA_CIERRE, MODE_PRIVATE);
        return prefs.getString("identificadorCierre", null);
    }

    public static String getUltimoComercio(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(FECHA_CIERRE, MODE_PRIVATE);
        return prefs.getString("Comercio", null);
    }

    public static String getFechaUltimaIncializacion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(DefinesBANCARD.namePreferencia, MODE_PRIVATE);
        return prefs.getString(DefinesBANCARD.fechaInicializacion, null);
    }

    public static int fallback(int retVal) {
        int ret = retVal;
        if (ret > 1) {

            if (ret == 124) {//NO AID
                menus.contFallback = ENTRY_MODE_FALLBACK;
                ret = Tcode.T_err_fallback;
            } else {
                menus.contFallback = NO_FALLBACK;
            }
        }
        return ret;
    }

    public static boolean validateCard(int timeout, TransUI transUI) {
        boolean ret;
        final int TIMEOUT_REMOVE_CARD = timeout * 1000;

        IccReader iccReader0;
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        iccReader0 = IccReader.getInstance(SlotType.USER_CARD);
        long start = SystemClock.uptimeMillis();

        while (true) {
            try {
                if (iccReader0.isCardPresent()) {
                    transUI.showMessage("Retire la tarjeta", true);
                    toneG.startTone(ToneGenerator.TONE_PROP_BEEP2, 2000);

                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        Logger.error("Exception" + e.toString());
                        Thread.currentThread().interrupt();
                    }

                    if (SystemClock.uptimeMillis() - start > TIMEOUT_REMOVE_CARD) {
                        toneG.stopTone();
                        ret = false;
                        break;
                    }
                } else {
                    ret = true;
                    break;
                }
            } catch (Exception e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                ret = true;
                break;
            }

        }
        return ret;
    }

    public static long validaPedirVuelto(Context context, int timeout, TransUI transUI, String pan, long montoVenta) {

        long rtn = 0;

        /*
        - Para habilitar cashBack, debe estar habilitado en transacciones y cards
        - El monto como sugerencia de cashback, es tomado de la tabla transacciones
         */
        int montoCashback = validarCashBackPorTransaccion(DefinesBANCARD.POLARIS_NAME_TX_VUELTO);

        if (montoCashback > -1 && validarCashBackPorBines()) {
            int tempo = preguntarCahBack(timeout, transUI, montoVenta);
            if (tempo == 1) {
                rtn = pedirCashback(context, timeout, "Venta con vuelto", transUI, montoCashback);
            } else {
                rtn = tempo;
            }
        }
        return rtn;
    }

    /**
     * @param nombre
     * @return :
     * -1 Cashback Deshabilitado
     * > Mayor a -1 : CashBack habilitado
     */
    private static int validarCashBackPorTransaccion(String nombre) {
        int montoCashback = -1; // -1 Cashback Deshabilitado

        try {
            for (TRANSACCIONES transacciones : listadoTransacciones) {
                if (transacciones.getNombre().contains(nombre) && transacciones.getHabilitar()) {
                    if (transacciones.getCashBack()) {
                        if (transacciones.getCashBackMonto().isEmpty()) {
                            montoCashback = 0;
                        } else {// Monto valido
                            montoCashback = Integer.parseInt(transacciones.getCashBackMonto());
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.debug("Fallo al obtener CASHBACK por TRANSACCION :" + e.getMessage());
        }


        return montoCashback;
    }

    private static boolean validarCashBackPorBines() {
        return tablaCards.isCashBack();
    }

    /*
    return > true/false
     */
    public static int preguntarCahBack(int timeout, TransUI transUI, long montoVenta) {
        String format = "";

        ModeloMensajeConfirmacion mensajeConfirmacion = new ModeloMensajeConfirmacion();
        mensajeConfirmacion.setBanner("VUELTO");

        format = String.valueOf(montoVenta);
        if (!format.equals("")) {
            mensajeConfirmacion.setTitulo("Monto compra : Gs. " + PAYUtils.FormatPyg(format));
        }

        mensajeConfirmacion.setMensaje("¿Desea vuelto?");
        mensajeConfirmacion.setMsgBtnAceptar("Si");
        mensajeConfirmacion.setMsgBtnCancelar("No");

        InputInfo info = transUI.showMensajeConfirmacion(timeout, mensajeConfirmacion);
        if (info.isResultFlag()) {
            return 1;
        } else {
            if (info.getResult() != null && info.getResult().equals("no")) {
                return 0;
            }
            return -1;

        }

    }

    /**
     * Preguntar confirmar transsacion
     *
     * @param timeout   wait time
     * @param transUI   ui
     * @param questions subfield 86
     * @return true if user press "SI" in ui
     */
    public static boolean confirmTransaction(int timeout, TransUI transUI, String questions) {
        try {
            String code = "xxxx";
            String description = "-";
            String amount = "Gs. 0";
            String data = questions.substring(6, questions.length()-2);
            if (questions.length() > 0) {
                int amountLength=data.length()-20;
                code = data.substring(0, 11).trim();
                description = data.substring(code.length(), amountLength).trim();
                amount = data.substring(amountLength).trim();
            }

            ModeloMensajeConfirmacion confirmacion = new ModeloMensajeConfirmacion();
            confirmacion.setBanner("Confirmación");
            confirmacion.setTitulo("\n"+code + "\n" + description + "\n" + amount + "\n");
            confirmacion.setMensaje("¿Confirma?");
            confirmacion.setMsgBtnAceptar("Sí");
            confirmacion.setMsgBtnCancelar("No");

            InputInfo info = transUI.showMensajeConfirmacion(timeout, confirmacion);

            if (info.isResultFlag() && info.getResult() != null) {
                return info.getResult().equals("si");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /* Solicita el ingreso de CashBack - Vuelto
    -1 : Operacion Cancelada por usario
    Otro valor : Valor digitado por el usuario
     */
    public static long pedirCashback(Context context, int timeout, String transEName, TransUI transUI, int montoCashBack) {
        long rtnMonto = -1;

        Logger.debug("Solicitud Cash Back - Vuelto");

        try {
            String strMonto;
            strMonto = String.valueOf(montoCashBack);
            InputInfo info = transUI.showIngresoDataNumerico(
                    timeout,
                    INGRESO_VUELTO, "Monto máximo : Gs. " + PAYUtils.FormatPygNoCentimos(strMonto),
                    "Vuelto: ", 7,
                    transEName,
                    0);
            if (info.isResultFlag()) {
                rtnMonto = Long.parseLong(info.getResult()) * 100; // * 100, adiciona centavos 00
                Logger.debug("Vuelto ingresado" + rtnMonto);
            }
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.debug("FALLO Solicitud Cash Back - Vuelto :" + e.getMessage());
        }

        return rtnMonto;
    }

    /**
     * Metodo que dado una cadena de String con tags edita un nuevo mensaje
     *
     * @param originalData data original
     * @param tag          T
     * @param length       L
     * @param newValue     V
     * @return newData
     * <p>
     * Nota:
     * El tamaño del nuevo valor tiene que ser igual al tamaño del tag que se encuentra en la data original
     * <p>
     * En caso de que el tamaño exceda el tamaño real de newValue se retorna null
     */
    public static String setTag(byte[] originalData, int tag, int length, String newValue) {


        if (originalData == null)
            return null;

        int lengNewVal = newValue.length() / 2;

        if (length != lengNewVal)
            return null;

        byte[] oriDatB = new byte[originalData.length - 1];

        System.arraycopy(originalData, 1, oriDatB, 0, oriDatB.length);

        int offset = 0;
        int totalLen = oriDatB.length;

        while (offset < totalLen) {
            int tagLocal;
            if ((oriDatB[offset] & 31) == 31) {
                tagLocal = PBOCUtil.byte2int(oriDatB, offset, 2);
                offset += 2;
            } else {
                tagLocal = PBOCUtil.byte2int(new byte[]{oriDatB[offset++]});
            }

            int len = PBOCUtil.byte2int(new byte[]{oriDatB[offset++]});
            if ((len & -128) != 0) {
                int lenL = len & 3;
                len = PBOCUtil.byte2int(oriDatB, offset, lenL);
                offset += lenL;
            }

            byte[] temp;

            if (tag == tagLocal) {
                temp = ISOUtil.hex2byte(newValue);
                System.arraycopy(temp, 0, oriDatB, offset, len);
                Logger.debug("Se encontro el tag: 0x" + Integer.toHexString(tag));
            }
            offset += len;

        }

        byte[] resp = new byte[originalData.length];
        resp[0] = originalData[0];

        System.arraycopy(oriDatB, 0, resp, 1, resp.length - 1);

        return ISOUtil.byte2hex(resp);
    }
}
