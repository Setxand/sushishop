package com.sushishop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liqpay.LiqPayUtil;
import com.sushishop.client.LiqpayClient;
import com.sushishop.dto.LiqpayResponse;
import com.sushishop.model.OrderModel;
import config.LiqpayConfigProps;
import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
public class PaymentService {

	private final OrderService orderService;
	private final LiqpayClient liqpayClient;
	private final ObjectMapper objectMapper;
	private final LiqpayConfigProps liqpayProps;

	private static final Logger logger = Logger.getLogger(PaymentService.class);

	public PaymentService(OrderService orderService, LiqpayClient liqpayClient, ObjectMapper objectMapper,
						  LiqpayConfigProps liqpayProps) {
		this.orderService = orderService;
		this.liqpayClient = liqpayClient;
		this.objectMapper = objectMapper;
		this.liqpayProps = liqpayProps;
	}

	@Transactional
	public void updatePaymentStatus(String data, String signature) {
		String sign = LiqPayUtil.base64_encode(
				LiqPayUtil.sha1(liqpayProps.getPrivateKey() + data + liqpayProps.getPrivateKey()));

		if (!sign.equals(signature)) {
			throw new AccessDeniedException("Signature is non-identical");
		}

		String jsonData = new String(Base64.getDecoder().decode(data));
		LiqpayResponse liqpayResponse;

		try {
			 liqpayResponse = objectMapper.readValue(jsonData, LiqpayResponse.class);
		} catch (JsonProcessingException e) {
			logger.warn("Failed to update payment status", e);
			throw new RuntimeException(e);
		}

		OrderModel order = orderService.getOrderById(liqpayResponse.orderId);

		if (liqpayResponse.status.equals("success")) {
			order.setStatus(OrderModel.OrderStatus.SUCCEED);
			order.setOrderNumber(liqpayResponse.paymentId);
		} else {
			order.setStatus(OrderModel.OrderStatus.FAILED);
		}
	}

	@Transactional
	public String checkout(String userId) {
		OrderModel order = orderService.getActiveOrder(userId);
		String orderProductDescription = order.getProducts()
				.stream().map(o -> o.getName() + " ("+o.getPrice() +" UAH) x" + order.getProductAmounts()
						.get(o.getId())).collect(Collectors.joining(";\n"));
		return liqpayClient.createPaymentForm(order.getTotalPrice().toString(), order.getId(), orderProductDescription);
	}
}
