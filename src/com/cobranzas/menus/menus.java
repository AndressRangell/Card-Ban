package com.cobranzas.menus;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.cobranzas.actividades.MainActivity;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.tools.PolarisUtil;
import com.newpos.libpay.Logger;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cobranzas.actividades.StartAppBANCARD.isInit;

public class menus extends AppCompatActivity {

    public static final int FALLBACK = 3;
    public static final int NO_FALLBACK = 0;
    public static final int TOTAL_BATCH = 200;
    public static int contFallback = 0;
    boolean isClose = true;
    boolean isOne;
    boolean isDisplay;
    String clase = "menus.java";
    CountDownTimer countDownTimerMenus;
    CountDownTimer countDownTimerDisplay;
    TextView version;
    TextView tvCardReader;
    ViewPager viewPager;
    private String menu;
    private int positionpager = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        switch (extras.getString(DefinesBANCARD.DATO_MENU)) {
            default:
                setContentView(R.layout.activity_menu);
                tvCardReader = findViewById(R.id.tvCardReader);
                tvCardReader.setVisibility(View.GONE);
                break;
        }

        if (extras != null) {
            mostrarMenu(Objects.requireNonNull(extras.getString(DefinesBANCARD.DATO_MENU)));
            menu = Objects.requireNonNull(extras.getString(DefinesBANCARD.DATO_MENU));
        }
    }


    private void mostrarMenu(String tipoMenu) {

        switch (tipoMenu) {
            default:
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                RecyclerViewAdaptadorMenu recyclerViewAdaptadorMenu;
                if (tipoMenu.equals(DefinesBANCARD.MENU_PRINCIPAL)) {
                    recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                } else {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                }
                recyclerViewAdaptadorMenu = new RecyclerViewAdaptadorMenu(obtenerItems(tipoMenu), this, DefinesBANCARD.TIPO_LAYOUT_LINEAR);
                recyclerViewAdaptadorMenu.setTipoMenu(tipoMenu);
                recyclerView.setAdapter(recyclerViewAdaptadorMenu);
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        return true;
    }


    public List<menuItemsModelo> obtenerItems(String tipoMenu) {
        List<menuItemsModelo> itemMenu = new ArrayList<>();
        switch (tipoMenu) {
            case DefinesBANCARD.MENU_PRINCIPAL:
                counterDownTimerMenus();
                deleteTimerMenus();
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_CONFIG, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_POLARIS, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_ECHO_TEST, R.drawable.mainitemunclick));
                break;
            case DefinesBANCARD.ITEM_COMERCIO:
                counterDownTimerMenus();
                deleteTimerMenus();
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_VENTA, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_RECARGAS, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_VENTA_MINUTOS, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_REPORTE, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_ANULACION, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_TECNICAS, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_ECHO_TEST, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_CONFIG, R.drawable.mainitemunclick));
                break;
            case DefinesBANCARD.ITEM_LEALTAD:
                counterDownTimerMenus();
                deleteTimerMenus();
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_VALE, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_CONSULTA, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_CANJE, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_PROMO, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_REPORTE, R.drawable.mainitemunclick));
                break;
            case DefinesBANCARD.CONFI_LLAVES:
                counterDownTimerMenus();
                deleteTimerMenus();
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.DUKPT, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.MK, R.drawable.mainitemunclick));
                break;
            case DefinesBANCARD.ITEM_VENTA:
                counterDownTimerMenus();
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_VENTA_TARJETA, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_VENTA_SIN_TARJETA, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_VENTA_CON_VUELTO, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_VENTA_SIN_CONTACTO, R.drawable.mainitemunclick));
                break;
            case DefinesBANCARD.ITEM_RECARGAS:
                counterDownTimerMenus();
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_RECARGA_CARGA_ZIMPLE, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_RECARGA_GIRO_ZIMPLE, R.drawable.mainitemunclick));
                itemMenu.add(new menuItemsModelo(DefinesBANCARD.ITEM_RECARGA_EXTRACCION_ZIMPLE, R.drawable.mainitemunclick));
                break;
            default:
                Logger.debug("Case invalid");
                break;
        }
        return itemMenu;
    }


    private void deleteTimerDisplay() {
        if (countDownTimerDisplay != null) {
            countDownTimerDisplay.cancel();
            countDownTimerDisplay = null;
        }
    }

    private void counterDownTimerMenus() {
        if (countDownTimerMenus != null) {
            countDownTimerMenus.cancel();
            countDownTimerMenus = null;
        }
        countDownTimerMenus = new CountDownTimer(30000, 5000) {
            public void onTick(long millisUntilFinished) {
                Log.i("onTick", "init onTick countDownTimer Home");
            }

            public void onFinish() {
                Log.i("onTick", "finish onTick countDownTimer Homet");
                deleteTimerMenus();
                deleteTimerDisplay();
                finish();
            }
        }.start();
    }

    private void deleteTimerMenus() {
        if (countDownTimerMenus != null) {
            countDownTimerMenus.cancel();
            countDownTimerMenus = null;
        }
    }

    public void setBrightness(int brightness1) {
        //Getting Current screen brightness.
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness1);
    }

    @Override
    public void onBackPressed() {
        if (menu.equals(DefinesBANCARD.ITEM_CONFIG_ACCEPTER)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        if (menu.equals(DefinesBANCARD.ITEM_COMERCIO) && (positionpager != 0 && viewPager != null)) {
            viewPager.setCurrentItem(0);
            positionpager = 0;
            return;
        }
        if (!menu.equals(DefinesBANCARD.MENU_PRINCIPAL)) {
            deleteTimerDisplay();
            super.onBackPressed();

            if (isClose && !isDisplay)
                finish();
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deleteTimerMenus();
                        deleteTimerDisplay();
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        contFallback = 0;
        isInit = PolarisUtil.isInitPolaris(menus.this);
    }
}

