package com.sushishop.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.dto.ProductDTO;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.sushishop.TestUtil.createProduct;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseIntegrationTest {


	protected static final String PRODUCTS_BASE_URL = "/v1/products";
	protected static final String RECIPES_BASE_URL = "/v1/recipes";
	private static final Map<Class<? extends BaseIntegrationTest>, String> baseUrlMap = new HashMap<>();

	static {
		baseUrlMap.put(ProductIntegrationTest.class, PRODUCTS_BASE_URL);
		baseUrlMap.put(RecipeIntegrationTest.class, RECIPES_BASE_URL);
	}

	@Autowired protected MockMvc mockMvc;
	@Autowired protected ObjectMapper objectMapper;

	protected ProductDTO createProductPostRequest() throws Exception {
		ProductDTO productRequestBody = createProduct();
		MockHttpServletRequestBuilder postRequest = postRequestWithUrl(PRODUCTS_BASE_URL, productRequestBody);

		return objectMapper.readValue(mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString(), ProductDTO.class);
	}

	protected MockHttpServletRequestBuilder postRequest(Object requestBody) throws JsonProcessingException {
		return post(baseUrlMap.get(this.getClass()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody));
	}

	protected MockHttpServletRequestBuilder postRequestWithUrl(String url, Object requestBody)
			throws JsonProcessingException {
		return post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody));
	}

}
