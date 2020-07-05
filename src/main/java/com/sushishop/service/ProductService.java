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

	private static final String INVALID_PRODUCT = "Invalid Product ID"
			;
	private final ProductRepository productRepo;

	public ProductService(ProductRepository productRepo) {
		this.productRepo = productRepo;
	}

	@Transactional
	public Product createProduct(ProductDTO dto) {
		Product product = new Product();
		product.setName(dto.name);
		product.setPrice(dto.price);
		product.setPicture(dto.picture);

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

		productRepo.save(product);
	}
}
