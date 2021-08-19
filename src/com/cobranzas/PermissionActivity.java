package com.cobranzas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cobranzas.inicializacion.trans_init.Init;
import com.cobranzas.tools.PermissionStatus;
import com.wposs.cobranzas.R;

import java.util.Timer;
import java.util.TimerTask;

public class PermissionActivity extends AppCompatActivity {

    PermissionStatus permissionStatus;
    Timer timer;
    TextView tvPermisos;
    ImageView imgPermisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        permissionStatus = new PermissionStatus(PermissionActivity.this, this);
        permissionStatus.reqPermissions();

        tvPermisos = findViewById(R.id.tvPermisos);
        imgPermisos = findViewById(R.id.imgPermisos);
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionStatus.confirmPermissionMsg();
        if (permissionStatus.validatePermissions()){

            tvPermisos.setVisibility(View.VISIBLE);
            imgPermisos.setVisibility(View.VISIBLE);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setClass(PermissionActivity.this, Init.class);
                    startActivity(intent);
                }
            }, (long) 3 * 1000);
        }
    }
}