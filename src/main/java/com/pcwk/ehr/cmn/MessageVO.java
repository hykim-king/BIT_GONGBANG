package com.pcwk.ehr.cmn;

/**
 * AJAX 공통 JSON 응답 (v2): code, message, data.
 */
public class MessageVO {

	private String code;
	private String message;
	private Object data;

	public MessageVO() {
	}

	public MessageVO(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public MessageVO(String code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
