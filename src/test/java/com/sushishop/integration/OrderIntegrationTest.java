package com.sushishop.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest extends BaseIntegrationTest {

	@Test
	public void orderIntegrationTest() throws Exception {


		// Create order (Add address data and something else)
//		OrderDTO orderDTO;
//		mockMvc.perform(postRequest(orderDTO))
//				.andExpect(status().isCreated());
		// Get order
		// Checkout
	}
}
