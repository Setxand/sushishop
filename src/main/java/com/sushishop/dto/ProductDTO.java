package com.sushishop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Set;

public class ProductDTO {

	public String id;
	public String name;
	public String picture;
	public BigDecimal price;

	@JsonIgnore
	public Set<String> keys;
}
