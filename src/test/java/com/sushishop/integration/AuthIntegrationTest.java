package com.sushishop.integration;

import com.sushishop.client.EmailClient;
import com.sushishop.dto.LoginRequestDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.User;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.sushishop.TestUtil.createUserDTO;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-sources/snippets")
public class AuthIntegrationTest extends BaseIntegrationTest {

	@Autowired UserService userService;
	@MockBean EmailClient emailClient;

	@Test
	@Sql("classpath:clean.sql")
	public void userIntegrationTest() throws Exception {


		// User Register
		UserDTO newUserInput = createUserDTO();
		newUserInput.password = "2342dfs$";
		newUserInput.id = null;
		JwtResponse jwtResponse = objectMapper.readValue(mockMvc.perform(postRequestWithUrl("/signup", newUserInput))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.userId").isNotEmpty())
				.andDo(document("signup-ok", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), JwtResponse.class);

		User user = userService.getUser(jwtResponse.getUserId());

		// Login
		LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
		loginRequestDTO.email = user.getEmail();
		loginRequestDTO.password = newUserInput.password;

		signInRequest(jwtResponse.getUserId(), loginRequestDTO);

		// Login with invalid credentials
		loginRequestDTO.password = loginRequestDTO.password + "@";
		mockMvc.perform(postRequestWithUrl("/login", loginRequestDTO))
				.andExpect(status().isForbidden())
				.andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ACCESS_DENIED"))
				.andDo(document("login-forbidden",
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Refresh token
		mockMvc.perform(get("/refresh-token")
				.header("Authorization", "Bearer " + jwtResponse.getRefreshToken()))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(jwtResponse.getUserId()))
				.andDo(document("refresh-token-ok",
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Forgot password
		mockMvc.perform(post("/forgot-password").param("email", user.getEmail()))
				.andExpect(status().isOk())
				.andDo(document("forgot-password",
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

//todo		Mockito.verify(emailClient).sendMessage();
	}
}
