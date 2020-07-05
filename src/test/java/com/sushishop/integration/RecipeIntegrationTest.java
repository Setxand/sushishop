package com.sushishop.integration;

import com.sushishop.dto.ProductDTO;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.dto.RecipeDTOResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sushishop.TestUtil.createRequestRecipe;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RecipeIntegrationTest extends BaseIntegrationTest {


	private static final int DEFAULT_PRODUCTS_SIZE = 5;
	private static final String PRODUCTS_JSON_PATH = "$.products";
	private static final String URI_WITH_ID_VAR = RECIPES_BASE_URL + "/{recipeId}";

	@Test
	@Sql("classpath:clean.sql")
	public void recipeIntegrationTest() throws Exception {

		// in case of DEFAULT_PRODUCTS_SIZE has changed
		Assert.assertTrue(DEFAULT_PRODUCTS_SIZE >= 3);


		// Create recipe
		RecipeDTOResponse recipeWithFiveProducts = createRecipeWithFiveProducts();
		String recipeId = recipeWithFiveProducts.id;

		// Get created recipe
		String getRecipeResponse = mockMvc.perform(get(RECIPES_BASE_URL + "/{recipeId}", recipeId))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON_PATH).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON_PATH).value(recipeWithFiveProducts.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON_PATH, hasSize(DEFAULT_PRODUCTS_SIZE)))
				.andReturn().getResponse().getContentAsString();

		// Update created recipe (minus one product)
		RecipeDTOResponse recipe = objectMapper.readValue(getRecipeResponse, RecipeDTOResponse.class);

		RecipeDTORequest recipeDTORequest = new RecipeDTORequest();
		recipeDTORequest.name = recipe.name;
		recipeDTORequest.productIds = recipe.products.stream().map(p -> p.id).collect(Collectors.toList());

		recipeDTORequest.name = "Recipe-test-name New recipe name";
		recipeDTORequest.productIds.remove(0);

		mockMvc.perform(patch(URI_WITH_ID_VAR, recipe.id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(recipeDTORequest)))
				.andExpect(status().isNoContent());


		// Get updated recipe
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON_PATH).value(recipeId))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON_PATH).value(recipeDTORequest.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON_PATH, hasSize(DEFAULT_PRODUCTS_SIZE - 1)));


		// Update created recipe (plus two products)
		recipeDTORequest.productIds.add(createProductPostRequest().id);
		recipeDTORequest.productIds.add(createProductPostRequest().id);

		mockMvc.perform(patch(URI_WITH_ID_VAR, recipe.id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(recipeDTORequest)))
				.andExpect(status().isNoContent());


		// Get updated recipe with two products
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON_PATH).value(recipeId))
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON_PATH).value(recipeDTORequest.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON_PATH, hasSize(DEFAULT_PRODUCTS_SIZE + 1)));

		// Remove recipe
		removeRecipeRequest(recipeId);

		// Get removed recipe ( Must be 400 error )
		mockMvc.perform(get(URI_WITH_ID_VAR, recipeId))
				.andExpect(status().isBadRequest());


		// Create 5 recipes and get page from 3 elements

		List<RecipeDTOResponse> recipeResponses = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			recipeResponses.add(createRecipeWithFiveProducts());
		}

		mockMvc.perform(get(RECIPES_BASE_URL + "?page=0&size=3"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(CONTENT_JSON_PATH, hasSize(3)));

		mockMvc.perform(get(RECIPES_BASE_URL + "?page=1&size=2"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(2)));
	}

	private void removeRecipeRequest(String recipeId) throws Exception {
		mockMvc.perform(delete(URI_WITH_ID_VAR, recipeId)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	private RecipeDTOResponse createRecipeWithFiveProducts() throws Exception {
		// Create products for the recipe

		List<ProductDTO> productDTOS = new ArrayList<>();
		for (int i = 0; i < DEFAULT_PRODUCTS_SIZE; i++) {
			productDTOS.add(createProductPostRequest());
		}
		RecipeDTORequest recipeRequest = createRequestRecipe(productDTOS.stream().map(p -> p.id)
				.collect(Collectors.toList()));


		// Create recipe
		String recipeJsonResponse = mockMvc.perform(postRequest(recipeRequest))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON_PATH).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON_PATH).value(recipeRequest.name))
				.andExpect(MockMvcResultMatchers.jsonPath(PRODUCTS_JSON_PATH, hasSize(DEFAULT_PRODUCTS_SIZE)))
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(recipeJsonResponse, RecipeDTOResponse.class);
	}
}
