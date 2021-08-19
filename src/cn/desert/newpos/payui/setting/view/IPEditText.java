package cn.desert.newpos.payui.setting.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.PAYUtils;
import com.wposs.cobranzas.R;

public class IPEditText extends LinearLayout {

    static String clase = "IPEditText.java";
    private EditText mFirstIP;
    private EditText mSecondIP;
    private EditText mThirdIP;
    private EditText mFourthIP;
    TextView p1;
    TextView p2;
    TextView p3;
    private LinearLayout linearLayout;
    private String mText1;
    private String mText2;
    private String mText3;
    private String mText4;
    String tips;

    private SharedPreferences mPreferences;

    public IPEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        /**
         * 初始化控件
         */
        mFirstIP = (EditText) findViewById(R.id.ip_first);
        mSecondIP = (EditText) findViewById(R.id.ip_second);
        mThirdIP = (EditText) findViewById(R.id.ip_third);
        mFourthIP = (EditText) findViewById(R.id.ip_fourth);
        p1 = (TextView) findViewById(R.id.punto1);
        p2 = (TextView) findViewById(R.id.punto2);
        p3 = (TextView) findViewById(R.id.punto3);
        linearLayout = (LinearLayout) findViewById(R.id.linerLy);
        tips = context.getResources().getString(R.string.ip_invalid);
        mPreferences = context.getSharedPreferences("config_IP", Context.MODE_PRIVATE);
        operatingEditText();
    }

    /**
     * 获得EditText中的内容,当每个Edittext的字符达到三位时,自动跳转到下一个EditText,当用户点击.时,
     * 下一个EditText获得焦点
     */
    private void operatingEditText() {
        mFirstIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /**
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0) {
                    boolean contains = s.toString().trim().contains(".");
                    if (s.length() > 2 || contains) {
                        if (contains) {
                            mText1 = s.toString().substring(0, s.length() - 1);
                            mFirstIP.setText(mText1);
                        } else {
                            mText1 = s.toString().trim();
                        }
                        if (Integer.parseInt(mText1) > 255) {
                            return;

                        }
                        Editor editor = mPreferences.edit();
                        editor.putInt("IP_FIRST", mText1.length());
                        editor.commit();

                        mSecondIP.setFocusable(true);
                        mSecondIP.requestFocus();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void afterTextChanged(Editable s) {
                throw new UnsupportedOperationException();

            }
        });

        mSecondIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /**
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0) {
                    boolean contains = s.toString().trim().contains(".");
                    if (s.length() > 2 || contains) {
                        if (contains) {
                            mText2 = s.toString().substring(0, s.length() - 1);
                            mSecondIP.setText(mText2);
                        } else {
                            mText2 = s.toString().trim();
                        }
                        if (Integer.parseInt(mText2) > 255) {
                            return;
                        }
                        Editor editor = mPreferences.edit();
                        editor.putInt("IP_SECOND", mText2.length());
                        editor.commit();

                        mThirdIP.setFocusable(true);
                        mThirdIP.requestFocus();
                    }
                }

                /**
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (start == 0
                        && s != null && s.length() == 0
                        && !PAYUtils.isNullWithTrim(mFirstIP.getText()
                        .toString()) && mFirstIP.length() > 2) {
                    mFirstIP.setFocusable(true);
                    mFirstIP.requestFocus();
                    mFirstIP.setSelection(mPreferences.getInt("IP_FIRST", 0));
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void afterTextChanged(Editable s) {
                throw new UnsupportedOperationException();
            }
        });

        mThirdIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /**
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0) {
                    boolean contains = s.toString().trim().contains(".");
                    if (s.length() > 2 || contains) {
                        if (contains) {
                            mText3 = s.toString().substring(0, s.length() - 1);
                            mThirdIP.setText(mText3);
                        } else {
                            mText3 = s.toString().trim();
                        }

                        if (Integer.parseInt(mText3) > 255) {
                            return;
                        }

                        Editor editor = mPreferences.edit();
                        editor.putInt("IP_THIRD", mText3.length());
                        editor.commit();

                        mFourthIP.setFocusable(true);
                        mFourthIP.requestFocus();
                    }
                }

                /**
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (start == 0 && s != null && s.length() == 0
                        && !PAYUtils.isNullWithTrim(mSecondIP.getText().toString())
                        && mSecondIP.length() > 2) {
                    mSecondIP.setFocusable(true);
                    mSecondIP.requestFocus();
                    mSecondIP.setSelection(mPreferences.getInt("IP_SECOND", 0));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void afterTextChanged(Editable s) {
                throw new UnsupportedOperationException();
            }
        });

        mFourthIP.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /**
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0) {
                    mText4 = s.toString().trim();

                    Editor editor = mPreferences.edit();
                    editor.putInt("IP_FOURTH", mText4.length());
                    editor.commit();
                }

                /**
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (start == 0 && s != null && s.length() == 0
                        && !PAYUtils.isNullWithTrim(mThirdIP.getText().toString())
                        && mThirdIP.length() > 2) {
                    mThirdIP.setFocusable(true);
                    mThirdIP.requestFocus();
                    mThirdIP.setSelection(mPreferences.getInt("IP_THIRD", 0));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void afterTextChanged(Editable s) {
                throw new UnsupportedOperationException();
            }
        });
    }

    public String getIPText() {
        if (!PAYUtils.isNullWithTrim(mFirstIP.getText().toString())
                && !PAYUtils.isNullWithTrim(mSecondIP.getText().toString())
                && !PAYUtils.isNullWithTrim(mThirdIP.getText().toString())
                && !PAYUtils.isNullWithTrim(mFourthIP.getText().toString())) {
            return mFirstIP.getText().toString() + "."
                    + mSecondIP.getText().toString() + "."
                    + mThirdIP.getText().toString() + "."
                    + mFourthIP.getText().toString();
        } else {
            return null;
        }
    }

    public String getIPHint() {
        if (!PAYUtils.isNullWithTrim(mFirstIP.getHint().toString())
                && !PAYUtils.isNullWithTrim(mSecondIP.getHint().toString())
                && !PAYUtils.isNullWithTrim(mThirdIP.getHint().toString())
                && !PAYUtils.isNullWithTrim(mFourthIP.getHint().toString())) {
            return mFirstIP.getHint().toString() + "."
                    + mSecondIP.getHint().toString() + "."
                    + mThirdIP.getHint().toString() + "."
                    + mFourthIP.getHint().toString();
        } else {
            return null;
        }
    }

    public String getIPTextOrHint() {
        if (!PAYUtils.isNullWithTrim(getTextOrHint(mFirstIP))
                && !PAYUtils.isNullWithTrim(getTextOrHint(mSecondIP))
                && !PAYUtils.isNullWithTrim(getTextOrHint(mThirdIP))
                && !PAYUtils.isNullWithTrim(getTextOrHint(mFourthIP))) {
            return getTextOrHint(mFirstIP) + "." +
                    getTextOrHint(mSecondIP) + "." +
                    getTextOrHint(mThirdIP) + "." +
                    getTextOrHint(mFourthIP);
        } else {
            return null;
        }
    }

    private String getTextOrHint(EditText editText) {
        try {
            if (!PAYUtils.isNullWithTrim(editText.getText().toString())) {
                return editText.getText().toString();
            } else if (!PAYUtils.isNullWithTrim(editText.getHint().toString())) {
                return editText.getHint().toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, clase, e.getMessage());
            Logger.logLine(LogType.EXCEPTION, clase, e.getStackTrace());
            Log.d("Exception", "getTextOrHint: " + e.getMessage());
            return null;
        }

    }

    public void setIPText(String[] ip) {
        mFirstIP.setText(ip[0]);
        mSecondIP.setText(ip[1]);
        mThirdIP.setText(ip[2]);
        mFourthIP.setText(ip[3]);
    }

    public void setIpHint(String[] ip) {
        mFirstIP.setHint(ip[0]);
        mSecondIP.setHint(ip[1]);
        mThirdIP.setHint(ip[2]);
        mFourthIP.setHint(ip[3]);
    }

    public void setBackground(int color, Context context) {

        linearLayout.setBackground(context.getDrawable(color));

    }

    @Override
    public void setEnabled(boolean isEnabled) {
        mFirstIP.setEnabled(isEnabled);
        mSecondIP.setEnabled(isEnabled);
        mThirdIP.setEnabled(isEnabled);
        mFourthIP.setEnabled(isEnabled);
    }

    public void setLiveOrDeath(boolean isLive) {
        if (!isLive) {
            mFirstIP.setTextColor(Color.GRAY);
            mSecondIP.setTextColor(Color.GRAY);
            mThirdIP.setTextColor(Color.GRAY);
            mFourthIP.setTextColor(Color.GRAY);
        } else {
            mFirstIP.setTextColor(Color.BLACK);
            mSecondIP.setTextColor(Color.BLACK);
            mThirdIP.setTextColor(Color.BLACK);
            mFourthIP.setTextColor(Color.BLACK);
        }
        mFirstIP.setEnabled(isLive);
        mSecondIP.setEnabled(isLive);
        mThirdIP.setEnabled(isLive);
        mFourthIP.setEnabled(isLive);
    }

    public boolean isOk() {
        return PAYUtils.isNullWithTrim(mFirstIP.getText().toString())
                || PAYUtils.isNullWithTrim(mSecondIP.getText().toString())
                || PAYUtils.isNullWithTrim(mThirdIP.getText().toString())
                || PAYUtils.isNullWithTrim(mFourthIP.getText().toString());

    }
}
