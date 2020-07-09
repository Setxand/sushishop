package com.sushishop.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartDTO {

	public String id;
	public String userId;
	public BigDecimal totalPrice;

	// key -> productId or name; val -> amount of product
//	public Map<Object, Object> amounts = new HashMap<>();
	public List<ProductDTO> products = new ArrayList<>();

}
