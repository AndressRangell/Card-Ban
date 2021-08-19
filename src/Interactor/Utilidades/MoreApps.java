package Interactor.Utilidades;

import com.cobranzas.adaptadores.ModeloApps;
import com.cobranzas.inicializacion.configuracioncomercio.APLICACIONES;

import java.util.ArrayList;
import java.util.List;

public interface MoreApps {

    List<ModeloApps> listadoAplicaciones(ArrayList<APLICACIONES> aplicaciones);

    boolean isAppInstalada(String packageFaceId);
}
