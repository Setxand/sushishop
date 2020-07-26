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
public class OrderModel extends BaseModel{

	public enum OrderStatus {
		CREATED,
		ACTIVE,
		CANCELED,
		FAILED,
		SUCCEED
	}

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid")
	@GeneratedValue(generator = "uuid")
	private String id;
	private String orderNumber;
	private String userId;
	private BigDecimal totalPrice;
	private String paymentDate;

	@Enumerated(EnumType.STRING)
	private OrderStatus status = OrderStatus.CREATED; // only one order can be active

	@OneToOne(cascade = CascadeType.ALL)
	private Address address;

	@ElementCollection
	Map<String, Integer> productAmounts = new HashMap<>();

	@ManyToMany(cascade = CascadeType.PERSIST)
	private List<Product> products = new ArrayList<>();

	public BigDecimal getTotalPrice() {
		return totalPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
}
