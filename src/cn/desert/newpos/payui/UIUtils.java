package cn.desert.newpos.payui;

import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.newpos.libpay.LogType;
import com.newpos.libpay.Logger;
import com.newpos.libpay.utils.ISOUtil;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKException;
import com.pos.device.config.DevConfig;
import com.pos.device.rtc.RealTimeClock;
import com.wposs.cobranzas.BuildConfig;
import com.wposs.cobranzas.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.desert.newpos.payui.master.ResultControl;

import static java.lang.Thread.sleep;

/**
 * @author zhouqiang
 * @email wy1376359644@163.com
 */
public class UIUtils {

    private static final String NAMECLASS = "UIUtils.java";
    private static final String EXCEPTION = "Exception";

    /**
     * 显示交易结果
     *
     * @param activity
     * @param flag
     * @param info
     */
    public static void startResult(Activity activity, boolean flag, String info) {
        Intent intent = new Intent();
        intent.setClass(activity, ResultControl.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean("flag", flag);
        bundle.putString("info", info);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * @param activity
     * @param cls
     */
    public static void startView(Activity activity, Class<?> cls, String putExtra) {
        Intent intent = new Intent();
        intent.setClass(activity, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DefinesBANCARD.DATO_MENU, putExtra);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * 自定义提示信息
     *
     * @param activity
     * @param content
     */
    public static void toast(Activity activity, boolean flag, int content) {
        LayoutInflater inflater3 = activity.getLayoutInflater();
        View view3 = inflater3.inflate(R.layout.app_toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout));
        ImageView face = (ImageView) view3.findViewById(R.id.app_t_iv);
        if (flag) {
            face.setImageDrawable(activity.getDrawable(R.drawable.icon_face_laugh));
        } else {
            face.setImageDrawable(activity.getDrawable(R.drawable.icon_face_cry));
        }
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view3);
        ((TextView) view3.findViewById(R.id.toast_tv)).
                setText(activity.getResources().getString(content));
        toast.show();
    }

    /**
     * 自定义提示信息
     *
     * @param activity
     * @param str
     */
    public static void toast(Activity activity, boolean flag, String str) {
        LayoutInflater inflater3 = activity.getLayoutInflater();
        View view3 = inflater3.inflate(R.layout.app_toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout));
        ImageView face = (ImageView) view3.findViewById(R.id.app_t_iv);
        if (flag) {
            face.setImageDrawable(activity.getDrawable(R.drawable.icon_face_laugh));
        } else {
            face.setImageDrawable(activity.getDrawable(R.drawable.icon_face_cry));
        }
        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view3);
        ((TextView) view3.findViewById(R.id.toast_tv)).setText(str);
        toast.show();
    }

    public static void toast(Activity activity, int ico, String str, int duration) {
        LayoutInflater inflater3 = activity.getLayoutInflater();
        View view3 = inflater3.inflate(R.layout.app_toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout));
        ImageView face = (ImageView) view3.findViewById(R.id.app_t_iv);

        face.setImageDrawable(activity.getDrawable(ico));

        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER, 0, 400);
        toast.setDuration(duration);
        toast.setView(view3);
        ((TextView) view3.findViewById(R.id.toast_tv)).setText(str);
        toast.show();
    }

    public static Dialog centerDialog(Context mContext, int resID, int root) {
        final Dialog pd = new Dialog(mContext, R.style.Translucent_Dialog);
        pd.setContentView(resID);
        LinearLayout layout = (LinearLayout) pd.findViewById(root);
        layout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.up_down));
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(true);
        pd.show();
        return pd;
    }


    public static void sendKeyCode(final int keyCode) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getStackTrace());
                    Logger.error("Exception when send PointerSync");
                }
            }
        }.start();
    }

    /**
     * 拷贝assets文件夹至某一目录
     *
     * @param context
     * @param assetDir
     * @param dir
     */
    public static void copyToAssets(Context context, String assetDir, String dir) {

        String[] files;
        try {
            // 获得Assets一共有几多文件
            files = context.getResources().getAssets().list(assetDir);
        } catch (IOException e1) {
            e1.printStackTrace();
            Logger.logLine(LogType.EXCEPTION, NAMECLASS, e1.getMessage());
            Logger.logLine(LogType.EXCEPTION, NAMECLASS, e1.getStackTrace());
            return;
        }
        File mWorkingPath = new File(dir);
        // 如果文件路径不存在
        if (!mWorkingPath.exists()) {
            // 创建文件夹
            mWorkingPath.mkdirs();
        }

        for (int i = 0; i < files.length; i++) {
            try {

                // 获得每个文件的名字
                String fileName = files[i];
                // 根据路径判断是文件夹还是文件
                if (!fileName.contains(".")) {
                    if (0 == assetDir.length()) {
                        copyToAssets(context, fileName, dir + fileName + "/");
                    } else {
                        copyToAssets(context, assetDir + "/" + fileName, dir + "/"
                                + fileName + "/");
                    }
                    continue;
                }

                InputStream in = null;
                try {
                    if (0 != assetDir.length()) {
                        in = context.getAssets().open(assetDir + "/" + fileName);
                    } else {
                        in = context.getAssets().open(fileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getMessage());
                    Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getStackTrace());
                    Logger.error(EXCEPTION + e.toString());
                    //it said that this is a directory
                    if (0 == assetDir.length()) {
                        copyToAssets(context, fileName, dir + fileName + "/");
                    } else {
                        copyToAssets(context, assetDir + "/" + fileName, dir + "/"
                                + fileName + "/");
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }

                File outFile = new File(mWorkingPath, fileName);
                if (outFile.exists()) {
                    outFile.delete();
                }

                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    if (in != null) {
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.flush();
                        out.getFD().sync();
                        in.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getStackTrace());
                Logger.error(EXCEPTION + e.toString());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * @param pathName
     * @param reqWidth  单位：px
     * @param reqHeight 单位：px
     * @return
     * @description 从SD卡上加载图片
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName,
                                                     int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize);
    }

    /**
     * @param options   参数
     * @param reqWidth  目标的宽度
     * @param reqHeight 目标的高度
     * @return
     * @description 计算图片的压缩比率
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * @param src
     * @param dstWidth
     * @param dstHeight
     * @return
     * @description 通过传入的bitmap，进行压缩，得到符合标准的bitmap
     */
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
                                            int dstHeight, int inSampleSize) {
        // 如果inSampleSize是2的倍数，也就说这个src已经是我们想要的缩略图了，直接返回即可。
        if (inSampleSize == 1) {
            return src;
        }
        // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响，我们这里是缩小图片，所以直接设置为false
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }


    /**
     * 删除文件或者文件夹
     *
     * @param file
     */
    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    /**
     * 根据资源ID获取资源字符串
     *
     * @param context 上下文对象
     * @param resid   资源ID
     * @return
     */
    public static String getStringByInt(Context context, int resid) {
        return context.getResources().getString(resid);
    }

    /**
     * 根据资源ID获取资源字符串
     *
     * @param context
     * @param resid
     * @param parm
     * @return
     */
    public static String getStringByInt(Context context, int resid, String parm) {
        String sAgeFormat1 = context.getResources().getString(resid);
        return String.format(sAgeFormat1, parm);
    }

    /**
     * 根据资源ID获取资源字符串
     *
     * @param context
     * @param resid
     * @param parm1
     * @param parm2
     * @return
     */
    public static String getStringByInt(Context context, int resid,
                                        String parm1, String parm2) {
        String sAgeFormat1 = context.getResources().getString(resid);
        return String.format(sAgeFormat1, parm1, parm2);
    }

    public static void beep(int typeTone) {

        int timeOut = 2;
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);


        long start = SystemClock.uptimeMillis();
        while (true) {
            toneG.startTone(typeTone, 2000);
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getStackTrace());
                Logger.error(EXCEPTION + e.toString());
                Thread.currentThread().interrupt();
            }

            if (SystemClock.uptimeMillis() - start > timeOut) {
                toneG.stopTone();
                break;
            }
        }
    }

    public static String labelHTML(final String label, final String value) {
        return "<b>" + label + "</b>" + " " + value;
    }

    /**
     * Crea un dialogo de alerta
     *
     * @return Nuevo dialogo
     */
    public static void showAlertDialog(String title, String msg, Context context) {
        final android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);

        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setIcon(R.drawable.ic_cobranzas_blanca);
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog = alertDialog.create();
        dialog.show();
    }


    public static void dateTime(String date, String time) {
        if (time != null && date != null && !time.isEmpty() && !date.isEmpty()) {
            StringBuilder dateTime = new StringBuilder();
            try {
                dateTime.append(PAYUtils.getYear());
                dateTime.append(date);
                dateTime.append(time);

                RealTimeClock.set(dateTime.toString());
            } catch (SDKException e) {
                Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getMessage());
                Logger.logLine(LogType.EXCEPTION, NAMECLASS, e.getStackTrace());
                e.printStackTrace();
                e.printStackTrace();
            }
        }
    }

    public static void mostrarSerialvsVersion(TextView tvVersion, TextView tvSerial) {
        tvVersion.setText(BuildConfig.VERSION_NAME);
        tvSerial.setText(formatSerial(DevConfig.getSN()));
    }

    private static String formatSerial(String serial) {
        int espacio = 5;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < serial.length(); i += espacio) {
            if (i + espacio >= serial.length()) {
                result.append(serial.substring(i));
            } else {
                result.append(serial.substring(i, i + espacio)).append("-");
            }
        }
        return result.toString();
    }

    public static boolean verificacionBoolean(Context context, String parametro, String claseEvento) {
        if (!parametro.isEmpty()) {
            if (parametro.equals("true") || parametro.equals("1") || parametro.equalsIgnoreCase("WIFI")) {
                return true;
            } else if (parametro.equals("false") || parametro.equals("0") || parametro.equalsIgnoreCase("3G")) {
                return false;
            } else {
                ISOUtil.showMensaje("Parametro no definido \n" + claseEvento, context);
            }
        }
        return false;
    }
}
