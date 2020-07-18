delete from cart_products where cart_id in (select id from cart where user_id in (select id from user where user.name like "John Doe-test-name%"));
delete from cart_amounts where cart_id in (select id from cart where user_id in (select id from user where user.name like "John Doe-test-name%"));

update user set user.cart_id=null where id in (select u.id from (select id from user where user.name like "John Doe-test-name%") as u);
delete from cart where cart.user_id in (select id from user where user.name like "John Doe-test-name%");

delete from recipe_products where recipe_id in (select id from recipe where recipe.name like "Recipe-test-name%");

delete from order_model_products where order_model_id in (select id from order_model where user_id in (select id from user where user.name like "John Doe-test-name%"));
delete from order_model_product_amounts where order_model_id in (select id from order_model where user_id in (select id from user where user.name like "John Doe-test-name%"));
delete from order_model where user_id in (select id from user where user.name like "John Doe-test-name%");


delete from product where product.name like "Product-test-name%";
delete from recipe where recipe.name like "Recipe-test-name%";

--delete from cart_products where cart_id in (select id from cart where user_id in (select id from user where user.name like "John Doe-test-name%"));
--delete from cart where cart.user_id in (select id from user where user.name like "John Doe-test-name%");

update user set user.address_id=null where id in (select u.id from (select id from user where user.name like "John Doe-test-name%") as u);
delete from address where id in (select id from user where user.name like "John Doe-test-name%");
delete from user where user.name like "John Doe-test-name%";
