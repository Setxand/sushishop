package com.sushishop.service;

import com.sushishop.dto.CartDTO;
import com.sushishop.model.Cart;
import com.sushishop.repository.CartRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Service
public class CartService {

	private final CartRepository cartRepo;
	private final ProductService productService;
	private final RecipeService recipeService;

	public CartService(CartRepository cartRepo, ProductService productService, RecipeService recipeService) {
		this.cartRepo = cartRepo;
		this.productService = productService;
		this.recipeService = recipeService;
	}

	public Cart getCart(String userId) {
		return cartRepo.findByUserId(userId).orElseGet(() -> new Cart(userId));
	}

	@Transactional
	public Cart addToCart(String userId, String productId) {
		Cart cart = getCart(userId);

		cart.getProducts().add(productService.getProduct(productId));

		if (cart.getId() == null)
			cart = cartRepo.saveAndFlush(cart);

		return cart;
	}

	@Transactional
	public Cart addRecipeToCart(String userId, String recipeId) {
		Cart cart = getCart(userId);
		cart.getProducts().addAll(recipeService.getRecipe(recipeId).getProducts());
		return cart;
	}

	@Transactional
	public void removeProductFromCart(String userId, String productId) {
		Cart cart = getCart(userId);
		cart.getProducts().remove(cart.getProducts().stream().filter(p -> p.getId().equals(productId))
				.collect(Collectors.toList()).get(0));
	}
}
