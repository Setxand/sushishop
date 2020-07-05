package com.sushishop.integration;

import com.sushishop.dto.ProductDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

	@Test
//	@Sql("clean.sql")
	public void productIntegrationTest() throws Exception {


		// Create new Product
		ProductDTO productResponse = createProductPostRequest();
		String productId = productResponse.id;

		// Check scale of price
		Assert.assertEquals(productResponse.price.scale(), 2);

		// Get new product
		mockMvc.perform(get("/v1/products/{productId}", productId))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(productResponse.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(productResponse.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.price").value(productResponse.price))
				.andExpect(MockMvcResultMatchers.jsonPath("$.picture").value(productResponse.picture));

		// Update product
		ProductDTO productRequestBodyToUpdate = new ProductDTO();
		productRequestBodyToUpdate.price = new BigDecimal("32.99");
		productRequestBodyToUpdate.name = "Product-test-name New product name";
		productRequestBodyToUpdate.picture = "New product picture";

		mockMvc.perform(patch("/v1/products/{productId}", productId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(productRequestBodyToUpdate)))
				.andExpect(status().isNoContent());

		// Get updated product
		mockMvc.perform(get("/v1/products/{productId}", productId))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(productResponse.id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(productRequestBodyToUpdate.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.price").value(productRequestBodyToUpdate.price))
				.andExpect(MockMvcResultMatchers.jsonPath("$.picture").value(productRequestBodyToUpdate.picture));

		// Remove product
		mockMvc.perform(delete("/v1/products/{productId}", productId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

		// Get removed product ( Must be 400 error )
		mockMvc.perform(get("/v1/products/{productId}", productId))
				.andExpect(status().isBadRequest());

		// Get page of products (size = 3)
		mockMvc.perform(get(PRODUCTS_BASE_URL + "?page=0&size=3"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(3)));
	}
}
