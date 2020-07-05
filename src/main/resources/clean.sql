delete from recipe_products where recipe_id in (select id from recipe where recipe.name like "Recipe-test-name%");
delete from product where product.name like "Product-test-name%";
delete from recipe where recipe.name like "Recipe-test-name%";