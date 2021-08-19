package com.newpos.libpay.trans.translog;

import java.io.Serializable;

/**
 * 交易日志详情信息类
 *
 * @author zhouqiang
 */
public class TransLogData implements Serializable {

    private String nombreComercio;
    private String phoneTrade;
    private String aid;
    private String aidname;
    private String addressTrade;
    private int numCuotas;
    private String datePrint;
    private String msgID;
    private String labelCard;
    private String typeCoin;
    private String nameCard;
    private int aac;
    private boolean isNFC;
    private boolean isScan;
    private boolean isICC;
    private String batchNo;
    private boolean isVoided;
    private String typeTransVoid;
    private String pan;
    private String transEName;
    protected String tipoVenta;
    private String panNormal;
    private String procCode;
    private Long amount;
    private Long otherAmount;
    private String traceNo;
    private String localTime;
    private boolean fallback;
    private String settleDate;
    private String typeAccount;
    private String issuerName;
    private String localDate;
    private String expDate;
    private String entryMode;
    private String panSeqNo;
    private String nii;
    private String svrCode;
    private String acquirerID;
    private String track2;
    private String rrn;
    private String authCode;
    private String rspCode;
    private String termID;
    private String merchID;
    private String currencyCode;
    private int oprNo;
    private String pin;
    private byte[] iccdata;
    private String field60;
    private String field62;
    private String field63;

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getDatePrint() {
        return datePrint;
    }

    public void setDatePrint(String datePrint) {
        this.datePrint = datePrint;
    }

    public int getNumCuotas() {
        return numCuotas;
    }

    public void setNumCuotas(int numCuotas) {
        this.numCuotas = numCuotas;
    }

    public String getAddressTrade() {
        return addressTrade;
    }

    public void setAddressTrade(String addressTrade) {
        this.addressTrade = addressTrade;
    }

    public String getNombreComercio() {
        return nombreComercio;
    }

    public void setNombreComercio(String nameTrade) {
        this.nombreComercio = nameTrade;
    }

    public String getPhoneTrade() {
        return phoneTrade;
    }

    public void setPhoneTrade(String phoneTrade) {
        this.phoneTrade = phoneTrade;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getAidname() {
        return aidname;
    }

    public void setAidname(String aidname) {
        this.aidname = aidname;
    }

    public String getLabelCard() {
        return labelCard;
    }

    public void setLabelCard(String labelCard) {
        this.labelCard = labelCard;
    }

    public String getNameCard() {
        return nameCard;
    }

    public void setNameCard(String nameCard) {
        this.nameCard = nameCard;
    }

    public String getTypeCoin() {
        return typeCoin;
    }

    public void setTypeCoin(String typeCoin) {
        this.typeCoin = typeCoin;
    }

    public int getAac() {
        return aac;
    }

    public void setAac(int aac) {
        this.aac = aac;
    }

    public boolean isNFC() {
        return isNFC;
    }

    public void setNFC(boolean isNFC) {
        this.isNFC = isNFC;
    }

    public boolean isICC() {
        return isICC;
    }

    public void setICC(boolean isICC) {
        this.isICC = isICC;
    }

    public boolean isScan() {
        return isScan;
    }

    public void setScan(boolean scan) {
        isScan = scan;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public boolean getIsVoided() {
        return isVoided;
    }

    public void setVoided(boolean isVoided) {
        this.isVoided = isVoided;
    }

    public String getTypeTransVoid() {
        return typeTransVoid;
    }

    public void setTypeTransVoid(String typeTransVoid) {
        this.typeTransVoid = typeTransVoid;
    }

    public String getEName() {
        return transEName;
    }

    public void setEName(String eName) {
        transEName = eName;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getPanNormal() {
        return panNormal;
    }

    public void setPanNormal(String panNormal) {
        this.panNormal = panNormal;
    }

    public String getProcCode() {
        return procCode;
    }

    public void setProcCode(String procCode) {
        this.procCode = procCode;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(Long otherAmount) {
        this.otherAmount = otherAmount;
    }

    public String getTraceNo() {
        return traceNo;
    }

    public void setTraceNo(String traceNo) {
        this.traceNo = traceNo;
    }

    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }

    public String getLocalDate() {
        return localDate;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public String getPanSeqNo() {
        return panSeqNo;
    }

    public void setPanSeqNo(String panSeqNo) {
        this.panSeqNo = panSeqNo;
    }

    public String getNii() {
        return nii;
    }

    public void setNii(String nii) {
        this.nii = nii;
    }

    public String getSvrCode() {
        return svrCode;
    }

    public void setSvrCode(String svrCode) {
        this.svrCode = svrCode;
    }

    public String getAcquirerID() {
        return acquirerID;
    }

    public void setAcquirerID(String acquirerID) {
        this.acquirerID = acquirerID;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rRN) {
        rrn = rRN;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public String getTermID() {
        return termID;
    }

    public void setTermID(String termID) {
        this.termID = termID;
    }

    public String getMerchID() {
        return merchID;
    }

    public void setMerchID(String merchID) {
        this.merchID = merchID;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getOprNo() {
        return oprNo;
    }

    public void setOprNo(int oprNo) {
        this.oprNo = oprNo;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public byte[] getIccdata() {
        return iccdata;
    }

    public void setIccdata(byte[] iCCData) {
        iccdata = iCCData;
    }

    public String getField60() {
        return field60;
    }

    public void setField60(String field60) {
        this.field60 = field60;
    }

    public String getField62() {
        return field62;
    }

    public void setField62(String field62) {
        this.field62 = field62;
    }

    public String getField63() {
        return field63;
    }

    public void setField63(String field63) {
        this.field63 = field63;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public String getTypeAccount() {
        return typeAccount;
    }

    public void setTypeAccount(String typeAccount) {
        this.typeAccount = typeAccount;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public String getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(String tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    /**
     * BANCARD
     */
    private long ammountCashOver;
    private long montoFijo;
    private String field02;
    private String field19;
    private String field48;
    private String field55;
    private String field61;
    private String arqc;
    private String tc;
    private String tvr;
    private String tsi;
    private String numCuotasDeferred;
    private String tipoMontoFijo;
    private String merchantType;
    private boolean isPinExist;
    private boolean isDebit;
    private String nroCargo;
    private String codigoDelNegocio;
    private String tipoTarjeta; //C = Credito, D = Debito - Ultima letra del subCampo89
    private String additionalAmount;

    public long getAmmountCashOver() {
        return ammountCashOver;
    }

    public void setAmmountCashOver(long ammountCashOver) {
        this.ammountCashOver = ammountCashOver;
    }

    public long getMontoFijo() {
        return montoFijo;
    }

    public void setMontoFijo(long montoFijo) {
        this.montoFijo = montoFijo;
    }

    public String getField02() {
        return field02;
    }

    public void setField02(String field02) {
        this.field02 = field02;
    }

    public String getField19() {
        return field19;
    }

    public void setField19(String field19) {
        this.field19 = field19;
    }

    public String getField48() {
        return field48;
    }

    public void setField48(String field48) {
        this.field48 = field48;
    }

    public String getField55() {
        return field55;
    }

    public void setField55(String field55) {
        this.field55 = field55;
    }

    public String getField61() {
        return field61;
    }

    public void setField61(String field61) {
        this.field61 = field61;
    }

    public String getArqc() {
        return arqc;
    }

    public void setArqc(String arqc) {
        this.arqc = arqc;
    }

    public String getTc() {
        return tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getTsi() {
        return tsi;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public String getNumCuotasDeferred() {
        return numCuotasDeferred;
    }

    public void setNumCuotasDeferred(String numCuotasDeferred) {
        this.numCuotasDeferred = numCuotasDeferred;
    }

    public boolean isVoided() {
        return isVoided;
    }

    public String getTransEName() {
        return transEName;
    }

    public void setTransEName(String transEName) {
        this.transEName = transEName;
    }

    public String getTipoMontoFijo() {
        return tipoMontoFijo;
    }

    public void setTipoMontoFijo(String tipoMontoFijo) {
        this.tipoMontoFijo = tipoMontoFijo;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public boolean isPinExist() {
        return isPinExist;
    }

    public void setPinExist(boolean pinExist) {
        isPinExist = pinExist;
    }

    public boolean isDebit() {
        return isDebit;
    }

    public void setDebit(boolean debit) {
        isDebit = debit;
    }

    public String getNroCargo() {
        return nroCargo;
    }

    public void setNroCargo(String nroCargo) {
        this.nroCargo = nroCargo;
    }

    public String getCodigoDelNegocio() {
        return codigoDelNegocio;
    }

    public void setCodigoDelNegocio(String codigoDelNegocio) {
        this.codigoDelNegocio = codigoDelNegocio;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getAdditionalAmount() {
        return additionalAmount;
    }

    public void setAdditionalAmount(String additionalAmount) {
        this.additionalAmount = additionalAmount;
    }


}
