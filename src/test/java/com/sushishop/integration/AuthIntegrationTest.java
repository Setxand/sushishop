package com.sushishop.integration;

import com.sushishop.client.EmailClient;
import com.sushishop.dto.LoginRequestDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.User;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.sushishop.TestUtil.createUserDTO;
import static org.junit.Assert.assertTrue;
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


	private static final String REFRESH_TOK_URL = "/refresh-token";
	private static final String FORGOT_PASS_URL = "/forgot-password?email=";


	private static final String CODE_JSON = "$.code";

	private static final String SIGNUP_OK_DOC = "signup-ok";
	private static final String LOGIN_FORBIDDEN_DOC = "login-forbidden";
	private static final String REFRESH_TOK_DOC = "refresh-token-ok";
	private static final String FORGOT_PASS_DOC = "forgot-password";

	private static final String ACCESS_DENIED_CODE = "ACCESS_DENIED";
	private static final String UPDATE_USER_NEW_PASSWORD = "2342dfs$";
	private static final String GENERATED_URL_MESSAGE = "Your url to change password: ";

	@Autowired UserService userService;
	@MockBean EmailClient emailClient;

	@Value("${ui.forgotpass.url}") String forgotPassUrl;

	@Test
	@Sql("classpath:clean.sql")
	public void userIntegrationTest() throws Exception {


		// User Register
		UserDTO newUserInput = createUserDTO();
		newUserInput.password = UPDATE_USER_NEW_PASSWORD;
		newUserInput.id = null;
		JwtResponse jwtResponse = objectMapper.readValue(mockMvc.perform(postRequestWithUrl(SIGNUP_URL, newUserInput))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath(ACCESS_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(REFRESH_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).isNotEmpty())
				.andDo(document(SIGNUP_OK_DOC, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
				.andReturn().getResponse().getContentAsString(), JwtResponse.class);

		User user = userService.getUser(jwtResponse.getUserId());

		// Login
		LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
		loginRequestDTO.email = user.getEmail();
		loginRequestDTO.password = newUserInput.password;

		signInRequest(jwtResponse.getUserId(), loginRequestDTO);

		// Login with invalid credentials
		loginRequestDTO.password = loginRequestDTO.password + "@";
		mockMvc.perform(postRequestWithUrl(LOGIN_URL, loginRequestDTO))
				.andExpect(status().isForbidden())
				.andExpect(MockMvcResultMatchers.jsonPath(CODE_JSON).value(ACCESS_DENIED_CODE))
				.andDo(document(LOGIN_FORBIDDEN_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Refresh token
		mockMvc.perform(get(REFRESH_TOK_URL)
				.headers(authHeader(jwtResponse.getRefreshToken())))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath(ACCESS_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(REFRESH_TOK_JSON).isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath(USER_ID_JSON).value(jwtResponse.getUserId()))
				.andDo(document(REFRESH_TOK_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		// Forgot password
		mockMvc.perform(post(FORGOT_PASS_URL + user.getEmail()))
				.andExpect(status().isOk())
				.andDo(document(FORGOT_PASS_DOC,
						preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));

		ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(emailClient)
				.sendResetPasswordEmail(stringArgumentCaptor.capture(), ArgumentMatchers.eq(user.getEmail()));

		String partOfGeneratedUrl = GENERATED_URL_MESSAGE + forgotPassUrl;
		assertTrue(stringArgumentCaptor.getValue().startsWith(partOfGeneratedUrl));
	}
}
