package com.sushishop.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDTO {

	public String id;
	public String userId;
	public BigDecimal totalPrice;

	// key -> productId or name; val -> amount of product
//	public Map<Object, Object> amounts = new HashMap<>();
	public List<ProductDTO> products = new ArrayList<>();

}
