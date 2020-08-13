package com.sushishop.controller;

import com.sushishop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class PaymentController {


	@Autowired PaymentService paymentService;

	@PostMapping("/v1/payments/webhook")
	public void updatePaymentStatus(@RequestParam String data, @RequestParam String signature) {
		paymentService.updatePaymentStatus(data, signature);
	}

	@GetMapping("/v1/users/{userId}/payments")
	public String checkout(@PathVariable String userId) {
		return paymentService.checkout(userId);
	}
}
