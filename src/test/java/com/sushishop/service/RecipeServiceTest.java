//package com.sushishop.service;
//
//import com.sushishop.TestUtil;
//import com.sushishop.dto.RecipeDTORequest;
//import com.sushishop.model.Recipe;
//import com.sushishop.repository.RecipeRepository;
//import org.junit.Before;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.Arrays;
//
//import static com.sushishop.TestUtil.createProduct;
//import static com.sushishop.TestUtil.generateUUID;
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//class RecipeServiceTest {
//
//	@Mock ProductService productService;
//	@Mock RecipeRepository recipeRepo;
//
//	@InjectMocks private RecipeService recipeService;
//
//	private static RecipeDTORequest recipeRequest;
//
//
//	@Before
//	public void setUp() {
//		recipeRequest = TestUtil.
//				createRequestRecipe(Arrays.asList(createProduct().id,
//						createProduct().id, createProduct().id, createProduct().id));
//		recipeRequest.id = generateUUID();
//
//		Recipe recipe = new Recipe();
//		recipe.setId(recipeRequest.id);
//		recipe.setName(recipeRequest.name);
//	}
//
//	@Test
//	void updateRecipe() {
////		createRecipeDTORequest();
////		when(recipeService.getRecipe(recipeRequest.id)).thenReturn(recipeRequest);
//		recipeService.updateRecipe(recipeRequest);
//
//	}
//}