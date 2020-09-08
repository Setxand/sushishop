package com.sushishop.dto;

import com.sushishop.security.dto.JwtResponse;

public class AnonymousCartResponse {

	public JwtResponse anonymousUserDetails;
	public CartDTO cart;

	public AnonymousCartResponse(JwtResponse anonymousUserDetails, CartDTO cart) {
		this.anonymousUserDetails = anonymousUserDetails;
		this.cart = cart;
	}
}
