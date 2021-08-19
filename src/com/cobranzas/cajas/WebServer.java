package com.cobranzas.cajas;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {
    private Context context;
    String response = "";
    int status = 0;
    String clase = "WebServer.java";


    public WebServer(int port, Context context) {
        super(port);
        this.context = context;
    }

    public WebServer(String hostname, int port, Context context) {
        super(hostname, port);
        this.context = context;
    }


    public Response.Status lookup(int requestStatus) {
        for (Response.Status estado : Response.Status.values()) {
            if (estado.getRequestStatus() == requestStatus) {
                return estado;
            }
        }
        return null;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final CountDownLatch count = new CountDownLatch(1);
            Map<String, String> headers = session.getHeaders();
            String uri = session.getUri();

            Logger.debug(uri);

            Log.e("Data", "Dat: " + headers);

            Integer contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
            InputStream allData = session.getInputStream();

            int lenght = allData.available();
            byte[] buffer = new byte[lenght];

            allData.read(buffer, 0, lenght);
            String body = new String(buffer);
            Log.e(clase, "Body Data: " + body);
            if (body.length() == contentLength) {
                Logger.debug("Todo Correcto");
            } else {
                Logger.debug("Error ct-length");
            }

            String data = new String(buffer);
            Log.e("WebServer", "RequestBody: " + data);

            new ApiJson(context, uri, body, new wsCallback() {
                @Override
                public void rspCallback(Object json, String restHttp) {
                    Gson gson = new Gson();
                    response = gson.toJson(json);
                    status = Integer.parseInt(restHttp);
                    guardarLogs(restHttp, response);
                    count.countDown();
                }
            }).run();

            try {
                count.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e("response", "rsp: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Response.Status status1 = lookup(status);
        return newFixedLengthResponse(status1, "application/json", response);
    }


    private void guardarLogs(String restHttp, String response) {
        StringBuilder os = new StringBuilder();
        os.append("HTTP/1.0 ").append(restHttp).append("\r\n");
        os.append("Content type: application/json" + "\r\n");
        os.append("Content length: ").append(response.length()).append("\r\n");
        os.append("\r\n");
        os.append(response).append("\r\n");
        Log.d(clase, "ENVIAR PETICION --> " + "Envio de POS a CAJA: \n" + os.toString());
        Logger.logLine(LogType.COMUNICACION, clase, "Envio de POS a CAJA: \n" + os.toString());
    }
}

interface wsCallback {
    void rspCallback(Object json, String restHttp);
}