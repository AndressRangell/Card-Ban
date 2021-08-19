package com.newpos.libpay.presenter;

import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputManager;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.adaptadores.ModeloMensajeConfirmacion;
import com.newpos.libpay.device.card.CardInfo;
import com.newpos.libpay.device.pinpad.OfflineRSA;
import com.newpos.libpay.device.pinpad.PinInfo;
import com.newpos.libpay.trans.translog.TransLogData;

import java.util.ArrayList;

/**
 * Created by zhouqiang on 2017/3/15.
 *
 * @author zhouqiang
 * 交易UI接口类
 */

public interface TransUI {
    /**
     * 获取外界输入UI接口(提示用户输入信息)
     *
     * @return return
     */
    InputInfo getOutsideInput(int timeout, InputManager.Mode type, String title, String trx, long amount);

    /**
     * 获取外界卡片UI接口(提示用户用卡)
     *
     * @return return
     */
    CardInfo getCardUse(String msg, int timeout, int mode, String title, long amount, boolean opciones);

    /**
     * 获取外界卡片UI接口(提示用户用卡)
     *
     * @return return
     */

    CardInfo getCardUse(String msg, int timeout, int mode);

    /**
     * @param msg
     * @param timeout
     * @param mode
     * @param title
     * @return
     */
    CardInfo getCardFallback(String msg, int timeout, int mode, String title, long amount);


    /**
     * 获取密码键盘输入联机PIN
     *
     * @param timeout Timeout
     * @param amount  Monto
     * @param cardNo  Numero Tarjeta
     */
    PinInfo getPinpadOnlinePin(int timeout, String amount, String cardNo);

    /**
     * @param timeout
     * @param amount
     * @param cardNo
     * @return
     */
    PinInfo getPinpadOnlinePinDUKPT(int timeout, String amount, String cardNo);

    /**
     * @param timeout       Timeout
     * @param i             Listener
     * @param key           Key
     * @param offlinecounts Contador
     * @return return
     */
    PinInfo getPinpadOfflinePin(int timeout, int i, OfflineRSA key, int offlinecounts);

    /**
     * 人机交互显示UI接口(卡号确认)
     *
     * @param cn 卡号
     */
    int showCardConfirm(int timeout, String cn);

    /**
     * @param msg        Mensaje
     * @param btnCancel  Boton cancel
     * @param btnConfirm boton confirmar
     * @return return
     */
    InputInfo showMessageInfo(String title, String msg, String btnCancel, String btnConfirm, int timeout);

    /**
     * @param msg        Mensaje
     * @param btnCancel  Boton cancel
     * @param btnConfirm boton confirmar
     * @return return
     */
    InputInfo showMessageImpresion(String title, String msg, String btnCancel, String btnConfirm, int timeout);

    /**
     * 人机交互显示UI接口(多应用卡片选择)
     *
     * @param timeout Timeout
     * @param list    Lista
     * @return return
     */
    int showCardApplist(int timeout, String[] list);

    /**
     * 人机交互显示UI接口（多语言选择接口）
     *
     * @param timeout Timeout
     * @param langs   Lenguajes
     * @return return
     */
    int showMultiLangs(int timeout, String[] langs);

    /**
     * 人机交互显示UI接口(耗时处理操作)
     *
     * @param timeout Timeout
     * @param status  TransStatus 状态标志以获取详细错误信息
     */
    void handling(int timeout, int status);

    /**
     * 人机交互显示UI接口(耗时处理操作)
     *
     * @param timeout Timeout
     * @param status  TransStatus 状态标志以获取详细错误信息
     */
    void handling(int timeout, int status, String title);


    /**
     * 人机交互显示UI接口(耗时处理操作)
     *
     * @param timeout Timeout
     * @param mensaje TransStatus 状态标志以获取详细错误信息
     */
    void handling(int timeout, String mensaje, String title);


    /**
     * 人机交互显示UI接口
     *
     * @param timeout Timeout
     * @param logData 详细交易日志
     */
    int showTransInfo(int timeout, TransLogData logData);

    /**
     * 交易成功处理结果
     *
     * @param code Codigo
     */
    void trannSuccess(int timeout, int code, String... args);

    /**
     * 人机交互显示UI接口(显示交易出错错误信息)
     *
     * @param errcode 实际代码错误返回码
     */
    void showError(int timeout, int errcode, boolean isIconoWfi);


    /**
     * 人机交互显示UI接口(显示交易出错错误信息)
     *
     * @param errcode 实际代码错误返回码
     */
    void showError(int timeout, String encabezado, int errcode, boolean isIconoWfi, boolean aprobado);


    /**
     * 人机交互显示UI接口(显示交易出错错误信息)
     *
     * @param errcode 实际代码错误返回码
     */
    void showError(int timeout, String encabezado, String errcode, boolean isIconoWfi, boolean aprobado);

    /**
     * @param timeout Timeout
     * @param title   Titulo
     * @return return
     */
    InputInfo showTypeCoin(int timeout, final String title);

    /**
     * @param timeout Timeout
     * @param title   Titulo
     * @return return
     */
    InputInfo showInputUser(int timeout, final String title, final String label2, int min, int max);

    /**
     * @param errcode Error code
     */
    void toasTrans(int errcode, boolean sound, boolean isErr);

    void toasTrans(String errcode, boolean sound, boolean isErr);

    /**
     * @param message Mensaje
     */
    void showMessage(String message, boolean transaccion);

    /**
     * @param img Imagen
     */
    void showCardImg(String img);

    /**
     * @param timeout   Timeout
     * @param title     Titulo
     * @param transType Tipo Transaccion
     * @return return
     */
    InputInfo showSignature(int timeout, String title, String transType);

    /**
     * @param timeout   Timeout
     * @param title     Titulo
     * @param transType Tipo Transaccion
     * @return return
     */
    InputInfo showIngresoDataNumerico(int timeout, String tipoIngreso, String title, int longitudMaxima, String trx, long amount);

    InputInfo showIngresoDataNumerico(int timeout, String tipoIngreso, String mensaje, String title, int longitudMaxima, String trx, long amount);

    InputInfo showSeleccionTipoDeCuenta(int timeout);

    void showImprimiendo(int timeout);

    InputInfo showResult(int timeout, boolean aprobada, boolean isIconoWifi, boolean opciones, String mensajeHost);

    void showFinish();

    InputInfo showBotones(int timeout, String titulo, ArrayList<ModeloBotones> cuentas);

    InputInfo showMensajeConfirmacion(int timeout, ModeloMensajeConfirmacion modelo);

    void showContacLessInfo(boolean finish);
}
