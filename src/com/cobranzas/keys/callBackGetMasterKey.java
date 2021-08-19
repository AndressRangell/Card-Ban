package com.cobranzas.keys;

import android.os.AsyncTask;
import android.util.Log;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.SDKException;
import com.pos.device.apdu.CommandApdu;
import com.pos.device.apdu.ResponseApdu;
import com.pos.device.icc.ContactCard;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.OperatorMode;
import com.pos.device.icc.SlotType;
import com.pos.device.icc.VCC;

/**
 * Created by Acer on 5/02/2018.
 */

public class callBackGetMasterKey extends AsyncTask<String, Integer, String> {

    static String clase = "callBackGetMasterKey.java";
    private FileCallback callback;
    private static final String TAG = "IccReader";

    public callBackGetMasterKey(final FileCallback callback) {

        this.callback = callback;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        IccReader iccReader0;
        boolean cardPresent;
        String hex1 = null;
        String hex2;
        String rta = "1";

        try {
            while (true) {

                if (isCancelled()) {
                    rta = "2";
                    break;
                }

                iccReader0 = IccReader.getInstance(SlotType.USER_CARD);

                ContactCard contactCardSam = null;

                try {
                    contactCardSam = iccReader0.connectCard(VCC.VOLT_5, OperatorMode.EMV_MODE);
                } catch (SDKException e) {
                    Logger.logLine(LogType.FLUJO,clase, e.getStackTrace());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                if (contactCardSam != null) {

                    byte[] passMK = InjectMasterKey.pwMasterKey.getBytes("US-ASCII");

                    String comandoApduPin = "00200000" + "0" + InjectMasterKey.pwMasterKey.length() + ISOUtil.bcd2str(passMK, 0, passMK.length);

                    byte[] apdu = com.newpos.libpay.utils.ISOUtil.str2bcd(comandoApduPin, false);

                    CommandApdu cmdApdu = new CommandApdu(apdu);
                    cardPresent = iccReader0.isCardPresent();

                    if (cardPresent) {

                        try {
                            //Verificacion pin
                            ResponseApdu rspApdu = iccReader0.transmit(contactCardSam, cmdApdu);
                            String respuestaAPDU = null;
                            if (rspApdu != null) {
                                respuestaAPDU = com.newpos.libpay.utils.ISOUtil.bcd2str(rspApdu.getBytes(), 0, rspApdu.getBytes().length * 2, false);
                            }

                            if (respuestaAPDU != null && !respuestaAPDU.equals("9000")) {
                                rta = "1";
                                break;
                            }
                            //get master key part 1
                            byte[] apdu2 = com.newpos.libpay.utils.ISOUtil.str2bcd("00B0850010", false);
                            byte[] rspData = iccReader0.transmit(contactCardSam, apdu2);
                            if (rspData != null) {
                                hex1 = com.newpos.libpay.utils.ISOUtil.bcd2str(rspData, 0, rspData.length * 2, false);
                            }

                            //get master key part 2
                            byte[] apdu3 = com.newpos.libpay.utils.ISOUtil.str2bcd("00B0860010", false);
                            byte[] rspData3 = iccReader0.transmit(contactCardSam, apdu3);

                            if (rspData3 != null) {

                                hex2 = com.newpos.libpay.utils.ISOUtil.bcd2str(rspData3, 0, rspData3.length * 2, false);

                                String auxMk1 = convertHexToString(hex1);
                                String auxMk2 = convertHexToString(hex2);

                                String finalMk = auxMk1.substring(0, auxMk1.length() - 2) + auxMk2.substring(0, auxMk2.length() - 2);

                                if (finalMk != null) {
                                    rta = finalMk;
                                    break;
                                }
                            }
                            rta = "3";
                            break;
                        } catch (Exception e) {
                            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                            Logger.error("Exception" + e.toString());
                        }
                    }

                } else {
                    Log.e(TAG, "contactCardSam=null");
                }
            }//end while

        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Log.e("Error", e.getMessage());
        }

        return rta;

    }

    public String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        Logger.debug("Decimal : " + temp.toString());

        return sb.toString();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(String text) {
        super.onPostExecute(text);
        callback.rspUnPack(text);
    }

    public interface FileCallback {
        String rspUnPack(String okUnpack);
    }
}
