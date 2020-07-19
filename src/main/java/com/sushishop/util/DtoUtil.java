package com.sushishop.util;

import com.sushishop.dto.*;
import com.sushishop.model.*;

import java.math.BigDecimal;
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
		dto.inStock = entity.isInStock();

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
		dto.address = entity.getAddress() != null ? address(entity.getAddress()) : null;

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

		return dto;
	}

	public static AddressDTO address(Address entity) {
		AddressDTO dto = new AddressDTO();
		dto.id = entity.getId();
		dto.city = entity.getCity();
		dto.floor = entity.getFloor();
		dto.entrance = entity.getEntrance();
		dto.house = entity.getHouse();
		dto.housing = entity.getHousing();
		dto.roomNumber = entity.getRoomNumber();
		dto.street = entity.getStreet();

		return dto;
	}

	public static OrderDTO order(OrderModel entity) {
		OrderDTO dto = new OrderDTO();
		dto.id = entity.getId();
		dto.orderNumber = entity.getOrderNumber();
		dto.createdAt = entity.getCreatedAt();
		dto.userId = entity.getUserId();
		dto.totalPrice = entity.getTotalPrice();
		dto.address = address(entity.getAddress());
		dto.status = entity.getStatus();

		dto.products = entity.getProducts().stream().map(DtoUtil::product).collect(Collectors.toList());
		dto.products.forEach(p -> {
			p.amount = entity.getProductAmounts().get(p.id);
			p.price = p.price.multiply(new BigDecimal(entity.getProductAmounts().get(p.id)));
			p.weight = entity.getProductAmounts().get(p.id) * p.weight;
		});

		return dto;
	}
}
