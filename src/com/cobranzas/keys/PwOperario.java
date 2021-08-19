package com.cobranzas.keys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wposs.cobranzas.R;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.menus.menus;
import com.newpos.libpay.global.TMConfig;

import cn.desert.newpos.payui.UIUtils;

public class PwOperario extends AppCompatActivity {


    private EditText etPw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pw_operario);
        Button btnOk;
        Button btnCnl;
        etPw = (EditText) findViewById(R.id.et_pw_operario);
        btnOk = (Button) findViewById(R.id.btn_conf_mon);
        btnCnl = (Button) findViewById(R.id.btn_cancel_mon);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etPw.getText().toString().equals(TMConfig.getInstance().getMasterPass())) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(PwOperario.this, menus.class);
                    intent.putExtra(DefinesBANCARD.DATO_MENU, DefinesBANCARD.ITEM_MENU_OPERARIO);
                    startActivity(intent);
                    finish();
                } else {
                    etPw.setText("");
                    UIUtils.toast(PwOperario.this, R.drawable.ic_cobranzas_blanca, getString(R.string.err_msg_pwoperario), Toast.LENGTH_SHORT);
                }
            }
        });

        btnCnl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        etPw.setText("");
    }

    @Override
    public void onBackPressed() {
        // Do nothing because of X and Y.
    }
}
