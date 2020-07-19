package com.sushishop.service;

import com.sushishop.dto.ProductDTO;
import com.sushishop.model.Product;
import com.sushishop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ProductService {

	private static final String INVALID_PRODUCT = "Invalid Product ID";
	private final ProductRepository productRepo;

	public ProductService(ProductRepository productRepo) {
		this.productRepo = productRepo;
	}

	@Transactional
	public com.sushishop.model.Product createProduct(ProductDTO dto) {
		com.sushishop.model.Product product = new com.sushishop.model.Product();
		product.setName(dto.name);
		product.setPrice(dto.price);
		product.setPicture(dto.picture);
		product.setDescription(dto.description);
		product.setWeight(dto.weight);

		return productRepo.saveAndFlush(product);
	}

	public Product getProduct(String productId) {
		return findProduct(productId);
	}

	public void removeProduct(String productId) {
		productRepo.deleteById(productId);
	}

	public Page<Product> getProducts(Pageable pageable) {
		return productRepo.findAll(pageable);
	}

	private Product findProduct(String productId) {
		return productRepo.findById(productId).orElseThrow(() -> new IllegalArgumentException(INVALID_PRODUCT));
	}

	@Transactional
	public void updateProduct(ProductDTO dto) {
		Product product = findProduct(dto.id);

		if (dto.keys.contains("name")) {
			product.setName(dto.name);
		}

		if (dto.keys.contains("picture")) {
			product.setPicture(dto.picture);
		}

		if (dto.keys.contains("price")) {
			product.setPrice(dto.price);
		}

		if (dto.keys.contains("picture")) {
			product.setPicture(dto.picture);
		}

		if (dto.keys.contains("description")) {
			product.setDescription(dto.description);
		}

		if (dto.keys.contains("inStock")) {
			product.setInStock(dto.inStock);
		}

		if (dto.keys.contains("weight")) {
			product.setWeight(dto.weight);
		}

		productRepo.save(product);
	}
}
