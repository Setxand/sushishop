package com.sushishop.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTOResponse;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-sources/snippets")
public class RecipeIntegrationTest extends BaseIntegrationTest {


	private static final int DEFAULT_PRODUCTS_SIZE = 5;
	private static final String PRODUCTS_JSON_PATH = "$.products";
	private static final String URI_WITH_ID_VAR = RECIPES_BASE_URL + "/{recipeId}";

	@Test
	@Sql("classpath:clean.sql")
	public void recipeIntegrationTest() throws Exception {

		// in case of DEFAULT_PRODUCTS_SIZE has changed
		Assert.assertTrue(DEFAULT_PRODUCTS_SIZE >= 3);

		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		// Create recipe
		RecipeDTOResponse recipeWithFiveProducts = createRecipeWithFiveProducts();
		String recipeId = recipeWithFiveProducts.id;

		// Get created recipe
		String getRecipeResponse = mockMvc.perform(get(URI_WITH_ID_VAR, recipeId)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON).value(recipeWithFiveProducts.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON_PATH, hasSize(DEFAULT_PRODUCTS_SIZE)))
				.andDo(document("get-recipe", preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString();

		// Update created recipe (minus one product)
		Map<String, Object> recipe = objectMapper.readValue(getRecipeResponse, new TypeReference<Map<String, Object>>(){});


		HashMap<String, Object> updateRecipeBody = new HashMap<>();
		updateRecipeBody.put("name", "Recipe-test-name New recipe name");
		List<ProductDTO> products = objectMapper
				.convertValue(recipe.get("products"), new TypeReference<List<ProductDTO>>() {});
		products.remove(0);

		updateRecipeBody.put("productIds", products.stream().map(p -> p.id).collect(Collectors.toList()));
		updateRecipeBody.put("name", recipe.get("name"));

		mockMvc.perform(patch(URI_WITH_ID_VAR, recipe.get("id"))
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRecipeBody)))
					.andExpect(status().isNoContent())
				.andDo(document("update-recipe", preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));


		// Get updated recipe
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId).headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON).value(recipeId))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON).value(recipe.get("name")))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON_PATH, hasSize(DEFAULT_PRODUCTS_SIZE - 1)));


		// Update created recipe (plus two products)
		List<String> productIds = (List<String>) updateRecipeBody.get("productIds");
		productIds.add(createProductPostRequest().id);
		productIds.add(createProductPostRequest().id);

		mockMvc.perform(patch(URI_WITH_ID_VAR, recipe.get("id"))
					.headers(authHeader(accessToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(updateRecipeBody)))
				.andExpect(status().isNoContent());


		// Get updated recipe with two products
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId)
					.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON).value(recipeId))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON).value(updateRecipeBody.get("name")))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON_PATH, hasSize(DEFAULT_PRODUCTS_SIZE + 1)));

		// Remove recipe
		mockMvc.perform(delete(URI_WITH_ID_VAR, recipeId)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent())
				.andDo(document("remove-recipe", preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));

		// Get removed recipe ( Must be 400 error )
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId).headers(authHeader(accessToken)))
				.andExpect(status().isBadRequest());


		// Create 5 recipes and get page from 3 elements

		List<RecipeDTOResponse> recipeResponses = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			recipeResponses.add(createRecipeWithFiveProducts());
		}

		mockMvc.perform(get(RECIPES_BASE_URL + "?page=0&size=3")
					.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(CONTENT_JSON, hasSize(3)))
				.andDo(document("get-recipes-list", preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));

		mockMvc.perform(get(RECIPES_BASE_URL + "?page=1&size=2")
					.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(CONTENT_JSON, hasSize(2)));
	}
}
