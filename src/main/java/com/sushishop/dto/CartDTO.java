package com.sushishop.dto;

import java.util.ArrayList;
import java.util.List;

public class CartDTO {

	public String id;
	public String userId;
	public List<ProductDTO> products = new ArrayList<>();

}
