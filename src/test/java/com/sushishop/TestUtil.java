package com.sushishop;

import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.*;
import com.sushishop.util.DtoUtil;

import java.math.BigDecimal;
import java.util.*;

public class TestUtil {


	public static ProductDTO createProductDTO() {
		return DtoUtil.product(createTestProduct());
	}

	public static Product createTestProduct() {
		return createTestProduct(generateUUID());
	}

	public static Product createTestProduct(String id) {
		String alph = "12345abcde";
		Product product = new Product();
		double price = randomPrice();
		product.setId(id);
		product.setPrice(new BigDecimal(price));
		product.setName("Product-test-name " + alph.charAt(new Random().nextInt(alph.length())));
		product.setDescription("Product-test-description");
		product.setWeight(0.1);
		product.setPicture("Picture-test-url");
		return product;
	}

	private static double randomPrice() {
		return ((Math.random() * (100 - 20)) + 20);
	}

	public static Cart createTestCart(String userId) {
		Cart cart = new Cart(userId);
		cart.setTotalPrice(BigDecimal.valueOf(randomPrice()));

		Product testProduct1 = createTestProduct(generateUUID());
		cart.getProducts().add(testProduct1);
		Product testProduct2 = createTestProduct(generateUUID());
		cart.getProducts().add(testProduct2);
		Product testProduct3 = createTestProduct(generateUUID());
		cart.getProducts().add(testProduct3);

		Map<String, Integer> map = new HashMap<>();
		map.put(testProduct1.getId(), 2);
		map.put(testProduct2.getId(), 2);
		map.put(testProduct3.getId(), 2);

		cart.setAmounts(map);
		cart.setTotalPrice(testProduct1.getPrice()
				.add(testProduct2.getPrice()).add(testProduct3.getPrice()).multiply(new BigDecimal(2)));
		return cart;
	}

	public static RecipeDTORequest createRequestRecipe(List<String> productIds) {
		RecipeDTORequest dto = new RecipeDTORequest();
		dto.name = "Recipe-test-name";
		dto.productIds = productIds;
		return dto;
	}

	public static User createTestUser() {
		User user = new User();
		user.setPhone("+380956435344");
		user.setEmail("Email-test@test.com");
		user.setId(generateUUID());
		user.setName("John Doe-test-name");
		user.setPassword("1111sd@f");
		user.setRole(User.UserRole.ROLE_USER);
		return user;
	}

	public static UserDTO createUserDTO() {
		return DtoUtil.user(createTestUser());
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

	public static Recipe createTestRecipe(List<Product> products) {
		Recipe recipe = new Recipe();
		recipe.setId(generateUUID());
		recipe.setName("Recipe-test-name");
		recipe.setProducts(products);
		return recipe;
	}

	public static AddressDTO createAddressDTO() {
		return DtoUtil.address(createTestAddress());
	}

	private static Address createTestAddress() {
		Address address = new Address();
		address.setCity("Kiev");
		address.setEntrance("Entrance-test");
		address.setFloor("6");
		address.setHouse("34");
		address.setHousing("A");
		address.setRoomNumber("27");
		address.setStreet("Bankova");

		return address;
	}

	public static OrderModel createTestOrder(String userId) {
		OrderModel orderModel = new OrderModel();
		orderModel.setUserId(userId);
		orderModel.setId(generateUUID());
		orderModel.setStatus(OrderModel.OrderStatus.CREATED);

		Cart testCart = createTestCart(orderModel.getUserId());
		orderModel.setProductAmounts(testCart.getAmounts());
		orderModel.setProducts(testCart.getProducts());
		orderModel.setTotalPrice(testCart.getTotalPrice());
		orderModel.setAddress(createTestAddress());

		return orderModel;
	}
}
