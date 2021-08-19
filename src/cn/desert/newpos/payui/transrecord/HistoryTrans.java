package cn.desert.newpos.payui.transrecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.menus.MenusActivity;
import com.newpos.libpay.device.printer.PrintManager;
import com.newpos.libpay.trans.translog.TransLog;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.PayApplication;
import cn.desert.newpos.payui.master.FormularioActivity;

import static com.newpos.libpay.trans.Trans.idLote;

public class HistoryTrans extends FormularioActivity implements View.OnClickListener {

    public static final String EVENTS = "EVENTS";
    public static final String LAST = "LAST";
    public static final String COMMON = "COMMON";
    public static final String ALL = "ALL";
    public static final String ALL_F_REDEN = "ALL_F_REDEN";
    public static final String REPORTE_TOTAL = "REPORTE_TOTAL";
    public static final String REPORTE_DETALLADO = "REPORTE_DETALLADO";
    public static final String BUSQUEDA_CARGO = "BUSQUEDA_CARGO";
    public static final String BUSQUEDA_BOLETA = "BUSQUEDA_BOLETA";

    ListView lvTrans;
    View viewNodata;
    View viewReprint;
    EditText searchEdit;
    ImageView search;
    LinearLayout z;
    LinearLayout root;
    ImageView close;
    Toolbar toolbar;

    private HistorylogAdapter adapter;
    private boolean isSearch = false;
    private boolean isCommonEvents = false;
    private PrintManager manager = null;
    private String tipoBusqueda;
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            viewReprint.setVisibility(View.GONE);
            lvTrans.setVisibility(View.VISIBLE);
            z.setVisibility(View.VISIBLE);
            if (!isCommonEvents) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        PayApplication.getInstance().addActivity(this);

        initToolbar();
        mostrarSerialvsVersion();

        lvTrans = findViewById(R.id.history_lv);
        viewNodata = findViewById(R.id.history_nodata);
        viewReprint = findViewById(R.id.reprint_process);
        searchEdit = findViewById(R.id.history_search_edit);
        search = findViewById(R.id.history_search);
        z = findViewById(R.id.history_search_layout);
        root = findViewById(R.id.transaction_details_root);
        adapter = new HistorylogAdapter(this);
        lvTrans.setAdapter(adapter);
        viewReprint.setVisibility(View.GONE);
        search.setOnClickListener(new SearchListener());
        manager = PrintManager.getmInstance(this, null);
        searchEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String even = bundle.getString(HistoryTrans.EVENTS);
            switch (Objects.requireNonNull(even)) {

                case ALL:
                    printAll(ALL);
                    break;
                case ALL_F_REDEN:
                    printAll(ALL_F_REDEN);
                    break;
                case REPORTE_TOTAL:
                    printReporte();
                    break;
                case REPORTE_DETALLADO:
                    printReporteDetallado();
                    break;
                case BUSQUEDA_BOLETA:
                    tipoBusqueda = BUSQUEDA_BOLETA;
                    searchEdit.setHint("Número de boleta");
                    isCommonEvents = true;
                    break;
                case BUSQUEDA_CARGO:
                    tipoBusqueda = BUSQUEDA_CARGO;
                    searchEdit.setHint("Número de cargo");
                    isCommonEvents = true;
                    break;
                default:
                    isCommonEvents = true;
                    break;
            }
        }

        searchEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    view.clearFocus();
                    new SearchListener().onClick(view);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void printReporteDetallado() {
        final List<TransLogData> list = TransLog.getInstance(idLote).getData();
        new Thread() {
            @Override
            public void run() {
                manager.imprimirReporteDetallado(list);
                myHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void printReporte() {

        final List<TransLogData> list = TransLog.getInstance(idLote).getData();

        ArrayList<String> codigosDeComercios = new ArrayList<>();
        for (TransLogData transLogData : list) {
            if (!codigosDeComercios.contains(transLogData.getCodigoDelNegocio())) {
                codigosDeComercios.add(transLogData.getCodigoDelNegocio());
            }
        }

        for (String codComercio : codigosDeComercios) {
            int cont = 0;
            long montoSuma = 0;
            for (TransLogData transLogData : list) {
                if (codComercio.equals(transLogData.getCodigoDelNegocio())
                        && transLogData.getAmount() != null) {
                    montoSuma += transLogData.getAmount();
                    cont++;

                }
            }
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_id);
        toolbar.setNavigationIcon(R.drawable.ic__back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HistoryTrans.this, MenusActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<TransLogData> list = TransLog.getInstance(idLote).getData();
        List<TransLogData> temp = new ArrayList<>();
        int num = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            temp.add(num, list.get(i));
            num++;
        }
        if (!list.isEmpty()) {
            showView(false);
            adapter.setList(temp);
            adapter.notifyDataSetChanged();
            isSearch = true;
            search.setImageResource(android.R.drawable.ic_menu_search);
        } else {
            showView(true);
        }
    }

    private void showView(boolean isShow) {
        if (isShow) {
            lvTrans.setVisibility(View.GONE);
            viewNodata.setVisibility(View.VISIBLE);
        } else {
            lvTrans.setVisibility(View.VISIBLE);
            viewNodata.setVisibility(View.GONE);
        }
    }

    private void printAll(final String key) {
        new Thread() {
            @Override
            public void run() {
                manager.selectPrintReport(key);
                myHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        if (view.equals(close)) {
            finish();
        }
    }

    private final class SearchListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (isSearch) {
                String edit = searchEdit.getText().toString();
                if (!PAYUtils.isNullWithTrim(edit)) {
                    TransLog transLog = TransLog.getInstance(idLote);
                    TransLogData data;
                    if (tipoBusqueda.equals(BUSQUEDA_BOLETA)) {
                        edit = ISOUtil.padleft(edit, 12, '0');
                        data = transLog.searchTransLogByRNN(edit);
                    } else {
                        edit = ISOUtil.padleft(edit, 6, '0');
                        data = transLog.searchTransLogByNroCargo(edit);
                    }
                    if (data != null) {
                        InputMethodManager imm = (InputMethodManager) HistoryTrans.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(imm).hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
                        List<TransLogData> list = new ArrayList<>();
                        list.add(0, data);
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                        search.setImageResource(android.R.drawable.ic_menu_revert);
                        isSearch = false;
                    } else {
                        UIUtils.toast(HistoryTrans.this, R.drawable.ic_cobranzas_blanca, HistoryTrans.this.getResources().getString(R.string.not_any_record), Toast.LENGTH_SHORT);
                    }
                }
            } else {
                searchEdit.setText("");
                loadData();
            }
        }
    }
}
