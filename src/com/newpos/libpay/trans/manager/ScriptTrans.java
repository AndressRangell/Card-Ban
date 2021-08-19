package com.newpos.libpay.trans.manager;

import android.content.Context;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;

/**
 * 脚本处理实体类
 *
 * @author zhouqiang
 */
public class ScriptTrans extends Trans {
    String clase = "ScriptTrans.java";

    public ScriptTrans(Context ctx, String transEname) {
        super(ctx, transEname);
        iso8583.setHasMac(false);
    }

    protected void setFields(TransLogData data) {

        try {
            if (MsgID != null) {
                iso8583.setField(0, MsgID);
            }
            if (data.getPan() != null) {
                iso8583.setField(2, data.getPan());
            }
            if (data.getProcCode() != null) {
                iso8583.setField(3, data.getProcCode());
            }
            if (data.getAmount() != null) {
                if (data.getAmount() >= 0) {
                    String AmoutData = "";
                    AmoutData = ISOUtil.padleft(data.getAmount() + "", 12, '0');
                    iso8583.setField(4, AmoutData);
                }
            }
            if (TraceNo != null) {
                iso8583.setField(11, TraceNo);
            }
            if (data.getEntryMode() != null) {
                iso8583.setField(22, data.getEntryMode());
            }

            if (data.getNii() != null) {
                iso8583.setField(24, data.getNii());
            }
            if (data.getSvrCode() != null) {
                iso8583.setField(25, data.getSvrCode());
            }

            if (data.getAcquirerID() != null) {
                iso8583.setField(32, data.getAcquirerID());
            }
            if (data.getRrn() != null) {
                iso8583.setField(37, data.getRrn());
            }
            if (data.getAuthCode() != null) {
                iso8583.setField(38, data.getAuthCode());
            }
            if (TermID != null) {
                iso8583.setField(41, TermID);
            }
            if (MerchID != null) {
                iso8583.setField(42, MerchID);
            }
            if (data.getCurrencyCode() != null) {
                iso8583.setField(49, data.getCurrencyCode());
            }
            if (data.getIccdata() != null) {
                iso8583.setField(55, ISOUtil.byte2hex(data.getIccdata()));
            }
            // 60.1 00 60.2 批次号 60.3 951
            // Field60 = "00" + config.getBatchNo() + "951";
            if (Field60 != null) {
                iso8583.setField(60, Field60);
            }
            // 61.1 原交易批次号 61.2 原pos流水号 61.3 原交易日期
		/*
		Field61 = data.getTraceNo() + data.getBatchNo() + data.getLocalDate();
		iso8583.setField(61, Field61);
		*/

            if (data.getField63() != null) {
                iso8583.setField(63, data.getField63());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }

    }

    public int sendScriptResult(TransLogData data) {
        setFields(data);
        int ret = OnLineTrans();
        return ret;
    }
}
