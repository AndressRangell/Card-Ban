package com.newpos.libpay.device.contactless;

import com.android.newpos.libemv.PBOCUtil;
import com.newpos.libpay.utils.ISOUtil;

public class EmvPBOCUpi {

    String AID;
    String TVR;
    byte[] temp;
    byte[] dest;

    public EmvPBOCUpi(byte[] temp) {
        this.temp = temp;
    }


    public void setAID(String AID) {
        this.AID = AID;
    }


    public String GetLable() {
        byte[] label = getDataKernel(0x50);
        return ISOUtil.hex2AsciiStr(ISOUtil.byte2hex(label));
    }

    public String GetAid() {
        return AID;
    }

    public String GetTVR() {
        byte[] tvr = getDataKernel(0x95);
        return ISOUtil.hexString(tvr);
    }

    public String GetTSI() {
        byte[] tsi = getDataKernel(0x9B);
        return ISOUtil.hexString(tsi);
    }

    public String GetARQC() {
        byte[] arqc = getDataKernel(0x9F26);
        return ISOUtil.hexString(arqc);
    }


    private byte[] getDataKernel(int tag) {
        int len = PBOCUtil.get_tlv_data_kernel(tag, temp);
        if (len > 0) {
            dest = new byte[len];
            System.arraycopy(temp, 0, dest, 0, len);
        }
        return dest;
    }

}
