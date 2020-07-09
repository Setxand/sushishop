delete from cart_products where cart_id in (select id from cart where user_id in (select id from user where user.name like "John Doe-test-name%"));
delete from cart_amounts where cart_id in (select id from cart where user_id in (select id from user where user.name like "John Doe-test-name%"));

delete from cart where cart.user_id in (select id from user where user.name like "John Doe-test-name%");

delete from recipe_products where recipe_id in (select id from recipe where recipe.name like "Recipe-test-name%");
delete from product where product.name like "Product-test-name%";
delete from recipe where recipe.name like "Recipe-test-name%";

delete from cart_products where cart_id in (select id from cart where user_id in (select id from user where user.name like "John Doe-test-name%"));
delete from cart where cart.user_id in (select id from user where user.name like "John Doe-test-name%");
delete from user where user.name like "John Doe-test-name%";
