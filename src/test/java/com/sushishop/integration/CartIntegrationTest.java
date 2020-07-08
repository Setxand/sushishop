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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CartIntegrationTest extends BaseIntegrationTest {

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

		mockMvc.perform(put("/v1/users/{userId}/carts/products/{productId}", jwtResponse.getUserId(), product.id)
					.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(jwtResponse.getUserId()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.products", hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.products[0].id").value(product.id));

		// Get cart with new product
		CartDTO cart = getCart(jwtResponse.getUserId());
		Assert.assertEquals(cart.products.get(0).id, product.id);
		Assert.assertEquals(cart.userId, jwtResponse.getUserId());

		// Add list of products to cart (recipe) (Cart is creating if not exists)
		RecipeDTOResponse recipeWithFiveProducts = createRecipeWithFiveProducts();
		mockMvc.perform(put("/v1/users/{userId}/carts/recipes/{recipeId}", jwtResponse.getUserId(),
					recipeWithFiveProducts.id)
					.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(jwtResponse.getUserId()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.products", hasSize(6)));

		// Get cart with new 5 products and assert it
		cart = getCart(jwtResponse.getUserId());
		Assert.assertEquals(6, cart.products.size());
		Assert.assertEquals(jwtResponse.getUserId(), cart.userId);

		// Remove product from cart
		mockMvc.perform(delete("/v1/users/{userId}/carts/products/{productId}",
				jwtResponse.getUserId(), product.id)
				.headers(authHeader(accessToken)))
				.andExpect(status().isNoContent());

		// Get cart with 5 products (one was removed)
		cart = getCart(jwtResponse.getUserId());
		Assert.assertEquals(5, cart.products.size());
		Assert.assertEquals(jwtResponse.getUserId(), cart.userId);
		Assert.assertTrue(cart.products.stream().noneMatch(p -> p.id.equals(product.id)));

		// Checkout ( order creation)

		// Remove cart after checkout
	}

	private CartDTO getCart(String userId) throws Exception {
		return objectMapper.readValue(mockMvc.perform(get("/v1/users/{userId}/carts", userId)
				.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), CartDTO.class);
	}

}
