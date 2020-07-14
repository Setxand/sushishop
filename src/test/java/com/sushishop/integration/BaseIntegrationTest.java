package com.sushishop.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.dto.RecipeDTOResponse;
import com.sushishop.dto.UserDTO;
import com.sushishop.security.JwtTokenUtil;
import com.sushishop.security.dto.JwtResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sushishop.TestUtil.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseIntegrationTest {

	protected static final String ID_JSON = "$.id";
	protected static final String NAME_JSON = "$.name";
	protected static final String CONTENT_JSON = "$.content";

	protected static final String PRODUCTS_BASE_URL = "/v1/products";
	protected static final String RECIPES_BASE_URL = "/v1/recipes";
	protected static final String USERS_BASE_URL = "/v1/users";
	protected static final String ORDERS_BASE_URL = USERS_BASE_URL + "/orders";
	protected static final String CARTS_BASE_URL = USERS_BASE_URL + "/{userId}/carts";

	private static final Map<Class<? extends BaseIntegrationTest>, String> baseUrlMap = new HashMap<>();

	static {
		baseUrlMap.put(ProductIntegrationTest.class, PRODUCTS_BASE_URL);
		baseUrlMap.put(RecipeIntegrationTest.class, RECIPES_BASE_URL);
		baseUrlMap.put(AuthIntegrationTest.class, USERS_BASE_URL);
	}

	@Autowired protected MockMvc mockMvc;
	@Autowired protected ObjectMapper objectMapper;
	@Autowired private JwtTokenUtil jwtTokenUtil;

	protected String accessToken = "";

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
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody));
	}

	protected MockHttpServletRequestBuilder postRequestWithUrl(String url, Object requestBody)
			throws JsonProcessingException {
		return post(url)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody));
	}

	protected RecipeDTOResponse createRecipeWithFiveProducts() throws Exception {
		// Create products for the recipe

		List<ProductDTO> productDTOS = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			productDTOS.add(createProductPostRequest());
		}
		RecipeDTORequest recipeRequest = createRequestRecipe(productDTOS.stream().map(p -> p.id)
				.collect(Collectors.toList()));

		// Create recipe
		String recipeJsonResponse = mockMvc.perform(postRequestWithUrl(RECIPES_BASE_URL, recipeRequest)
				.headers(authHeader(accessToken)))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON).value(recipeRequest.name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.products", hasSize(5)))
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(recipeJsonResponse, RecipeDTOResponse.class);
	}

	protected JwtResponse signUpRequest() throws Exception {
		UserDTO newUserInput = createUserDTO();
		return objectMapper.readValue(mockMvc.perform(postRequestWithUrl("/signup", newUserInput))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNotEmpty())
				.andReturn().getResponse().getContentAsString(), JwtResponse.class);
	}

	protected HttpHeaders authHeader(String token) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(token);
		return httpHeaders;
	}

}
