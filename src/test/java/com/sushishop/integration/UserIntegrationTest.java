package com.sushishop.integration;

import com.sushishop.TestUtil;
import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.User;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest extends BaseIntegrationTest {

	private static final String BY_ID = USERS_BASE_URL + "/{userId}";
	private static final String EMAIL_JSON_PATH = "$.email";
	private static final String PHONE_JSON_PATH = "$.phone";

	@Autowired UserService userService;

	@Test
	@Sql("classpath:clean.sql")
	public void userIntegrationTest() throws Exception {

		// Create new user
		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		User user = userService.getUser(jwtResponse.getUserId());


		// Get User by id
		UserDTO userRequest = getUserRequest(jwtResponse.getUserId());
		assertEquals(jwtResponse.getUserId(), userRequest.id);
		assertEquals(user.getEmail(), userRequest.email);
		assertEquals(user.getPhone(), userRequest.phone);
		assertEquals(user.getName(), userRequest.name);

		// Add address to user
		AddressDTO addressDTO = TestUtil.createAddressDTO();
		addTestAddressToUser(jwtResponse.getUserId(), addressDTO);

		// Get user and check address
		userRequest = getUserRequest(jwtResponse.getUserId());
		assertEquals(addressDTO.city, userRequest.address.city);
		assertEquals(addressDTO.entrance, userRequest.address.entrance);
		assertEquals(addressDTO.floor, userRequest.address.floor);
		assertEquals(addressDTO.house, userRequest.address.house);
		assertEquals(addressDTO.housing, userRequest.address.housing);
		assertEquals(addressDTO.roomNumber, userRequest.address.roomNumber);
		assertEquals(addressDTO.street, userRequest.address.street);
		assertEquals(jwtResponse.getUserId(), userRequest.address.id);

	}

	private UserDTO getUserRequest(String userId) throws Exception {
		String jsonResponse = mockMvc.perform(get(BY_ID, userId)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(jsonResponse, UserDTO.class);
	}

}
