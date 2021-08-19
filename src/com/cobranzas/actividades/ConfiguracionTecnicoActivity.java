package com.cobranzas.actividades;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cobranzas.adaptadores.Adaptador;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.menus.MenusActivity;
import com.cobranzas.setting.ListSetting;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.trans.translog.TransLog;
import com.pos.device.SDKException;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;
import com.pos.device.ped.Ped;
import com.wposs.cobranzas.R;

import java.io.File;
import java.util.List;

import Interactor.View.CustomDialog;
import Interactor.View.CustomDialogImpl;
import cn.desert.newpos.payui.master.FormularioActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.cobranzas.actividades.StartAppBANCARD.readWriteFileMDM;
import static com.cobranzas.inicializacion.trans_init.Init.DEFAULT_DOWNLOAD_PATH;
import static com.newpos.libpay.trans.Trans.idLote;

public class ConfiguracionTecnicoActivity extends FormularioActivity {

    String clase = "ConfiguracionTecnicoActivity.java";
    TextView tvTitulo;
    RecyclerView rvMenus;
    Toolbar toolbar;
    Dialog mDialog;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initToolbar();

        tvTitulo = findViewById(R.id.tvTitulo);
        tvTitulo.setText("Configuración técnico");
        rvMenus = findViewById(R.id.rvMenus);

        cargarMenus();

        mostrarSerialvsVersion();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void cargarMenus() {
        rvMenus.setLayoutManager(new GridLayoutManager(ConfiguracionTecnicoActivity.this, 3));
        rvMenus.setHasFixedSize(true);
        rvMenus.setNestedScrollingEnabled(false);

        List<ModeloBotones> modePlansList = ListSetting.getInstanceListadoTecnico(ConfiguracionTecnicoActivity.this);
        Adaptador adaptador = new Adaptador(ConfiguracionTecnicoActivity.this, modePlansList);
        adaptador.setLayout(R.layout.item_menu_);
        rvMenus.setAdapter(adaptador);
        adaptador.setOnItemClickListener(onItemClickListener);
    }

    Adaptador.OnItemClickListener onItemClickListener = new Adaptador.OnItemClickListener() {
        @Override
        public void onItemClick(View view, ModeloBotones obj, int position) {
            Intent intent = new Intent();
            switch (obj.getNombreBoton()) {
                case DefinesBANCARD.ITEM_CONFIG_RED:
                    intent.setClass(ConfiguracionTecnicoActivity.this, ConfigRedActivity.class);
                    startActivity(intent);
                    break;
                case DefinesBANCARD.ITEM_DETALLE_INICIALIZACION:
                    intent.setClass(ConfiguracionTecnicoActivity.this, DetallesInicializacionActivity.class);
                    startActivity(intent);
                    break;
                case DefinesBANCARD.DUKPT:
                case DefinesBANCARD.MK:
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ConfiguracionTecnicoActivity.this, ConfiActivity.class);
                    intent.putExtra("menu", obj.getNombreBoton());
                    startActivity(intent);
                    break;
                case DefinesBANCARD.ITEM_ELIMINAR_LLAVES:
                    solicitarEliminarLLaves(ConfiguracionTecnicoActivity.this);
                    break;
                case DefinesBANCARD.ITEM_LIMPIAR_DATOS:
                    restartApp(ConfiguracionTecnicoActivity.this,
                            "¿Borrar datos?", "Atención se perderá toda la información del POS");
                    break;

                default:
                    Logger.debug("Caso no definido");
                    break;

            }
        }
    };

    private void restartApp(final Context context, final String titulo, final String mensaje) {
        SweetAlertDialog confirmDialog = new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setCustomImage(R.drawable.ic_limpiar_datos)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmButtonBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .setCancelButtonBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .setConfirmButton("Sí, Eliminar", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        clearData(context);
                    }
                })
                .setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                });
        confirmDialog.show();
    }

    public static void reLaunchApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    private void clearData(Context context) {
        eliminarCache(getApplicationContext(), clase);
        String[] paths = {DEFAULT_DOWNLOAD_PATH, getApplicationInfo().dataDir};
        for (String path : paths) {
            eliminarDatos(new File(path), clase);
        }
        clearTranslog();
        readWriteFileMDM.writeFileMDM(readWriteFileMDM.IS_TRANSACCION_DEACTIVE);
        readWriteFileMDM.writeFileMDM(readWriteFileMDM.SETTLE_DEACTIVE, readWriteFileMDM.REVERSE_DEACTIVE);
        showCustomDialog(context, "Importante", "El cache y los Datos fueron borrados con Exito, Se reiniciara la aplicación.");
    }

    private void clearTranslog() {
        TransLog.clearReveral();
        TransLog.clearScriptResult();
        TransLog.getInstance(idLote).clearAll(idLote);
    }


    @SuppressLint("ResourceType")
    private void solicitarEliminarLLaves(Context context) {
        CustomDialog customDialog = new CustomDialogImpl(context);
        customDialog.setIcono(R.drawable.ic_keys);
        customDialog.setTextViewTitulo("¿Eliminar llaves?");
        customDialog.setTextoBotonAcceptar("Sí, eliminar");
        customDialog.setTextoBotonCancelar("No");
        customDialog.setTextViewDescripcion("Cuidado. Si continúa, su POS ya no podrá realizar transacciones.");
        customDialog.showCustomDialog();
        customDialog.setEventoDialog(new CustomDialog.EventoDialog() {
            @Override
            public void confirmacionEvent() {
                try {
                    Ped.getInstance().deleteKey(KeySystem.DUKPT_DES, KeyType.KEY_TYPE_DUKPTK, 1);
                    Ped.getInstance().deleteKey(KeySystem.MS_DES, KeyType.KEY_TYPE_MASTK, 0);
                } catch (SDKException e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                startActivity(new Intent(ConfiguracionTecnicoActivity.this, StartAppBANCARD.class));
            }

            @Override
            public void cancelacionEvent() {
                // Do nothing because of X and Y.
            }
        });
    }


    private void showCustomDialog(final Context context, final String titulo, final String mensaje) {
        Log.e(clase, "showCustomDialog: mensaje: " + mensaje);
        counterDownTimer(context, 3000, "showCustomDialog");
        SweetAlertDialog infoDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(titulo)
                .setContentText(mensaje)
                .setConfirmButtonBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .setConfirmButton("Aceptar", new SweetAlertDialog.OnSweetClickListener() {
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        deleteTimer();
                        reLaunchApp(context);
                    }
                });
        infoDialog.show();
    }

    private void deleteTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            Log.e("Mensaje ", "deleteTimer: " + " Finalizado");
        }
    }

    private void counterDownTimer(final Context context, final int timeout, final String metodo) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countDownTimer = new CountDownTimer(timeout, 500) {
            public void onTick(long millisUntilFinished) {
                Log.e("onTick", "init onTick countDownTimer " + metodo + " " + millisUntilFinished);
            }

            public void onFinish() {
                countDownTimer.cancel();
                countDownTimer = null;
                reLaunchApp(context);
            }
        }.start();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ConfiguracionTecnicoActivity.this, MenusActivity.class));
            }
        });
    }
}
