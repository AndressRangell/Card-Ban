package Interactor.View;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wposs.cobranzas.R;

public class CustomDialogImpl extends CustomDialog {

    Context context;
    int wrapContent = WindowManager.LayoutParams.WRAP_CONTENT;

    public CustomDialogImpl(Context context) {
        this.context = context;
    }


    @Override
    public void showCustomDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_info_confirmacion);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = wrapContent;
        lp.height = wrapContent;

        AppCompatButton btnAceptar = dialog.findViewById(R.id.btn_aceptar);

        ImageView imageView = dialog.findViewById(R.id.icon);
        if (getIcono() != 0) {
            imageView.setImageDrawable(context.getDrawable(getIcono()));
        }


        TextView textViewTitulo = dialog.findViewById(R.id.textView_title);
        textViewTitulo.setText(getTextViewTitulo());

        TextView textViewSubContenido = dialog.findViewById(R.id.textView_SupContenido);
        textViewSubContenido.setText(getTextViewDescripcion());

        if (getTextoBotonCancelar() != null && !getTextoBotonCancelar().isEmpty()) {

            AppCompatButton btnCancelar = dialog.findViewById(R.id.btn_cancelar);
            btnCancelar.setVisibility(View.VISIBLE);
            btnCancelar.setText(getTextoBotonCancelar());

            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    getEventoDialog().cancelacionEvent();
                }
            });

        }

        if (getTextoBotonAcceptar() != null && !getTextoBotonAcceptar().isEmpty()) {
            btnAceptar.setText(getTextoBotonAcceptar());
            btnAceptar.setVisibility(View.VISIBLE);
            btnAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    getEventoDialog().confirmacionEvent();
                }
            });
        }


        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}
