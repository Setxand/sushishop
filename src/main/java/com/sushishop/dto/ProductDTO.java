package com.sushishop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Set;

public class ProductDTO {

	public String id;
	public String name;
	public String picture;
	public String description;
	public double weight;
	public boolean inStock;

	// Amount of the products in the cart
	public int amount;
	public BigDecimal price; ///todo need calculation of pricesl

	@JsonIgnore
	public Set<String> keys;

	@Override
	public String toString() {
		return " " + name + " x" + amount + " ";
	}
}
