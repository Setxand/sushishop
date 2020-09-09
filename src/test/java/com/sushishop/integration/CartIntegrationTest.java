package com.sushishop.integration;

import com.sushishop.dto.AnonymousCartResponse;
import com.sushishop.dto.CartDTO;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTOResponse;
import com.sushishop.model.User;
import com.sushishop.security.dto.JwtResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-sources/snippets")
public class CartIntegrationTest extends BaseIntegrationTest {

	private static final String USER_ID_JSON = "$.userId";
	private static final String TOTAL_PRICE_JSON = "$.totalPrice";
	private static final String GET_EMPTY_CART_DOC = "get-empty-cart";
	private static final String TEST_EMAIL = "test@test12.com";
	private static final String TEST_PHONE = "+3809711123";
	private static final String PUT_IN_THE_CART_URL = "/v1/users/{userId}/carts/products/{productId}";
	private static final String PUT_IN_THE_CART_DOC = "put-in-the-cart";
	private static final String GET_CART_DOC = "get-cart";
	private static final String ADD_RECIPE_TO_THE_CART_URL = "/v1/users/{userId}/carts/recipes/{recipeId}";
	private static final String ADD_RECIPE_TO_THE_CART_DOC = "add-recipe-to-cart";
	private static final int PRODUCTS_SUM_SIZE = 6;
	private static final String REMOVE_FROM_CART_URL = "/v1/users/{userId}/carts/products/{productId}";
	private static final String REMOVE_FROM_CART_DOC = "remove-from-cart";
	private static final String REMOVE_CART_URL = "/v1/users/{userId}/carts";
	private static final String REMOVE_CART_DOC = "remove-cart";
	private static final String PUT_PRODUCT_IN_CART_ANONYMOUS_URL = "/v1/anonymous-users/products/{productId}";
	private static final String PUT_PRODUCT_IN_CART_ANONYMOUS_DOC = "put-in-the-cart-anonymous";

	@Test
	@Sql("classpath:clean.sql")
	public void cartIntegrationTest() throws Exception {

		JwtResponse jwtResponse = signUpRequest();
		setAccessToken(jwtResponse.getAccessToken());

		// Get empty cart
		CartDTO emptyCart = objectMapper.readValue(mockMvc.perform(get(CARTS_BASE_URL, jwtResponse.getUserId())
				.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andExpect(status().isOk())
				.andDo(document(GET_EMPTY_CART_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), CartDTO.class);

		Assert.assertTrue(emptyCart.products.isEmpty());

		// Add product to cart (Cart is creating if not exists)
		JwtResponse adminJwtResponse = signUpRequest(User.UserRole.ROLE_ADMIN, TEST_EMAIL, TEST_PHONE);
		accessToken = adminJwtResponse.getAccessToken();
		ProductDTO product = createProductPostRequest();

		// Put product in the cart
		objectMapper.readValue(
				mockMvc.perform(put(PUT_IN_THE_CART_URL, jwtResponse.getUserId(), product.id)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andDo(document(PUT_IN_THE_CART_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), CartDTO.class);

		// Get cart with new product
		accessToken = jwtResponse.getAccessToken();
		CartDTO cart = objectMapper.readValue(mockMvc.perform(get(CARTS_BASE_URL, jwtResponse.getUserId())
				.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andExpect(status().isOk())
				.andDo(document(GET_CART_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), CartDTO.class);

		assertEquals(product.id, cart.products.get(0).id);
		assertEquals(jwtResponse.getUserId(), cart.userId);

		// Add list of products to cart (recipe) (Cart is creating if not exists)
		accessToken = adminJwtResponse.getAccessToken();
		RecipeDTOResponse recipeWithFiveProducts = createRecipeWithFiveProducts();
		mockMvc.perform(put(ADD_RECIPE_TO_THE_CART_URL, jwtResponse.getUserId(),
				recipeWithFiveProducts.id)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON, hasSize(PRODUCTS_SUM_SIZE)))
				.andDo(document(ADD_RECIPE_TO_THE_CART_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Get cart with new 5 products and assert it
		accessToken = jwtResponse.getAccessToken();
		cart = getCart(jwtResponse.getUserId());
		assertEquals(PRODUCTS_SUM_SIZE, cart.products.size());
		assertEquals(jwtResponse.getUserId(), cart.userId);

		// Add the same product
		CartDTO testCart = putInTheCart(product, jwtResponse.getUserId());
		assertEquals(jwtResponse.getUserId(), cart.userId);
		assertEquals(cart.totalPrice.add(product.price), testCart.totalPrice);
		assertEquals(PRODUCTS_SUM_SIZE, testCart.products.size());


		// Get cart and check product amount, price and weight
		cart = getCart(jwtResponse.getUserId());
		assertEquals(product.weight * 2, cart.products.stream()
				.filter(p -> p.id.equals(product.id)).findFirst().get().weight, 0.0);
		assertEquals(2, cart.products.stream().filter(p -> p.id.equals(product.id)).findFirst().get().amount);
		assertEquals(product.price.multiply(new BigDecimal(2)), cart.products.stream()
				.filter(p -> p.id.equals(product.id)).findFirst().get().price);

		// Remove product from cart
		mockMvc.perform(delete(REMOVE_FROM_CART_URL, jwtResponse.getUserId(), product.id)
					.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON, hasSize(PRODUCTS_SUM_SIZE)))
				.andExpect(status().isNoContent())
				.andDo(document(REMOVE_FROM_CART_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Get cart with 5 products (one was removed)
		cart = getCart(jwtResponse.getUserId());
		assertEquals(PRODUCTS_SUM_SIZE, cart.products.size());
		assertEquals(jwtResponse.getUserId(), cart.userId);
		assertEquals(1, cart.products.stream().filter(p -> p.id.equals(product.id)).findFirst().get().amount);

		// Remove cart
		mockMvc.perform(delete(REMOVE_CART_URL, jwtResponse.getUserId()).headers(authHeader(accessToken)))
				.andExpect(status().isNoContent())
				.andDo(document(REMOVE_CART_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Check cart (must be empty)
		cart = getCart(jwtResponse.getUserId());
		assertTrue(cart.products.isEmpty());

		// Put product in the cart by anonymous user
		AnonymousCartResponse anonymousCartResponse = objectMapper.readValue(
				mockMvc.perform(put(PUT_PRODUCT_IN_CART_ANONYMOUS_URL, product.id))
						.andExpect(status().isOk())
						.andDo(document(PUT_PRODUCT_IN_CART_ANONYMOUS_DOC,
								preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
						.andReturn().getResponse().getContentAsString(), AnonymousCartResponse.class);

		String userId = anonymousCartResponse.anonymousUserDetails.getUserId();
		String anonymousToken = anonymousCartResponse.anonymousUserDetails.getAccessToken();
		assertNotNull(userId);
		assertNotNull(anonymousToken);
		assertEquals(product.id, anonymousCartResponse.cart.products.get(0).id);

		CartDTO anonCard = putInTheCart(product, userId);
		assertEquals(2, anonCard.products.get(0).amount);

		accessToken = adminJwtResponse.getAccessToken();
		ProductDTO anotherProduct = createProductPostRequest();

		// Put in the cart another product
		accessToken = anonymousToken;
		anonCard = putInTheCart(anotherProduct, userId);
		assertTrue(anonCard.products.stream().map(p -> p.id).collect(Collectors.toList()).contains(anotherProduct.id));
		assertEquals(2, anonCard.products.size());
	}
}
