package com.cobranzas.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.cobranzas.inicializacion.configuracioncomercio.IPS;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
 */

public class UtilNetwork {

    static String clase = "UtilNetwork.java";

    private UtilNetwork(){}

    /**
     * Convert byte array to hex string
     *
     * @param bytes toConvert
     * @return hexValue
     */

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for (int idx = 0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Get utf8 byte array.
     *
     * @param str which to be converted
     * @return array of NULL if error was found
     */

    public static byte[] getUTF8Bytes(String str) {
        try {
            return str.getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Logger.logLine(LogType.EXCEPTION, clase, ex.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, ex.getStackTrace());
            return null;
        }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     *
     * @param filename which to be converted to string
     * @return String value of File
     * @throws java.io.IOException if error occurs
     */

    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN = 1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8 = false;
            int read;
            int count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count += read;
            }
            return isUTF8 ? new String(baos.toByteArray(), StandardCharsets.UTF_8) : new String(baos.toByteArray());
        }
        finally {
            try {
                is.close();
            } catch (Exception ignored) {
                Logger.logLine(LogType.EXCEPTION, clase, ignored.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, ignored.getStackTrace());
            }
        }
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null && !intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, ignored.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, ignored.getStackTrace());
        } // for now eat exceptions
        return "";
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, ignored.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, ignored.getStackTrace());
        } // for now eat exceptions
        return "";
    }

    public static String[] showDhcpData(Context context) {

        String[] datos = new String[4];

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

        datos[0] = intToIp(dhcpInfo.netmask);
        datos[1] = intToIp(dhcpInfo.dns1);
        datos[2] = intToIp(dhcpInfo.dns2);
        datos[3] = intToIp(dhcpInfo.gateway);

        return datos;
    }

    public static String intToIp(int addr) {
        return ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }

    public static String getMask(String ip) {

        String mask = "";
        int type = Integer.parseInt(ip.substring(0, ip.indexOf(".")));
        if (type >= 0 && type <= 127) {//class A
            mask = "255.0.0.0";
        } else if (type >= 128 && type <= 191) {//class B
            mask = "255.255.0.0";
        } else if (type >= 192 && type <= 223) { //class C
            mask = "255.255.255.0";
        } else {
            mask = "0.0.0.0";
        }
        return mask;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getImei(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            return "NA";
        }
    }

    public static String getIpFull(Context context) {
        String result = "";
        String[] s = IPS.getIPAddress(true).replace(".", "-").split("-");
        if (s.length != 4) {
            return "Erro en ip";
        }
        for (int i = 0; i < s.length; i++) {
            s[i] = ISOUtil.padleft(s[i], 3, '0');
        }
        for (String r : s) {
            result += r;
        }
        return result;
    }
}
