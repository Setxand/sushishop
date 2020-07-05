package com.sushishop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.dto.ProductDTO;
import com.sushishop.service.ProductService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ProductController {

	@Autowired ProductService productService;
	@Autowired ObjectMapper objectMapper;

	@PostMapping("/v1/products")
	@ResponseStatus(HttpStatus.CREATED)
	public ProductDTO createProduct(@RequestBody ProductDTO dto) {
		return DtoUtil.product(productService.createProduct(dto));
	}

	@GetMapping("/v1/products/{productId}")
	public ProductDTO getProduct(@PathVariable String productId) {
		return DtoUtil.product(productService.getProduct(productId));
	}

	@GetMapping("/v1/products")
	public Page<ProductDTO> getProducts(Pageable pageable) {
		return productService.getProducts(pageable).map(DtoUtil::product);
	}

	@PatchMapping("/v1/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateProduct(@RequestBody Map<String, Object> body, @PathVariable String productId) {
		ProductDTO dto = objectMapper.convertValue(body, ProductDTO.class);
		dto.id = productId;
		dto.keys = body.keySet();
		productService.updateProduct(dto);
	}

	@DeleteMapping("/v1/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateProduct(@PathVariable String productId) {
		productService.removeProduct(productId);
	}
}
