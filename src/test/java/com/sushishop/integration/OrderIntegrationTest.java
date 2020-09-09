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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-sources/snippets")
public class OrderIntegrationTest extends BaseIntegrationTest {

	private static final String CHECKOUT_FORM_SNIPPET =
			"<form method=\"post\" action=\"https://www.liqpay.ua/api/3/checkout\"";
	private static final String CREATE_ORDER_DOC = "create-order";
	private static final String GET_ORDER_URL = "/v1/users/{userId}/active-order";
	private static final String STATUS_JSON = "$.status";
	private static final String CITY_JSON = "$.address.city";
	private static final String STREET_JSON = "$.address.street";
	private static final String HOUSE_JSON = "$.address.house";
	private static final String HOUSING_JSON = "$.address.housing";
	private static final String ENTRANCE_JSON = "$.address.entrance";
	private static final String FLOOR_JSON = "$.address.floor";
	private static final String ROOM_NUM_JSON = "$.address.roomNumber";
	private static final String ORDER_NUM_JSON = "$.orderNumber";
	private static final String GET_ORDER_DOC = "get-order";
	private static final String CODE_JSON = "$.code";
	private static final String INVALID_STATE_ERR = "INVALID_STATE";
	private static final String MESSAGE_JSON = "$.message";
	private static final String ONLY_ONE_MESSAGE = "Active order must be only one";
	private static final String UPDATE_ORDER_ADDR_URL = "/v1/orders/{orderId}/addresses";
	private static final String UPDATE_ORDER_ADDR_DOC = "add-order-address";
	private static final String CHECKOUT_URL = "/v1/users/{userId}/payments";
	private static final String CHECKOUT_DOC = "checkout";
	private static final String CANCEL_ORDER_URL = "/v1/orders/{orderId}";
	private static final String CANCEL_ORDER_DOC = "cancel-order";
	private static final String FIND_USER_ORDERS_DOC = "find-user-orders";
	private static final int PRODUCTS_SIZE = 5;

	@Autowired UserService userService;

	private JwtResponse adminJwtResponse;
	private JwtResponse jwtResponse;

	@Before
	public void setUp() throws Exception {
		adminJwtResponse = signUpRequest(User.UserRole.ROLE_ADMIN, "test@test12.com", "+3809711123");
		jwtResponse = signUpRequest();
	}

	@Test
	@Sql("classpath:clean.sql")
	public void orderIntegrationTest() throws Exception {

		accessToken = jwtResponse.getAccessToken();

		addTestAddressToUser(jwtResponse.getUserId(), TestUtil.createAddressDTO());

		User user = userService.getUser(jwtResponse.getUserId());

		// Create order (Add address data and something else)
		accessToken = adminJwtResponse.getAccessToken();
		createCart(user.getId());

		accessToken = jwtResponse.getAccessToken();
		mockMvc.perform(postRequest(null, user.getId()))
				.andExpect(status().isCreated())
				.andDo(document(CREATE_ORDER_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));


		// Get order
		OrderDTO order = objectMapper.readValue(mockMvc.perform(get(GET_ORDER_URL, user.getId())
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(STATUS_JSON).value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath(CITY_JSON).value(user.getAddress().getCity()))
				.andExpect(jsonPath(STREET_JSON).value(user.getAddress().getStreet()))
				.andExpect(jsonPath(HOUSE_JSON).value(user.getAddress().getHouse()))
				.andExpect(jsonPath(HOUSING_JSON).value(user.getAddress().getHousing()))
				.andExpect(jsonPath(ENTRANCE_JSON).value(user.getAddress().getEntrance()))
				.andExpect(jsonPath(FLOOR_JSON).value(user.getAddress().getFloor()))
				.andExpect(jsonPath(ROOM_NUM_JSON).value(user.getAddress().getRoomNumber()))
				.andExpect(jsonPath(ORDER_NUM_JSON).isEmpty())
				.andExpect(jsonPath(CREATED_JSON).isNotEmpty())
				.andExpect(jsonPath(USER_ID_JSON).value(user.getId()))
				.andDo(document(GET_ORDER_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), OrderDTO.class);

		// Create another order while active order is exists
		mockMvc.perform(postRequest(null, user.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath(CODE_JSON).value(INVALID_STATE_ERR))
				.andExpect(jsonPath(MESSAGE_JSON).value(ONLY_ONE_MESSAGE));


		// Update order address
		AddressDTO addressToUpdate = TestUtil.createAddressDTO();
		addressToUpdate.created = null;
		HashMap<String, Object> addressMap = objectMapper
				.convertValue(addressToUpdate, new TypeReference<HashMap<String, Object>>() {
				});
		addressMap.entrySet()
				.removeAll(addressMap.entrySet().stream().filter(m -> m.getValue() == null).collect(Collectors.toList()));

		mockMvc.perform(patchRequestWithUrl(UPDATE_ORDER_ADDR_URL, addressMap, order.id))
				.andExpect(jsonPath(PRODUCTS_JSON, hasSize(order.products.size())))
				.andExpect(jsonPath(STATUS_JSON).value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath(CITY_JSON).value(addressToUpdate.city))
				.andExpect(jsonPath(STREET_JSON).value(addressToUpdate.street))
				.andExpect(jsonPath(HOUSE_JSON).value(addressToUpdate.house))
				.andExpect(jsonPath(HOUSING_JSON).value(addressToUpdate.housing))
				.andExpect(jsonPath(ENTRANCE_JSON).value(addressToUpdate.entrance))
				.andExpect(jsonPath(FLOOR_JSON).value(addressToUpdate.floor))
				.andExpect(jsonPath(ROOM_NUM_JSON).value(addressToUpdate.roomNumber))
				.andDo(document(UPDATE_ORDER_ADDR_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));


		// Checkout
		String paymentForm = mockMvc.perform(get(CHECKOUT_URL, jwtResponse.getUserId())
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andDo(document(CHECKOUT_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString();

		assertTrue(paymentForm.startsWith(CHECKOUT_FORM_SNIPPET));

		// Cancel order
		mockMvc.perform(delete(CANCEL_ORDER_URL, order.id)
				.headers(authHeader(accessToken)))
				.andDo(document(CANCEL_ORDER_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andExpect(status().isNoContent());

		// Find orders by user
		accessToken = adminJwtResponse.getAccessToken();
		CartDTO cart = createCart(user.getId());

		accessToken = jwtResponse.getAccessToken();
		OrderDTO order1 = objectMapper.readValue(mockMvc.perform(postRequest(null, user.getId()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath(PRODUCTS_JSON, hasSize(cart.products.size())))
				.andExpect(jsonPath(STATUS_JSON).value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath(CITY_JSON).value(user.getAddress().getCity()))
				.andExpect(jsonPath(STREET_JSON).value(addressToUpdate.street))
				.andExpect(jsonPath(HOUSE_JSON).value(addressToUpdate.house))
				.andExpect(jsonPath(HOUSING_JSON).value(addressToUpdate.housing))
				.andExpect(jsonPath(ENTRANCE_JSON).value(addressToUpdate.entrance))
				.andExpect(jsonPath(FLOOR_JSON).value(addressToUpdate.floor))
				.andExpect(jsonPath(ROOM_NUM_JSON).value(addressToUpdate.roomNumber))
				.andExpect(jsonPath(ORDER_NUM_JSON).isEmpty())
				.andExpect(jsonPath(USER_ID_JSON).value(user.getId()))
				.andDo(document(FIND_USER_ORDERS_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), OrderDTO.class);

		cancelOrder(order1.id);
		OrderDTO order2 = createOrder(user);

		accessToken = jwtResponse.getAccessToken();
		mockMvc.perform(get(ORDERS_BASE_URL, user.getId())
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(CONTENT_JSON, hasSize(3)));
	}

	private void cancelOrder(String orderId) throws Exception {
		mockMvc.perform(delete(CANCEL_ORDER_URL, orderId)
				.headers(authHeader(accessToken)))
				.andExpect(status().isNoContent());
	}

	private OrderDTO getOrder(User user) throws Exception {
		return objectMapper.readValue(mockMvc.perform(get(GET_ORDER_URL, user.getId())
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(STATUS_JSON).value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath(CITY_JSON).value(user.getAddress().getCity()))
				.andExpect(jsonPath(PRODUCTS_JSON, hasSize(PRODUCTS_SIZE)))
				.andExpect(jsonPath(STREET_JSON).value(user.getAddress().getStreet()))
				.andExpect(jsonPath(HOUSE_JSON).value(user.getAddress().getHouse()))
				.andExpect(jsonPath(HOUSING_JSON).value(user.getAddress().getHousing()))
				.andExpect(jsonPath(ENTRANCE_JSON).value(user.getAddress().getEntrance()))
				.andExpect(jsonPath(FLOOR_JSON).value(user.getAddress().getFloor()))
				.andExpect(jsonPath(ROOM_NUM_JSON).value(user.getAddress().getRoomNumber()))
				.andExpect(jsonPath(ORDER_NUM_JSON).isEmpty())
				.andExpect(jsonPath(USER_ID_JSON).value(user.getId()))
				.andReturn().getResponse().getContentAsString(), OrderDTO.class);
	}

	private OrderDTO createOrder(User user) throws Exception {
		accessToken = adminJwtResponse.getAccessToken();

		CartDTO cart = createCart(user.getId());

		accessToken = jwtResponse.getAccessToken();
		OrderDTO dto = objectMapper.readValue(mockMvc.perform(postRequest(null, user.getId()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath(PRODUCTS_JSON, hasSize(PRODUCTS_SIZE)))
				.andExpect(jsonPath(STATUS_JSON).value(OrderModel.OrderStatus.ACTIVE.name()))
				.andExpect(jsonPath(CITY_JSON).value(user.getAddress().getCity()))
				.andExpect(jsonPath(PRODUCTS_JSON, hasSize(PRODUCTS_SIZE)))
				.andExpect(jsonPath(STREET_JSON).value(user.getAddress().getStreet()))
				.andExpect(jsonPath(HOUSE_JSON).value(user.getAddress().getHouse()))
				.andExpect(jsonPath(HOUSING_JSON).value(user.getAddress().getHousing()))
				.andExpect(jsonPath(ENTRANCE_JSON).value(user.getAddress().getEntrance()))
				.andExpect(jsonPath(FLOOR_JSON).value(user.getAddress().getFloor()))
				.andExpect(jsonPath(ROOM_NUM_JSON).value(user.getAddress().getRoomNumber()))
				.andExpect(jsonPath(ORDER_NUM_JSON).isEmpty())
				.andExpect(jsonPath(USER_ID_JSON).value(user.getId()))
				.andReturn().getResponse().getContentAsString(), OrderDTO.class);

		assertEquals(0, dto.totalPrice.compareTo(cart.totalPrice));
		return dto;
	}

	private CartDTO createCart(String userId) throws Exception {
		for (int i = 0; i < PRODUCTS_SIZE; i++) {
			ProductDTO product = createProductPostRequest();
			putInTheCart(product, userId);
		}

		return getCart(userId);
	}
}
