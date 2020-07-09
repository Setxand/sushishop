package com.sushishop.util;

import com.sushishop.dto.CartDTO;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTOResponse;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.Cart;
import com.sushishop.model.Product;
import com.sushishop.model.Recipe;
import com.sushishop.model.User;

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
		dto.products = entity.getProducts().stream().map(DtoUtil::product).collect(Collectors.toList());
		return dto;
	}
}
