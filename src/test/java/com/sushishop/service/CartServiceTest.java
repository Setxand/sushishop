package com.sushishop.service;

import com.sushishop.TestUtil;
import com.sushishop.model.Cart;
import com.sushishop.model.Product;
import com.sushishop.model.Recipe;
import com.sushishop.repository.CartRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static com.sushishop.TestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CartServiceTest {

	@Mock private CartRepository cartRepo;
	@Mock private ProductService productService;
	@Mock private RecipeService recipeService;

	@InjectMocks private CartService cartService;

	private String USER_ID_TEST;
	private Cart cart;

	@Before
	public void setUp() {
		USER_ID_TEST = generateUUID();
		cart = createTestCart(USER_ID_TEST);
		cart.setId(generateUUID());

		when(cartRepo.findByUserId(USER_ID_TEST)).thenReturn(Optional.of(cart));
	}

	@Test
	public void getCart() {
		Cart cartToReturn = cartService.getCart(USER_ID_TEST);
		Assert.assertEquals(cart, cartToReturn);
	}

	@Test
	public void getEmptyCart() {
		Cart cartToReturn = cartService.getCart("Some else id");
		assertTrue(cartToReturn.getProducts().isEmpty());
	}

	@Test
	public void addToCartTest() {
		Product product = TestUtil.createTestProduct();

		when(productService.getProduct(product.getId())).thenReturn(product);
		BigDecimal oldTotalPrice = cart.getTotalPrice();
		Cart cart = cartService.addToCart(USER_ID_TEST, product.getId());


		Assert.assertEquals(oldTotalPrice.add(product.getPrice()), cart.getTotalPrice());
		Assert.assertEquals(4, cart.getAmounts().size());
		Assert.assertEquals(4, cart.getProducts().size());
	}

	@Test
	public void removeProductFromCart() {
		Product product = createTestProduct();

		when(productService.getProduct(product.getId())).thenReturn(product);

		cartService.addToCart(USER_ID_TEST, product.getId());

		Assert.assertEquals(4, cart.getAmounts().size());
		Assert.assertEquals(4, cart.getProducts().size());
		assertTrue(cart.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId())));

		cartService.removeProductFromCart(USER_ID_TEST, product.getId());

		Assert.assertEquals(3, cart.getAmounts().size());
		Assert.assertEquals(3, cart.getProducts().size());
		assertTrue(cart.getProducts().stream().noneMatch(p -> p.getId().equals(product.getId())));
	}

	@Test
	public void addRecipeToCartTest() {
		cart.setAmounts(new HashMap<>());
		cart.setProducts(new ArrayList<>());
		cart.setTotalPrice(BigDecimal.ZERO);

		List<Product> products = Arrays.asList(createTestProduct(), createTestProduct(), createTestProduct());

		for (Product product : products) {
			when(productService.getProduct(product.getId())).thenReturn(product);
		}

		Recipe recipe = createRecipe(products);

		when(recipeService.getRecipe(recipe.getId())).thenReturn(recipe);

		Cart cart = cartService.addRecipeToCart(USER_ID_TEST, recipe.getId());

		assertEquals(products.stream().map(Product::getPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add), cart.getTotalPrice());
		assertEquals(3, cart.getAmounts().size());
		assertEquals(3, cart.getProducts().size());
	}
}