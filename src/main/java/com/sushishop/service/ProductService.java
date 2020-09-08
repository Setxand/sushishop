package com.sushishop.service;

import com.sushishop.dto.ProductDTO;
import com.sushishop.model.Product;
import com.sushishop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class ProductService {

	private static final String INVALID_PRODUCT = "Invalid Product ID";
	private static final String NO_SUCH_FIELD = "No such field";
	private final ProductRepository productRepo;

	public ProductService(ProductRepository productRepo) {
		this.productRepo = productRepo;
	}

	@Transactional
	public Product createProduct(ProductDTO dto) {
		com.sushishop.model.Product product = new com.sushishop.model.Product();
		product.setName(dto.name);
		product.setSubName(dto.subName);
		product.setPrice(dto.price);
		product.setPicture(dto.picture);
		product.setDescription(dto.description);
		product.setWeight(dto.weight);
		product.setProductType(dto.productType);
		product.setHoldConditions(dto.holdConditions);
		product.setPackNumber(dto.packNumber);
		product.setPacking(dto.packing);

		return productRepo.saveAndFlush(product);
	}

	public Product getProduct(String productId) {
		return findProduct(productId);
	}

	public void removeProduct(String productId) {
		productRepo.deleteById(productId);
	}

	public Page<Product> getProducts(Product.ProductType type, Pageable pageable) {

		if (type == Product.ProductType.COMMON) {
			return productRepo.findCommonProducts(pageable);
		}

		return productRepo.findAllByProductType(type, pageable);
	}

	private Product findProduct(String productId) {
		return productRepo.findById(productId).orElseThrow(() -> new IllegalArgumentException(INVALID_PRODUCT));
	}

	@Transactional
	public void updateProduct(String productId, Map<String, Object> body) {
		Product product = findProduct(productId);

		body.computeIfPresent("price", (k, v) -> BigDecimal.valueOf((Double) v));

			body.keySet().forEach(k -> {
			try {
				Field declaredField = Product.class.getDeclaredField(k);
				declaredField.setAccessible(true);
				ReflectionUtils.setField(declaredField, product, body.get(k));
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(NO_SUCH_FIELD);
			}
		});

		productRepo.save(product);
	}
}
