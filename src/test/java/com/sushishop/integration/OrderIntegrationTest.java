package com.sushishop.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sushishop.TestUtil;
import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.CartDTO;
import com.sushishop.dto.OrderDTO;
import com.sushishop.dto.ProductDTO;
import com.sushishop.model.OrderModel;
import com.sushishop.model.User;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest extends BaseIntegrationTest {

	private static final String USER_ID_JSON = "$.userId";
	private static final String CHECKOUT_FORM_SNIPPET =
			"<form method=\"post\" action=\"https://www.liqpay.ua/api/3/checkout\"";

	@Autowired UserService userService;

	@Test
	public void orderIntegrationTest() throws Exception {

		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		addTestAddressToUser(jwtResponse.getUserId(), TestUtil.createAddressDTO());

		User user = userService.getUser(jwtResponse.getUserId());

		// Create order (Add address data and something else)
		OrderDTO order = createOrder(user);


		// Get order
		order = getOrder(user);

		// Create another order while active orer is exists
		mockMvc.perform(postRequest(null, user.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_STATE"))
				.andExpect(jsonPath("$.message").value("Active order must be only one"));


		// Update order address
		AddressDTO addressToUpdate = TestUtil.createAddressDTO();
		HashMap<String, Object> addressMap = objectMapper
				.convertValue(addressToUpdate, new TypeReference<HashMap<String, Object>>() {});
		addressMap.entrySet().removeAll(addressMap.entrySet().stream().filter(m -> m.getValue() == null).collect(Collectors.toList()));

		mockMvc.perform(patchRequestWithUrl("/v1/orders/{orderId}/addresses", addressMap, jwtResponse.getUserId()))
				.andExpect(jsonPath("$.products", hasSize(order.products.size())))
				.andExpect(jsonPath("$.totalPrice").value(order.totalPrice))
				.andExpect(jsonPath("$.status").value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath("$.address.city").value(addressToUpdate.city))
				.andExpect(jsonPath("$.address.street").value(addressToUpdate.street))
				.andExpect(jsonPath("$.address.house").value(addressToUpdate.house))
				.andExpect(jsonPath("$.address.housing").value(addressToUpdate.housing))
				.andExpect(jsonPath("$.address.entrance").value(addressToUpdate.entrance))
				.andExpect(jsonPath("$.address.floor").value(addressToUpdate.floor))
				.andExpect(jsonPath("$.address.roomNumber").value(addressToUpdate.roomNumber));

		// Checkout
		String paymentForm = mockMvc.perform(get("/v1/users/{userId}/payments", jwtResponse.getUserId()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		assertTrue(paymentForm.startsWith(CHECKOUT_FORM_SNIPPET));


		// Cancel order
//		mockMvc.perform(delete("/v1/orders/{orderId}", order.id));
		// Find orders by user
	}

	private OrderDTO getOrder(User user) throws Exception {
		return objectMapper.readValue(mockMvc.perform(get(ORDERS_BASE_URL, user.getId())
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.products", hasSize(order.products.size())))
//				.andExpect(jsonPath("$.totalPrice").value(order.totalPrice))
				.andExpect(jsonPath("$.status").value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath("$.address.city").value(user.getAddress().getCity()))
				.andExpect(jsonPath("$.address.street").value(user.getAddress().getStreet()))
				.andExpect(jsonPath("$.address.house").value(user.getAddress().getHouse()))
				.andExpect(jsonPath("$.address.housing").value(user.getAddress().getHousing()))
				.andExpect(jsonPath("$.address.entrance").value(user.getAddress().getEntrance()))
				.andExpect(jsonPath("$.address.floor").value(user.getAddress().getFloor()))
				.andExpect(jsonPath("$.address.roomNumber").value(user.getAddress().getRoomNumber()))
				.andExpect(jsonPath("$.orderNumber").isEmpty())
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath(USER_ID_JSON).value(user.getId())).andReturn().getResponse().getContentAsString(), OrderDTO.class);
	}

	private OrderDTO createOrder(User user) throws Exception {
		CartDTO cart = createCart(user.getId());

		return objectMapper.readValue(mockMvc.perform(postRequest(null, user.getId()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.products", hasSize(cart.products.size())))
				.andExpect(jsonPath("$.totalPrice").value(cart.totalPrice))
				.andExpect(jsonPath("$.status").value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath("$.address.city").value(user.getAddress().getCity()))
				.andExpect(jsonPath("$.address.street").value(user.getAddress().getStreet()))
				.andExpect(jsonPath("$.address.house").value(user.getAddress().getHouse()))
				.andExpect(jsonPath("$.address.housing").value(user.getAddress().getHousing()))
				.andExpect(jsonPath("$.address.entrance").value(user.getAddress().getEntrance()))
				.andExpect(jsonPath("$.address.floor").value(user.getAddress().getFloor()))
				.andExpect(jsonPath("$.address.roomNumber").value(user.getAddress().getRoomNumber()))
				.andExpect(jsonPath("$.orderNumber").isEmpty())
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath(USER_ID_JSON).value(user.getId()))
				.andReturn().getResponse().getContentAsString(), OrderDTO.class);
	}

	private CartDTO createCart(String userId) throws Exception {
		for (int i = 0; i < 5; i++) {
			ProductDTO product = createProductPostRequest();
			putInTheCart(product, userId);
		}

		return getCart(userId);
	}
}
