package com.sushishop.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Set;

import static com.sushishop.model.Product.ProductType;

public class ProductDTO extends BaseDTO {

	public String id;
	public String name;
	public String subName;
	public String picture;
	public String description;
	public double weight;
	public boolean inStock;
	public ProductType productType;
	public String holdConditions;
	public String packNumber;
	public String packing;

	// Amount of the products in the cart and order
	public int amount;
	public BigDecimal price;

	@JsonIgnore
	public Set<String> keys;

	@Override
	public String toString() {
		return " " + name + " x" + amount + " ";
	}
}
