package com.sushishop.integration;

import com.sushishop.dto.LoginRequestDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.User;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.sushishop.TestUtil.createUserDTO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest extends BaseIntegrationTest {

	@Autowired UserService userService;

	@Test
	public void userIntegrationTest() throws Exception {


		// User Register
		JwtResponse jwtResponse = signUpRequest();
		User user = userService.getUser(jwtResponse.getUserId());

		// Login
		LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
		loginRequestDTO.email = user.getEmail();
		loginRequestDTO.password = user.getPassword();

		mockMvc.perform(postRequestWithUrl("/login", loginRequestDTO))
					.andExpect(status().isAccepted())
					.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty())
					.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
					.andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(jwtResponse.getUserId()));

		// Login with invalid credentials
		loginRequestDTO.password = loginRequestDTO.password + "@";
		mockMvc.perform(postRequestWithUrl("/login", loginRequestDTO))
				.andExpect(status().isForbidden())
				.andExpect(MockMvcResultMatchers.jsonPath("$.code").value("AUTH_FAILED"));

		// Refresh token
		mockMvc.perform(get("/refresh-token")
				.header("Authorization", "Bearer " + jwtResponse.getRefreshToken()))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(jwtResponse.getUserId()));
	}
}
