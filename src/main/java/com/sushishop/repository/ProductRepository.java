package com.sushishop.repository;

import com.sushishop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
	Page<Product> findAllByProductType(Product.ProductType type, Pageable pageable);
}
