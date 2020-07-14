package com.sushishop.service;

import com.sushishop.model.Cart;
import com.sushishop.model.Product;
import com.sushishop.model.User;
import com.sushishop.repository.CartRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class CartService {

	private static final String FAILED_REMOVING = "Failed to remove cart";


	private final CartRepository cartRepo;
	private final ProductService productService;
	private final RecipeService recipeService;
	private final UserService userService;

	public CartService(CartRepository cartRepo, ProductService productService,
					   RecipeService recipeService, UserService userService) {
		this.cartRepo = cartRepo;
		this.productService = productService;
		this.recipeService = recipeService;
		this.userService = userService;
	}

	public Cart getCart(String userId) {
		return cartRepo.findByUserId(userId).orElseGet(() -> new Cart(userId));
	}

	@Transactional
	public Cart addToCart(String userId, String productId) {
		Cart cart = getCart(userId);

		Product product = productService.getProduct(productId);

		cart.setTotalPrice(cart.getTotalPrice().add(product.getPrice()));

		Integer amountOfProduct = cart.getAmounts().getOrDefault(productId, 0);
		amountOfProduct += 1;
		cart.getAmounts().put(productId, amountOfProduct);

		if (amountOfProduct == 1) {
			cart.getProducts().add(product);
		}

		if (cart.getId() == null) {
			cart = cartRepo.saveAndFlush(cart);
			userService.getUser(userId).setCart(cart);
		}

		return cart;
	}

	@Transactional
	public Cart addRecipeToCart(String userId, String recipeId) {
		Cart cart = getCart(userId);
		recipeService.getRecipe(recipeId).getProducts().forEach(p -> addToCart(userId, p.getId()));
		return cart;
	}

	@Transactional
	public Cart removeProductFromCart(String userId, String productId) {
		Cart cart = getCart(userId);
		Product product = cart.getProducts().stream().filter(p -> p.getId().equals(productId)).findFirst()
							.orElseThrow(() -> new IllegalArgumentException(FAILED_REMOVING));

		cart.setTotalPrice(cart.getTotalPrice().subtract(product.getPrice()));

		Integer amountOfProduct = cart.getAmounts().get(productId);
		amountOfProduct -= 1;

		if (amountOfProduct == 0) {
			cart.getAmounts().remove(productId);
			cart.getProducts().remove(product);
		} else {
			cart.getAmounts().put(productId, amountOfProduct);
		}

		return cart;
	}

	@Transactional
	public void removeCart(String userId) {
		User user = userService.getUser(userId);
		Cart cart = getCart(userId);
		user.setCart(null);
		cartRepo.delete(cart);
	}
}
