package com.sushishop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liqpay.LiqPayUtil;
import com.sushishop.TestUtil;
import com.sushishop.client.EmailClient;
import com.sushishop.client.LiqpayClient;
import com.sushishop.dto.LiqpayResponse;
import com.sushishop.model.OrderModel;
import com.sushishop.model.User;
import com.sushishop.util.DtoUtil;
import config.LiqpayConfigProps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import java.util.Base64;
import java.util.stream.Collectors;

import static com.sushishop.TestUtil.*;
import static com.sushishop.model.OrderModel.OrderStatus.FAILED;
import static com.sushishop.model.OrderModel.OrderStatus.SUCCEED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {


	private static final String SOME_DATA = "jsonData";

	@Mock OrderService orderService;
	@Mock LiqpayClient liqpayClient;
	@Mock LiqpayConfigProps liqpayProps;
	@Mock ObjectMapper objectMapper;
	@Mock EmailClient emailClient;
	@Mock UserService userService;

	@InjectMocks PaymentService paymentService;

	private String publicKey;
	private String privateKey;
	private String updatePaymentStatusJsonData;
	private User user;

	@Before
	public void setUp() {
		publicKey = generateUUID();
		privateKey = generateUUID();
		updatePaymentStatusJsonData = SOME_DATA;
		when(liqpayProps.getPrivateKey()).thenReturn(privateKey);
		user = createTestUser();
		when(userService.getUser(user.getId())).thenReturn(user);

	}

	@Test
	public void updatePaymentStatusSuccess() throws JsonProcessingException {
		LiqpayResponse liqpayResponse = createLiqpayResponse("success");
		OrderModel order = updatePaymentStatusTest(liqpayResponse);

		assertEquals(liqpayResponse.paymentId, order.getOrderNumber());
		assertEquals(liqpayResponse.orderId, order.getId());
		assertEquals(SUCCEED, order.getStatus());
	}

	@Test
	public void updatePaymentStatusFailed() throws JsonProcessingException {
		LiqpayResponse liqpayResponse = createLiqpayResponse("non-success");
		OrderModel order = updatePaymentStatusTest(liqpayResponse);

		assertNull(order.getOrderNumber());
		assertEquals(liqpayResponse.orderId, order.getId());
		assertEquals(FAILED, order.getStatus());
	}

	@Test(expected = AccessDeniedException.class)
	public void updatePaymentStatusSignatureError() {
		paymentService.updatePaymentStatus(updatePaymentStatusJsonData, generateUUID());
	}

	@Test
	public void createCheckoutFormTest() {
		OrderModel order = createTestOrder(user.getId());

		when(orderService.getActiveOrder(user.getId())).thenReturn(order);

		String orderProductDescription = order.getProducts()
				.stream().map(o -> o.getName() + " (" + o.getPrice() + " UAH) x" + order.getProductAmounts()
						.get(o.getId())).collect(Collectors.joining(";\n"));

		paymentService.checkout(user.getId());

		verify(liqpayClient).createPaymentForm(ArgumentMatchers.eq(order.getTotalPrice().toString()),
				eq(order.getId()), eq(orderProductDescription));
	}

	private OrderModel updatePaymentStatusTest(LiqpayResponse liqpayResponse) throws JsonProcessingException {

		String signature = LiqPayUtil.base64_encode(LiqPayUtil.sha1(privateKey + updatePaymentStatusJsonData + privateKey));
		String jsonData = new String(Base64.getDecoder().decode(updatePaymentStatusJsonData));

		when(objectMapper.readValue(jsonData, LiqpayResponse.class)).thenReturn(liqpayResponse);

		OrderModel order = TestUtil.createTestOrder(user.getId());
		order.setId(liqpayResponse.orderId);

		when(orderService.getOrderById(liqpayResponse.orderId)).thenReturn(order);

		paymentService.updatePaymentStatus(updatePaymentStatusJsonData, signature);


		ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(emailClient).sendEmail(msgCaptor.capture());
		assertEquals(DtoUtil.order(order).toString() + DtoUtil.user(user).toString(), msgCaptor.getValue());
		assertNotNull(liqpayResponse.paymentDate, order.getPaymentDate());

		return order;
	}

	private LiqpayResponse createLiqpayResponse(String status) {
		LiqpayResponse liqpayResponse = new LiqpayResponse();
		liqpayResponse.status = status;
		liqpayResponse.orderId = generateUUID();
		liqpayResponse.paymentId = generateUUID();
		liqpayResponse.paymentDate = 1595429947555L;
		return liqpayResponse;
	}
}