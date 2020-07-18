package com.sushishop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LiqpayResponse {

	@JsonProperty("order_id")
	public String orderId;

	@JsonProperty("payment_id")
	public String paymentId;
	public String status;

}
