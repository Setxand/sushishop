package com.sushishop.controller;

import com.sushishop.dto.CartDTO;
import com.sushishop.service.CartService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {

	@Autowired CartService cartService;

	@GetMapping("/v1/users/{userId}/carts")
	public CartDTO getCart(@PathVariable String userId) {
		return DtoUtil.cart(cartService.getCart(userId));
	}

	@PutMapping("/v1/users/{userId}/carts/products/{productId}")
	public CartDTO addToCart(@PathVariable String userId, @PathVariable String productId) {
		return DtoUtil.cart(cartService.addToCart(userId, productId));
	}

	@PutMapping("/v1/users/{userId}/carts/recipes/{recipeId}")
	public CartDTO addRecipeToCart(@PathVariable String userId, @PathVariable String recipeId) {
		return DtoUtil.cart(cartService.addRecipeToCart(userId, recipeId));
	}

	@DeleteMapping("/v1/users/{userId}/carts/products/{productId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public CartDTO removeProductFromCart(@PathVariable String userId, @PathVariable String productId) {
		return DtoUtil.cart(cartService.removeProductFromCart(userId, productId));
	}
}
