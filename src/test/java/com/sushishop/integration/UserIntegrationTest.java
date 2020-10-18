package com.sushishop.integration;

import com.sushishop.TestUtil;
import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.LoginRequestDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.User;
import com.sushishop.security.JwtTokenUtil;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-sources/snippets")
public class UserIntegrationTest extends BaseIntegrationTest {

	private static final String BY_ID = USERS_BASE_URL + "/{userId}";
	private static final String ADD_ADDR_TO_USER_URL = "/v1/users/{userId}/addresses";
	private static final String ADD_ADDR_TO_USER_DOC = "user-add-address";
	private static final String NEW_PASSWORD = "12345";
	private static final String PASSWORD_KEY = "password";
	private static final String UPDATE_USER_PASSWORD_URL = "/v1/users/passwords";
	private static final String UPDATE_USER_PASSWORD_DOC = "change-user-password";

	@Autowired UserService userService;
	@Autowired JwtTokenUtil jwtTokenUtil;

	@Test
	@Sql("classpath:clean.sql")
	public void userIntegrationTest() throws Exception {

		// Create new user
		JwtResponse jwtResponse = signUpRequest();
		accessToken = jwtResponse.getAccessToken();

		User user = userService.getUser(jwtResponse.getUserId());


		// Get User by id
		UserDTO userRequest = objectMapper.readValue(mockMvc.perform(get(BY_ID, jwtResponse.getUserId())
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andDo(document("get-user", preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), UserDTO.class);

		assertEquals(jwtResponse.getUserId(), userRequest.id);
		assertEquals(user.getEmail(), userRequest.email);
		assertEquals(user.getPhone(), userRequest.phone);
		assertEquals(user.getName(), userRequest.name);

		// Add address to user
		AddressDTO addressDTO = TestUtil.createAddressDTO();
		mockMvc.perform(put(ADD_ADDR_TO_USER_URL, jwtResponse.getUserId())
				.contentType(MediaType.APPLICATION_JSON)
				.headers(authHeader(accessToken))
				.content(objectMapper.writeValueAsString(convertToCorrectMap(addressDTO))))
				.andExpect(status().isOk())
				.andDo(document(ADD_ADDR_TO_USER_DOC, preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint())));

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


		// Update user (change password) error
		Map<String, Object> changePasswordMap = new HashMap<>();
		changePasswordMap.put(PASSWORD_KEY, NEW_PASSWORD);

		mockMvc.perform(patch(UPDATE_USER_PASSWORD_URL)
				.headers(authHeader(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordMap)))
				.andExpect(status().is4xxClientError());

		// Update user (change password)
		String changePasswordToken = jwtTokenUtil
				.generateToken(user.getId(), user.getEmail(), JwtTokenUtil.TokenType.RESET_PASSWORD);

		mockMvc.perform(patch(UPDATE_USER_PASSWORD_URL)
				.headers(authHeader(changePasswordToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordMap)))
				.andExpect(status().isOk())
				.andDo(document(UPDATE_USER_PASSWORD_DOC, preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())));


		// Login with new credentials

		LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
		loginRequestDTO.email = user.getEmail();
		loginRequestDTO.password = NEW_PASSWORD;

		signInRequest(user.getId(), loginRequestDTO);
	}

	private UserDTO getUserRequest(String userId) throws Exception {
		String jsonResponse = mockMvc.perform(get(BY_ID, userId)
				.headers(authHeader(accessToken)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(jsonResponse, UserDTO.class);
	}

}
