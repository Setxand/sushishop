package com.sushishop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.TestUtil;
import com.sushishop.model.Address;
import com.sushishop.model.Cart;
import com.sushishop.model.OrderModel;
import com.sushishop.model.User;
import com.sushishop.repository.AddressRepository;
import com.sushishop.repository.OrderModelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sushishop.TestUtil.*;
import static com.sushishop.model.OrderModel.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

	ObjectMapper objectMapper = new ObjectMapper();

	@Mock UserService userService;
	@Mock AddressRepository addressRepo;
	@Mock OrderModelRepository orderRepo;
	@Mock CartService cartService;

	@InjectMocks OrderService orderService;

	private User user;
	private OrderModel order;
	private Cart cart;

	@Before
	public void setUp() {
		user = createTestUser();
		user.setAddress(TestUtil.createTestAddress());
		order = createTestOrder(user.getId());
		cart = createTestCart(user.getId());

		when(userService.getUser(user.getId())).thenReturn(user);
		when(orderRepo.findByUserIdAndStatus(user.getId(), OrderModel.OrderStatus.ACTIVE))
				.thenReturn(Collections.singletonList(order));
		when(cartService.getCart(user.getId())).thenReturn(cart);
		when(orderRepo.findById(order.getId())).thenReturn(Optional.of(order));
	}

	@Test(expected = IllegalStateException.class)
	public void createOrderActiveIsAlreadyExists() {
		orderService.createOrder(user.getId());
	}

	@Test(expected = IllegalStateException.class)
	public void createOrderCartIsEmpty() {
		orderService.createOrder(user.getId());
	}

	@Test
	public void createOrder() {
		when(orderRepo.saveAndFlush(any(OrderModel.class))).thenReturn(createOrderFromUserAndCart());
		when(addressRepo.saveAndFlush(any(Address.class))).thenReturn(user.getAddress());
		when(orderRepo.findByUserIdAndStatus(user.getId(), ACTIVE)).thenReturn(Collections.emptyList());

		OrderModel order = orderService.createOrder(user.getId());
		assertEquals(ACTIVE, order.getStatus());
		assertEquals(user.getAddress(), order.getAddress());

		ArgumentCaptor<OrderModel> captor = ArgumentCaptor.forClass(OrderModel.class);

		verify(orderRepo).saveAndFlush(captor.capture());
		assertOrder(captor.getValue());
		assertEquals(CREATED, captor.getValue().getStatus());


		ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);

		verify(addressRepo).saveAndFlush(addressCaptor.capture());
		assertEquals(user.getAddress(), addressCaptor.getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void getOrderByIdInvalidArgument() {
		orderService.getOrderById(generateUUID());
	}

	@Test
	public void getOrderById() {
		orderService.getOrderById(order.getId());
		verify(orderRepo).findById(eq(order.getId()));
	}

	@Test
	public void cancelOrder() {
		order.setStatus(ACTIVE);
		orderService.cancelOrder(order.getId());

		verify(orderRepo).findById(eq(order.getId()));
		verify(cartService).removeCartByUser(order.getUserId());

		assertEquals(CANCELED, order.getStatus());
	}

	@Test(expected = IllegalArgumentException.class)
	public void cancelOrderNotActive() {
		orderService.cancelOrder(order.getId());

		verify(orderRepo).findById(eq(order.getId()));
		assertEquals(CREATED, order.getStatus());
	}

	@Test(expected = IllegalStateException.class)
	public void getActiveOrderMoreThanOne() {
		when(orderRepo.findByUserIdAndStatus(user.getId(), OrderModel.OrderStatus.ACTIVE))
				.thenReturn(Arrays.asList(createTestOrder(user.getId()), createTestOrder(user.getId())));
		orderService.getActiveOrder(user.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void getActiveOrderEmpty() {
		when(orderRepo.findByUserIdAndStatus(user.getId(), OrderModel.OrderStatus.ACTIVE))
				.thenReturn(Collections.emptyList());
		orderService.getActiveOrder(user.getId());
	}

	private void assertOrder(OrderModel order) {
		assertArrayEquals(cart.getProducts().toArray(), order.getProducts().toArray());
		assertEquals(cart.getTotalPrice(), order.getTotalPrice());
		assertEquals(user.getId(), order.getUserId());
		assertTrue(cart.getAmounts().entrySet().containsAll(order.getProductAmounts().entrySet()));
	}

	private OrderModel createOrderFromUserAndCart() {
		OrderModel order = new OrderModel();
		order.setUserId(user.getId());

		order.setTotalPrice(cart.getTotalPrice());
		cart.getProducts().forEach(p -> order.getProducts().add(p));

		Map<String, Integer> amounts = cart.getAmounts();
		Map<String, Integer> orderProductAmounts = amounts.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		order.setProductAmounts(orderProductAmounts);
		return order;
	}
}