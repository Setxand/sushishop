package com.sushishop.client;

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


	public void sendResetPasswordEmail(String message, String email) {
		sendMessage(message, email, "Recover your password!");
	}


	public void sendEmailToAdmin(String emailMessage) {
		sendMessage(emailMessage, recipientEmail, "New order!");
	}

	public void sendMessage(String emailMessage, String email, String subject) {
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		try {
			message.setContent(emailMessage, "text/plain");
			helper.setSubject(subject);
			helper.setTo(email);
			emailSender.send(message);
		} catch (MessagingException e) {
			logger.warn("failed to send email message", e);
			throw new RuntimeException(e);
		}
	}
}
