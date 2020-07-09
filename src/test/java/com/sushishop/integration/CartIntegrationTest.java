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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CartIntegrationTest extends BaseIntegrationTest {

	private static final String PRODUCTS_JSON = "$.products";
	private static final String USER_ID_JSON = "$.userId";
	private static final String TOTAL_PRICE_JSON = "$.totalPrice";
	private static final String AMOUNT_JSON = "$.amount";
	private static final String WEIGHT_JSON = "$.amount";

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
		putInTheCart(product, jwtResponse.getUserId(), 1, product.price);

		// Get cart with new product
		CartDTO cart = getCart(jwtResponse.getUserId());
		Assert.assertEquals(product.id, cart.products.get(0).id);
		Assert.assertEquals(jwtResponse.getUserId(), cart.userId);

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
		Assert.assertEquals(6, cart.products.size());
		Assert.assertEquals(jwtResponse.getUserId(), cart.userId);

		// Add the same product
		putInTheCart(product, jwtResponse.getUserId(), 6, cart.totalPrice.add(product.price));

		// Remove product from cart
		mockMvc.perform(delete(CARTS_BASE_URL + "/products/{productId}", jwtResponse.getUserId(), product.id)
					.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON, hasSize(6)))
				.andExpect(status().isNoContent());

		// Get cart with 5 products (one was removed)
		cart = getCart(jwtResponse.getUserId());
		Assert.assertEquals(6, cart.products.size());
		Assert.assertEquals(jwtResponse.getUserId(), cart.userId);
		Assert.assertEquals(1, cart.products.stream().filter(p -> p.id.equals(product.id)).findFirst().get().amount);

		// Checkout ( order creation)

		// Remove cart after checkout
	}

	private void putInTheCart(ProductDTO product, String userId,
							  int expProductsSize, BigDecimal expTotalPrice)
			throws Exception {
		mockMvc.perform(put(CARTS_BASE_URL + "/products/{productId}", userId, product.id)
					.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(userId))
				.andExpect(MockMvcResultMatchers.jsonPath(TOTAL_PRICE_JSON).value(expTotalPrice))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON, hasSize(expProductsSize)));
	}

	private CartDTO getCart(String userId) throws Exception {
		return objectMapper.readValue(mockMvc.perform(get(CARTS_BASE_URL, userId)
				.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(userId))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), CartDTO.class);
	}

}
