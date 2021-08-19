package com.newpos.libpay.paras;

import com.pos.device.emv.CAPublicKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmvCapkInfo implements Serializable {

	public static final String FILENAME = "default_capks.dat";

	private List<byte[]> capkList;

	public EmvCapkInfo() {
		capkList = new ArrayList<byte[]>();
	}

	public List<byte[]> getCapkList() {
		return capkList;
	}

	public void setCapkList(List<byte[]> capkList) {
		this.capkList = capkList;
	}
}
