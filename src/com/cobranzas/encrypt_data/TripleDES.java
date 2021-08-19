package com.cobranzas.encrypt_data;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.pos.device.config.DevConfig;

public class TripleDES {

    private TripleDES(){}
    static String clase = "TripleDES.java";
    // Caracter de relleno, si lo requiere la cadena a encriptar
    static final String PAD = "@";
    // Caracter para la anulacion del caracter de relleno
    static final String BLANK = " ";
    static final String ENCODING = "ISO9797-1";

    private static final byte ENCRYPT = 0;

    public static String hexString(byte[] b, int offset, int len) {
        StringBuilder d = new StringBuilder(len * 2);
        len += offset;
        for (int i = offset; i < len; i++) {
            d.append(hexStrings[b[i] & 0xFF]);
        }
        return d.toString();
    }

    protected static final String[] hexStrings;

    static {
        hexStrings = new String[256];
        for (int i = 0; i < 256; i++) {
            StringBuilder d = new StringBuilder(2);
            char ch = Character.forDigit((byte) i >> 4 & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            ch = Character.forDigit((byte) i & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            hexStrings[i] = d.toString();
        }
    }

    public static byte[] stringToByte(byte[] strings) {

        byte[] pad = {(byte) 0x00};
        int tamBuffer = strings.length + 8 - (strings.length % 8);
        byte[] buffer = new byte[tamBuffer];
        int indice = 0;

        System.arraycopy(strings, 0, buffer, indice, strings.length);
        indice = strings.length;

        for (int i = 1; i <= (tamBuffer - strings.length); i++) {
            System.arraycopy(pad, 0, buffer, indice, 1);
            indice++;
        }

        return buffer;
    }

    public static byte[] cryptBytes(byte[] plain, int index, byte[] key) {

        int lenBuffer;

        byte[] input = new byte[plain.length - index];
        System.arraycopy(plain, index, input, 0, input.length);

        if (input.length % 8 != 0)
            lenBuffer = input.length + 8 - (input.length % 8);
        else
            lenBuffer = input.length;

        byte[] cifred = new byte[lenBuffer];
        byte[] byteCode = new byte[2048];

        byte[] stringToByte = stringToByte(input);

        tripleDes16(stringToByte, byteCode, key, ENCRYPT);

        System.arraycopy(byteCode, 0, cifred, 0, lenBuffer);

        return cifred;
    }

    //******************************************************************************
//*                                                                            *
//*  Function        tripleDes16                                               *
//*                                                                            *
//*  Description     Ciphers 64 bits block of data.                            *
//*                                                                            *
//*  Input           inputData  - Pointer to 8 bytes input data                *
//*                  outputData - Pointer to 8 bytes output buffer             *
//*                  inputkey   - Pointer to 16 bytes input key                *
//*                  mode       - 0 => Encrypt                                 *
//*                               1 => Decrypt                                 *
//*                                                                            *
//*  Return          None                                                      *
//*                                                                            *
//******************************************************************************
    public static void tripleDes16(byte[] inputData, byte[] outputData, byte[] inputKey, byte mode) {
        byte[] temporal = new byte[8];
        byte[] temporal2 = new byte[8];
        byte[] key = new byte[8];
        int i;

        try {

            for (i = 0; i < inputData.length; i += 8) {

                System.arraycopy(inputData, i, temporal2, 0, 8);

                System.arraycopy(inputKey, 0, key, 0, 8);
                temporal = DES.cipher(temporal2, key, mode);

                System.arraycopy(inputKey, 8, key, 0, 8);
                temporal2 = DES.cipher(temporal, key, (byte) (mode ^ 0x01));

                System.arraycopy(inputKey, 0, key, 0, 8);
                temporal = DES.cipher(temporal2, key, mode);

                System.arraycopy(temporal, 0, outputData, i, 8);
            }

        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    public static String getKsnInicial() {
        String ret = "00001";
        ret += DevConfig.getSN().substring(1);
        ret += "000000";
        return ret;
    }

    public static String getKsnInicial2Hsm() {
        String ret = "00001";
        ret += DevConfig.getSN().substring(1);
        ret += "0";
        return ret;
    }

    public static byte[] xor(final byte[] input, final byte[] secret) {
        final byte[] output = new byte[input.length];
        if (secret.length == 0) {
            throw new IllegalArgumentException("empty security key");
        }
        int spos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            ++spos;
            if (spos >= secret.length) {
                spos = 0;
            }
        }
        return output;
    }
}

