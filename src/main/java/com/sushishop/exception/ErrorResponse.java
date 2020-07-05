package com.sushishop.exception;

public class ErrorResponse {

	public String code;
	public String message;
	public Object data;

	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public ErrorResponse(String code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
}
