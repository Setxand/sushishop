DELETE FROM cart_products WHERE cart_id IN (SELECT id FROM cart WHERE user_id IN (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%"));
DELETE FROM cart_amounts WHERE cart_id IN (SELECT id FROM cart WHERE user_id IN (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%"));

UPDATE user SET user.cart_id=null WHERE id IN (SELECT u.id FROM (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%") AS u);
DELETE FROM cart WHERE cart.user_id IN (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%");

DELETE FROM recipe_products WHERE recipe_id IN (SELECT id FROM recipe WHERE recipe.name LIKE "Recipe-test-name%");

DELETE FROM order_model_products WHERE order_model_id IN (SELECT id FROM order_model WHERE user_id IN (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%"));
DELETE FROM order_model_product_amounts WHERE order_model_id IN (SELECT id FROM order_model WHERE user_id IN (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%"));
DELETE FROM order_model WHERE id IN (SELECT o.id FROM (SELECT id FROM order_model WHERE user_id IN (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%")) AS o);

DELETE FROM product WHERE product.name LIKE "Product-test-name%";

DELETE FROM recipe WHERE recipe.name LIKE "Recipe-test-name%";

UPDATE user SET user.address_id=null WHERE id IN (SELECT u.id FROM (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%") AS u);

DELETE FROM address WHERE id IN (SELECT id FROM user WHERE user.name LIKE "John Doe-test-name%");

DELETE FROM user WHERE user.name LIKE "John Doe-test-name%";
