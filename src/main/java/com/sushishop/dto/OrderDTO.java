package com.sushishop.dto;

import com.sushishop.model.OrderModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

	public String id;
	public String userId;
	public String orderNumber;
	public OrderModel.OrderStatus status;

	public BigDecimal totalPrice;
	public List<ProductDTO> products;
	public AddressDTO address;

	public LocalDateTime createdAt;

}
