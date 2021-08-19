package com.cobranzas.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.adaptadores.recy.AdaptadorRcyContenedor;
import com.cobranzas.adaptadores.recy.AdaptadorRcyWidget;
import com.cobranzas.adaptadores.recy.AdapterRcy2Widget;
import com.cobranzas.adaptadores.recy.AdapterRecyItem;
import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.encrypt_data.TripleDES;
import com.cobranzas.inicializacion.configuracioncomercio.ChequeoIPs;
import com.cobranzas.inicializacion.configuracioncomercio.IPS;
import com.cobranzas.keys.DUKPT;
import com.cobranzas.keys.InjectMasterKey;
import com.cobranzas.model.Contenedor;
import com.cobranzas.model.ContenedorRcyModel;
import com.cobranzas.model.ContenedorRcyWidgetM;
import com.cobranzas.model.Item;
import com.cobranzas.model.widgets.EdiTextWMbtn;
import com.cobranzas.model.widgets.EdiTextWidgetModel;
import com.cobranzas.model.widgets.IpEdiTextWidgetModel;
import com.cobranzas.model.widgets.ItemWidget;
import com.cobranzas.model.widgets.LayoutManagerRcy;
import com.cobranzas.model.widgets.SwitchWidgetModel;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;
import com.pos.device.SDKException;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;
import com.pos.device.ped.Ped;
import com.wposs.cobranzas.R;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import cn.desert.newpos.payui.master.FormularioActivity;

import static com.cobranzas.actividades.StartAppBANCARD.isInit;
import static com.cobranzas.actividades.StartAppBANCARD.listadoIps;
import static com.cobranzas.keys.InjectMasterKey.MASTERKEYIDX;
import static com.cobranzas.keys.InjectMasterKey.TRACK2KEYIDX;
import static com.cobranzas.keys.InjectMasterKey.threreIsKey;
import static com.cobranzas.keys.InjectMasterKey.threreIsKeyWK;

public class ConfiActivity extends FormularioActivity implements View.OnClickListener {
    private static final String NOHEXA = "No es un componente hexadecimal";
    private static final String LOGINVAL = "Longitud invalida";
    private static final String VALDEFAULT = "0000000000000000";
    String clase = "ConfiActivity.java";
    private String menu = "";
    private IPS ip;
    private List<Item> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ip = IPS.getSingletonInstance(this);
        if (getIntent().hasExtra("menu")) {
            menu = getIntent().getStringExtra("menu");
        }


        list = getItems(menu);

        AdaptadorRcyContenedor adapter;
        switch (menu) {

            case DefinesBANCARD.ITEM_CONFIG_RED:
                AdaptadorRcyWidget item = new AdaptadorRcyWidget(R.layout.itemw_configuracion);

                AdaptadorRcyContenedor itemContenedor = new AdaptadorRcyContenedor(R.layout.contenedor_configuracion2, item);
                itemContenedor.setLayoutManager(new LayoutManagerRcy(this));

                adapter = new AdaptadorRcyContenedor(list, this, R.layout.contenedor_configuracion, itemContenedor);
                adapter.setLayoutManager(new LayoutManagerRcy(this, 0));

                break;

            case DefinesBANCARD.MK:
            case DefinesBANCARD.DUKPT:

                AdapterRcy2Widget adapterRcy2Widget = new AdapterRcy2Widget(R.layout.itemw_configuracion_btn);
                adapter = new AdaptadorRcyContenedor(list, this, R.layout.contenedor_configuracion, adapterRcy2Widget);

                break;
            default:

                AdapterRecyItem adaptadorRcyWidget = new AdaptadorRcyWidget(R.layout.itemw_configuracion);
                adapter = new AdaptadorRcyContenedor(list, this, R.layout.contenedor_configuracion, adaptadorRcyWidget);

                break;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        ImageView imageView = findViewById(R.id.iv_close);
        imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_save_white, null));
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(this);
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = 60;
        layoutParams.height = 60;
        imageView.setLayoutParams(layoutParams);

        mostrarSerialvsVersion();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private List<Item> getItems(String menu) {
        EdiTextWMbtn ediTextWMbtn;
        EdiTextWMbtn ediTextWMbtn2;

        List<Item> result = new ArrayList<>();

        List<Item> items = new ArrayList<>();
        EdiTextWidgetModel ediText;
        SwitchWidgetModel aSwitch;

        switch (menu) {
            case DefinesBANCARD.ITEM_CONFIG_RED:
                if (isInit) {

                    IpEdiTextWidgetModel ipEditext1;
                    List<Contenedor> itemsContenedores = new ArrayList<>();
                    IPS nIP;
                    for (int i = 0; i < ChequeoIPs.getLengIps(); i++) {
                        nIP = ChequeoIPs.seleccioneIP(i);
                        ipEditext1 = new IpEdiTextWidgetModel(IPS.fieldsEdit[0], 2, true, this);
                        ipEditext1.setIPHint(nIP.getIP());
                        ipEditext1.setDiseño(1);
                        items.add(ipEditext1);

                        ediText = new EdiTextWidgetModel(IPS.fieldsEdit[1], 0, 0, InputType.TYPE_CLASS_NUMBER, true, this);
                        ediText.setHint(nIP.getPuerto());
                        ediText.setDiseño(0);
                        items.add(ediText);

                        aSwitch = new SwitchWidgetModel(IPS.fieldsEdit[2], 0, this);
                        aSwitch.setChecked(String.valueOf(nIP.isTls()));
                        aSwitch.setDiseño(0);
                        items.add(aSwitch);
                        itemsContenedores.add(new ContenedorRcyWidgetM(nIP.getIdIp(), items));
                        items = new ArrayList<>();
                    }
                    result.add(new ContenedorRcyModel("Conexión", eliminarRepetidos(itemsContenedores)));

                }

                break;


            case DefinesBANCARD.CONFI_PARAMETROS_ADQUIRIENTE:


                break;

            case DefinesBANCARD.DUKPT:
                ediTextWMbtn = new EdiTextWMbtn("IPEK (Initial Pin Encryption Key)", 0, 32, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, false, this);
                ediTextWMbtn.setHint(VALDEFAULT);
                ediTextWMbtn.clearFocus();
                Button button2 = new Button(this);
                button2.setWidth(40);
                button2.setHeight(20);
                button2.setText("SCAN");
                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing because of X and Y.
                    }
                });


                ediTextWMbtn.setButton(button2);
                ediTextWMbtn.setDiseño(0);
                items.add(ediTextWMbtn);


                ediText = new EdiTextWidgetModel("KSN (Key Serial Number)", 0, 20, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, false, this);
                ediText.clearFocus();
                ediText.setHint(TripleDES.getKsnInicial());
                ediText.setDiseño(0);
                items.add(ediText);


                ContenedorRcyModel contenedorRcyModel = new ContenedorRcyModel("DUKPT \n(Derived Unique Key Per Transaction)", items);
                result.add(contenedorRcyModel);
                break;
            case DefinesBANCARD.MK:

                ediTextWMbtn = new EdiTextWMbtn("Componente 1", 0, 32, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, false, this);
                ediTextWMbtn.setHint(VALDEFAULT);
                ediTextWMbtn.clearFocus();
                Button button = new Button(this);
                button.setWidth(40);
                button.setHeight(20);
                button.setText("SCAN");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing because of X and Y.
                    }
                });

                ediTextWMbtn.setButton(button);
                ediTextWMbtn.setDiseño(0);
                items.add(ediTextWMbtn);


                ediTextWMbtn2 = new EdiTextWMbtn("Componente 2", 0, 32, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, false, this);
                ediTextWMbtn2.setHint(VALDEFAULT);
                ediTextWMbtn2.clearFocus();
                Button button3 = new Button(this);
                button3.setWidth(40);
                button3.setHeight(20);
                button3.setText("SCAN");
                button3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing because of X and Y.
                    }
                });

                ediTextWMbtn2.setButton(button3);
                ediTextWMbtn2.setDiseño(0);
                items.add(ediTextWMbtn2);


                contenedorRcyModel = new ContenedorRcyModel("Master Key", items);
                result.add(contenedorRcyModel);
                break;
            default:
        }

        return result;
    }


    @Override
    public void onClick(View view) {

        ContenedorRcyWidgetM contenedorWidget;
        Contenedor contenedor;
        String msg = "";
        String[] args;
        String[] rowToModificate;

        ItemWidget itemWidget;
        switch (menu) {
            case DefinesBANCARD.ITEM_CONFIG_RED:
                boolean cambios = false;
                for (Item contenedores : list) {
                    contenedor = (Contenedor) contenedores;
                    for (Item item : contenedor.getList()) {
                        contenedorWidget = (ContenedorRcyWidgetM) item;
                        rowToModificate = contenedorWidget.getRowToModificate();
                        args = contenedorWidget.getArgsToModificate();
                        if (rowToModificate.length != 0 && args.length == rowToModificate.length &&
                                ip.updateSelectIps(contenedorWidget.getTitulo(), rowToModificate, args, this)) {
                            cambios = true;
                        }
                    }
                }

                if (cambios) {
                    listadoIps = ChequeoIPs.selectIP(ConfiActivity.this);
                }
                onBackPressed();

                break;

            case DefinesBANCARD.DUKPT:
                try {
                    contenedor = (Contenedor) list.get(0);
                    itemWidget = (ItemWidget) contenedor.getList().get(0);
                    if (validacionLength32(itemWidget, LOGINVAL) && validacionHexadecimal(itemWidget, NOHEXA)) {
                        if (DUKPT.injectIPEK(itemWidget.getResultado()) == 0) {
                            Toast.makeText(ConfiActivity.this, "Llave IPEK inyectada correctamente", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, ConfiguracionTecnicoActivity.class));
                        } else {
                            Toast.makeText(ConfiActivity.this, "Fallo en la inyeccion de la llave", Toast.LENGTH_SHORT).show();
                        }

                    }
                } catch (SDKException e) {
                    e.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                }
                return;

            case DefinesBANCARD.MK:
                contenedor = (Contenedor) list.get(0);
                itemWidget = (ItemWidget) contenedor.getList().get(0);
                ItemWidget itemWidget2 = (ItemWidget) contenedor.getList().get(1);
                if (validacionLength32(itemWidget2, LOGINVAL) && validacionHexadecimal(itemWidget2, NOHEXA) && validacionHexadecimal(itemWidget, NOHEXA) && validacionLength32(itemWidget, LOGINVAL)) {
                    byte[] encrypted2 = TripleDES.xor(ISOUtil.hex2byte(itemWidget.getResultado()), ISOUtil.hex2byte(itemWidget2.getResultado()));
                    byte[] dataEncrypted = TripleDES.cryptBytes(encrypted2, 0, encrypted2);

                    verificacion2eliminacionLLaves();
                    if (InjectMasterKey.injectMk(ISOUtil.byte2hex(encrypted2)) == 0) {
                        msg = "Master Key inyectada correctamente";
                        InjectMasterKey.injectWorkingKey(ISOUtil.byte2hex(dataEncrypted));
                        startActivity(new Intent(this, ConfiguracionTecnicoActivity.class));

                    } else {
                        msg = "Fallo en la inyeccion de la llave";
                    }

                }
                break;
            default:
        }

        if (!msg.equals("")) {
            Toast.makeText(ConfiActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validacionHexadecimal(ItemWidget itemWidget, String msg) {
        boolean hexa = false;
        if (itemWidget.getResultado().matches("[0-9a-fA-F]+")) {
            hexa = true;
        } else {
            Toast.makeText(ConfiActivity.this, msg + " de " + itemWidget.getTituloItem(), Toast.LENGTH_SHORT).show();
        }
        return hexa;
    }

    private void verificacion2eliminacionLLaves() {
        if (verificarLlaves()) {
            try {
                Ped.getInstance().deleteKey(KeySystem.MS_DES, KeyType.KEY_TYPE_MASTK, 0);
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            }
        }
    }

    private boolean verificarLlaves() {
        return DUKPT.checkIPEK() == 0 && threreIsKey(MASTERKEYIDX, "Debe cargar Master Key", ConfiActivity.this) &&
                threreIsKeyWK(TRACK2KEYIDX, "Debe cargar Work key", ConfiActivity.this);
    }


    private ArrayList<Item> eliminarRepetidos(List<Contenedor> resultados) {
        TreeMap<String, Contenedor> map = new TreeMap<>();
        for (Contenedor item : resultados) {
            map.put(item.getTitulo(), item);
        }
        resultados.clear();
        resultados.addAll(map.values());
        return new ArrayList<Item>(resultados);
    }


    private boolean validacionLength32(ItemWidget widget, String msg) {
        if (widget.getResultado().length() < 32) {
            Toast.makeText(ConfiActivity.this, msg + " de " + widget.getTituloItem(), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


}
