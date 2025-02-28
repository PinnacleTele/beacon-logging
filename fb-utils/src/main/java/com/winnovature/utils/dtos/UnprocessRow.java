package com.winnovature.utils.dtos;

import java.io.Serializable;

public class UnprocessRow implements Serializable {

	private static final long serialVersionUID = 1L;
	private String esmeaddr;
	private String fileId;
	private String mobile;
	private String reason;

	public String getEsmeaddr() {
		return esmeaddr;
	}

	public void setEsmeaddr(String esmeaddr) {
		this.esmeaddr = esmeaddr;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return " [esmeaddr=" + esmeaddr + ", fileId=" + fileId + ", mobile=" + mobile + ", reason=" + reason + "]";
	}
}
