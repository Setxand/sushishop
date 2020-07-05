package com.sushishop.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.nashorn.internal.objects.annotations.Getter;

import java.io.Serializable;

public class JwtResponse implements Serializable {

	private static final long serialVersionUID = -8091879091924046844L;

	private String accessToken;
	private String refreshToken;
	private String userId;

	public JwtResponse(String accessToken, String refreshToken, String userId) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.userId = userId;
	}


	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getUserId() {
		return userId;
	}
}
