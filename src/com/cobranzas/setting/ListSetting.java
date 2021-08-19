package com.cobranzas.setting;

import android.content.Context;

import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.Device;
import com.cobranzas.model.ModelSetting;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.List;

import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;

public class ListSetting {

    private static List<ModelSetting> modelSettings;
    private static List<ModelSetting> listadoInicializacion;
    static List<ModeloBotones> modePlansList;
    private static List<ModeloBotones> modeListTecnico;

    private ListSetting() {
    }

    public static List<ModelSetting> getInstanceListMenus(Context context) {

        modelSettings = new ArrayList<>();
        modelSettings.add(new ModelSetting("01", DefinesBANCARD.SETTING_CONFIGURACIONES, armarMenuConfiguraciones(context)));
        modelSettings.add(new ModelSetting("02", DefinesBANCARD.SETTING_TEST, armarMenuTest(context)));
        return modelSettings;
    }

    public static List<ModeloBotones> getInstanceListadoTecnico(Context context) {
        if (modeListTecnico == null) {
            modeListTecnico = new ArrayList<>();
            modeListTecnico.add(new ModeloBotones("1", DefinesBANCARD.ITEM_CONFIG_RED, context.getDrawable(R.drawable.ic__cierre)));
            modeListTecnico.add(new ModeloBotones("5", DefinesBANCARD.ITEM_ELIMINAR_LLAVES, context.getDrawable(R.drawable.config)));
            modeListTecnico.add(new ModeloBotones("3", DefinesBANCARD.DUKPT, context.getDrawable(R.drawable.ic_candado)));
            modeListTecnico.add(new ModeloBotones("4", DefinesBANCARD.MK, context.getDrawable(R.drawable.ic_candado)));
            modeListTecnico.add(new ModeloBotones("5", DefinesBANCARD.ITEM_DETALLE_INICIALIZACION, context.getDrawable(R.drawable.detallesinicializacion)));
            modeListTecnico.add(new ModeloBotones("6", DefinesBANCARD.ITEM_LIMPIAR_DATOS, context.getDrawable(R.drawable.ic_limpiar_datos)));
        }
        return modeListTecnico;
    }

    private static List<ModeloBotones> armarMenuTest(Context context) {
        List<ModeloBotones> modePlansList = new ArrayList<>();
        modePlansList.add(new ModeloBotones("8", DefinesBANCARD.ITEM_ECHO_TEST, context.getDrawable(R.drawable.ic_echo)));
        modePlansList.add(new ModeloBotones("9", DefinesBANCARD.ITEM_WIFI, context.getDrawable(R.drawable.ic_wifi)));
        TransLogData revesalData = TransLog.getReversal();
        if (revesalData != null) {
            modePlansList.add(new ModeloBotones("11", DefinesBANCARD.ITEM_REVERSAL, context.getDrawable(R.drawable.ic_tarjeta)));
        }        return modePlansList;
    }

    private static List<ModeloBotones> armarMenuConfiguraciones(Context context) {
        List<ModeloBotones> modePlansList = new ArrayList<>();
        modePlansList.add(new ModeloBotones("5", DefinesBANCARD.ITEM_CONFIG_COMERCIO, context.getDrawable(R.drawable.config)));
        modePlansList.add(new ModeloBotones("6", DefinesBANCARD.ITEM_CONFIG_TECNICO, context.getDrawable(R.drawable.config)));
        modePlansList.add(new ModeloBotones("7", DefinesBANCARD.ITEM_INICIALIZACION, context.getDrawable(R.drawable.ic_inicializacion)));
        return modePlansList;
    }

    public static List<ModelSetting> getInstanceListDetalles() {
        if (listadoInicializacion == null) {
            listadoInicializacion = new ArrayList<>();
            listadoInicializacion.add(new ModelSetting("01", DefinesBANCARD.DETALLES_COMERCIOS, armaComercios()));
            listadoInicializacion.add(new ModelSetting("02", DefinesBANCARD.DETALLES_DEVICE, armarDevices()));
        }
        return listadoInicializacion;
    }

    private static List<ModeloBotones> armarDevices() {
        List<ModeloBotones> listadoDevices = new ArrayList<>();
        listadoDevices.add(new ModeloBotones("IDENTIFICADOR :", Device.getDeviceIdentifier()));
        listadoDevices.add(new ModeloBotones("DESCRIPCION :", Device.getDeviceDescription()));
        listadoDevices.add(new ModeloBotones("CAJA :", String.valueOf(Device.getCajaPOS())));
        listadoDevices.add(new ModeloBotones("NUMERO CAJA :", Device.getNumeroCajas()));
        listadoDevices.add(new ModeloBotones("NUMERO SERIAL :", Device.getNumSerial()));
        listadoDevices.add(new ModeloBotones("PRIORIDAD :", Device.getPrioridad()));
        listadoDevices.add(new ModeloBotones("ESTADO :", Device.getEstado()));
        listadoDevices.add(new ModeloBotones("VERSION SOFTWARE :", Device.getVersionSoftware()));
        listadoDevices.add(new ModeloBotones("FECHA ULTIMO \nECHO :", Device.getFechaUltimoEcho()));
        listadoDevices.add(new ModeloBotones("FECHA ULTIMA \nTRANSACCION :", Device.getFechaUltimaTransaccion()));
        listadoDevices.add(new ModeloBotones("GRUPO :", Device.getGrupo()));
        listadoDevices.add(new ModeloBotones("FECHA ALTA :", Device.getFechaAlta()));
        listadoDevices.add(new ModeloBotones("FECHA ULTIMA \nACTUALIZACION :", Device.getFechaUltimaActualizacion()));
        listadoDevices.add(new ModeloBotones("USUARIO ULTIMA \nACTUALIZACION :", Device.getUsuarioUltimoActualizacion()));
        return listadoDevices;
    }

    private static List<ModeloBotones> armaComercios() {
        List<ModeloBotones> listadoComercios = new ArrayList<>();
        listadoComercios.add(new ModeloBotones("CATEGORIA :", tablaComercios.getCategoria()));
        listadoComercios.add(new ModeloBotones("DESCRIPCION :", tablaComercios.getMerchantDescription()));
        listadoComercios.add(new ModeloBotones("HABILITA_FIRMA :", String.valueOf(tablaComercios.isHabilitaFirma())));
        listadoComercios.add(new ModeloBotones("PERFIL :", tablaComercios.getPerfil()));
        listadoComercios.add(new ModeloBotones("TIPO :", tablaComercios.getTipo()));
        listadoComercios.add(new ModeloBotones("FECHA HORA_ALTA :", tablaComercios.getFechaHoraAlta()));
        listadoComercios.add(new ModeloBotones("FECHA HORA \n ULTIMA ACTUALIZACION :", tablaComercios.getFechaHoraUltimaActualizacion()));
        listadoComercios.add(new ModeloBotones("USUARIO ULTIMA \n ACTUALIZACION :", tablaComercios.getUsuarioUltimaActualizacion()));
        return listadoComercios;
    }

    public static void setModelSettingsNull() {
        ListSetting.modelSettings = null;
        ListSetting.listadoInicializacion = null;
        ListSetting.modePlansList = null;
        ListSetting.modeListTecnico = null;
    }
}
