package Interactor.Utilidades;

import android.content.Context;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FechaImpl extends Fecha {
    Context context;
    String clase ="FechaImpl.java";

    public FechaImpl(Context context) {
        this.context = context;
    }


    @Override
    public boolean detectarNuevoDia(String fechaIncial, String dia, String hora, String tiempo) throws Exception {

        if ((fechaIncial == null && fechaIncial.isEmpty()) && (dia == null && dia.isEmpty()) && (hora != null && hora.isEmpty())) {
            return false;
        }

        try{
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date = dateFormat.format(new Date());

            Date fechaIni = dateFormat.parse(fechaIncial);
            Date fechaFin = dateFormat.parse(date);

        }catch (Exception e){
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            throw e;
        }
        return false;
    }
}
