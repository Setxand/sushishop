package com.sushishop;

import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.*;
import com.sushishop.util.DtoUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestUtil {


	public static ProductDTO createProductDTO() {
		return DtoUtil.product(createTestProduct());
	}

	public static Product createTestProduct() {
		return createTestProduct(generateUUID());
	}
	public static Product createTestProduct(String id) {
		Product product = new Product();
		double price = randomPrice();
		product.setId(id);
		product.setPrice(new BigDecimal(price));
		product.setName("Product-test-name " + generateUUID());
		product.setDescription("Product-test-description" + generateUUID());
		product.setWeight(0.1);
		product.setPicture("Picture-test-url " + generateUUID());
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
		dto.name = "Recipe-test-name " + generateUUID();
		dto.productIds = productIds;
		return dto;
	}

	public static UserDTO createUserDTO() {
		UserDTO dto = new UserDTO();
		dto.email = "Email-test" + generateUUID();
		dto.phone = "Phone-test" + generateUUID();
		dto.name = "John Doe-test-name" + generateUUID();
		dto.password = dto.email + "1111";
		dto.role = User.UserRole.ROLE_USER.name();
		return dto;
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

	public static Recipe createRecipe(List<Product> products) {
		Recipe recipe = new Recipe();
		recipe.setId(generateUUID());
		recipe.setName("Recipe-test-name" + generateUUID());
		recipe.setProducts(products);
		return recipe;
	}

	public static AddressDTO createAddressDTO() {
		return DtoUtil.address(createTestAddress());
	}

	private static Address createTestAddress() {
		Address address = new Address();
		address.setCity("city-test" + generateUUID());
		address.setEntrance(generateUUID());
		address.setFloor(generateUUID());
		address.setHouse(generateUUID());
		address.setHousing(generateUUID());
		address.setRoomNumber(generateUUID());
		address.setStreet(generateUUID());

		return  address;
	}
}
