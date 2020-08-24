package com.sushishop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liqpay.LiqPayUtil;
import com.sushishop.client.EmailClient;
import com.sushishop.client.LiqpayClient;
import com.sushishop.dto.LiqpayResponse;
import com.sushishop.model.OrderModel;
import com.sushishop.util.DtoUtil;
import config.LiqpayConfigProps;
import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
public class PaymentService {

	private static final String SUCCESS_TR_STATUS = "success";
	private static final Object PAYMENT_UPDATE_FAILED = "Failed to update payment status";
	private static final String SIGNATURE_ERROR = "Signature is non-identical";


	private final OrderService orderService;
	private final LiqpayClient liqpayClient;
	private final ObjectMapper objectMapper;
	private final LiqpayConfigProps liqpayProps;
	private final EmailClient emailClient;
	private final UserService userService;

	private static final Logger logger = Logger.getLogger(PaymentService.class);

	public PaymentService(OrderService orderService, LiqpayClient liqpayClient, ObjectMapper objectMapper,
						  LiqpayConfigProps liqpayProps, EmailClient emailClient, UserService userService) {
		this.orderService = orderService;
		this.liqpayClient = liqpayClient;
		this.objectMapper = objectMapper;
		this.liqpayProps = liqpayProps;
		this.emailClient = emailClient;
		this.userService = userService;
	}

	@Transactional
	public void updatePaymentStatus(String data, String signature) {
		String sign = LiqPayUtil.base64_encode(
				LiqPayUtil.sha1(liqpayProps.getPrivateKey() + data + liqpayProps.getPrivateKey()));

		if (!sign.equals(signature)) {
			throw new AccessDeniedException(SIGNATURE_ERROR);
		}

		String jsonData = new String(Base64.getDecoder().decode(data));
		LiqpayResponse liqpayResponse;

		try {
			 liqpayResponse = objectMapper.readValue(jsonData, LiqpayResponse.class);
		} catch (JsonProcessingException e) {
			logger.warn(PAYMENT_UPDATE_FAILED, e);
			throw new RuntimeException(e);
		}

		OrderModel order = orderService.getOrderById(liqpayResponse.orderId);

		if (liqpayResponse.status.equals(SUCCESS_TR_STATUS)) {
			order.setStatus(OrderModel.OrderStatus.SUCCEED);
			order.setOrderNumber(liqpayResponse.paymentId);
			order.setPaymentDate(new Timestamp(liqpayResponse.paymentDate).toLocalDateTime().toString());

			emailClient.sendEmail(DtoUtil.order(order).toString() +
					DtoUtil.user(userService.getUser(order.getUserId())).toString());
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
