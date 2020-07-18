package com.sushishop.integration;

import com.sushishop.dto.CartDTO;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTOResponse;
import com.sushishop.security.dto.JwtResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CartIntegrationTest extends BaseIntegrationTest {

	private static final String PRODUCTS_JSON = "$.products";
	private static final String USER_ID_JSON = "$.userId";
	private static final String TOTAL_PRICE_JSON = "$.totalPrice";

	@Test
	@Sql("classpath:clean.sql")
	public void cartIntegrationTest() throws Exception {

		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		// Get empty cart
		CartDTO emptyCart = getCart(jwtResponse.getUserId());
		Assert.assertTrue(emptyCart.products.isEmpty());

		// Add product to cart (Cart is creating if not exists)
		ProductDTO product = createProductPostRequest();

		// Put product in the cart
		putInTheCart(product, jwtResponse.getUserId());

		// Get cart with new product
		CartDTO cart = getCart(jwtResponse.getUserId());
		assertEquals(product.id, cart.products.get(0).id);
		assertEquals(jwtResponse.getUserId(), cart.userId);

		// Add list of products to cart (recipe) (Cart is creating if not exists)
		RecipeDTOResponse recipeWithFiveProducts = createRecipeWithFiveProducts();
		mockMvc.perform(put(CARTS_BASE_URL + "/recipes/{recipeId}", jwtResponse.getUserId(),
				recipeWithFiveProducts.id)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON, hasSize(6)));

		// Get cart with new 5 products and assert it
		cart = getCart(jwtResponse.getUserId());
		assertEquals(6, cart.products.size());
		assertEquals(jwtResponse.getUserId(), cart.userId);

		// Add the same product
		CartDTO testCart = putInTheCart(product, jwtResponse.getUserId());
		assertEquals(jwtResponse.getUserId(), cart.userId);
		assertEquals(cart.totalPrice.add(product.price), testCart.totalPrice);
		assertEquals(6, testCart.products.size());


		// Get cart and check product amount, price and weight
		cart = getCart(jwtResponse.getUserId());
		assertEquals(product.weight * 2, cart.products.stream()
				.filter(p -> p.id.equals(product.id)).findFirst().get().weight, 0.0);
		assertEquals(2, cart.products.stream().filter(p -> p.id.equals(product.id)).findFirst().get().amount);
		assertEquals(product.price.multiply(new BigDecimal(2)), cart.products.stream()
				.filter(p -> p.id.equals(product.id)).findFirst().get().price);

		// Remove product from cart
		mockMvc.perform(delete(CARTS_BASE_URL + "/products/{productId}", jwtResponse.getUserId(), product.id)
					.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON, hasSize(6)))
				.andExpect(status().isNoContent());

		// Get cart with 5 products (one was removed)
		cart = getCart(jwtResponse.getUserId());
		assertEquals(6, cart.products.size());
		assertEquals(jwtResponse.getUserId(), cart.userId);
		assertEquals(1, cart.products.stream().filter(p -> p.id.equals(product.id)).findFirst().get().amount);

		// Checkout ( order creation)

		// Remove cart after checkout
		mockMvc.perform(delete(CARTS_BASE_URL, jwtResponse.getUserId()).headers(authHeader(accessToken)))
				.andExpect(status().isNoContent());

		// Check cart (must be empty)
		cart = getCart(jwtResponse.getUserId());
		assertTrue(cart.products.isEmpty());
	}
}
