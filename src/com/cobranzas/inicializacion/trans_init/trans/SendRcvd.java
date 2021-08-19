package com.cobranzas.inicializacion.trans_init.trans;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.ISOUtil;
import com.wposs.cobranzas.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.defines_bancard.DefinesBANCARD.ENTRY_POINT;
import static com.cobranzas.defines_bancard.DefinesBANCARD.PROCESSING;
import static com.cobranzas.defines_bancard.DefinesBANCARD.REVOK;
import static com.cobranzas.defines_bancard.DefinesBANCARD.TERMINAL;
import static com.cobranzas.inicializacion.trans_init.Init.APLICACIONES;
import static com.cobranzas.inicializacion.trans_init.Init.CAPKS;
import static com.cobranzas.inicializacion.trans_init.Init.CARDS;
import static com.cobranzas.inicializacion.trans_init.Init.COMERCIOS;
import static com.cobranzas.inicializacion.trans_init.Init.DEVICE;
import static com.cobranzas.inicializacion.trans_init.Init.EMVAPPS;
import static com.cobranzas.inicializacion.trans_init.Init.HOST;
import static com.cobranzas.inicializacion.trans_init.Init.IPS;
import static com.cobranzas.inicializacion.trans_init.Init.InitParcial;
import static com.cobranzas.inicializacion.trans_init.Init.InitTotal;
import static com.cobranzas.inicializacion.trans_init.Init.PLANES;
import static com.cobranzas.inicializacion.trans_init.Init.RED;
import static com.cobranzas.inicializacion.trans_init.Init.SUCURSAL;
import static com.cobranzas.inicializacion.trans_init.Init.tareas;


/**
 * Created by Technology&Solutions on 27/03/2017.
 */

/**
 * Send and wait response from host.
 */
public class SendRcvd extends AsyncTask<Void, Integer, byte[]> {

    static String clase = "SendRcvd.java";
    public static final String TAG = "SendClass";
    private static final String TXT = ".txtT";
    private static final String FIELD_03 = "310100";
    public static final int TIMEOUT = 1;
    public static final int TIMEOUTSOCKET = 4;
    public static final int NO_ACCESS_INTERNET = 2;
    public static final int HOST_OFF = 3;

    private int resultTx = 0;
    private String ipHost;
    private int portHost;
    private int timeOut;
    private InputStream in;
    private OutputStream dis;
    Socket clientRequest = null;
    private Context context;
    private TcpCallback callback;
    private ProgressDialog pd;

    private String pathDefault;
    private String nii;
    private String tid;
    private int tramaQueEnvia;
    private String fileName;
    private String offset;
    String gHashTotal;
    private java.io.File file;
    private byte[] txBuf;
    private byte[] rxBuf;
    private String resultOk;

    private boolean isWithMensaje;

    /**
     * @param ipHost
     * @param portHost
     * @param timeOut
     * @param ctx
     * @param callback
     */
    public SendRcvd(String ipHost, int portHost, int timeOut, Context ctx, final TcpCallback callback) {
        this.callback = callback;
        this.ipHost = ipHost;
        this.portHost = portHost;
        this.timeOut = timeOut;
        this.context = ctx;

        pd = new ProgressDialog(ctx);
    }

    public SendRcvd(String ipHost, int portHost, int timeOut, Context ctx) {
        this.ipHost = ipHost;
        this.portHost = portHost;
        this.timeOut = timeOut;
        this.context = ctx;
        this.isWithMensaje = true;
    }

    public void callbackResponse(final TcpCallback callback) {
        this.callback = callback;
    }

    public String getPathDefault() {
        return pathDefault;
    }

    public void setPathDefault(String pathDefault) {
        this.pathDefault = pathDefault;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public int getTramaQueEnvia() {
        return tramaQueEnvia;
    }

    public void setTramaQueEnvia(int tramaQueEnvia) {
        this.tramaQueEnvia = tramaQueEnvia;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public void setWithMensaje(boolean withMensaje) {
        isWithMensaje = withMensaje;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        this.pd.setCancelable(false);
        this.pd.show();
        this.pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.pd.setContentView(R.layout.progress_inicializacion);
        TextView textView = pd.findViewById(R.id.Texto);
        textView.setText("Inicializando POS");
        mostrarSerialvsVersion(pd);
    }

    private void mostrarSerialvsVersion(ProgressDialog pd) {
        TextView tvVersion = pd.findViewById(R.id.tvVersion);
        TextView tvSerial = pd.findViewById(R.id.tvSerial);
        UIUtils.mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    @Override
    protected byte[] doInBackground(Void... voids) {

        long waitTime;
        byte[] lenIsoRx = new byte[2];
        ByteArrayOutputStream byteOs;
        byteOs = new ByteArrayOutputStream();

        if (!isNetworkAvailable()) {
            resultTx = NO_ACCESS_INTERNET;
            return null;
        }

        try {

            clientRequest = new Socket();
            clientRequest.setSoTimeout(timeOut);

            clientRequest.connect(new InetSocketAddress(ipHost, portHost), 5000);

            in = clientRequest.getInputStream();
            dis = clientRequest.getOutputStream();

            if (clientRequest.isConnected()) {

                offset = "" + calcularOffset(fileName, true);
                while (true) {
                    txBuf = packIsoInit();

                    Log.i(TAG, "Sending...");
                    Log.i(TAG, ISOUtil.hexString(txBuf));
                    dis.write(txBuf);
                    dis.flush();

                    waitTime = System.currentTimeMillis() + this.timeOut;

                    do {
                        if (System.currentTimeMillis() >= waitTime) {
                            resultTx = TIMEOUT;
                            break;
                        }

                        int i;
                        int len;
                        long total = 0;
                        int lenpp = 0;
                        byte[] bb;

                        try {
                            byteOs = new ByteArrayOutputStream();
                            if ((i = in.read(lenIsoRx)) != -1) {
                                len = ISOUtil.bcdToInt(lenIsoRx);
                                bb = new byte[len + 2];

                                lenpp = len;
                                while (len > 0 && (i = in.read(bb)) != -1) {
                                    total += i;
                                    publishProgress((int) ((total * 100) / lenpp));
                                    byteOs.write(bb, 0, i);
                                    len -= i;
                                }
                                break;
                            }
                        } catch (InterruptedIOException e) {
                            // 读取超时处理
                            Log.w("PAY_SDK", "Recive：3 读取流数据超时异常 1");
                            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                            return null;
                        }
                    } while (true);
                    Log.i(TAG, "Connection closing...");

                    if (resultTx == TIMEOUT) {
                        break;
                    }

                    try {
                        rxBuf = byteOs.toByteArray();

                        if (rxBuf == null)
                            break;

                        //Recibe la rx del host
                        Log.i(TAG, "Receiving...");
                        Log.i(TAG, ISOUtil.hexString(rxBuf));

                        if (unpackDescarga(new ISO(rxBuf, ISO.lenghtNotInclude, ISO.TpduInclude))) {
                            ISO rspIso = new ISO(rxBuf, ISO.lenghtNotInclude, ISO.TpduInclude);
                            TMConfig.getInstance().incTraceNo().save();

                            Log.i(TAG, rspIso.GetField(ISO.field_60_RESERVED_PRIVATE));

                            if (rspIso.GetField(ISO.field_03_PROCESSING_CODE).equals("310101")) {
                                offset = "" + calcularOffset(fileName, false);
                                continue;
                            } else if (rspIso.GetField(ISO.field_03_PROCESSING_CODE).equals(FIELD_03)) {
                                resultOk = "OK"; // Finaliza inicializacion con 310100
                                break;
                            }
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        return null;
                    }
                }
            } else {
                Log.e("Clt", "Client no connected..");
                resultTx = HOST_OFF;
            }

        } catch (IOException e) {
            Log.e(TAG, "The port of server is closed...");
            resultTx = mensajeExceptiones(e);
            return null;

        } finally {

            try {
                if (resultTx != HOST_OFF && resultTx != TIMEOUTSOCKET) {
                    in.close();
                    dis.close();
                    clientRequest.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
        return byteOs.toByteArray();
    }

    private int mensajeExceptiones(IOException e) {
        if (e instanceof SocketTimeoutException) {
            return TIMEOUTSOCKET;
        } else if (e instanceof UnknownHostException) {
            return HOST_OFF;
        } else {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return HOST_OFF;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(byte[] iso) {
        super.onPostExecute(iso);
        try {

            //Barra indicadora de progreso
            if (pd != null && pd.isShowing())
                pd.dismiss();
            //Check messages of error
            validatedMessageError(resultTx);
            //call for process the buffer
            callback.rspHost(iso, resultOk);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    /**
     * Check if device have connectivity to internet
     *
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Check if to port of server is open
     *
     * @param ip
     * @param port
     * @param timeout
     * @return
     */
    public static boolean isPortOpen(final String ip, final int port, final int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
            return true;
        } catch (ConnectException ce) {
            ce.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, ce.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, ce.getStackTrace());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return false;
        }
    }

    /**
     * Show messages of error
     *
     * @param msgE
     */
    public void validatedMessageError(int msgE) {
        if (isWithMensaje) {
            switch (msgE) {
                case TIMEOUT:
                    UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, "ERROR, TIEMPO DE ESPERA AGOTADO", Toast.LENGTH_LONG);
                    break;
                case NO_ACCESS_INTERNET:
                    UIUtils.toast((Activity) context, R.drawable.infonetwhite, "ERROR, NO HAY CONEXIÓN A INTERNET", Toast.LENGTH_LONG);
                    break;
                case HOST_OFF:
                    UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, "ERROR, NO HAY CONEXIÓN CON EL SERVIDOR", Toast.LENGTH_LONG);
                    break;
                case TIMEOUTSOCKET:
                    UIUtils.toast((Activity) context, R.drawable.ic_cobranzas_blanca, "TIEMPO DE ESPERA DE RESPUESTA DEL SERVIDOR AGOTADO", Toast.LENGTH_LONG);
                    break;

                default:
                    Logger.debug("Case invalid");
                    break;
            }
        }
    }

    /**
     * In this interface the definition of the onPostExecute
     * method is performed, which receives the
     * response of the request from the WS method.
     */
    public interface TcpCallback {
        void rspHost(byte[] rxBuf, String resultOk);
    }

    //===================== proceso init ==================
    private long calcularOffset(String fileName, boolean deleteFile) {
        long len = 0;
        java.io.File dir = new File(pathDefault);
        file = new File(pathDefault + file.separator + fileName + "T");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (file.exists()) {
            if (deleteFile)
                file.delete();

            len = file.length();

        } else {
            try {
                if (!file.createNewFile()) {
                    Log.d("tag", "create file fail");
                    return -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                return -1;
            }
        }
        return len;
    }

    private String getHash(String fileNameTable, boolean forcedInit) {
        String ret = "NA";
        FileInputStream fileIn = null;
        if (forcedInit == false) {

            File fileToRead = new File(pathDefault + file.separator + fileNameTable);
            try {
                if (fileToRead.exists()) {
                    fileIn = new FileInputStream(fileToRead);
                    InputStreamReader inputRead = new InputStreamReader(fileIn);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    byte[] inputBuffer = new byte[1024];
                    int charRead;

                    while ((charRead = fileIn.read(inputBuffer)) > 0) {
                        bos.write(inputBuffer, 0, charRead);
                    }
                    inputRead.close();
                    bos.close();

                    ret = Tools.hashSha1(bos.toByteArray());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
        return ret;
    }

    private byte[] armarTramaDescarga(String aFileName, String aOffset) {
        String outField60 = null;
        String solicitudBytes = "65000";
        byte tmp[] = null;

        ISO iso = new ISO(ISO.lenghtInclude, ISO.TpduInclude);
        iso.setTPDUId("60");
        iso.setTPDUDestination(nii = ISOUtil.padleft(nii + "", 4, '0'));
        iso.setTPDUSource("0000");

        iso.setMsgType("0800");
        iso.setField(ISO.field_03_PROCESSING_CODE, FIELD_03);
        iso.setField(ISO.field_11_SYSTEMS_TRACE_AUDIT_NUMBER, Strings.padStart(String.valueOf(TMConfig.getInstance().getTraceNo()), 6, '0'));

        outField60 = aFileName + "," + aOffset + "," + solicitudBytes;
        iso.setField(ISO.field_60_RESERVED_PRIVATE, outField60);
        iso.setField(ISO.field_61_RESERVED_PRIVATE, getTid());
        iso.setField(ISO.field_62_RESERVED_PRIVATE, "NA|NA|NA|NA|NA|NA|NA|NA|NA|NA|NA|NA|NA|NA|NA|NA|");

        tmp = iso.getTxnOutput();

        return tmp;
    }

    private byte[] armarTramaDescargaParcial(String aFileName, String aOffset, boolean forcedInit) {
        String outField60 = null;
        String outField61 = null;
        String separator = "|";
        String tidAUX = aFileName.replace(".zip", "");

        String nombreArchivo = null;
        String offset = null;
        String solicitudBytes = "9000";
        byte tmp[] = null;

        nombreArchivo = aFileName;
        offset = aOffset;

        ISO iso = new ISO(ISO.lenghtInclude, ISO.TpduInclude);
        iso.setTPDUId("60");
        iso.setTPDUDestination(nii = ISOUtil.padleft(nii + "", 4, '0'));
        iso.setTPDUSource("0000");

        iso.setMsgType("0800");
        if (offset.equals("0"))
            iso.setField(ISO.field_03_PROCESSING_CODE, FIELD_03);
        else
            iso.setField(ISO.field_03_PROCESSING_CODE, "310101");

        iso.setField(ISO.field_11_SYSTEMS_TRACE_AUDIT_NUMBER, Strings.padStart(String.valueOf(TMConfig.getInstance().getTraceNo()), 6, '0'));
        iso.setField(ISO.field_41_CARD_ACCEPTOR_TERMINAL_IDENTIFICATION, "MED40400");

        outField60 = nombreArchivo + "," + offset + "," + solicitudBytes;
        if (offset.equals("0"))
            iso.setField(ISO.field_60_RESERVED_PRIVATE, "NEWPOS_9220_034.zip,0,20000");
        else
            iso.setField(ISO.field_60_RESERVED_PRIVATE, "NEWPOS_9220_034.zip,20000,20000");

        outField61 = "";

        outField61 += getHash(tidAUX + "_" + RED + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + COMERCIOS + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + SUCURSAL + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + DEVICE + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + CARDS + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + EMVAPPS + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + CAPKS + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + HOST + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + IPS + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + PLANES + TXT, forcedInit) + separator +
                 getHash(tidAUX + "_" + APLICACIONES + TXT, forcedInit) + separator +
                getHash(tidAUX + "_" + tareas + TXT, forcedInit) + separator +

                //CTL Files
                getHash(tidAUX + "_" + ENTRY_POINT + ".bin", forcedInit) + separator +
                getHash(tidAUX + "_" + PROCESSING + ".bin", forcedInit) + separator +
                getHash(tidAUX + "_" + REVOK + ".bin", forcedInit) + separator +
                getHash(tidAUX + "_" + TERMINAL + ".bin", forcedInit) + separator;

        iso.setField(ISO.field_62_RESERVED_PRIVATE, outField61);

        tmp = iso.getTxnOutput();
        return tmp;
    }

    private byte[] packIsoInit() {
        byte[] data = new byte[0];

        switch (tramaQueEnvia) {
            case InitTotal:
                data = armarTramaDescarga(fileName, offset);
                break;
            case InitParcial:
                data = armarTramaDescargaParcial(fileName, offset, false);
                break;
            default:
                Logger.debug("No invalid");
        }
        return data;
    }

    private boolean unpackDescarga(ISO rspTx) {
        String rspCode = rspTx.GetField(ISO.field_39_RESPONSE_CODE);
        String procCode = rspTx.GetField(ISO.field_03_PROCESSING_CODE);
        String field64;
        byte f64[];
        String field60;
        String field61;
        String field62;
        String hashSegmento;

        if (procCode.equals("960080")) {
            field62 = rspTx.GetField(ISO.field_62_RESERVED_PRIVATE);
            hashSegmento = field62.substring(0, field62.indexOf("|"));
            gHashTotal = field62.substring(field62.indexOf("|") + 1);
            f64 = rspTx.GetFieldB(ISO.field_64_MESSAGE_AUTHENTICATION_CODE);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }

            try {
                int len = rspTx.getSizeField(64);
                if (Tools.hashSha1(f64).equals(hashSegmento)) {
                    fileOutputStream.write(f64, 0, rspTx.getSizeField(64));
                }

            } catch (IOException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
            return true;
        } else {
            int len64 = 0;
            if (rspCode.equals("00")) {
                field61 = rspTx.GetField(ISO.field_62_RESERVED_PRIVATE);
                hashSegmento = field61.substring(0, field61.indexOf("|"));
                gHashTotal = field61.substring(field61.indexOf("|") + 1);
                f64 = rspTx.GetFieldB(ISO.field_64_MESSAGE_AUTHENTICATION_CODE);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file, true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                try {
                    int len = rspTx.getSizeField(64);
                    if (Tools.hashSha1(f64).equals(hashSegmento)) {
                        fileOutputStream.write(f64, 0, rspTx.getSizeField(64));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                return true;
            } else if (rspCode.equals("05")) {
                field60 = "ERROR EN LA DESCARGA \n NO EXISTE TERMINAL";
                resultOk = field60;
                return false;
            } else if (rspCode.equals("95")) {
                field60 = rspTx.GetField(ISO.field_60_RESERVED_PRIVATE);
                resultOk = field60;
                return false;
            } else {
                field60 = "Code: " + rspCode + " ERROR DESCONOCIDO";
                resultOk = field60;
                return false;
            }
        }
    }
}


