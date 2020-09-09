package com.sushishop.service;


import com.sushishop.model.Address;
import com.sushishop.model.Cart;
import com.sushishop.model.OrderModel;
import com.sushishop.model.OrderModel.OrderStatus;
import com.sushishop.model.User;
import com.sushishop.repository.AddressRepository;
import com.sushishop.repository.OrderModelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

	private static final String EMPTY_CART = "Cart is empty";
	private static final String INVALID_ORDER = "Invalid Order ID";
	private static final String ACTIVE_ORDER_EX_MESSAGE = "Active order must be only one";
	private static final String NOT_ACTIVE = "There is non-active order";
	private static final String NO_SUCH_FIELD = "No such field";

	private final UserService userService;
	private final AddressRepository addressRepo;
	private final OrderModelRepository orderRepo;
	private final CartService cartService;

	public OrderService(UserService userService, AddressRepository addressRepo, OrderModelRepository orderRepo,
						CartService cartService) {
		this.userService = userService;
		this.addressRepo = addressRepo;
		this.orderRepo = orderRepo;
		this.cartService = cartService;
	}

	@Transactional
	public OrderModel createOrder(String userId) {

		if (checkActiveOrderByUserId(userId)) {
			throw new IllegalStateException(ACTIVE_ORDER_EX_MESSAGE);
		}

		OrderModel order = new OrderModel();
		order.setUserId(userId);

		Cart cart = cartService.getCart(userId);

		if (cart.getProducts().isEmpty())
			throw new IllegalArgumentException(EMPTY_CART);

		order.setTotalPrice(cart.getTotalPrice());
		cart.getProducts().forEach(p -> order.getProducts().add(p));

		Map<String, Integer> amounts = cart.getAmounts();
		Map<String, Integer> orderProductAmounts = amounts.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		order.setProductAmounts(orderProductAmounts);

		User user = userService.getUser(userId);
		Address userAddress = user.getAddress();

		OrderModel savedOrder = orderRepo.saveAndFlush(order);

		Address address = addOrderAddress(savedOrder, user.getAddress());
		savedOrder.setAddress(addressRepo.saveAndFlush(address));

		savedOrder.setStatus(OrderStatus.ACTIVE);

		return savedOrder;
	}

	private Address addOrderAddress(OrderModel savedOrder, Address userAddress) {
		Address address = new Address();
		address.setId(savedOrder.getId());
		address.setEntrance(userAddress.getEntrance());
		address.setCity(userAddress.getCity());
		address.setStreet(userAddress.getStreet());
		address.setFloor(userAddress.getFloor());
		address.setRoomNumber(userAddress.getRoomNumber());
		address.setHouse(userAddress.getHouse());
		address.setHousing(userAddress.getHousing());
		return address;
	}

	@Transactional
	public OrderModel updateOrderAddress(String orderId, Map<String, Object> body) {
		OrderModel activeOrder = getOrderById(orderId);
		Address address = activeOrder.getAddress();

		body.keySet().forEach(k -> {
			try {
				Field declaredField = Address.class.getDeclaredField(k);
				declaredField.setAccessible(true);
				ReflectionUtils.setField(declaredField, address, body.get(k));
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(NO_SUCH_FIELD);
			}
		});

		return activeOrder;
	}

	public OrderModel getOrderById(String orderId) {
		return orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException(INVALID_ORDER));
	}

	@Transactional
	public void cancelOrder(String orderId) {
		OrderModel order = getOrderById(orderId);

		if (order.getStatus() == OrderStatus.ACTIVE) {
			cartService.removeCartByUser(order.getUserId());
			order.setStatus(OrderStatus.CANCELED);
		} else {
			throw new IllegalArgumentException(NOT_ACTIVE);
		}
	}

	public Page<OrderModel> getOrders(String userId, Pageable pageable) {
		return orderRepo.findByUserId(userId, pageable);
	}

	private boolean checkActiveOrderByUserId(String userId) {
		return !orderRepo.findByUserIdAndStatus(userId, OrderStatus.ACTIVE).isEmpty();
	}

	public OrderModel getActiveOrder(String userId) {
		List<OrderModel> activeOrders = orderRepo.findByUserIdAndStatus(userId, OrderStatus.ACTIVE);

		if (activeOrders.size() > 1) {
			throw new IllegalStateException(ACTIVE_ORDER_EX_MESSAGE);
		}

		if (activeOrders.isEmpty()) {
			throw new IllegalArgumentException(NOT_ACTIVE);
		}

		return activeOrders.get(0);
	}
}
