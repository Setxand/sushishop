package com.sushishop.client;

import com.liqpay.LiqPay;
import config.LiqpayConfigProps;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class LiqpayClient {

	private final LiqpayConfigProps liqpayProps;

	public LiqpayClient(LiqpayConfigProps liqpayProps) {
		this.liqpayProps = liqpayProps;
	}

	public String createPaymentForm(String amount, String orderId, String orderProductDescription) {
		HashMap<String, String> params = new HashMap<>();

		params.put("action", "pay");
		params.put("server_url", liqpayProps.getServerUrl());
		params.put("amount", amount);
		params.put("currency", "UAH");
		params.put("description", orderProductDescription);
		params.put("order_id", orderId);
		params.put("version", "3");

		LiqPay liqpay = new LiqPay(liqpayProps.getPublicKey(), liqpayProps.getPrivateKey());
		return liqpay.cnb_form(params);
	}
}
