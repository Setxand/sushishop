package com.sushishop.client;

import com.sushishop.dto.OrderDTO;
import com.sushishop.dto.UserDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class EmailClient {

	private final JavaMailSender emailSender;
	private final String recipientEmail;

	private static final Logger logger = Logger.getLogger(EmailClient.class);

	public EmailClient(JavaMailSender emailSender,@Value("${spring.mail.username}") String recipientEmail) {
		this.emailSender = emailSender;
		this.recipientEmail = recipientEmail;
	}


	public void sendEmail(OrderDTO order, UserDTO user) {
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		String htmlMsg = user.toString() + order.toString();

		try {
//			message.setContent(htmlMsg, "text/html; charset=utf-8");
			message.setContent(htmlMsg, "text/plain");
			helper.setSubject("New order!");
			helper.setTo(recipientEmail);
			emailSender.send(message);
		} catch (MessagingException e) {
			logger.warn("failed to send email message", e);
			throw new RuntimeException(e);
		}
	}
}
