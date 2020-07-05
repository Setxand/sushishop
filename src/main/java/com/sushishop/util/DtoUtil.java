package com.sushishop.util;

import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTOResponse;
import com.sushishop.model.Product;
import com.sushishop.model.Recipe;

import java.util.stream.Collectors;

public class DtoUtil {


	public static ProductDTO product(Product entity) {
		ProductDTO dto = new ProductDTO();
		dto.id = entity.getId();
		dto.name = entity.getName();
		dto.price = entity.getPrice();
		dto.picture = entity.getPicture();

		return dto;
	}

	public static RecipeDTOResponse recipe(Recipe entity) {
		RecipeDTOResponse dto = new RecipeDTOResponse();
		dto.id = entity.getId();
		dto.name = entity.getName();
		dto.products = entity.getProducts().stream().map(DtoUtil::product).collect(Collectors.toList());
		return dto;
	}
}
