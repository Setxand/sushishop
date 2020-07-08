package com.sushishop.integration;

import com.sushishop.dto.ProductDTO;
import com.sushishop.security.dto.JwtResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
public class ProductIntegrationTest extends BaseIntegrationTest {

	private static final String PIC_JSON_PATH = "$.picture";
	private static final String PRICE_JSON_PATH = "$.price";
	private static final String URI_WITH_ID_VAR = PRODUCTS_BASE_URL + "/{productId}";


	@Test
	@Sql("classpath:clean.sql")
	public void productIntegrationTest() throws Exception {


		// Create User to authenticate
		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		// Create new Product
		ProductDTO productResponse = createProductPostRequest();
		String productId = productResponse.id;

		// Check scale of price
		Assert.assertEquals(productResponse.price.scale(), 2);

		// Get new product
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON_PATH).value(productResponse.id))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON_PATH).value(productResponse.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRICE_JSON_PATH).value(productResponse.price))
				.andExpect(MockMvcResultMatchers.jsonPath(PIC_JSON_PATH).value(productResponse.picture));

		// Update product
		ProductDTO productRequestBodyToUpdate = new ProductDTO();
		productRequestBodyToUpdate.price = new BigDecimal("32.99");
		productRequestBodyToUpdate.name = "Product-test-name New product name";
		productRequestBodyToUpdate.picture = "New product picture";

		mockMvc.perform(patch(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(productRequestBodyToUpdate)))
				.andExpect(status().isNoContent());

		// Get updated product
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON_PATH).value(productResponse.id))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON_PATH).value(productRequestBodyToUpdate.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRICE_JSON_PATH).value(productRequestBodyToUpdate.price))
				.andExpect(MockMvcResultMatchers.jsonPath(PIC_JSON_PATH).value(productRequestBodyToUpdate.picture));

		// Remove product
		mockMvc.perform(delete(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		// Get removed product ( Must be 400 error )
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
				.andExpect(status().isBadRequest());

		// Get page of products (size = 3)
		mockMvc.perform(get(PRODUCTS_BASE_URL + "?page=0&size=3").headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(CONTENT_JSON_PATH, hasSize(3)));
	}
}
