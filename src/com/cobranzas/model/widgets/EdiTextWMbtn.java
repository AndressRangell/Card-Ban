package com.cobranzas.model.widgets;

import android.content.Context;
import android.widget.Button;

public class EdiTextWMbtn extends EdiTextWidgetModel{

    Button button;

    public EdiTextWMbtn(String titulo, int tipoWidget, int lengthMax, int typeInput, Boolean enabled, Context context) {
        super(titulo, tipoWidget, lengthMax, typeInput, enabled, context);
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }
}
