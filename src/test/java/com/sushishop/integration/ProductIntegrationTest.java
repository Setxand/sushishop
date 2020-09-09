package com.sushishop.integration;

import com.sushishop.TestUtil;
import com.sushishop.dto.ProductDTO;
import com.sushishop.model.Product;
import com.sushishop.model.User;
import com.sushishop.security.dto.JwtResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sushishop.TestUtil.createProductDTO;
import static org.junit.Assert.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-sources/snippets")
public class ProductIntegrationTest extends BaseIntegrationTest {

	private static final String PRICE_JSON = "$.price";
	private static final String DESC_JSON = "$.description";
	private static final String WEIGHT_JSON = "$.weight";

	private static final String URI_WITH_ID_VAR = PRODUCTS_BASE_URL + "/{productId}";
	private static final String STOCK_JSON = "$.inStock";

	private static final String GET_PRODUCT_DOC = "get-product";
	private static final String UPDATE_PRODUCT_DOC = "update-product";
	private static final String CHANGE_IN_STOCK_DOC = "change-stock-level";
	private static final String REMOVE_PRODUCT_DOC = "remove-product";
	private static final String CREATE_PRODUCT_DOC = "create-product";
	private static final String PRODUCT_TYPE_JSON = "$.productType";
	private static final String HOLD_CONDITIONS_JSON = "$.holdConditions";
	private static final String PACK_NUMBER_JSON = "$.packNumber";
	private static final String PACKING_JSON = "$.packing";
	private static final String TEST_EMAIL = "test@test12.com";
	private static final String TEST_PHONE = "+3809711123";
	private static final String PRICE_KEY = "price";
	private static final String NAME_KEY = "name";
	private static final String SUB_NAME_KEY = "subName";
	private static final String PICTURE_KEY = "picture";
	private static final String WEIGHT_KEY = "weight";
	private static final String DESCRIPTION_KEY = "description";
	private static final String HOLD_CONDITIONS_KEY = "holdConditions";
	private static final String PACK_NUMBER_KEY = "packNumber";
	private static final String PACKING_KEY = "packing";
	private static final String IN_STOCK_KEY = "inStock";
	private static final int PRODUCTS_SIZE = 5;
	private static final String GET_PRODUCTS_BY_TYPE_URL = "" +
			"/v1/products?type=" + Product.ProductType.MEAT + "&size=10&page=0";
	private static final String GET_PRODUCTS_BY_TYPE_DOC = "get-products-by-type";


	@Test
	@Sql("classpath:clean.sql")
	public void productIntegrationTest() throws Exception {
		// Create User to authenticate
		JwtResponse userToken = signUpRequest();
		accessToken = userToken.getAccessToken();

		// Create new Product not an admin
		ProductDTO productRequestBody = createProductDTO();
		productRequestBody.id = null;
		MockHttpServletRequestBuilder postRequest = postRequestWithUrl(PRODUCTS_BASE_URL, productRequestBody);

		mockMvc.perform(postRequest)
				.andExpect(status().is4xxClientError());

		// Create new Product
		JwtResponse adminJwtResponse = signUpRequest(User.UserRole.ROLE_ADMIN, TEST_EMAIL, TEST_PHONE);
		accessToken = adminJwtResponse.getAccessToken();

		postRequest = postRequestWithUrl(PRODUCTS_BASE_URL, productRequestBody);
		ProductDTO productResponse = objectMapper.readValue(mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andDo(document(CREATE_PRODUCT_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), ProductDTO.class);

		Assert.assertTrue(StringUtils.isNotBlank(productResponse.description));
		Assert.assertTrue(productResponse.weight > 0.0);
		String productId = productResponse.id;

		// Check scale of price
		assertEquals(productResponse.price.scale(), 2);

		accessToken = userToken.getAccessToken();
		// Get new product
		ProductDTO product = objectMapper.readValue(mockMvc.perform(get(URI_WITH_ID_VAR, productId)
				.headers(authHeader(this.accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath(ID_JSON).value(productResponse.id))
				.andExpect(jsonPath(NAME_JSON).value(productResponse.name))
				.andExpect(jsonPath(SUB_NAME_JSON).value(productResponse.subName))
				.andExpect(jsonPath(DESC_JSON).value(productResponse.description))
				.andExpect(jsonPath(WEIGHT_JSON).value(productResponse.weight))
				.andExpect(jsonPath(PICTURE_JSON).value(productResponse.picture))
				.andExpect(jsonPath(PRODUCT_TYPE_JSON).value(Product.ProductType.COMMON.name()))
				.andExpect(jsonPath(HOLD_CONDITIONS_JSON).value(productResponse.holdConditions))
				.andExpect(jsonPath(PACK_NUMBER_JSON).value(productResponse.packNumber))
				.andExpect(jsonPath(PACKING_JSON).value(productResponse.packing))
				.andExpect(jsonPath(CREATED_JSON).isNotEmpty())
				.andDo(document(GET_PRODUCT_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), ProductDTO.class);
		assertTrue(product.inStock);
		assertEquals(0, product.price.compareTo(productResponse.price));

		// Update product
		Map<String, Object> productRequestBodyToUpdate = new HashMap<>();
		productRequestBodyToUpdate.put(PRICE_KEY, new BigDecimal("32.99"));

		productRequestBodyToUpdate.put(NAME_KEY, "Product-test-name New product name");
		productRequestBodyToUpdate.put(SUB_NAME_KEY, "Product-test-sub_name New product sub name");
		productRequestBodyToUpdate.put(PICTURE_KEY, "New product picture");
		productRequestBodyToUpdate.put(WEIGHT_KEY, new BigDecimal("1.2").setScale(2, BigDecimal.ROUND_HALF_UP)
				.doubleValue());
		productRequestBodyToUpdate.put(DESCRIPTION_KEY, TestUtil.generateUUID());
		productRequestBodyToUpdate.put(HOLD_CONDITIONS_KEY, "10 days with -5 to 0 degrees");
		productRequestBodyToUpdate.put(PACK_NUMBER_KEY, "7");
		productRequestBodyToUpdate.put(PACKING_KEY, "metal box");

		mockMvc.perform(patch(URI_WITH_ID_VAR, productId).headers(authHeader(this.accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(productRequestBodyToUpdate)))
				.andExpect(status().isNoContent())
				.andDo(document(UPDATE_PRODUCT_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Get updated product
		product = getProduct(productId, productRequestBodyToUpdate);
		assertTrue(product.inStock);
		BigDecimal price = (BigDecimal) productRequestBodyToUpdate.get(PRICE_KEY);
		assertEquals(0, price.compareTo(product.price));
		assertEquals(productRequestBodyToUpdate.get(SUB_NAME_KEY), product.subName);
		assertEquals(productRequestBodyToUpdate.get(HOLD_CONDITIONS_KEY), product.holdConditions);
		assertEquals(productRequestBodyToUpdate.get(PACK_NUMBER_KEY), product.packNumber);
		assertEquals(productRequestBodyToUpdate.get(PACKING_KEY), product.packing);

		// Change product's 'inStock' to false
		Map<String, Object> productInStockFalse = new HashMap<>();
		productInStockFalse.put(IN_STOCK_KEY, false);
		mockMvc.perform(patch(URI_WITH_ID_VAR, productId).headers(authHeader(this.accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(productInStockFalse)))
				.andExpect(status().isNoContent())
				.andDo(document(CHANGE_IN_STOCK_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Get product that's not in stock
		product = getProduct(productId, productRequestBodyToUpdate);
		assertFalse(product.inStock);
		price = (BigDecimal) productRequestBodyToUpdate.get("price");
		assertEquals(0, price.compareTo(product.price));

		// Remove product
		mockMvc.perform(delete(URI_WITH_ID_VAR, productId).headers(authHeader(this.accessToken))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent())
				.andDo(document(REMOVE_PRODUCT_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Get removed product ( Must be 400 error )
		mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(this.accessToken)))
				.andExpect(status().isBadRequest());

		// Get page of products (size = 3)
		// Create 5 products
		accessToken = adminJwtResponse.getAccessToken();
		for (int i = 0; i < PRODUCTS_SIZE; i++) {
			createProductPostRequest();
		}

		List<ProductDTO> productsList = getProducts(Product.ProductType.COMMON);
		assertTrue(productsList.stream().allMatch(p -> p.productType == Product.ProductType.COMMON));


		// Create products with different types
		ProductDTO productMeat = createProductDTO();
		productMeat.productType = Product.ProductType.MEAT;

		ProductDTO productSendRequest = createProductSendRequest(productMeat);

		assertEquals(Product.ProductType.MEAT, productSendRequest.productType);

		// Get products by type
		mockMvc.perform(get(GET_PRODUCTS_BY_TYPE_URL))
				.andExpect(status().isOk())
				.andDo(document(GET_PRODUCTS_BY_TYPE_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

	}

	private ProductDTO getProduct(String productId, Map<String, Object> requestBody) throws Exception {

		return objectMapper.readValue(mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
						.andExpect(status().isOk())
						.andExpect(jsonPath(ID_JSON).value(productId))
						.andExpect(jsonPath(NAME_JSON).value(requestBody.get(NAME_KEY)))
						.andExpect(jsonPath(SUB_NAME_JSON).value(requestBody.get(SUB_NAME_KEY)))
						.andExpect(jsonPath(PICTURE_JSON).value(requestBody.get(PICTURE_KEY)))
						.andExpect(jsonPath(DESC_JSON).value(requestBody.get(DESCRIPTION_KEY)))
						.andExpect(jsonPath(PICTURE_JSON).value(requestBody.get(PICTURE_KEY)))
						.andExpect(jsonPath(HOLD_CONDITIONS_JSON).value(requestBody.get(HOLD_CONDITIONS_KEY)))
						.andExpect(jsonPath(PACK_NUMBER_JSON).value(requestBody.get(PACK_NUMBER_KEY)))
						.andExpect(jsonPath(PACKING_JSON).value(requestBody.get(PACKING_KEY)))
						.andReturn().getResponse().getContentAsString(),
				ProductDTO.class);
	}
}
