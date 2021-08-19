package com.cobranzas.basedatos;

public class ModelConfiguracion {

    String ipPrincipalConfig;
    int portPrincipalConfig;
    String ipSecundariaConfig;
    int portSecundarioConfig;
    int timerConfig;
    int timerDataConfig;
    String niiConfig;

    public ModelConfiguracion() {
        // Do nothing because of X and Y.
    }

    public String getIpPrincipalConfig() {
        return ipPrincipalConfig;
    }

    public void setIpPrincipalConfig(String ipPrincipalConfig) {
        this.ipPrincipalConfig = ipPrincipalConfig;
    }

    public int getPortPrincipalConfig() {
        return portPrincipalConfig;
    }

    public void setPortPrincipalConfig(int portPrincipalConfig) {
        this.portPrincipalConfig = portPrincipalConfig;
    }

    public String getIpSecundariaConfig() {
        return ipSecundariaConfig;
    }

    public void setIpSecundariaConfig(String ipSecundariaConfig) {
        this.ipSecundariaConfig = ipSecundariaConfig;
    }

    public int getPortSecundarioConfig() {
        return portSecundarioConfig;
    }

    public void setPortSecundarioConfig(int portSecundarioConfig) {
        this.portSecundarioConfig = portSecundarioConfig;
    }

    public int getTimerConfig() {
        return timerConfig;
    }

    public void setTimerConfig(int timerConfig) {
        this.timerConfig = timerConfig;
    }

    public int getTimerDataConfig() {
        return timerDataConfig;
    }

    public void setTimerDataConfig(int timerDataConfig) {
        this.timerDataConfig = timerDataConfig;
    }

    public String getNiiConfig() {
        return niiConfig;
    }

    public void setNiiConfig(String niiConfig) {
        this.niiConfig = niiConfig;
    }
}
