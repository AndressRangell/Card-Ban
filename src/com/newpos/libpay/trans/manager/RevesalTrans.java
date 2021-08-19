package com.newpos.libpay.trans.manager;

import android.content.Context;
import android.util.Log;

import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;
import com.cobranzas.transactions.echotest.EchoTest;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.pinpad.PinpadManager;
import com.newpos.libpay.presenter.TransUI;
import com.newpos.libpay.trans.Tcode;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.TransInputPara;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;

import static com.cobranzas.defines_bancard.DefinesBANCARD.POLARIS_APP_NAME;

/**
 * 冲正交易实体类
 *
 * @author zhouqiang
 */
public class RevesalTrans extends Trans {

    private final boolean reversoDespuesdeTrans;
    String clase = "RevesalTrans.java";
    int timeOut;

    public RevesalTrans(Context ctx, String transEname, int timeOut) {
        super(ctx, transEname);
        isUseOrgVal = true; // 使用原交易的60.1 60.3
        iso8583.setHasMac(false);
        isTraceNoInc = false; // 冲正不需要自增流水号
        this.timeOut = timeOut;

        /* reversoDespuesdeTrans :
            true : DespuesDeTransaccion
            false: AntesDeTransaccion
        */

        Logger.logLine(LogType.COMUNICACION, clase, "CLASE REVERSAL TRANS ---");

        /*Reverso enviado justo despues de enviar la transaccion y no obtener respuesta*/
        // reversoDespuesdeTrans = true;//ISOUtil.stringToBoolean(comercios.getREVERSO());
        APLICACIONES aplicaciones = APLICACIONES.getSingletonInstanceAppActual(POLARIS_APP_NAME);

        Logger.logLine(LogType.COMUNICACION, clase, "APLICACIONES.getSingletonInstanceAppActua " + aplicaciones);

        if (aplicaciones != null) {
            reversoDespuesdeTrans = aplicaciones.isReverso();
            Logger.logLine(LogType.COMUNICACION, clase, "APLICACIONES.getSingletonInstanceAppActua if " +
                    "reversoDespuesdeTrans :\n" +
                    "            true : DespuesDeTransaccion\n" +
                    "            false: AntesDeTransaccion tipo Reverso Habilitado = >" + reversoDespuesdeTrans);
        } else {
            Logger.logLine(LogType.COMUNICACION, clase, "APLICACIONES.getSingletonInstanceAppActua  else ");
            reversoDespuesdeTrans = true;
        }
    }

    public static boolean isReversalPending() {
        TransLogData revesalData = TransLog.getReversal();
        if (revesalData != null) {
            return true;
        }
        return false;
    }

    private void setFields(TransLogData data) {

        iso8583.setField(0, "0400");

        if (data.getField02() != null) {
            iso8583.setField(2, data.getField02());
        }

        if (data.getProcCode() != null) {
            if (data.getEName().equals(Type.ANULACION)) {
                iso8583.setField(3, "320000");
            } else {
                String procCodeReversal = data.getProcCode();
                switch (procCodeReversal) {
                    case "920000": // REVERSO NORMAL
                        iso8583.setField(3, "000000");
                        break;
                    default:
                        iso8583.setField(3, data.getProcCode());
                        break;
                }
            }
        }

        if (data.getAmount() != null) {
            Log.d(clase, "setFields: data.getAmount()");
            String AmoutData;
            AmoutData = ISOUtil.padleft(data.getAmount() + "", 12, '0');
            iso8583.setField(4, AmoutData);
        }

        if (data.getTraceNo() != null) {
            Log.d(clase, "setFields: data.getTraceNo()");
            iso8583.setField(11, data.getTraceNo());
        }
        if (data.getField19() != null) {
            iso8583.setField(19, data.getField19());
        }
        if (data.getEntryMode() != null) {
            iso8583.setField(22, data.getEntryMode());
        }
        if (data.getPanSeqNo() != null) {
            iso8583.setField(23, data.getPanSeqNo());
        }
        if (data.getNii() != null) {
            iso8583.setField(24, data.getNii());
        }
        if (data.getSvrCode() != null) {
            iso8583.setField(25, data.getSvrCode());
        }
        if (data.getTrack2() != null) {
            iso8583.setField(35, PinpadManager.getInstance().encryptTrack2(data.getTrack2()));
        }
        if (data.getRrn() != null) {
            iso8583.setField(37, data.getRrn());
        }
        if (data.getAuthCode() != null) {
            iso8583.setField(37, data.getAuthCode());
        }
        if (data.getTermID() != null) {
            Log.d(clase, "setFields: data.getTermID()");
            iso8583.setField(41, data.getTermID());
        }
        if (data.getMerchID() != null) {
            iso8583.setField(42, data.getMerchID());
        }
        if (data.getField48() != null) {
            iso8583.setField(48, data.getField48());
        }
        if (data.getCurrencyCode() != null) {
            iso8583.setField(49, data.getCurrencyCode());
        }
        if (data.getPin() != null) {
            iso8583.setField(52, data.getPin());
        }
        /*if (data.getField55() != null){
            iso8583.setField(55, data.getField55());
        }*/
        if (data.getField60() != null) {
            iso8583.setField(60, data.getField60());
        }
        if (data.getField61() != null) {
            iso8583.setField(61, data.getField61());
        }
        if (data.getField62() != null) {
            String hexCargo = ISOUtil.asciiToHex(data.getField62());
            iso8583.setField(62, hexCargo);
        }
        if (data.getField63() != null) {
            iso8583.setField(63, data.getField63());
        }
    }


    private int sendReversal(TransUI transUI) {

        int rtn = Tcode.T_reversal_fail;
        TransLogData data = TransLog.getReversal();

        if (data != null) {
            String procCodeReversal = data.getProcCode();
            Log.d(clase, "sendReversal: procCodeReversal --> " + procCodeReversal);
            switch (procCodeReversal) {

                case "920000": // REVERSO NORMAL
                default:
                    setFields(data);
                    break;
            }
            //setFieldsSale(data);
            rtn = OnLineTrans();
            switch (rtn) {
                case Tcode.T_success:
                    RspCode = iso8583.getfield(39);
                    switch (RspCode) {
                        case "00":
                        case "12":
                        case "25":
                            return rtn;

                        default:
                            data.setRspCode("06");
                            TransLog.saveReversal(data);
                            return Tcode.T_receive_refuse;
                    }
                case Tcode.T_package_mac_err:
                    Logger.logLine(LogType.COMUNICACION, clase, "T_package_mac_err ");
                    data.setRspCode("A0");
                    TransLog.saveReversal(data);
                    break;
                case Tcode.T_receive_err:
                    Logger.logLine(LogType.COMUNICACION, clase, "T_receive_err");
                    data.setRspCode("08");
                    TransLog.saveReversal(data);
                    break;
                case Tcode.T_package_illegal:
                    Logger.logLine(LogType.COMUNICACION, clase, "T_package_illegal");
                    data.setRspCode("08");
                    TransLog.saveReversal(data);
                    break;
                default:
                    Logger.debug("Revesal result :" + rtn);
                    Logger.logLine(LogType.COMUNICACION, clase, "Revesal result :" + rtn);
                    break;
            }
        }

        return rtn;
    }

    /**
     * @return : true/false
     */
    private boolean EchoTestReverso(TransUI transUI) {
        TransInputPara paraEcho = new TransInputPara();
        paraEcho.setTransUI(transUI);
        paraEcho.setTransType(Trans.Type.ECHO_TEST);
        paraEcho.setNeedOnline(true);
        paraEcho.setNeedPass(false);
        paraEcho.setEmvAll(false);

        EchoTest echoTest = new EchoTest(context, Trans.Type.ECHO_TEST, paraEcho, false);
        int rta = echoTest.sendEchoTest();
        Logger.logLine(LogType.COMUNICACION, clase, "sendEchoTest : " + rta);
        if (rta == 0) {
            return true;
        }
        return false;
    }


    /**
     * sendReversal_retries : Asegurarse que exista reverso antes de llamar este metodo.
     *
     * @param transUI : vistas
     * @return - 0 : Reverso evacuado exitosamente
     * - T_envio_fallido_reverso_fail
     * - T_socket_err
     * - T_send_err
     * - T_reversal_fail
     * - T_reversal_fail_EchoOK : Cuando hay comunicacion (comprobada con Echo Test) y no se obtiene respuesta del reverso
     */
    private int sendReversal_retries(TransUI transUI) {
        boolean IsPending = isReversalPending();
        int reintentos = 2;
        int contEcho = 0;

        Logger.logLine(LogType.COMUNICACION, clase, "isReversalPending :" + IsPending);

        if (IsPending) {

            Logger.logLine(LogType.COMUNICACION, clase, "sendReversal_retries : Pendiente");

            for (int i = 0; i < reintentos; i++) {

                transUI.handling(timeOut, "REVERSANDO TRANSACCION", "ENVIANDO REVERSA [ Int " + (i + 1) + " ]");

                Logger.logLine(LogType.COMUNICACION, clase, "sendReversal_retries : Pendiente");

                int rtn = sendReversal(transUI);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    e.printStackTrace();
                }

                switch (rtn) {
                    case Tcode.T_success:
                        return rtn;

                    default:// Tcode.T_socket_err - Tcode.T_send_err: - Tcode.T_receive_err:, y otros...
                        if (transUI != null) {
                            switch (rtn) {
                                case Tcode.T_package_illegal:
                                case Tcode.T_receive_refuse:
                                    break;
                                default:
                                    transUI.toasTrans(rtn, true, true);
                                    break;
                            }

                        }
                        if (EchoTestReverso(transUI)) {
                            Logger.logLine(LogType.COMUNICACION, clase, "Echo Test OK");
                            contEcho++;
                        }
                        if (verificadoIntento(contEcho, reintentos)) {
                            Logger.debug("Verificando reverso con ECHO TEST --> " + rtn);
                            return Tcode.T_reversal_fail_EchoOK;
                        }

                        break;
                }
            }

        }

        return Tcode.T_envio_fallido_reverso_fail;
    }

    boolean verificadoIntento(int contEcho, int reintentos) {
        if (contEcho == reintentos) {
            Logger.debug("verificadoIntento intento Echotest --- " + contEcho + " TransReversal -- " + reintentos);
            Logger.logLine(LogType.COMUNICACION, clase, "verificadoIntento intento Echotest " + contEcho + " TransReversal -- " + reintentos);
            return true;
        }
        return false;
    }

    public int analizarReversoDespuesDe(TransUI transUI) {
        boolean IsPending = isReversalPending();
        Logger.logLine(LogType.COMUNICACION, clase, "analizarReversoDespuesDe : ");
        if (IsPending) {
            Logger.debug("Reverso Pendiente ");

            if (reversoDespuesdeTrans) {// Analisis despues de enviar transaccion
                Logger.logLine(LogType.COMUNICACION, clase, "reversoDespuesdeTrans == true");
                Logger.debug("reversoDespuesdeTrans == true");
                try {
                    int rtn = sendReversal_retries(transUI);
                    Logger.logLine(LogType.COMUNICACION, clase, "sendReversal_retries : " + rtn);

                    switch (rtn) {
                        case Tcode.T_success:
                            TransLog.clearReveral();
                            Logger.logLine(LogType.COMUNICACION, clase, "sendReversal_retries  if : " + rtn);
                            transUI.showError(timeout, "REVERSA APROBADA", Tcode.T_envio_fallido_reverso_ok, false, true);
                            return Tcode.T_success;


                        default:
                            if (transUI != null) {
                                switch (rtn) {
                                    case Tcode.T_envio_fallido_reverso_fail:
                                    case Tcode.T_reversal_fail_EchoOK:
                                        break;
                                    default:
                                        transUI.toasTrans(rtn, true, true);
                                        break;
                                }

                            }
                            Logger.logLine(LogType.COMUNICACION, clase, "sendReversal_retries else : " + rtn);
                            transUI.showError(timeout, "REVERSA PENDIENTE", Tcode.T_envio_fallido_reverso_fail, false, false);
                            return Tcode.T_socket_err;

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    Logger.debug("Exception " + e.getMessage());
                    return Tcode.T_envio_fallido_reverso_fail;
                }


            } else {
                transUI.showError(timeout, "REVERSA PENDIENTE", Tcode.T_envio_fallido_reverso_fail, false, false);
                return Tcode.T_envio_fallido_reverso_fail;
            }//else : NO ENVIAR, configurado como antes de Trans
        }// else : NO Hay Reverso Pendiente

        return Tcode.T_success;
    }

    /**
     * @param transUI : vistas
     * @return - 0 :
     * Reverso evacuado exitosamente
     * No Existe reverso para evacuacion
     * - T_envio_fallido_reverso_fail
     * - T_socket_err
     * - T_send_err
     * - T_reversal_fail
     */
    public int analizarReversoAntesDe(TransUI transUI) {

        boolean IsPending = isReversalPending();
        Logger.logLine(LogType.COMUNICACION, clase, "analizarReversoAntesDe : ");
        Logger.debug("analizarReversoAntesDe : ");

        if (IsPending) {
            Logger.logLine(LogType.COMUNICACION, clase, "Reverso Pendiente ");
            Logger.debug("Reverso Pendiente ");
            //RevesalTrans reversalTrans = new RevesalTrans(context, "REVERSAL");
            try {
                int rtn = sendReversal_retries(transUI);

                switch (rtn) {
                    case Tcode.T_success:
                        Logger.logLine(LogType.COMUNICACION, clase, "sendReversal : if");
                        TransLog.clearReveral();
                        transUI.toasTrans(Tcode.Status.rev_receive_ok, true, false);
                        break;

                    case Tcode.T_reversal_fail_EchoOK:
                        Logger.debug("rtn :" + rtn);
                        Logger.logLine(LogType.COMUNICACION, clase, "rtn : " + rtn);
                        deleteReversal_by_EchoTestOk(transUI);
                        break;

                    default:
                        if (transUI != null && rtn != Tcode.T_envio_fallido_reverso_fail) {
                            transUI.toasTrans(rtn, true, true);
                        }
                        return rtn;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Logger.debug("Exception " + e.getMessage());
                return Tcode.T_envio_fallido_reverso_fail;
            }

        }
        return Tcode.T_success;
    }


    /**
     * deleteReversal_by_EchoTestOk
     *
     * @param transUI : Vistas
     * @return : Tcode.T_success
     */
    private void deleteReversal_by_EchoTestOk(TransUI transUI) {

        Logger.logLine(LogType.COMUNICACION, clase, "deleteReversal_by_EchoTestOk");
        Logger.debug("deleteReversal_by_EchoTestOk");
        transUI.toasTrans(Tcode.Status.revEerso_borrado_localmente, true, false);
        TransLog.clearReveral();
    }


}
