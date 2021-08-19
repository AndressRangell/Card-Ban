package com.newpos.libpay;

import android.app.Activity;
import android.content.Context;

import com.cobranzas.defines_bancard.DefinesBANCARD;
import com.cobranzas.transactions.Reversal.ReversalTransAuto;
import com.cobranzas.transactions.echotest.EchoTest;
import com.cobranzas.transactions.inyeccion.InyeccionLlave;
import com.cobranzas.transactions.venta.Venta;
import com.newpos.libpay.device.card.CardManager;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.global.TMConstants;
import com.newpos.libpay.paras.EmvAidInfo;
import com.newpos.libpay.paras.EmvCapkInfo;
import com.newpos.libpay.presenter.TransPresenter;
import com.newpos.libpay.presenter.TransUIImpl;
import com.newpos.libpay.presenter.TransView;
import com.newpos.libpay.trans.Trans;
import com.newpos.libpay.trans.TransInputPara;
import com.newpos.libpay.utils.PAYUtils;
import com.pos.device.SDKManager;
import com.pos.device.SDKManagerCallback;
import com.wposs.cobranzas.R;

/**
 * Created by zhouqiang on 2017/4/25.
 *
 * @author zhouqiang
 * 支付sdk管理者
 */
public class PaySdk {

    /**
     * 单例
     */
    private static PaySdk mInstance = null;
    /**
     * 标记sdk环境前端是否进行初始化操作
     */
    private static boolean isInit = false;
    String clase = "PaySdk.java";
    /**
     * 上下文对象，用于获取相关资源和使用其相应方法
     */
    private Context mContext = null;
    /**
     * 获前端段activity对象，主要用于扫码交易
     */
    private Activity mActivity = null;
    /**
     * MVP交媾P层接口，用于对m和v的交互
     */
    private TransPresenter presenter = null;
    /**
     * 初始化PaySdk环境的回调接口
     */
    private PaySdkListener mListener = null;
    /**
     * PaySdk产生的相关文件的保存路径
     * 如代码不进行设置，默认使用程序data分区
     *
     * @link @{@link String}
     */
    private String cacheFilePath = null;
    /**
     * 终端参数文件路径,用于设置一些交易中的偏好属性
     * 如代码不进行设置，默认使用程序自带配置文件
     *
     * @link @{@link String}
     */
    private String paraFilepath = null;

    private PaySdk() {
    }

    public static PaySdk getInstance() {
        if (mInstance == null) {
            mInstance = new PaySdk();
        }
        return mInstance;
    }

    public Context getContext() throws PaySdkException {
        if (this.mContext == null) {
            throw new PaySdkException(PaySdkException.PARA_NULL);
        }
        return mContext;
    }

    public PaySdk setActivity(Activity activity) {
        this.mActivity = activity;
        return mInstance;
    }

    public PaySdk setParaFilePath(String path) {
        this.paraFilepath = path;
        return mInstance;
    }

    public String getParaFilepath() {
        return this.paraFilepath;
    }

    public String getCacheFilePath() {
        return this.cacheFilePath;
    }

    public PaySdk setCacheFilePath(String path) {
        this.cacheFilePath = path;
        return mInstance;
    }

    public PaySdk setListener(PaySdkListener listener) {
        this.mListener = listener;
        return mInstance;
    }

    public void init(Context context) throws PaySdkException {
        this.mContext = context;
        this.init();
    }

    public void init(Context context, PaySdkListener listener) throws PaySdkException {
        this.mContext = context;
        this.mListener = listener;
        this.init();
    }

    public void deleteFile() {
        try {
            PAYUtils.deleteFile(EmvAidInfo.FILENAME);
            PAYUtils.deleteFile(EmvCapkInfo.FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() throws PaySdkException {
        System.out.println("init->start.....");
        if (this.mContext == null) {
            throw new PaySdkException(PaySdkException.PARA_NULL);
        }

        if (this.paraFilepath == null || !this.paraFilepath.endsWith("properties")) {
            this.paraFilepath = TMConstants.DEFAULTCONFIG;
        }

        if (this.cacheFilePath == null) {
            this.cacheFilePath = mContext.getFilesDir() + "/";
        } else if (!this.cacheFilePath.endsWith("/")) {
            this.cacheFilePath += "/";
        }


        TMConfig.setRootFilePath(this.cacheFilePath);
        System.out.println("init->paras files path:" + this.paraFilepath);
        System.out.println("init->cache files will be saved in:" + this.cacheFilePath);
        System.out.println("init->pay sdk will run based on:" + (TMConfig.getInstance().getBankid() == 1 ? "UNIONPAY" : "CITICPAY"));
        if (!TMConfig.getInstance().isOnline()) {
            PAYUtils.copyAssetsToData(this.mContext, EmvAidInfo.FILENAME);
            PAYUtils.copyAssetsToData(this.mContext, EmvCapkInfo.FILENAME);
        }
        SDKManager.init(mContext, new SDKManagerCallback() {
            @Override
            public void onFinish() {
                isInit = true;
                System.out.println("init->success");
                if (mListener != null) {
                    mListener.success();
                }
            }
        });
    }

    /**
     * 释放卡片驱动资源
     */
    public void releaseCard() {
        if (isInit) {
            CardManager.getInstance(0).releaseAll();
        }
    }

    /**
     * 释放sdk环境资源
     */
    public void exit() {
        if (isInit) {
            SDKManager.release();
            isInit = false;
        }
    }

    public void startTrans(String transType, String data, TransView tv, Activity activity, boolean isCajas) throws PaySdkException {
        if (this.mActivity == null) {
            throw new PaySdkException(PaySdkException.PARA_NULL);
        }
        TransInputPara para = new TransInputPara();
        para.setTransUI(new TransUIImpl(mActivity, tv));

        switch (transType) {
            case Trans.Type.ECHO_TEST:
                para.setTransType(Trans.Type.ECHO_TEST);
                para.setNeedOnline(true);
                para.setNeedPass(false);
                para.setEmvAll(false);
                presenter = new EchoTest(this.mContext, Trans.Type.ECHO_TEST, para, true);
                break;

            case Trans.Type.INYECCION:
                para.setTransType(Trans.Type.INYECCION);
                para.setNeedOnline(true);
                para.setNeedPass(false);
                para.setEmvAll(false);
                presenter = new InyeccionLlave(this.mContext, Trans.Type.INYECCION, para, activity);
                break;

            case Trans.Type.REVERSAL:
                para.setTransType(Trans.Type.REVERSAL);
                para.setNeedOnline(true);
                para.setNeedPass(false);
                para.setEmvAll(false);
                presenter = new ReversalTransAuto(this.mContext, Trans.Type.REVERSAL, Integer.parseInt(mContext.getResources().getString(R.string.timerDataConfig)), para);
                break;

            default:
                break;
        }

        if (isInit) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        presenter.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            throw new PaySdkException(PaySdkException.NOT_INIT);
        }
    }
}
