package com.newpos.libpay.presenter;

import com.android.desert.keyboard.InputManager;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.adaptadores.ModeloMensajeConfirmacion;
import com.cobranzas.inicializacion.configuracioncomercio.Defered;
import com.newpos.libpay.device.user.OnUserResultListener;
import com.newpos.libpay.trans.translog.TransLogData;

import java.util.ArrayList;

/**
 * Created by zhouqiang on 2017/4/25.
 * 交易用户显示接口
 *
 * @author zhouqiang
 * 面向用户的接口
 */

public interface TransView {
    /**
     * 通知上层UI显示刷卡样式
     *
     * @param timeout 超时时间 单位 秒
     * @param mode    输入模式，详见@{@link com.newpos.libpay.device.card.CardManager}
     */
    void showCardView(String msg, int timeout, int mode, String title, long amount, boolean opciones, OnUserResultListener l);


    /**
     * 通知UI显示当前交易读取的卡号供用户确认
     *
     * @param timeout 超时时间
     * @param pan     当前卡号
     * @param l       需要上层通过此接口给底层回调通知 详见@{@link OnUserResultListener}
     */
    void showCardNo(int timeout, String pan, OnUserResultListener l);

    /**
     * @param title      Titulo
     * @param msg        Mensaje
     * @param btnCancel  Boton cancelar
     * @param btnConfirm Boton confirmar
     * @param l          Listener
     */
    void showMessageInfo(String title, String msg, String btnCancel, String btnConfirm, int timeout, OnUserResultListener l);

    /**
     * @param title      Titulo
     * @param msg        Mensaje
     * @param btnCancel  Boton cancelar
     * @param btnConfirm Boton confirmar
     * @param l          Listener
     */
    void showMessageImpresion(String title, String msg, String btnCancel, String btnConfirm, int timeout, OnUserResultListener l);

    /**
     * 获取输入信息
     *
     * @param type 输入类型 @{@link InputManager.Mode}
     * @return 输入结果
     */
    String getInput(InputManager.Mode type);

    /**
     * 通知UI显示交易详细信息，用于消费撤销及预授权完成撤销
     *
     * @param timeout 超时时间
     * @param data    交易详情信息 详见@{@link TransLogData}
     * @param l       需要上层通过此接口给底层回调用户行为 详见@{@link OnUserResultListener}
     */
    void showTransInfoView(int timeout, TransLogData data, OnUserResultListener l);

    /**
     * 通知UI显示卡片多应用
     *
     * @param timeout 超时时间
     * @param apps    应用列表
     * @param l       需要上层通过此接口给底层回调用户行为 详见@{@link OnUserResultListener}
     */
    void showCardAppListView(int timeout, String[] apps, OnUserResultListener l);

    /**
     * 通知UI显示卡片多语言选择
     *
     * @param timeout 超时时间
     * @param langs   语言列表
     * @param l       需要上层通过此接口给底层回调用户行为 详见@{@link OnUserResultListener}
     */
    void showMultiLangView(int timeout, String[] langs, OnUserResultListener l);

    /**
     * 通知UI交易结束成功后续处理
     *
     * @param timeout 超时时间
     * @param info    交易结果详情
     */
    void showSuccess(int timeout, String info);

    /**
     * 通过UI交易结束失败后续处理
     *
     * @param timeout 超时时间
     * @param err     错误详情信息
     */
    void showError(int timeout, String err);

    /**
     * 通知UI显示交易进行到某一状态
     *
     * @param timeout 超时时间
     * @param status  状态信息
     */
    void showMsgInfo(int timeout, String status, boolean transaccion, boolean withClose);

    /**
     * 通知UI显示交易进行到某一状态
     *
     * @param timeout 超时时间
     * @param status  状态信息
     */
    void showMsgInfo(int timeout, String status, String title, boolean transaccion);

    /**
     * @param timeout Timeout
     * @param title   Titulo
     * @param l       Listener
     */
    void showTypeCoinView(int timeout, final String title, OnUserResultListener l);

    /**
     * @param timeout Timeout
     * @param title   Titulo
     * @param label   Mensaje
     * @param l       Listener
     */
    void showInputUser(int timeout, final String title, final String label, int min, int max, OnUserResultListener l);

    /**
     * @param errcode Codigo de error
     */
    void toasTransView(String errcode, boolean sound);

    /**
     * @param timeout Timeout
     * @param title   Titulo
     * @param label   Mensaje
     */
    void showConfirmAmountView(int timeout, final String title, final String label, String amnt, boolean isHTML, OnUserResultListener l);

    /**
     * @param img Imagen
     * @param l   Listener
     */
    void showCardViewImg(String img, OnUserResultListener l);

    /**
     * @param timeout   Timeout
     * @param l         Listener
     * @param title     Titulo
     * @param transType Tipo Transaccion
     */
    void showSignatureView(int timeout, OnUserResultListener l, String title, String transType);

    /**
     * @param timeout   Timeout
     * @param l         Listener
     * @param title     Titulo
     * @param transType Tipo Transaccion
     */
    void showListView(int timeout, OnUserResultListener l, String title, String transType, final ArrayList<String> listMenu, int id);

    /**
     *
     * @param timeout
     * @param transType
     * @param nameAcq
     * @param cls
     * @param l
     */
    //void showInputPromptView(int timeout, String transType, String nameAcq, Prompt cls, OnUserResultListener l);

    /**
     * @param timeout
     * @param l
     * @param title
     * @param transType
     * @param defered
     * @param id
     */
    void showListDeferedView(int timeout, OnUserResultListener l, String title, String transType, ArrayList<Defered> defered, int id);

    void showIngresoDataNumericoView(int timeout, String tipoIngreso, final String mensajeSecundaio, String title, int longitudMaxima, String trx, long amount, OnUserResultListener l);

    void showSeleccionTipoDeCuentaView(int timeout, OnUserResultListener listener);

    void showImprimiendoView(int timeout);

    void showResultView(int timeout, boolean aprobada, boolean isIconoWifi, boolean opciones, String mensajeHost, OnUserResultListener listener);

    void showResultView(int timeout, String emcabezado, boolean aprobada, boolean isIconoWifi, boolean opciones, String mensajeHost, OnUserResultListener listener);

    void showFinishView();

    void showBotonesView(int timeout, String titulo, ArrayList<ModeloBotones> botones, OnUserResultListener listener);

    void showMensajeConfirmacionView(int timeout, ModeloMensajeConfirmacion modelo, OnUserResultListener listener);


    void showContacLessInfoView(boolean finish);

    void showRetireTarjeta();
}
