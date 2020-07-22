package com.sushishop.dto;

import com.sushishop.model.OrderModel;

import java.math.BigDecimal;
import java.util.List;

import static com.sushishop.model.OrderModel.OrderStatus.SUCCEED;

public class OrderDTO {

	public String id;
	public String userId;
	public String orderNumber;
	public OrderModel.OrderStatus status;

	public BigDecimal totalPrice;
	public List<ProductDTO> products;
	public AddressDTO address;
	public String paymentDate;

	@Override
	public String toString() {
		return
				"\norderNumber: " + orderNumber +
				"\nstatus: " + (status == SUCCEED ? "payed" : "unpayed") +
				"\ntotalPrice: " + totalPrice +
				"\nproducts: " + products +
				"\naddress: " + address +
				"\npaymentDate: " + paymentDate;
	}
}
