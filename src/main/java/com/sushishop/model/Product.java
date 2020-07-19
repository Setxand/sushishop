package com.sushishop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

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

	public BigDecimal getPrice() {
		return price.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
}
