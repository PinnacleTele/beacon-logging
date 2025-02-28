package com.winnovature.utils.dtos;

import java.sql.Timestamp;

public class Templates {

	private String tmplid;
	private String clientId;
	private String tmplname;
	private String tmpl_flds;
	private String msg_text;
	private String phoneNumberField;
	private Timestamp cdate;
	private String cuser;
	private Timestamp mdate;
	private String muser;
	private int unicode;
	private String language;
	private String templateType;

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getTmplid() {
		return tmplid;
	}

	public void setTmplid(String tmplid) {
		this.tmplid = tmplid;
	}


	public String getTmplname() {
		return tmplname;
	}

	public void setTmplname(String tmplname) {
		this.tmplname = tmplname;
	}

	public String getTmpl_flds() {
		return tmpl_flds;
	}

	public void setTmpl_flds(String tmpl_flds) {
		this.tmpl_flds = tmpl_flds;
	}

	public String getMsg_text() {
		return msg_text;
	}

	public void setMsg_text(String msg_text) {
		this.msg_text = msg_text;
	}

	public String getPhoneNumberField() {
		return phoneNumberField;
	}

	public void setPhoneNumberField(String phoneNumberField) {
		this.phoneNumberField = phoneNumberField;
	}

	public Timestamp getCdate() {
		return cdate;
	}

	public void setCdate(Timestamp cdate) {
		this.cdate = cdate;
	}

	public String getCuser() {
		return cuser;
	}

	public void setCuser(String cuser) {
		this.cuser = cuser;
	}

	public Timestamp getMdate() {
		return mdate;
	}

	public void setMdate(Timestamp mdate) {
		this.mdate = mdate;
	}

	public String getMuser() {
		return muser;
	}

	public void setMuser(String muser) {
		this.muser = muser;
	}

	public int getUnicode() {
		return unicode;
	}

	public void setUnicode(int unicode) {
		this.unicode = unicode;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "Template [tmplid=" + tmplid + ", clientId=" + clientId + ", tmplname=" + tmplname + ", tmpl_flds="
				+ tmpl_flds + ", msg_text=" + msg_text + ", phoneNumberField=" + phoneNumberField + ", cdate=" + cdate
				+ ", cuser=" + cuser + ", mdate=" + mdate + ", muser=" + muser + ", unicode=" + unicode + ", language="
				+ language + ", templateType=" + templateType + "]";
	}
	
	
}
