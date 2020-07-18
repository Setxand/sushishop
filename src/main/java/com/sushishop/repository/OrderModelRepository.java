package com.sushishop.repository;

import com.sushishop.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderModelRepository extends JpaRepository<OrderModel, String> {
	List<OrderModel> findByUserIdAndStatus(String userId, OrderModel.OrderStatus status);
}
