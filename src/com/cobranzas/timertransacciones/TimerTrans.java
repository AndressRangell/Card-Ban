package com.cobranzas.timertransacciones;

import android.os.CountDownTimer;
import android.util.Log;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

public class TimerTrans extends CountDownTimer {

    static TimerTrans timerTrans;
    static final String NOMBRE_CLASE = "TimerTrans.java";
    int timeout;
    String mensaje;
    String metodo;
    static OnResultTimer resultTimer;

    public TimerTrans(int timeout, final String mensaje, final String metodo) {
        super(timeout, 5000);
        this.timeout = timeout;
        this.mensaje = mensaje;
        this.metodo = metodo;

    }

    public interface OnResultTimer {
        void rsp2Timer();
    }


    @Override
    public void onTick(long millisUntilFinished) {
        Log.e("onTick", "init onTick countDownTimer <<< " + mensaje + ">>> Metodo -> "+metodo+" Time : " + millisUntilFinished);
    }

    @Override
    public void onFinish() {
        timerTrans.cancel();
        Log.e("Mensaje ", "counterDownTimer: " + " Finalizado ");

        resultTimer.rsp2Timer();

    }

    public static void deleteTimer() {
        if (timerTrans != null) {
            try {
                timerTrans.finalize();
            } catch (Throwable throwable) {
                Logger.logLine(LogType.EXCEPTION,NOMBRE_CLASE, throwable.getMessage());
                Logger.logLine(LogType.EXCEPTION,NOMBRE_CLASE, throwable.getStackTrace());
                throwable.printStackTrace();
            }
            timerTrans.cancel();
            Log.e(NOMBRE_CLASE, "<<< clase TimerTrans  >>> : Se elimna  Timer del  ->  ( Metodo : " + timerTrans.metodo + " ) se Cancela ");
        }
    }


    public static void setResultTimer(OnResultTimer resultTimer) {
        TimerTrans.resultTimer = resultTimer;
    }

    public static void getInstanceTimerTrans(int timeout, String mensaje, String metodo, OnResultTimer resultTimer) {
        setResultTimer(resultTimer);

        if (timerTrans != null) {
            timerTrans.cancel();
            Log.e(NOMBRE_CLASE, "<<< clase TimerTrans  >>> : Se detecto un Timer ->  ( Metodo : " + timerTrans.metodo + " ) se Cancela ");
            timerTrans = null;
        }
        timerTrans = new TimerTrans(timeout, mensaje, metodo);
        timerTrans.start();
        Log.e(NOMBRE_CLASE, "<<< clase TimerTrans >>> : Se Crea  un Nuevo Timer ->  ( Metodo : " + metodo + " ) ");
    }
}
