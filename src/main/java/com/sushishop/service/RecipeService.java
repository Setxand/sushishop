package com.sushishop.service;

import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.model.Product;
import com.sushishop.model.Recipe;
import com.sushishop.repository.RecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

	private static final String INVALID_RECIPE = "Invalid Recipe ID";
	private final ProductService productService;
	private final RecipeRepository recipeRepo;

	public RecipeService(ProductService productService, RecipeRepository recipeRepo) {
		this.productService = productService;
		this.recipeRepo = recipeRepo;
	}

	public Recipe createRecipe(RecipeDTORequest dto) {
		Recipe recipe = new Recipe();
		recipe.setName(dto.name);

		List<Product> products = dto.productIds.stream().map(productService::getProduct).collect(Collectors.toList());
		recipe.setProducts(products);
		return recipeRepo.saveAndFlush(recipe);
	}

	public Recipe getRecipe(String recipeId) {
		return recipeRepo.findById(recipeId).orElseThrow(() -> new IllegalArgumentException(INVALID_RECIPE));
	}

	public void updateRecipe(RecipeDTORequest dto) {
		Recipe recipe = getRecipe(dto.id);

		if (dto.keys.contains("name")) {
			recipe.setName(dto.name);
		}

		if (dto.keys.contains("productIds")) {
			List<Product> products = recipe.getProducts();
			List<String> productIds = dto.productIds;

			products.removeAll(products.stream()
					.filter(p -> productIds.stream()
							.noneMatch(id -> p.getId().equals(id))).collect(Collectors.toList()));

			products.addAll(productIds.stream()
					.filter(id -> products.stream()
							.noneMatch(p -> p.getId().equals(id)))
					.map(productService::getProduct).collect(Collectors.toList()));

		}
		recipeRepo.saveAndFlush(recipe);
	}

	public void deleteRecipe(String recipeId) {
		recipeRepo.deleteById(recipeId);
	}

	public Page<Recipe> getRecipes(Pageable pageable) {
		return recipeRepo.findAll(pageable);
	}
}
