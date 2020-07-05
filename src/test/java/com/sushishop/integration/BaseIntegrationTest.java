package com.sushishop.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.sushishop.TestUtil.createProductDTO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseIntegrationTest {

	protected static final String ID_JSON_PATH = "$.id";
	protected static final String NAME_JSON_PATH = "$.name";
	protected static final String CONTENT_JSON_PATH = "$.content";


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
		ProductDTO productRequestBody = createProductDTO();
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
