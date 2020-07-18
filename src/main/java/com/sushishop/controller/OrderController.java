package com.sushishop.controller;

import com.sushishop.dto.OrderDTO;
import com.sushishop.service.OrderService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OrderController {

	@Autowired OrderService orderService;

	@PostMapping("/v1/users/{userId}/orders")
	@ResponseStatus(HttpStatus.CREATED)
	public OrderDTO createOrder(@PathVariable String userId) {
		return DtoUtil.order(orderService.createOrder(userId));
	}

	@GetMapping("/v1/users/{userId}/orders")
	public OrderDTO getActiveOrder(@PathVariable String userId) {
		return DtoUtil.order(orderService.getActiveOrder(userId));
	}

	@PatchMapping("/v1/orders/{orderId}/addresses")
	public OrderDTO updateOrderAddress(@PathVariable String orderId, @RequestBody Map<String, Object> body) {
		return DtoUtil.order(orderService.updateOrderAddress(orderId, body));
	}
}
