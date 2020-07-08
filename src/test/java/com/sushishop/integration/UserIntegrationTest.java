package com.sushishop.integration;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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

		User user = userService.getUser(jwtResponse.getUserId());


		// Get User by id
		String jsonResponse = mockMvc.perform(get(BY_ID, jwtResponse.getUserId())
				.headers(authHeader(jwtResponse.getAccessToken())))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(NAME_JSON_PATH).value(user.getName()))
				.andExpect(MockMvcResultMatchers.jsonPath(ID_JSON_PATH).value(user.getId()))
				.andExpect(MockMvcResultMatchers.jsonPath(EMAIL_JSON_PATH).value(user.getEmail()))
				.andExpect(MockMvcResultMatchers.jsonPath(PHONE_JSON_PATH).value(user.getPhone()))
				.andReturn().getResponse().getContentAsString();

		UserDTO dto = objectMapper.readValue(jsonResponse, UserDTO.class);
	}

}
