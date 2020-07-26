package com.sushishop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.dto.ProductDTO;
import com.sushishop.service.ProductService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.sushishop.model.Product.ProductType;

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
	public Page<ProductDTO> getProducts(@RequestParam(defaultValue = "COMMON") ProductType type,
										@PageableDefault(sort = "created") Pageable pageable) {
		return productService.getProducts(type, pageable).map(DtoUtil::product);
	}

	@PatchMapping("/v1/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateProduct(@RequestBody Map<String, Object> body, @PathVariable String productId) {
		productService.updateProduct(productId, body);
	}

	@DeleteMapping("/v1/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateProduct(@PathVariable String productId) {
		productService.removeProduct(productId);
	}
}
