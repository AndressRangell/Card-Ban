package com.cobranzas.model.widgets;

import android.content.Context;

import android.graphics.drawable.Drawable;
import android.support.annotation.StyleRes;
import android.view.View;
import android.widget.Switch;

import com.wposs.cobranzas.R;

public class SwitchWidgetModel extends WidgetModel implements ItemWidget {

    private final Switch aSwitch;
    private Context ctx;

    public SwitchWidgetModel(String titulo, int tipo, Context context) {
        super(titulo, tipo, context);
        aSwitch = new Switch(context);
    }

    public void setChecked(String oneOrZero) {
        oldValue = oneOrZero;
        aSwitch.setChecked(oneOrZero.equals("true"));
    }

    public void setDiseño(int i) {
        if (i == 0) {
            aSwitch.setLayoutParams(lp);
            aSwitch.setPadding(0, 0, 16, 0);
            aSwitch.setSwitchMinWidth(96);
            aSwitch.setThumbDrawable(context.getDrawable(R.drawable.fondo_switch));
            aSwitch.setSwitchTextAppearance(context, R.style.SwitchTextAppearance);
            aSwitch.setShowText(true);
        }
    }

    public void setSwitchTextAppearance(@StyleRes int style) {
        aSwitch.setSwitchTextAppearance(ctx, style);
    }

    public void setShowText(boolean showText) {
        aSwitch.setShowText(showText);
    }

    public void setThumbDrawable(Drawable drawable) {
        aSwitch.setThumbDrawable(drawable);
    }

    @Override
    public Switch getWidget() {
        return aSwitch;
    }

    @Override
    public String getTituloItem() {
        return titulo;
    }

    @Override
    public View getWidgetItem() {
        return aSwitch;
    }

    @Override
    public String getResultado() {
        String result;
        if (aSwitch.isChecked()) {
            result = "true";
        } else {
            result = "false";
        }
        return result;
    }

    @Override
    public boolean isNewValor() {
        boolean result = false;
        if (aSwitch.isChecked() && oldValue.equals("false")) {
            result = true;
        } else if (!aSwitch.isChecked() && oldValue.equals("true")) result = true;
        return result;
    }
}
