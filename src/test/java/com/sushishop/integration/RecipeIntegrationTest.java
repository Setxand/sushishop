package com.sushishop.integration;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sushishop.TestUtil.createProductDTO;
import static com.sushishop.model.Product.ProductType.RECIPE_COMPONENT;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-sources/snippets")
public class RecipeIntegrationTest extends BaseIntegrationTest {


	private static final int DEFAULT_PRODUCTS_SIZE = 5;
	private static final String URI_WITH_ID_VAR = RECIPES_BASE_URL + "/{recipeId}";
	private static final int RECIPE_COMPONENT_SIZE = 4;
	private static final int WHOLE_PRODUCTS_SIZE = DEFAULT_PRODUCTS_SIZE + RECIPE_COMPONENT_SIZE;
	private static final String TEST_EMAIL = "test@test12.com";
	private static final String TEST_PHONE = "+3809711123";
	private static final String NAME_KEY = "name";
	private static final String SUB_NAME_KEY = "subName";
	private static final String PRODUCTS_KEY = "products";
	private static final String PRODUCT_IDS_KEY = "productIds";
	private static final String UPDATE_RECIPE_DOC = "update-recipe";
	private static final String GET_RECIPE_DOC = "get-recipe";
	private static final String ID_KEY = "id";
	private static final String REMOVE_RECIPE_DOC = "remove-recipe";
	private static final String GET_RECIPE_LIST_DOC = "get-recipes-list";

	@Test
	@Sql("classpath:clean.sql")
	public void recipeIntegrationTest() throws Exception {

		// in case of DEFAULT_PRODUCTS_SIZE has changed
		Assert.assertTrue(DEFAULT_PRODUCTS_SIZE >= 3);

		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		// Create recipe
		JwtResponse adminJwtResponce = signUpRequest(User.UserRole.ROLE_ADMIN, TEST_EMAIL, TEST_PHONE);
		accessToken = adminJwtResponce.getAccessToken();
		RecipeDTOResponse recipeWithFiveProducts = createRecipeWithFiveProducts();
		String recipeId = recipeWithFiveProducts.id;


		// Get created recipe
		accessToken = jwtResponse.getAccessToken();
		String getRecipeResponse = mockMvc.perform(get(URI_WITH_ID_VAR, recipeId)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(ID_JSON).isNotEmpty())
				.andExpect(jsonPath(NAME_JSON).value(recipeWithFiveProducts.name))
				.andExpect(jsonPath(SUB_NAME_JSON).value(recipeWithFiveProducts.subName))
				.andExpect(jsonPath(PICTURE_JSON).value(recipeWithFiveProducts.picture))
				.andExpect(jsonPath(CREATED_JSON).isNotEmpty())
				.andExpect(jsonPath(PRODUCTS_JSON, hasSize(DEFAULT_PRODUCTS_SIZE)))
				.andReturn().getResponse().getContentAsString();

		// Update created recipe (minus one product)
		accessToken = adminJwtResponce.getAccessToken();
		Map<String, Object> recipe = objectMapper.readValue(getRecipeResponse, new TypeReference<Map<String, Object>>() {
		});


		HashMap<String, Object> updateRecipeBody = new HashMap<>();
		updateRecipeBody.put(NAME_KEY, "Recipe-test-name New recipe name");
		updateRecipeBody.put(SUB_NAME_KEY, "Recipe-test-sub-name New recipe sub name");
		List<ProductDTO> products = objectMapper
				.convertValue(recipe.get(PRODUCTS_KEY), new TypeReference<List<ProductDTO>>() {
				});
		products.remove(0);

		for (int i = 0; i < RECIPE_COMPONENT_SIZE; i++) {
			products.add(createProductSendRequest(createProductDTO(RECIPE_COMPONENT)));
		}

		updateRecipeBody.put(PRODUCT_IDS_KEY, products.stream().map(p -> p.id).collect(Collectors.toList()));

		mockMvc.perform(patch(URI_WITH_ID_VAR, recipe.get("id"))
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRecipeBody)))
				.andExpect(status().isNoContent())
				.andDo(document(UPDATE_RECIPE_DOC, preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));


		// Get updated recipe
		accessToken = jwtResponse.getAccessToken();
		RecipeDTOResponse resp = objectMapper
				.readValue(mockMvc.perform(get(URI_WITH_ID_VAR, recipeId).headers(authHeader(accessToken)))
						.andExpect(status().isOk())
						.andExpect(jsonPath(ID_JSON).value(recipeId))
						.andExpect(jsonPath(NAME_JSON).value(updateRecipeBody.get(NAME_KEY)))
						.andExpect(jsonPath(SUB_NAME_JSON).value(updateRecipeBody.get(SUB_NAME_KEY)))
						.andExpect(jsonPath(PRODUCTS_JSON,
								hasSize(WHOLE_PRODUCTS_SIZE - 1)))
						.andDo(document(GET_RECIPE_DOC, preprocessRequest(prettyPrint()),
								preprocessResponse(prettyPrint())))
						.andReturn().getResponse().getContentAsString(), RecipeDTOResponse.class);


		Assert.assertEquals(RECIPE_COMPONENT_SIZE,
				resp.products.stream().filter(p -> p.productType == RECIPE_COMPONENT).count());


		// Update created recipe (plus two products)
		accessToken = adminJwtResponce.getAccessToken();
		List<String> productIds = (List<String>) updateRecipeBody.get(PRODUCT_IDS_KEY);
		productIds.add(createProductPostRequest().id);
		productIds.add(createProductPostRequest().id);

		mockMvc.perform(patch(URI_WITH_ID_VAR, recipe.get(ID_KEY))
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRecipeBody)))
				.andExpect(status().isNoContent());


		// Get updated recipe with two products
		accessToken = jwtResponse.getAccessToken();
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(ID_JSON).value(recipeId))
				.andExpect(jsonPath(NAME_JSON).value(updateRecipeBody.get(NAME_KEY)))
				.andExpect(jsonPath(SUB_NAME_JSON).value(updateRecipeBody.get(SUB_NAME_KEY)))
				.andExpect(jsonPath(PRODUCTS_JSON, hasSize(WHOLE_PRODUCTS_SIZE + 1)));

		// Remove recipe
		accessToken = adminJwtResponce.getAccessToken();
		mockMvc.perform(delete(URI_WITH_ID_VAR, recipeId)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent())
				.andDo(document(REMOVE_RECIPE_DOC, preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));

		// Get removed recipe ( Must be 400 error )
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId).headers(authHeader(accessToken)))
				.andExpect(status().isBadRequest());


		// Create 5 recipes and get page from 3 elements

		List<RecipeDTOResponse> recipeResponses = new ArrayList<>();
		for (int i = 0; i < DEFAULT_PRODUCTS_SIZE; i++) {
			recipeResponses.add(createRecipeWithFiveProducts());
		}

		int pageSize = 3;
		mockMvc.perform(get(RECIPES_BASE_URL + "?page=0&size=" + pageSize)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(CONTENT_JSON, hasSize(pageSize)))
				.andDo(document(GET_RECIPE_LIST_DOC, preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));

		pageSize = 2;
		mockMvc.perform(get(RECIPES_BASE_URL + "?page=1&size=" + pageSize)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(CONTENT_JSON, hasSize(pageSize)));
	}
}
