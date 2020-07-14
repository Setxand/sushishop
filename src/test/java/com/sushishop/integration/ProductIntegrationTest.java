package com.sushishop.integration;

import com.sushishop.TestUtil;
import com.sushishop.dto.ProductDTO;
import com.sushishop.security.dto.JwtResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductIntegrationTest extends BaseIntegrationTest {

	private static final String PIC_JSON = "$.picture";
	private static final String PRICE_JSON = "$.price";
	private static final String DESC_JSON = "$.description";
	private static final String WEIGHT_JSON = "$.weight";

	private static final String URI_WITH_ID_VAR = PRODUCTS_BASE_URL + "/{productId}";
	private static final String STOCK_JSON = "$.inStock";


	@Test
	@Sql("classpath:clean.sql")
	public void productIntegrationTest() throws Exception {


		// Create User to authenticate
		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		// Create new Product
		ProductDTO productResponse = createProductPostRequest();
		Assert.assertTrue(StringUtils.isNotBlank(productResponse.description));
		Assert.assertTrue(productResponse.weight > 0.0);
		String productId = productResponse.id;

		// Check scale of price
		Assert.assertEquals(productResponse.price.scale(), 2);

		// Get new product
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON).value(productResponse.id))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON).value(productResponse.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRICE_JSON).value(productResponse.price))
				.andExpect(MockMvcResultMatchers.jsonPath(DESC_JSON).value(productResponse.description))
				.andExpect(MockMvcResultMatchers.jsonPath(WEIGHT_JSON).value(productResponse.weight))
				.andExpect(MockMvcResultMatchers.jsonPath(PIC_JSON).value(productResponse.picture));

		// Update product
		Map<String, Object> productRequestBodyToUpdate = new HashMap<>();
		productRequestBodyToUpdate.put("price", new BigDecimal("32.99"));

		productRequestBodyToUpdate.put("name", "Product-test-name New product name");
		productRequestBodyToUpdate.put("picture", "New product picture");
		productRequestBodyToUpdate.put("weight", new BigDecimal("1.2").setScale(2).doubleValue());
		productRequestBodyToUpdate.put("description", TestUtil.generateUUID());

		// Request update
		updateProductRequest(productId, productRequestBodyToUpdate);

		// Get updated product
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON).value(productResponse.id))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON).value(productRequestBodyToUpdate.get("name")))
				.andExpect(MockMvcResultMatchers.jsonPath(PRICE_JSON).value(productRequestBodyToUpdate.get("price")))
				.andExpect(MockMvcResultMatchers.jsonPath(PIC_JSON).value(productRequestBodyToUpdate.get("picture")))
				.andExpect(MockMvcResultMatchers.jsonPath(DESC_JSON).value(productRequestBodyToUpdate.get("description")))
				.andExpect(MockMvcResultMatchers.jsonPath(STOCK_JSON).value(true))
				.andExpect(MockMvcResultMatchers.jsonPath(PIC_JSON).value(productRequestBodyToUpdate.get("picture")));

		// Change product's 'inStock' to false
		Map<String, Object> productInStockFalse = new HashMap<>();
		productInStockFalse.put("inStock", false);
		updateProductRequest(productId, productInStockFalse);

		// Get product that's not in stock
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON).value(productResponse.id))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON).value(productRequestBodyToUpdate.get("name")))
				.andExpect(MockMvcResultMatchers.jsonPath(PRICE_JSON).value(productRequestBodyToUpdate.get("price")))
				.andExpect(MockMvcResultMatchers.jsonPath(PIC_JSON).value(productRequestBodyToUpdate.get("picture")))
				.andExpect(MockMvcResultMatchers.jsonPath(DESC_JSON).value(productRequestBodyToUpdate.get("description")))
				.andExpect(MockMvcResultMatchers.jsonPath(STOCK_JSON).value(productInStockFalse.get("inStock")))
				.andExpect(MockMvcResultMatchers.jsonPath(PIC_JSON).value(productRequestBodyToUpdate.get("picture")));

		// Remove product
		mockMvc.perform(delete(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		// Get removed product ( Must be 400 error )
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
				.andExpect(status().isBadRequest());

		// Get page of products (size = 3)
		// Create 5 products
		for (int i = 0; i < 5; i++) {
			createProductPostRequest();
		}
		mockMvc.perform(get(PRODUCTS_BASE_URL + "?page=0&size=3").headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(CONTENT_JSON, hasSize(3)));
	}

	private void updateProductRequest(String productId, Map<String, Object> productRequestBodyToUpdate)
			throws Exception {
		mockMvc.perform(patch(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(productRequestBodyToUpdate)))
				.andExpect(status().isNoContent());
	}
}
