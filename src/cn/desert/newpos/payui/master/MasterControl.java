package cn.desert.newpos.payui.master;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.desert.keyboard.InputManager;
import com.cobranzas.actividades.MainActivity;
import com.cobranzas.adaptadores.BotonesAdaptador;
import com.cobranzas.adaptadores.ModeloBotones;
import com.cobranzas.adaptadores.ModeloMensajeConfirmacion;
import com.cobranzas.cajas.ApiJson;
import com.cobranzas.cajas.model.ErrorJSON;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.inicializacion.configuracioncomercio.Defered;
import com.cobranzas.inicializacion.init_emv.CAPK_ROW;
import com.cobranzas.inicializacion.init_emv.EMVAPP_ROW;
import com.cobranzas.menus.NewAdapterMenus;
import com.cobranzas.menus.menuItemsModelo;
import com.cobranzas.timertransacciones.TimerTrans;
import com.cobranzas.tools.MenuApplicationsList;
import com.cobranzas.tools.WaitSelectApplicationsList;
import com.cobranzas.transactions.DataAdicional.DataAdicional;
import com.cobranzas.transactions.callbacks.waitResponseFallback;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.PaySdk;
import com.newpos.libpay.PaySdkException;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.device.user.OnUserResultListener;
import com.newpos.libpay.presenter.TransView;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.SlotType;
import com.wposs.cobranzas.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.PayApplication;

import static com.cobranzas.actividades.MainActivity.modoCaja;
import static com.cobranzas.actividades.StartAppBANCARD.tablaCards;
import static com.cobranzas.menus.MenuAction.callBackSeatle;
import static com.cobranzas.menus.menus.FALLBACK;
import static com.cobranzas.menus.menus.contFallback;
import static com.newpos.libpay.trans.finace.FinanceTrans.LOCAL;
import static java.lang.Thread.sleep;

/**
 * Created by zhouqiang on 2017/7/3.
 */

public class MasterControl extends FormularioActivity implements TransView, View.OnClickListener, NewAdapterMenus.OnItemClickListenerMesas {

    public static final String TRANS_KEY = "TRANS_KEY";
    private static final String CASE_INVALID = "case invalid";
    private static final String TIME_DELAY = "Tiempo de espera de ingreso de datos agotado";
    public static String CODIGO = "DATA";
    public static String CAJAS = "CAJAS";
    public static boolean CTL_SIGN;
    public static String HOLDER_NAME;
    public static Context mcontext;
    public static waitResponseFallback callbackFallback;
    public static String field22;
    Button btnConfirm;
    Button btnCancel;
    EditText editCardNO;
    EditText transInfo;
    ImageView close;
    ImageView menu;
    TextView etTitle;
    RadioButton rbMon1;
    RadioButton rbMon2;
    Button btnCancelTypeCoin;
    Button btnAcceptTypeCoin;
    String typeCoin = "1";
    TextView btnCancelInputUser;
    TextView btnAcceptInputUser;
    EditText etInputUser;
    TextView tvInputUser;
    int minEtInputUser;
    int maxEtInputUser;
    Button btnCancelMsg;
    Button btnConfirmMsg;
    EditText etMsgInfo;
    OnUserResultListener listener;
    String clase = "MasterControl.java";
    String inputContent = "";
    TimerTrans timerTrans;


    //Firma
    boolean isSignature;
    boolean isOnSignature;
    //prompt
    TextView tituloPrompt;
    EditText entradaDatos;
    Button btnCancelarPrompt;
    Button btnAceptarPrompt;
    //Tarjeta manual
    FloatingActionButton btnTarjetaManual;
    ProgressBar progressBar;
    StringBuilder builder;
    EditText editText;
    int longitudMaxima = 0;
    String tipoIngreso;
    List<menuItemsModelo> itemMenu;
    // Ingreso numero
    ImageButton numberDone;
    IccReader iccReader0;
    boolean isCajas = false;
    Thread proceso = null;
    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;

    public MasterControl() {
        // Do nothing because of X and Y
    }

    public static String ch2en(String ch) {
        String[] chs = PrintRes.TRANSCH;
        int index = 0;
        for (int i = 0; i < chs.length; i++) {
            if (chs[i].equals(ch)) {
                index = i;
            }
        }
        return PrintRes.TRANSEN[index];
    }

    public static String en2ch(String en) {
        String[] chs = PrintRes.TRANSEN;
        int index = 0;
        for (int i = 0; i < chs.length; i++) {
            if (chs[i].equals(en)) {
                index = i;
            }
        }
        return PrintRes.TRANSCH[index];
    }

    /**
     * Check card exist in table.
     *
     * @param cardNum Numero Tarjeta
     * @return return
     */
    public static boolean incardTable(String cardNum) {

        if (cardNum == null)
            return false;

        if (cardNum.length() < 10)
            return false;

        if (!tablaCards.inCardTable(cardNum, mcontext)) {
            Logger.debug("No se encontraron parametros");
            return false;
        }

        return true;
    }

    public static void setMcontext(Context mcontext) {
        MasterControl.mcontext = mcontext;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PayApplication.getInstance().addActivity(this);

        CTL_SIGN = false;
        HOLDER_NAME = "";
        String type = getIntent().getStringExtra(TRANS_KEY);
        String codigo = getIntent().getStringExtra(CODIGO);
        isCajas = getIntent().getBooleanExtra(CAJAS, false);

        if (contFallback != FALLBACK) {


            switch (type) {
                case Trans.Type.SETTLE:
                case Trans.Type.ECHO_TEST:
                case Trans.Type.AUTO_SETTLE:
                case Trans.Type.INYECCION:
                    break;

                default:
                    //--------PENDIENTE REVISAR INIT EMV----------------
                    EMVAPP_ROW emvappRow = null;
                    emvappRow = EMVAPP_ROW.getSingletonInstance();
                    emvappRow.selectEMVAPP_ROW(MasterControl.this);

                    CAPK_ROW capkRow = null;
                    capkRow = CAPK_ROW.getSingletonInstance();
                    capkRow.selectCAPK_ROW(MasterControl.this);
                    //--------------------------------------------------

                    break;
            }

            if (type.equals(Trans.Type.AUTO_SETTLE)) {
                startTrans(type, null, MasterControl.this, isCajas);
            } else {
                startTrans(type, codigo, MasterControl.this, isCajas);
            }
        } else {
            startTrans(type, codigo, MasterControl.this, isCajas);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (tipoIngreso != null && tipoIngreso.equals(DefinesBANCARD.INGRESO_MONTO) &&
                (tipoIngreso.equals(DefinesBANCARD.INGRESO_MONTO)
                        || tipoIngreso.equals(DefinesBANCARD.INGRESO_TELEFONO)
                        || tipoIngreso.equals(DefinesBANCARD.INGRESO_VUELTO)
                        || tipoIngreso.equals(DefinesBANCARD.INGRESO_PIN)
                        || tipoIngreso.equals(DefinesBANCARD.INGRESO_CUOTAS)
                        || tipoIngreso.equals(DefinesBANCARD.INGRESO_CODIGO))) {
            switch (view.getId()) {
                case R.id.number0:
                    if (tipoIngreso.equals(DefinesBANCARD.INGRESO_MONTO)
                            || tipoIngreso.equals(DefinesBANCARD.INGRESO_VUELTO)
                            || tipoIngreso.equals(DefinesBANCARD.INGRESO_CUOTAS)) {
                        if (builder.length() != 0) {
                            builder.append("0");
                        }
                    } else {
                        if (!builder.toString().equals("0") || !builder.equals("000")) {
                            builder.append("0");
                        }
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
                case R.id.number000:
                    if (builder.length() != 0) {
                        builder.append("000");
                    }
                    mostrarEnTextView();
                    break;
                case R.id.btnClear:
                case R.id.numberAC:
                    builder.setLength(0);
                    mostrarEnTextView();
                    break;
                case R.id.numberDelete:
                    eliminarUltimoCaracter();
                    break;

                default:
                    Logger.debug(CASE_INVALID);
                    break;
            }
        }

        if (view.equals(close)) {
            listener.cancel();
        }

        if (view.equals(btnCancel)) {
            listener.cancel();
        }
        if (view.equals(btnConfirm)) {
            listener.confirm(InputManager.Style.COMMONINPUT);
        }

        //Type Account
        if (view.equals(btnCancelTypeCoin)) {
            listener.cancel();
        }
        if (view.equals(btnAcceptTypeCoin)) {
            inputContent = typeCoin;
            listener.confirm(InputManager.Style.COMMONINPUT);
        }
        if (view.equals(rbMon1)) {
            rbMon1.setChecked(true);
            rbMon2.setChecked(false);
            typeCoin = "1";
        }
        if (view.equals(rbMon2)) {
            rbMon1.setChecked(false);
            rbMon2.setChecked(true);
            typeCoin = "2";
        }

        //ingreso de datos
        if (view.equals(btnAcceptInputUser)) {

            if (etInputUser.getText().toString().equals(""))
                UIUtils.toast(MasterControl.this, R.drawable.ic_cobranzas_blanca, getString(R.string.ingrese_dato), Toast.LENGTH_SHORT);
            else {
                if (etInputUser.length() < minEtInputUser) {
                    UIUtils.toast(MasterControl.this, R.drawable.ic_cobranzas_blanca, getString(R.string.longitud_invalida), Toast.LENGTH_SHORT);
                } else {
                    hideKeyBoard(etInputUser.getWindowToken());
                    inputContent = etInputUser.getText().toString();
                    listener.confirm(InputManager.Style.COMMONINPUT);
                }
            }
        }
        if (view.equals(btnCancelInputUser)) {
            listener.cancel();
        }

        //Show Message
        if (view.equals(btnConfirmMsg)) {
            listener.confirm(InputManager.Style.COMMONINPUT);
        }
        if (view.equals(btnCancelMsg)) {
            listener.cancel();
        }

        //prompt
        if (view.equals(btnAceptarPrompt) && entradaDatos.length() == 0) {

            UIUtils.toast(MasterControl.this, R.drawable.ic_cobranzas_blanca, getString(R.string.ingrese_dato), Toast.LENGTH_SHORT);

        }
        if (view.equals(btnCancelarPrompt)) {
            listener.cancel();
        }

        //Tarjeta Manual
        if (view.equals(btnTarjetaManual)) {
            Snackbar.make(view, "Tarjeta Manual", Snackbar.LENGTH_LONG).show();
            inputContent = "MANUAL";
            listener.confirm(InputManager.Style.COMMONINPUT);
        }
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder item, int position, int id) {
        menuItemsModelo menuModel = itemMenu.get(position);
        if (menuModel != null) {
            switch (menuModel.getTextoItem()) {
                case DefinesBANCARD.ITEM_COMERCIO:
                    inputContent = DefinesBANCARD.ITEM_COMERCIO;
                    listener.confirm(InputManager.Style.COMMONINPUT);
                    break;
                case DefinesBANCARD.ITEM_LEALTAD:
                    inputContent = DefinesBANCARD.ITEM_LEALTAD;
                    listener.confirm(InputManager.Style.COMMONINPUT);
                    break;
                case DefinesBANCARD.ITEM_COBRANZAS:
                    Toast.makeText(this, "No disponible", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Logger.debug(CASE_INVALID);
                    break;
            }
        }
    }

    @Override
    public void showCardView(final String msg, final int timeout, final int mode,
                             final String title, final long amount, final boolean opciones, OnUserResultListener l) {
        this.listener = l;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_insertar_tarjeta);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);

                TextView tvMensaje = findViewById(R.id.tvMensaje);
                if (msg != null && !msg.equals("")) {
                    tvMensaje.setText(msg);
                }

                TextView tv1 = findViewById(R.id.tv1);
                String monto = String.valueOf(amount);
                int len = monto.length();
                if (len > 2) {
                    monto = monto.substring(0, len - 2);
                }
                if (amount == 0) {
                    tv1.setVisibility(View.GONE);
                }
                tv1.setText("Monto compra: " + formatearValor(monto));

                close = (ImageView) findViewById(R.id.iv_close);
                close.setVisibility(View.VISIBLE);

                ImageView imgPos = findViewById(R.id.imgPos);

                int inmodeMag = 0x02;
                int inmodeIc = 0x08;
                int inmodeNfc = 0x10;

                if ((mode & inmodeMag) != 0) {
                    if ((mode & inmodeIc) != 0) {
                        if ((mode & inmodeNfc) == 0) {
                            imgPos.setImageDrawable(getDrawable(R.drawable.ic_pos_mag_ic));
                        }
                    } else {
                        imgPos.setImageDrawable(getDrawable(R.drawable.ic_pos_mag));
                    }
                } else if ((mode & inmodeIc) != 0) {
                    imgPos.setImageDrawable(getDrawable(R.drawable.ic_pos_ic));
                }

                LinearLayout linearOpciones = findViewById(R.id.linearOpciones);
                linearOpciones.setVisibility(View.GONE);
                if (opciones) {
                    linearOpciones.setVisibility(View.VISIBLE);
                }
                close.setOnClickListener(MasterControl.this);
                deleteTimer();
            }
        });
    }

    @Override
    public void showCardNo(final int timeout, final String pan, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_cardno);
                showConfirmCardNO(pan);

                deleteTimer();
            }
        });
    }

    @Override
    public void showMessageInfo(final String title, final String msg, final String btnCancel, final String btnConfirm, final int timeout, OnUserResultListener l) {
        this.listener = l;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_cardno);

                close = (ImageView) findViewById(R.id.iv_close);
                etTitle = (TextView) findViewById(R.id.textView_titleToolbar);
                etMsgInfo = (EditText) findViewById(R.id.cardno_display_area);
                btnCancelMsg = (Button) findViewById(R.id.cardno_cancel);
                btnConfirmMsg = (Button) findViewById(R.id.cardno_confirm);

                close.setVisibility(View.VISIBLE);
                etTitle.setText(title);
                etMsgInfo.setText(msg);
                btnCancelMsg.setText(btnCancel);
                btnConfirmMsg.setText(btnConfirm);

                close.setOnClickListener(MasterControl.this);
                btnCancelMsg.setOnClickListener(MasterControl.this);
                btnConfirmMsg.setOnClickListener(MasterControl.this);

                counterDownTimer(timeout, TIME_DELAY, true, "showMessageInfo");
            }
        });
    }

    @Override
    public void showMessageImpresion(final String title, final String msg, final String btnCancel, final String btnConfirm, final int timeout, OnUserResultListener l) {
        this.listener = l;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_cardno);

                close = (ImageView) findViewById(R.id.iv_close);
                etTitle = (TextView) findViewById(R.id.textView_titleToolbar);
                etMsgInfo = (EditText) findViewById(R.id.cardno_display_area);
                btnCancelMsg = (Button) findViewById(R.id.cardno_cancel);
                btnConfirmMsg = (Button) findViewById(R.id.cardno_confirm);

                close.setVisibility(View.VISIBLE);
                etTitle.setText(title);
                etMsgInfo.setText(msg);
                btnCancelMsg.setText(btnCancel);
                btnConfirmMsg.setText(btnConfirm);

                close.setOnClickListener(MasterControl.this);
                btnCancelMsg.setOnClickListener(MasterControl.this);
                btnConfirmMsg.setOnClickListener(MasterControl.this);

                counterDownTimer(timeout, "", false, "showMessageImpresion");
            }
        });
    }

    @Override
    public String getInput(InputManager.Mode type) {
        return inputContent;
    }

    @Override
    public void showTransInfoView(final int timeout, final TransLogData data, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_transinfo);
                showOrignalTransInfo(data);
                counterDownTimer(timeout, "Tiempo de espera de confirmacion de datos agotado", true, "showTransInfoView");
            }
        });
    }

    @Override
    public void showCardAppListView(int timeout, final String[] apps, OnUserResultListener l) {
        this.listener = l;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                MenuApplicationsList applicationsList = new MenuApplicationsList(MasterControl.this);
                applicationsList.menuApplicationsList(apps, new WaitSelectApplicationsList() {
                    @Override
                    public void getAppListSelect(int idApp) {
                        listener.confirm(idApp);
                    }
                });
            }
        });
    }

    @Override
    public void showMultiLangView(int timeout, String[] langs, OnUserResultListener l) {
        this.listener = l;
    }

    @Override
    public void showSuccess(int timeout, final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtils.startResult(MasterControl.this, true, info);
                deleteTimer();
            }
        });
    }

    @Override
    public void showError(int timeout, final String err) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtils.startResult(MasterControl.this, false, err);
                deleteTimer();
            }
        });
    }

    @Override
    public void showMsgInfo(final int timeout, final String status, final boolean transaccion, final boolean withClose) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_handling);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
                close = (ImageView) findViewById(R.id.iv_close);
                if (withClose) {
                    close.setVisibility(View.VISIBLE);
                }
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showError(timeout, "");

                    }
                });
                progressBar = findViewById(R.id.handling_loading);
                if (transaccion) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                showHanding(status);

                deleteTimer();
            }
        });
    }

    @Override
    public void showMsgInfo(final int timeout, final String status, final String title, final boolean transaccion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_handling);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
                close = (ImageView) findViewById(R.id.iv_close);
                close.setOnClickListener(MasterControl.this);
                etTitle = (TextView) findViewById(R.id.tvDataInfo);
                progressBar = findViewById(R.id.handling_loading);

                if (transaccion) {
                    progressBar.setVisibility(View.INVISIBLE);
                }

                try {
                    etTitle.setVisibility(View.VISIBLE);
                    etTitle.setText(title.replace("_", " "));
                } catch (Exception e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    etTitle.setText(title);
                }

                showHanding(status);

                deleteTimer();
            }
        });
    }

    @Override
    public void showTypeCoinView(final int timeout, final String title, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_menu_tipo_moneda);
                rbMon1 = (RadioButton) findViewById(R.id.rb_moneda1);
                rbMon2 = (RadioButton) findViewById(R.id.rb_moneda2);
                btnCancelTypeCoin = (Button) findViewById(R.id.btn_cancel_mon);
                btnAcceptTypeCoin = (Button) findViewById(R.id.btn_conf_mon);

                rbMon1.setOnClickListener(MasterControl.this);
                rbMon2.setOnClickListener(MasterControl.this);
                btnCancelTypeCoin.setOnClickListener(MasterControl.this);
                btnAcceptTypeCoin.setOnClickListener(MasterControl.this);

                rbMon1.setChecked(true);
                setToolbar(title);
            }
        });
    }

    @Override
    public void showInputUser(final int timeout, final String title, final String label, final int min, final int max, OnUserResultListener l) {
        this.listener = l;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_input_user);

                close = (ImageView) findViewById(R.id.iv_close);
                etTitle = (TextView) findViewById(R.id.textView_titleToolbar);
                close.setVisibility(View.VISIBLE);
                try {
                    etTitle.setText(title.replace("_", " "));
                } catch (Exception e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    etTitle.setText("");
                }

                minEtInputUser = min;
                maxEtInputUser = max;

                etInputUser = (EditText) findViewById(R.id.editText_input);
                etInputUser.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});
                tvInputUser = (TextView) findViewById(R.id.textView_title);
                btnCancelInputUser = (TextView) findViewById(R.id.last4_cancel);
                btnAcceptInputUser = (TextView) findViewById(R.id.last4_confirm);

                close.setOnClickListener(MasterControl.this);
                btnAcceptInputUser.setOnClickListener(MasterControl.this);
                btnCancelInputUser.setOnClickListener(MasterControl.this);

                tvInputUser.setText(label);

                counterDownTimer(timeout, TIME_DELAY, true, "showInputUser");
            }
        });
    }

    @Override
    public void toasTransView(final String errcode, final boolean sound) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sound) {
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_PROP_BEEP2, 2000);
                    toneG.stopTone();
                }
                UIUtils.toast(MasterControl.this, R.drawable.ic_cobranzas_blanca, errcode, Toast.LENGTH_SHORT);
            }
        });

    }

    @Override
    public void showConfirmAmountView(final int timeout, final String title, final String label, final String amnt, final boolean isHTML, OnUserResultListener l) {
        this.listener = l;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_confirm_amount);

                close = (ImageView) findViewById(R.id.iv_close);
                etTitle = (TextView) findViewById(R.id.textView_titleToolbar);
                btnCancel = (Button) findViewById(R.id.btn_cancel_mon);
                btnConfirm = (Button) findViewById(R.id.btn_conf_mon);

                close.setVisibility(View.VISIBLE);
                try {
                    etTitle.setText(title.replace("_", " "));
                } catch (Exception e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    etTitle.setText("");
                }
                close.setOnClickListener(MasterControl.this);
                btnCancel.setOnClickListener(MasterControl.this);
                btnConfirm.setOnClickListener(MasterControl.this);

                EditText total = (EditText) findViewById(R.id.monto_display_area);

                if (isHTML)
                    total.setText(Html.fromHtml(label + " " + amnt));
                else
                    total.setText(label + " " + amnt);

                counterDownTimer(timeout, TIME_DELAY, true, "showConfirmAmountView");
            }
        });
    }

    @Override
    public void showSignatureView(final int timeout, OnUserResultListener l, final String title, final String transType) {
        this.listener = l;
        isSignature = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_signature);
                final EditText editTextCedula = (EditText) findViewById(R.id.editText_cedula);
                final EditText editTextTelefono = (EditText) findViewById(R.id.editText_telefono);
                runTime();
                mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
                mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
                    @Override
                    public void onStartSigning() {
                        isOnSignature = true;
                    }

                    @Override
                    public void onSigned() {
                        mClearButton.setEnabled(true);
                    }

                    @Override
                    public void onClear() {
                        runTime();
                        mClearButton.setEnabled(false);
                        isOnSignature = false;
                    }
                });
                mClearButton = (Button) findViewById(R.id.clear_button);
                mSaveButton = (Button) findViewById(R.id.save_button);
                mSaveButton.setEnabled(true);
                mClearButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSignaturePad.clear();
                    }
                });
                mSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editTextCedula.getText().toString().trim().length() > 5 && isOnSignature) {
                            Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                            saveImage(signatureBitmap);
                            inputContent = editTextCedula.getText().toString() + ";" + editTextTelefono.getText().toString();
                            listener.confirm(InputManager.Style.COMMONINPUT);
                        } else if (!isOnSignature) {
                            UIUtils.showAlertDialog("Informacion", "Debe ingresar firma", MasterControl.this);
                        } else if (editTextCedula.getText().toString().trim().length() <= 5) {
                            UIUtils.showAlertDialog("Informacion", "Debe ingresar cédula", MasterControl.this);
                        }
                    }
                });


            }
        });

    }

    private void runTime() {
        // Do nothing because of X and Y.
    }

    final void saveImage(Bitmap signature) {

        String root = Environment.getExternalStorageDirectory().toString();

        // the directory where the signature will be saved
        File myDir = new File(root + "/saved_signature");

        // make the directory if it does not exist yet
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // set the file name of your choice
        String fname = "signature.png";

        // in our case, we delete the previous file, you can remove this
        File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }

        try {

            // save the signature
            FileOutputStream out = new FileOutputStream(file);
            signature.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();


        } catch (Exception e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            e.printStackTrace();
        }
    }

    @Override
    public void showListView(final int timeout, OnUserResultListener l, final String title, final String transType, final ArrayList<String> listMenu, final int id) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.frag_show_list);
                close = (ImageView) findViewById(R.id.iv_close);
                etTitle = (TextView) findViewById(R.id.textView_titleToolbar);
                menu = (ImageView) findViewById(R.id.iv_menus);

                close.setVisibility(View.VISIBLE);
                menu.setImageResource(id);

                try {
                    etTitle.setText(title.replace("_", " "));
                } catch (Exception e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    etTitle.setText("");
                }

                initList(transType, listMenu);

                close.setOnClickListener(MasterControl.this);

                counterDownTimer(timeout, TIME_DELAY, true, "showListView");
            }
        });

    }

    @Override
    public void showListDeferedView(final int timeout, OnUserResultListener l, final String title, final String transType, final ArrayList<Defered> defered, final int id) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.frag_show_list);
                close = (ImageView) findViewById(R.id.iv_close);
                etTitle = (TextView) findViewById(R.id.textView_titleToolbar);
                menu = (ImageView) findViewById(R.id.iv_menus);

                close.setVisibility(View.VISIBLE);
                menu.setImageResource(id);

                try {
                    etTitle.setText(title.replace("_", " "));
                } catch (Exception e) {
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                    etTitle.setText("");
                }

                initList(defered);

                close.setOnClickListener(MasterControl.this);

                counterDownTimer(timeout, TIME_DELAY, true, "showListDeferedView");
            }
        });
    }

    @Override
    public void showBotonesView(final int timeout, final String titulo, final ArrayList<ModeloBotones> botones, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
                TextView tvTransName = findViewById(R.id.tvTransName);
                tvTransName.setText(titulo);


                counterDownTimer(timeout, TIME_DELAY, true, "showBotonesView");

                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                inicializarRecyclerViewLinear(MasterControl.this, recyclerView);

                ImageView ivClose = (ImageView) findViewById(R.id.iv_close);
                ivClose.setVisibility(View.VISIBLE);
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        inputContent = "CANCEL";
                        listener.confirm(InputManager.Style.COMMONINPUT);
                    }
                });

                BotonesAdaptador adaptador = new BotonesAdaptador(MasterControl.this, botones);
                recyclerView.setAdapter(adaptador);
                adaptador.setOnItemClickListener(new BotonesAdaptador.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, ModeloBotones obj, int position) {
                        deleteTimer();
                        inputContent = obj.getCodBoton();
                        listener.confirm(InputManager.Style.COMMONINPUT);
                    }
                });


            }
        });
    }

    @Override
    public void showMensajeConfirmacionView(final int timeout, final ModeloMensajeConfirmacion modelo, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_mensaje_confirmacion);

                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);

                counterDownTimer(timeout, TIME_DELAY, false, "showMensajeConfirmacionView");

                ImageView ivClose = (ImageView) findViewById(R.id.iv_close);
                ivClose.setVisibility(View.VISIBLE);
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.cancel();
                    }
                });

                ImageView imgBanner = findViewById(R.id.imgBanerBancard);
                TextView tvBanner = findViewById(R.id.tvBannerBancard);
                TextView tvTitulo = findViewById(R.id.tvTitulo);
                ImageView imgData = findViewById(R.id.imgData);
                TextView mensaje = findViewById(R.id.mensaje);
                Button btnSi = findViewById(R.id.btnSi);
                Button btnNo = findViewById(R.id.btnNo);
                TextView tvSubMensaje = findViewById(R.id.tvSubMensaje);

                if (modelo.getBanner() != null) {
                    imgBanner.setVisibility(View.GONE);
                    tvBanner.setVisibility(View.VISIBLE);
                    tvBanner.setText(modelo.getBanner());
                }

                if (modelo.getTitulo() != null) {
                    tvTitulo.setText(modelo.getTitulo());
                }

                if (modelo.getDrawable() != null) {
                    imgData.setVisibility(View.VISIBLE);
                    imgData.setImageDrawable(modelo.getDrawable());
                }

                if (modelo.getMensaje() != null) {
                    mensaje.setText(modelo.getMensaje());
                }

                if (modelo.getMsgBtnAceptar() != null) {
                    btnSi.setVisibility(View.VISIBLE);
                    btnSi.setText(modelo.getMsgBtnAceptar());
                }

                if (modelo.getMsgBtnCancelar() != null) {
                    btnNo.setVisibility(View.VISIBLE);
                    btnNo.setText(modelo.getMsgBtnCancelar());
                }

                if (modelo.getSubMensaje() != null) {
                    tvSubMensaje.setText(modelo.getSubMensaje());
                }

                btnSi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        inputContent = "si";
                        listener.confirm(InputManager.Style.COMMONINPUT);
                    }
                });

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        inputContent = "no";
                        listener.confirm(InputManager.Style.COMMONINPUT);
                    }
                });

            }
        });
    }

    @Override
    public void showContacLessInfoView(final boolean finish) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_remove_ctl);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
                if (!finish) {
                    TextView tvTitulo = findViewById(R.id.tvTitulo);
                    TextView tvSubTitulo = findViewById(R.id.tvSubTitulo);
                    tvSubTitulo.setVisibility(View.VISIBLE);
                    tvTitulo.setText("MANTENGA LA TARJETA");
                    ImageView imageView = findViewById(R.id.iv_remove__card);
                    imageView.setImageDrawable(getDrawable(R.drawable.nfc_acerca));
                }

            }
        });
    }

    @Override
    public void showRetireTarjeta() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_remove_card);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
            }
        });
    }

    @Override
    public void showIngresoDataNumericoView(final int timeout, final String tipo, final String mensajeSecundaio,
                                            final String title, final int longitud,
                                            final String trx, final long amount, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_ingreso_numerico);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);

                ImageView ivClose = (ImageView) findViewById(R.id.iv_close);
                ivClose.setVisibility(View.VISIBLE);
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        if (tipoIngreso.equals(DefinesBANCARD.INGRESO_PIN)) {
                            inputContent = "CANCEL";
                            listener.confirm(InputManager.Style.COMMONINPUT);
                        } else {
                            listener.cancel();
                        }
                    }
                });

                tipoIngreso = tipo;
                builder = new StringBuilder();
                editText = findViewById(R.id.editText);
                switch (tipoIngreso) {
                    case DefinesBANCARD.INGRESO_MONTO:
                    case DefinesBANCARD.INGRESO_VUELTO:
                        editText.setHint("Gs. 0");
                        editText.setGravity(Gravity.CENTER);
                        break;
                    case DefinesBANCARD.INGRESO_TELEFONO:
                        break;
                    case DefinesBANCARD.INGRESO_CODIGO:
                        break;
                    case DefinesBANCARD.INGRESO_PIN:
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        editText.setHint("****");
                        break;
                    default:
                        Logger.debug("Case invalid");
                        break;
                }

                TextView tvTransName = findViewById(R.id.tvTransName);
                tvTransName.setText(trx);
                final TextView tvMensajeSecundaio = findViewById(R.id.tvMensaje02);

                if (mensajeSecundaio != null) {
                    if (!mensajeSecundaio.isEmpty()) {
                        tvMensajeSecundaio.setVisibility(View.VISIBLE);
                        tvMensajeSecundaio.setText(mensajeSecundaio);
                    }
                }

                TextView tvAmount = findViewById(R.id.tvAmount);
                if (tipoIngreso.equals(DefinesBANCARD.INGRESO_MONTO)) {
                    tvAmount.setText("Ingreso de monto");
                } else if (tipoIngreso.equals(DefinesBANCARD.INGRESO_VUELTO)) {
                    tvAmount.setText("Ingreso de vuelto");
                } else {
                    String monto = String.valueOf(amount);
                    int len = monto.length();
                    monto = monto.substring(0, len - 2);
                    tvAmount.setText("Monto compra: " + formatearValor(monto));
                }

                TextView tvMensaje = (TextView) findViewById(R.id.tvMensaje);
                tvMensaje.setText(title);

                longitudMaxima = longitud;

                ImageButton btnOk = findViewById(R.id.btnOk);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (builder.length() != 0) {

                            if (tipoIngreso.equals(DefinesBANCARD.INGRESO_TELEFONO)) {
                                if (builder.length() == longitudMaxima) {
                                    deleteTimer();
                                    inputContent = builder.toString();
                                    listener.confirm(InputManager.Style.COMMONINPUT);
                                } else {
                                    UIUtils.toast(MasterControl.this, R.drawable.ic_cobranzas_blanca, title + " no valido ", Toast.LENGTH_SHORT);
                                }
                            } else {
                                deleteTimer();
                                inputContent = builder.toString();
                                listener.confirm(InputManager.Style.COMMONINPUT);
                            }
                        } else {
                            UIUtils.toast(MasterControl.this, R.drawable.ic_cobranzas_blanca, "Ingrese " + title, Toast.LENGTH_SHORT);
                        }
                    }
                });

                counterDownTimer(timeout, TIME_DELAY, true, "showIngresoDataNumericoView");
            }
        });
    }

    @Override
    public void showSeleccionTipoDeCuentaView(int timeout, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_seleccion_tipo_cuenta);

                Button btnCancelMon = findViewById(R.id.btn_cancel_mon);
                Button btnConfMon = findViewById(R.id.btn_conf_mon);
                final RadioButton cuentaAhorros = findViewById(R.id.rbCAhorros);
                final RadioButton cuentaCorriente = findViewById(R.id.rbCCorriente);
                final RadioButton credito = findViewById(R.id.rbCredito);

                btnConfMon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String cuenta = null;
                        if (cuentaAhorros.isChecked()) {
                            cuenta = DefinesBANCARD.TIPO_AHORROS;
                        }
                        if (cuentaCorriente.isChecked()) {
                            cuenta = DefinesBANCARD.TIPO_CORRIENTE;
                        }
                        if (credito.isChecked()) {
                            cuenta = DefinesBANCARD.TIPO_CREDITO;
                        }
                        if (cuenta != null) {
                            inputContent = cuenta;
                            listener.confirm(InputManager.Style.COMMONINPUT);
                        } else {
                            Toast.makeText(MasterControl.this, "Selecciona el tipo de cuenta.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnCancelMon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.cancel();
                    }
                });

            }
        });
    }

    @Override
    public void showImprimiendoView(int timeout) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_imprimiendo);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
            }
        });
    }

    @Override
    public void showResultView(final int timeout, final boolean aprobada, final boolean isIconoWifi, final boolean opciones, final String mensajeHost, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_result_trans);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
                deleteTimer();
                counterDownTimer(timeout, TIME_DELAY, true, "showResultView");

                ImageView imgView = findViewById(R.id.imgView);
                LinearLayout linearOpciones = findViewById(R.id.linearOpciones);
                linearOpciones.setVisibility(View.VISIBLE);
                if (!aprobada) {
                    if (isIconoWifi && field22 == null) {
                        linearOpciones.setVisibility(View.GONE);
                        TextView tvTitulo = findViewById(R.id.tvTitulo);
                        tvTitulo.setText("ERROR DE CONEXIÓN");
                        imgView.setImageDrawable(getDrawable(R.drawable.ic_icono_bancard));
                    } else {
                        linearOpciones.setVisibility(View.GONE);
                        TextView tvTitulo = findViewById(R.id.tvTitulo);
                        tvTitulo.setText("RECHAZADA");
                        imgView.setImageDrawable(getDrawable(R.drawable.transaccion_fallida));
                    }

                }

                FloatingActionButton buttonF = findViewById(R.id.buttonF);

                if (!opciones) {
                    linearOpciones.setVisibility(View.GONE);
                    buttonF.setVisibility(View.VISIBLE);
                }

                ImageView ivClose = (ImageView) findViewById(R.id.iv_close);
                ivClose.setVisibility(View.VISIBLE);
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        removeCard();
                        listener.cancel();
                    }
                });

                buttonF.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        removeCard();
                    }
                });


                TextView tvMensajeHost = findViewById(R.id.tvMensajeHost);
                if (field22 != null) {
                    tvMensajeHost.setText(field22.trim());
                } else if (mensajeHost != null) {
                    tvMensajeHost.setText(mensajeHost.trim());
                }


                Button btnNoImprimir = findViewById(R.id.btnNoImprmir);

                btnNoImprimir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        removeCard();
                        listener.cancel();
                    }
                });

            }
        });
    }

    @Override
    public void showResultView(final int timeout, final String emcabezado, final boolean aprobada, final boolean isIconoWifi, final boolean opciones, final String mensajeHost, final OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.vista_result_trans);
                TextView tvSerial = findViewById(R.id.tvSerial);
                TextView tvVersion = findViewById(R.id.tvVersion);
                mostrarSerialvsVersion(tvVersion, tvSerial);
                deleteTimer();
                counterDownTimer(timeout, TIME_DELAY, true, "showResultView");

                ImageView imgView = findViewById(R.id.imgView);
                LinearLayout linearOpciones = findViewById(R.id.linearOpciones);

                TextView tvTitulo = findViewById(R.id.tvTitulo);
                tvTitulo.setText(emcabezado);

                linearOpciones.setVisibility(View.VISIBLE);
                if (!aprobada) {
                    if (isIconoWifi && field22 == null) {
                        linearOpciones.setVisibility(View.GONE);
                        imgView.setImageDrawable(getDrawable(R.drawable.ic_icono_bancard));
                    } else {
                        linearOpciones.setVisibility(View.GONE);
                        imgView.setImageDrawable(getDrawable(R.drawable.transaccion_fallida));
                    }

                }

                FloatingActionButton buttonF = findViewById(R.id.buttonF);

                if (!opciones) {
                    linearOpciones.setVisibility(View.GONE);
                    buttonF.setVisibility(View.VISIBLE);
                }

                ImageView ivClose = (ImageView) findViewById(R.id.iv_close);
                ivClose.setVisibility(View.VISIBLE);
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        removeCard();
                    }
                });

                buttonF.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        removeCard();
                    }
                });

                if (modoCaja && !aprobada) {
                    if (mensajeHost != null) {
                        mensajeErrorCajas(mensajeHost.trim());

                    }
                }

                TextView tvMensajeHost = findViewById(R.id.tvMensajeHost);
                if (field22 != null) {
                    tvMensajeHost.setText(field22.trim());
                } else if (mensajeHost != null) {
                    tvMensajeHost.setText(mensajeHost.trim());
                }


                Button btnNoImprimir = findViewById(R.id.btnNoImprmir);

                btnNoImprimir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTimer();
                        removeCard();
                    }
                });

            }
        });
    }

    private void mensajeErrorCajas(String mensaje) {
        ErrorJSON error = new ErrorJSON();
        error.setStatusCode(400);
        error.setError("Bad Request");
        error.setMessage(mensaje);
        if (ApiJson.listener != null) {
            ApiJson.listener.rsp2Cajas(error, "400");
        } else {
            Log.d("ERROR", "mensajeErrorCajas: " + " ApiJson.listener == null");
        }

    }

    @Override
    public void showFinishView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeCard();
            }
        });
    }

    private void removeCard() {
        if (proceso == null || !proceso.isAlive()) {
            proceso = new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iccReader0 = IccReader.getInstance(SlotType.USER_CARD);
                            if (iccReader0.isCardPresent()) {
                                setContentView(R.layout.activity_remove_card);
                                TextView tvSerial = findViewById(R.id.tvSerial);
                                TextView tvVersion = findViewById(R.id.tvVersion);
                                mostrarSerialvsVersion(tvVersion, tvSerial);
                            }
                        }
                    });
                    if (validarICC()) {
                        UIUtils.startView(MasterControl.this, MainActivity.class, "");
                        if (callBackSeatle != null)
                            callBackSeatle.getRspSeatleReport(0);
                    }
                }
            });
            proceso.start();
        }
    }

    private boolean validarICC() {
        iccReader0 = IccReader.getInstance(SlotType.USER_CARD);
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        while (true) {
            try {
                if (iccReader0.isCardPresent()) {
                    toneG.startTone(ToneGenerator.TONE_PROP_BEEP2, 2000);
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        Logger.error("Exception" + e.toString());
                        Thread.currentThread().interrupt();
                    }

                } else {
                    return true;
                }
            } catch (Exception e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                return true;
            }
        }
    }

    private void eliminarUltimoCaracter() {
        int len = builder.length();
        if (len != 0) {
            builder.deleteCharAt(len - 1);
        }
        mostrarEnTextView();
    }

    private void mostrarEnTextView() {
        int len = builder.length();
        if (len <= longitudMaxima) {
            switch (tipoIngreso) {
                case DefinesBANCARD.INGRESO_MONTO:
                case DefinesBANCARD.INGRESO_VUELTO:
                    editText.setText(formatearValor(builder.toString()));
                    break;
                case DefinesBANCARD.INGRESO_TELEFONO:
                    editText.setText(PAYUtils.FormarPhonePyg(builder.toString()));
                    break;
                case DefinesBANCARD.INGRESO_CUOTAS:
                    editText.setText(builder.toString());
                    break;
                case DefinesBANCARD.INGRESO_CODIGO:
                case DefinesBANCARD.INGRESO_PIN:
                    setSizeText(len);
                    editText.setText(builder.toString());
                    break;
                default:
                    Logger.debug("Case invalid");
                    break;
            }
        } else {
            eliminarUltimoCaracter();
        }

    }

    private void setSizeText(int len) {
        if (len > 13 && len < 18) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        }
        if (len > 18 && len < 28) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }
        if (len > 28 && len < 33) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }
    }

    private String formatearValor(String dato) {
        dato = dato.replace(".", "");


        if (dato.equals("")) {
            return "Gs. 0";
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
            return "Gs. " + dato1.toString();
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

        return "Gs. " + salida;
    }

    private void initList(String transType, final ArrayList<String> listMenu) {
        final ListView listview = (ListView) findViewById(R.id.simpleListView);
        final StableArrayAdapter adapter = new StableArrayAdapter(MasterControl.this, android.R.layout.simple_list_item_1, listMenu);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = String.valueOf(parent.getItemIdAtPosition(position));
                view.animate().setDuration(500).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {

                                if (!item.equals("")) {
                                    inputContent = item;
                                    listener.confirm(InputManager.Style.COMMONINPUT);
                                }

                            }
                        });
            }

        });
    }

    private void initList(ArrayList<Defered> defered) {
        final ListView listview = (ListView) findViewById(R.id.simpleListView);
        ArrayList<String> list = new ArrayList<>();
        Iterator<Defered> itrDefered = defered.iterator();

        while (itrDefered.hasNext()) {
            Defered deferedSel = itrDefered.next();
            list.add(deferedSel.getDescripcionPlan());
        }
        final StableArrayAdapter adapter = new StableArrayAdapter(MasterControl.this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = String.valueOf(parent.getItemIdAtPosition(position));
                view.animate().setDuration(500).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {

                                if (!item.equals("")) {
                                    inputContent = item;
                                    listener.confirm(InputManager.Style.COMMONINPUT);
                                }

                            }
                        });
            }

        });
    }

    private void startTrans(String type, String codigo, Activity activity, boolean isCajas) {
        try {
            PaySdk.getInstance().startTrans(type, codigo, this, activity, isCajas);
        } catch (PaySdkException e) {
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Logger.error("Exception" + e.toString());
        }
    }

    private void showConfirmCardNO(String pan) {
        btnConfirm = (Button) findViewById(R.id.cardno_confirm);
        btnCancel = (Button) findViewById(R.id.cardno_cancel);
        editCardNO = (EditText) findViewById(R.id.cardno_display_area);
        btnCancel.setOnClickListener(MasterControl.this);
        btnConfirm.setOnClickListener(MasterControl.this);
        editCardNO.setText(pan);
    }

    private void showOrignalTransInfo(TransLogData data) {
        btnConfirm = (Button) findViewById(R.id.transinfo_confirm);
        btnCancel = (Button) findViewById(R.id.transinfo_cancel);
        btnCancel.setOnClickListener(MasterControl.this);
        btnConfirm.setOnClickListener(MasterControl.this);
        transInfo = (EditText) findViewById(R.id.transinfo_display_area);

        StringBuilder info = new StringBuilder();

        info.append("<b>" + getString(R.string.void_original_trans) + "</b>" + " ");
        info.append(data.getEName().replace("_", " "));
        info.append("<br/>");
        info.append("<b>" + getString(R.string.void_card_no) + "</b>");
        info.append(" ");
        info.append(data.getPan() + "<br/>");
        info.append("<b>" + getString(R.string.void_trace_no) + "</b>");
        info.append(" ");
        info.append(data.getTraceNo() + "<br/>");
        if (!PAYUtils.isNullWithTrim(data.getAuthCode())) {
            info.append("<b>" + getString(R.string.void_auth_code) + "</b>");
            info.append(" ");
            info.append(data.getAuthCode() + "<br/>");
        }
        info.append("<b>" + getString(R.string.void_batch_no) + "</b>");
        info.append(" ");
        info.append(data.getBatchNo() + "<br/>");

        if (data.getTypeCoin().equals(LOCAL)) {
            info.append("<b>" + getString(R.string.void_amount) + "</b>");
            info.append(" $. ");
            info.append(PAYUtils.getStrAmount(data.getAmount()) + "<br/>");
        } else {
            info.append("<b>" + getString(R.string.void_amount) + "</b>");
            info.append(" $ ");
            info.append(PAYUtils.getStrAmount(data.getAmount()) + "<br/>");
        }
        info.append("<b>" + getString(R.string.void_time) + "</b>");
        info.append(" ");
        info.append(PAYUtils.printStr(data.getLocalDate(), data.getLocalTime()));

        transInfo.setText(Html.fromHtml(info.toString()));
    }

    private void showHanding(String msg) {
        TextView tv = (TextView) findViewById(R.id.handing_msginfo);
        tv.setText(msg);
    }

    private void setToolbar(String titleToolbar) {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        String title = "<h4>" + titleToolbar + "</h4>";
        toolbar.setTitle(Html.fromHtml(title));
        toolbar.setLogo(R.drawable.ic_cobranzas_blanca);
        toolbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                setSupportActionBar(toolbar);
            }
        }, 0);
    }

    private void hideKeyBoard(IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    @Override
    public void onBackPressed() {
        // Do nothing because of X and Y.
    }

    @Override
    public void showCardViewImg(final String img, OnUserResultListener l) {
        this.listener = l;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.trans_show_card_image);
                loadWebGifImg(img);
            }
        });
    }

    private void loadWebGifImg(String nameCard) {

        ImageView wvInsert = (ImageView) findViewById(R.id.webview_card_img);

        switch (nameCard.trim()) {
            case "0"://visa
                wvInsert.setImageResource(R.drawable.visa);
                break;
            case "1"://Master
                wvInsert.setImageResource(R.drawable.mastercard);
                break;
            case "2"://Amex
                wvInsert.setImageResource(R.drawable.amex);
                break;
            case "3"://Diners
                wvInsert.setImageResource(R.drawable.diners);
                break;
            case "4"://Visa Electron
                wvInsert.setImageResource(R.drawable.electron);
                break;
            case "5"://Maestro
                wvInsert.setImageResource(R.drawable.maestro);
                break;
            case "6"://
                wvInsert.setImageResource(R.drawable.infonet_other);
                break;
            case "payclub"://PAYCLUB
                wvInsert.setImageResource(R.drawable.payclub);
                break;
            case "wallet"://PAYBLUE
                wvInsert.setImageResource(R.drawable.payblue);
                break;
            default:
                break;
        }
    }

    /**
     * se agrega booleano para la pantalla de imprimir copia no utilice
     * el Resultcontrol y finalice la actividad
     */
    private void counterDownTimer(int timeout, final String mensaje, final boolean usarStar, final String metodo) {
        timerTrans.getInstanceTimerTrans(timeout, mensaje, metodo, new TimerTrans.OnResultTimer() {
            @Override
            public void rsp2Timer() {
                if (modoCaja && isCajas && usarStar) {
                    mensajeErrorCajas("Tiempo agotado");
                }
                if (usarStar) {
                    Log.e("onFinish", "startResult - " + metodo);
                    UIUtils.startResult(MasterControl.this, false, mensaje);
                    listener.cancel();
                } else {
                    Log.e("onFinish", "confirm - " + metodo);
                    listener.confirm(0);
                }
            }
        });
    }

    private void deleteTimer() {
        timerTrans.deleteTimer();
    }

    private void encenderPantalla(final Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<>();

        StableArrayAdapter(Context context, int textViewResourceId,
                           List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
