package Interactor.View;

public abstract class CustomDialog {

    private EventoDialog eventoDialog;
    private String textViewTitulo;
    private String textViewDescripcion;
    private String textoBotonAcceptar;
    private String textoBotonCancelar;
    private int icono;

    protected CustomDialog() {
    }

    public interface EventoDialog {
        void confirmacionEvent();

        void cancelacionEvent();
    }

    protected EventoDialog getEventoDialog() {
        return eventoDialog;
    }

    public void setEventoDialog(EventoDialog eventoDialog) {
        this.eventoDialog = eventoDialog;
    }

    public abstract void showCustomDialog();


    protected String getTextoBotonAcceptar() {
        return textoBotonAcceptar;
    }

    public void setTextoBotonAcceptar(String textoBotonAcceptar) {
        this.textoBotonAcceptar = textoBotonAcceptar;
    }

    protected String getTextoBotonCancelar() {
        return textoBotonCancelar;
    }

    public void setTextoBotonCancelar(String textoBotonCancelar) {
        this.textoBotonCancelar = textoBotonCancelar;
    }

    protected int getIcono() {
        return icono;
    }

    public void setIcono(int icono) {
        this.icono = icono;
    }

    protected String getTextViewTitulo() {
        return textViewTitulo;
    }

    public void setTextViewTitulo(String textViewTitulo) {
        this.textViewTitulo = textViewTitulo;
    }

    protected String getTextViewDescripcion() {
        return textViewDescripcion;
    }

    public void setTextViewDescripcion(String textViewDescripcion) {
        this.textViewDescripcion = textViewDescripcion;
    }

}
