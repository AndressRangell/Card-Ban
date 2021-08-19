package Interactor.CasteoAplicacion;

import android.content.Context;

import com.cobranzas.inicializacion.configuracioncomercio.Tareas;
import com.cobranzas.inicializacion.trans_init.trans.ISO;
import com.google.common.base.Strings;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.config.DevConfig;
import com.wposs.cobranzas.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CasteoTransPackImpl implements TransPack {

    String nii;
    Context context;
    List<Tareas> tareasList;
    String clase = "CasteoTransPacklmpl.java";

    public CasteoTransPackImpl(Context context, List<Tareas> tareasList) {
        this.context = context;
        this.tareasList = tareasList;
    }

    @Override
    public byte[] packIsoInit() {
        nii = context.getResources().getString(R.string.niiConfig);

        ISO iso = new ISO(ISO.lenghtInclude, ISO.TpduInclude);
        iso.setTPDUId("60");
        nii = ISOUtil.padleft(nii + "", 4, '0');
        iso.setTPDUDestination(nii);
        iso.setTPDUSource("0000");

        iso.setMsgType("0800");
        iso.setField(ISO.field_03_PROCESSING_CODE, "510100");
        iso.setField(ISO.field_11_SYSTEMS_TRACE_AUDIT_NUMBER, Strings.padStart(String.valueOf(TMConfig.getInstance().getTraceNo()), 6, '0'));
        iso.setField(ISO.field_41_CARD_ACCEPTOR_TERMINAL_IDENTIFICATION, setTID() );//"20123456"

        String fecha = getCurrentTimeStamp();
        if (tareasList != null && !tareasList.isEmpty()) {
            String campo58 = tareasList.get(0).getTarea() + "," + fecha;
            iso.setField(ISO.field_58_RESERVED_NATIONAL, campo58);
        }
        iso.setField(ISO.field_61_RESERVED_PRIVATE, DevConfig.getSN());

        return iso.getTxnOutput();
    }


    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        return sdfDate.format(now);
    }

    public String setTID() {
        String modelo = "";
        String serial = "";
        try {
            String modeloSerial = DevConfig.getSN(); // 4 digitos iniciales del serial corresponden al modelo
            modelo = modeloSerial.substring(2, modeloSerial.length());
            serial = modelo;
        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
        return serial;
    }
}
