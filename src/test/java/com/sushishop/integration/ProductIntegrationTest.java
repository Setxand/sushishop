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
		JwtResponse adminJwtResponse = signUpRequest(User.UserRole.ROLE_ADMIN, "test@test12.com", "+3809711123");
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
		productRequestBodyToUpdate.put("price", new BigDecimal("32.99"));

		productRequestBodyToUpdate.put("name", "Product-test-name New product name");
		productRequestBodyToUpdate.put("subName", "Product-test-sub_name New product sub name");
		productRequestBodyToUpdate.put("picture", "New product picture");
		productRequestBodyToUpdate.put("weight", new BigDecimal("1.2").setScale(2, BigDecimal.ROUND_HALF_UP)
				.doubleValue());
		productRequestBodyToUpdate.put("description", TestUtil.generateUUID());
		productRequestBodyToUpdate.put("holdConditions", "10 days with -5 to 0 degrees");
		productRequestBodyToUpdate.put("packNumber", "7");
		productRequestBodyToUpdate.put("packing", "metal box");

		mockMvc.perform(patch(URI_WITH_ID_VAR, productId).headers(authHeader(this.accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(productRequestBodyToUpdate)))
				.andExpect(status().isNoContent())
				.andDo(document(UPDATE_PRODUCT_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Get updated product
		product = getProduct(productId, productRequestBodyToUpdate);
		assertTrue(product.inStock);
		BigDecimal price = (BigDecimal) productRequestBodyToUpdate.get("price");
		assertEquals(0, price.compareTo(product.price));
		assertEquals(productRequestBodyToUpdate.get("subName"), product.subName);
		assertEquals(productRequestBodyToUpdate.get("holdConditions"), product.holdConditions);
		assertEquals(productRequestBodyToUpdate.get("packNumber"), product.packNumber);
		assertEquals(productRequestBodyToUpdate.get("packing"), product.packing);

		// Change product's 'inStock' to false
		Map<String, Object> productInStockFalse = new HashMap<>();
		productInStockFalse.put("inStock", false);
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
		for (int i = 0; i < 5; i++) {
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
		mockMvc.perform(get(PRODUCTS_BASE_URL + "?type=" + Product.ProductType.MEAT + "&size=10&page=0"))
				.andExpect(status().isOk())
				.andDo(document("get-products-by-type",
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

	}

	private ProductDTO getProduct(String productId, Map<String, Object> requestBody) throws Exception {

		return objectMapper.readValue(mockMvc.perform(get(URI_WITH_ID_VAR, productId).headers(authHeader(accessToken)))
						.andExpect(status().isOk())
						.andExpect(jsonPath(ID_JSON).value(productId))
						.andExpect(jsonPath(NAME_JSON).value(requestBody.get("name")))
						.andExpect(jsonPath(SUB_NAME_JSON).value(requestBody.get("subName")))
						.andExpect(jsonPath(PICTURE_JSON).value(requestBody.get("picture")))
						.andExpect(jsonPath(DESC_JSON).value(requestBody.get("description")))
						.andExpect(jsonPath(PICTURE_JSON).value(requestBody.get("picture")))
						.andExpect(jsonPath(HOLD_CONDITIONS_JSON).value(requestBody.get("holdConditions")))
						.andExpect(jsonPath(PACK_NUMBER_JSON).value(requestBody.get("packNumber")))
						.andExpect(jsonPath(PACKING_JSON).value(requestBody.get("packing")))
						.andReturn().getResponse().getContentAsString(),
				ProductDTO.class);
	}
}
