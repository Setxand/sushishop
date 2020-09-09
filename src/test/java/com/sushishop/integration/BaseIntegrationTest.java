package com.sushishop.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.dto.*;
import com.sushishop.model.Product;
import com.sushishop.model.User;
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
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseIntegrationTest {

	protected static final String ID_JSON = "$.id";
	protected static final String NAME_JSON = "$.name";
	protected static final String SUB_NAME_JSON = "$.subName";
	protected static final String CONTENT_JSON = "$.content";
	protected static final String CREATED_JSON = "$.created";
	protected static final String PICTURE_JSON = "$.picture";
	protected static final String ACCESS_TOK_JSON = "$.accessToken";
	protected static final String REFRESH_TOK_JSON = "$.refreshToken";
	protected static final String USER_ID_JSON = "$.userId";
	protected static final String PRODUCTS_JSON = "$.products";

	protected static final String PRODUCTS_BASE_URL = "/v1/products";
	protected static final String RECIPES_BASE_URL = "/v1/recipes";
	protected static final String USERS_BASE_URL = "/v1/users";
	protected static final String ORDERS_BASE_URL = USERS_BASE_URL + "/{userId}/orders";
	protected static final String CARTS_BASE_URL = USERS_BASE_URL + "/{userId}/carts";
	protected static final String LOGIN_URL = "/login";
	protected static final String SIGNUP_URL = "/signup";
	private static final String ADDRESSES_URL = "/{userId}/addresses";

	private static final String GET_PRODUCTS_DOC = "get-products-list";
	private static final String CREATE_RECIPE_DOC = "create-recipe";
	private static final String LOGIN_DOC = "login-ok";

	private static final int RECIPE_PRODUCTS_NUM = 5;
	private static final int GET_PRODUCTS_PAGE_SIZE = 3;
	private static final String SIGN_UP_PASSWORD = "1111dfd@";
	private static final String CONTENT_KEY = "content";

	private static final Map<Class<? extends BaseIntegrationTest>, String> baseUrlMap = new HashMap<>();
	private static final String GET_PRODUCTS_PAGE_URL = PRODUCTS_BASE_URL + "?type=%s&page=0&size=" +
			GET_PRODUCTS_PAGE_SIZE;

	static {
		baseUrlMap.put(ProductIntegrationTest.class, PRODUCTS_BASE_URL);
		baseUrlMap.put(RecipeIntegrationTest.class, RECIPES_BASE_URL);
		baseUrlMap.put(AuthIntegrationTest.class, USERS_BASE_URL);
		baseUrlMap.put(OrderIntegrationTest.class, ORDERS_BASE_URL);
	}

	@Autowired protected MockMvc mockMvc;
	@Autowired protected ObjectMapper objectMapper;

	protected String accessToken = "";

	protected ProductDTO createProductPostRequest() throws Exception {
		return createProductSendRequest(createProductDTO());
	}

	protected ProductDTO createProductSendRequest(ProductDTO productRequestBody) throws Exception {

		MockHttpServletRequestBuilder postRequest = postRequestWithUrl(PRODUCTS_BASE_URL, productRequestBody);

		return objectMapper.readValue(mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString(), ProductDTO.class);
	}

	protected List<ProductDTO> getProducts(Product.ProductType type) throws Exception {
		Map<String, Object> map = objectMapper.readValue(mockMvc.perform(
				get(String.format(GET_PRODUCTS_PAGE_URL, type.name()))
						.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(CONTENT_JSON, hasSize(GET_PRODUCTS_PAGE_SIZE)))
				.andDo(document(GET_PRODUCTS_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
		});

		return objectMapper.convertValue(map.get(CONTENT_KEY),
				new TypeReference<List<ProductDTO>>() {
				});
	}

	protected MockHttpServletRequestBuilder postRequest(Object requestBody, Object... uriVars)
			throws JsonProcessingException {
		return post(baseUrlMap.get(this.getClass()), uriVars)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody));
	}

	protected MockHttpServletRequestBuilder patchRequest(Object requestBody, Object... uriVars)
			throws JsonProcessingException {
		return patch(baseUrlMap.get(this.getClass()), uriVars)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody));
	}

	protected MockHttpServletRequestBuilder patchRequestWithUrl(String url, Object requestBody, Object... uriVars)
			throws JsonProcessingException {
		return patch(url, uriVars)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody));
	}

	protected MockHttpServletRequestBuilder postRequestWithUrl(String url, Object requestBody)
			throws JsonProcessingException {

		return post(url)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(convertToCorrectMap(requestBody)));
	}

	protected Object convertToCorrectMap(Object requestBody) {
		Map<String, Object> requestMap = objectMapper
				.convertValue(requestBody, new TypeReference<Map<String, Object>>() {
				});
		return requestMap.entrySet().stream().filter(e -> e.getValue() != null)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	protected void addTestAddressToUser(String userId, AddressDTO address) throws Exception {
		mockMvc.perform(put(USERS_BASE_URL + ADDRESSES_URL, userId)
				.contentType(MediaType.APPLICATION_JSON)
				.headers(authHeader(accessToken))
				.content(objectMapper.writeValueAsString(address)))
				.andExpect(status().isOk());
	}

	protected RecipeDTOResponse createRecipeWithFiveProducts() throws Exception {
		// Create products for the recipe

		List<ProductDTO> productDTOS = new ArrayList<>();
		for (int i = 0; i < RECIPE_PRODUCTS_NUM; i++) {
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
				.andExpect(jsonPath(PICTURE_JSON).value(recipeRequest.picture))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON, hasSize(RECIPE_PRODUCTS_NUM)))
				.andDo(document(CREATE_RECIPE_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(recipeJsonResponse, RecipeDTOResponse.class);
	}

	protected JwtResponse signUpRequest() throws Exception {
		UserDTO newUserInput = createUserDTO();
		newUserInput.password = SIGN_UP_PASSWORD;
		newUserInput.id = null;
		return objectMapper.readValue(mockMvc.perform(postRequestWithUrl(SIGNUP_URL, newUserInput))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath(ACCESS_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(REFRESH_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).isNotEmpty())
				.andReturn().getResponse().getContentAsString(), JwtResponse.class);
	}

	protected JwtResponse signUpRequest(User.UserRole role, String email, String phone) throws Exception {
		UserDTO newUserInput = createUserDTO();
		newUserInput.role = role.name();
		newUserInput.email = email;
		newUserInput.phone = phone;
		newUserInput.password = SIGN_UP_PASSWORD;
		newUserInput.id = null;
		return objectMapper.readValue(mockMvc.perform(postRequestWithUrl(SIGNUP_URL, newUserInput))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath(ACCESS_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(REFRESH_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).isNotEmpty())
				.andReturn().getResponse().getContentAsString(), JwtResponse.class);
	}

	protected CartDTO putInTheCart(ProductDTO product, String userId)
			throws Exception {
		return objectMapper.readValue(mockMvc.perform(put(CARTS_BASE_URL + "/products/{productId}", userId, product.id)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(), CartDTO.class);
	}

	protected CartDTO getCart(String userId) throws Exception {
		return objectMapper.readValue(mockMvc.perform(get(CARTS_BASE_URL, userId)
				.headers(authHeader(accessToken)))
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(userId))
				.andExpect(jsonPath(CREATED_JSON).isNotEmpty())
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), CartDTO.class);
	}

	protected HttpHeaders authHeader(String token) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(token);
		return httpHeaders;
	}

	protected JwtResponse signInRequest(String userId, LoginRequestDTO loginRequest) throws Exception {
		return objectMapper.readValue(
				mockMvc.perform(postRequestWithUrl(LOGIN_URL, loginRequest))
						.andExpect(status().isAccepted())
						.andExpect(MockMvcResultMatchers.jsonPath(ACCESS_TOK_JSON).isNotEmpty())
						.andExpect(MockMvcResultMatchers.jsonPath(REFRESH_TOK_JSON).isNotEmpty())
						.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(userId))
						.andDo(document(LOGIN_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
						.andReturn().getResponse().getContentAsString(),
				JwtResponse.class);
	}

	protected void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

}
