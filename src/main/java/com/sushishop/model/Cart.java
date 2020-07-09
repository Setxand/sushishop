package com.sushishop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart {

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid")
	@GeneratedValue(generator = "uuid")
	private String id;
	private String userId;
	private BigDecimal totalPrice = new BigDecimal("0.00");

	@ElementCollection
	private Map<String, Integer> amounts = new HashMap<>();

	@ManyToMany
	private List<Product> products = new ArrayList<>();

	public Cart(String userId) {
		this.userId = userId;
	}
}
