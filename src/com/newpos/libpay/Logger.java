package com.newpos.libpay;

import static com.cobranzas.defines_bancard.DefinesBANCARD.POLARIS_APP_NAME;
import static com.newpos.libpay.LogType.ERROR;
import static com.newpos.libpay.LogType.EXCEPTION;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.PAYUtils;
import com.wposs.cobranzas.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by zhouqiang on 2017/3/8.
 *
 * @author zhouqiang
 * sdk全局日主输出
 */

public class Logger extends AsyncTask<Void, String, Void> {

    public static final String TAG = "PAYSDK";
    public static final String CLASE_ACTUAL = "Logger.java";

    private static final String ERROR_MAX_PESO = "Error: El archivo de logs no se puede seguir modificando, maximo tamaño excedido";
    private static final String FILE_SEPARATOR = "/";

    FileWriter fileWriter;
    BufferedWriter bufferedWriter;
    File file;
    LogType logType;
    String mensaje;

    private File rutaSd = Environment.getExternalStorageDirectory();
    private int opc = 0;

    private Logger(LogType logType, String mensaje) {
        this.logType = logType;
        this.mensaje = mensaje;
    }

    private static void getInstanceClaseLogger(LogType logType, String msg) {
        Logger claseLogger = new Logger(logType, msg);
        claseLogger.execute();
    }

    public static void debug(String msg) {
        if (TMConfig.getInstance().isDebug()) {
            Log.i(TAG, msg);
        }
    }

    public static void error(String msg) {
        Log.e(TAG, msg);
    }

    public static void logLine(LogType logType, String clase, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg + "\n");
        sb.append("--");
        getInstanceClaseLogger(logType, clase + ": " + sb.toString());
    }

    public static void logLine(LogType logType, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg + "\n");
        sb.append("--");
        getInstanceClaseLogger(logType, sb.toString());
    }

    public static void logLine(LogType logType, String clase, Exception exception) {
        final StackTraceElement[] stackTrace = exception.getStackTrace();
        logLine(logType, clase, stackTrace);
    }

    public static void logLine(LogType logType, StackTraceElement[] msg) {
        logLine(logType, null, msg);
    }

    public static void logLine(LogType logType, String clase, StackTraceElement[] traceElements) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement traceElement : traceElements) {
            sb.append("Nom del archivo: " + traceElement.getFileName() + "\n");
            sb.append("Num de la linea: " + traceElement.getLineNumber() + "\n");
            sb.append("Nomb del metodo: " + traceElement.getMethodName() + "\n");
            sb.append("--" + "\n");
        }
        if (clase == null)
            getInstanceClaseLogger(logType, sb.toString());
        else
            getInstanceClaseLogger(logType, clase + ": " + sb.toString());
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            APLICACIONES aplicaciones = APLICACIONES.getSingletonInstanceAppActual(POLARIS_APP_NAME);
            if (aplicaciones != null) {
                boolean isLog = false;
                switch (logType) {
                    case ERROR:
                    case FLUJO:
                        isLog = aplicaciones.isLogFlujo();
                        break;
                    case EXCEPTION:
                        isLog = aplicaciones.isLogExcepciones();
                        break;
                    case COMUNICACION:
                        isLog = aplicaciones.isLogComunicacion();
                        break;
                    case TIMER:
                        isLog = aplicaciones.isLogTimer();
                        break;
                    case PRINT:
                        isLog = aplicaciones.isLogImpresion();
                        break;
                    case CONSOLE:
                        isLog = true;
                        break;
                    default:
                        break;
                }
                Log.i(logType.name(), mensaje);
                if (isLog) writeLog(BuildConfig.VERSION_NAME, logType.name() + ": " + mensaje);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void validarPeso(File fichero) {
        float longitud = fichero.length();
        if (longitud > 5120000) {
            opc = 1;
        } else {
            opc = 0;
        }
    }

    private void writeLog(String carpeta, String msg) {
        crearCarpeta(carpeta);
        String date = PAYUtils.getLocalDate();
        String time = PAYUtils.getLocalTime();


        String nomArchivo = "logsdata" + date + ".txt";
        file = new File(rutaSd.getAbsolutePath() + "/LogsWposs" + FILE_SEPARATOR +
                POLARIS_APP_NAME + FILE_SEPARATOR + carpeta + FILE_SEPARATOR + nomArchivo);
        validarPeso(file);

        if (file.exists() && opc == 0) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                logLine(EXCEPTION, CLASE_ACTUAL, e.getMessage());
                logLine(EXCEPTION, CLASE_ACTUAL, e.getStackTrace());
            }
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                StringBuilder archivo = new StringBuilder();
                for (String leer; (leer = bufferedReader.readLine()) != null; ) {
                    archivo.append(leer);
                    archivo.append("\n");
                }
                fileWriter = new FileWriter(file);
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(archivo.toString());
                bufferedWriter.append(time).append(" - ").append(msg);
                bufferedWriter.newLine();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                logLine(EXCEPTION, CLASE_ACTUAL, e.getMessage());
                logLine(EXCEPTION, CLASE_ACTUAL, e.getStackTrace());
            }
        } else if (file.exists() && opc == 1) {
            logLine(ERROR, CLASE_ACTUAL, ERROR_MAX_PESO);
        } else {
            try {
                fileWriter = new FileWriter(file);
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.newLine();
                bufferedWriter.append(time).append(" - ").append(msg);
                bufferedWriter.newLine();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                logLine(EXCEPTION, CLASE_ACTUAL, e.getMessage());
                logLine(EXCEPTION, CLASE_ACTUAL, e.getStackTrace());
            }
        }
    }

    private File crearCarpeta(String nombreDirectorio) {
        File carpeta = new File(rutaSd.getAbsolutePath() + "/LogsWposs" +
                FILE_SEPARATOR + POLARIS_APP_NAME + FILE_SEPARATOR + nombreDirectorio);
        carpeta.mkdirs();
        if (!carpeta.exists()) {
            logLine(ERROR, CLASE_ACTUAL, "Error: No se creo la carpeta");
        }
        return carpeta;
    }
}
