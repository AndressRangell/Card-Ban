package com.cobranzas.tools;

import com.pos.device.printer.Printer;

public class PaperStatus {

    static PaperStatus paperStatus;

    private int ret;
    Printer printer = Printer.getInstance();

    public int getRet() {
        ret = printer.getStatus();
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public static PaperStatus getInstance(){
        if (paperStatus == null){
            paperStatus = new PaperStatus();
        }
        return paperStatus;
    }

}
