package com.cobranzas.model.widgets;

import android.content.Context;
import android.view.View;

import com.wposs.cobranzas.R;

import cn.desert.newpos.payui.setting.view.IPEditText;

public class IpEdiTextWidgetModel extends WidgetModel implements ItemWidget {

    IPEditText ipEditText;

    public IpEdiTextWidgetModel(String titulo, int tipo, boolean isEnabled, Context context) {
        super(titulo, tipo, context);
        ipEditText = new IPEditText(context, null);
        ipEditText.setLiveOrDeath(isEnabled);
    }

    public void setIPHint(String ip) {
        oldValue = ip;
        String[] ipValor = ip.replace(".", "-").split("-");
        if (ipValor.length == 4) {
            ipEditText.setIpHint(ipValor);
        }
    }

    public void setText(String ip) {
        oldValue = ip;
        String[] ipValor = ip.replace(".", "-").split("-");
        if (ipValor.length == 4) {
            ipEditText.setIPText(ipValor);
        }
    }

    public void setDiseño(int i) {
        switch (i) {
            case 0:
                ipEditText.setLayoutParams(lp);
                break;
            case 1:
                ipEditText.setLayoutParams(lp);
                ipEditText.setBackground(R.drawable.fondo_editext, context);
                ipEditText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ipEditText.setIPText(new String[]{"0", "0", "0", "0"});

                        return false;
                    }
                });
                break;
            default:
        }
    }

    @Override
    public IPEditText getWidget() {
        return ipEditText;
    }

    @Override
    public String getTituloItem() {
        return titulo;
    }

    @Override
    public View getWidgetItem() {
        return ipEditText;
    }

    @Override
    public String getResultado() {
        return ipEditText.getIPTextOrHint();
    }

    @Override
    public boolean isNewValor() {
        return !oldValue.equals(ipEditText.getIPTextOrHint());
    }
}
