package com.cobranzas.tools;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.desert.keyboard.Desertboard;
import com.android.desert.keyboard.DesertboardListener;
import com.android.desert.keyboard.InputInfo;
import com.android.desert.keyboard.InputListener;
import com.android.desert.keyboard.InputManager;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;


public class InputManager2 extends InputManager {

    static String clase = "InputManager2.java";
    private Context context;
    private InputManager instance;
    private WindowManager mWindowManager;
    private LinearLayout container;
    private ImageView alipayView;
    private ImageView wetchatView;
    private EditText input;
    private String mTitle = InputManager.class.getSimpleName();
    private InputManager.Mode mInputMode;
    private InputManager.Lang mLang;
    private boolean mDisorder;
    private boolean mAddEdit;
    private boolean mAddKeyboard;
    private boolean mAddStyles;
    private boolean useOnce;
    private int mLenData;
    private InputListener mListener;
    private static final String PAY_EN = "Please chose pay style";
    private static final String PAY_CH = "请选择付款方式";
    private static final int MAX_LEN_TITLE = 52;//en el diseño base solo se tiene un total de 2 lineas, con un maximo de 26 caracteres por linea
    private static final String BACKGROUND_COLOR = "#0E3E8A";

    @SuppressLint("WrongConstant")
    public InputManager2(Context c) {
        super(c);
        this.mInputMode = InputManager.Mode.AMOUNT;
        this.mLang = InputManager.Lang.EN;
        this.mDisorder = false;
        this.mAddEdit = true;
        this.mAddKeyboard = true;
        this.mAddStyles = false;
        this.useOnce = false;
        this.context = c;
        this.instance = this;
        this.mLenData = 8;
        this.mWindowManager = (WindowManager) this.context.getSystemService("window");
    }

    @SuppressLint("WrongConstant")
    // @Override
    public View getView(boolean flagBtn) {

        this.container = new LinearLayout(this.context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        this.container.setLayoutParams(lp);
        this.container.setOrientation(1);
        this.container.setBackgroundColor(-1);
        LinearLayout layout = new LinearLayout(this.context);
        lp = new LinearLayout.LayoutParams(-1, 0, 2.0F);
        layout.setLayoutParams(lp);
        layout.setOrientation(1);
        layout.setBackgroundColor(Color.parseColor(BACKGROUND_COLOR));
        TextView tv = new TextView(this.context);
        if (this.mTitle.length() > MAX_LEN_TITLE)
            tv.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 3.0F));
        else
            tv.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.4F));
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        tv.setTextSize(26.0F);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setText(this.mTitle);
        tv.setGravity(17);
        layout.addView(tv);
        if (this.mAddEdit) {
            this.input = new EditText(this.context);
            if (this.mTitle.length() > MAX_LEN_TITLE)
                lp = new LinearLayout.LayoutParams(-1, 0, 1.5F);
            else
                lp = new LinearLayout.LayoutParams(-1, 0, 1.1F);
            lp.setMarginStart(20);
            lp.setMarginEnd(20);
            this.input.setLayoutParams(lp);
            this.input.setTextSize(32);
            this.input.setTypeface(null, Typeface.BOLD);
            this.input.setTextColor(Color.parseColor("#FFFFFF"));//Monto editable
            this.initEditText1(this.input);
            layout.addView(this.input);
        }

        TextView tv1 = new TextView(this.context);
        tv1.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0F));
        layout.addView(tv1);
        this.container.addView(layout);
        if (this.mAddKeyboard) {
            Desertboard keyboardView;
            keyboardView = new Desertboard(this.context);
            keyboardView.setVisibility(0);
            keyboardView.setListener(new InputManager2.UserInputListener());
            keyboardView.setDisorder(this.mDisorder);
            keyboardView.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 3.0F));
            this.container.addView(keyboardView);
        }

        if (this.mAddStyles) {

            LinearLayout ll = new LinearLayout(this.context);
            if (flagBtn)
                ll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 0.5F));
            else
                ll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0F));
            ll.setOrientation(0);
            ll.setBackgroundColor(Color.parseColor(BACKGROUND_COLOR));
            this.alipayView = new ImageView(this.context);
            this.wetchatView = new ImageView(this.context);
            if (flagBtn) {
                this.wetchatView.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 0.5F));
                this.wetchatView.setImageBitmap(Desertboard.getBitmapFromAssets(this.context, "back2.png"));
                ll.addView(this.wetchatView);
            } else {
                this.alipayView.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1.0F));
                this.alipayView.setImageBitmap(Desertboard.getBitmapFromAssets(this.context, "pay_ali.jpg"));
                ll.addView(this.alipayView);
            }
            this.container.addView(ll);
            this.initViewsListeners1();
        }

        return this.container;
    }

    @Override
    public InputManager setTitle(String title) {
        this.mTitle = title;
        return this.instance;
    }

    @Override
    public InputManager setTitle(int rid) {
        this.mTitle = this.context.getResources().getString(rid);
        return this.instance;
    }

    @Override
    public InputManager setLang(InputManager.Lang l) {
        this.mLang = l;
        return this.instance;
    }

    @Override
    public InputManager setUseOnce(boolean useOnce) {
        this.useOnce = useOnce;
        return this.instance;
    }

    @Override
    public InputManager addEdit(InputManager.Mode mode) {
        this.mAddEdit = true;
        this.mInputMode = mode;
        return this.instance;
    }

    public InputManager addEdit(InputManager.Mode mode, int lenData) {
        this.mAddEdit = true;
        this.mInputMode = mode;
        this.mLenData = lenData;
        return this.instance;
    }

    @Override
    public InputManager addKeyboard(boolean disorder) {
        this.mAddKeyboard = true;
        this.mDisorder = disorder;
        return this.instance;
    }

    @Override
    public InputManager addStyles() {
        this.mAddStyles = true;
        return this.instance;
    }

    @Override
    public InputManager setListener(InputListener l) {
        this.mListener = l;
        return this.instance;
    }

    /**
     * @deprecated
     */
    @Deprecated
    @Override
    @SuppressWarnings("deprecation")
    public void release() {
        if (this.container != null) {
            this.container.removeAllViews();
            this.container = null;
        }
    }

    private void initEditText1(final EditText et) {
        if (InputManager.Mode.AMOUNT == this.mInputMode) {
            et.setBackgroundColor(Color.parseColor(BACKGROUND_COLOR));
            BitmapDrawable drawable;
            if (this.mLang == InputManager.Lang.EN) {
                drawable = new BitmapDrawable(Desertboard.getBitmapFromAssets(this.context, "guaraw.png"));
            } else {
                drawable = new BitmapDrawable(Desertboard.getBitmapFromAssets(this.context, "rmb.png"));
            }

            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            Display dm = this.mWindowManager.getDefaultDisplay();
            et.setPadding((dm.getWidth() / 2) - 250, 0, 0, 0);
            et.setCompoundDrawables(drawable, (Drawable) null, (Drawable) null, (Drawable) null);

            et.setCursorVisible(false);
            et.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View v, MotionEvent event) {
                    et.requestFocus();
                    return false;
                }
            });
            et.setRawInputType(3);
            et.addTextChangedListener(new InputManager2.EditChangeXsTwoListener(et));
            et.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            et.setSelection(et.getText().length());
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        }

        if (InputManager.Mode.VOUCHER == this.mInputMode || InputManager.Mode.AUTHCODE == this.mInputMode) {
            et.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            et.setBackgroundColor(-1);
            et.setGravity(17);
            et.setInputType(2);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        }

        if (InputManager.Mode.PASSWORD == this.mInputMode) {
            et.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            et.setBackgroundColor(-1);
            et.setGravity(17);
            et.setInputType(129);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(this.mLenData)});
        }

        if (InputManager.Mode.DATETIME == this.mInputMode) {
            et.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            et.setBackgroundColor(-1);
            et.setGravity(17);
            et.setInputType(2);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        }

        if (InputManager.Mode.REFERENCE == this.mInputMode) {
            et.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            et.setBackgroundColor(-1);
            et.setGravity(17);
            et.setInputType(2);
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(this.mLenData)});
        }

    }

    private void initViewsListeners1() {
        this.alipayView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                InputManager2.this.handleInputData1(InputManager.Style.ALIPAY);
            }
        });
        this.wetchatView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                InputInfo inputInfo = new InputInfo();
                inputInfo.setResultFlag(false);
                mListener.callback(inputInfo);
                if (useOnce) {
                    container.removeAllViews();
                }
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void handleInputData1(InputManager.Style style) {
        String in = this.input.getText().toString();
        InputInfo inputInfo = new InputInfo();
        inputInfo.setNextStyle(style);
        if (style == InputManager.Style.COMMONINPUT && this.mAddStyles) {
            Toast.makeText(this.context, this.mLang == InputManager.Lang.CH ? PAY_CH : PAY_EN,
                    Toast.LENGTH_SHORT).show();
        } else {
            inputInfo.setResultFlag(true);
            if (InputManager.Mode.AMOUNT == this.mInputMode) {
                in = in.replaceAll("(\\.)?", "");
            }

            if (InputManager.Mode.VOUCHER == this.mInputMode && in.length() < 6) {
                in = padleft(in, 6, '0');
            }

            if (InputManager.Mode.DATETIME == this.mInputMode && in != null && in.length() < 8) {
                if (in.length() == 6) {
                    in = padleft(in, 7, '0');
                }

                if (in != null) {
                    in = padleft(in, 8, '2');
                }
            }

            inputInfo.setResult(in);
            this.mListener.callback(inputInfo);
            if (this.useOnce) {
                this.container.removeAllViews();
            }
        }

    }

    private static void sendKeyCode(final int keyCode) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception var2) {
                    var2.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, clase, var2.getMessage());
                    Logger.logLine(LogType.EXCEPTION, clase, var2.getStackTrace());
                    Logger.error("Exception" + var2.toString());
                }

            }
        }).start();
    }

    private static String padleft(String s, int len, char c) {
        s = s.trim();
        if (s.length() > len) {
            return null;
        } else {
            StringBuilder d = new StringBuilder(len);
            int var4 = len - s.length();

            while (var4-- > 0) {
                d.append(c);
            }

            d.append(s);
            return d.toString();
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    @SuppressLint("Deprecated")
    private static class EditChangeXsTwoListener implements TextWatcher {
        private EditText editText;
        private boolean isChanged = false;

        public EditChangeXsTwoListener(EditText e) {
            this.editText = e;
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Do nothing because of X and Y
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing because of X and Y
        }

        public void afterTextChanged(Editable s) {
            if (isChanged) {
                return;
            }
            String dato = s.toString();
            dato = dato.replace(".", "");
            isChanged = true;

            if (dato.equals("")) {
                isChanged = false;
                return;
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
                editText.setText(dato1.toString());
                editText.setSelection(editText.length());
                isChanged = false;
                return;
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

            editText.setText(salida);
            editText.setSelection(editText.length());
            isChanged = false;
        }
    }

    final class UserInputListener implements DesertboardListener {
        UserInputListener() {
        }

        public void onVibrate(int ms) {
            // Do nothing because of X and Y
        }

        public void onChar() {
            // Do nothing because of X and Y
        }

        public void onInputKey(int key) {
            switch (key) {
                case 0:
                    InputManager2.sendKeyCode(7);
                    break;
                case 1:
                    InputManager2.sendKeyCode(8);
                    break;
                case 2:
                    InputManager2.sendKeyCode(9);
                    break;
                case 3:
                    InputManager2.sendKeyCode(10);
                    break;
                case 4:
                    InputManager2.sendKeyCode(11);
                    break;
                case 5:
                    InputManager2.sendKeyCode(12);
                    break;
                case 6:
                    InputManager2.sendKeyCode(13);
                    break;
                case 7:
                    InputManager2.sendKeyCode(14);
                    break;
                case 8:
                    InputManager2.sendKeyCode(15);
                    break;
                case 9:
                    InputManager2.sendKeyCode(16);
                    break;
                case 129:
                    if (InputManager2.this.mInputMode == InputManager.Mode.AMOUNT ||
                            InputManager2.this.mInputMode == InputManager.Mode.PASSWORD ||
                            InputManager2.this.mInputMode == InputManager.Mode.VOUCHER ||
                            InputManager2.this.mInputMode == InputManager.Mode.REFERENCE) {
                        InputManager2.this.handleInputData1(InputManager.Style.UNIONPAY);
                    } else {
                        InputManager2.this.handleInputData1(InputManager.Style.COMMONINPUT);
                    }
                    break;
                case 130:
                    InputManager2.sendKeyCode(67);
                    break;
                default:
                    break;
            }

        }

        public void onClr() {
            // Do nothing because of X and Y
        }

        public void onCannel() {
            // Do nothing because of X and Y
        }

        public void onEnter(String encpin) {
            // Do nothing because of X and Y
        }
    }

    public enum Mode {
        AMOUNT(1),
        PASSWORD(2),
        VOUCHER(3),
        AUTHCODE(4),
        DATETIME(5),
        REFERENCE(6);

        private int val;

        private Mode(int value) {
            this.val = value;
        }

        protected int getVal() {
            return this.val;
        }
    }

    public enum Lang {
        CH(1),
        EN(2);

        private int val;

        private Lang(int value) {
            this.val = value;
        }

        protected int getVal() {
            return this.val;
        }
    }

    public enum Style {
        ALIPAY(0),
        WETCHATPAY(1),
        UNIONPAY(2),
        COMMONINPUT(3);

        private int val;

        Style(int value) {
            this.val = value;
        }

        protected int getVal() {
            return this.val;
        }
    }
}
