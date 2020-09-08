package com.sushishop.controller;

import com.sushishop.dto.AnonymousCartResponse;
import com.sushishop.dto.CartDTO;
import com.sushishop.model.Cart;
import com.sushishop.model.User;
import com.sushishop.security.JwtTokenUtil;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.CartService;
import com.sushishop.service.UserService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class CartController {

	@Autowired CartService cartService;
	@Autowired UserService userService;
	@Autowired JwtTokenUtil jwtTokenUtil;

	@GetMapping("/v1/users/{userId}/carts")
	public CartDTO getCart(@PathVariable String userId) {
		return DtoUtil.cart(cartService.getCart(userId));
	}

	@PutMapping("/v1/users/{userId}/carts/products/{productId}")
	public CartDTO addToCart(@PathVariable String userId, @PathVariable String productId) {
		return DtoUtil.cart(cartService.addToCart(userId, productId));
	}

	@PutMapping("/v1/anonymous-users/products/{productId}")
	public AnonymousCartResponse addToCartAnonymous(@PathVariable String productId) {
		User anonymous = userService.createAnonymous();
		Cart cart = cartService.addToCart(anonymous.getId(), productId);

		String anonymousToken = jwtTokenUtil
				.generateToken(anonymous.getId(), anonymous.getEmail(), JwtTokenUtil.TokenType.ACCESS);
		JwtResponse jwtResponse = new JwtResponse(anonymousToken, null, anonymous.getId());
		return new AnonymousCartResponse(jwtResponse, DtoUtil.cart(cart));
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

	@DeleteMapping("/v1/users/{userId}/carts")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeCart(@PathVariable String userId) {
		cartService.removeCartByUser(userId);
	}
}
