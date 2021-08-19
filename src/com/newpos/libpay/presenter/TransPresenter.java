package com.newpos.libpay.presenter;

import com.newpos.libpay.helper.iso8583.ISO8583;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by zhouqiang on 2017/3/15.
 * @author zhouqiang
 * 交易属性接口类
 */

public interface TransPresenter {
    /**
     * 开始交易流程MODEL接口
     * 用户可以通过此接口进行某一交易流程的入口开始
     */
    void start() throws Exception;

    /**
     * 获取交易过程中拼接报文对象
     * 用户可用此对象进行相关交易域的修改及设置
     * {@link ISO8583}
     * @return
     */
    ISO8583 getISO8583();
}
