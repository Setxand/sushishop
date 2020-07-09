package com.sushishop.util;

import com.sushishop.dto.CartDTO;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTOResponse;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.Cart;
import com.sushishop.model.Product;
import com.sushishop.model.Recipe;
import com.sushishop.model.User;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public class DtoUtil {


	public static ProductDTO product(Product entity) {
		ProductDTO dto = new ProductDTO();
		dto.id = entity.getId();
		dto.name = entity.getName();
		dto.price = entity.getPrice();
		dto.picture = entity.getPicture();
		dto.description = entity.getDescription();
		dto.weight = entity.getWeight();

		return dto;
	}

	public static RecipeDTOResponse recipe(Recipe entity) {
		RecipeDTOResponse dto = new RecipeDTOResponse();
		dto.id = entity.getId();
		dto.name = entity.getName();
		dto.products = entity.getProducts().stream().map(DtoUtil::product).collect(Collectors.toList());
		return dto;
	}

	public static UserDTO user(User entity) {
		UserDTO dto = new UserDTO();
		dto.id = entity.getId();
		dto.name = entity.getName();
		dto.phone = entity.getPhone();
		dto.role = entity.getRole().name();
		dto.email = entity.getEmail();
		return dto;
	}

	public static CartDTO cart(Cart entity) {
		CartDTO dto = new CartDTO();
		dto.id = entity.getId();
		dto.userId = entity.getUserId();
		dto.totalPrice = entity.getTotalPrice();
		dto.products = entity.getProducts().stream().map(DtoUtil::product).collect(Collectors.toList());

		dto.products.forEach(p -> {
			p.amount = entity.getAmounts().get(p.id);
			p.price = p.price.multiply(new BigDecimal(entity.getAmounts().get(p.id)));
			p.weight = entity.getAmounts().get(p.id) * p.weight;
		});
//		dto.amounts = entity.getAmounts().entrySet().stream()
//				.collect(Collectors.toMap(k -> entity.getProducts().stream()
//								.filter(p -> p.getId().equals(k.getKey())).map(Product::getName)
//								.findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid product amount")),
//						Map.Entry::getValue));

		return dto;
	}
}
