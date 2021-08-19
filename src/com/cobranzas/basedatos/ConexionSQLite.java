package com.cobranzas.basedatos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cobranzas.basedatos.interfaces.Conexion;

public class ConexionSQLite extends SQLiteOpenHelper implements Conexion {

    protected static final String DATABASE_NAME = "Bancard.db";
    private static final String CREATE = "CREATE TABLE ";
    private static final String TEXT = " TEXT, ";
    private static final int DATABASE_VERSION = 3;
    protected SQLiteDatabase conexionSQlite;
    protected String columnLogsId = "logs_id";
    protected String columnLogsNumlote = "logs_numLote";
    protected String columnLogsDiscriminadocomercios = "logs_discriminadoComercios";
    protected String columnLogsCantCredito = "logs_cantCredito";
    protected String columnLogsTotalCredito = "logs_totalCredito";
    protected String columnLogsCantDebito = "logs_cantDebito";
    protected String columnLogsTotalDebito = "logs_totalDebito";
    protected String columnLogsCantmovil = "logs_cantMovil";
    protected String columnLogsTotalmovil = "logs_totalMovil";
    protected String columnLogsCantAnular = "logs_cantAnular";
    protected String columnLogsTotalAnular = "logs_totalAnular";
    protected String columnLogsCantVuelto = "logs_cantVuelto";
    protected String columnLogsTotalVuelto = "logs_totalVuelto";
    protected String columnLogsCantsaldo = "logs_cantSaldo";
    protected String columnLogsTotalsaldo = "logs_totalSaldo";
    protected String columnLogsCantgeneral = "logs_cantGeneral";
    protected String columnLogsTotalgeneral = "logs_totalGeneral";
    protected String columnLogsCargo = "logs_cargo";
    protected String columnLogsNumBoleta = "logs_boleta";
    protected String columnLogsMonto = "logs_monto";
    protected String columnLogsFecha = "logs_fecha";
    protected String columnLogsHora = "logs_hora";
    protected String columnLogsTrans = "logs_trans";
    protected String columnLogsTarjeta = "logs_tarjeta";
    protected String columnLogsTipotarjeta = "logs_tipoTarjeta";
    protected String tableTmconfig = "TMConfig";
    protected String columnIpPrimaria = "Ip_Primaria";
    protected String columnPuertoPrimaria = "Puerto_Primaria";
    protected String columnIpSecundaria = "Ip_Secundaria";
    protected String columnPuertoSecundaria = "Puerto_Secundaria";
    protected String columnTimeout = "Timeout";
    protected String columnTimeoutData = "TimeoutData";
    protected String columnNii = "Nii";
    protected String tableVoucher = "Voucher";
    protected String columnPan = "PAN";
    protected String columnNroboleta = "NroBoleta";
    protected String columnNrocargo = "NroCargo";
    protected String columnMonto = "Monto";
    protected String columnTipoventa = "TipoVenta";
    protected String columnFecha = "Fecha";
    protected String columnImgvoucher = "ImgVoucher";
    Context   context;

    public ConexionSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tablaConfiguracionIP());
        db.execSQL(tablaVoucherReimpresion());
    }

    private String tablaVoucherReimpresion() {
        return CREATE + tableVoucher + " (" +
                columnNrocargo + TEXT +
                columnPan + TEXT +
                columnNroboleta + TEXT +
                columnMonto + TEXT +
                columnTipoventa + TEXT +
                columnFecha + TEXT +
                columnImgvoucher + " BLOB " +
                ")";
    }

    private String tablaConfiguracionIP() {
        return CREATE + tableTmconfig + " (" +
                columnIpPrimaria + TEXT +
                columnPuertoPrimaria + TEXT +
                columnIpSecundaria + TEXT +
                columnPuertoSecundaria + TEXT +
                columnTimeout + TEXT +
                columnTimeoutData + TEXT +
                columnNii + " TEXT NOT NULL " +
                ")";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Do nothing because of X and Y.
    }
}
