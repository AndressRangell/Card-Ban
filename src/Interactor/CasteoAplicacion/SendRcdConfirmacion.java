package Interactor.CasteoAplicacion;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.cobranzas.inicializacion.trans_init.trans.SendRcvd.TAG;

public class SendRcdConfirmacion extends AsyncTask<Void, Integer, byte[]> {

    private static final int TIMEOUT = 1;
    private static final int NO_ACCESS_INTERNET = 2;
    private static final int HOST_OFF = 3;

    private int resultTx = 0;

    Context context;
    private InputStream inputStream;
    private OutputStream outputStream;
    Socket clientRequest = null;
    TransPack transPack;
    private String ipHost;
    private int portHost;
    private int timeOut;
    setEventoListener setEventoListener;
    String clase ="SendRcdConfirmacion.java";

    public SendRcdConfirmacion(Context context, TransPack transPack, String ipHost, int portHost, int timeOut, setEventoListener eventoListener) {
        this.context = context;
        this.transPack = transPack;
        this.ipHost = ipHost;
        this.portHost = portHost;
        this.timeOut = timeOut;
        this.setEventoListener = eventoListener;
    }

    public interface setEventoListener {
        void onShowError(String mensaje);

        void onShowSuccess(byte[] rspServidor);
    }

    @Override
    protected byte[] doInBackground(Void... voids) {
        byte[] rxbuf;
        long waitTime;
        byte[] lenIsoRx = new byte[2];
        ByteArrayOutputStream byteOs;
        byteOs = new ByteArrayOutputStream();

        if (!isNetworkAvailable()) {
            Log.e(TAG, "No Internet access...");
            resultTx = NO_ACCESS_INTERNET;
            return null;
        }

        try {

            clientRequest = new Socket();
            clientRequest.setSoTimeout(timeOut);

            clientRequest.connect(new InetSocketAddress(ipHost, portHost), 5000);

            inputStream = clientRequest.getInputStream();
            outputStream = clientRequest.getOutputStream();

            if (clientRequest.isConnected()) {

                while (true) {
                    rxbuf = transPack.packIsoInit();

                    Log.i(TAG, "Sending...");
                    Log.i(TAG, ISOUtil.hexString(rxbuf));
                    outputStream.write(rxbuf);
                    outputStream.flush();

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
                            if ((i = inputStream.read(lenIsoRx)) != -1) {
                                len = ISOUtil.bcdToInt(lenIsoRx);
                                bb = new byte[len + 2];

                                lenpp = len;
                                while (len > 0 && (i = inputStream.read(bb)) != -1) {
                                    total += i;
                                    publishProgress((int) ((total * 100) / lenpp));
                                    byteOs.write(bb, 0, i);
                                    len -= i;
                                }
                                break;
                            }
                        } catch (InterruptedIOException e) {
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
                        rxbuf = byteOs.toByteArray();

                        if (rxbuf == null)
                            break;

                        //Recibe la rx del host
                        Log.i(TAG, "Receiving...");
                        Log.i(TAG, ISOUtil.hexString(rxbuf));

                        Thread.sleep(1000);
                        if (rxbuf != null){
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
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
            Log.e(TAG, "The port of server is closed...");
            resultTx = HOST_OFF;
            return null;

        } finally {

            try {
                if (resultTx != HOST_OFF) {
                    inputStream.close();
                    outputStream.close();
                    clientRequest.close();
                }
            } catch (IOException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                e.printStackTrace();
            }
        }
        return byteOs.toByteArray();
    }


    @Override
    protected void onPostExecute(byte[] iso) {
        super.onPostExecute(iso);
        try {
            validatedMessageError(resultTx);

            setEventoListener.onShowSuccess(iso);
        } catch (Exception exception) {
            Logger.logLine(LogType.EXCEPTION, clase, exception.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, exception.getStackTrace());
            exception.printStackTrace();
        }

    }

    public void validatedMessageError(int msgE) {
        switch (msgE) {
            case TIMEOUT:
                setEventoListener.onShowError( "ERROR, TIEMPO DE ESPERA AGOTADO");
                break;
            case NO_ACCESS_INTERNET:
                setEventoListener.onShowError("ERROR, NO HAY CONEXIÓN A INTERNET");
                break;
            case HOST_OFF:
                setEventoListener.onShowError("ERROR, NO HAY CONEXIÓN CON EL SERVIDOR");
                break;
            default:
                Logger.debug("Case invalid");
                break;
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
