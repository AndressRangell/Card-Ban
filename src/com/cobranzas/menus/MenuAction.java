package com.cobranzas.menus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.widget.Toast;

import com.cobranzas.actividades.InfoActivity;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.trans_init.Init;
import com.cobranzas.tools_bacth.ToolsBatch;
import com.cobranzas.transactions.callbacks.makeInitCallback;
import com.cobranzas.transactions.callbacks.waitPrintReport;
import com.cobranzas.transactions.callbacks.waitSeatleReport;
import com.newpos.libpay.device.printer.PrintRes;
import com.wposs.cobranzas.R;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.MasterControl;
import cn.desert.newpos.payui.transrecord.HistoryTrans;

import static com.newpos.libpay.trans.Trans.idLote;

public class MenuAction {

    public static final String JUMP_KEY = "JUMP_KEY";
    public static final String CONFIG_TYPE = "";
    //Claves para cuando no esta inicializado el POS
    static final String TERMINAL_PWD = "123456";
    public static waitPrintReport callbackPrint;
    public static waitSeatleReport callBackSeatle;
    public static makeInitCallback makeInitCallback;
    Dialog mDialog;
    private Context context;
    private String tipoDeMenu;


    MenuAction(Context context, String tipoDeMenu) {
        this.context = context;
        this.tipoDeMenu = tipoDeMenu;
        MasterControl.setMcontext(context);
    }

    void selectAction() {
        Intent intent = new Intent();
        switch (tipoDeMenu) {
            case DefinesBANCARD.ITEM_REPORTE:
                intent = new Intent(context, HistoryTrans.class);
                intent.putExtra(HistoryTrans.EVENTS, HistoryTrans.COMMON);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_ECHO_TEST:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[20]);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_REIMPRESION:
                intent = new Intent(context, HistoryTrans.class);
                intent.putExtra(HistoryTrans.EVENTS, HistoryTrans.LAST);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_VENTA_TARJETA:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[21]);
                intent.putExtra(MasterControl.CODIGO, DefinesBANCARD.ITEM_VENTA_TARJETA);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_VENTA_CON_VUELTO:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[21]);
                intent.putExtra(MasterControl.CODIGO, DefinesBANCARD.ITEM_VENTA_CON_VUELTO);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_VENTA_SIN_CONTACTO:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[21]);
                intent.putExtra(MasterControl.CODIGO, DefinesBANCARD.ITEM_VENTA_SIN_CONTACTO);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_CUOTAS_TARJETA:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[22]);
                intent.putExtra(MasterControl.CODIGO, DefinesBANCARD.ITEM_CUOTAS_TARJETA);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_CUOTAS_SERVICIOS:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[22]);
                intent.putExtra(MasterControl.CODIGO, DefinesBANCARD.ITEM_CUOTAS_SERVICIOS);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_CUOTAS_SIN_CONTACTO:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[22]);
                intent.putExtra(MasterControl.CODIGO, DefinesBANCARD.ITEM_CUOTAS_SIN_CONTACTO);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.CONFI_INFO:
                intent.setClass(context, InfoActivity.class);
                intent.putExtra("menu", tipoDeMenu);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_DIFERIDO:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[26]);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_ANULACION:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, MasterControl.class);
                intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[22]);
                context.startActivity(intent);
                break;
            case DefinesBANCARD.ITEM_COMUNICACION:
            case DefinesBANCARD.ITEM_BORRAR_LOTE:
            case DefinesBANCARD.ITEM_BORRAR_REVERSO:
            case DefinesBANCARD.ITEM_PARAMETROS:
            case DefinesBANCARD.ITEM_DEFERED:
            case DefinesBANCARD.MENU_REPORTE_TESTPOS:
                maintainPwd();
                break;
            case DefinesBANCARD.MENU_REPORTE_REIMPRESION:
                if (ToolsBatch.statusTrans(idLote)) {
                    intent = new Intent(context, HistoryTrans.class);
                    intent.putExtra(HistoryTrans.EVENTS, HistoryTrans.COMMON);
                    context.startActivity(intent);
                } else {
                    UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, DefinesBANCARD.LOTE_VACIO, Toast.LENGTH_LONG);
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 500);
                }
                break;
            case DefinesBANCARD.ITEM_POLARIS:
                if (!ToolsBatch.statusTrans(idLote)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(context, Init.class);
                    context.startActivity(intent);
                } else {

                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 500);
                }
                break;
            default:
                intent.setClass(context, menus.class);
                intent.putExtra(DefinesBANCARD.DATO_MENU, tipoDeMenu);
                context.startActivity(intent);
                break;
        }
    }

    private void maintainPwd() {
        mDialog = UIUtils.centerDialog(context, R.layout.setting_home_pass, R.id.setting_pass_layout);
    }
}
