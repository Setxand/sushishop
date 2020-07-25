package com.sushishop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

	public enum ProductType {

		COMMON,
		RECIPE_COMPONENT

	}

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid")
	@GeneratedValue(generator = "uuid")
	private String id;
	private String name;
	private String picture;
	private String description;
	private double weight;
	private boolean inStock = true;
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	private ProductType productType;

	public BigDecimal getPrice() {
		return price.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
}
