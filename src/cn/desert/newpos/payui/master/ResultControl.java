package cn.desert.newpos.payui.master;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cobranzas.actividades.MainActivity;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.SlotType;
import com.wposs.cobranzas.R;

import java.util.Timer;

import cn.desert.newpos.payui.UIUtils;

import static com.cobranzas.menus.MenuAction.callBackSeatle;
import static java.lang.Thread.sleep;

/**
 * Created by zhouqiang on 2016/11/12.
 */
public class ResultControl extends FormularioActivity {
    static String clase = "ResultControl.java";
    Button confirm;
    ImageView removeCard;
    IccReader iccReader0;
    Thread proceso = null;
    private Timer timer = null;
    private boolean back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ResultControl.java", "onCreate activity");
        setContentView(R.layout.activity_remove_card);
        mostrarSerialvsVersion();
        removeCard2();
    }

    private void mostrarSerialvsVersion() {
        TextView tvSerial = findViewById(R.id.tvSerial);
        TextView tvVersion = findViewById(R.id.tvVersion);
        mostrarSerialvsVersion(tvVersion, tvSerial);
    }

    private void removeCard2() {
        proceso = new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iccReader0 = IccReader.getInstance(SlotType.USER_CARD);
                        if (iccReader0.isCardPresent()) {
                            setContentView(R.layout.activity_remove_card);
                            mostrarSerialvsVersion();
                        }
                    }
                });
                if (validarICC()) {
                    UIUtils.startView(ResultControl.this, MainActivity.class, "");
                    if (callBackSeatle != null)
                        callBackSeatle.getRspSeatleReport(0);
                }
            }
        });
        proceso.start();
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
                        e.printStackTrace();
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        Logger.error("Exception" + e.toString());
                        Thread.currentThread().interrupt();
                    }

                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                return true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            over();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void over() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCard();
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (!back) {
                backToMainMenu();
            }

            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    private void removeCard() {
        if (proceso == null) {
            proceso = new Thread(new Runnable() {
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iccReader0 = IccReader.getInstance(SlotType.USER_CARD);

                            if (iccReader0.isCardPresent()) {
                                setContentView(R.layout.activity_remove_card);
                                mostrarSerialvsVersion();
                                removeCard = (ImageView) findViewById(R.id.iv_remove__card);
                                removeCard.setImageResource(R.drawable.ic_retire_la_tarjeta);
                            }
                        }
                    });

                    if (checkCard()) {
                        finish();
                        backToMainMenu();
                        if (callBackSeatle != null)
                            callBackSeatle.getRspSeatleReport(0);
                    }
                }
            });
            proceso.start();
        }
    }

    private boolean checkCard() {
        boolean ret;
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        while (true) {

            try {
                if (iccReader0.isCardPresent()) {
                    back = true;
                    toneG.startTone(ToneGenerator.TONE_PROP_BEEP2, 2000);
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                        Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                        Logger.error("Exception" + e.toString());
                        Thread.currentThread().interrupt();
                    }

                } else {
                    back = false;
                    ret = true;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
                ret = true;
                break;
            }
        }

        proceso = null;
        return ret;
    }

    public void backToMainMenu() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
