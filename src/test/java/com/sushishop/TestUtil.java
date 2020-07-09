package com.sushishop;

import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.Product;
import com.sushishop.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class TestUtil {


	public static ProductDTO createProductDTO() {
		ProductDTO dto = new ProductDTO();
		double price = ((Math.random() * (100 - 20)) + 20);
		dto.price = new BigDecimal(price);
		dto.name = "Product-test-name " + generateUUID();
		dto.description = "Product-test-description" + generateUUID();
		dto.weight = 0.1;
		dto.picture = "Picture-test-url " + generateUUID();
		return dto;
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
}
