package com.sushishop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.dto.RecipeDTORequest;
import com.sushishop.dto.RecipeDTOResponse;
import com.sushishop.service.RecipeService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RecipeController {

	@Autowired RecipeService recipeService;
	@Autowired ObjectMapper objectMapper;

	@PostMapping("/v1/recipes")
	@ResponseStatus(HttpStatus.CREATED)
	public RecipeDTOResponse createRecipe(@RequestBody RecipeDTORequest dto) {
		return DtoUtil.recipe(recipeService.createRecipe(dto));
	}

	@GetMapping("/v1/recipes")
	public Page<RecipeDTOResponse> getRecipes(@PageableDefault(sort = "created") Pageable pageable) {
		return recipeService.getRecipes(pageable).map(DtoUtil::recipe);
	}

	@GetMapping("/v1/recipes/{recipeId}")
	public RecipeDTOResponse getRecipe(@PathVariable String recipeId) {
		return DtoUtil.recipe(recipeService.getRecipe(recipeId));
	}

	@PatchMapping("/v1/recipes/{recipeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateRecipe(@PathVariable String recipeId, @RequestBody Map<String, Object> body) {
		RecipeDTORequest dto = objectMapper.convertValue(body, RecipeDTORequest.class);
		dto.keys = body.keySet();
		dto.id = recipeId;
		recipeService.updateRecipe(dto);
	}

	@DeleteMapping("/v1/recipes/{recipeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRecipe(@PathVariable String recipeId) {
		recipeService.deleteRecipe(recipeId);
	}
}
