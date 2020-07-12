package com.sushishop.service;

import com.sushishop.TestUtil;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.model.Cart;
import com.sushishop.model.Product;
import com.sushishop.model.Recipe;
import com.sushishop.repository.CartRepository;
import com.sushishop.repository.RecipeRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.sushishop.TestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static com.sushishop.TestUtil.generateUUID;

@RunWith(MockitoJUnitRunner.class)
public class RecipeServiceTest {

	@Mock ProductService productService;
	@Mock RecipeRepository recipeRepo;

	@InjectMocks RecipeService recipeService;

	private String USER_ID_TEST;
	private Recipe recipe;
	private RecipeDTORequest recipeDTORequest;
	private List<Product> recipeProducts;

	@Before
	public void setUp() {
		USER_ID_TEST = generateUUID();

		recipeProducts = new ArrayList<>(Arrays.asList(createTestProduct(), createTestProduct(), createTestProduct()));
		List<String> productIds = new ArrayList<>();

		for (Product product : recipeProducts) {
			when(productService.getProduct(product.getId())).thenReturn(product);
			productIds.add(product.getId());
		}
		recipe = createRecipe(recipeProducts);
		recipeDTORequest = createRequestRecipe(productIds);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRecipeInvalidIdTest() {
		recipeService.getRecipe("Invalid id");
	}


	@Test
	public void createRecipeTest() {

		recipeService.createRecipe(recipeDTORequest);

		ArgumentCaptor<Recipe> recipeArgumentCaptor = ArgumentCaptor.forClass(Recipe.class);
		Mockito.verify(recipeRepo).saveAndFlush(recipeArgumentCaptor.capture());

		assertTrue(recipeArgumentCaptor.getValue().getProducts().containsAll(recipeProducts));
		assertEquals(recipeDTORequest.name, recipeArgumentCaptor.getValue().getName());
	}

	@Test
	public void updateRecipeTest() {
		Recipe recipe = createRecipe(recipeProducts);
		recipeDTORequest.id = recipe.getId();
		recipeDTORequest.name = "New recipe name";

		recipeDTORequest.keys = new HashSet<>();
		recipeDTORequest.keys.add("name");
		recipeDTORequest.keys.add("productIds");

		// Adding testProduct to request object
		Product testProduct = createTestProduct();
		recipeDTORequest.productIds.add(testProduct.getId());

		// Removing testProduct form request object
		Product productToRemove = recipeProducts.get(0);
		recipeDTORequest.productIds.remove(productToRemove.getId());

		when(productService.getProduct(testProduct.getId())).thenReturn(testProduct);
		when(recipeRepo.findById(recipeDTORequest.id)).thenReturn(Optional.of(recipe));

		recipeService.updateRecipe(recipeDTORequest);

		ArgumentCaptor<Recipe> recipeArgumentCaptor = ArgumentCaptor.forClass(Recipe.class);
		Mockito.verify(recipeRepo).saveAndFlush(recipeArgumentCaptor.capture());

		Recipe savedRecipe = recipeArgumentCaptor.getValue();
		assertEquals(recipeDTORequest.name, savedRecipe.getName());
		assertTrue(recipeDTORequest.productIds
				.containsAll(savedRecipe.getProducts().stream().map(Product::getId).collect(Collectors.toList())));
		assertTrue(savedRecipe.getProducts().stream().noneMatch(p -> p.equals(productToRemove)));
		assertTrue(savedRecipe.getProducts().contains(testProduct));
	}

	@Test
	public void removeRecipeTest() {
		recipeService.deleteRecipe(recipe.getId());
		Mockito.verify(recipeRepo).deleteById(recipe.getId());
	}
}