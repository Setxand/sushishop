package com.sushishop;

import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class TestUtil {


	public static ProductDTO createProduct() {
		ProductDTO dto = new ProductDTO();
		double price = ((Math.random() * (100 - 20)) + 20);
		dto.price = new BigDecimal(price);
		dto.name = "Product-test-name " + generateUUID();
		dto.picture = "Picture-test-url " + generateUUID();
		return dto;
	}

	public static RecipeDTORequest createRequestRecipe(List<String> productIds) {
		RecipeDTORequest dto = new RecipeDTORequest();
		dto.name = "Recipe-test-name " + generateUUID();
		dto.productIds = productIds;
		return dto;
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
}
