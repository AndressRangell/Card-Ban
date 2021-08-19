package Interactor.Utilidades;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.cobranzas.adaptadores.ModeloApps;
import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;
import com.newpos.libpay.utils.ISOUtil;

import java.util.ArrayList;
import java.util.List;

public class MenusAplicaciones implements MoreApps {

    Context context;

    public MenusAplicaciones(Context context) {
        this.context = context;
    }

    @Override
    public List<ModeloApps> listadoAplicaciones(ArrayList<APLICACIONES> aplicaciones) {
        List<ModeloApps> appList = new ArrayList<>();
        appList.add(new ModeloApps("POLARIS CLOUD"));
        for (APLICACIONES aplication : aplicaciones) {

            switch (aplication.getNameApp()) {
                case "LEALTAD":
                    if (ISOUtil.stringToBoolean(aplication.getActive()))
                        appList.add(new ModeloApps(aplication.getNameApp()));
                    break;

                case "FLOTA":
                    if (ISOUtil.stringToBoolean(aplication.getActive()))
                        appList.add(new ModeloApps(aplication.getNameApp()));
                    break;

                case "INFONET COBRANZAS":
                    if (ISOUtil.stringToBoolean(aplication.getActive()))
                        appList.add(new ModeloApps(aplication.getNameApp()));
                    break;
                default:
            }

        }

        return appList;
    }

    @Override
    public boolean isAppInstalada(String packageFaceId) {
        if (packageFaceId != null && !packageFaceId.isEmpty()) {
            List<PackageInfo> packageList = context.getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packageList.size(); i++) {
                PackageInfo packageInfo = packageList.get(i);
                if (packageInfo.packageName.equals(packageFaceId)) {
                    return true;
                }
            }
        }

        return false;
    }

}
