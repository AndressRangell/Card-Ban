package com.newpos.libpay.helper.ssl;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;
import com.wposs.cobranzas.R;

import org.jpos.iso.ISOException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


/**
 * 网络助手类
 *
 * @author zhouqiang
 */
public class NetworkHelper {

    private Socket socket;//SSLSocket对象
    private InputStream is; // 输入流
    private OutputStream os; // 输出流
    private String ip;//连接IP地址
    private int port;//连接端口号
    String clase = "NetworkHelper.java";
    private Context tcontext;//上下文对象
    private int timeout; //超时时间
    private int timeoutCon;
    private int timeoutRes;
    private boolean isTls;
    private int protocol; // 协议 0: 2字节长度+数据 1:stx协议
    private final String CLIENT_KEY_MANAGER = "X509"; // 密钥管理器
    private final String CLIENT_AGREEMENT = "TLSv1.2"; // 使用协议
    private final String CLIENT_KEY_KEYSTORE = "BKS"; // "JKS";//密库，这里用的是BouncyCastle密库
    private final String CLIENT_KEY_PASS = "123456";// 密码

    /**
     * @param ip 初始化连接的IP
     * @throws IOException
     * @throws UnknownHostException
     */
    public NetworkHelper(String ip, int port, int timeout, Context context) {
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
        this.tcontext = context;
    }

    public NetworkHelper(String ip, int port, Context tcontext, int timeoutCon, int timeoutRes, boolean isTls) {
        this.ip = ip;
        this.port = port;
        this.tcontext = tcontext;
        this.timeoutCon = timeoutCon;
        this.timeoutRes = timeoutRes;
        this.isTls = isTls;
    }


    /**
     * 连接socket
     *
     * @return
     * @throws IOException
     */
    public int Connect() {
        try {
            Log.d(clase, "Connect: "+"------------ConnectConnect---------------------");
            Logger.logLine(LogType.COMUNICACION, clase, "------------ConnectConnect---------------------");
            Logger.logLine(LogType.COMUNICACION, clase, "createSocket : " + ip + " - " + port);
            Logger.logLine(LogType.COMUNICACION, clase, "timeoutRes : " + timeoutRes);
            Logger.logLine(LogType.COMUNICACION, clase, "timeoutCon : " + timeoutCon);
            Logger.logLine(LogType.COMUNICACION, clase, "TLS  : " + isTls);


            if (isTls) {
                //TLSSocketFactory sslFactory = new TLSSocketFactory(tcontext);
                //SSLFactory sslFactory = new SSLFactory(tcontext);
                //socket = sslFactory.createSocket();

                Resources res = tcontext.getResources();
                InputStream tlsKeyStore = res.openRawResource(R.raw.ca);

                AndroidSocketFactory sf = new AndroidSocketFactory(tlsKeyStore);
                sf.setAlgorithm("TLSv1.2");
                sf.setKeyPassword("wposs2020");
                sf.setPassword("wposs2020");
                sf.setServerAuthNeeded(false);
                sf.setClientAuthNeeded(false);
                sf.setEnabledCipherSuites(new String[]{
                        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                        "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
                        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                        "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
                        "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
                        "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
                        "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
                        "TLS_RSA_WITH_AES_128_GCM_SHA256",
                        "TLS_RSA_WITH_AES_256_GCM_SHA384",
                        "TLS_RSA_WITH_AES_128_CBC_SHA",
                        "TLS_RSA_WITH_AES_256_CBC_SHA",
                        "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"
                });
                socket = sf.createSocket(ip, port, timeoutCon);

                socket.setSoTimeout(timeoutRes);
                is = socket.getInputStream();
                os = socket.getOutputStream();
            } else {
                socket = new Socket();
                socket.setSoTimeout(timeoutRes);
                socket.connect(new InetSocketAddress(ip, port), timeoutCon);
                is = socket.getInputStream();
                os = socket.getOutputStream();
            }




        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                String mensaje = "SE AGOTO TIEMPO DE ESPERA DEL SERVIDOR ";
                Logger.logLine(LogType.EXCEPTION, clase, mensaje);
                Log.d(clase, "Connect: " + mensaje);
            } else {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                Logger.logLine(LogType.COMUNICACION, clase, "ConnectConnect ERROR 1: " + timeoutCon);
            }
            return -1;
        } catch (ISOException e) {
            e.printStackTrace();
            Logger.logLine(LogType.COMUNICACION, clase, "ConnectConnect ERROR 2: " + timeoutCon);
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return -1;
        }catch (Exception exception){
            if (exception instanceof NetworkOnMainThreadException){
                Log.d(clase, "Connect: "+" Fallo de Conexion "+ exception.getMessage());
                return -1;
            } else if (exception instanceof java.net.ConnectException ){
                Logger.logLine(LogType.COMUNICACION, clase, "FALLO CONEXION NO SE OBTUVO RESPUESTA DE SERVIDOR");
                return -1;
            }else {
                exception.printStackTrace();
                Logger.logLine(LogType.COMUNICACION, clase, "ConnectConnect ERROR 3: " + timeoutCon);
                Logger.logLine(LogType.EXCEPTION, clase, exception.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, exception.getStackTrace());
                return -1;
            }
        }
        return 0;
    }

    /**
     * 关闭socket
     */
    public int close() {
        try {
            if (socket != null)
                socket.close();
            Logger.logLine(LogType.COMUNICACION, clase, "-----------------------------close OK----------------------------");
        } catch (IOException e) {
            Logger.logLine(LogType.COMUNICACION, clase, "close ERROR");
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return -1;
        }catch (Exception exception){
            if (exception instanceof NetworkOnMainThreadException){
                return -1;
            } else if (exception instanceof java.net.ConnectException ){
                Logger.logLine(LogType.COMUNICACION, clase, "FALLO CONEXION NO SE OBTUVO RESPUESTA DE SERVIDOR");
                return -1;
            }else {
                exception.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, exception.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, exception.getStackTrace());
                return -1;
            }
        }
        return 0;
    }

    /**
     * 发送数据包
     *
     * @param data
     * @return
     */
    public int Send(byte[] data) {
        byte[] newData = null;
        if (protocol == 0) {
            newData = new byte[data.length + 2];
            newData[0] = (byte) (data.length >> 8);
            newData[1] = (byte) data.length;// 丢失高位
            System.arraycopy(data, 0, newData, 2, data.length);
        }
        try {
            os.write(newData);
            //os.write(data);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.logLine(LogType.COMUNICACION, clase, "Error en envio.");
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.COMUNICACION, clase, "Error en envio. Exception");
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return -1;
        }
        return 0;
    }

    /**
     * 接受数据包
     *
     * @return
     * @throws IOException
     */
    public byte[] Recive(int max, int timeout) throws IOException {
        Logger.debug("Inicio de Recive() - NetworkHelper \n");
        ByteArrayOutputStream byteOs;
        byte[] resP = null;

        Logger.logLine(LogType.COMUNICACION, clase, "Recive  - Valor del protocol Recive() " + protocol);
        if (timeout < 5 * 1000 || timeout > 2 * 60 * 1000) {
            Logger.logLine(LogType.COMUNICACION, clase, "Recive - Recalculando timer)");
            timeout = 10 * 1000;
        }
        if (protocol == 0) {
            byte[] packLen = new byte[2];
            int len;
            byte[] bb = new byte[2 + max];
            int i;
            byteOs = new ByteArrayOutputStream();

            try {
                Logger.logLine(LogType.COMUNICACION, clase, "Antes de 1er Try");
                if ((i = is.read(packLen)) != -1) {
                    Logger.logLine(LogType.COMUNICACION, clase, "Recive - packLen - " + packLen);

                    len = ISOUtil.byte2int(packLen);

                    Logger.logLine(LogType.COMUNICACION, clase, "Recive - len - " + len);


                    while (len > 0 && (i = is.read(bb)) != -1) {
                        byteOs.write(bb, 0, i);
                        len -= i;
                    }

                    Logger.logLine(LogType.COMUNICACION, clase, "Recive - fin While ");
                }
            } catch (InterruptedIOException e) {
                // 读取超时处理
                Logger.logLine(LogType.COMUNICACION, clase, "ERROR : " + e.getMessage());
                Logger.logLine(LogType.COMUNICACION, clase, "InterruptedIOException " + e.toString());
                Log.w("PAY_SDK", "Recive 1：读取流数据超时异常");
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                return null;
            } catch (IOException e) {
                // 读取超时处理
                Logger.logLine(LogType.COMUNICACION, clase, "ERROR : " + e.getMessage());
                Logger.logLine(LogType.COMUNICACION, clase, "IOException " + e.toString());
                Log.w("PAY_SDK", "Recive 2 ：读取流数据超时异常");
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                return null;
            }

            Logger.logLine(LogType.COMUNICACION, clase, "Antes de toByteArray - Recive()");
            resP = byteOs.toByteArray();
        }

        Logger.logLine(LogType.COMUNICACION, clase, "Fin de Recive() - NetworkHelper resP : " + resP);
        return resP;
    }

    public void checkConnection() {
        final String DEBUG_TAG = "NetworkStatusExample";
        ConnectivityManager connMgr = (ConnectivityManager) tcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);

    }
}
