package com.cobranzas.inicializacion.trans_init.trans;

import android.content.Context;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.BuildConfig;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Julian on 7/06/2018.
 */

public class Tools {
    static Context ctx;
    static String clase = "Tools.java";

    public static Context getCurrentContext()
    {
        return ctx;
    }

    public static String getSerial() {
        return "2H000000";
    }

    public static String getVersion()
    {
        return    BuildConfig.VERSION_NAME;
    }

    /**
     * Convert a array of byte to hex String. <br/>
     * Each byte is covert a two character of hex String. That is <br/>
     * if byte of int is less than 16, then the hex String will append <br/>
     * a character of '0'.
     *
     * @param bytes array of byte
     * @return hex String represent the array of byte
     */

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int value = b & 0xFF;
            if (value < 16) {
                // if value less than 16, then it's hex String will be only
                // one character, so we need to append a character of '0'
                sb.append("0");
            }
            sb.append(Integer.toHexString(value).toUpperCase());
        }
        return sb.toString();
    }

    /**
     * Compute the SHA-1 hash of the given byte array
     *
     * @param hashThis
     *            byte[]
     * @return byte[]
     */

    public static String hashSha1(byte[] hashThis) {
        try {
            byte[] hash = new byte[20];
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            hash = md.digest(hashThis);
            return bytesToHexString(hash);
        } catch (NoSuchAlgorithmException nsae) {
            Logger.logLine(LogType.EXCEPTION,clase, nsae.getMessage());
            Logger.logLine(LogType.EXCEPTION,clase, nsae.getStackTrace());
            System.err.println("SHA-1 algorithm is not available...");
            System.exit(2);
        }
        return null;
    }
}
