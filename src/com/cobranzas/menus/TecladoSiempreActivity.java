package com.cobranzas.menus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.newpos.libpay.device.printer.PrintRes;

import cn.desert.newpos.payui.master.MasterControl;

public class TecladoSiempreActivity extends AppCompatActivity {

    StringBuilder builder;
    TextView editText;
    int longitudMaxima = 9;
    ImageButton numberDone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teclado_menu);
        builder = new StringBuilder();

        editText = findViewById(R.id.editText);

        numberDone = findViewById(R.id.numberDone);
        numberDone.setEnabled(true);

        numberDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (builder.length() != 0){
                    builder.append("00");
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(TecladoSiempreActivity.this, MasterControl.class);
                    intent.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[21]);
                    intent.putExtra(MasterControl.CODIGO, builder.toString());
                    startActivity(intent);
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.number0:
                if (builder.length() != 0) {
                    builder.append("0");
                }
                mostrarEnTextView();
                break;
            case R.id.number1:
                builder.append("1");
                mostrarEnTextView();
                break;
            case R.id.number2:
                builder.append("2");
                mostrarEnTextView();
                break;
            case R.id.number3:
                builder.append("3");
                mostrarEnTextView();
                break;
            case R.id.number4:
                builder.append("4");
                mostrarEnTextView();
                break;
            case R.id.number5:
                builder.append("5");
                mostrarEnTextView();
                break;
            case R.id.number6:
                builder.append("6");
                mostrarEnTextView();
                break;
            case R.id.number7:
                builder.append("7");
                mostrarEnTextView();
                break;
            case R.id.number8:
                builder.append("8");
                mostrarEnTextView();
                break;
            case R.id.number9:
                builder.append("9");
                mostrarEnTextView();
                break;
            case R.id.numberAC:
                builder.setLength(0);
                mostrarEnTextView();
                break;
            case R.id.numberDelete:
                eliminarUltimoCaracter();
                break;
            case R.id.btnMenu:
                Intent intent = new Intent();
                intent.setClass(this, menus.class);
                intent.putExtra(DefinesBANCARD.DATO_MENU, DefinesBANCARD.ITEM_COMERCIO);
                startActivity(intent);
                builder.setLength(0);
                break;

            default:
                Logger.debug("Case invalid");
                break;
        }
    }

    private void mostrarEnTextView() {
        int len = builder.length();
        if (len <= longitudMaxima) {
            editText.setText(formatearValor());
        } else {
            eliminarUltimoCaracter();
        }
    }

    private void eliminarUltimoCaracter() {
        int len = builder.length();
        if (len != 0 ){
            builder.deleteCharAt(len - 1);
        }
        mostrarEnTextView();
    }

    private String formatearValor(){

        String dato = builder.toString();
        dato = dato.replace(".", "");


        if (dato.equals("")) {
            return "G. 0";
        }

        StringBuilder dato1 = new StringBuilder();
        char[] aux1 = dato.toCharArray();
        for (char c : aux1) {
            dato1.append(c);
        }

        String str = dato1.toString();
        String salida = "";
        int longitud = str.length();
        if (longitud < 4) {
            return "G. " + dato1.toString();
        }

        if (longitud == 4) {
            String sub2 = str.substring(0, 1);
            String sub1 = str.substring(1, 4);
            salida = sub2 + "." + sub1;
        }
        if (longitud == 5) {
            String sub2 = str.substring(0, 2);
            String sub1 = str.substring(2, 5);
            salida = sub2 + "." + sub1;
        }
        if (longitud == 6) {
            String sub2 = str.substring(0, 3);
            String sub1 = str.substring(3, 6);
            salida = sub2 + "." + sub1;
        }
        if (longitud == 7) {
            String sub2 = str.substring(0, 1);
            String sub1 = str.substring(1, 4);
            String sub0 = str.substring(4, 7);
            salida = sub2 + "." + sub1 + "." + sub0;
        }
        if (longitud == 8) {
            String sub2 = str.substring(0, 2);
            String sub1 = str.substring(2, 5);
            String sub0 = str.substring(5, 8);
            salida = sub2 + "." + sub1 + "." + sub0;
        }
        if (longitud == 9) {
            String sub2 = str.substring(0, 3);
            String sub1 = str.substring(3, 6);
            String sub0 = str.substring(6, 9);
            salida = sub2 + "." + sub1 + "." + sub0;
        }
        if (longitud == 10) {
            String sub2 = str.substring(0, 1);
            String sub1 = str.substring(1, 4);
            String sub0 = str.substring(4, 7);
            String sub = str.substring(7, 10);
            salida = sub2 + "." + sub1 + "." + sub0 + "." + sub;
        }
        if (longitud == 11) {
            String sub2 = str.substring(0, 2);
            String sub1 = str.substring(2, 5);
            String sub0 = str.substring(5, 8);
            String sub = str.substring(8, 11);
            salida = sub2 + "." + sub1 + "." + sub0 + "." + sub;
        }
        if (longitud == 12) {
            String sub2 = str.substring(0, 3);
            String sub1 = str.substring(3, 6);
            String sub0 = str.substring(6, 9);
            String sub = str.substring(9, 12);
            salida = sub2 + "." + sub1 + "." + sub0 + "." + sub;
        }
        if (longitud == 13) {
            String sub2 = str.substring(0, 1);
            String sub1 = str.substring(1, 4);
            String sub0 = str.substring(4, 7);
            String sub = str.substring(7, 10);
            String su = str.substring(10, 13);
            salida = sub2 + "." + sub1 + "." + sub0 + "." + sub + "." + su;
        }
        return "G. " + salida;
    }

    @Override
    public void onBackPressed() {
        // Do nothing because of X and Y.
    }
}
