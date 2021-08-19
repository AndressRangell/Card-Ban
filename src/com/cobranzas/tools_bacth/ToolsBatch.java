package com.cobranzas.tools_bacth;

import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;

import java.util.List;

public class ToolsBatch {

    private ToolsBatch() {
    }

    public static boolean statusTrans(String idAcq) {
        List<TransLogData> list = TransLog.getInstance(idAcq).getData();
        return !list.isEmpty();
    }

    /**
     * @param value
     * @return
     */
    public static String incBatchNo(String value) {
        int val = Integer.parseInt(value);
        if (val == 999999) {
            val = 0;
        }
        val += 1;
        return ISOUtil.padleft(String.valueOf(val), 6, '0');
    }
}
