package com.sushishop.security.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class JwtRequest implements Serializable {

	private static final long serialVersionUID = 5926468583005150707L;

	public String email;
	public String phone;
	public String password;

}
