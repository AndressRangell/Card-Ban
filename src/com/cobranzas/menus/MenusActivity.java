package com.cobranzas.menus;

import static com.cobranzas.actividades.StartAppBANCARD.tablaComercios;
import static com.newpos.libpay.trans.Trans.idLote;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.actividades.ConfiguracionComercioActivity;
import com.cobranzas.actividades.ConfiguracionTecnicoActivity;
import com.cobranzas.actividades.InfoActivity;
import com.cobranzas.actividades.MainActivity;
import com.cobranzas.adaptadores.AdaptadorSetting;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.Red;
import com.cobranzas.inicializacion.trans_init.Init;
import com.cobranzas.model.ModelSetting;
import com.cobranzas.setting.ListSetting;
import com.cobranzas.tools_bacth.ToolsBatch;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.wposs.cobranzas.R;

import java.util.List;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.master.FormularioActivity;
import cn.desert.newpos.payui.master.MasterControl;
import cn.desert.newpos.payui.transrecord.HistoryTrans;

public class MenusActivity extends FormularioActivity {

    String clase = "MenusActivity.java";
    Bundle bundle;
    TextView textView;
    private Dialog mDialog;
    private boolean tapItem;
    AdaptadorSetting.OnItemClickListener onItemClickListener = new AdaptadorSetting.OnItemClickListener() {
        @Override
        public void onItemClick(View view, ModeloBotones obj, int position) {
            if (!tapItem) {
                tapItem = true;
                Intent intent1 = new Intent();
                switch (obj.getNombreBoton()) {
                    case DefinesBANCARD.ITEM_REIMPRESION:
                        if (!verificacionIncializacion(MenusActivity.this, false)) {
                            if (ToolsBatch.statusTrans(idLote)) {
                                intent1 = new Intent(MenusActivity.this, HistoryTrans.class);
                                intent1.putExtra(HistoryTrans.EVENTS, HistoryTrans.BUSQUEDA_BOLETA);
                                startActivity(intent1);
                                overridePendingTransition(0, 0);
                            } else {
                                UIUtils.toast(MenusActivity.this, R.drawable.ic_cobranzas_blanca, DefinesBANCARD.LOTE_VACIO, Toast.LENGTH_LONG);
                                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                                toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 500);
                            }
                        }
                        tapItem = false;
                        break;
                    case "8":
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.setClass(MenusActivity.this, MasterControl.class);
                        intent1.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[20]);
                        tapItem = false;
                        break;
                    case "9":
                        intent1.putExtra("menu", DefinesBANCARD.INFORMACION);
                        intent1.setClass(MenusActivity.this, InfoActivity.class);
                        tapItem = false;
                        break;
                    case DefinesBANCARD.ITEM_CONFIG_COMERCIO:
                        solicitarContrasena(DefinesBANCARD.PASS_COMERCIO);
                        break;
                    case DefinesBANCARD.ITEM_CONFIG_TECNICO:
                        solicitarContrasena(DefinesBANCARD.PASS_TECNICO);
                        break;
                    case DefinesBANCARD.ITEM_INICIALIZACION:
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.setClass(MenusActivity.this, Init.class);
                        startActivity(intent1);
                        overridePendingTransition(0, 0);
                        tapItem = false;
                        break;
                    case DefinesBANCARD.ITEM_ECHO_TEST:
                        if (!verificacionIncializacion(MenusActivity.this, false)) {
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent1.setClass(MenusActivity.this, MasterControl.class);
                            intent1.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[20]);
                            startActivity(intent1);
                            overridePendingTransition(0, 0);
                        }
                        tapItem = false;
                        break;
                    case DefinesBANCARD.ITEM_WIFI:
                        boolean estadoWifi = false;
                        if (obj.getImageDrw().getConstantState().equals(getDrawable(R.drawable.ic_wifi).getConstantState())) {
                            estadoWifi = true;
                            obj.setImageDrw(getDrawable(R.drawable.ic_wifi_desactivado));
                        } else {
                            estadoWifi = false;
                            obj.setImageDrw(getDrawable(R.drawable.ic_wifi));
                        }
                        cambiarEstadoWifi(estadoWifi);
                        tapItem = false;
                        break;
                    case DefinesBANCARD.ITEM_REVERSAL:
                        TransLogData revesalData = TransLog.getReversal();
                        if (revesalData != null) {
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent1.setClass(MenusActivity.this, MasterControl.class);
                            intent1.putExtra(MasterControl.TRANS_KEY, PrintRes.TRANSEN[32]);
                            startActivity(intent1);
                            overridePendingTransition(0, 0);
                        }
                        tapItem = false;
                        break;
                    default:
                        Toast.makeText(MenusActivity.this, "Otra opcion", Toast.LENGTH_SHORT).show();
                        intent1.setClass(MenusActivity.this, MenusActivity.class);

                        startActivity(intent1);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menus);

        verificacionErrorInyeccionLLaves();

        initToolbar();
        arraySetting();
        mostrarSerialvsVersion();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void verificacionErrorInyeccionLLaves() {
        bundle = getIntent().getExtras();
        if (bundle != null) {
            String mensaje = bundle.getString(DefinesBANCARD.MENSAJE_ERROR_INYECCION_LLAVES, "");
            if (!mensaje.equals("")) {
                ISOUtil.showMensaje(mensaje, MenusActivity.this);
            }
        }
    }

    private void arraySetting() {
        try {
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            inicializarRecyclerView(this, recyclerView);

            List<ModelSetting> modelSettings = ListSetting.getInstanceListMenus(MenusActivity.this);

            AdaptadorSetting adaptadorSetting = new AdaptadorSetting(this, modelSettings);
            recyclerView.setAdapter(adaptadorSetting);
            adaptadorSetting.setOnItemClickListener(onItemClickListener);
        } catch (Exception e) {
            Log.d("ERROR", "arraySetting: " + e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
        }
    }

    private void inicializarRecyclerView(Context context, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initToolbar() {
        // [Esneider] no eliminar
/*       Toolbar toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!verificacionIncializacion(MenusActivity.this, true)) {
                    startActivity(new Intent(MenusActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                }
            }
        });*/

        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setVisibility(View.VISIBLE);
        ivClose.setImageDrawable(getDrawable(R.drawable.ic__back));
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!verificacionIncializacion(MenusActivity.this, true)) {
                    startActivity(new Intent(MenusActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                }
            }
        });

        ImageView ivInfo = findViewById(R.id.iv_Info);
        ivInfo.setImageDrawable(getDrawable(R.drawable.ic_acerca_de));
        ivInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenusActivity.this, InfoActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    private boolean verificacionIncializacion(Context context, boolean isIntentInicializaar) {
        if (bundle != null && bundle.getBoolean(DefinesBANCARD.HABILITARMENUS, false)) {
            if (isIntentInicializaar) {

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MenusActivity.this, Init.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else {
                showCustomDialog(context, "Importante", "Es necesario realizar la inicialización\ndel POS.");
            }
            return true;
        }
        return false;
    }

    private void showCustomDialog(final Context context, final String titulo, final String mensaje) {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(context);
        dialogo1.setTitle(titulo);
        dialogo1.setMessage(mensaje);
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                // Do nothing because of X and Y.
            }
        });
        dialogo1.show();
    }

    public void solicitarContrasena(final String tipoClave) {
        mDialog = UIUtils.centerDialog(MenusActivity.this, R.layout.setting_home_pass, R.id.setting_pass_layout);
        final EditText editText;
        ImageView close;
        ImageButton aceptar;
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        editText = mDialog.findViewById(R.id.editText);
        close = mDialog.findViewById(R.id.setting_pass_close);
        aceptar = mDialog.findViewById(R.id.btnAceptar);

        if (tipoClave.equals(DefinesBANCARD.PASS_COMERCIO)) {
            editText.setHint("Contraseña del comercio");
        } else if (tipoClave.equals(DefinesBANCARD.PASS_TECNICO)) {
            editText.setHint("Contraseña del técnico");
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
                editText.requestFocus();
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarContrasena(tipoClave, editText.getText().toString());
                mDialog.dismiss();
                editText.requestFocus();
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
                if (keyCode == keyEvent.KEYCODE_ENDCALL) {
                    validarContrasena(tipoClave, editText.getText().toString());
                    mDialog.dismiss();
                    editText.requestFocus();
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                return true;
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                tapItem = false;
                editText.clearFocus();
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        editText.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void validarContrasena(String tipoClave, String pass) {
        if (tipoClave.equals(DefinesBANCARD.PASS_COMERCIO)) {
            if (pass.equals(tablaComercios.getClaveComercio().trim())) {
                startActivity(new Intent(MenusActivity.this, ConfiguracionComercioActivity.class));
                overridePendingTransition(0, 0);
            } else {
                UIUtils.toast(MenusActivity.this, R.drawable.ic_cobranzas_blanca, "Contraseña incorrecta", Toast.LENGTH_SHORT);
            }
        } else if (tipoClave.equals(DefinesBANCARD.PASS_TECNICO)) {
            String claveTecnico = Red.getInstance(false).getClaveTecnico();
            if (claveTecnico != null && !claveTecnico.isEmpty() && !claveTecnico.equals("")) {
                if (pass.equals(claveTecnico.trim())) {
                    startActivity(new Intent(MenusActivity.this, ConfiguracionTecnicoActivity.class));
                    overridePendingTransition(0, 0);
                } else {
                    UIUtils.toast(MenusActivity.this, R.drawable.ic_cobranzas_blanca, "Contraseña incorrecta", Toast.LENGTH_SHORT);
                }
            } else {
                UIUtils.toast(MenusActivity.this, R.drawable.ic_cobranzas_blanca, "Clave no establecida", Toast.LENGTH_SHORT);
            }
        }
    }

    private void cambiarEstadoWifi(boolean estadoWifi) {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!estadoWifi) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                Toast.makeText(MenusActivity.this, "Wifi Activado", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                Toast.makeText(MenusActivity.this, "Wifi Desactivado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}
